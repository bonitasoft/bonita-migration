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

class RemoveTenantIdFromConfigurationIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromConfiguration updateStep = new RemoveTenantIdFromConfiguration()

    def "should remove tenantId from configuration table"() {
        given:
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_PORTAL", 'forms-config.properties', 'some bytes'.bytes)
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_ENGINE", "bonita-tenant-community-custom.properties" , "some content".bytes)
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_SECURITY_SCRIPTS", "SamplePermissionRule.groovy.sample", "groovy content".bytes)
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_PORTAL", 'forms-config.properties', 'some form content'.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("configuration", "tenant_id")
            hasPrimaryKeyOnTable("configuration", "pk_configuration")
            hasIndexOnTable("configuration", "idx_configuration")
        }
        updateContext.sql.rows("SELECT 1 FROM configuration WHERE content_type LIKE 'TENANT_TEMPLATE_%'").isEmpty()

        def rows = updateContext.sql.rows("SELECT resource_name FROM configuration")
        rows.size() == 1
        rows[0].resource_name == 'forms-config.properties'
    }
}
