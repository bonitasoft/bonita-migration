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
package org.bonitasoft.update.version.to10_3_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Remove tenantId from flownode_instance table
 *
 * @author Emmanuel Duchastenier
 */
class RemoveTenantIdFromFlowNodeInstance extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            dropForeignKey("flownode_instance", "fk_flownode_instance_tenantId")

            dropForeignKey("ref_biz_data_inst", "fk_ref_biz_data_fn")

            dropForeignKey("pending_mapping", "fk_pending_mapping_flownode_instanceId")
            // only for Oracle, as the FK is named differently:
            dropForeignKey("pending_mapping", "fk_pMap_flnId")

            // recreate PK:
            dropPrimaryKey("flownode_instance")
            createPrimaryKey("flownode_instance", "id")

            createForeignKey("ref_biz_data_inst", "fk_ref_biz_data_fn", "flownode_instance", ["fn_inst_id"], ["id"], true)
            createForeignKey("pending_mapping", "fk_pending_mapping_flownode_instanceId", "flownode_instance", ["activityId"], ["id"], true)

            // Finally drop the column:
            dropColumnIfExists("flownode_instance", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from flownode_instance table"
    }
}
