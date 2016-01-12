/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration.core.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.bonitasoft.migration.core.MigrationStep.DBVendor
import org.bonitasoft.migration.core.database.schema.ColumnDefinition
import org.bonitasoft.migration.core.database.schema.IndexDefinition

/**
 * @author Baptiste Mesta
 */
class DatabaseHelper {


    Sql sql
    DBVendor dbVendor
    String version

    /**
     * execute a postgres script converted to the database specified by dbVendor
     * @param statement
     * @return
     */
    def boolean execute(GString statement) {
        //TODO: replace statement by file name, get current version from context & split statement against @@ joker
        return sql.execute(adaptFor(statement))
    }

    /**
     * execute statement without adapting syntax to dbVendor dialect
     * @param statement
     * @return
     */
    def boolean executeDbVendorStatement(String statement) {
        return sql.execute(statement)
    }

    def boolean execute(String statement) {
        return sql.execute(adaptFor(statement))
    }

    def boolean execute(String statement, List<Object> params) {
        return sql.execute(adaptFor(statement), params)
    }

    def int executeUpdate(GString statement) {
        return sql.executeUpdate(adaptFor(statement))
    }

    def int executeUpdate(String statement) {
        return sql.executeUpdate(adaptFor(statement))
    }

    /**
     * adapting could have a different result that a fresh install
     * example: VARCHAR(50) should be a VARCHAR2(50 CHAR) in oracle
     * example: column name contains reserved keyword (qrtz_simprop_triggers)
     * @param statement
     * @return
     */
    @Deprecated
    def String adaptFor(String statement) {
        switch (dbVendor) {
            case DBVendor.ORACLE:
                return adaptForOracle(statement)
                break;
            case DBVendor.MYSQL:
                return adaptForMysql(statement)
                break;
            case DBVendor.SQLSERVER:
                return adaptForSqlServer(statement)
                break;
            default:
                return statement
        }
    }

    def static String adaptForOracle(String statement) {
        def oracleStatement = statement;
        oracleStatement = oracleStatement.replaceAll("BYTEA", "BLOB")
        oracleStatement = oracleStatement.replaceAll("BIGINT", "NUMBER(19, 0)")
        oracleStatement = oracleStatement.replaceAll("INT8", "NUMBER(19, 0)")
        oracleStatement = oracleStatement.replaceAll("INTEGER", "NUMBER(10, 0)")
        oracleStatement = oracleStatement.replaceAll("VARCHAR", "VARCHAR2")
        oracleStatement = oracleStatement.replaceAll("TEXT", "VARCHAR2(1024)")
        oracleStatement = oracleStatement.replaceAll("SMALLINT", "SMALLINT")
        oracleStatement = oracleStatement.replaceAll("LONGVARBINARY", "BLOB")
        oracleStatement = oracleStatement.replaceAll("LONGBLOB", "BLOB")
        oracleStatement = oracleStatement.replaceAll("BOOLEAN", " NUMBER(1)")
        oracleStatement = oracleStatement.replaceAll("true", "1")
        oracleStatement = oracleStatement.replaceAll("false", "0")
        return oracleStatement;
    }

    def static String adaptForMysql(String statement) {
        def mysqlStatement = statement;
        mysqlStatement = mysqlStatement.replaceAll("BYTEA", "BLOB")
        mysqlStatement = mysqlStatement.replaceAll("INT8", "BIGINT")
        return mysqlStatement;
    }

