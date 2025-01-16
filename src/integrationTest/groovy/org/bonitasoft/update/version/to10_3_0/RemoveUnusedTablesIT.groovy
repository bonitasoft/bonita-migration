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

class RemoveUnusedTablesIT extends AbstractTestTo10_3_0 {

    private RemoveUnusedTables updateStep = new RemoveUnusedTables()

    @Override
    def createTestTables() {
        dbUnitHelper.createTables("10_3_0/unused_tables")
    }

    @Override
    def dropTestTables() {
        dbUnitHelper.dropTables(["external_identity_mapping", "queriablelog_p", "queriablelog", "blob_", "sequence"] as String[])
    }

    def "should drop tables 'queriablelog_p' and 'blob_'"() {
        given:
        assert updateContext.databaseHelper.hasTable("external_identity_mapping")
        assert updateContext.databaseHelper.getSequenceValue(1, 10070) != null
        assert updateContext.databaseHelper.hasTable("queriablelog_p")
        assert updateContext.databaseHelper.getSequenceValue(1, 31) != null
        assert updateContext.databaseHelper.hasTable("blob_")

        when:
        updateStep.execute(updateContext)

        then:
        !updateContext.databaseHelper.hasTable("external_identity_mapping")
        updateContext.databaseHelper.getSequenceValue(1, 10070) == null
        !updateContext.databaseHelper.hasTable("queriablelog_p")
        updateContext.databaseHelper.getSequenceValue(1, 31) == null
        !updateContext.databaseHelper.hasTable("blob_")
    }
}
