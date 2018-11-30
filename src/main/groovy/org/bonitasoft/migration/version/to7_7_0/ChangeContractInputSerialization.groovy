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

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.database.DatabaseHelper

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

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
                type = "LONGTEXT"
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

        // This is already the right column type. Migration has already run:
        if (databaseHelper.getColumnType(table_name, "val").toUpperCase() == normalizeType(newType, context.dbVendor)) {
            //table already migrated
            context.logger.info("The table $table_name is already fully migrated")
            return
        }

        def counter = new AtomicLong()
        if (!databaseHelper.hasColumnOnTable(table_name, "tmp_val")) {
            context.logger.info("Migrating $table_name ...")
            //create temporary column only if it does not exists (to make the step re-entrant)
            databaseHelper.addColumn(table_name, "tmp_val", newType, null, null)
        } else {
            context.logger.info("The migration of $table_name was not completed, continuing...")
        }
        //move data

        def rows = getFirst5kToMigrate(sql, table_name)
        while (!rows.isEmpty()) {
            rows.collect { contract_data ->
                updateAsync(counter, contract_data.tenantid, contract_data.id, context, table_name)
            }.last().get() // we wait for the last future to finish before reading the next 5000
            rows = getFirst5kToMigrate(sql, table_name)
        }
        context.logger.info("Migrated in total ${counter.get()} $table_name")
        def numberOfNotMigratedRows = sql.firstRow("SELECT COUNT(*) FROM $table_name WHERE val IS NOT NULL AND tmp_val IS NULL" as String)[0]
        if (numberOfNotMigratedRows > 0) {
            throw new IllegalStateException("$numberOfNotMigratedRows rows from $table_name where not migrated, check your logs and retry the migration")
        }
        // drop original column
        databaseHelper.dropColumn(table_name, "val")
        //rename column
        databaseHelper.renameColumn(table_name, "tmp_val", "val", newType)
    }

    static String normalizeType(String type, DBVendor dbVendor) {
        switch (dbVendor) {
            case SQLSERVER:
                return "NVARCHAR"
            default:
                return type
        }
    }

    private List<GroovyRowResult> getFirst5kToMigrate(Sql sql, String table_name) {
        sql.rows("SELECT tenantid, id FROM $table_name WHERE val IS NOT NULL AND tmp_val IS NULL" as String, 0, 5000)
    }

    private Object getFirstNonNullValue(Sql sql, String table_name) {
        def row = sql.firstRow("SELECT val from $table_name WHERE val IS NOT NULL" as String)
        if (row == null) {
            return null
        }
        def firstNonNullValue = row.get("val")
        firstNonNullValue
    }

    private Future<Void> updateAsync(AtomicLong counter, tenantid, id, MigrationContext context, String table_name) {
        context.asyncExec((Callable<Void>) {
            try {
                def full_contract_data = context.sql.firstRow("SELECT val FROM $table_name WHERE tenantid=:tenantid AND id=:id AND tmp_val IS NULL" as String, [tenantid: tenantid, id: id])
                if (full_contract_data?.val == null) {
                    //already migrated
                    return
                }
                def newValue = deserialize(context.databaseHelper, full_contract_data.val)
                context.sql.executeUpdate("UPDATE $table_name SET tmp_val = ? WHERE tenantid = ? AND id = ?",
                        newValue,
                        tenantid,
                        id
                )
                def currentCounter = counter.incrementAndGet()
                if (currentCounter % 500 == 0) {
                    context.logger.info("Migrated $currentCounter $table_name ...")
                }
            } catch (Throwable t) {
                context.logger.error("error while migrating contract input $tenantid,$id: $t.message")
                t.printStackTrace()
            }
        })

    }

    static String deserialize(DatabaseHelper databaseHelper, Object contractDataVal) {
        if (contractDataVal != null) {
            byte[] content = databaseHelper.getBlobContentAsBytes(contractDataVal)
            return new ObjectInputStream(new ByteArrayInputStream(content)).readObject() as String
        }
        return null
    }

    @Override
    String getDescription() {
        return "Change how contract data are serialized"
    }
}
