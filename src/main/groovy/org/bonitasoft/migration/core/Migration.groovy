/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.migration.core

import com.github.zafarkhaja.semver.Version

/**
 *
 * Get all versions and steps to execute and launch the migration runner with it
 *
 * @author Baptiste Mesta
 */
class Migration {

    public static final Version FIRST_VERSION_WITHOUT_BONITA_HOME = Version.valueOf('7.3.0')

    // This list contains the steps in which we cannot stop. Migration will execute steps but slide until next version:
    public static List<Version> TRANSITION_VERSIONS = ['7.7.0', '7.8.0', '7.8.1', '7.8.2'].collect {
        Version.valueOf(it)
    }
    private static final Logger logger = new Logger()
    private MigrationContext context
    private DisplayUtil displayUtil

    // for testing only
    Migration(MigrationContext context, DisplayUtil displayUtil) {
        this.context = context
        this.displayUtil = displayUtil
    }

    static void main(String[] args) {
        startApplication(args, false)
    }

    static void startApplication(String[] args, boolean isSp) {
        def arguments = parseArguments(args)
        try {
            def migrationContext = new MigrationContext(logger: logger)
            migrationContext.verifyOnly = arguments.verify
            migrationContext.updateCaseOverview = arguments.updateCaseOverview
            migrationContext.processToUpdate = arguments.processToUpdate
            new Migration(
                    migrationContext,
                    new DisplayUtil(logger: logger)
            ).run(isSp)
        } catch (Throwable ignored) {
            // logs managed in the run method
            System.exit(-1)
        }
    }

    private static MigrationArguments parseArguments(String[] args) {
        try {
            MigrationArguments arguments = MigrationArguments.parse(args)
            if (arguments.printHelp) {
                MigrationArguments.printHelp()
                System.exit(-1)
            }
            return arguments
        } catch (Exception e) {
            logger.error("Invalid command line: " + e.getMessage())
            MigrationArguments.printHelp()
            System.exit(-1)
        }
    }

    void run(boolean isSp) {
        try {
            def runner = createRunner()
            context.start()
            logMigrationBannerAndGlobalWarnings(isSp, runner)
            logJvmInformation()
            context.loadConfiguration()
            logJdbcDriverInformation()

            connectToDatabase()
            try {
                if (runner instanceof UpdateV6CaseOverview) {
                    if (Version.valueOf(getPlatformVersion().normalVersion) >= Version.valueOf('7.8.0')) {
                        throw new IllegalStateException("'updateCaseOverview' mode is designed to migrate V6 case overviews to V7 auto-generated case overviews. It is not necessary for versions >= 7.8.0 as no V6 forms may be present in them.")
                    }
                } else {
                    def versionMigrations = getMigrationVersionsToRun(runner)
                    runner.migrationVersions = versionMigrations
                }
                runner.run(isSp)
            }
            finally {
                context.closeSqlConnection()
            }
        } catch (Throwable t) {
            logger.error(t.getMessage())
            logger.debug('', t)
            throw t
        }
    }

    protected MigrationAction createRunner() {
        if (context.verifyOnly) {
            return new MigrationVerifier(context: context, logger: logger, displayUtil: displayUtil)
        } else if (context.updateCaseOverview) {
            return new UpdateV6CaseOverview(context: context, logger: logger, displayUtil: displayUtil, processDefinition: new Long(context.processToUpdate))
        } else {
            return new MigrationRunner(context: context, logger: logger, displayUtil: displayUtil)
        }
    }

    /**
     * get a version as string and return the class of the migration step
     */
    Closure toVersionMigrationInstance = { Version it ->
        def versionUnderscored = it.toString().replace(".", "_")
        def versionMigrationClass
        def className = "com.bonitasoft.migration.version.to${versionUnderscored}.MigrateTo$versionUnderscored"
        try {
            logger.debug("Trying to find " + className)
            versionMigrationClass = Thread.currentThread().contextClassLoader.loadClass(className)
        } catch (ClassNotFoundException ignored) {
            logger.debug("Unable to find " + className)
            versionMigrationClass = Thread.currentThread().contextClassLoader.loadClass("org.bonitasoft.migration.version.to${versionUnderscored}.MigrateTo$versionUnderscored")
        }
        logger.debug("Using " + versionMigrationClass)
        return versionMigrationClass.newInstance(version: it, logger: logger)
    }

    private void connectToDatabase() {
        def dbVendor = context.dbVendor
        logger.info "Gathering Database Information"
        context.openSqlConnection()

        List<String> databaseInformation = MigrationUtil.getDatabaseInformation(context.sql, dbVendor)
        if (!databaseInformation.empty) {
            logger.info 'Database Information'
            databaseInformation.each { logger.info "  ${it}" }
        }
    }

    List<VersionMigration> getMigrationVersionsToRun(MigrationAction runner) {
        def version = Version.valueOf(getPlatformVersion().normalVersion)
        verifyPlatformIsValid(version)
        logger.info("Detected version in database: " + version)
        if (version < FIRST_VERSION_WITHOUT_BONITA_HOME) {
            verifyPlatformIsTheSameInBonitaHome(version)
        }
        context.sourceVersion = version
        def versions = getVersionsAfter(version)
        def visibleVersions = filterOutInvisibleVersions(versions)
        logger.info(runner.getDescription())
        if (context.targetVersion == null) {
            logger.info "Enter the target version"
            context.targetVersion = Version.valueOf(MigrationUtil.askForOptions(visibleVersions.collect {
                it.toString()
            }))
        }
        verifyTargetVersionIsValid(visibleVersions)
        return getVersionsToExecute(versions)
    }

    static List<Version> filterOutInvisibleVersions(List<Version> versions) {
        versions.findAll { !TRANSITION_VERSIONS.contains(it) }
    }

