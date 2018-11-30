/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.migration.version.to7_4_2

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class IsManagerOfUserInvolvedIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.4.2")
        dbUnitHelper.createTables("7_4_2", "configuration")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    def "should migrate form-config.properties"() {
        given:
        migrationContext.sql.execute("""
            INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content)
            VALUES (1, 'TENANT_PORTAL', 'form-config.properties', ${'prop.A    someValue\npropB      true'}.bytes)
            """)

        when:
        new AddManagerInvolvedConfiguration().execute(migrationContext)

        def content = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                AND resource_name = 'form-config.properties'""")["resource_content"])

        then:
        // to be sure appended content is correctly at the beginning of a new line:
        content.eachLine { line ->
            if (line.startsWith("form.authorizations.manager.allowed     false")) {
                return true
            }
        }
    }

    def "should migrate bonita-tenants-custom.xml"() {
        given:
        def currentFileContent = "<beans>\n     someBeans...\n</beans>\n"

        migrationContext.sql.execute("""
            INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content)
            VALUES (1, 'TENANT_PORTAL', 'bonita-tenants-custom.xml', $currentFileContent.bytes)
            """)

        when:
        new AddManagerInvolvedConfiguration().execute(migrationContext)

        String content = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""
                    SELECT resource_content
                    FROM configuration
                    WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                    AND resource_name = 'bonita-tenants-custom.xml'
                    """)["resource_content"])

        then:
        content.contains 'class="org.bonitasoft.engine.core.form.impl.ManagerInvolvedAuthorizationRuleMappingImpl"/>'
        content.contains '<bean id="isManagerOfUserInvolvedInProcessInstanceRule" class="org.bonitasoft.engine.page.IsManagerOfUserInvolvedInProcessInstanceRule">'
    }


    def "should migrate Engine file bonita-tenant-community-custom.properties"() {
        given:
        migrationContext.sql.execute("""
            INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content)
            VALUES (-1, 'TENANT_TEMPLATE_ENGINE', 'bonita-tenant-community-custom.properties', ${'#myPropertyOne=defaultValue\npropertyTwo=customValue'}.bytes)
            """)

        when:
        new AddManagerInvolvedConfiguration().execute(migrationContext)

        def content = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""
                        SELECT resource_content FROM configuration WHERE tenant_id = -1 and content_type = 'TENANT_TEMPLATE_ENGINE'
                        AND resource_name = 'bonita-tenant-community-custom.properties'
                        """)["resource_content"])

        then:
        content.contains "\n# to restore pre-7.3.0 behavior (where manager of user involved in process instance could access Case Overview), use this implementation below instead:\n#bonita.tenant.authorization.rule.mapping=managerInvolvedAuthorizationRuleMappingImpl"
    }

}
