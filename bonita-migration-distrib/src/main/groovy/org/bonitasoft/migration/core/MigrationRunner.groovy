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

import groovy.sql.Sql
import groovy.time.TimeCategory




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
    private def groovy.sql.Sql sql
    private def String sourceVersion
    private def String targetVersion
    private def TransitionGraph graph

    private def startMigrationDate

    public void execute(GroovyScriptEngine gse){
        println ''
        MigrationUtil.printInRectangle("","Bonita migration tool","",
                "This tool will migrate your installation of bonita",
                "Both database and bonita home will be modified",
                "please refer to the documentation for further steps to completely migrate your production environnement",
                "",
                "Warning:",
                "Backup the database AND the bonita home before executing the migration","")
        MigrationUtil.askIfWeContinue();

        def path = initPropertiesAndChooseMigrationPath()
        if(path == null){
            return;
        }
        startMigrationDate = new Date()
        def transitions = path.getTransitions()
        transitions.eachWithIndex { Transition transition, idx ->
            def sourceStepVersion = transition.source
            def targetStepVersion = transition.target
            MigrationUtil.printInRectangle("Migration of version $sourceStepVersion to version $targetStepVersion",
                    "migration number ${idx+1} on a total of ${transitions.size()}");
            MigrationUtil.askIfWeContinue()



            def String migrationVersionFolder = "versions" + File.separatorChar + sourceStepVersion + "-" + targetStepVersion + File.separatorChar
            //the new bonita home
            def bonitaHomeMigrationFolder = new File(migrationVersionFolder + "Bonita-home")
            def newBonitaHome = bonitaHomeMigrationFolder.listFiles().findAll { it.isDirectory() && it.exists() && it.getName().startsWith("bonita")}[0]
            migrateDatabase(gse, migrationVersionFolder, newBonitaHome)
            migrateBonitaHome(gse, migrationVersionFolder, bonitaHomeMigrationFolder, newBonitaHome)
        }

        def end = new Date()
        println "\nMigration successfully completed, in " + TimeCategory.minus(end, startMigrationDate);
        MigrationUtil.getAndDisplayPlatformVersion(sql);
        sql.close()
    }

    Path initPropertiesAndChooseMigrationPath(){
        def Properties properties = MigrationUtil.getProperties();

        println ""
        println "Configuration : "
        sourceVersion = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.SOURCE_VERSION, false);
        targetVersion = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.TARGET_VERSION, false);
        bonitaHome = new File(MigrationUtil.getAndPrintProperty(properties, MigrationUtil.BONITA_HOME, true));
        if(!bonitaHome.exists()){
            throw new IllegalStateException("Bonita home does not exist.");
        }
        dbVendor = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_VENDOR, true);
        def dburl = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_URL, true);
        def user = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_USER, true);
        def pwd = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_PASSWORD, true);
        def driverClass = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_DRIVERCLASS, true);
        println ""

        sql = MigrationUtil.getSqlConnection(dburl, user, pwd, driverClass);
        graph = getMigrationPaths(new File("versions"))
        sourceVersion = checkSourceVersion(sql,bonitaHome,sourceVersion)
        if(sourceVersion == null){
            return false;
        }
        //all steps from a version to an other
        println "List of all possible migration starting from your version:"
        List<Path> paths = graph.getPaths(sourceVersion)
        paths.each {
            println it.toString();
        }
        println ""
        //check if we can migrate to an other version here
        if(paths.isEmpty()){
            println "no migration possible starting from version $sourceVersion, possible paths are:"
            paths.each { println "${it.key} --> ${it.value}" }
            return null;
        }
        targetVersion = checkTargetVersion(sourceVersion, targetVersion, paths);
        println ""
        if(targetVersion == null){
            return false;
        }
        def path = graph.getShortestPath(sourceVersion,targetVersion)
        println "MIGRATE $sourceVersion TO $targetVersion using path $path"
        MigrationUtil.askIfWeContinue();
        return path;
    }

    String checkSourceVersion(Sql sql,File bonitaHome,String givenSourceVersion){
        //get version in sources
        def String platformVersionInDatabase = MigrationUtil.getPlatformVersion(sql)
        def s = File.separator;
        def File versionFile = new File(bonitaHome, "server${s}platform${s}conf${s}VERSION");
        def String platformVersionInBonitaHome = versionFile.exists()?versionFile.text:null;
        return checkSourceVersion(platformVersionInDatabase, platformVersionInBonitaHome, givenSourceVersion);
    }
    String checkSourceVersion(String platformVersionInDatabase, String platformVersionInBonitaHome, String givenSourceVersion){
        def String sourceVersion = null
        def String detectedVersion = null;
        if(!platformVersionInDatabase.startsWith("6")){
            //[6.0,6.1[
            if(givenSourceVersion != null){
                println "No version detected: using given version $givenSourceVersion"
                detectedVersion = givenSourceVersion
            }else{
                println "Unable to detect the current version of bonita, please choose a start version from the list below:"
                def possibleVersion = graph.getStartNodes().findAll{it.startsWith("6.0")}
                detectedVersion = MigrationUtil.askForOptions(possibleVersion)

            }
            //pre 6.1 --> use given version, but check that it start's with 6.0
            if(!detectedVersion.startsWith("6.0")){
                println "sorry the but you're installation seems to be between 6.0.0 included and 6.1.0 excluded"
                return null;
            }
            return detectedVersion
        }else if(platformVersionInBonitaHome == null){
            //[6.1,6.2[ -> use database version, but check that given version starts with 6.1
            if(givenSourceVersion != null && !givenSourceVersion.equals(platformVersionInDatabase)){
                println "Sorry,your installation is $platformVersionInDatabase but you specified $givenSourceVersion"
                return null;
            }
            return platformVersionInDatabase;
        }else{
            // > 6.2
            if(platformVersionInBonitaHome != platformVersionInDatabase || (givenSourceVersion != null && platformVersionInDatabase != givenSourceVersion) ){
                //invalid case: given source (if any) not the same as version in db and as version in bonita home
                println "The versions are not consistent:"
                println "The version of the database is $platformVersionInDatabase"
                println "The version of the bonita home is $platformVersionInBonitaHome"
                if(givenSourceVersion != null){
                    println "The declared version is $givenSourceVersion"
                }
                println "Check that you configuration is correct and restart the migration"
                return null;
            }
            return platformVersionInBonitaHome
        }
    }

    String checkTargetVersion(String sourceVersion, String givenTargetVersion, List<Path> paths){
        def targetVersion = givenTargetVersion
        //ask user for a target version
        def possibleTarget = paths.collect{ it.getLastVersion() }

        if(targetVersion == null){
            println "Please choose a target version from the list below:"
            targetVersion = MigrationUtil.askForOptions(possibleTarget);
        }
        if (!possibleTarget.contains(targetVersion)){
            println "no migration possible to the version $targetVersion, possible target are: $possibleTarget"

        }
        return targetVersion;
    }

    TransitionGraph getMigrationPaths(File migrationFolders){
        def dirNames = []
        migrationFolders.eachDir {
            dirNames.add(it.getName())
        }
        return new TransitionGraph(dirNames);
    }

    void migrateFolder(String migrationVersionFolder, String folderName, String description, Closure closure){
        def folder = new File(migrationVersionFolder + folderName)
        if(!folder.exists()){
            throw new IllegalStateException(folder.absolutePath + " doesn't exist.")
        }

        println "$description :"
        println ""
        MigrationUtil.executeWrappedWithTabs { closure.call(folder) }
    }

    public migrateDatabase(GroovyScriptEngine gse, String migrationVersionFolder, File newBonitaHome) {
        migrateFolder(migrationVersionFolder, "Database", "Migration of database") { File folder ->
            def features = []
            def files = folder.listFiles()
            Arrays.sort(files)
            files.each {
                if(it.isDirectory())
                    features.add(it)
            }
            features.eachWithIndex { file, idx->
                StringBuilder result = new StringBuilder(file.getName().substring(4))
                def feature = result.replace(0, 1, result.substring(0, 1).toUpperCase()).toString()
                println "[ Migrating <" + feature + "> " + (idx + 1) + "/" + features.size() + " ]"

                def binding = new Binding(["sql":sql, "dbVendor":dbVendor, "bonitaHome":bonitaHome, "feature":file, "newBonitaHome":newBonitaHome]);
                migrateFeature(gse, file, binding);
            }
        }
    }

    public migrateBonitaHome(GroovyScriptEngine gse, String migrationVersionFolder, File bonitaHomeMigrationFolder, File newBonitaHome) {
        migrateFolder(migrationVersionFolder, "Bonita-home", "Migration of bonita home") { File folder ->
            def binding = new Binding(["bonitaHome":bonitaHome, "feature":folder, "startMigrationDate":startMigrationDate, "newBonitaHome":newBonitaHome, "gse":gse]);
            migrateFeature(gse, folder, binding);
        }
    }

    private migrateFeature(GroovyScriptEngine gse, File file, Binding binding){
        new File(file, "Description.txt").eachLine{ line -> println "Description : " + line }
        MigrationUtil.executeMigration(gse, file, "MigrateFeature.groovy", binding, startMigrationDate)
    }
}
