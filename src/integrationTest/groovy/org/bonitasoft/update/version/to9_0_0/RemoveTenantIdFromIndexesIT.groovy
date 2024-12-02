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

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveTenantIdFromIndexesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveTenantIdFromIndexes updateStep = new RemoveTenantIdFromIndexes()

    def setup() {
        dropTestTables()
        updateContext.setVersion("9.0.0")
        dbUnitHelper.createTables("9_0_0/indexes_without_tenantid")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables([
            "arch_contract_data",
            "contract_data",
            "process_comment",
            "arch_process_comment",
            "arch_document_mapping",
            "connector_instance",
            "arch_connector_instance",
            "arch_process_instance",
            "arch_ref_biz_data_inst",
            "ref_biz_data_inst",
            "waiting_event",
            "business_app_menu",
            "business_app_page",
            "business_app",
            "profile",
            "page",
            "data_instance",
            "arch_data_instance",
            "user_contactinfo",
            "custom_usr_inf_def",
            "group_",
            "role",
            "user_",
            "bar_resource",
            "tenant_resource",
            "job_log",
            "job_param",
            "job_desc",
            "pending_mapping",
            "flownode_instance",
            "process_instance"
        ] as String[])
    }

    def "should update indexes without 'tenantId'"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        !dbUnitHelper.hasIndexOnTable("job_param", "idx_job_param_tenant_jobid")
        checkIndexOnTable("job_param", "idx_job_param_jobid", "jobdescriptorid")
        !dbUnitHelper.hasIndexOnTable("job_param", "fk_job_param_jobId_idx") // duplicate: should have been dropped

        !dbUnitHelper.hasIndexOnTable("job_log", "fk_job_log_jobid_idx")
        checkIndexOnTable("job_log", "idx_job_log_jobdescid", "jobdescriptorid")

        !dbUnitHelper.hasIndexOnTable("job_desc", "fk_job_desc_id_idx")
        checkIndexOnTable("job_desc", "idx_job_desc_id", "id")

        !dbUnitHelper.hasIndexOnTable("contract_data", "idx_cd_scope_name")
        checkIndexOnTable("contract_data", "idx_cd_kind_scope_name", "kind", "scopeid", "name")

        !dbUnitHelper.hasIndexOnTable("arch_contract_data", "idx_acd_scope_name")
        checkIndexOnTable("arch_contract_data", "idx_acd_kind_scope_name", "kind", "scopeid", "name")

        checkIndexOnTable("tenant_resource", "idx_tenant_resource", "type", "name")

        checkIndexOnTable("bar_resource", "idx_bar_resource", "process_id", "type", "name")

        checkIndexOnTable("user_", "idx_user_name", "username")

        checkIndexOnTable("role", "idx_role_name", "name")

        checkIndexOnTable("group_", "idx_group_name", "parentpath", "name")

        checkIndexOnTable("custom_usr_inf_def", "idx_custom_usr_inf_def_name", "name")

        checkIndexOnTable("user_contactinfo", "idx_user_contactinfo", "userid", "personal")

        checkIndexOnTable("arch_data_instance", "idx1_arch_data_instance", "containerid", "containertype", "archivedate", "name", "sourceobjectid")
        checkIndexOnTable("arch_data_instance", "idx2_arch_data_instance", "sourceobjectid", "containerid", "archivedate", "id")

        checkIndexOnTable("data_instance", "idx_datai_container", "containerid", "containertype", "name")

        checkIndexOnTable("business_app", "idx_app_token", "token")
        checkIndexOnTable("business_app", "idx_app_profile", "profileid")
        checkIndexOnTable("business_app", "idx_app_homepage", "homepageid")
        checkIndexOnTable("business_app_page", "idx_app_page_token", "applicationid", "token")
        checkIndexOnTable("business_app_page", "idx_app_page_pageid", "pageid")
        checkIndexOnTable("business_app_menu", "idx_app_menu_app", "applicationid")
        checkIndexOnTable("business_app_menu", "idx_app_menu_page", "applicationpageid")
        checkIndexOnTable("business_app_menu", "idx_app_menu_parent", "parentid")

        checkIndexOnTable("waiting_event", "idx_waiting_event", "progress", "kind", "locked", "active")

        checkIndexOnTable("pending_mapping", "idx_uq_pending_mapping", true, "activityid", "userid", "actorid")

        checkIndexOnTable("flownode_instance", "idx_fni_loggroup3_terminal", "logicalgroup3", "terminal")
        checkIndexOnTable("flownode_instance", "idx_fn_lg2_state", "logicalgroup2", "statename")
        checkIndexOnTable("flownode_instance", "idx_fni_activity_instance_id_kind", "activityinstanceid", "kind")

        !dbUnitHelper.hasIndexOnTable("ref_biz_data_inst", "idx_biz_data_inst1")
        checkIndexOnTable("ref_biz_data_inst", "idx_biz_data_inst3", "proc_inst_id") // ensure the index has not been touched
        checkIndexOnTable("ref_biz_data_inst", "idx_biz_data_inst2", "fn_inst_id")

        checkIndexOnTable("arch_ref_biz_data_inst", "idx_arch_biz_data_inst1", "orig_proc_inst_id")
        checkIndexOnTable("arch_ref_biz_data_inst", "idx_arch_biz_data_inst2", "orig_fn_inst_id")

        checkIndexOnTable("arch_process_instance", "idx1_arch_process_instance", "sourceobjectid", "rootprocessinstanceid", "callerid")
        checkIndexOnTable("arch_process_instance", "idx2_arch_process_instance", "processdefinitionid", "archivedate")
        checkIndexOnTable("arch_process_instance", "idx3_arch_process_instance", "sourceobjectid", "callerid", "stateid")

        checkIndexOnTable("arch_flownode_instance", "idx_afi_kind_lg2_executedby", "logicalgroup2", "kind", "executedby")
        checkIndexOnTable("arch_flownode_instance", "idx_afi_kind_lg3", "kind", "logicalgroup3")
        checkIndexOnTable("arch_flownode_instance", "idx_afi_lg4", "logicalgroup4")
        checkIndexOnTable("arch_flownode_instance", "idx_afi_sourceid_kind", "sourceobjectid", "kind")
        checkIndexOnTable("arch_flownode_instance", "idx1_afi_root_parent", "rootcontainerid", "parentcontainerid")
        checkIndexOnTable("arch_flownode_instance", "idx_lg4_lg2", "logicalgroup4", "logicalgroup2")

        checkIndexOnTable("arch_connector_instance", "idx1_arch_connector_instance", "containerid", "containertype")
        checkIndexOnTable("connector_instance", "idx_ci_container_activation", "containerid", "containertype", "activationevent")

        checkIndexOnTable("arch_document_mapping", "idx_a_doc_mp_pr_id", "processinstanceid")

        checkIndexOnTable("arch_process_comment", "idx1_arch_process_comment", "sourceobjectid")
        checkIndexOnTable("arch_process_comment", "idx2_arch_process_comment", "processinstanceid", "archivedate")
        checkIndexOnTable("process_comment", "idx1_process_comment", "processinstanceid")

        // In the end, we check that there is no more index with tenantId as column:
        def indexes = dbUnitHelper.getIndexesWithTenantIdAsColumn()
        if (!indexes.isEmpty()) {
            updateContext.logger.error("Some indexes still have 'tenantId' in their columns (size:${indexes.size()}):")
            indexes.each {
                updateContext.logger.error("Table: ${it.tableName}, Index: ${it.indexName}")
            }
        }
        indexes.isEmpty() // assertion to make sure all indexes with 'tenantId' have been removed / replaced
    }

    private boolean checkIndexOnTable(String tableName, String indexName, boolean unique = false, String... columns) {
        def index = updateContext.databaseHelper.getIndexDefinition(tableName, unique, columns)
        if (index == null) {
            updateContext.logger.error("Index '$indexName' not found on table '$tableName'")
            return false
        } else if (index.indexName != indexName) {
            updateContext.logger
                    .error("Index '${index.indexName}' found on table '$tableName' does not match expected name '$indexName'")
            return false
        }
        return true
    }
}
