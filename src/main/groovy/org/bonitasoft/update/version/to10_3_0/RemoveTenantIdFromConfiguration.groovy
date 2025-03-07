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
 * Remove tenantId from 'configuration' tables
 * @author Emmanuel Duchastenier
 */
class RemoveTenantIdFromConfiguration extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // recreate index:
            dropIndexIfExists("configuration", "idx_configuration")
            createIndex("configuration", "idx_configuration", "content_type")

            // recreate PK:
            recreatePrimaryKey("configuration", "content_type", "resource_name")

            // drop the columns:
            dropColumnIfExists("configuration", "tenant_id")

            // remove all TENANT_TEMPLATE_* rows from configuration table:
            sql.execute("DELETE FROM configuration WHERE content_type LIKE 'TENANT_TEMPLATE_%'")

            // remove unused file 'platform-tenant-config.properties':
            sql.execute("DELETE FROM configuration WHERE resource_name = 'platform-tenant-config.properties'")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'configuration' table. Also remove tenant template configurations and unused files."
    }
}
