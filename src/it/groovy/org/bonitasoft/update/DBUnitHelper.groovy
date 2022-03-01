/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.update

import groovy.xml.StreamingMarkupBuilder
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.database.schema.IndexDefinition
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.ext.oracle.OracleConnection

import java.sql.DriverManager
import java.sql.SQLException

/**
 * @author Baptiste Mesta
 */
class DBUnitHelper {

    private static final INSTANCE = new DBUnitHelper(new UpdateContext(logger: new Logger()))

    static DBUnitHelper getInstance() {
        return INSTANCE
    }

    UpdateContext context
    private Logger logger

    DBUnitHelper(UpdateContext context) {
        context.start()
        context.loadConfiguration()
        context.openSqlConnection()
        this.context = context
        this.logger = context.logger
    }

    static Map trueValueMap = [
            (UpdateStep.DBVendor.ORACLE)   : 1,
            (UpdateStep.DBVendor.POSTGRES) : true,
            (UpdateStep.DBVendor.MYSQL)    : true,
            (UpdateStep.DBVendor.SQLSERVER): true
    ]
    static Map falseValueMap = [
            (UpdateStep.DBVendor.ORACLE)   : 0,
            (UpdateStep.DBVendor.POSTGRES) : false,
            (UpdateStep.DBVendor.MYSQL)    : false,
            (UpdateStep.DBVendor.SQLSERVER): false
    ]

    def trueValue() {
        trueValueMap.get(context.dbVendor)
    }

    def falseValue() {
        falseValueMap.get(context.dbVendor)

    }

