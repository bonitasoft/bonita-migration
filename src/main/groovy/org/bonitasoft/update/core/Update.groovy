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

package org.bonitasoft.update.core

import com.github.zafarkhaja.semver.Version

import static org.bonitasoft.update.core.UpdateUtil.getDisplayVersion

/**
 *
 * Get all versions and steps to execute and launch the update runner with it
 *
 * @author Baptiste Mesta
 */
class Update {

    // This list contains the steps in which we cannot stop.
    // Update will execute steps but slide until next version:
    // It's currently empty but add the version in the list if needed
    public static List<Version> TRANSITION_VERSIONS = [].collect {
        Version.valueOf(it)
    }
    private static final Logger logger = new Logger()
    private UpdateContext context
    private DisplayUtil displayUtil

    // for testing only
    Update(UpdateContext context, DisplayUtil displayUtil) {
        this.context = context
        this.displayUtil = displayUtil
    }

    static void main(String[] args) {
        startApplication(args, false)
    }

    static void startApplication(String[] args, boolean isSp) {
        def arguments = parseArguments(args)
        try {
            def updateContext = new UpdateContext(logger: logger)
            updateContext.verifyOnly = arguments.verify
            new Update(
                    updateContext,
                    new DisplayUtil(logger: logger)
            ).run(isSp)
        } catch (Throwable ignored) {
            // logs managed in the run method
            System.exit(-1)
        }
    }

    private static UpdateArguments parseArguments(String[] args) {
        try {
            UpdateArguments arguments = UpdateArguments.parse(args)
            if (arguments.printHelp) {
                UpdateArguments.printHelp()
                System.exit(-1)
            }
            return arguments
        } catch (Exception e) {
            logger.error("Invalid command line: " + e.getMessage())
            UpdateArguments.printHelp()
            System.exit(-1)
        }
    }

