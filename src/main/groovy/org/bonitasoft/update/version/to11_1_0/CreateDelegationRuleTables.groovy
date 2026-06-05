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

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Creates the {@code delegation_rule} and {@code delegation_rule_process} tables introduced by the Task Delegation
 * feature, along with the {@code idx_delegation_rule_delegate_id} index, and registers their Hibernate sequences.
 */
class CreateDelegationRuleTables extends UpdateStep {

    static final long DELEGATION_RULE_SEQUENCE_ID = 20097L
    static final long DELEGATION_RULE_PROCESS_SEQUENCE_ID = 20098L

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            if (!hasTable("delegation_rule")) {
                executeScript("CreateDelegationRule")
            }
            if (!hasTable("delegation_rule_process")) {
                executeScript("CreateDelegationRuleProcess")
            }
            noMoreTenant.addSequenceIfNotExists(DELEGATION_RULE_SEQUENCE_ID, 1)
            noMoreTenant.addSequenceIfNotExists(DELEGATION_RULE_PROCESS_SEQUENCE_ID, 1)
        }
    }

    @Override
    String getDescription() {
        return "Create 'delegation_rule' and 'delegation_rule_process' tables"
    }
}