    def static String adaptForSqlServer(String statement) {
        def sqlServerStatement = statement;
        sqlServerStatement = sqlServerStatement.replaceAll("BYTEA", "VARBINARY(MAX)")
        sqlServerStatement = sqlServerStatement.replaceAll("BLOB", "VARBINARY(MAX)")
        sqlServerStatement = sqlServerStatement.replaceAll("BIGINT", "NUMERIC(19, 0)")
        sqlServerStatement = sqlServerStatement.replaceAll("INT8", "NUMERIC(19, 0)")
        sqlServerStatement = sqlServerStatement.replaceAll("VARCHAR", "NVARCHAR")
        sqlServerStatement = sqlServerStatement.replaceAll("TEXT", "NVARCHAR(MAX)")
        sqlServerStatement = sqlServerStatement.replaceAll("LONGVARBINARY", "BLOB")
        sqlServerStatement = sqlServerStatement.replaceAll("DEFAULT true", "DEFAULT 1")
        sqlServerStatement = sqlServerStatement.replaceAll("DEFAULT TRUE", "DEFAULT 1")
        sqlServerStatement = sqlServerStatement.replaceAll("DEFAULT false", "DEFAULT 0")
        sqlServerStatement = sqlServerStatement.replaceAll("DEFAULT FALSE", "DEFAULT 0")
        sqlServerStatement = sqlServerStatement.replaceAll("BOOLEAN", " BIT")
        sqlServerStatement = sqlServerStatement.replaceAll("false", "0")
        sqlServerStatement = sqlServerStatement.replaceAll("true", "1")
        sqlServerStatement = sqlServerStatement.replaceAll(";", "\nGO")
        return sqlServerStatement;
    }

    def renameColumn(String table, String oldName, String newName, String newType) {
        switch (dbVendor) {
            case DBVendor.ORACLE:
                execute("ALTER TABLE $table RENAME COLUMN $oldName TO $newName")
                break;
            case DBVendor.MYSQL:
                execute("ALTER TABLE $table CHANGE COLUMN $oldName $newName $newType")
                break;
            case DBVendor.SQLSERVER:
                execute("""BEGIN
EXEC sp_rename '${table}.$oldName', '$newName', 'COLUMN'
END""")
                break;
            default:
                executeDbVendorStatement("ALTER TABLE $table RENAME $oldName TO $newName")
        }
    }


