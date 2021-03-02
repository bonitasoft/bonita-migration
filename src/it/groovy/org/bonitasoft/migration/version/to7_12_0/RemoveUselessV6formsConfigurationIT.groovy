/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_12_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.version.to7_13_0.RemoveUselessV6formsConfiguration
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class RemoveUselessV6formsConfigurationIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    private RemoveUselessV6formsConfiguration migrationStep = new RemoveUselessV6formsConfiguration()

    def setup() {
        dropTestTables()
        // Schema is the same, so reusing 7.4.2 SQL resource files:
        dbUnitHelper.createTables("7_4_2", "configuration")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }


    def "should remove the 'forms-config.properties' file from Template and any tenant"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_PORTAL", 'forms-config.properties', 'some bytes'.bytes)
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_PORTAL", 'forms-config.properties', 'some bytes'.bytes)
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                101L, "TENANT_PORTAL", 'forms-config.properties', 'BINARY CONTENT'.bytes)
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                102L, "TENANT_PORTAL", 'forms-config.properties', 'other tenant binary'.bytes)
        assert 4 == dbUnitHelper.countConfigFileWithNameOfAnyType('forms-config.properties')

        when:
        migrationStep.execute(migrationContext)

        then:
        0 == dbUnitHelper.countConfigFileWithNameOfAnyType('forms-config.properties')

    }
}
