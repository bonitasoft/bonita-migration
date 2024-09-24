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
package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * @author Emmanuel Duchastenier
 */
class RefactorPlatformColumns extends UpdateStep {

    public static final String PLATFORM_TABLE = "platform"

    @Override
    def execute(UpdateContext context) {
        def helper = context.databaseHelper
        if (helper.hasColumnOnTable(PLATFORM_TABLE, "initialVersion")) {
            // last parameter is only used by MySQL
            helper.renameColumn(PLATFORM_TABLE, 'initialVersion', 'initial_bonita_version', 'VARCHAR(50)')
        }
        if (helper.hasColumnOnTable(PLATFORM_TABLE, "createdBy")) {
            // last parameter is only used by MySQL
            helper.renameColumn(PLATFORM_TABLE, 'createdBy', 'created_by', 'VARCHAR(50)')
        }
        helper.dropColumnIfExists(PLATFORM_TABLE, "previousVersion")
    }

    @Override
    String getDescription() {
        "Refactor columns in 'platform' table to better server database version scheme"
    }
}