    def dropTableIfExists(String tableName) {
        switch (dbVendor) {
        //same script for Postgres and MySQL
            case DBVendor.POSTGRES:
            case DBVendor.MYSQL:
                executeDbVendorStatement("DROP TABLE IF EXISTS $tableName")
                break

            case DBVendor.ORACLE:
                def query = """
                    SELECT *
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                if (sql.firstRow(query) != null) {
                    executeDbVendorStatement("DROP TABLE $tableName")
                }
                break

            case DBVendor.SQLSERVER:
                executeDbVendorStatement("""
                    IF OBJECT_ID('$tableName', 'U') IS NOT NULL
                    DROP TABLE $tableName;
                """)
                break

        }
    }

    def renameTable(String table, String newName) {
        switch (dbVendor) {
            case DBVendor.MYSQL:
                execute("RENAME TABLE $table TO $newName")
                break;
            case DBVendor.SQLSERVER:
                execute("sp_rename $table , $newName")
                break;
            default:
                execute("ALTER TABLE $table RENAME TO $newName")
        }
    }

    def dropNotNull(String table, String column, String type) {
        switch (dbVendor) {
            case DBVendor.ORACLE:
                execute("ALTER TABLE $table MODIFY $column NULL")
                break;
            case DBVendor.MYSQL:
                execute("ALTER TABLE $table MODIFY $column $type NULL")
                break;
            case DBVendor.SQLSERVER:
                execute("ALTER TABLE $table ALTER COLUMN $column $type NULL")
                break;
            default:
                execute("ALTER TABLE $table ALTER COLUMN $column DROP NOT NULL")
        }
    }

    def dropColumn(String table, String column) {
        switch (dbVendor) {
            case DBVendor.ORACLE:
                execute("ALTER TABLE $table DROP COLUMN $column")
                break;
            case DBVendor.SQLSERVER:
                execute("ALTER TABLE $table DROP COLUMN $column")
                break;
            default:
                execute("ALTER TABLE $table DROP $column")
        }
    }

    def addColumn(String table, String column, String type, String defaultValue, String constraint) {
        execute("ALTER TABLE $table ADD $column $type ${defaultValue != null ? "DEFAULT $defaultValue" : ""} ${constraint != null ? constraint : ""}")
    }

    def dropForeignKey(String table, String name) {
        switch (dbVendor) {
            case DBVendor.MYSQL:
                execute("ALTER TABLE $table DROP FOREIGN KEY $name")
                break;
            default:
                execute("ALTER TABLE $table DROP CONSTRAINT $name")
        }
    }

    /**
     * remove existing index if already exists and create new index
     * @param tableName
     * @param indexName
     * @param columns
     * @return create index SQl statement
     */
    def String addOrReplaceIndex(String tableName, String indexName, String... columns) {
        dropIndexIfExists(tableName, indexName)

        def concatenatedColumns = columns.collect { it }.join(", ")
        String request = "CREATE INDEX $indexName ON $tableName ($concatenatedColumns)"
        println "Executing request: $request"
        sql.execute(request)
        return request
    }

    /**
     * remove existing index if already exists
     * @param tableName
     * @param indexName
     * @return
     */
    def dropIndexIfExists(String tableName, String indexName) {
        if (hasIndexOnTable(tableName, indexName)) {
            def query
            switch (dbVendor) {
                case DBVendor.POSTGRES:
                case DBVendor.ORACLE:
                    query = "DROP INDEX " + indexName
                    break
                case DBVendor.MYSQL:
                    query = "DROP INDEX " + indexName + " on " + tableName
                    break
                case DBVendor.SQLSERVER:
                    query = "DROP INDEX " + tableName + "." + indexName
                    break
            }
            sql.execute(query)
        }
    }

    /**
     * retrieve index definition for a given table
     * @param tableName
     * @param indexName
     * @return
     */
    def IndexDefinition getIndexDefinition(String tableName, String indexName) {
        def query = getScriptContent("/database/indexDefinition", "indexDefinition")
        def indexDefinition = new IndexDefinition(tableName, indexName)
        sql.eachRow(query, [tableName, indexName]) {
            indexDefinition.addColumn(new ColumnDefinition(it["column_name"], it["column_order"]))
        }
        indexDefinition
    }

    /**
     * checks if given foreign key exists on table
     * @param tableName
     * @param foreignKeyName
     * @return true if exists, false otherwise
     */
    def boolean hasForeignKeyOnTable(String tableName, String foreignKeyName) {
        def query = getScriptContent("/database/foreignKey", "foreignKey")
        def firstRow = sql.firstRow(query, [tableName, foreignKeyName])
        return firstRow != null
    }

    /**
     * checks if given index exists on table
     * @param tableName
     * @param indexName
     * @return true if exists, false otherwise
     */
    def boolean hasIndexOnTable(String tableName, String indexName) {
        def query
        switch (dbVendor) {
            case DBVendor.POSTGRES:
                query = """
                    SELECT
                        pg_class.relname AS table_name,
                        pg2.relname AS index_name
                    FROM
                        pg_index,
                        pg_class,
                        pg_class AS pg2
                    WHERE
                        pg_class.oid = pg_index.indrelid
                        AND pg2.oid = pg_index.indexrelid
                        AND UPPER(pg_class.relname) = UPPER(?)
                        AND UPPER(pg2.relname) = UPPER(?)
                    """
                break

            case DBVendor.ORACLE:
                query = """
                    SELECT
                        i.TABLE_NAME,
                        i.INDEX_NAME
                    FROM
                        USER_INDEXES i
                    WHERE
                        LOWER(i.TABLE_NAME) = LOWER(?)
                        AND LOWER(i.index_name) = LOWER(?)
                    """
                break

            case DBVendor.MYSQL:
                query = """
                SELECT
                    DISTINCT s.TABLE_NAME,
                    s.INDEX_NAME
                FROM
                    INFORMATION_SCHEMA.STATISTICS s
                WHERE
                    s.TABLE_SCHEMA =(
                        SELECT
                            DATABASE()
                    )
                    AND UPPER( s.table_name ) = UPPER( ? )
                    AND UPPER( s.index_name ) = UPPER( ? )
                    """
                break

            case DBVendor.SQLSERVER:
                query = """
                   SELECT
                        t.name,
                        i.name
                    FROM
                        sys.tables t INNER JOIN sys.indexes i
                            ON i.object_id = t.object_id
                    WHERE
                        UPPER(t.name) = UPPER(?)
                        AND UPPER(i.name) = UPPER(?)
                    """
                break
        }

        def firstRow = sql.firstRow(query, [tableName, indexName])
        return firstRow != null
    }

    /**
     * checks if given column exists on table
     * @param tableName
     * @param columnName
     * @return true if exists, false otherwise
     */
    def boolean hasColumnOnTable(String tableName, String columnName) {
        def query
        switch (dbVendor) {
            case DBVendor.POSTGRES:
            case DBVendor.SQLSERVER:
                query = """
                    SELECT
                        C.TABLE_NAME,
                        C.COLUMN_NAME
                    FROM
                        INFORMATION_SCHEMA.COLUMNS C
                    WHERE
                         UPPER( C.TABLE_NAME ) = UPPER( ? )
                    AND UPPER( C.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case DBVendor.ORACLE:
                query = """
                   SELECT
                        c.TABLE_NAME,
                        c.COLUMN_NAME
                    FROM
                        user_tab_cols c
                    WHERE
                         UPPER( c.TABLE_NAME ) = UPPER( ? )
                    AND UPPER( c.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case DBVendor.MYSQL:
                query = """
                SELECT
                    c.TABLE_NAME,
                    c.COLUMN_NAME
                FROM
                    INFORMATION_SCHEMA.COLUMNS c
                WHERE
                    c.TABLE_SCHEMA =(
                        SELECT
                            DATABASE()
                    )
                    AND UPPER( c.TABLE_NAME ) = UPPER( ? )
                    AND UPPER( c.COLUMN_NAME ) = UPPER( ? )
                    """
                break
        }

        def firstRow = sql.firstRow(query, [tableName, columnName])
        return firstRow != null
    }

    def GroovyRowResult selectFirstRow(GString string) {
        return sql.firstRow(adaptFor(string))
    }

    def long getAndUpdateNextSequenceId(long sequenceId, long tenantId) {
        def long nextId = (Long) selectFirstRow("SELECT nextId from sequence WHERE id = $sequenceId and tenantId = $tenantId").get("nextId")
        executeUpdate("UPDATE sequence SET nextId = ${nextId + 1} WHERE tenantId = $tenantId and id = $sequenceId")
        return nextId
    }

    /**
     * get a script from the resources and execute it
     *
     * the script should be located in the src/main/resources/version/to_<version>/<dbvendor>_<scriptName>.sql
     * @param scriptName
     */
    def executeScript(String folderName, String scriptName) {
        def statements = getScriptContent(getVersionFolder() + "/$folderName", scriptName).split("@@")
        statements.each {
            def trimmed = it.trim()
            println "execute statement:\n${trimmed}"
            sql.execute(trimmed)
        }
    }

    private String getScriptContent(String folderName, String scriptName) {
        def scriptContent = ""
        def sqlFile = "$folderName/${dbVendor.toString().toLowerCase()}_${scriptName}.sql"
        println "execute script: $sqlFile"
        def stream1 = this.class.getResourceAsStream(sqlFile)
        stream1.withStream { InputStream s ->
            scriptContent = s.text
        }
        scriptContent
    }

    private GString getVersionFolder() {
        def versionFolder = "/version/to_${version.replace('.', '_')}"
        versionFolder
    }

    static String getClobContent(Object clob) {
        if (clob instanceof String) {
            return clob
        } else {
            return clob.stringValue()
        }
    }

    def addSequenceOnAllTenants(int sequenceKey) {
        getAllTenants().each {
            tenant -> sql.execute("INSERT INTO sequence (tenantid, id, nextid) VALUES(${tenant.id}, $sequenceKey, 1)")
        }
    }

    def getSequenceValue(def tenantId, def sequenceId) {
        sql.firstRow("select s.tenantid,s.id,s.nextid from sequence s where s.tenantid = ${tenantId} and s.id=${sequenceId}")
    }

    def getAllTenants() {
        sql.rows("select t.id, t.name, t.status from tenant t order by t.id")
    }

}
