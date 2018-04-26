/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_7_0

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class ChangeContractInputSerialization extends MigrationStep {


    @Override
    def execute(MigrationContext context) {
        String type
        switch (context.dbVendor) {
            case ORACLE:
                type = "CLOB"
                break
            case POSTGRES:
                type = "TEXT"
                break
            case MYSQL:
                type = "MEDIUMTEXT"
                break
            case SQLSERVER:
                type = "NVARCHAR(MAX)"
                break
        }
        updateSerializationOnTable(context, "contract_data", type)
        updateSerializationOnTable(context, "arch_contract_data", type)
    }

    private void updateSerializationOnTable(MigrationContext context, String table_name, String newType) {
        def databaseHelper = context.databaseHelper
        def sql = context.sql

        //create temporary column
        databaseHelper.addColumn(table_name, "tmp_val", newType, null, null)
        //move data
        sql.eachRow("SELECT * FROM $table_name" as String) { contract_data ->
            sql.executeUpdate("UPDATE $table_name SET tmp_val = ? WHERE tenantid = ? AND id = ?",
                    deserialize(databaseHelper.getBlobContentAsBytes(contract_data.val)),
                    contract_data.tenantid,
                    contract_data.id,
            )
        }
        // drop original column
        databaseHelper.dropColumn(table_name, "val")
        //rename column
        databaseHelper.renameColumn(table_name, "tmp_val", "val", newType)
    }

     String deserialize(byte[] content) {
        return new ObjectInputStream(new ByteArrayInputStream(content)).readObject() as String
    }

    @Override
    String getDescription() {
        return "Change how contract data are serialized"
    }
}
