import groovy.io.FileType

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
;




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
 * @author Celine Souchet
 *
 */
public class Migration {


    public static void main(String[] args){
        new Migration().run(args)
    }
    public void run(String[] args){
        //add libraries to the classpath
        def getRootParent = { it->
            def root = it;
            while(root.getParent() != null){
                root = root.getParent();
            }
            return root;
        };
        def classLoader = getRootParent(this.class.classLoader);
        new File("lib").eachFile(FileType.FILES, {
            println "adding ${it.getPath()} to classpath"
            classLoader.addURL(it.toURI().toURL())
        })
        def gse = new GroovyScriptEngine("");
        Class runner = gse.loadScriptByName("org/bonitasoft/migration/core/MigrationRunner.groovy");
        def instance = runner.newInstance();
        def method = runner.getMethod("execute", String[].class, GroovyScriptEngine.class)
        method.invoke(instance, args, gse);
    }
}
