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
package org.bonitasoft.update.version.to11_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class AddTemporaryContentLargeObjectCleanupTriggerPostgres extends UpdateStep {

    private static final String SCRIPT_PATH =
    "/version/to_11_0_0/AddTemporaryContentLargeObjectCleanupTrigger/postgres_.sql"

    @Override
    def execute(UpdateContext context) {
        if (context.dbVendor != DBVendor.POSTGRES) {
            context.logger.info("Skipping LO cleanup trigger: not PostgreSQL (vendor: ${context.dbVendor})")
            return
        }

        // Important: table might not exist depending on upgrade path / snapshot
        if (!context.databaseHelper.hasTable("temporary_content")) {
            context.logger.info("Skipping LO cleanup trigger: table temporary_content does not exist")
            return
        }

        def stream = this.class.getResourceAsStream(SCRIPT_PATH)
        if (stream == null) {
            throw new IllegalStateException("Missing SQL resource: ${SCRIPT_PATH}")
        }
        def sqlText = stream.withStream { it.text }
        // Here we CANNOT use directly 'context.databaseHelper.executeScript()' because the script contains `;` inside the DB function:
        context.sql.execute(sqlText)
    }

    @Override
    String getDescription() {
        return "Add PostgreSQL trigger to cleanup large objects referenced by temporary_content.content"
    }
}
