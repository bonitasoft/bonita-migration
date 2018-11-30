package org.bonitasoft.migration.version.to7_1_2

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class QrtzSimpPropTriggersIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.1.2")
    }

    def cleanup() {
        dbUnitHelper.dropTables([
                "QRTZ_FIRED_TRIGGERS",
                "QRTZ_PAUSED_TRIGGER_GRPS",
                "QRTZ_SCHEDULER_STATE",
                "QRTZ_LOCKS",
                "QRTZ_SIMPLE_TRIGGERS",
                "QRTZ_SIMPROP_TRIGGERS",
                "QRTZ_CRON_TRIGGERS",
                "QRTZ_BLOB_TRIGGERS",
                "QRTZ_TRIGGERS",
                "QRTZ_JOB_DETAILS",
                "QRTZ_CALENDARS"] as String[])
    }

    @Unroll
    def "platform created in #version should rename qrtz_simprop_triggers columns"(String version) {
        setup:
        dbUnitHelper.createTables("$version/quartzTables", "createQuartzTables")
        println("""dbUnitHelper.hasColumnOnTable("qrtz_simprop_triggers", "int8_prop_1") in version $version :""" + dbUnitHelper.hasColumnOnTable("qrtz_simprop_triggers", "int8_prop_1"))
        println("""dbUnitHelper.hasColumnOnTable("qrtz_simprop_triggers", "int8_prop_2") in version $version :""" + dbUnitHelper.hasColumnOnTable("qrtz_simprop_triggers", "int8_prop_2"))

        when:
        new MigrateQuartzRenameColumn().execute(migrationContext)

        then:
        dbUnitHelper.hasColumnOnTable("qrtz_simprop_triggers", "long_prop_1")
        dbUnitHelper.hasColumnOnTable("qrtz_simprop_triggers", "long_prop_2")

        where:
        version << ["6_4_2", "6_5_0"]

    }

}
