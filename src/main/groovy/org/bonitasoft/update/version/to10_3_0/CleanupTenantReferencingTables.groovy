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
 * Remove tenantId from 'sequence' table and delete 'tenant' table
 */
class CleanupTenantReferencingTables extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            recreatePrimaryKey("sequence")
            dropColumnIfExists("sequence", "tenantid")

            recreatePrimaryKey("platform")

            String status = context.sql.firstRow("select status from tenant")['status'] as String
            addColumnIfNotExist("platform", "maintenance_enabled", BOOLEAN(), booleanValue("ACTIVATED" != status), "NOT NULL")

            dropTableIfExists("tenant")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'sequence' table & delete 'tenant' table"
    }
}
