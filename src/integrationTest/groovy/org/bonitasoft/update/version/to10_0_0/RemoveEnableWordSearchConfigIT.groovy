package org.bonitasoft.update.version.to10_0_0

import groovy.sql.GroovyRowResult
import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveEnableWordSearchConfigIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveEnableWordSearchConfig updateStep = new RemoveEnableWordSearchConfig()

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

    def "should remove the enableWordSearch from configuration file"() {
        given:
        byte[] confContent = this.getClass().getResourceAsStream("/conf/bonita-platform-community-custom.properties").text.bytes
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "PLATFORM_ENGINE", 'bonita-platform-community-custom.properties', confContent)

        when:
        updateStep.execute(updateContext)
        List<GroovyRowResult> newConfRaw = updateContext.sql.rows("SELECT RESOURCE_CONTENT FROM configuration WHERE RESOURCE_NAME = 'bonita-platform-community-custom.properties'")

        then:
        newConfRaw.size() == 1
        def confString = updateContext.databaseHelper.getBlobContentAsString(newConfRaw.get(0).getProperty("resource_content"))
        !confString.containsIgnoreCase("enableWordSearch")
        confString.containsIgnoreCase("bonita.platform.persistence.platform.likeEscapeCharacter=#\n" +
                "#\n" + "#\n" + "#")
    }
}
