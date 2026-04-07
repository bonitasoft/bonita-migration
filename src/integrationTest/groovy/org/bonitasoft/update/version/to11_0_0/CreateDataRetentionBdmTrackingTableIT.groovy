/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to11_0_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class CreateDataRetentionBdmTrackingTableIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private CreateDataRetentionBdmTrackingTable updateStep = new CreateDataRetentionBdmTrackingTable()

    def setup() {
        dropTestTables()
        updateContext.setVersion("11.0.0")
        dbUnitHelper.createTables("11_0_0/sequence")
    }

    def cleanup() {
        dropTestTables()
    }

    private dropTestTables() {
        dbUnitHelper.dropTables(["data_retention_bdm_tracking", "sequence"] as String[])
    }

    def "should create data_retention_bdm_tracking table"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            hasTable("data_retention_bdm_tracking")
            hasColumnOnTable("data_retention_bdm_tracking", "id")
            hasColumnOnTable("data_retention_bdm_tracking", "data_id")
            hasColumnOnTable("data_retention_bdm_tracking", "data_classname")
            hasColumnOnTable("data_retention_bdm_tracking", "created_at")
            hasColumnOnTable("data_retention_bdm_tracking", "last_modified_at")
            hasPrimaryKeyOnTable("data_retention_bdm_tracking", "pk_data_retention_bdm_tracking")
        }
        // validate index
        dbUnitHelper.hasIndexOnTable("data_retention_bdm_tracking", "idx_data_retention_bdm_tracking_data_classname")
        // validate new sequence presence
        updateContext.sql.firstRow("SELECT nextid FROM sequence WHERE id = 9") != null
    }

    def "should be idempotent"() {
        when:
        updateStep.execute(updateContext)
        updateStep.execute(updateContext)

        then:
        noExceptionThrown()
        updateContext.databaseHelper.hasTable("data_retention_bdm_tracking")
    }
}
