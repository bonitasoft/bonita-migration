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

class RemoveTenantIdFromPageAndFormMappingIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromPageAndFormMapping updateStep = new RemoveTenantIdFromPageAndFormMapping()

    def "should remove tenantId from page_mapping and form_mapping"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("page_mapping", "tenantId")
            hasPrimaryKeyOnTable("page_mapping", "pk_page_mapping")
            !hasUniqueKeyOnTableWithColumns("page_mapping", "tenantId", "key_")
            hasUniqueKeyOnTableWithNameAndColumns("page_mapping", "uk_page_mapping_key", "key_")

            !hasColumnOnTable("form_mapping", "tenantId")
            !hasColumnOnTable("form_mapping", "page_mapping_tenant_id")
            hasPrimaryKeyOnTable("form_mapping", "pk_form_mapping")
            hasForeignKeyOnTable("form_mapping", "fk_form_mapping_key")
        }
    }
}
