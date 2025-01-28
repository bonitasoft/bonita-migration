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

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

import static org.bonitasoft.update.core.UpdateStep.DBVendor.ORACLE

/**
 * Remove tenantId from business data and flownode instance tables: flownode_instance, data_instance,
 * arch_data_instance, ref_biz_data_inst, arch_ref_biz_data_inst, multi_biz_data, arch_multi_biz_data
 */
class RemoveTenantIdFromBusinessDataAndFlowNodeInstance extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            dropForeignKey("flownode_instance", "fk_flownode_instance_tenantId")
            dropForeignKey("multi_biz_data", "fk_multi_biz_data_tenantId")
            dropForeignKey("ref_biz_data_inst", "fk_ref_biz_data_inst_tenantId")
            if (dbVendor != ORACLE) {
                // does not exist on Oracle
                dropForeignKey("data_instance", "fk_data_instance_tenantId")
                dropForeignKey("arch_data_instance", "fk_arch_data_instance_tenantId")
            }
            dropForeignKey("pending_mapping", "fk_pending_mapping_tenantId")
            dropForeignKey("connector_instance", "fk_connector_instance_tenantId")
            dropForeignKey("ref_biz_data_inst", "fk_ref_biz_data_fn")
            dropForeignKey("multi_biz_data", "fk_rbdi_mbd")
            dropForeignKey("arch_multi_biz_data", "fk_arch_rbdi_mbd")
            // on Oracle the FK is named differently:
            dropForeignKey("pending_mapping", dbVendor == ORACLE ? "fk_pMap_flnId" : "fk_pending_mapping_flownode_instanceId")

            // recreate PK:
            recreatePrimaryKey("flownode_instance")
            recreatePrimaryKey("data_instance")
            recreatePrimaryKey("arch_data_instance")
            recreatePrimaryKey("ref_biz_data_inst")
            recreatePrimaryKey("multi_biz_data", "id", "data_id")
            recreatePrimaryKey("arch_ref_biz_data_inst")
            recreatePrimaryKey("arch_multi_biz_data", "id", "data_id")
            recreatePrimaryKey("pending_mapping")
            recreatePrimaryKey("connector_instance")
            recreatePrimaryKey("arch_connector_instance")

            // recreate UK:
            dropUniqueKey("pending_mapping", "idx_UQ_pending_mapping")
            createUniqueKey("pending_mapping", "uk_pending_mapping_activityid_userid_actorid", "activityId", "userId", "actorId")

            // recreate FK:
            createForeignKey("ref_biz_data_inst", "fk_ref_biz_data_inst_fn_inst_id",
                    "flownode_instance", ["fn_inst_id"], ["id"], true)
            createForeignKey("multi_biz_data", "fk_multi_biz_data_id",
                    "ref_biz_data_inst", ["id"], ["id"], true)
            createForeignKey("arch_multi_biz_data", "fk_arch_multi_biz_data_id",
                    "arch_ref_biz_data_inst", ["id"], ["id"], true)
            createForeignKey("pending_mapping", "fk_pending_mapping_activityid",
                    "flownode_instance", ["activityId"], ["id"], false)
            // rename FK to match new naming convention:
            dropForeignKey("ref_biz_data_inst", "fk_ref_biz_data_proc")
            createForeignKey("ref_biz_data_inst", "fk_ref_biz_data_inst_proc_inst_id",
                    "process_instance", ["proc_inst_id"], ["id"], true)

            // Finally drop the column:
            dropColumnIfExists("flownode_instance", "tenantId")
            dropColumnIfExists("data_instance", "tenantId")
            dropColumnIfExists("arch_data_instance", "tenantId")
            dropColumnIfExists("ref_biz_data_inst", "tenantid")
            dropColumnIfExists("multi_biz_data", "tenantid")
            dropColumnIfExists("arch_ref_biz_data_inst", "tenantid")
            dropColumnIfExists("arch_multi_biz_data", "tenantid")
            dropColumnIfExists("pending_mapping", "tenantid")
            dropColumnIfExists("connector_instance", "tenantid")
            dropColumnIfExists("arch_connector_instance", "tenantid")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from business data and flownode instance tables: flownode_instance, data_instance," +
                " arch_data_instance, ref_biz_data_inst, arch_ref_biz_data_inst, multi_biz_data, arch_multi_biz_data"
    }
}
