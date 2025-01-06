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
        !updateContext.databaseHelper.hasColumnOnTable("page_mapping", "tenantId")
        updateContext.databaseHelper.hasPrimaryKeyOnTable("page_mapping", "pk_page_mapping")
        !updateContext.databaseHelper.hasUniqueKeyOnTableByColumns("page_mapping", "tenantId", "key_")
        updateContext.databaseHelper.hasUniqueKeyOnTable("page_mapping", "uk_page_mapping_key")
        updateContext.databaseHelper.hasUniqueKeyOnTableByColumns("page_mapping", "key_")

        !updateContext.databaseHelper.hasColumnOnTable("form_mapping", "tenantId")
        !updateContext.databaseHelper.hasColumnOnTable("form_mapping", "page_mapping_tenant_id")
        updateContext.databaseHelper.hasPrimaryKeyOnTable("form_mapping", "pk_form_mapping")
        updateContext.databaseHelper.hasForeignKeyOnTable("form_mapping", "fk_form_mapping_key")
    }
}
