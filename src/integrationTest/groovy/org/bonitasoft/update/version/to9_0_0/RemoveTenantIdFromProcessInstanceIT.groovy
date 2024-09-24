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
package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveTenantIdFromProcessInstanceIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveTenantIdFromProcessInstance updateStep = new RemoveTenantIdFromProcessInstance()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("9_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["temporary_content", "sequence", "platform", "ref_biz_data_inst", "process_instance", "tenant"] as String[])
    }


    def "should remove tenantId from process_instance"() {
        when:
        updateStep.execute(updateContext)

        then:
        ! updateContext.databaseHelper.hasColumnOnTable("process_instance", "tenantId")
        updateContext.databaseHelper.hasPrimaryKeyOnTable("process_instance", "pk_process_instance")
        ! updateContext.databaseHelper.hasForeignKeyOnTable("process_instance", "fk_process_instance_tenantId")
        updateContext.databaseHelper.hasIndexOnTable("process_instance", "idx1_proc_inst_pdef_state")
        updateContext.databaseHelper.hasForeignKeyOnTable("ref_biz_data_inst", "fk_ref_biz_data_proc")
        updateContext.databaseHelper.hasIndexOnTable("ref_biz_data_inst", "idx_biz_data_inst3")
    }
}
