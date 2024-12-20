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

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractTestTo10_3_0 extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    def setup() {
        dropTestTables()
        updateContext.setVersion("10.3.0")
        createTestTables()
    }

    def cleanup() {
        dropTestTables()
    }

    def createTestTables() {
        dbUnitHelper.createTables("10_3_0")
    }

    def dropTestTables() {
        dbUnitHelper.dropTables([
            "bpm_failure",
            "arch_bpm_failure",
            "sequence",
            "ref_biz_data_inst",
            "pending_mapping",
            "flownode_instance",
            "tenant",
            "form_mapping",
            "page_mapping"
        ] as String[])
    }
}
