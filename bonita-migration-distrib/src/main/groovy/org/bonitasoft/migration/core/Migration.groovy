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

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
class Migration {

    private String dbVendor
    private def dburl
    private def user
    private def pwd
    private def driverClass
    private def sourceVersion
    private def targetVersion

    public static void main(String[] args) {
        new Migration().run()
    }

    /**
     * get a version as string and return the class of the migration step
     */
    def Closure toVersionMigrationInstance = { String it ->
        def versionUnderscored = it.replace(".", "_")
        def versionMigrationClass = Class.forName("org.bonitasoft.migration.version.to${versionUnderscored}.MigrateTo$versionUnderscored")
        return versionMigrationClass.newInstance()
    }

    public void run() {
        initializeProperties();
        setupOutputs()
        printWarning()
        println "using db vendor: " + dbVendor
        println "using db url: " + dburl
        println "migrate from version ${sourceVersion} to version ${targetVersion}"
        def sql = MigrationUtil.getSqlConnection(dburl, user, pwd, driverClass)
        def versionMigrations = getMigrationVersionsToRun(sql, sourceVersion, targetVersion)
        Logger logger = new Logger()
        def runner = new MigrationRunner(versionMigrations: versionMigrations, sql: sql, dbVendor: dbVendor, logger: logger)
        runner.run()
    }


    def initializeProperties() {
        def properties = MigrationUtil.properties
        dbVendor = properties.getProperty("db.vendor")
        dburl = properties.getProperty("db.url")
        user = properties.getProperty("db.user")
        pwd = properties.getProperty("db.password")
        driverClass = properties.getProperty("db.driverClass")

        sourceVersion = properties.getProperty("source.version")
        targetVersion = properties.getProperty("target.version")
    }

    def verifySourceVersionIsValid(Sql sql, String sourceVersion, List<String> versions) {
        if (!versions.contains(sourceVersion)) {
            throw new IllegalStateException("The source version $sourceVersion is not valid")
        }
        //get version in sources
        def String platformVersionInDatabase = MigrationUtil.getPlatformVersion(dburl, user, pwd, driverClass)
        def String platformVersionInBonitaHome = MigrationUtil.getBonitaVersionFromBonitaHome()
        if (!checkSourceVersion(platformVersionInDatabase, platformVersionInBonitaHome, sourceVersion)) {
            throw new IllegalStateException("Your installation is not consistent, verify all parameters are correctly set")
        }
    }

    def verifyTargetVersionIsValid(Sql sql, String sourceVersion, String targetVersion, List<String> versions) {
        if (!versions.contains(targetVersion)) {
            throw new IllegalStateException("the target version $targetVersion is not a valid version")
        }
        if (versions.indexOf(sourceVersion) >= versions.indexOf(targetVersion)) {
            throw new IllegalStateException("the target version $targetVersion must be after the source version $sourceVersion")

        }
    }

    def List<VersionMigration> getMigrationVersionsToRun(Sql sql, String sourceVersion, String targetVersion) {
        def versions = getAllVersions()
        verifySourceVersionIsValid(sql, sourceVersion, versions);
        verifyTargetVersionIsValid(sql, sourceVersion, targetVersion, versions)
        return getVersionsToExecute(versions, sourceVersion, targetVersion)
    }

    private List<VersionMigration> getVersionsToExecute(List<String> versions, String sourceVersion, String targetVersion) {
        return versions.subList(versions.indexOf(sourceVersion) + 1, versions.indexOf(targetVersion) + 1).collect(toVersionMigrationInstance) as List<VersionMigration>
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
