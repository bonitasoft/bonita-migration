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

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

import groovy.lang.Binding;
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.util.GroovyScriptEngine;



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
        println ""
        println "MIGRATE " + sourceVersion + " TO " + targetVersion

        PrintStream stdout = MigrationUtil.setSystemOutWithTab(1);
        def String migrationVersionFolder = "versions" + MigrationUtil.FILE_SEPARATOR + sourceVersion + "-" + targetVersion + MigrationUtil.FILE_SEPARATOR
        migrateDatabase(gse, migrationVersionFolder)
        migrateBonitaHome(gse, migrationVersionFolder)
        System.setOut(stdout);

        def end = new Date()
        println "\nMigration successful, in " + TimeCategory.minus(end, startMigrationDate);
        MigrationUtil.getAndDisplayPlatformVersion(sql);
        sql.close()
    }

    void init(){
        def Properties properties = MigrationUtil.getProperties();

        println ""
        println "Properties : "
        sourceVersion = MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.SOURCE_VERSION);
        targetVersion = MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.TARGET_VERSION);
        bonitaHome = new File(MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.BONITA_HOME));
        if(!bonitaHome.exists()){
            throw new IllegalStateException("Bonita home does not exist.");
        }
        dbVendor = MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.DB_VENDOR);
        def dburl = MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.DB_URL);
        def user = MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.DB_USER);
        def pwd = MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.DB_PASSWORD);
        def driverClass = MigrationUtil.getAndDisplayProperty(properties, MigrationUtil.DB_DRIVERCLASS);

        sql = MigrationUtil.getSqlConnection(dburl, user, pwd, driverClass);
        println ""
        MigrationUtil.getAndDisplayPlatformVersion(sql);

        println "Press ENTER to start migration !"
        System.console().readLine()
    }

    public migrateDatabase(GroovyScriptEngine gse, String migrationVersionFolder) {
        def startDate = new Date();
        def resources = new File(migrationVersionFolder + "Database")
        if(!resources.exists()){
            throw new IllegalStateException(resources.absolutePath + " doesn't exist.")
        }

        println "Migration of database :"
        PrintStream stdout = MigrationUtil.setSystemOutWithTab(2);
        println "You should make a backup of your database."
        println "Press ENTER to continue..."
        System.console().readLine()
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
        new File(file, "Description.txt").eachLine{ line -> println line }
        System.setOut(stdout);
        
        MigrationUtil.executeMigration(gse, file, "MigrateFeature.groovy", binding, nbTabs, startMigrationDate);
    }
}
