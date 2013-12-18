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
    def read = System.in.newReader().&readLine

    public void execute(GroovyScriptEngine gse){
        def childWithExt = { file,ext->
            file.listFiles(new FileFilter(){
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith(ext)
                        }
                    })
        }
        println ''
        println ''
        MigrationUtil.printInRectangle("","Bonita migration tool","",
                "This tool will migrate your installation of bonita including database and bonita home",
                "Make a backup before going any further","")

        def path = init()
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


            def String migrationVersionFolder = "versions" + File.separatorChar + sourceStepVersion + "-" + targetStepVersion + File.separatorChar
            migrateDatabase(gse, migrationVersionFolder)
            migrateBonitaHome(gse, migrationVersionFolder)
        }

        def end = new Date()
        println "\nMigration successfully completed, in " + TimeCategory.minus(end, startMigrationDate);
        MigrationUtil.getAndDisplayPlatformVersion(sql);
        sql.close()
    }

    Path init(){
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

        sql = MigrationUtil.getSqlConnection(dburl, user, pwd, driverClass);
        graph = getMigrationPaths(new File("versions"))
        sourceVersion = checkSourceVersion(sql,bonitaHome,sourceVersion)
        if(sourceVersion == null){
            return false;
        }
        targetVersion = checkTargetVersion(sourceVersion, targetVersion);
        if(targetVersion == null){
            return false;
        }
        def path = graph.getShortestPath(sourceVersion,targetVersion)
        println "MIGRATE $sourceVersion TO $targetVersion using path $path"
        if(!MigrationUtil.isAutoAccept()){
            println "Press ENTER to start migration or Ctrl+C to cancel."
            System.console().readLine()
        }
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

    String checkTargetVersion(String sourceVersion, String givenTargetVersion){
        //all steps from a version to an other
        println "List of all possible migration starting from your version"

        List<Path> paths = graph.getPaths(sourceVersion)
        paths.each {
            println it.toString();
        }
        //check if we can migrate to an other version here
        if(paths.isEmpty()){
            println "no migration possible starting from version $sourceVersion, possible paths are:"
            paths.each { println "${it.key} --> ${it.value}" }
            return null;
        }
        def targetVersion = givenTargetVersion
        //ask user for a target version
        def possibleTarget = paths.collect{ it.getLastVersion() }

        if(targetVersion == null){
            println "Please choose a target version from the list below:"
            targetVersion = askForOptions(possibleTarget);
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
                detectedVersion = askForOptions(possibleVersion)

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

    String askForOptions(List<String> options){
        def input = null;
        while(true){
            options.eachWithIndex {it,idx->
                println "${idx+1} -- $it "
            }
            print "choice: "
            input = read();
            try{
                def choiceNumber = Integer.valueOf(input) -1 //index in the list is -1
                if(choiceNumber <= options.size()){
                    return options.get(choiceNumber)
                }
            }catch (Exception e){
            }
            println "Invalid choice, please enter a value between 1 and ${options.size()}"
        }
    }

    String getPlatformVersionInBonitaHome(File bonitaHome,String defaultSourceVersion){
        new File(bonitaHome,)
    }

    String getPlatformVersionInDatabase(Sql sql, String defaultPlatformVersion){
        def String platformVersion = MigrationUtil.getPlatformVersion(sql)
        def String version = null;
        if(platformVersion == "BOS-6.0"){
            //means that version is in [6.0.0,6.1.0[
            if(defaultPlatformVersion != null){
                version = defaultPlatformVersion
            }else{
                println "Can't detect version in database, please enter the current version of bonita:"
                version = read()
                println "version is $version"
            }
        }
    }

    public migrateDatabase(GroovyScriptEngine gse, String migrationVersionFolder) {
        def startDate = new Date();
        def resources = new File(migrationVersionFolder + "Database")
        if(!resources.exists()){
            throw new IllegalStateException(resources.absolutePath + " doesn't exist.")
        }

        println "Migration of database :"
        PrintStream stdout = MigrationUtil.setSystemOutWithTab(2);
        println "Make a backup of your database and bonita_home before migrating."
        if(!MigrationUtil.isAutoAccept()){
            println "Press ENTER to continue or Ctrl+C to cancel."
            System.console().readLine()
        }
        println ""

        def features = []
        def files = resources.listFiles()
        Arrays.sort(files)
        files.each {
            if(it.isDirectory())
                features.add(it)
        }
        features.eachWithIndex { file, idx->
            StringBuilder result = new StringBuilder(file.getName().substring(4))
            def feature = result.replace(0, 1, result.substring(0, 1).toUpperCase()).toString()
            println "[ Migrating <" + feature + "> " + (idx + 1) + "/" + features.size() + " ]"

            def binding = new Binding(["sql":sql, "dbVendor":dbVendor, "bonitaHome":bonitaHome, "feature":file]);
            migrateFeature(gse, file, binding, 3);
        }
        System.setOut(stdout);
        MigrationUtil.printSuccessMigration(startDate, startMigrationDate);
    }


    public migrateBonitaHome(GroovyScriptEngine gse, String migrationVersionFolder) {
        def startDate = new Date();
        def feature = new File(migrationVersionFolder + "Bonita-home")
        if(!feature.exists()){
            throw new IllegalStateException(feature.absolutePath + " doesn't exist.")
        }

        println "Migration of bonita home :"
        def binding = new Binding(["bonitaHome":bonitaHome, "feature":feature, "startMigrationDate":startMigrationDate, "gse":gse]);
        migrateFeature(gse, feature, binding, 2);
    }
    private migrateFeature(GroovyScriptEngine gse, File file, Binding binding, int nbTabs){
        PrintStream stdout = MigrationUtil.setSystemOutWithTab(nbTabs);
        new File(file, "Description.txt").eachLine{ line -> println "Description : " + line }
        System.setOut(stdout);

        MigrationUtil.executeMigration(gse, file, "MigrateFeature.groovy", binding, nbTabs, startMigrationDate);
    }
}
