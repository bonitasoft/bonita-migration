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
    }

}