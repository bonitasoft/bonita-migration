package org.bonitasoft.migration.version.to7_7_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

/**
 * @author Danila Mazour
 */
class ChangeContractInputSerializationOnMysqlToCorrectFormatIfNotDone extends MigrationStep {

    @Override
    def execute(MigrationContext context) {

        switch (context.dbVendor) {
            case MYSQL:
                def table_type = context.sql.firstRow("SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'contract_data' AND COLUMN_NAME = 'val';")
                if (!table_type.containsValue("LONGTEXT")) {
                    updateValColumnTypeOnTable(context, "contract_data")
                    updateValColumnTypeOnTable(context, "arch_contract_data")
                } else {
                    context.logger.info("The migration step has already been performed, skipping.")
                }
                break
            default:
                context.logger.info("The database is of type $context.dbVendor, skipping step.")
                break
        }
    }

    private static void updateValColumnTypeOnTable(MigrationContext context, String table_name) {

        String  type = "LONGTEXT"
        def databaseHelper = context.databaseHelper
        def sql = context.sql
        //create temporary column
        databaseHelper.addColumn(table_name, "tmp_val", type, null, null)
        //move data
        sql.eachRow("SELECT * FROM $table_name" as String) { contract_data ->
            sql.executeUpdate("UPDATE $table_name SET tmp_val = ? WHERE tenantid = ? AND id = ?",
                    contract_data.val,
                    contract_data.tenantid,
                    contract_data.id,
            )
        }

        // drop original column
        databaseHelper.dropColumn(table_name, "val")
        //rename column
        databaseHelper.renameColumn(table_name, "tmp_val", "val", type)
    }

    @Override
    String getDescription() {
        return "Change the column type of the contract data value on MySql, if it has not been done already."
    }

}
