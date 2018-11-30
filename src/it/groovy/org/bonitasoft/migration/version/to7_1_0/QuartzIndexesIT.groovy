package org.bonitasoft.migration.version.to7_1_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author Elias Ricken de Medeiros
 */
class QuartzIndexesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        //simulate migration step to v 7.1.0
        migrationContext.setVersion("7.1.0")
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
    def "platform created in #version should create indexes on quartz table "(String version) {
        setup:
        dbUnitHelper.createTables("$version/quartzTables", "createQuartzTables")

        when:
        new MigrateQuartzIndexes().execute(migrationContext)

        then:
        dbUnitHelper.hasIndexOnTable("qrtz_job_details", "idx_qrtz_j_req_recovery")
        dbUnitHelper.hasIndexOnTable("qrtz_job_details", "idx_qrtz_j_grp")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_j")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_jg")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_c")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_g")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_state")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_n_state")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_n_g_state")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_next_fire_time")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_nft_st")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_nft_misfire")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_nft_st_misfire")
        dbUnitHelper.hasIndexOnTable("qrtz_triggers", "idx_qrtz_t_nft_st_misfire_grp")
        dbUnitHelper.hasIndexOnTable("qrtz_fired_triggers", "idx_qrtz_ft_trig_inst_name")
        dbUnitHelper.hasIndexOnTable("qrtz_fired_triggers", "idx_qrtz_ft_inst_job_req_rcvry")
        dbUnitHelper.hasIndexOnTable("qrtz_fired_triggers", "idx_qrtz_ft_j_g")
        dbUnitHelper.hasIndexOnTable("qrtz_fired_triggers", "idx_qrtz_ft_jg")
        dbUnitHelper.hasIndexOnTable("qrtz_fired_triggers", "idx_qrtz_ft_t_g")
        dbUnitHelper.hasIndexOnTable("qrtz_fired_triggers", "idx_qrtz_ft_tg")

        if (!"mysql".equals(migrationContext.dbVendor)) {
            //mysql primary key is always named "primary"
            dbUnitHelper.getPrimaryKey("qrtz_triggers") == "pk_quartz_triggers"
        }

        dbUnitHelper.hasForeignKeyOnTable("qrtz_simple_triggers", "fk_qrtz_simple_triggers")
        dbUnitHelper.hasForeignKeyOnTable("qrtz_simprop_triggers", "fk_qrtz_simprop_triggers")
        dbUnitHelper.hasForeignKeyOnTable("qrtz_triggers", "fk_qrtz_triggers")

        where:
        version << ["6_4_2", "6_5_0", "7_1_0"]

    }

}
