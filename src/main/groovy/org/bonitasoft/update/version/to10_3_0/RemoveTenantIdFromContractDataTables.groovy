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

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Remove tenantId from contract_data and arch_contract_data tables
 */
class RemoveTenantIdFromContractDataTables extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            // no FK for those tables

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            dropIndexIfExists("contract_data", "idx_cd_kind_scope_name")
            dropIndexIfExists("arch_contract_data", "idx_acd_kind_scope_name")

            // recreate PK:
            dropPrimaryKey("contract_data")
            createPrimaryKey("contract_data", "id", "scopeId")
            dropPrimaryKey("arch_contract_data")
            createPrimaryKey("arch_contract_data", "id", "scopeId")

            // recreate UK:
            dropUniqueKey("contract_data", "uc_cd_scope_name")
            createUniqueKey("contract_data", "uk_contract_data_kind_scopeid_name", "kind", "scopeId", "name")
            dropUniqueKey("arch_contract_data", "uc_acd_scope_name")
            createUniqueKey("arch_contract_data", "uk_arch_contract_data_kind_scopeid_name", "kind", "scopeId", "name")

            // recreate FK:
            // no FK for those tables

            // drop the columns:
            dropColumnIfExists("contract_data", "tenantId")
            dropColumnIfExists("arch_contract_data", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'contract_data' and 'arch_contract_data' tables"
    }
}
