/**
 * Copyright (C) 2018 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.migration.version.to7_7_1

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.MYSQL

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Danila Mazour
 */
class ChangeContractInputSerializationOnMysqlToCorrectFormatIfNotDone extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.logger.info("The database is of type $context.dbVendor")
        switch (context.dbVendor) {
            case MYSQL:
                context.logger.info('Checking if contract data tables must be updated')
                updateValColumnTypeIfNeeded(context, 'contract_data')
                updateValColumnTypeIfNeeded(context, 'arch_contract_data')
                break
            default:
                context.logger.info("Not a MYSQL database, skipping step.")
                break
        }
    }

    private static final String valColumnTargetType = "LONGTEXT"

    private void updateValColumnTypeIfNeeded(MigrationContext context, String table_name) {
        def dataTypes = context.sql.firstRow("SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '$table_name' AND COLUMN_NAME = 'val';" as String)
        if (!((String)dataTypes.get('DATA_TYPE')).equalsIgnoreCase('LONGTEXT')) {
            context.logger.info("The 'val' column type on table '$table_name' is not '$valColumnTargetType' (found $dataTypes) so updating the column")
            updateValColumnTypeOnTable(context, table_name)
        } else {
            context.logger.info("The migration has already been performed on table '$table_name', skipping it.")
        }
    }

    // Visible for testing
    void updateValColumnTypeOnTable(MigrationContext context, String table_name) {
        def databaseHelper = context.databaseHelper
        def sql = context.sql
        //create temporary column
        databaseHelper.addColumn(table_name, "tmp_val", valColumnTargetType, null, null)
        //move data
        sql.eachRow("SELECT * FROM $table_name" as String) { contract_data ->
            sql.executeUpdate("UPDATE $table_name SET tmp_val = ? WHERE tenantid = ? AND id = ?" as String,
                    contract_data.val,
                    contract_data.tenantid,
                    contract_data.id,
            )
        }

        // drop original column
        databaseHelper.dropColumn(table_name, "val")
        //rename column
        databaseHelper.renameColumn(table_name, "tmp_val", "val", valColumnTargetType)
    }

    @Override
    String getDescription() {
        return "Change the column type of the contract data value on MySql, if it has not been done already."
    }

}
