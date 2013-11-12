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

import org.bonitasoft.migration.MigrationUtil


/**
 *
 * Main script to execute the migration
 *
 * @author Baptiste Mesta
 *
 */
public class Version_6_0_2_to_6_1_0 {

    public static void main(String[] args) {
        new Version_6_0_2_to_6_1_0().execute(args);
    }

    public execute(String[] args){
        def migrationUtil = new MigrationUtil()
        def Map props = migrationUtil.parseOrAskArgs(args);
        def dbVendor = props.get(MigrationUtil.DB_VENDOR);
        def sql = migrationUtil.getSqlConnection();
        def resources = new File("versions/"+this.getClass().getName())
        executeSqlFiles(resources, dbVendor, sql)
        sql.close()
    }

    public executeSqlFiles(File resources, String dbVendor, groovy.sql.Sql sql) {
        def features = [];
        resources.listFiles().each {
            if(it.isDirectory())
                features.add(it.getAbsoluteFile());
        }
        features.eachWithIndex { file,idx->
            println "Migrating <"+file.getName()+"> "+(idx+1)+"/"+features.size();
            def sqlFile = new File(file,dbVendor+".sql");
            def content = sqlFile.text;
            sql.execute(content);
        }
    }
}