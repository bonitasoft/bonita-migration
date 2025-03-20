/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.database.DatabaseHelper
import org.bonitasoft.update.core.database.schema.IndexDefinition
import org.bonitasoft.update.core.database.schema.IndexTransformation

/**
 * @author Emmanuel Duchastenier
 */
class RemoveTenantIdFromIndexes extends UpdateStep {

    Logger logger

    List<IndexTransformation> indexTransformations = [
        new IndexTransformation(
        new IndexDefinition("job_param", "idx_job_param_tenant_jobid", "tenantid", "jobdescriptorid"),
        new IndexDefinition("job_param", "idx_job_param_jobid", "jobdescriptorid")
        ),
        new IndexTransformation(
        new IndexDefinition("job_log", "fk_job_log_jobid_idx", "jobdescriptorid", "tenantid"), // only exists on mysql for now...
        new IndexDefinition("job_log", "idx_job_log_jobdescid", "jobdescriptorid") // ...now exists on all databases ;-)
        ),
        new IndexTransformation(
        new IndexDefinition("job_desc", "fk_job_desc_id_idx", "id", "tenantid"), // only exists on mysql for now...
        new IndexDefinition("job_desc", "idx_job_desc_id", "id") // ...now exists on all databases ;-)
        ),
        new IndexTransformation(
        new IndexDefinition("contract_data", "idx_cd_scope_name", "kind", "scopeid", "name", "tenantid"),
        new IndexDefinition("contract_data", "idx_cd_kind_scope_name", "kind", "scopeid", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_contract_data", "idx_acd_scope_name", "kind", "scopeid", "name", "tenantid"),
        new IndexDefinition("arch_contract_data", "idx_acd_kind_scope_name", "kind", "scopeid", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("tenant_resource", "idx_tenant_resource", "tenantid", "type", "name"),
        new IndexDefinition("tenant_resource", "idx_tenant_resource", "type", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("bar_resource", "idx_bar_resource", "tenantid", "process_id", "type", "name"),
        new IndexDefinition("bar_resource", "idx_bar_resource", "process_id", "type", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("user_", "idx_user_name", "tenantid", "username"),
        new IndexDefinition("user_", "idx_user_name", "username")
        ),
        new IndexTransformation(
        new IndexDefinition("role", "idx_role_name", "tenantid", "name"),
        new IndexDefinition("role", "idx_role_name", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("group_", "idx_group_name", "tenantid", "parentpath", "name"),
        new IndexDefinition("group_", "idx_group_name", "parentpath", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("custom_usr_inf_def", "idx_custom_usr_inf_def_name", "tenantid", "name"),
        new IndexDefinition("custom_usr_inf_def", "idx_custom_usr_inf_def_name", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("user_contactinfo", "idx_user_contactinfo", "userid", "tenantid", "personal"),
        new IndexDefinition("user_contactinfo", "idx_user_contactinfo", "userid", "personal")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_data_instance", "idx1_arch_data_instance", "tenantid", "containerid", "containertype", "archivedate", "name", "sourceobjectid"),
        new IndexDefinition("arch_data_instance", "idx1_arch_data_instance", "containerid", "containertype", "archivedate", "name", "sourceobjectid")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_data_instance", "idx2_arch_data_instance", "sourceobjectid", "containerid", "archivedate", "id", "tenantid"),
        new IndexDefinition("arch_data_instance", "idx2_arch_data_instance", "sourceobjectid", "containerid", "archivedate", "id")
        ),
        new IndexTransformation(
        new IndexDefinition("data_instance", "idx_datai_container", "tenantid", "containerid", "containertype", "name"),
        new IndexDefinition("data_instance", "idx_datai_container", "containerid", "containertype", "name")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app", "idx_app_token", "token", "tenantid"),
        new IndexDefinition("business_app", "idx_app_token", "token")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app", "idx_app_profile", "profileid", "tenantid"),
        new IndexDefinition("business_app", "idx_app_profile", "profileid")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app", "idx_app_homepage", "homepageid", "tenantid"),
        new IndexDefinition("business_app", "idx_app_homepage", "homepageid")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app_page", "idx_app_page_token", "applicationid", "token", "tenantid"),
        new IndexDefinition("business_app_page", "idx_app_page_token", "applicationid", "token")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app_page", "idx_app_page_pageid", "pageid", "tenantid"),
        new IndexDefinition("business_app_page", "idx_app_page_pageid", "pageid")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app_menu", "idx_app_menu_app", "applicationid", "tenantid"),
        new IndexDefinition("business_app_menu", "idx_app_menu_app", "applicationid")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app_menu", "idx_app_menu_page", "applicationpageid", "tenantid"),
        new IndexDefinition("business_app_menu", "idx_app_menu_page", "applicationpageid")
        ),
        new IndexTransformation(
        new IndexDefinition("business_app_menu", "idx_app_menu_parent", "parentid", "tenantid"),
        new IndexDefinition("business_app_menu", "idx_app_menu_parent", "parentid")
        ),
        new IndexTransformation(
        new IndexDefinition("waiting_event", "idx_waiting_event", "progress", "tenantid", "kind", "locked", "active"),
        new IndexDefinition("waiting_event", "idx_waiting_event", "progress", "kind", "locked", "active")
        ),
        // This index is UNIQUE:
        new IndexTransformation(
        new IndexDefinition("pending_mapping", "idx_uq_pending_mapping", true, "tenantid", "activityid", "userid", "actorid"),
        new IndexDefinition("pending_mapping", "idx_uq_pending_mapping", true, "activityid", "userid", "actorid")
        ),
        new IndexTransformation(
        new IndexDefinition("flownode_instance", "idx_fni_loggroup3_terminal", "logicalgroup3", "terminal", "tenantid"),
        new IndexDefinition("flownode_instance", "idx_fni_loggroup3_terminal", "logicalgroup3", "terminal")
        ),
        new IndexTransformation(
        new IndexDefinition("flownode_instance", "idx_fn_lg2_state_tenant_del", "logicalgroup2", "statename", "tenantid"),
        new IndexDefinition("flownode_instance", "idx_fn_lg2_state", "logicalgroup2", "statename")
        ),
        new IndexTransformation(
        new IndexDefinition("flownode_instance", "idx_fni_activity_instance_id_kind", "activityinstanceid", "kind", "tenantid"),
        new IndexDefinition("flownode_instance", "idx_fni_activity_instance_id_kind", "activityinstanceid", "kind")
        ),
        new IndexTransformation(
        new IndexDefinition("ref_biz_data_inst", "idx_biz_data_inst1", "tenantid", "proc_inst_id"),
        new IndexDefinition("ref_biz_data_inst", "idx_biz_data_inst3", "proc_inst_id") // already exists, so it will simply remove idx_biz_data_inst1
        ),
        new IndexTransformation(
        new IndexDefinition("ref_biz_data_inst", "idx_biz_data_inst2", "tenantid", "fn_inst_id"),
        new IndexDefinition("ref_biz_data_inst", "idx_biz_data_inst2", "fn_inst_id")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_ref_biz_data_inst", "idx_arch_biz_data_inst1", "tenantid", "orig_proc_inst_id"),
        new IndexDefinition("arch_ref_biz_data_inst", "idx_arch_biz_data_inst1", "orig_proc_inst_id")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_ref_biz_data_inst", "idx_arch_biz_data_inst2", "tenantid", "orig_fn_inst_id"),
        new IndexDefinition("arch_ref_biz_data_inst", "idx_arch_biz_data_inst2", "orig_fn_inst_id")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_process_instance", "idx1_arch_process_instance", "tenantid", "sourceobjectid", "rootprocessinstanceid", "callerid"),
        new IndexDefinition("arch_process_instance", "idx1_arch_process_instance", "sourceobjectid", "rootprocessinstanceid", "callerid")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_process_instance", "idx2_arch_process_instance", "tenantid", "processdefinitionid", "archivedate"),
        new IndexDefinition("arch_process_instance", "idx2_arch_process_instance", "processdefinitionid", "archivedate")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_process_instance", "idx3_arch_process_instance", "tenantid", "sourceobjectid", "callerid", "stateid"),
        new IndexDefinition("arch_process_instance", "idx3_arch_process_instance", "sourceobjectid", "callerid", "stateid")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_flownode_instance", "idx_afi_kind_lg2_executedby", "logicalgroup2", "tenantid", "kind", "executedby"),
        new IndexDefinition("arch_flownode_instance", "idx_afi_kind_lg2_executedby", "logicalgroup2", "kind", "executedby")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_flownode_instance", "idx_afi_kind_lg3", "tenantid", "kind", "logicalgroup3"),
        new IndexDefinition("arch_flownode_instance", "idx_afi_kind_lg3", "kind", "logicalgroup3")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_flownode_instance", "idx_afi_kind_lg4", "tenantid", "logicalgroup4"),
        new IndexDefinition("arch_flownode_instance", "idx_afi_lg4", "logicalgroup4")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_flownode_instance", "idx_afi_sourceid_tenantid_kind", "sourceobjectid", "tenantid", "kind"),
        new IndexDefinition("arch_flownode_instance", "idx_afi_sourceid_kind", "sourceobjectid", "kind")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_flownode_instance", "idx1_arch_flownode_instance", "tenantid", "rootcontainerid", "parentcontainerid"),
        new IndexDefinition("arch_flownode_instance", "idx1_afi_root_parent", "rootcontainerid", "parentcontainerid")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_flownode_instance", "idx_lg4_lg2", "tenantid", "logicalgroup4", "logicalgroup2"),
        new IndexDefinition("arch_flownode_instance", "idx_lg4_lg2", "logicalgroup4", "logicalgroup2")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_connector_instance", "idx1_arch_connector_instance", "tenantid", "containerid", "containertype"),
        new IndexDefinition("arch_connector_instance", "idx1_arch_connector_instance", "containerid", "containertype")
        ),
        new IndexTransformation(
        new IndexDefinition("connector_instance", "idx_ci_container_activation", "tenantid", "containerid", "containertype", "activationevent"),
        new IndexDefinition("connector_instance", "idx_ci_container_activation", "containerid", "containertype", "activationevent")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_document_mapping", "idx_a_doc_mp_pr_id", "processinstanceid", "tenantid"),
        new IndexDefinition("arch_document_mapping", "idx_a_doc_mp_pr_id", "processinstanceid")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_process_comment", "idx1_arch_process_comment", "sourceobjectid", "tenantid"),
        new IndexDefinition("arch_process_comment", "idx1_arch_process_comment", "sourceobjectid")
        ),
        new IndexTransformation(
        new IndexDefinition("arch_process_comment", "idx2_arch_process_comment", "processinstanceid", "archivedate", "tenantid"),
        new IndexDefinition("arch_process_comment", "idx2_arch_process_comment", "processinstanceid", "archivedate")
        ),
        new IndexTransformation(
        new IndexDefinition("process_comment", "idx1_process_comment", "processinstanceid", "tenantid"),
        new IndexDefinition("process_comment", "idx1_process_comment", "processinstanceid")
        )
    ]

    @Override
    def execute(UpdateContext context) {
        this.logger = context.logger
        context.databaseHelper.with { helper ->

            if (dbVendor == DBVendor.MYSQL) {
                // MySQL has a duplicate index that we need to drop before running the update step:
                dropIndexIfExists("job_param", "fk_job_param_jobId_idx")

                // MySQL has a foreign key referencing the index we want to drop / recreate:
                dropForeignKey("job_param", "fk_job_param_jobid") // drop it at the beginning...
                dropForeignKey("job_log", "fk_job_log_jobid")
                dropForeignKey("business_app", "fk_app_profileId")
                dropIndexIfExists("business_app", "fk_app_profileId")
                dropForeignKey("business_app_menu", "fk_app_menu_appId")
                dropIndexIfExists("business_app_menu", "fk_app_menu_appId")
                dropForeignKey("ref_biz_data_inst", "fk_ref_biz_data_fn")
                dropIndexIfExists("ref_biz_data_inst", "fk_ref_biz_data_fn")
                dropForeignKey("business_app_page", "fk_page_id")
                dropIndexIfExists("business_app_page", "fk_page_id")
                dropForeignKey("business_app_menu", "fk_app_menu_pageId")
                dropIndexIfExists("business_app_menu", "fk_app_menu_pageId")
                dropForeignKey("business_app_menu", "fk_app_menu_parentId")
                dropIndexIfExists("business_app_menu", "fk_app_menu_parentId")
                dropForeignKey("pending_mapping", "fk_pending_mapping_flownode_instanceId")
            }

            indexTransformations.each {
                logger.info("---------------------------------------------")
                it.with {
                    // Let's look for potential indexes based on the columns names and order:
                    def sourceIndexDefFromDB = getIndexDefinition(source.tableName, source.unique, source.columnNames as String[])
                    if (sourceIndexDefFromDB) {
                        // an index with source columns exists
                        if (sourceIndexDefFromDB == source) {
                            // if source index is the default one, we drop it:
                            logger.info("Obsolete index '${source.indexName}' detected. Removing it.")
                            dropIndexIfExists(source.tableName, source.indexName)
                            dealWithTargetIndex(target, helper)
                        } else if (sourceIndexDefFromDB.isSameWithDifferentIndexName(source)) {
                            // Drop it:
                            logger.warn("Obsolete index detected (with a different name: wanted '${source.indexName}', got '${sourceIndexDefFromDB.indexName}'). Removing it.")
                            dropIndexIfExists(source.tableName, sourceIndexDefFromDB.indexName)
                            dealWithTargetIndex(target, helper)
                        }
                    } else {
                        // source index does not exist
                        logger.warn("Obsolete index '${source.indexName}' NOT found. Nothing to remove.")
                        dealWithTargetIndex(target, helper)
                    }
                }
            }

            if (dbVendor == DBVendor.MYSQL) {
                // ...restore it at the end
                createForeignKey("job_param", "fk_job_param_jobid", "job_desc", ["jobdescriptorid"], ["id"], true)
                createForeignKey("job_log", "fk_job_log_jobid", "job_desc", ["jobdescriptorid"], ["id"], true)
                createForeignKey("business_app", "fk_app_profileId", "profile", ["tenantid", "profileId"], ["tenantid", "id"], true)
                createForeignKey("business_app_menu", "fk_app_menu_appId", "business_app", ["tenantid", "applicationId"], ["tenantid", "id"], true)

                createForeignKey("business_app_menu", "fk_app_menu_pageId", "business_app_page", ["tenantid", "applicationPageId"], ["tenantid", "id"], true)
                createForeignKey("business_app_menu", "fk_app_menu_parentId", "business_app_menu", ["tenantid", "parentId"], ["tenantid", "id"], true)
                createForeignKey("pending_mapping", "fk_pending_mapping_flownode_instanceId", "flownode_instance", ["tenantid", "activityId"], ["tenantid", "id"], true)
                createForeignKey("business_app_page", "fk_page_id", "page", ["tenantid", "pageId"], ["tenantid", "id"], true)
                createForeignKey("ref_biz_data_inst", "fk_ref_biz_data_fn", "flownode_instance", ["tenantid", "fn_inst_id"], ["tenantid", "id"], true)
            }

            // Special case for this unique index that MUST keep the column tenantId until 10.3, to avoid deadlocks issue (Oracle & SQLServer),
            // Version 10.3 will remove this useless index:
            addOrReplaceIndex("pending_mapping", "idx_pending_mapping_deadlock", "tenantid", "activityId")
        }
    }

    void dealWithTargetIndex(IndexDefinition target, DatabaseHelper dbHelper) {
        def targetIndexDefFromDB = dbHelper.getIndexDefinition(target.tableName, target.unique, target.columnNames as String[])
        if (targetIndexDefFromDB) {
            // target index exists
            if (targetIndexDefFromDB == target) {
                // target index exists and is the default one, nothing to do
                logger.warn("Required index '${target.indexName}' already exists. Skipping creation.")
            } else if (targetIndexDefFromDB.isSameWithDifferentIndexName(target)) {
                // target index exists but with a different name, we simply rename it:
                logger.warn("Required index already exists but with a different name (wanted '${target.indexName}', got '${targetIndexDefFromDB.indexName}'). Renaming it.")
                dbHelper.renameIndex(target.tableName, targetIndexDefFromDB.indexName, target.indexName)
            }
        } else {
            // target index does not exist already, create it:
            logger.info("Required index '${target.indexName}' does not exist yet. Creating it.")
            dbHelper.createIndex(target.tableName, target.indexName, target.unique, target.columnNames as String[])
        }
    }

    @Override
    String getDescription() {
        return "Ensure no more 'tenantId' in database indexes, to match multitenancy removal"
    }
}