    def verifyPlatformIsTheSameInBonitaHome(Version version) {
        if (!version.equals(getBonitaHomeVersion())) {
            //invalid case: given source (if any) not the same as version in db and as version in bonita home
            logger.error("The versions are not consistent:")
            logger.error("The version of the database is ${version}")
            logger.error("The version of the bonita home is ${getBonitaHomeVersion()}")
            logger.error("Check that you configuration is correct and restart the migration")
            throw new IllegalStateException("Versions are not consistent, see logs")
        }
    }

    def verifyPlatformIsValid(Version platformVersionInDatabase) {
        if (platformVersionInDatabase.majorVersion != 7) {
            throw new IllegalStateException("Sorry, but this tool can't manage version under 7.0.0")
        }
    }

    private Version getPlatformVersion() {
        return MigrationUtil.getPlatformVersion(context.sql)
    }

    private Version getBonitaHomeVersion() {
        if (context.bonitaHome == null) {
            throw new IllegalStateException("The property bonita.home is neither set in system property nor in the configuration file")
        }
        File versionFile = context.bonitaHome.toPath().resolve("engine-server/work/platform/VERSION").toFile()
        if (!versionFile.exists()) {
            throw new IllegalStateException("The bonita home does not exists or is not consistent, the file $versionFile.path does not exists")
        }
        return Version.valueOf(versionFile.text)
    }

    def verifyTargetVersionIsValid(List<Version> possibleTarget) {
        if (context.targetVersion < context.sourceVersion) {
            throw new IllegalStateException("The target version $context.targetVersion can not be before source version ${MigrationUtil.getDisplayVersion(context.sourceVersion)}")
        }
        if (context.targetVersion == context.sourceVersion) {
            throw new IllegalStateException("The version is already in ${MigrationUtil.getDisplayVersion(context.sourceVersion)}")
        }
        if (!possibleTarget?.contains(context.targetVersion)) {
            if (TRANSITION_VERSIONS.contains(context.targetVersion)) {
                if (System.getProperty("ignore.invalid.target.version") != null) {
                    // only accept this hidden sysprop "ignore.invalid.target.version" if the targetVersion is in the list of invisible transition versions:
                    logger.info("Ignoring normally-forbidden target version $context.targetVersion (for tests only)")
                } else {
                    throw new IllegalStateException("Migrating to version $context.targetVersion is forbidden. Please choose a more recent version")
                }
            } else {
                throw new IllegalStateException("$context.targetVersion is not yet handled by this version of the migration tool")
            }
        }
    }

    private List<VersionMigration> getVersionsToExecute(List<Version> versions) {
        return versions.subList(versions.indexOf(context.sourceVersion) + 1, versions.indexOf(context.targetVersion) + 1).collect(toVersionMigrationInstance) as List<VersionMigration>
    }

    private List<Version> getVersionsAfter(Version sourceVersion) {
        Properties migrationProperties = getMigrationProperties()
        def versionsAsString = migrationProperties.getProperty("versions")
        def allVersions = parseVersionsFromMigrationProperties(versionsAsString)
        def indexOfSourceVersion = allVersions.indexOf(sourceVersion)
        if (indexOfSourceVersion == -1) {
            throw new IllegalStateException("Sorry the version $sourceVersion can not be migrated using this migration tool")
        }
        return allVersions.subList(indexOfSourceVersion + 1, allVersions.size())
    }

    private static List<Version> parseVersionsFromMigrationProperties(String versionsAsString) {
        return versionsAsString.substring(1, versionsAsString.length() - 1).split(",").collect {
            it.trim()
        }.collect {
            Version.valueOf(it)
        }
    }

    private Properties getMigrationProperties() {
        return loadFromClasspath("/bonita-versions.properties")
    }

    private Properties getProjectProperties() {
        return loadFromClasspath("/bonita-migration-info.properties")
    }

    private Properties loadFromClasspath(String name) {
        def properties = new Properties()
        this.class.getResourceAsStream(name).withStream {
            properties.load(it)
        }
        return properties
    }

    def logMigrationBannerAndGlobalWarnings(boolean isSp, MigrationAction runner) {
        def migrationToolVersion = getProjectProperties().getProperty("migration.tool.version", "DEV")
        def banner = (["", "Bonita migration tool ${migrationToolVersion} ${Edition.from(isSp).displayName} edition", ""] +
                runner.getBannerAndGlobalWarnings() + [""]) as String[]
        displayUtil.logInfoCenteredInRectangle(banner)
    }

    private logJvmInformation() {
        def sysProps = System.getProperties()
        logger.info "JVM Information"
        logger.info "  java.version ${sysProps['java.version']}"
        logger.info "  java.runtime.version ${sysProps['java.runtime.version']}"
        logger.info "  java.vendor ${sysProps['java.vendor']}"
        logger.info "  java.vm.name ${sysProps['java.vm.name']}"
        logger.info "  java.vm.vendor ${sysProps['java.vm.vendor']}"
        logger.info "  os.name ${sysProps['os.name']}"
        logger.info "  os.arch ${sysProps['os.arch']}"
    }

    private logJdbcDriverInformation() {
        String driverClassName = context.dbConfig?.dbDriverClassName
        if (!driverClassName) {
            // not provided, mainly in tests, so skip
            return
        }
        logger.info "Jdbc Driver Information"
        logger.info "  driver ${driverClassName}"
        def version = Class.forName(driverClassName).getPackage().implementationVersion
        if (!version) {
            // MSSQL Server case
            // We may read the Bundle-Version attribute in the Manifest
            version = 'N/A'
        }
        logger.info "  implementation-version ${version}"
    }

}
