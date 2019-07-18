package org.bonitasoft.engine.migration

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.bonitasoft.engine.migration.tables.ArchContractDataPre7_0Table
import org.bonitasoft.engine.migration.tables.ArchContractDataTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.BeforeTest

@SpringBootTest
class UpdateArchContractDataTest {
    val logger = LoggerFactory.getLogger(UpdateArchContractDataTest::class.java)

    @Autowired
    lateinit var updateArchContractData: UpdateArchContractData
    @Value("\${spring.datasource.driver-class-name:h2}")
    lateinit var driverClassName: String
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate


    val archContractDataRepository = ArchContractDataRepository()

    @BeforeTest
    fun before() {
        transaction {
            logger.info("Drop tables")
            SchemaUtils.drop(ArchContractDataPre7_0Table, ArchContractDataTable)
        }
        transaction {
            when {
                driverClassName.contains("postgresql") -> {
                    createTablesFromScript("/postgres.sql")
                }
                driverClassName.contains("oracle") -> {
                    createTablesFromScript("/oracle.sql")
                }
                driverClassName.contains("mysql") -> {
                    createTablesFromScript("/mysql.sql")
                }
                driverClassName.contains("sqlserver") -> {
                    createTablesFromScript("/sqlserver.sql")
                }
                else -> {
                    logger.info("create tables using DSL (h2)")
                    SchemaUtils.create(ArchContractDataPre7_0Table, ArchContractDataTable)
                }
            }

        }
    }

    private fun createTablesFromScript(fileName: String) {
        logger.info("create tables using script $fileName")
        val content = UpdateArchContractData::class.java.getResource(fileName).readText()
        content.split(";").filter { !it.isBlank() }.forEach {
            jdbcTemplate.execute(it.trim())
        }
    }


    @Test
    fun `it should migrate arch contract data from the backup`() {
        transaction {
            archContractDataRepository.insert_arch_contract_data_backup_before_migration()
            archContractDataRepository.insert_arch_contract_data_before_migration()

        }

        updateArchContractData.execute()


        val dataAfterMigration = transaction {
            ArchContractDataTable.selectAll().map(toList)
        }
        Assertions.assertThat(dataAfterMigration.map { it[1] })
                .containsExactlyInAnyOrder(*archContractDataRepository.getValuesOf_arch_contract_data_after_migration().map { it[1] }.toTypedArray())
    }


    @Test
    fun `it should just log when table is already migrated`() {
        transaction {
            SchemaUtils.drop(ArchContractDataPre7_0Table)
        }
        TestLoggerAppender.clear()

        updateArchContractData.execute()


        assertThat(TestLoggerAppender.allLogs()).anyMatch { it.message.contains("Table arch_contract_data_backup does not exists. Everything should be migrated already") }
    }

    val toList = fun(it: ResultRow): List<String?> {
        return listOf(
                it[ArchContractDataTable.tenantId].toString(),
                it[ArchContractDataTable.id].toString(),
                it[ArchContractDataTable.kind],
                it[ArchContractDataTable.scopeId].toString(),
                it[ArchContractDataTable.name],
                it[ArchContractDataTable.val_],
                it[ArchContractDataTable.archiveDate].toString(),
                it[ArchContractDataTable.sourceObjectId].toString()
        )
    }


}