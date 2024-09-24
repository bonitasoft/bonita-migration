/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class RemoveUnusedSequences extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            execute("DELETE FROM sequence WHERE tenantId = -1 AND id = 30")
            execute("DELETE FROM sequence WHERE tenantId = -1 AND id = 31")
        }
    }

    @Override
    String getDescription() {
        return "Remove unused Bonita sequences"
    }
}