    def static dataSet(data) {
        new ReplacementDataSet(new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind {
            dataset data
        }.toString())), ["[NULL]": null], null)
    }


    def executeScript(URL resource) {
        resource.text.split("@@|GO|;").each({ String stmt ->
            stmt = stmt.trim()
            if (!stmt.isEmpty()) {
                context.sql.execute(stmt)
            }
        })
    }

    String[] createTables(String folder, String feature) {
        logger.info("Create tables from sql file in $folder with suffix $feature")
        executeScript(INSTANCE.class.getClassLoader().getResource("sql/v${folder}/${context.dbVendor.name().toLowerCase()}-${feature}.sql"))
    }

    String[] createTables(String folder) {
        logger.info("Create tables from sql file in $folder")
        executeScript(INSTANCE.class.getClassLoader().getResource("sql/v${folder}/${context.dbVendor.name().toLowerCase()}.sql"))
    }

    boolean hasIndexOnTable(String tableName, String indexName) {
        context.databaseHelper.hasIndexOnTable(tableName, indexName)
    }

    boolean hasColumnOnTable(String tableName, String columnName) {
        context.databaseHelper.hasColumnOnTable(tableName, columnName)
    }

    boolean hasForeignKeyOnTable(String tableName, String foreignKey) {
        context.databaseHelper.hasForeignKeyOnTable(tableName, foreignKey)
    }

    boolean hasPrimaryKeyOnTable(String tableName, String pkName) {
        context.databaseHelper.hasPrimaryKeyOnTable(tableName, pkName)
    }

    boolean hasUniqueKeyOnTable(String tableName, String ukName) {
        context.databaseHelper.hasUniqueKeyOnTable(tableName, ukName)
    }

    String getPrimaryKey(String tableName) {
        context.databaseHelper.getPrimaryKey(tableName)
    }

    IndexDefinition getIndexDefinition(String tableName, String indexName) {
        context.databaseHelper.getIndexDefinition(tableName, indexName)
    }


    def hasSequenceForTenant(def tenantId, def sequenceId) {
        def value = context.databaseHelper.getSequenceValue(tenantId, sequenceId)
        value && value.nextid > 0
    }

    def getBlobContentAsString(Object blobValue) {
        context.databaseHelper.getBlobContentAsString(blobValue)
    }

    JdbcDatabaseTester createTester() {
        new JdbcDatabaseTester(context.dbDriverClassName, context.dburl, context.dbUser, context.dbPassword) {
            IDatabaseConnection getConnection() {
                if (context.dbVendor == UpdateStep.DBVendor.ORACLE) {
                    def conn = DriverManager.getConnection(context.dburl, context.dbUser, context.dbPassword)
                    return new OracleConnection(conn, context.dbUser)
                } else {
                    return super.getConnection()
                }
            }
        }
    }

    def dropTables(String[] tables) {
        tables.each {
            //add .toString to avoid the error bellow. Is there a better way to do that?
            //Failed to execute: DROP TABLE ? because: ERROR: syntax error at or near "$1"
            if (context.databaseHelper.hasTable(it)) {
                def statement = "DROP TABLE $it".toString()
                logger.info("DROP TABLE [$it]".toString())
                try {
                    context.sql.execute(statement)
                } catch (SQLException e) {
                    logger.error(String.format("error while executing %s", statement))
                    throw e
                }
            } else {
                logger.info("table [$it] does not exists")
            }
        }
    }

    def countPropertyInConfigFile(String configFile, String expectedKey, String expectedValue) {
        def scannedFiles = 0
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${configFile}       
                ORDER BY content_type, tenant_id 
                """) {
            scannedFiles++
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            def properties = new Properties()
            String content = getBlobContentAsString(it.resource_content)
            context.logger.debug(String.format("check that property file | tenant id: %3d | type: %-25s | file name: %s | contains %s = %s",
                    tenantId, contentType, configFile, expectedKey, expectedValue))
            StringReader reader = new StringReader(content)
            properties.load(reader)
            assert (properties.containsKey(expectedKey))
            assert (properties.get(expectedKey) == expectedValue)

        }
        scannedFiles

    }

    def countConfigFileWithContent(String configFile, String expectedContent) {
        def scannedFiles = 0
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${configFile}       
                ORDER BY content_type, tenant_id 
                """) {
            scannedFiles++
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            String content = getBlobContentAsString(it.resource_content)
            context.logger.debug(String.format("check configuration file content | tenant id: %3d | type: %-25s | file name: %s ",
                    tenantId, contentType, configFile))
            assert (content == expectedContent)
        }
        scannedFiles
    }

    def countConfigFileWithContent(String contentType, String configFile, String expectedContent) {
        def scannedFiles = 0
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${configFile}   
                AND content_type=${contentType}    
                ORDER BY tenant_id 
                """) {
            def tenantId = it.tenant_id as long
            String content = getBlobContentAsString(it.resource_content)
            context.logger.debug(String.format("check configuration file content | tenant id: %3d | type: %-25s | file name: %s ",
                    tenantId, contentType, configFile))
            if (content == expectedContent) {
                scannedFiles++
            }
        }
        scannedFiles
    }
    
    def countConfigFileWithName(String contentType, String configFile) {
        def scannedFiles = 0
        context.sql.eachRow("""
                SELECT tenant_id, content_type
                FROM configuration
                WHERE resource_name=${configFile}   
                AND content_type=${contentType}    
                ORDER BY tenant_id 
                """) {
            scannedFiles++
        }
        scannedFiles
    }

    def countConfigFileWithNameOfAnyType(String configFileName) {
        return context.sql.firstRow("SELECT COUNT(1) FROM configuration WHERE resource_name=${configFileName}")[0]
    }

    String getConfigFileContent(long tenantId, String contentType, String configFile) {
        String content = getBlobContentAsString(context.sql.firstRow("""
                    SELECT resource_content
                    FROM configuration
                    WHERE tenant_id = ${tenantId}
                    AND content_type = ${contentType}
                    AND resource_name = ${configFile}
                    """)["resource_content"])

        context.logger.debug(String.format("Got configuration file content | tenant id: %3d | type: %-25s | file name: %s ",
                tenantId, contentType, configFile))
        content
    }

}
