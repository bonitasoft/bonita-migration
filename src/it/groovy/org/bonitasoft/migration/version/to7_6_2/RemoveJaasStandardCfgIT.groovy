package org.bonitasoft.migration.version.to7_6_2

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Anthony Birembaut
 */
class RemoveJaasStandardCfgIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    private RemoveJaasStandardCfg migrationStep = new RemoveJaasStandardCfg()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_4_2", "configuration")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }


    def "should remove the jaas-standard.cfg file from PLATFORM_PORTAL"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "PLATFORM_PORTAL", 'jaas-standard.cfg', 'somebytes'.bytes)
        assert 1 == dbUnitHelper.countConfigFileWithName("PLATFORM_PORTAL", 'jaas-standard.cfg')

        when:
        migrationStep.execute(migrationContext)

        then:
        0 == dbUnitHelper.countConfigFileWithName("PLATFORM_PORTAL", 'jaas-standard.cfg')

    }
}
