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

import groovy.time.TimeCategory
import groovy.time.TimeDuration

import org.bonitasoft.migration.versions.Version_6_0_2_to_6_1_0



/**
 *
 * Launched by Migration.groovy
 *
 * @author Baptiste Mesta
 *
 */
public class MigrationRunner {


    public static void main(String[] args){
        new MigrationRunner().execute(args);
    }

    public void execute(String[] args){
        println "Starting migration"
        def start = new Date()
        def childWithExt = { file,ext->
            file.listFiles(new FileFilter(){
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith(ext);
                        }
                    })
        };
        confirmProperties(args);
        println "execute 6.0.2 to 6.1.0"
        new Version_6_0_2_to_6_1_0().execute(args);
        def end = new Date()
        TimeDuration duration = TimeCategory.minus(end, start)
        println "migration successful, took "+duration
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
}
