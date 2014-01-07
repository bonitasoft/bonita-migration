import java.io.IOException;
import java.io.OutputStream;

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




/**
 *
 * Main script to execute the migration
 *
 * This is the master script for the migration
 * it must be launched with parameters listed below
 *
 * Required parameters in Config.properties:
 *  bonita.home=<path to bonita home>
 *  source.version=<the current version of your installation>     -> not used yet
 *  target.version=<the version your installation will be in>     -> not used yet
 *  db.vendor=<the kind on you database, can be [mysql,postgres,sqlserver,oracle]
 *  db.url=<the url of the database>
 *  db.driverclass=<the class of the jdbc driver>
 *  db.user=<the username to connect to the database>
 *  db.password=<the password to connect to the database>
 *
 *  Also not that the jdbc driver must be put in the lib folder.
 *
 *  It launches all scripts inside the versions folder.
 *
 *
 *  example: groovy Migration.groovy 
 *
 *
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 *
 */
public class Migration {


    public static void main(String[] args){
        new Migration().run()
    }

    public void run(){
        //add libraries to the classpath
        def getRootParent = { it->
            def root = it;
            while(root.getParent() != null){
                root = root.getParent();
            }
            return root;
        };
        def logInFile = new FileOutputStream(new File("migration-"+new Date().format("yyyy-MM-dd-HHmmss")+".log"))
        System.setOut(new PrintStream(new SplitPrintStream(System.out, logInFile )))
        System.setErr(new PrintStream(new SplitPrintStream(System.err, logInFile )))
        def classLoader = getRootParent(this.class.classLoader);
        new File("lib").eachFile(FileType.FILES, {
            if(it.getName().endsWith(".jar") && ! it.getName().startsWith("groovy-all")){//groovy-all added by the .sh/.bat
                println "adding ${it.getPath()} to classpath"
                classLoader.addURL(it.toURI().toURL())
            }
        })
        def gse = new GroovyScriptEngine("");
        Class runner = gse.loadScriptByName("org/bonitasoft/migration/core/MigrationRunner.groovy");
        def instance = runner.newInstance();
        def method = runner.getMethod("execute", GroovyScriptEngine.class)
        method.invoke(instance, gse);
    }
    public class SplitPrintStream extends OutputStream {

        private final OutputStream out1
        private final OutputStream out2
        public SplitPrintStream(OutputStream out1, OutputStream out2){
            this.out2 = out2
            this.out1 = out1
        }
        @Override
        public void write(int b) throws IOException {
            out1.write(b)
            out2.write(b)
        }
        @Override
        public void flush() throws IOException {
            out1.flush();
            out2.flush();
        }

        @Override
        public void close() throws IOException {
            out1.close()
            out2.close()
        }
    }
}
