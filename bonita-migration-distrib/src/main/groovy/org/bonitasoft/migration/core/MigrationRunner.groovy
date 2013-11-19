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

import java.io.File

import groovy.time.TimeCategory
import groovy.time.TimeDuration



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

    public void execute(String[] args, GroovyScriptEngine gse){
        def start = new Date()
        def childWithExt = {
            file,ext->
            file.listFiles(new FileFilter(){
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(ext)
                }
            })
        }
        init(args)

        println "execute " + sourceVersion + " to " + targetVersion
        migrateFeatures(gse)
        sql.close()

        def end = new Date()
        TimeDuration duration = TimeCategory.minus(end, start)
        println "migration successful, took "+duration
    }

    void init(String[] args){
        confirmProperties(args)

        def Map props = MigrationUtil.parseOrAskArgs(args)
        sourceVersion = props.get(MigrationUtil.SOURCE_VERSION)
        targetVersion = props.get(MigrationUtil.TARGET_VERSION)
        dbVendor = props.get(MigrationUtil.DB_VENDOR)
        bonitaHome = new File(props.get(MigrationUtil.BONITA_HOME))
        if(!bonitaHome.exists()){
            throw new IllegalStateException("Bonita home does not exist.")
        }

        sql = MigrationUtil.getSqlConnection(props)
    }

    void confirmProperties(String[] args) {
        println "properties:"
        def listToMap = {
            list->
            def map = [:]
            def iterator = list.iterator()
            while (iterator.hasNext()) {
                map.put(iterator.next(),iterator.next())
            }
            return map
        }
        listToMap(args).each {
            println it.key.substring(2) + "=" + it.value
        }
        println "Press ENTER to continue"
        System.console().readLine()
    }

    public migrateFeatures(GroovyScriptEngine gse) {
        def resources = new File("versions/" + sourceVersion + "-" + targetVersion)
        if(!resources.exists()){
            throw new IllegalStateException(resources.absolutePath + " doesn't exist.")
        }

        def features = []
        def files = resources.listFiles()
        Arrays.sort(files)
        files.each {
            if(it.isDirectory())
            features.add(it)
        }
        features.eachWithIndex {
            file, idx->
            StringBuilder result = new StringBuilder(file.getName().substring(4))
            def feature = result.replace(0, 1, result.substring(0, 1).toUpperCase()).toString()
            println "Migrating <" + feature + "> " + (idx + 1) + "/" + features.size()

            def binding = new Binding(["sql":sql, "dbVendor":dbVendor, "bonitaHome":bonitaHome, "feature":file]);
            gse.run(new File(file, "MigrateFeature.groovy").getPath(), binding)
        }
    }
}
