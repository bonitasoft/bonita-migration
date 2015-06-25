/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import groovy.time.TimeCategory
import org.bonitasoft.migration.core.graph.Path
import org.bonitasoft.migration.core.graph.Transition
import org.bonitasoft.migration.core.graph.TransitionGraph

/**
 *
 * Launched by Migration.groovy
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 *
 */
public class MigrationRunner {

    private def File bonitaHome
    private def dbVendor
    private def dburl
    private def user
    private def pwd
    private def driverClass
    private def String sourceVersion
    private def String targetVersion
    private def TransitionGraph graph
    private Reporter reporter = new Reporter()

    private def startMigrationDate

    public void execute(GroovyScriptEngine gse) {
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
        MigrationUtil.askIfWeContinue();

        def path = initPropertiesAndChooseMigrationPath()
        if (path == null) {
            return;
        }
        startMigrationDate = new Date()
        def transitions = path.getTransitions()
        transitions.eachWithIndex { Transition transition, idx ->
            def sourceStepVersion = transition.source
            def targetStepVersion = transition.target
            IOUtil.printInRectangle("Migration of version $sourceStepVersion to version $targetStepVersion",
                    "migration number ${idx + 1} of a total of ${transitions.size()}");
            MigrationUtil.askIfWeContinue()

            def sql = MigrationUtil.getSqlConnection(dburl, user, pwd, driverClass)

            def String migrationVersionFolder = "versions" + File.separatorChar + targetStepVersion + File.separatorChar
            //the new bonita home
            def bonitaHomeMigrationFolder = new File(migrationVersionFolder + "Bonita-home")
            def newBonitaHome = bonitaHomeMigrationFolder.listFiles().findAll {
                it.isDirectory() && it.exists() && it.getName().startsWith("bonita")
            }[0]
            if (newBonitaHome == null) {
                println "Error there is no bonita home for the target version $targetStepVersion"
                throw new GroovyRuntimeException("inconsistent migration tool");
            }
            migrateDatabase(gse, migrationVersionFolder, newBonitaHome, sql)
            migrateBonitaHome(gse, migrationVersionFolder, newBonitaHome)
            changePlatformVersion(sql, transition.getTarget())
            sql.close();
            println "Migration from $transition.source to $transition.target is complete."
            println ""
        }

        def end = new Date()
        println "Migration successfully completed, in " + TimeCategory.minus(end, startMigrationDate);
        println "The version of your Bonita BPM installation is now: " + MigrationUtil.getPlatformVersion(dburl, user, pwd, driverClass);
        println "Now, you must to reapply the customizations of your bonita home."
        println ""
        reporter.printReport()
    }

    Path initPropertiesAndChooseMigrationPath() {
        def Properties properties = MigrationUtil.getProperties();

        println ""
        println "Configuration: "
        sourceVersion = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.SOURCE_VERSION, false);
        targetVersion = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.TARGET_VERSION, false);
        bonitaHome = MigrationUtil.getBonitaHome();
        if (!bonitaHome.exists()) {
            throw new IllegalStateException("Bonita home does not exist.");
        }
        dbVendor = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_VENDOR, true);
        dburl = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_URL, true);
        user = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_USER, true);
        pwd = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_PASSWORD, true);
        driverClass = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_DRIVERCLASS, true);
        println ""

        graph = getMigrationPaths(new File("versions"))
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

    public static List<String> getValidReleasedVersions(List<File> migrationFolders) {
        return migrationFolders.findAll {
            def bonitaHome = new File(it, "Bonita-home")
            def f1 = new File(bonitaHome, "bonita")
            def f2 = new File(bonitaHome, "bonita-efficiency")
            def f3 = new File(bonitaHome, "bonita-performance")
            def f4 = new File(bonitaHome, "bonita-performance-cluster")
            def f5 = new File(bonitaHome, "bonita-teamwork")
            return f1.exists() || f2.exists() || f3.exists() || f4.exists() || f5.exists()
        }.collect { it.getName() }
    }

    public changePlatformVersion(groovy.sql.Sql sql, String version) {
        sql.executeUpdate("UPDATE platform SET previousVersion = version");
        sql.executeUpdate("UPDATE platform SET version = $version")
        println "Platform version in database changed to $version"
    }

    public migrateDatabase(GroovyScriptEngine gse, String migrationVersionFolder, File newBonitaHome, groovy.sql.Sql sql) {
        migrateFolder(migrationVersionFolder, "Database", "Migration of database") { File folder ->
            def features = []
            def files = folder.listFiles()
            Arrays.sort(files)
            files.each {
                if (it.isDirectory())
                    features.add(it)
            }
            features.eachWithIndex { file, idx ->
                StringBuilder result = new StringBuilder(file.getName().substring(4))
                def feature = result.replace(0, 1, result.substring(0, 1).toUpperCase()).toString()
                println "[ Migrating <" + feature + "> " + (idx + 1) + "/" + features.size() + " ]"

                def binding = new Binding(["sql": sql, "dbVendor": dbVendor, "bonitaHome": bonitaHome, "feature": file, "newBonitaHome": newBonitaHome, "gse": gse, "reporter": reporter]);
                sql.withTransaction {
                    migrateFeature(gse, file, binding);
                }
            }
        }
    }

    void migrateBonitaHome(GroovyScriptEngine gse, String migrationVersionFolder, File newBonitaHome) {
        migrateFolder(migrationVersionFolder, "Bonita-home", "Migration of bonita home") { File folder ->
            def binding = new Binding(["bonitaHome": bonitaHome, "feature": folder, "startMigrationDate": startMigrationDate, "newBonitaHome": newBonitaHome, "gse": gse]);
            migrateFeature(gse, folder, binding);
        }
    }

    void migrateFolder(String migrationVersionFolder, String folderName, String description, Closure closure) {
        println "$description:"
        println ""
        def folder = new File(migrationVersionFolder + folderName)
        if (!folder.exists()) {
            println "Nothing to do"
            return;
        }
        IOUtil.executeWrappedWithTabs { closure.call(folder) }
    }

    private migrateFeature(GroovyScriptEngine gse, File file, Binding binding) {
        new File(file, "Description.txt").eachLine { line -> println "Description : " + line }
        MigrationUtil.executeMigration(gse, file, "MigrateFeature.groovy", binding, startMigrationDate)
    }
}
