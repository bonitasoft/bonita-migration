package org.bonitasoft.migration.version.to7_6_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class RemovePlatformInitCommunityCustomPropertiesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    private RemovePlatformInitCommunityCustomProperties migrationStep = new RemovePlatformInitCommunityCustomProperties()

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.3.1")
        dbUnitHelper.createTables("7_3_1/configuration")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }


    def "should remove the bonita-platform-init-community-custom.properties file from PLATFORM_INIT_ENGINE"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "PLATFORM_INIT_ENGINE", 'bonita-platform-init-community-custom.properties', 'somebytes'.bytes)
        assert 1 == dbUnitHelper.countConfigFileWithContent("PLATFORM_INIT_ENGINE", 'bonita-platform-init-community-custom.properties', 'somebytes')

        when:
        migrationStep.execute(migrationContext)

        then:
        0 == dbUnitHelper.countConfigFileWithContent("PLATFORM_INIT_ENGINE", 'bonita-platform-init-community-custom.properties', 'somebytes')

    }
}
