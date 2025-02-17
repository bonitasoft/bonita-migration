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

class CreateBpmFailureTablesIT extends AbstractTestTo10_3_0 {

    private CreateBpmFailureTables updateStep = new CreateBpmFailureTables()

    def "should create bpm failure tables"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        updateContext.databaseHelper.with {
            hasTable("bpm_failure")
            hasColumnOnTable("bpm_failure", "id")
            hasColumnOnTable("bpm_failure", "failureDate")
            hasColumnOnTable("bpm_failure", "processDefinitionId")
            hasColumnOnTable("bpm_failure", "processInstanceId")
            hasColumnOnTable("bpm_failure", "rootProcessInstanceId")
            hasColumnOnTable("bpm_failure", "flowNodeInstanceId")
            hasColumnOnTable("bpm_failure", "scope")
            hasColumnOnTable("bpm_failure", "context")
            hasColumnOnTable("bpm_failure", "errorMessage")
            hasColumnOnTable("bpm_failure", "stackTrace")
            hasPrimaryKeyOnTable("bpm_failure", "pk_bpm_failure")

            hasTable("arch_bpm_failure")
            hasColumnOnTable("arch_bpm_failure", "id")
            hasColumnOnTable("arch_bpm_failure", "failureDate")
            hasColumnOnTable("arch_bpm_failure", "processDefinitionId")
            hasColumnOnTable("arch_bpm_failure", "processInstanceId")
            hasColumnOnTable("arch_bpm_failure", "rootProcessInstanceId")
            hasColumnOnTable("arch_bpm_failure", "flowNodeInstanceId")
            hasColumnOnTable("arch_bpm_failure", "scope")
            hasColumnOnTable("arch_bpm_failure", "context")
            hasColumnOnTable("arch_bpm_failure", "errorMessage")
            hasColumnOnTable("arch_bpm_failure", "stackTrace")
            hasColumnOnTable("arch_bpm_failure", "sourceObjectId")
            hasColumnOnTable("arch_bpm_failure", "archiveDate")
            hasPrimaryKeyOnTable("arch_bpm_failure", "pk_arch_bpm_failure")

            // validate new sequence presence
            getSequenceValue(-1, 6) != null
            getSequenceValue(-1, 7) != null
        }
    }
}
