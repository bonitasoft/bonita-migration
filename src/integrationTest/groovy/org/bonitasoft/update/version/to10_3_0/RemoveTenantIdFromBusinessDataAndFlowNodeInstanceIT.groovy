/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to10_3_0

class RemoveTenantIdFromBusinessDataAndFlowNodeInstanceIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromBusinessDataAndFlowNodeInstance updateStep = new RemoveTenantIdFromBusinessDataAndFlowNodeInstance()

    def "should remove tenantId from business data and flownode instance tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("flownode_instance", "tenantId")
            hasPrimaryKeyOnTable("flownode_instance", "pk_flownode_instance")
            !hasForeignKeyOnTable("flownode_instance", "fk_flownode_instance_tenantId")

            !hasColumnOnTable("arch_flownode_instance", "tenantid")
            hasPrimaryKeyOnTable("arch_flownode_instance", "pk_arch_flownode_instance")
            !hasForeignKeyOnTable("arch_flownode_instance", "fk_arch_flownode_instance_tenantId")
            !hasForeignKeyOnTable("arch_flownode_instance", "fk_AFln_tenId") // Oracle

            !hasColumnOnTable("arch_process_instance", "tenantid")
            hasPrimaryKeyOnTable("arch_process_instance", "pk_arch_process_instance")
            !hasForeignKeyOnTable("arch_process_instance", "fk_arch_process_instance_tenantId")
            !hasForeignKeyOnTable("arch_process_instance", "fk_AProc_tenId") // Oracle

            !hasColumnOnTable("pending_mapping", "tenantid")
            !hasForeignKeyOnTable("pending_mapping", "fk_pending_mapping_tenantId")
            hasForeignKeyOnTable("pending_mapping", "fk_pending_mapping_activityid")
            !hasForeignKeyOnTable("pending_mapping", "fk_pending_mapping_flownode_instanceId")
            !hasForeignKeyOnTable("pending_mapping", "fk_pMap_flnId") // ORACLE
            !hasUniqueKeyOnTable("pending_mapping", "idx_UQ_pending_mapping")
            hasUniqueKeyOnTableWithNameAndColumns("pending_mapping", "uk_pending_mapping_activityid_userid_actorid", "activityid", "userid", "actorid")

            !hasColumnOnTable("ref_biz_data_inst", "tenantid")
            hasPrimaryKeyOnTable("ref_biz_data_inst", "pk_ref_biz_data_inst")
            !hasForeignKeyOnTable("ref_biz_data_inst", "fk_ref_biz_data_inst_tenantId")
            // assert FK renaming
            !hasForeignKeyOnTable("ref_biz_data_inst", "fk_ref_biz_data_proc")
            hasForeignKeyOnTable("ref_biz_data_inst", "fk_ref_biz_data_inst_proc_inst_id")
            // assert FK renaming
            !hasForeignKeyOnTable("ref_biz_data_inst", "fk_ref_biz_data_fn")
            hasForeignKeyOnTable("ref_biz_data_inst", "fk_ref_biz_data_inst_fn_inst_id")

            !hasColumnOnTable("multi_biz_data", "tenantid")
            hasPrimaryKeyOnTable("multi_biz_data", "pk_multi_biz_data")
            !hasForeignKeyOnTable("multi_biz_data", "fk_multi_biz_data_tenantId")
            // assert FK renaming
            !hasForeignKeyOnTable("multi_biz_data", "fk_rbdi_mbd")
            hasForeignKeyOnTable("multi_biz_data", "fk_multi_biz_data_id")

            !hasColumnOnTable("arch_ref_biz_data_inst", "tenantid")
            hasPrimaryKeyOnTable("arch_ref_biz_data_inst", "pk_arch_ref_biz_data_inst")

            !hasColumnOnTable("arch_multi_biz_data", "tenantid")
            hasPrimaryKeyOnTable("arch_multi_biz_data", "pk_arch_multi_biz_data")
            // assert FK renaming
            !hasForeignKeyOnTable("arch_multi_biz_data", "fk_arch_rbdi_mbd")
            hasForeignKeyOnTable("arch_multi_biz_data", "fk_arch_multi_biz_data_id")

            !hasColumnOnTable("data_instance", "tenantid")
            hasPrimaryKeyOnTable("data_instance", "pk_data_instance")
            !hasForeignKeyOnTable("data_instance", "fk_data_instance_tenantId")

            !hasColumnOnTable("arch_data_instance", "tenantid")
            hasPrimaryKeyOnTable("arch_data_instance", "pk_arch_data_instance")
            !hasForeignKeyOnTable("arch_data_instance", "fk_arch_data_instance_tenantId")

            !hasColumnOnTable("connector_instance", "tenantid")
            !hasForeignKeyOnTable("connector_instance", "fk_connector_instance_tenantId")
            hasPrimaryKeyOnTable("connector_instance", "pk_connector_instance")

            !hasColumnOnTable("arch_connector_instance", "tenantid")
            hasPrimaryKeyOnTable("arch_connector_instance", "pk_arch_connector_instance")
        }
    }
}
