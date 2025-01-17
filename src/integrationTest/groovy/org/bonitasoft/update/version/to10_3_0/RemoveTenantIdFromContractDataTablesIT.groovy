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

class RemoveTenantIdFromContractDataTablesIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromContractDataTables updateStep = new RemoveTenantIdFromContractDataTables()

    def "should remove tenantId from contract data tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("contract_data", "tenantId")
            hasPrimaryKeyOnTable("contract_data", "pk_contract_data")
            !hasUniqueKeyOnTableWithColumns("contract_data", "kind", "scopeId", "name", "tenantid")
            hasUniqueKeyOnTableWithNameAndColumns("contract_data", "uk_contract_data_kind_scopeid_name", "kind", "scopeId", "name")
            !hasIndexOnTable("contract_data", "idx_cd_kind_scope_name")

            !hasColumnOnTable("arch_contract_data", "tenantId")
            hasPrimaryKeyOnTable("arch_contract_data", "pk_arch_contract_data")
            !hasUniqueKeyOnTableWithColumns("arch_contract_data", "kind", "scopeId", "name", "tenantid")
            hasUniqueKeyOnTableWithNameAndColumns("arch_contract_data", "uk_arch_contract_data_kind_scopeid_name", "kind", "scopeId", "name")
            !hasIndexOnTable("arch_contract_data", "idx_acd_kind_scope_name")
        }
    }
}
