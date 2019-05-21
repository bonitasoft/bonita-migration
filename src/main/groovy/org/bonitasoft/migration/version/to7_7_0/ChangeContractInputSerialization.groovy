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

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.database.DatabaseHelper

import groovy.sql.GroovyRowResult
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope

class ChangeContractInputSerialization extends MigrationStep {

    private MigrationContext context

    private Collection<NotMigratedContractData> pendingNotMigratedContractData = newNotMigratedContractData()

    private static Collection<NotMigratedContractData> newNotMigratedContractData() {
        // use a concurrent implementation as error elements are added by async tasks running in parallel
        new ConcurrentSkipListSet<>()
    }

    @Override
    def execute(MigrationContext context) {
        this.context = context
        String newColumnType = newColumnType(context)
        updateSerializationOnContractDataTableIfRequested(newColumnType)
        updateSerializationOnTable("arch_contract_data", newColumnType)
    }

    private void updateSerializationOnContractDataTableIfRequested(String newType) {
        if (Boolean.getBoolean('bonita.migration.step.skip.770.serialization.contract_data')) {
            context.logger.info("Skipping serialization update of the 'contract_data' table as per configuration")
            return
        }
        updateSerializationOnTable("contract_data", newType)
    }

    private void updateSerializationOnTable(String tableName, String newType) {
        def databaseHelper = context.databaseHelper

        // This is already the right column type. Migration has already run
        if (databaseHelper.getColumnType(tableName, "val").toUpperCase() == normalizeType(newType, context.dbVendor)) {
            context.logger.info("The $tableName table is already fully migrated")
            return
        }

        //create temporary column only if it does not exists (to make the step re-entrant)
        if (!databaseHelper.hasColumnOnTable(tableName, "tmp_val")) {
            context.logger.info("Migrating $tableName ...")
            databaseHelper.addColumn(tableName, "tmp_val", newType, null, null)
        } else {
            context.logger.info("The migration of the $tableName table was not completed, continuing...")
        }

        SerializationUpdateCounter counter = new SerializationUpdateCounter()
        context.logger.info("Getting rows to process for the $tableName table")
        List<GroovyRowResult> rows = context.sql.rows("SELECT tenantid, id FROM $tableName" as String)
        counter.totalToProcess = rows.size()
        context.logger.info("There are ${counter.totalToProcess} rows to process")

        if(!rows.isEmpty()) {
            rows.collect { contractData ->
                updateAsync(counter, contractData.tenantid, contractData.id, tableName)
            }.last().get() // we wait for the last future to finish before managing potential errors
            TimeUnit.MILLISECONDS.sleep(100) // give some time for the latest Future to complete

            while (!pendingNotMigratedContractData.isEmpty()) {
                context.logger.info("${pendingNotMigratedContractData.size()} transient errors occured, retrying the migration for the related rows")

                Collection<NotMigratedContractData> notMigratedContractData = pendingNotMigratedContractData
                pendingNotMigratedContractData = newNotMigratedContractData()
                notMigratedContractData.collect { contractData ->
                    updateAsync(counter, contractData.tenantId, contractData.id, tableName)
                }.last().get() // we wait for the last future to finish before managing potential errors
                TimeUnit.MILLISECONDS.sleep(100) // give some time for the latest Future to complete
            }
        }

        context.logger.info("Processing completed for the $tableName table")
        logCurrentStatus(counter)

        context.logger.info('Finalizing val column migration')
        context.logger.info('Dropping the original val column')
        databaseHelper.dropColumn(tableName, "val")
        context.logger.info('Renaming the tmp_val column into val')
        databaseHelper.renameColumn(tableName, "tmp_val", "val", newType)
        context.logger.info("val column migration done for the $tableName table")
    }

    private static String normalizeType(String type, DBVendor dbVendor) {
        switch (dbVendor) {
            case SQLSERVER:
                return "NVARCHAR"
            default:
                return type
        }
    }

    // visible for testing
    @PackageScope
    static String newColumnType(MigrationContext context) {
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
        type
    }

    private Future<Void> updateAsync(SerializationUpdateCounter counter, tenantId, id, String tableName) {
        context.asyncExec((Callable<Void>) {
            try {
                counter.processAttempts.incrementAndGet()
                def fullContractData = context.sql.firstRow("SELECT val FROM $tableName WHERE tenantid=:tenantid AND id=:id AND tmp_val IS NULL" as String, [tenantid: tenantId, id: id])
                // already migrated
                if (fullContractData == null) {
                    counter.processed.incrementAndGet()
                    counter.previouslyMigrated.incrementAndGet()
                    logCurrentStatusIfNecessary(counter)
                    return
                }
                // null value, no migration required
                if (fullContractData.val == null) {
                    counter.processed.incrementAndGet()
                    counter.nullValues.incrementAndGet()
                    logCurrentStatusIfNecessary(counter)
                    return
                }
                def newValue = deserialize(context.databaseHelper, fullContractData.val)
                context.sql.executeUpdate("UPDATE $tableName SET tmp_val = ? WHERE tenantid = ? AND id = ?",
                        newValue,
                        tenantId,
                        id
                )
                counter.processed.incrementAndGet()
                counter.migrated.incrementAndGet()
                logCurrentStatusIfNecessary(counter)
            } catch (Throwable t) {
                counter.transientErrors.incrementAndGet()
                context.logger.info("Unable to migrate tenantid: $tenantId id: $id. This will be retried later. Cause: ${t.message}")
                context.logger.debug('Failure details', t)
                pendingNotMigratedContractData.add(new NotMigratedContractData(tenantId: tenantId, id: id))
            }
        })
    }

    private void logCurrentStatusIfNecessary(SerializationUpdateCounter counter) {
        if (counter.processed.get() % 500 == 0) {
            logCurrentStatus(counter)
        }
    }

    private logCurrentStatus(SerializationUpdateCounter counter) {
        context.logger.info("Processing status:  ${counter.processed} processed / ${counter.migrated} migrated"
                + " / ${counter.previouslyMigrated} previously migrated / ${counter.nullValues} null values (no migration)"
                + " / ${counter.transientErrors} transient errors"
                + " / ${counter.processAttempts} process attempts"
                + " / ${counter.totalToProcess} total to process")
    }

    private static String deserialize(DatabaseHelper databaseHelper, Object contractDataVal) {
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


    @EqualsAndHashCode
    private static class NotMigratedContractData {
        long tenantId
        long id
    }

    private static class SerializationUpdateCounter {

        long totalToProcess
        def migrated = new AtomicLong()
        def nullValues = new AtomicLong()
        def processed = new AtomicLong()
        def processAttempts = new AtomicLong()
        def transientErrors = new AtomicLong()
        def previouslyMigrated = new AtomicLong()

    }

}
