package org.bonitasoft.update.version.to10_0_0

import groovy.sql.GroovyRowResult
import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddEnableDynamicCheckConfigIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddEnableDynamicCheckConfig updateStep = new AddEnableDynamicCheckConfig()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("10_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    def "should add the enable dynamic check property to configuration file"() {
        given:
        byte[] confContent = this.getClass().getResourceAsStream("/conf/bonita-tenant-community-custom.properties").text.bytes
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_ENGINE", 'bonita-tenant-community-custom.properties', confContent)

        when:
        updateStep.execute(updateContext)
        List<GroovyRowResult> newConfRaw = updateContext.sql.rows("SELECT RESOURCE_CONTENT FROM configuration WHERE RESOURCE_NAME = 'bonita-tenant-community-custom.properties'")

        then:
        newConfRaw.size() == 1
        def confString = updateContext.databaseHelper.getBlobContentAsString(newConfRaw.get(0).getProperty("resource_content"))
        confString.containsIgnoreCase("## Set this value to false to disable any dynamic permissions totally\n")
        confString.containsIgnoreCase("#bonita.runtime.authorization.dynamic-check.enabled=true")
    }
}
