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
package org.bonitasoft.migration.versions;

import groovy.sql.Sql

import org.bonitasoft.migration.MigrationUtil

/**
 *
 * Main script to execute the migration
 *
 * @author Baptiste Mesta
 *
 */
public class Version_6_0_3_to_6_1_0 {

    public static void main(String[] args) {
        def Map props = MigrationUtil.parseOrAskArgs(args);
        def sql = Sql.newInstance(props.get("db.url"), props.get("db.user"), props.get("db.password"), props.get("db.driverclass"))
        def resources = new File("versions/Version_6_0_3_to_6_1_0")
        def features = [];
        resources.listFiles().each {
            if(it.isDirectory())
                features.add(it.getAbsoluteFile());
        }
        features.eachWithIndex { file,idx->
            println "migrating <"+file.getName()+"> "+(idx+1)+"/"+features.size();
            def sqlFile = new File(file,props.get("db.vendor")+".sql");
            def content = sqlFile.text;
            //            content.split("\n").each {
            //            println "execute sql:"+content;
            sql.execute(content);
            //            }
        }
        //        MigrationUtil.executeAllScripts(props.get("db.vendor"),sql)
        sql.close()
    }
}