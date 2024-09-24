package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveWorkDelayOnXAResourcePropertiesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveWorkDelayOnXAResourceProperties updateStep = new RemoveWorkDelayOnXAResourceProperties()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_13_0/configuration")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }


    def "should remove work delay properties"() {
        def propFileContent = """
bonita.tenant.work.postgres.delayOnMultipleXAResource=1000
bonita.tenant.work.sqlserver.delayOnMultipleXAResource=1000
bonita.tenant.work.oracle.delayOnMultipleXAResource=1000
bonita.tenant.work.mysql.delayOnMultipleXAResource=1000
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_ENGINE", RemoveWorkDelayOnXAResourceProperties.CONFIG_FILE_NAME, propFileContent.bytes)
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_ENGINE", RemoveWorkDelayOnXAResourceProperties.CONFIG_FILE_NAME, propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """
bonita.tenant.work.postgres.delayOnMultipleXAResource=1000
"""
        List updatedRows = updateContext.sql.rows("SELECT tenant_id, content_type, resource_name, resource_content FROM configuration order by tenant_id ASC")

        updatedRows.size() == 2
        updatedRows[0].tenant_id == 0L
        updatedRows[0].content_type == "TENANT_TEMPLATE_ENGINE"
        updatedRows[0].resource_name == RemoveWorkDelayOnXAResourceProperties.CONFIG_FILE_NAME
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content)  == expectedPropFileContent
        updatedRows[1].tenant_id == 1L
        updatedRows[1].content_type == "TENANT_ENGINE"
        updatedRows[1].resource_name == RemoveWorkDelayOnXAResourceProperties.CONFIG_FILE_NAME
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[1].resource_content)  == expectedPropFileContent

    }

}