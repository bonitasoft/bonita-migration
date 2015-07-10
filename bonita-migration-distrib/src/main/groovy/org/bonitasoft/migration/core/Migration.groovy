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

import org.bonitasoft.migration.core.graph.Path
import org.bonitasoft.migration.core.graph.TransitionGraph

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

    private def TransitionGraph graph

    public static void main(String[] args) {
        new Migration().run()
    }

    public void run() {
        def properties = MigrationUtil.properties
        dbVendor = properties.getProperty("db.vendor")
        dburl = properties.getProperty("db.url")
        user = properties.getProperty("db.user")
        pwd = properties.getProperty("db.password")
        driverClass = properties.getProperty("db.driverClass")

        sourceVersion = properties.getProperty("source.version")
        targetVersion = properties.getProperty("target.version")


        setupOutputs()
        printWarning()

        println "using db vendor: " + dbVendor
        println "using db url: " + dburl
        println "migrate from version ${sourceVersion} to version ${targetVersion}"

        def sql = MigrationUtil.getSqlConnection(dburl, user, pwd, driverClass)
        def List<VersionMigration> versionMigrations = versions.collect {
            def versionUnderscored = it.replace(".", "_")
            def versionMigrationClass = Class.forName("org.bonitasoft.migration.version.to${versionUnderscored}.MigrateTo$versionUnderscored")
            return versionMigrationClass.newInstance()
        } as List<VersionMigration>
        versionMigrations.each {
            it.getMigrationSteps().each { step ->
                println "execute " + step.description
                step.execute(sql, MigrationStep.DBVendor.valueOf(dbVendor.toUpperCase()))
            }
        }
    }

    def List<String> getVersions() {
        graph = getMigrationPaths( new File( getClass().getResource("../version").path))
        sourceVersion = checkSourceVersion(bonitaHome, sourceVersion)
        if (sourceVersion == null) {
            return null;
        }
        //all steps from a version to an other
        List<Path> paths = graph.getPaths(sourceVersion)
        if (!paths.isEmpty()) {
            println "List of all possible migration starting from your version:"
            paths.each {
                println it.toString();
            }
            println ""
        } else {
            println "no migration possible starting from version $sourceVersion"
            return null;
        }
        targetVersion = checkTargetVersion(sourceVersion, targetVersion, paths);
        println ""
        if (targetVersion == null) {
            return false;
        }
        def path = graph.getShortestPath(sourceVersion, targetVersion)
        println "MIGRATE $sourceVersion TO $targetVersion using path $path"
        return path;

//        ["7.0.1"]
    }

    String checkTargetVersion(String sourceVersion, String givenTargetVersion, List<Path> paths) {
        def targetVersion = givenTargetVersion
        //ask user for a target version
        def possibleTarget = paths.collect { it.getLastVersion() }

        if (targetVersion == null) {
            println "Please choose a target version from the list below:"
            targetVersion = MigrationUtil.askForOptions(possibleTarget);
        }
        if (!possibleTarget.contains(targetVersion)) {
            println "no migration possible to the version $targetVersion, possible target are: $possibleTarget"

        }
        return targetVersion;
    }


    TransitionGraph getMigrationPaths(File parent) {
        def List<File> migrationFolders = []
        parent.eachDir {
            migrationFolders.add(it);
        }
        def validVersions = ["7.0.0"]
        validVersions.addAll(getValidReleasedVersions(migrationFolders));
        return new TransitionGraph("7.0.0", validVersions);
    }

    String checkSourceVersion(File bonitaHome, String givenSourceVersion) {
        //get version in sources
        def String platformVersionInDatabase = MigrationUtil.getPlatformVersion(dburl, user, pwd, driverClass)
        def String platformVersionInBonitaHome = MigrationUtil.getBonitaVersionFromBonitaHome()
        return checkSourceVersion(platformVersionInDatabase, platformVersionInBonitaHome, givenSourceVersion);
    }

    String checkSourceVersion(String platformVersionInDatabase, String platformVersionInBonitaHome, String givenSourceVersion) {
        def String sourceVersion = null
        def String detectedVersion = null;
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

    public static List<String> getValidReleasedVersions(List<File> migrationFolders) {
        return migrationFolders.findAll {
            def bonitaHome = new File(it, "Bonita-home")
            def f1 = new File(bonitaHome, "bonita-home")
            return f1.exists()
        }.collect { it.getName() }
    }

    private void setupOutputs() {
        def logInFile = new FileOutputStream(new File("migration-" + new Date().format("yyyy-MM-dd-HHmmss") + ".log"))
        System.setOut(new PrintStream(new SplitPrintStream(System.out, logInFile)))
        System.setErr(new PrintStream(new SplitPrintStream(System.err, logInFile)))
    }


    def printWarning() {
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
