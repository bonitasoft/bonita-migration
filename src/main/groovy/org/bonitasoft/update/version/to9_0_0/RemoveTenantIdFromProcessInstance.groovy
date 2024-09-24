/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class RemoveTenantIdFromProcessInstance extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            dropForeignKey("process_instance", "fk_process_instance_tenantId")
            // MySQL automatically creates an index with the same name:
            dropIndexIfExists("process_instance", "fk_process_instance_tenantId")

            dropForeignKey("ref_biz_data_inst", "fk_ref_biz_data_proc")
            // MySQL automatically creates an index with the same name:
            dropIndexIfExists("ref_biz_data_inst", "fk_ref_biz_data_proc")

            dropPrimaryKey("process_instance")

            createPrimaryKey("process_instance", "id")
            createForeignKey("ref_biz_data_inst", "fk_ref_biz_data_proc", "process_instance", ["proc_inst_id"], ["id"], true)
            // Index on foreign key is mandatory on Oracle
            addOrReplaceIndex("ref_biz_data_inst", "idx_biz_data_inst3","proc_inst_id")

            addOrReplaceIndex("process_instance", "idx1_proc_inst_pdef_state", "processdefinitionid", "stateid")

            // Finally drop the column!
            dropColumnIfExists("process_instance", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from process_instance table"
    }
}
