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

import java.io.File;
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
	private def File bonitaHome
	private def dbVendor

	/**
	 * migration with specific behaviors. Instead of executing the sql, the script will call the methods having the same name
	 */
	private def specificMigrations =  [
		"platform",
		"profile",
		"document"
	]

	public static void main(String[] args) {
		new Version_6_0_2_to_6_1_0().execute(args);
	}

    public execute(String[] args){
		def Map props = MigrationUtil.parseOrAskArgs(args);
		dbVendor = props.get(MigrationUtil.DB_VENDOR);
		bonitaHome = new File(props.get(MigrationUtil.BONITA_HOME));
		if(!bonitaHome.exists()){
			throw new IllegalStateException("bonita home does not exists");
		}
		def sql = MigrationUtil.getSqlConnection(props);
		def resources = new File("versions/"+this.getClass().getSimpleName())
		migrateFeatures(resources, sql)
		sql.close()
	}

    public migrateFeatures(File resources, groovy.sql.Sql sql) {
        def features = [];
        def files = resources.listFiles();
        Arrays.sort(files);
        files.each {
            if(it.isDirectory())
                features.add(it.getAbsoluteFile());
        }
        features.eachWithIndex { file,idx->
            def feature = file.getName().substring(4);
            println "Migrating <"+feature+"> " + (idx+1) + "/" + features.size();
            if(feature in specificMigrations){
                "$feature"(file, sql);
            }else{
                MigrationUtil.executeDefaultSqlFile(file, dbVendor, sql);
            }
        }
    }

	public platform(File feature, groovy.sql.Sql sql){
		def sqlFile = MigrationUtil.getSqlFile(feature, dbVendor, "");
		def parameters = Collections.singletonMap(":version", "6.1.0");
		MigrationUtil.executeContentFile(sqlFile, sql, parameters);
	}

    public profile(File feature, groovy.sql.Sql sql){
		def currentTime = System.currentTimeMillis();
		def tenants = MigrationUtil.getTenantsId(feature, dbVendor, sql);
		
		println "executing update for each tenants: "+tenants
		tenants.each {
			println "for tenant id=" + it;
			//there is profile and profile entries needed
			def adminId = MigrationUtil.getId(feature, dbVendor, "get_admin_profile_id", it, sql);
			def directoryId = MigrationUtil.getId(feature, dbVendor, "get_dir_profile_entry_id", it, sql);
			if(adminId != null && directoryId != null){
				sql.execute(MigrationUtil.getSqlContent(feature, dbVendor, "update")
						.replaceAll(":tenantId", String.valueOf(it))
						.replaceAll(":admin_profile_id", String.valueOf(adminId))
						.replaceAll(":dir_profile_entry_id", String.valueOf(directoryId)));
			}
			println "done";
		}
	}

    public document(File feature, groovy.sql.Sql sql){
        MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);
        //get the path from bonita home (default = bonita.home/platform/work)
        //for each row in document_mapping get the corresponding file content
        def contentRoot = new File(new File(new File(bonitaHome,"server"),"platform"),"work")
        def contents = [];
        def Map idByTenant = [:];
        def migrateContentFor = { table, contentIndex ->
            sql.eachRow("SELECT * FROM "+table) { row ->
                if(row[6]){//document has content
                    def contentId = row[contentIndex];
                    def tenantId = row[0];
                    idByTenant.put(tenantId,idByTenant.get(tenantId)==null?1:idByTenant.get(tenantId)+1)
                    def File content = new File(contentRoot,contentId);
                    if(!content.exists()){
                        throw new IllegalStateException("content not found " + content.getAbsolutePath());
                    }
                    sql.executeInsert(MigrationUtil.getSqlContent(feature, dbVendor, "insertcontent"), tenantId, idByTenant.get(tenantId), contentId, content.getBytes())
                    id++;
                    contents.add(content)
                }
            }
        }
        //execute for live document mappings
        migrateContentFor("document_mapping",9);
        //execute for archived document mappings
        migrateContentFor("arch_document_mapping",10);
        println "updating sequence"
        //ids are same for archived mapping of live mapping, all is in document content
        idByTenant.each {
            def tenantId= it.key;
            def nbElements = it.value;
            println "update sequence for tenantId "+tenantId+" with nextId="+(nbElements+1);
            println sql.executeUpdate(MigrationUtil.getSqlContent(feature, dbVendor,"updateSequence"),nbElements+1, tenantId) + " row(s) updated";
        }
        //deleting files
        contents.each { it.delete(); }
        println "migrated "+contents.size()+" documents from file system to database"
    }

}