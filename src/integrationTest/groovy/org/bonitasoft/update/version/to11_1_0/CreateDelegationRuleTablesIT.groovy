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
package org.bonitasoft.update.version.to11_1_0

import static CreateDelegationRuleTables.DELEGATION_RULE_PROCESS_SEQUENCE_ID
import static CreateDelegationRuleTables.DELEGATION_RULE_SEQUENCE_ID

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class CreateDelegationRuleTablesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private CreateDelegationRuleTables updateStep = new CreateDelegationRuleTables()

    def setup() {
        dropTestTables()
        updateContext.setVersion("11.1.0")
        dbUnitHelper.createTables("11_1_0/sequence")
    }

    def cleanup() {
        dropTestTables()
    }

    private dropTestTables() {
        // delegation_rule_process must be dropped before delegation_rule because of the FK
        dbUnitHelper.dropTables(["delegation_rule_process", "delegation_rule", "sequence"] as String[])
    }

    def "should create delegation_rule and delegation_rule_process tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            hasTable("delegation_rule")
            hasColumnOnTable("delegation_rule", "id")
            hasColumnOnTable("delegation_rule", "delegator_id")
            hasColumnOnTable("delegation_rule", "delegate_id")
            hasColumnOnTable("delegation_rule", "start_date")
            hasColumnOnTable("delegation_rule", "end_date")
            hasColumnOnTable("delegation_rule", "last_updated_by")
            hasColumnOnTable("delegation_rule", "last_updated_at")
            hasPrimaryKeyOnTable("delegation_rule", "pk_delegation_rule")
            hasUniqueKeyOnTableWithNameAndColumns("delegation_rule",
                    "uk_delegation_rule_delegator_id",
                    "delegator_id")

            hasTable("delegation_rule_process")
            hasColumnOnTable("delegation_rule_process", "id")
            hasColumnOnTable("delegation_rule_process", "delegation_rule_id")
            hasColumnOnTable("delegation_rule_process", "process_name")
            hasPrimaryKeyOnTable("delegation_rule_process", "pk_delegation_rule_process")
            hasUniqueKeyOnTableWithNameAndColumns("delegation_rule_process",
                    "uk_delegation_rule_process_delegation_rule_id_process_name",
                    "delegation_rule_id", "process_name")
            hasForeignKeyOnTable("delegation_rule_process", "fk_delegation_rule_process_delegation_rule_id")

            // validate new sequences presence
            noMoreTenant.getSequenceValue(DELEGATION_RULE_SEQUENCE_ID) != null
            noMoreTenant.getSequenceValue(DELEGATION_RULE_PROCESS_SEQUENCE_ID) != null
        }
        // validate index on delegate_id
        dbUnitHelper.hasIndexOnTable("delegation_rule", "idx_delegation_rule_delegate_id")
    }

    def "should be idempotent"() {
        when:
        updateStep.execute(updateContext)
        updateStep.execute(updateContext)

        then:
        noExceptionThrown()
        updateContext.databaseHelper.hasTable("delegation_rule")
        updateContext.databaseHelper.hasTable("delegation_rule_process")
    }
}
