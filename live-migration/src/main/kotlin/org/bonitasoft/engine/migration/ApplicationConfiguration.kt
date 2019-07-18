package org.bonitasoft.engine.migration

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.sql.Connection
import java.sql.Connection.TRANSACTION_READ_COMMITTED
import javax.sql.DataSource


@Configuration
@ComponentScan
@EnableScheduling
open class ApplicationConfiguration {

    val logger = LoggerFactory.getLogger(Application::class.java)
    @Bean
    open fun someBean(datasource: DataSource): Database {
        val connect = Database.connect(datasource, setupConnection = {connection -> connection.transactionIsolation = TRANSACTION_READ_COMMITTED })
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED
        logger.info("Using datasource with $datasource")
        return connect
    }




}