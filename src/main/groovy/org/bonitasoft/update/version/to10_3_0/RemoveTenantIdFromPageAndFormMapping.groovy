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

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Remove tenantId from page_mapping and form_mapping tables
 */
class RemoveTenantIdFromPageAndFormMapping extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            dropForeignKey("form_mapping", "fk_form_mapping_key")

            // recreate PK:
            dropPrimaryKey("page_mapping")
            createPrimaryKey("page_mapping", "id")
            dropPrimaryKey("form_mapping")
            createPrimaryKey("form_mapping", "id")

            // recreate UK:
            dropUniqueKeyFromColumns("page_mapping", "tenantId", "key_")
            createUniqueKey("page_mapping", "uk_page_mapping_key", "key_")

            // recreate FK:
            createForeignKey("form_mapping", "fk_form_mapping_key", "page_mapping", ["page_mapping_id"], ["id"], false)

            // drop the columns:
            dropColumnIfExists("form_mapping", "tenantId")
            dropColumnIfExists("form_mapping", "page_mapping_tenant_id")
            dropColumnIfExists("page_mapping", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'page_mapping' and 'form_mapping' tables"
    }
}
