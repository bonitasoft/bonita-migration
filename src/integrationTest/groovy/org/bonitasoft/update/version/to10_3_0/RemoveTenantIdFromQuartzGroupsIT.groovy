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


import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveTenantIdFromQuartzGroupsIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveTenantIdFromQuartzGroups updateStep = new RemoveTenantIdFromQuartzGroups()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("10_3_0/quartz")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables([
            "QRTZ_BLOB_TRIGGERS",
            "QRTZ_SIMPROP_TRIGGERS",
            "QRTZ_SIMPLE_TRIGGERS",
            "QRTZ_PAUSED_TRIGGER_GRPS",
            "QRTZ_FIRED_TRIGGERS",
            "QRTZ_CRON_TRIGGERS",
            "QRTZ_TRIGGERS",
            "QRTZ_JOB_DETAILS"
        ] as String[])
    }


    def "should not fail when updating Quartz table contents"() {
        when:
        updateStep.execute(updateContext)

        then:
        noExceptionThrown()
    }
}
