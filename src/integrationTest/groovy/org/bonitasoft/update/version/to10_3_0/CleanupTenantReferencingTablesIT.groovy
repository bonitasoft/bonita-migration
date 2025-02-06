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

class CleanupTenantReferencingTablesIT extends AbstractTestTo10_3_0 {

    @Override
    def createTestTables() {
        // Need to have only those 3 tables, as other tables have FK on tenant table, so we cannot drop it:
        dbUnitHelper.createTables("10_3_0/tenant_sequence")
    }

    @Override
    def dropTestTables() {
        dbUnitHelper.dropTables(["tenant", "sequence", "platform"] as String[])
    }

    private CleanupTenantReferencingTables updateStep = new CleanupTenantReferencingTables()

    def "should remove tenantId from sequence table and delete tenant table"() {
        given:
        updateContext.sql.executeInsert("INSERT INTO tenant(id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                , 101L, 1452271739683, 'system', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                'default', 'ACTIVATED')
        updateContext.sql.executeInsert("""INSERT INTO platform(id, version, initial_bonita_version, application_version,
maintenance_message_active, created, created_by, information) values (?,?,?,?,?,?,?,?)""",
                1L, "10.2", "10.2.0", "0.0.0", false, 112133L, "platformAdmin", null)

        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("sequence", "tenantid")
            hasPrimaryKeyOnTable("sequence", "pk_sequence")

            hasPrimaryKeyOnTable("platform", "pk_platform")
            hasColumnOnTable("platform", "status")
            updateContext.sql.firstRow("select status from platform")['status'] == 'ACTIVATED'
            !hasTable("tenant")
        }
    }
}
