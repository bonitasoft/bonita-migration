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

class RemoveTenantIdFromCommandsBARAndTenantResourcesIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromCommandsBARAndTenantResources updateStep = new RemoveTenantIdFromCommandsBARAndTenantResources()

    def "should remove tenantId from tables 'command', 'bar_resource', 'tenant_resource'"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("command", "tenantId")
            hasPrimaryKeyOnTable("command", "pk_command")
            !hasForeignKeyOnTable("command", "fk_command_tenantId")
            !hasUniqueKeyOnTableWithColumns("command", "tenantid", "name")
            hasUniqueKeyOnTableWithNameAndColumns("command", "uk_command_name", "name")

            !hasColumnOnTable("bar_resource", "tenantId")
            hasPrimaryKeyOnTable("bar_resource", "pk_bar_resource")
            !hasUniqueKeyOnTableWithColumns("bar_resource", "tenantid", "process_id", "name", "type")
            hasUniqueKeyOnTableWithNameAndColumns("bar_resource", "uk_bar_resource_processid_name_type", "process_id", "name", "type")
            !hasIndexOnTable("bar_resource", "idx_bar_resource")

            !hasColumnOnTable("tenant_resource", "tenantId")
            hasPrimaryKeyOnTable("tenant_resource", "pk_tenant_resource")
            !hasUniqueKeyOnTableWithColumns("tenant_resource", "tenantid", "name", "type")
            hasUniqueKeyOnTableWithNameAndColumns("tenant_resource", "uk_tenant_resource_name_type", "name", "type")
            !hasIndexOnTable("tenant_resource", "idx_tenant_resource")
        }
    }
}
