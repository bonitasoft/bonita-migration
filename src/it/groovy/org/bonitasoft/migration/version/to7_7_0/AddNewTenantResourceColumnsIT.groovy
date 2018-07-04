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
package org.bonitasoft.migration.version.to7_7_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AddNewTenantResourceColumnsIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    private AddNewTenantResourceColumns migrationStep = new AddNewTenantResourceColumns()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_7_0/tenant_resource")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["tenant_resource"] as String[])
    }

    @Unroll
    def "should add columns in the tenant_resource table with default values for existing rows"() {
        given:
        def content = 'an arbitrary content'
        migrationContext.sql.execute("""
insert into tenant_resource(tenantId , id, name, type, content)
values (1, 1, 'resource', 'TEST', $content.bytes)
""")

        when:
        migrationStep.execute(migrationContext)

        then:
        def tenantResource = migrationContext.sql.firstRow("SELECT * FROM tenant_resource WHERE tenantId  = 1 and id = 1")

        tenantResource.lastUpdatedBy == -1
        tenantResource.lastUpdateDate == 0
        tenantResource.state == 'INSTALLED'
    }

}