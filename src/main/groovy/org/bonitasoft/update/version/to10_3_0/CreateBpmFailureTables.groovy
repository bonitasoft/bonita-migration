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

class CreateBpmFailureTables extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.executeScript("CreateBpmFailureTable", "")
        context.databaseHelper.insertSequences(Map.of(-1L, 1L) , 6)
        context.databaseHelper.executeScript("CreateArchBpmFailureTable", "")
        context.databaseHelper.insertSequences(Map.of(-1L, 1L) , 7)
    }

    @Override
    String getDescription() {
        return "Create 'bpm_failure' and 'arch_bpm_failure' tables"
    }
}
