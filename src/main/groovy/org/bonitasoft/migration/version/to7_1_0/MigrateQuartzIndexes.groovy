package org.bonitasoft.migration.version.to7_1_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.database.DatabaseHelper

/**
 * @author Laurent Leseigneur
 */
class MigrateQuartzIndexes extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def databaseHelper = context.databaseHelper

        //indexes are used by primary and foreign keys
        dropForeignKeys(databaseHelper, "qrtz_job_details")
        dropForeignKeys(databaseHelper, "qrtz_triggers")
        databaseHelper.dropPrimaryKey("qrtz_triggers")

        removeFalsy7_0_0_indexes(databaseHelper)
        recreateIndexesAsIn6_4_2(databaseHelper)

        //recreate primary and foreign key
        databaseHelper.executeScript("Quartz", "constraints")
    }

    def dropForeignKeys(DatabaseHelper dbHelper, String tableName) {
        def foreignKeyDefinitions = dbHelper.getForeignKeyReferences(tableName)
        foreignKeyDefinitions.each { foreignKeyDefinition ->
            dbHelper.dropForeignKey(foreignKeyDefinition.tableName, foreignKeyDefinition.foreignKeyName)
        }
    }

    private void recreateIndexesAsIn6_4_2(DatabaseHelper dbHelper) {
        dbHelper.addOrReplaceIndex("QRTZ_JOB_DETAILS", "IDX_QRTZ_J_REQ_RECOVERY", "SCHED_NAME", "REQUESTS_RECOVERY")
        dbHelper.addOrReplaceIndex("QRTZ_JOB_DETAILS", "IDX_QRTZ_J_GRP", "SCHED_NAME", "JOB_GROUP")

        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_J", "SCHED_NAME", "JOB_NAME", "JOB_GROUP")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_JG", "SCHED_NAME", "JOB_GROUP")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_C", "SCHED_NAME", "CALENDAR_NAME")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_G", "SCHED_NAME", "TRIGGER_GROUP")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_STATE", "SCHED_NAME", "TRIGGER_STATE")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_N_STATE", "SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP", "TRIGGER_STATE")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_N_G_STATE", "SCHED_NAME", "TRIGGER_GROUP", "TRIGGER_STATE")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_NEXT_FIRE_TIME", "SCHED_NAME", "NEXT_FIRE_TIME")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_NFT_ST", "SCHED_NAME", "TRIGGER_STATE", "NEXT_FIRE_TIME")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_NFT_MISFIRE", "SCHED_NAME", "MISFIRE_INSTR", "NEXT_FIRE_TIME")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_NFT_ST_MISFIRE", "SCHED_NAME", "MISFIRE_INSTR", "NEXT_FIRE_TIME", "TRIGGER_STATE")
        dbHelper.addOrReplaceIndex("QRTZ_TRIGGERS", "IDX_QRTZ_T_NFT_ST_MISFIRE_GRP", "SCHED_NAME", "MISFIRE_INSTR", "NEXT_FIRE_TIME", "TRIGGER_GROUP", "TRIGGER_STATE")

        dbHelper.addOrReplaceIndex("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_TRIG_INST_NAME", "SCHED_NAME", "INSTANCE_NAME")
        dbHelper.addOrReplaceIndex("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_INST_JOB_REQ_RCVRY", "SCHED_NAME", "INSTANCE_NAME", "REQUESTS_RECOVERY")
        dbHelper.addOrReplaceIndex("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_J_G", "SCHED_NAME", "JOB_NAME", "JOB_GROUP")
        dbHelper.addOrReplaceIndex("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_JG", "SCHED_NAME", "JOB_GROUP")
        dbHelper.addOrReplaceIndex("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_T_G", "SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP")
        dbHelper.addOrReplaceIndex("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_TG", "SCHED_NAME", "TRIGGER_GROUP")
    }

    private void removeFalsy7_0_0_indexes(DatabaseHelper dbHelper) {
        dbHelper.dropIndexIfExists("QRTZ_TRIGGERS", "IDX_QRTZ_T_NF_TIME")
        dbHelper.dropIndexIfExists("QRTZ_TRIGGERS", "IDX_QRTZ_T_NF_ST")

        dbHelper.dropIndexIfExists("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_TRIG_NAME")
        dbHelper.dropIndexIfExists("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_TRIG_GROUP")
        dbHelper.dropIndexIfExists("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_TRIG_N_G")
        dbHelper.dropIndexIfExists("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_JOB_NAME")
        dbHelper.dropIndexIfExists("QRTZ_FIRED_TRIGGERS", "IDX_QRTZ_FT_JOB_GROUP")

        dbHelper.dropIndexIfExists("QRTZ_TRIGGERS", "IDX_QRTZ_T_NF_TIME_MISFIRE")
        dbHelper.dropIndexIfExists("QRTZ_TRIGGERS", "IDX_QRTZ_T_NF_ST_MISFIRE")
        dbHelper.dropIndexIfExists("QRTZ_TRIGGERS", "IDX_QRTZ_T_NF_ST_MISFIRE_GRP")


    }

    @Override
    String getDescription() {
        return "add indexes on Quartz tables"
    }
}
