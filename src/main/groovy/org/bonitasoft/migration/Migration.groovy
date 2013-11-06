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



/**
 *
 * Main script to execute the migration
 *
 * @author Baptiste Mesta
 *
 */
public class Migration {


    public static void main(String[] args){
        println "Starting migration"
        def childWithExt = {file,ext->
            file.listFiles(new FileFilter(){
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith(ext);
                        }
                    })
        };

        //load lib folder and MigrationUtil
        println new File("aa").getAbsolutePath();
        ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        childWithExt(new File("lib/"),".jar").each {
            def url = it.toURI().toURL();
            println "adding to classpath: "+url;
            loader.addURL(url);
        }
        def grClass = new File("MigrationUtil.groovy")
        Class groovyClass = loader.parseClass(grClass);
        def shell = new GroovyShell(loader);
        def versions = new File("versions");
        def scripts = childWithExt(versions,".groovy");
        scripts.eachWithIndex {it,i->
            println "Running script "+ (i+1)+"/"+scripts.size()+": "+it.getName();
            def result = shell.run(it, args);
        }
    }
}
