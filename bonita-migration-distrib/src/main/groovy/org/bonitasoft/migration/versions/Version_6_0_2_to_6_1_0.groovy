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
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.FlowNodeDefinition
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.TransitionInstance



/**
 *
 * Main script to execute the migration
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 *
 */
public class Version_6_0_2_to_6_1_0 {
    private def File bonitaHome
    private def dbVendor
	private def groovy.sql.Sql sql

    /**
     * migration with specific behaviors. Instead of executing the sql, the script will call the methods having the same name
     */
    private def specificMigrations =  [
        "platform",
        "profile",
        "document",
        "transition"
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
         sql = MigrationUtil.getSqlConnection(props);
        def resources = new File("versions/" + this.getClass().getSimpleName())
        migrateFeatures(resources)
        sql.close()
    }

    public migrateFeatures(File resources) {
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
                "$feature"(file);
            }else{
                MigrationUtil.executeDefaultSqlFile(file, dbVendor, sql);
            }
        }
    }

    public platform(File feature){
        def parameters = Collections.singletonMap(":version", "6.1.0");
        MigrationUtil.executeSqlFile(feature, dbVendor, null, parameters, sql, true);
    }

    public profile(File feature){
        def currentTime = System.currentTimeMillis();
        def tenants = MigrationUtil.getTenantsId(feature, dbVendor, sql);

        println "executing update for each tenants: "+tenants
        tenants.each {
            println "for tenant id=" + it;
            //there is profile and profile entries needed
            def adminId = MigrationUtil.getId(feature, dbVendor, "get_admin_profile_id", it, sql);
            def directoryId = MigrationUtil.getId(feature, dbVendor, "get_dir_profile_entry_id", it, sql);

            if(adminId != null && directoryId != null){
                def parameters = new HashMap();
                parameters.put(":tenantId", String.valueOf(it));
                parameters.put(":admin_profile_id", String.valueOf(adminId));
                parameters.put(":dir_profile_entry_id", String.valueOf(directoryId));
                MigrationUtil.executeSqlFile(feature, dbVendor, "update", parameters, sql, false);
            }
            println "done";
        }
    }

    public document(File feature){
        MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);
        //get the path from bonita home (default = bonita.home/platform/work)
        //for each row in document_mapping get the corresponding file content
        def contentRoot = new File(new File(new File(bonitaHome, "server"), "platform"), "work")
        def contents = [];
        def Map idByTenant = [:];
        def migrateContentFor = { table, contentIndex ->
            sql.eachRow("SELECT * FROM " + table) { row ->
                if(row[6]){//document has content
                    def contentId = row[contentIndex];
                    def tenantId = row[0];
                    idByTenant.put(tenantId, idByTenant.get(tenantId) == null ? 1 : idByTenant.get(tenantId) + 1)
                    def File content = new File(contentRoot, contentId);
                    if(!content.exists()){
                        throw new IllegalStateException("content not found " + content.getAbsolutePath());
                    }
                    def sqlFile = MigrationUtil.getSqlFile(feature, dbVendor, "insertcontent")
                    sql.executeInsert(MigrationUtil.getSqlContent(sqlFile.text[0], null), tenantId, idByTenant.get(tenantId), contentId, content.getBytes())
                    contents.add(content) 
                }
            }
        }
        //execute for live document mappings
        migrateContentFor("document_mapping", 9);
        //execute for archived document mappings
        migrateContentFor("arch_document_mapping", 10);
        println "updating sequence"
        //ids are same for archived mapping of live mapping, all is in document content
        idByTenant.each {
            def tenantId= it.key;
            def nbElements = it.value;
            println "update sequence for tenantId " + tenantId + " with nextId=" + (nbElements + 1);
            def parameters = new HashMap();
            parameters.put(":tenantId", tenantId);
            parameters.put(":nextId", nbElements + 1);
            MigrationUtil.executeSqlFile(feature, dbVendor, "updateSequence", parameters, sql, true);
        }
        //deleting files
        contents.each { it.delete(); }
        println "migrated " + contents.size() + " documents from file system to database"
    }

    public transition(File feature){
        return;
        //get all transitions
        sql.query("SELECT * transition_instance", { row ->
            def transition = new TransitionInstance(tenantid:row[0], id:row[1], rootContainerId:row[2], parentContainerId:row[3], name:row[4],source:row[5], processDefId:row[8], tokenRefId:row[14]);
            migrateTransition(transition);

        })
        //update sequence for the elements
        //delete the table
       MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);
    }

    public migrateTransition(TransitionInstance transition, File feature){
        //get the definition
        def s = File.separatorChar;
        def processDefXml = new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${transition.tenantid}${s}work${s}processes${s}${transition.processDefId}${s}server-process-definition.xml");
        def FlowNodeDefinition target = getTargetOfTransition(processDefXml.text, transition);

        //parse the xml
        //if target = gateway, create or hit the gateway
        // check merging condition: if merge set as finished
        //if target is not a gateway create the element
    }

    public Object getTargetOfTransition(String processDefXml, TransitionInstance transition) {
        def processDefinition = new XmlParser().parseText(processDefXml);
        def targetDefId = processDefinition.flowElements.transitions.transition.find{it.@id==transition.id}.@target
        println  targetDefId
        def flownode = processDefinition.depthFirst().grep{ it.@id == targetDefId }[0]
        def type = (flownode.name() == "gateway"?flownode.@gatewayType:"flownode");
        return new FlowNodeDefinition(id:flownode.@id,name:flownode.@name, type:type)
    }

}
