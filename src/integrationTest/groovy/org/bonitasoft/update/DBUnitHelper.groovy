/**
 * Copyright (C) 2014-2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.update

import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.database.schema.IndexDefinition

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

    def falseValue() {
        falseValueMap.get(context.dbVendor)
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

    List<IndexDefinition> getIndexesWithTenantIdAsColumn() {
        def query
        def dbVendor = System.getProperty("db.vendor")
        switch (dbVendor) {
            case "postgres":
                query = "SELECT tablename, indexname FROM pg_indexes WHERE LOWER(indexdef) LIKE LOWER('CREATE %INDEX idx%(%tenantid%)%')"
                break

            case "oracle":
                query = """
SELECT i.table_name, i.index_name
  FROM all_indexes i
  JOIN all_ind_columns ic ON i.index_name = ic.index_name
 WHERE LOWER(ic.column_name) = 'tenantid'
   AND LOWER(i.index_name) LIKE 'idx%'
ORDER BY i.table_name ASC, index_name ASC
"""
                break

            case "mysql":
                query = """
SELECT table_name, index_name
FROM   INFORMATION_SCHEMA.STATISTICS
WHERE  TABLE_SCHEMA = DATABASE()
  AND  LOWER(COLUMN_NAME) IN ('tenantid')
  AND  LOWER(index_name) LIKE 'idx%' -- to exclude all non-Bonita indexes (like foreign keys that MySQL creates automatically...)
ORDER BY table_name ASC, index_name ASC
  """
                break

            case "sqlserver":
                query = """
SELECT t.name, i.name
  FROM sys.indexes i
  JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
  JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
  JOIN sys.tables t ON t.object_id = i.object_id
 WHERE LOWER(c.name) = 'tenantid' and LOWER(i.name) LIKE 'idx%'
 ORDER BY t.name, i.name
"""
                break
            default:
                throw new IllegalStateException("db vendor invalid: $dbVendor")
        }

        List<IndexDefinition> indexesWithTenantId = []
        context.sql.eachRow(query) {
            indexesWithTenantId.add(new IndexDefinition(it[0], it[1]))
        }
        return indexesWithTenantId
    }
}
