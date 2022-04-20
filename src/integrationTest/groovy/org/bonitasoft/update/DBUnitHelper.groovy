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

import java.sql.SQLException

import groovy.xml.StreamingMarkupBuilder
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSet

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

    def countConfigFileWithNameOfAnyType(String configFileName) {
        return context.sql.firstRow("SELECT COUNT(1) FROM configuration WHERE resource_name=${configFileName}")[0]
    }

}
