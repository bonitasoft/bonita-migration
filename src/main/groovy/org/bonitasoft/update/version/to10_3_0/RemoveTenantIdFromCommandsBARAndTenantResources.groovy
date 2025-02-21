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

import static org.bonitasoft.update.core.UpdateStep.DBVendor.ORACLE

/**
 * Remove tenantId from tables 'command', 'bar_resource', 'tenant_resource'
 */
class RemoveTenantIdFromCommandsBARAndTenantResources extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            if (dbVendor != ORACLE) {
                // Does not exist on Oracle
                dropForeignKey("command", "fk_command_tenantId")
            }

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            dropIndexIfExists("bar_resource", "idx_bar_resource")
            dropIndexIfExists("tenant_resource", "idx_tenant_resource")

            // recreate PK:
            recreatePrimaryKey("command")
            recreatePrimaryKey("bar_resource")
            recreatePrimaryKey("tenant_resource")

            // recreate UK:
            dropUniqueKeyFromColumns("command", "tenantid", "name")
            createUniqueKey("command", "uk_command_name", "name")
            dropUniqueKeyFromColumns("bar_resource", "tenantid", "process_id", "name", "type")
            createUniqueKey("bar_resource", "uk_bar_resource_processid_name_type", "process_id", "name", "type")
            dropUniqueKeyFromColumns("tenant_resource", "tenantid", "name", "type")
            createUniqueKey("tenant_resource", "uk_tenant_resource_name_type", "name", "type")

            // recreate FK:
            // no FK to recreate for those tables

            // drop the columns:
            dropColumnIfExists("command", "tenantId")
            dropColumnIfExists("bar_resource", "tenantId")
            dropColumnIfExists("tenant_resource", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'command', 'bar_resource' and 'tenant_resource' tables"
    }
}
