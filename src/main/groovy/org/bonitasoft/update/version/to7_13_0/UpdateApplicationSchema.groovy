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
package org.bonitasoft.update.version.to7_13_0


import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class UpdateApplicationSchema extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            addColumnIfNotExist("business_app", "iconMimeType", VARCHAR(255), null, null)
            addColumnIfNotExist("business_app", "iconContent", BLOB(), null, null)
            addColumnIfNotExist("business_app", "editable", BOOLEAN(), booleanValue(true), null)
            addColumnIfNotExist("business_app", "internalProfile", VARCHAR(255), null, null)
        }
    }

    @Override
    String getDescription() {
        return "add new columns in `business_app` table"
    }
}
