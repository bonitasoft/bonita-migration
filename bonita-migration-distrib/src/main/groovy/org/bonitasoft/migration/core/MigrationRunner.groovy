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

    private def startMigrationDate
    def read = System.in.newReader().&readLine

    public void execute(GroovyScriptEngine gse){
        startMigrationDate = new Date()
        def childWithExt = { file,ext->
            file.listFiles(new FileFilter(){
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith(ext)
                        }
                    })
        }
        init();

        PrintStream stdout = MigrationUtil.setSystemOutWithTab(1);
        def String migrationVersionFolder = "versions" + MigrationUtil.FILE_SEPARATOR + sourceVersion + "-" + targetVersion + MigrationUtil.FILE_SEPARATOR
        migrateDatabase(gse, migrationVersionFolder)
        migrateBonitaHome(gse, migrationVersionFolder)
        System.setOut(stdout);

        def end = new Date()
        println "\nMigration successfully completed, in " + TimeCategory.minus(end, startMigrationDate);
        MigrationUtil.getAndDisplayPlatformVersion(sql);
        sql.close()
    }

    void init(){
        def Properties properties = MigrationUtil.getProperties();

        println ""
        println "Properties : "
        sourceVersion = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.SOURCE_VERSION);
        targetVersion = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.TARGET_VERSION);
        bonitaHome = new File(MigrationUtil.getAndPrintProperty(properties, MigrationUtil.BONITA_HOME));
        if(!bonitaHome.exists()){
            throw new IllegalStateException("Bonita home does not exist.");
        }
        dbVendor = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_VENDOR);
        def dburl = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_URL);
        def user = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_USER);
        def pwd = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_PASSWORD);
        def driverClass = MigrationUtil.getAndPrintProperty(properties, MigrationUtil.DB_DRIVERCLASS);

        sql = MigrationUtil.getSqlConnection(dburl, user, pwd, driverClass);
        println ""
        sourceVersion = checkSourceVersion(sql,bonitaHome,sourceVersion)

        println ""
        println "MIGRATE " + sourceVersion + " TO " + targetVersion

        if(!MigrationUtil.isAutoAccept()){
            println "Press ENTER to start migration or Ctrl+C to cancel."
            System.console().readLine()
        }
    }

    String checkSourceVersion(Sql sql,File bonitaHome,String defaultSourceVersion){
        def String platformVersionInDatabase = MigrationUtil.getPlatformVersion(sql)
        def s = File.separator;
        def File versionFile = new File(bonitaHome, "server${s}platform${s}conf${s}VERSION");
        def String platformVersionInBonitaHome = versionFile.exists()?versionFile.text:null;

        def String sourceVersion = null
        if(!platformVersionInDatabase.startsWith("6") || platformVersionInBonitaHome == null){
            if(defaultSourceVersion != null){
                sourceVersion = defaultSourceVersion
            }else{
                println "Unable to detect the current version of bonita, please enter it (e.g 6.0.2):"
                sourceVersion = read();
            }
        }
        println "Version is "+sourceVersion
        System.exit(0)

        //        def String platformVersionInDatabase = getPlatformVersionInDatabase(sql,defaultSourceVersion)
        //        def String platformVersionInBonitaHome = getPlatformVersionInBonitaHome(bonitaHome,defaultSourceVersion)


        return defaultSourceVersion
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
        // TODO : Lire le fichier de description et l'afficher
        PrintStream stdout = MigrationUtil.setSystemOutWithTab(nbTabs);
        new File(file, "Description.txt").eachLine{ line -> println "Description : " + line }
        System.setOut(stdout);

        MigrationUtil.executeMigration(gse, file, "MigrateFeature.groovy", binding, nbTabs, startMigrationDate);
    }
}
