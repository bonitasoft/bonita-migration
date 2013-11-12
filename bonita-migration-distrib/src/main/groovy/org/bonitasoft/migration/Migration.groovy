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
package org.bonitasoft.migration;

import groovy.time.TimeDuration



/**
 *
 * Main script to execute the migration
 *
 * This is the master script for the migration
 * it must be launched with parameters listed below
 *
 * Required parameters:
 *  --bonita.home <path to bonita home>
 *  --source.version <the current version of your installation>     -> not used yet
 *  --target.version <the version your installation will be in>     -> not used yet
 *  --db.vendor <the kind on you database, can be [mysql,postgres,sqlserver,oracle]
 *  --db.url <the url of the database>
 *  --db.driverclass <the class of the jdbc driver>
 *  --db.user <the username to connect to the database>
 *  --db.password <the password to connect to the database>
 *
 *  also not that the jdbc driver must be put in the lib folder
 *
 * it launches all scripts inside the versions folder
 *
 *
 *  example: groovy Migration.groovy --bonita.home /home/user/bonita.home --source.version 6.0.3
 *  --target.version 6.1.0 --db.vendor postgres --db.url jdbc:postgresql://localhost:5432/bonita
 *  --db.driverclass org.postgresql.Driver --db.user bonita --db.password bonita
 *
 *
 *
 * @author Baptiste Mesta
 *
 */
public class Migration {


    public static void main(String[] args){
        new Migration().execute(args);
    }

    public void execute(String[] args){
        println "Starting migration"
        def start = new Date()
        //load lib folder and MigrationUtil
        GroovyClassLoader classLoader = loadLibs();
        confirmProperties(args);
        def scriptsFolder = new File("versions");
        executeScriptsOfFolder(classLoader, scriptsFolder, args)
        def end = new Date()
        TimeDuration duration = TimeCategory.minus(end, start)
        println "migration successful, took "+duration
    }

    private executeScriptsOfFolder(GroovyClassLoader loader, File versions, String[] args) {
        def shell = new GroovyShell(loader);
        def scripts = filterChildrenHavingExtension(versions,".groovy");
        scripts.eachWithIndex {it,i->
            println "Running script "+ (i+1)+"/"+scripts.size()+": "+it.getName();
            def result = shell.run(it, args);
        }
    }

    void confirmProperties(String[] args) {
        println "properties:"
        def listToMap = {list->
            def map = [:];
            def iterator = list.iterator()
            while (iterator.hasNext()) {
                map.put(iterator.next(),iterator.next());
            }
            return map;
        }
        listToMap(args).each {
            println it.key.substring(2) + "="+it.value
        }
        println "Press ENTER to continue"
        System.console().readLine()
    }

    GroovyClassLoader loadLibs() {
        ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        filterChildrenHavingExtension(new File("lib/"),".jar").each {
            def url = it.toURI().toURL();
            println "adding to classpath: "+url;
            loader.addURL(url);
        }
        def grClass = new File("MigrationUtil.groovy")
        Class migrationUtil = loader.parseClass(grClass);
        Thread.currentThread().setContextClassLoader(loader)
        return loader
    }

    List filterChildrenHavingExtension(File file, String ext){
        def child =  file.listFiles(new FileFilter(){
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(ext);
                    }
                })
        Arrays.sort(child);
        return child;
    }
}
