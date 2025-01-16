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
 * Remove unused tables external_identity_mapping, queriablelog_p and blob_
 *
 * @author Emmanuel Duchastenier
 */
class RemoveUnusedTables extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            dropTableIfExists("external_identity_mapping")
            context.sql.executeUpdate("DELETE FROM sequence WHERE id = ${10070} ")

            dropTableIfExists("queriablelog_p")
            context.sql.executeUpdate("DELETE FROM sequence WHERE id = ${31} ")

            dropTableIfExists("blob_")
            // blob_ table did not have any sequence!
        }
    }

    @Override
    String getDescription() {
        return "Remove unused tables 'external_identity_mapping', 'queriablelog_p' and 'blob_'"
    }
}
