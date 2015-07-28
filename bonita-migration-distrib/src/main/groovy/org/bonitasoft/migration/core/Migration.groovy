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
        new Migration().run()
    }


    public void run() {
        context = new MigrationContext();
        setupOutputs()
        printWarning()
        println "using db vendor: " + context.dbVendor
        println "using db url: " + context.dburl
        println "migrate from version ${context.sourceVersion} to version ${context.targetVersion}"

        def versionMigrations = getMigrationVersionsToRun()
        def runner = new MigrationRunner(versionMigrations: versionMigrations, context: context, logger: logger)
        runner.run()
    }

    /**
     * get a version as string and return the class of the migration step
     */
    def Closure toVersionMigrationInstance = { String it ->
        def versionUnderscored = it.replace(".", "_")
        def versionMigrationClass = Class.forName("org.bonitasoft.migration.version.to${versionUnderscored}.MigrateTo$versionUnderscored")
        return versionMigrationClass.newInstance(version: it, logger: logger)
    }


    def verifySourceVersionIsValid(List<String> versions) {
        if (!versions.contains(context.sourceVersion)) {
            throw new IllegalStateException("The source version $context.sourceVersion is not valid")
        }
        //get version in sources
        def String platformVersionInDatabase = MigrationUtil.getPlatformVersion(context.dburl, context.dbUser, context.dbPassword, context.dbDriverClassName)
        def String platformVersionInBonitaHome = MigrationUtil.getBonitaVersionFromBonitaHome(context)
        if (!checkSourceVersion(platformVersionInDatabase, platformVersionInBonitaHome, context.sourceVersion)) {
            throw new IllegalStateException("Your installation is not consistent, verify all parameters are correctly set")
        }
    }

    def verifyTargetVersionIsValid(List<String> versions) {
        if (!versions.contains(context.targetVersion)) {
            throw new IllegalStateException("the target version $context.targetVersion is not a valid version")
        }
        if (versions.indexOf(context.sourceVersion) >= versions.indexOf(context.targetVersion)) {
            throw new IllegalStateException("the target version $context.targetVersion must be after the source version $context.sourceVersion")

        }
    }

    def List<VersionMigration> getMigrationVersionsToRun() {
        def versions = getAllVersions()
        verifySourceVersionIsValid(versions);
        verifyTargetVersionIsValid(versions)
        return getVersionsToExecute(versions)
    }

    private List<VersionMigration> getVersionsToExecute(List<String> versions) {
        return versions.subList(versions.indexOf(context.sourceVersion) + 1, versions.indexOf(context.targetVersion) + 1).collect(toVersionMigrationInstance) as List<VersionMigration>
    }

    private List<String> getAllVersions() {
        Properties migrationProperties = getMigrationProperties()
        def versionsAsString = migrationProperties.getProperty("versions")
        return parseVersionsFromMigrationProperties(versionsAsString)
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


    String checkSourceVersion(String platformVersionInDatabase, String platformVersionInBonitaHome, String givenSourceVersion) {
        if (!platformVersionInDatabase.startsWith("7")) {
            println "sorry the but this tool can't manage version under 7.0.0"
            return null;

        } else {
            // >=7.0.0
            if (platformVersionInBonitaHome != platformVersionInDatabase || (givenSourceVersion != null && !platformVersionInDatabase.startsWith(givenSourceVersion))) {
                //invalid case: given source (if any) not the same as version in db and as version in bonita home
                println "The versions are not consistent:"
                println "The version of the database is $platformVersionInDatabase"
                println "The version of the bonita home is $platformVersionInBonitaHome"
                if (givenSourceVersion != null) {
                    println "The declared version is $givenSourceVersion"
                }
                println "Check that you configuration is correct and restart the migration"
                return null;
            }
            return givenSourceVersion != null ? givenSourceVersion : platformVersionInBonitaHome
        }
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
