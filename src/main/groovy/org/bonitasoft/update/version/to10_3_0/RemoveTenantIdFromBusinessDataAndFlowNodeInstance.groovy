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
            dropForeignKey("ref_biz_data_inst", "fk_ref_biz_data_fn")
            dropForeignKey("multi_biz_data", "fk_rbdi_mbd")
            dropForeignKey("arch_multi_biz_data", "fk_arch_rbdi_mbd")
            // on Oracle the FK is named differently:
            dropForeignKey("pending_mapping", dbVendor == ORACLE ? "fk_pMap_flnId" : "fk_pending_mapping_flownode_instanceId")

            // recreate PK:
            dropPrimaryKey("flownode_instance")
            createPrimaryKey("flownode_instance", "id")
            dropPrimaryKey("data_instance")
            createPrimaryKey("data_instance", "id")
            dropPrimaryKey("arch_data_instance")
            createPrimaryKey("arch_data_instance", "id")
            dropPrimaryKey("ref_biz_data_inst")
            createPrimaryKey("ref_biz_data_inst", "id")
            dropPrimaryKey("multi_biz_data")
            createPrimaryKey("multi_biz_data", "id", "data_id")
            dropPrimaryKey("arch_ref_biz_data_inst")
            createPrimaryKey("arch_ref_biz_data_inst", "id")
            dropPrimaryKey("arch_multi_biz_data")
            createPrimaryKey("arch_multi_biz_data", "id", "data_id")

            // recreate FK:
            createForeignKey("ref_biz_data_inst", "fk_ref_biz_data_inst_fn_inst_id",
                    "flownode_instance", ["fn_inst_id"], ["id"], true)
            createForeignKey("multi_biz_data", "fk_multi_biz_data_id",
                    "ref_biz_data_inst", ["id"], ["id"], true)
            createForeignKey("arch_multi_biz_data", "fk_arch_multi_biz_data_id",
                    "arch_ref_biz_data_inst", ["id"], ["id"], true)
            createForeignKey("pending_mapping", "fk_pending_mapping_flownode_instanceId",
                    "flownode_instance", ["activityId"], ["id"], true)
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
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from business data and flownode instance tables: flownode_instance, data_instance," +
                " arch_data_instance, ref_biz_data_inst, arch_ref_biz_data_inst, multi_biz_data, arch_multi_biz_data"
    }
}