    void run(boolean isSp) {
        try {
            def runner = createRunner()
            context.start()
            logUpdateBannerAndGlobalWarnings(isSp, runner)
            logJvmInformation()
            context.loadConfiguration()
            logJdbcDriverInformation()

            connectToDatabase()
            try {
                def versionUpdates = getVersionUpdatesToRun(runner)
                if (versionUpdates.empty) {
                    return
                }
                runner.versionUpdates = versionUpdates
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

    protected UpdateAction createRunner() {
        if (context.verifyOnly) {
            return new UpdateVerifier(context: context, logger: logger, displayUtil: displayUtil)
        } else {
            return new UpdateRunner(context: context, logger: logger, displayUtil: displayUtil)
        }
    }

    /**
     * get a version as string and return the class of the update step
     */
    Closure toVersionUpdateInstance = { Version it ->
        def versionUnderscored = it.toString().replace(".", "_")
        def versionUpdateClass
        def className = "com.bonitasoft.update.version.to${versionUnderscored}.UpdateTo$versionUnderscored"
        try {
            logger.debug("Trying to find " + className)
            versionUpdateClass = Thread.currentThread().contextClassLoader.loadClass(className)
        } catch (ClassNotFoundException ignored) {
            logger.debug("Unable to find " + className)
            versionUpdateClass = Thread.currentThread().contextClassLoader.loadClass("org.bonitasoft.update.version.to${versionUnderscored}.UpdateTo$versionUnderscored")
        }
        logger.debug("Using " + versionUpdateClass)
        return versionUpdateClass.newInstance(version: it, logger: logger)
    }

    private void connectToDatabase() {
        def dbVendor = context.dbVendor
        logger.info "Gathering Database Information"
        context.openSqlConnection()

        List<String> databaseInformation = UpdateUtil.getDatabaseInformation(context.sql, dbVendor)
        if (!databaseInformation.empty) {
            logger.info 'Database Information'
            databaseInformation.each { logger.info "  ${it}" }
        }
    }

    List<VersionUpdate> getVersionUpdatesToRun(UpdateAction runner) {
        def version = Version.valueOf(getPlatformVersion().normalVersion)
        logger.info("Detected version in database: ${getDisplayVersion(version)}")
        verifyPlatformIsValid(version)
        context.sourceVersion = version
        def versions = getVersionsAfter(version)
        def visibleVersions = filterOutInvisibleVersions(versions)
        if (visibleVersions.empty) {
            logger.warn("Your Bonita version is already the latest supported version. Nothing to update.")
            return []
        }
        logger.info(runner.getDescription())
        if (context.targetVersion == null) {
            logger.info "Enter the target version"
            context.targetVersion = Version.valueOf(UpdateUtil.askForOptions(visibleVersions.collect {
                it.toString()
            }))
        }
        verifyTargetVersionIsValid(visibleVersions)
        return getVersionsToExecute(versions)
    }

    static List<Version> filterOutInvisibleVersions(List<Version> versions) {
        versions.findAll { !TRANSITION_VERSIONS.contains(it) }
    }

    def verifyPlatformIsValid(Version platformVersionInDatabase) {
        if (platformVersionInDatabase.majorVersion < 7
                || ( platformVersionInDatabase.majorVersion == 7 && platformVersionInDatabase.minorVersion < 10 )) {
            // Should we rename also migration tool v2 to update tool v2?
            throw new IllegalStateException("Sorry, but this tool can't manage version before 7.10.0, use the migration tool version 2")
        }
    }

    private Version getPlatformVersion() {
        return UpdateUtil.getPlatformVersion(context.sql)
    }

    def verifyTargetVersionIsValid(List<Version> possibleTarget) {
        if (context.targetVersion < context.sourceVersion) {
            throw new IllegalStateException("The target version $context.targetVersion can not be before source version ${getDisplayVersion(context.sourceVersion)}")
        }
        if (context.targetVersion == context.sourceVersion) {
            throw new IllegalStateException("The version is already in ${getDisplayVersion(context.sourceVersion)}")
        }
        if (!possibleTarget?.contains(context.targetVersion)) {
            if (TRANSITION_VERSIONS.contains(context.targetVersion)) {
                if (System.getProperty("ignore.invalid.target.version") != null) {
                    // only accept this hidden sysprop "ignore.invalid.target.version" if the targetVersion is in the list of invisible transition versions:
                    logger.info("Ignoring normally-forbidden target version $context.targetVersion (for tests only)")
                } else {
                    throw new IllegalStateException("Updating to version $context.targetVersion is forbidden. Please choose a more recent version")
                }
            } else {
                throw new IllegalStateException("$context.targetVersion is not yet handled by this version of the update tool")
            }
        }
    }

    private List<VersionUpdate> getVersionsToExecute(List<Version> versions) {
        return versions.subList(versions.indexOf(context.sourceVersion) + 1, versions.indexOf(context.targetVersion) + 1).collect(toVersionUpdateInstance) as List<VersionUpdate>
    }

    private List<Version> getVersionsAfter(Version sourceVersion) {
        Properties updateProperties = getUpdateProperties()
        def versionsAsString = updateProperties.getProperty("versions")
        def allVersions = parseVersionsFromUpdateProperties(versionsAsString)
        def indexOfSourceVersion = allVersions.indexOf(sourceVersion)
        if (indexOfSourceVersion == -1) {
            throw new IllegalStateException("Sorry the version $sourceVersion can not be updated using this update tool")
        }
        return allVersions.subList(indexOfSourceVersion + 1, allVersions.size())
    }

    private static List<Version> parseVersionsFromUpdateProperties(String versionsAsString) {
        return versionsAsString.substring(1, versionsAsString.length() - 1).split(",").collect {
            it.trim()
        }.collect {
            Version.valueOf(it)
        }
    }

    private Properties getUpdateProperties() {
        return loadFromClasspath("/bonita-versions.properties")
    }

    private Properties getProjectProperties() {
        return loadFromClasspath("/bonita-update-info.properties")
    }

    private Properties loadFromClasspath(String name) {
        def properties = new Properties()
        this.class.getResourceAsStream(name).withStream {
            properties.load(it)
        }
        return properties
    }

    def logUpdateBannerAndGlobalWarnings(boolean isSp, UpdateAction runner) {
        def updateToolVersion = getProjectProperties().getProperty("update.tool.version", "DEV")
        def banner = (["", "Bonita update tool ${updateToolVersion} ${Edition.from(isSp).displayName} edition", ""] +
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
