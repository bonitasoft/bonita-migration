package org.bonitasoft.engine.migration

import org.bonitasoft.engine.migration.tables.ArchContractDataPre7_0Table
import org.bonitasoft.engine.migration.tables.ArchContractDataTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.io.ObjectInputStream
import java.lang.Exception
import java.lang.RuntimeException
import java.sql.Blob
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Duration
import java.util.stream.Collectors
import javax.annotation.PreDestroy

@Service
class UpdateArchContractData(
        @Value("\${org.bonitasoft.engine.migration.batch_size:100}") val batchSize: Int,
        @Value("\${org.bonitasoft.engine.migration.parallelism:1}") val parallelism: Int,
        @Value("\${org.bonitasoft.engine.migration.skip_confirmation:true}") val skipConfirmation: Boolean,
        @Value("\${org.bonitasoft.engine.migration.delete_after_migration:true}") val deleteAfterMigration: Boolean,
        val logMonitor: LogMonitor, @Value("\${spring.datasource.url:#{null}}") val databaseUrl: String?,
        val jdbcTemplate: JdbcTemplate) {


    val parallelScheduler: Scheduler = Schedulers.newParallel("migration-thead", parallelism)

    val logger = LoggerFactory.getLogger(UpdateArchContractData::class.java)
    var running = true

    @PreDestroy
    fun stopMigration() {
        running = false
        parallelScheduler.dispose()
    }


    fun execute() {
        logger.info("Will migrate rows of arch_contract_data_backup back into arch_contract_data table")
        logger.info("Migration can be stopped at any time and will automatically resume when restarted")
        logger.info("Database is $databaseUrl")
        logger.info("Batch size $batchSize")
        logger.info("Parallelism is $parallelism")
        logger.info("Delete migrated row on the fly is $deleteAfterMigration")
        if (!skipConfirmation) {
            println("Start the migration using the above parameters? [y/N]")
            val readLine = readLine()
            if (!"Y".equals(readLine?.toUpperCase())) {
                println("Migration cancelled")
                System.exit(1)
            }
        }

        logger.info("All settings can be changed in application.properties file")
        logger.info("Starting migration....")

        val archContractDataBackupTableExists = transaction {
            ArchContractDataPre7_0Table.exists()
        }

        val archContractDataTableExists = transaction {
            ArchContractDataTable.exists()
        }

        if (!archContractDataBackupTableExists) {
            logger.info("Table arch_contract_data_backup does not exists. Everything should be migrated already.")
            return
        }

        if (!archContractDataTableExists) {
            logger.error("Table arch_contract_data does not exists. Table should be present on a normal Bonita installation.")
            return
        }


        val throughput = Throughput(Duration.ofSeconds(10), total =
        transaction {
            val totalElements = ArchContractDataPre7_0Table.selectAll().count()
            logger.info("${totalElements} rows will be migrated")
            totalElements
        })

        val shutdownHook = Thread({
            logger.info("Migration cancelled at ${throughput.getPercentageCompleted()}%:" +
                    " Migrated ${throughput.getCount()} elements on a total of ${throughput.getTotal()} in ${throughput.currentDuration}.")
        }, "Cleanup-thread")
        Runtime.getRuntime().addShutdownHook(shutdownHook)
        logMonitor.registerCounter(throughput)
        migrate(throughput)

        Runtime.getRuntime().removeShutdownHook(shutdownHook)

    }

    private fun migrate(throughput: Throughput) {
        transaction {
            val createIndex = SchemaUtils.createIndex(
                    Index(
                            listOf(ArchContractDataPre7_0Table.tenantId, ArchContractDataPre7_0Table.id),
                            false,
                            "idx_arch_contract_data_backup"
                    )
            )
            logger.info("Creating index $createIndex")
            try {
                createIndex.forEach {
                    jdbcTemplate.execute(it)
                }
                logger.info("Index created")
            } catch (e: Exception) {
                logger.info("Index already created")
            }
        }
        var lastTenantId = 0L
        var lastId = 0L
        do {
            val ids: List<Pair<Long, Long>>
            try {

                ids = transaction {
                    ArchContractDataPre7_0Table.slice(ArchContractDataPre7_0Table.tenantId, ArchContractDataPre7_0Table.id)
                            .select {
                                ArchContractDataPre7_0Table.tenantId greater lastTenantId
                                ArchContractDataPre7_0Table.id greater lastId

                            }
                            .orderBy(ArchContractDataPre7_0Table.tenantId)
                            .orderBy(ArchContractDataPre7_0Table.id)
                            .limit(batchSize).map {
                                it[ArchContractDataPre7_0Table.tenantId] to it[ArchContractDataPre7_0Table.id]
                            }
                }
            } catch (t: Throwable) {
                logOnError(t)
                return
            }
            lastTenantId = ids.last().first
            lastId = ids.last().second

            val count = Flux.fromIterable(ids)
                    .parallel(parallelism)
                    .runOn(parallelScheduler)
                    .map { element ->
                        transaction {
                            ArchContractDataPre7_0Table.select {
                                ArchContractDataPre7_0Table.tenantId eq element.first and
                                        (ArchContractDataPre7_0Table.id eq element.second)
                            }.forEach { old ->
                                try {
                                    ArchContractDataTable.insert { new ->
                                        new[tenantId] = old[ArchContractDataPre7_0Table.tenantId]
                                        new[id] = old[ArchContractDataPre7_0Table.id]
                                        new[kind] = old[ArchContractDataPre7_0Table.kind]
                                        new[scopeId] = old[ArchContractDataPre7_0Table.scopeId]
                                        new[name] = old[ArchContractDataPre7_0Table.name]
                                        new[val_] = unserialize(old[ArchContractDataPre7_0Table.val_])
                                        new[archiveDate] = old[ArchContractDataPre7_0Table.archiveDate]
                                        new[sourceObjectId] = old[ArchContractDataPre7_0Table.sourceObjectId]
                                    }
                                } catch (e: ExposedSQLException) {
                                    if (isConstraintViolationException(e)) {
                                        logger.debug("Row already migrated ${old[ArchContractDataPre7_0Table.tenantId]} ${old[ArchContractDataPre7_0Table.id]}")
                                    } else {
                                        throw   e
                                    }
                                }

                            }
                        }
                        return@map element
                    }

                    .sequential()
                    .publishOn(parallelScheduler)
                    .groupBy { it.first }
                    .map { groupByTenantId ->
                        groupByTenantId.collect(Collectors.toList()).map {
                            it.map { it.second }
                        }.map { ids ->
                            transaction {
                                if (deleteAfterMigration) {
                                    ArchContractDataPre7_0Table.deleteWhere {
                                        ArchContractDataPre7_0Table.tenantId eq groupByTenantId.key()
                                        ArchContractDataPre7_0Table.id inList ids
                                    }
                                }
                            }
                            return@map ids.size
                        }
                    }.flatMap { it }
                    .collect(Collectors.summingInt { it })


            var migratedCount: Int?
            try {
                migratedCount = count
                        .block()?.toInt()
                if (migratedCount != null)
                    throughput.increment(migratedCount)
            } catch (t: Throwable) {
                logOnError(t)
                return
            }

        } while (migratedCount == batchSize && running)
        if (running) {
            logger.info("Migrated ${throughput.getCount()} rows in ${throughput.currentDuration}")
            transaction {
                SchemaUtils.drop(ArchContractDataPre7_0Table)
            }
            logger.info("Migration completed")
        } else {
            logger.info("Migration cancelled")
        }

    }

    private fun isConstraintViolationException(e: ExposedSQLException): Boolean {
        return when (e.cause) {
            is SQLIntegrityConstraintViolationException -> true
            is SQLException -> e.sqlState?.substring(0, 2) == "23"
            else -> false
        }
    }

    private fun logOnError(t: Throwable) {
        if (!running) {
            logger.info("Migration cancelled")
        } else {
            logger.error("Error happened during the migration ${t.message}. Please relaunch the migration tool to finish this migration")
        }
    }

    private fun unserialize(value: Blob?): String? {
        if (value != null) {
            return ObjectInputStream(value.binaryStream).readObject() as String
        }
        return null
    }
}
