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

class RemoveTenantIdFromFlowNodeInstanceIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromFlowNodeInstance updateStep = new RemoveTenantIdFromFlowNodeInstance()

    def "should remove tenantId from flownode_instance"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("flownode_instance", "tenantId")
            hasPrimaryKeyOnTable("flownode_instance", "pk_flownode_instance")
            !hasForeignKeyOnTable("flownode_instance", "fk_flownode_instance_tenantId")
            hasForeignKeyOnTable("ref_biz_data_inst", "fk_ref_biz_data_fn")
            hasForeignKeyOnTable("pending_mapping", "fk_pending_mapping_flownode_instanceId")
        }
    }
}
