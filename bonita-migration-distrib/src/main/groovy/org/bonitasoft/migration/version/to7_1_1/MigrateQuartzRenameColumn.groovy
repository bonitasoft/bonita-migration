package org.bonitasoft.migration.version.to7_1_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class MigrateQuartzRenameColumn extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        if (context.dbVendor.equals(DBVendor.POSTGRES)) {
            def tableName = "qrtz_simprop_triggers"
            def like = "int8_%"
            def query = """
            SELECT
            count(*) as nb_columns
            FROM
            information_schema.columns b
            WHERE
            LOWER(b.table_name) = '$tableName'
            AND LOWER(b.column_name) LIKE '$like'
            """

            def result = context.databaseHelper.selectFirstRow(query)

            if (result.get("nb_columns") != "0") {
                context.databaseHelper.renameColumn("qrtz_simprop_triggers", "int8_prop_1", "long_prop_1", "int8")
                context.databaseHelper.renameColumn("qrtz_simprop_triggers", "int8_prop_2", "long_prop_2", "int8")
            }
        }
    }


    @Override
    String getDescription() {
        return "rename columns in qrtz_simprop_triggers (postgres only)"
    }

}
