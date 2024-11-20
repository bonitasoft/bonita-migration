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

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.version.to9_0_0.CreateTemporaryContentTable
import spock.lang.Shared
import spock.lang.Specification

class CreateBpmFailureTableIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private CreateBpmFailureTables updateStep = new CreateBpmFailureTables()

    def setup() {
        dropTestTables()
        updateContext.setVersion("10.3.0")
        dbUnitHelper.createTables("10_3_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["bpm_failure", "arch_bpm_failure", "sequence"] as String[])
    }


    def "should create bpm failures table"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        updateContext.databaseHelper.hasTable("bpm_failure")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "id")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "failureDate")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "processDefinitionId")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "processInstanceId")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "flowNodeInstanceId")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "scope")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "context")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "errorMessage")
        updateContext.databaseHelper.hasColumnOnTable("bpm_failure", "stackTrace")

        updateContext.databaseHelper.hasTable("arch_bpm_failure")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "id")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "failureDate")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "processDefinitionId")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "processInstanceId")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "flowNodeInstanceId")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "scope")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "context")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "errorMessage")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "stackTrace")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "sourceObjectId")
        updateContext.databaseHelper.hasColumnOnTable("arch_bpm_failure", "archiveDate")

        // validate new sequence presence
        updateContext.databaseHelper.getSequenceValue(-1, 6) != null
        updateContext.databaseHelper.getSequenceValue(-1, 7) != null
    }
}
