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

import java.sql.ResultSet

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
        def sql = migrationUtil.getSqlConnection(props);
        def resources = new File("versions/"+this.getClass().getSimpleName())
        executeSqlFiles(resources, dbVendor, sql)
        sql.close()
    }

    public executeSqlFiles(File resources, String dbVendor, groovy.sql.Sql sql) {
        def features = [];
        def files = resources.listFiles();
        Arrays.sort(files);
        files.each {
            if(it.isDirectory())
                features.add(it.getAbsoluteFile());
        }
        def specificMigrations = ["platform", "profile"]
        features.eachWithIndex { file,idx->
            def feature = file.getName();
            println "Migrating <"+feature+"> "+(idx+1)+"/"+features.size();
            if(feature in specificMigrations){
                "$feature"(file, dbVendor, sql);
            }
            def content = new File(file,dbVendor+".sql").text;
            println sql.executeUpdate(content) + " row(s) updated";
        }
    }

    public platform(File feature, String dbVendor, groovy.sql.Sql sql){
        def content = new File(feature,dbVendor+".sql").text;
        println sql.executeUpdate(content,"6.1.0") + " row(s) updated";
    }

    public profile(File feature, String dbVendor, groovy.sql.Sql sql){
        def content = new File(feature,dbVendor+".sql").text;
        def currentTime = System.currentTimeMillis();
        //        println sql.executeUpdate(content,currentTime,currentTime) + " row(s) updated";
        def tenants = []

        sql.query(new File(feature,dbVendor+"-tenants.sql").text){ ResultSet rs ->
            while (rs.next()) tenants.add(rs.getLong(1));
        }
        println "executing update for each tenants: "+tenants
        tenants.each {
            println "for tenant id="+it;
            sql.execute(new File(feature,dbVendor+"-update.sql").text.replaceAll(":tenantId", String.valueOf(it)));
            println "done";
        }
    }

}