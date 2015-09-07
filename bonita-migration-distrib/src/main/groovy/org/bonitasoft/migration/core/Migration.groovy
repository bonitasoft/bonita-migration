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
/**
 *
 * Get all versions and steps to execute and launch the migration runner with it
 *
 * @author Baptiste Mesta
 */
class Migration {

    Logger logger = new Logger()
    MigrationContext context

    public static void main(String[] args) {
        new Migration().run(false)
    }


    public void run(boolean isSp) {
        context = new MigrationContext(logger: logger);
        context.loadProperties()
        setupOutputs()
        printWarning()
        println "using db vendor: " + context.dbVendor
        println "using db url: " + context.dburl

        def versionMigrations = getMigrationVersionsToRun()
        def runner = new MigrationRunner(versionMigrations: versionMigrations, context: context, logger: logger)
        runner.run(isSp)
    }

    /**
     * get a version as string and return the class of the migration step
     */
    def Closure toVersionMigrationInstance = { String it ->
        def versionUnderscored = it.replace(".", "_")
        def versionMigrationClass
        def className = "com.bonitasoft.migration.version.to${versionUnderscored}.MigrateTo$versionUnderscored"
        try {
            logger.debug("Trying to find " + className)
            versionMigrationClass = Thread.currentThread().contextClassLoader.loadClass(className)
        } catch (ClassNotFoundException ignored) {
            logger.debug("Unable to find " + className)
            versionMigrationClass = Thread.currentThread().contextClassLoader.loadClass("org.bonitasoft.migration.version.to${versionUnderscored}.MigrateTo$versionUnderscored")
        }
        return versionMigrationClass.newInstance(version: it, logger: logger)
    }


    def List<VersionMigration> getMigrationVersionsToRun() {
        String platformVersionInBonitaHome = getBonitaHomeVersion()
        verifyPlatformIsValid(platformVersionInBonitaHome)
        context.sourceVersion = platformVersionInBonitaHome
        def versions = getVersionsAfter(platformVersionInBonitaHome)
        if (context.targetVersion == null) {
            println "enter the target version"
            context.targetVersion = MigrationUtil.askForOptions(versions)
        }
        verifyTargetVersionIsValid(versions)
        return getVersionsToExecute(versions)
    }

    def verifyPlatformIsValid(String platformVersionInBonitaHome) {
        String platformVersionInDatabase = getPlatformVersion()
        if (!platformVersionInDatabase.startsWith("7")) {
            logger.error("sorry the but this tool can't manage version under 7.0.0")
            throw new IllegalStateException("sorry the but this tool can't manage version under 7.0.0")

        } else {
            // >=7.0.0
            if (platformVersionInBonitaHome != platformVersionInDatabase) {
                //invalid case: given source (if any) not the same as version in db and as version in bonita home
                logger.error("The versions are not consistent:")
                logger.error("The version of the database is ${platformVersionInDatabase}")
                logger.error("The version of the bonita home is ${platformVersionInBonitaHome}")
                logger.error("Check that you configuration is correct and restart the migration")
                throw new IllegalStateException("Versions are not consistent, see logs")
            }
        }
    }

    private String getPlatformVersion() {
        return MigrationUtil.getPlatformVersion(context.dburl, context.dbUser, context.dbPassword, context.dbDriverClassName)
    }

    private String getBonitaHomeVersion() {
        def s = File.separator
        def File versionFile = new File(context.bonitaHome, "engine-server${s}work${s}platform${s}VERSION")
        if (!versionFile.exists()) {
            throw new IllegalStateException("The bonita home does not exists or is not consistent, the file $versionFile.path does not exists")
        }
        return versionFile.text
    }

    def verifyTargetVersionIsValid(List<String> versions) {
        def targetVersionIndex = versions.indexOf(context.targetVersion)
        def sourceVersionIndex = versions.indexOf(context.sourceVersion)
        if (!(targetVersionIndex > sourceVersionIndex) && sourceVersionIndex >= 0) {
            throw new IllegalStateException("the target version $context.targetVersion is not a valid version or is not reachable from $context.sourceVersion")
        }
    }

    private List<VersionMigration> getVersionsToExecute(List<String> versions) {
        return versions.subList(versions.indexOf(context.sourceVersion) + 1, versions.indexOf(context.targetVersion) + 1).collect(toVersionMigrationInstance) as List<VersionMigration>
    }

    private List<String> getVersionsAfter(String sourceVersion) {
        Properties migrationProperties = getMigrationProperties()
        def versionsAsString = migrationProperties.getProperty("versions")
        def allVersions = parseVersionsFromMigrationProperties(versionsAsString)
        def indexOfSourceVersion = allVersions.indexOf(sourceVersion)
        if (indexOfSourceVersion == -1) {
            throw new IllegalStateException("Sorry the version $sourceVersion can not be migrated using this migration tool")
        }
        return allVersions.subList(indexOfSourceVersion + 1, allVersions.size())
    }

    private List<String> parseVersionsFromMigrationProperties(String versionsAsString) {
        return versionsAsString.substring(1, versionsAsString.length() - 1).split(",").collect { it.trim() }
    }

    private Properties getMigrationProperties() {
        def migrationProperties = new Properties()
        this.class.getResourceAsStream("/bonita-versions.properties").withStream {
            migrationProperties.load(it)
        }
        return migrationProperties
    }


    static def setupOutputs() {
        def logInFile = new FileOutputStream(new File("migration-" + new Date().format("yyyy-MM-dd-HHmmss") + ".log"))
        System.setOut(new PrintStream(new SplitPrintStream(System.out, logInFile)))
        System.setErr(new PrintStream(new SplitPrintStream(System.err, logInFile)))
    }


    static def printWarning() {
        println ''
        IOUtil.printInRectangle("", "Bonita BPM migration tool", "",
                "This tool will migrate your installation of Bonita BPM.",
                "Both database and bonita home will be modified.",
                "Please refer to the documentation for further steps to completely migrate your production environment.",
                "",
                "Warning:",
                "Back up the database AND the bonita home before migrating",
                "If you have a custom Look & Feel, test and update it, if it's necessary when the migration is finished.",
                "If you have customized the configuration of your bonita home, reapply the customizations when the migration is finished.", "")
    }
}
