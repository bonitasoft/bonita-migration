/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.update.core.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.PackageScope
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateStep.DBVendor
import org.bonitasoft.update.core.database.schema.ForeignKeyDefinition
import org.bonitasoft.update.core.database.schema.IndexDefinition

import static org.bonitasoft.update.core.UpdateStep.DBVendor.*
/**
 * @author Baptiste Mesta
 */
class DatabaseHelper {


    Sql sql
    DBVendor dbVendor
    String version
    Logger logger

    /**
     * execute a postgres script converted to the database specified by dbVendor
     *
     * this method should not be use anymore and will be removed in next versions.
     * using "adaptFor" before executing request lead to renaming fields names instead of field definition as expected.
     * use DatabaseHelper.executeScript method in replacement ,
     * and store db vendor specific queries stored in resources/database/TOPIC/DB_VENDOR_TOPIC.sql files
     *
     * @param statement
     * @return
     */
    @Deprecated
    boolean execute(GString statement) {
        //TODO: replace statement by file name, get current version from context & split statement against @@ joker
        return sql.execute(adaptFor(statement))
    }

    /**
     * execute statement without adapting syntax to dbVendor dialect
     * @param statement
     * @return
     */
    boolean executeDbVendorStatement(String statement) {
        return sql.execute(statement)
    }

    boolean execute(String statement) {
        return sql.execute(adaptFor(statement))
    }

    boolean execute(String statement, List<Object> params) {
        return sql.execute(adaptFor(statement), params)
    }

    int executeUpdate(GString statement) {
        return sql.executeUpdate(adaptFor(statement))
    }

    int executeUpdate(String statement) {
        return sql.executeUpdate(adaptFor(statement))
    }

    /**
     * adapting could have a different result than a fresh install
     * example: VARCHAR(50) should be a VARCHAR2(50 CHAR) in oracle
     * example: column name contains reserved keyword (qrtz_simprop_triggers)
     * @param statement
     * @return
     */
    @Deprecated
    String adaptFor(String statement) {
        switch (dbVendor) {
            case ORACLE:
                return adaptForOracle(statement)
                break
            case MYSQL:
                return adaptForMysql(statement)
                break
            case SQLSERVER:
                return adaptForSqlServer(statement)
                break
            default:
                return statement
        }
    }

    static String adaptForOracle(String statement) {
        def oracleStatement = statement
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
        return oracleStatement
    }

    static String adaptForMysql(String statement) {
        def mysqlStatement = statement
        mysqlStatement = mysqlStatement.replaceAll("BYTEA", "BLOB")
        mysqlStatement = mysqlStatement.replaceAll("INT8", "BIGINT")
        return mysqlStatement
    }

    static String adaptForSqlServer(String statement) {
        def sqlServerStatement = statement
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
        return sqlServerStatement
    }

    def renameColumn(String table, String oldName, String newName, String newType) {
        def sql = "ALTER TABLE $table RENAME $oldName TO $newName"
        switch (dbVendor) {
            case ORACLE:
                sql = "ALTER TABLE ${table} RENAME COLUMN ${oldName} TO ${newName}"
                break
            case MYSQL:
                sql = "ALTER TABLE $table CHANGE COLUMN `${oldName}` `${newName}` ${newType}"
                break
            case SQLSERVER:
                sql = """BEGIN
EXEC sp_rename '${table}.${oldName}', '${newName}', 'COLUMN'
END"""
        }
        executeDbVendorStatement(sql as String)
    }


    def dropTableIfExists(String tableName) {
        switch (dbVendor) {
            //same script for Postgres and MySQL
            case POSTGRES:
            case MYSQL:
                executeDbVendorStatement("DROP TABLE IF EXISTS $tableName")
                break

            case ORACLE:
                def query = """
                    SELECT *
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                if (sql.firstRow(query) != null) {
                    executeDbVendorStatement("DROP TABLE $tableName")
                }
                break

            case SQLSERVER:
                executeDbVendorStatement("""
                    IF OBJECT_ID('$tableName', 'U') IS NOT NULL
                    DROP TABLE $tableName;
                """)
                break
        }
    }

    boolean hasTable(String tableName) {
        def query
        switch (dbVendor) {
            case POSTGRES:
                query = """
                    SELECT *
                     FROM information_schema.tables
                     WHERE table_schema='public'
                       AND table_type='BASE TABLE'
                       AND UPPER(table_name) = UPPER($tableName)
                    """
                break

            case ORACLE:
                query = """
                    SELECT *
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                break

            case MYSQL:
                query = """
                    SELECT *
                    FROM information_schema.tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    AND table_schema = DATABASE()
                    """
                break

            case SQLSERVER:
                query = """
                    SELECT * FROM information_schema.tables
                    WHERE UPPER(TABLE_NAME) = UPPER($tableName)
                    """
                break
        }
        def firstRow = sql.firstRow(query)
        return firstRow != null
    }


    String concat(String... argsToConcat) {
        // If called from a GString, calling a toString at the end might be necessary
        def result
        if (argsToConcat.size() > 2) {
            if (dbVendor == ORACLE) {
                result = argsToConcat.first()
                for (int i = 1; i in argsToConcat.getIndices(); i++) {
                    result = result + " || " + argsToConcat[i]
                }
                return result
            } else {
                result = " concat( " + argsToConcat.first()
                for (int i = 1; i < argsToConcat.size(); i++) {
                    result = result + ", " + argsToConcat[i]
                }
                result = result + " )"
                return result
            }
        } else {
            return argsToConcat.first()
        }
    }

    def renameTable(String table, String newName) {
        switch (dbVendor) {
            case MYSQL:
                execute("RENAME TABLE $table TO $newName")
                break
            case SQLSERVER:
                execute("sp_rename $table , $newName")
                break
            default:
                execute("ALTER TABLE $table RENAME TO $newName")
        }
    }

    def dropNotNull(String table, String column, String type) {
        switch (dbVendor) {
            case ORACLE:
                execute("ALTER TABLE $table MODIFY $column NULL")
                break
            case MYSQL:
                execute("ALTER TABLE $table MODIFY $column $type NULL")
                break
            case SQLSERVER:
                execute("ALTER TABLE $table ALTER COLUMN $column $type NULL")
                break
            default:
                execute("ALTER TABLE $table ALTER COLUMN $column DROP NOT NULL")
        }
    }

    def dropColumnIfExists(String table, String column) {
        if (hasColumnOnTable(table, column)) {
            switch (dbVendor) {
                case ORACLE:
                case SQLSERVER:
                    execute("ALTER TABLE $table DROP COLUMN $column")
                    break
                default:
                    execute("ALTER TABLE $table DROP $column")
            }
            logger.info("Dropped column '$column' from table '$table'")
        } else {
            logger.info("Column '$column' does not exist on table '$table'. Skipping DROP instruction.")
        }
    }

    /**
     * <b>IMPORTANT</b>: the default value is only used to fill the column when creating it. The default instruction is
     * then removed from the column as Bonita is always responsible for setting field values: we never rely on the database
     * to set field values.
     */
    def addColumn(String table, String column, String type, String defaultValue, String constraint) {
        def defaultValueClause = defaultValue != null ? "DEFAULT $defaultValue" : ""
        def constraintClause = constraint != null ? constraint : ""
        sql.execute("ALTER TABLE $table ADD $column $type $defaultValueClause $constraintClause" as String)
        // in this case, sqlserver sets the constraint but lets the column with a null value, so set the value by hand
        if (dbVendor == SQLSERVER && defaultValue != null && constraint == null) {
            sql.execute("UPDATE $table set $column = $defaultValue" as String)
        }
        dropColumnDefaultValueIfExists(table, column)
    }

    void dropColumnDefaultValueIfExists(String table, String column) {
        switch (dbVendor) {
            case ORACLE:
                sql.execute("ALTER TABLE $table MODIFY $column default NULL" as String)
                break
            case SQLSERVER:
                def defaultConstraintName = getSqlServerDefaultValueConstraintName(table, column)
                if (defaultConstraintName != null) {
                    sql.execute("ALTER TABLE $table DROP CONSTRAINT $defaultConstraintName" as String)
                }
                break
            case MYSQL:
                String defaultColumnValue = getMysqlColumnDefaultValue(table, column)
                if (defaultColumnValue != null) {
                    sql.execute("ALTER TABLE $table ALTER COLUMN $column drop default" as String)
                }
                break
            default:
                sql.execute("ALTER TABLE $table ALTER COLUMN $column drop default" as String)
        }
    }

    private String getSqlServerDefaultValueConstraintName(String table, String column) {
        return sql.firstRow("""
                SELECT name FROM SYS.DEFAULT_CONSTRAINTS
                WHERE PARENT_OBJECT_ID = OBJECT_ID('$table')
                  AND PARENT_COLUMN_ID = (
                    SELECT column_id FROM sys.columns
                    WHERE NAME = N'$column'
                      AND object_id = OBJECT_ID(N'$table')
                  )
                """ as String)?.get('name')
    }

    private String getMysqlColumnDefaultValue(String table, String column) {
        return sql.firstRow("""
                SELECT column_default FROM INFORMATION_SCHEMA.COLUMNS
                WHERE table_name = '$table'
                AND column_name = '$column'
                AND column_default is not null
                """ as String)?.get('column_default')
    }

    /**
     * <b>IMPORTANT</b>: see {@link #addColumn(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * for warnings about the default value.
     */
    def addColumnIfNotExist(String table, String columnName, String type, String defaultValue, String constraint) {
        if (!hasColumnOnTable(table, columnName)) {
            addColumn(table, columnName, type, defaultValue, constraint)
        }
    }

    def dropForeignKey(String table, String foreignKeyName) {
        if (!hasForeignKeyOnTable(table, foreignKeyName)) {
            logger.warn "Foreign key ${foreignKeyName} not found on table ${table}"
            return
        }
        def request
        switch (dbVendor) {
            case MYSQL:
                request = "ALTER TABLE " + table + " DROP FOREIGN KEY " + foreignKeyName
                break
            default:
                request = "ALTER TABLE " + table + " DROP CONSTRAINT " + foreignKeyName
        }
        logger.info "Executing request: $request"
        sql.execute(request)
        if (dbVendor == MYSQL) {
            // In the case of Mysql, an index is automatically created with the same name as the foreign key:
            dropIndexIfExists(table, foreignKeyName)
        }
    }

    /**
     * drop all foreign keys found on table
     * @param table
     * @return
     */
    def dropAllForeignKeys(String tableName) {
        def query = getScriptContent("/database/allForeignKeys", "foreignKey")
        sql.eachRow(query, [tableName]) { row ->
            dropForeignKey(row.table_name, row.constraint_name)
        }
    }

    def createForeignKey(String referencingTableName, String foreignKeyName, String referencedTableName,
            List<String> referencingColumns, List<String> referencedColumns, boolean onDeleteCascade) {
        def referencingCols = referencingColumns.collect { it }.join(", ")
        def referencedCols = referencedColumns.collect { it }.join(", ")
        String request = """ALTER TABLE $referencingTableName ADD CONSTRAINT $foreignKeyName FOREIGN KEY ($referencingCols)
                REFERENCES $referencedTableName ($referencedCols) ${onDeleteCascade ? "ON DELETE CASCADE" : ""}"""
        logger.info "Executing request: $request"
        sql.execute(request)
    }

    def dropPrimaryKey(String tableName) {
        def query = getScriptContent("/database/primaryKey", "primaryKey")
        sql.eachRow(query, [tableName]) { row ->
            String request
            switch (dbVendor) {
                case MYSQL:
                    request = "ALTER TABLE ${row.TABLE_NAME} DROP PRIMARY KEY"
                    break
                default:
                    request = "ALTER TABLE ${row.TABLE_NAME} DROP CONSTRAINT ${row.CONSTRAINT_NAME}"
            }
            logger.info "Executing request: $request"
            sql.execute(request)
        }
    }

    /**
     * By convention, the primary key created is named `pk_${tableName}`
     */
    def createPrimaryKey(String tableName, String... columns) {
        def concatenatedColumns = columns.collect { it }.join(", ")
        String request = "ALTER TABLE $tableName ADD CONSTRAINT pk_${tableName} PRIMARY KEY ($concatenatedColumns)"
        logger.info "Executing request: $request"
        sql.execute(request)
    }

    /**
     * remove unique constraint on table.
     * specific to oracle:
     *  in case index has been modified after constraint creation
     *  such as tablespace rebuild, table import export
     *  add drop of index
     * @param tableName
     * @param ukName
     * @return
     */
    def dropUniqueKey(String tableName, String ukName) {
        if (hasUniqueKeyOnTable(tableName, ukName)) {
            doDropExistingUniqueKey(tableName, ukName)
        } else {
            logger.warn("Unique key ${ukName} not found on table ${tableName}")
        }
    }

    def dropUniqueKeyWithNameInList(String tableName, String... ukNameList) {
        def found = false
        for (String ukName : ukNameList) {
            if (hasUniqueKeyOnTable(tableName, ukName)) {
                doDropExistingUniqueKey(tableName, ukName)
                found = true
            }
        }
        if (!found) {
            logger.warn("Unique key not found on table ${tableName} in name list ${ukNameList}")
        }
    }

    private doDropExistingUniqueKey(String tableName, String ukName) {
        switch (dbVendor) {
            case ORACLE:
                sql.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT " + ukName)
                if (hasIndexOnTable(tableName, ukName)) {
                    sql.execute("DROP INDEX " + ukName)
                }
                break
            case MYSQL:
                sql.execute("ALTER TABLE " + tableName + " DROP INDEX " + ukName)
                break
            default:
                sql.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT " + ukName)
                break
        }
    }

    /**
     * Drop the unique key identified by the given columns on the given table.
     * @param tableName table name where the unique key is defined
     * @param columns case is not important, as lookup is forced lowercase
     */
    def dropUniqueKeyFromColumns(String tableName, String... columns) {
        String ukName = getUniqueKeyByColumns(tableName, columns)
        if (ukName) {
            dropUniqueKey(tableName, ukName)
        } else {
            logger.warn("No unique key found on table $tableName for columns ${columns}")
        }
    }

    def createUniqueKey(String tableName, String constraintName, String... columns) {
        def concatenatedColumns = columns.collect { it }.join(", ")
        String request = "ALTER TABLE $tableName ADD CONSTRAINT $constraintName UNIQUE ($concatenatedColumns)"
        logger.info "Executing request: $request"
        sql.execute(request)
    }

    /**
     * Get the unique key name for the given columns on the given table.
     * @param tableName table name where the unique key is defined
     * @param columns columns of the unique key
     * @return the unique key name or null if not found
     */
    String getUniqueKeyByColumns(String tableName, String... columns) {
        def query = getScriptContent("/database/uniqueKeyByColumns", "uniqueKey")
                // groovy sql does not support list parameters
                .replaceAll("@COLUMN_NAMES@", columns.collect { it.toLowerCase() }.join("','"))
        def result = sql.firstRow(query, [tableName, columns.size()])
        return result == null || result.isEmpty() ? null : result[0] as String
    }

    String getUniqueKeyNameOnTable(String tableName) {
        def query = getScriptContent("/database/uniqueUnnamedKey", "uniqueKey")
        def result = sql.firstRow(query, [tableName])
        return result == null || result.isEmpty() ? null : result[0] as String
    }

    /**
     * Remove existing index if already exists and create new index
     *
     * @return create index SQl statement
     */
    String addOrReplaceIndex(String tableName, String indexName, String... columns) {
        dropIndexIfExists(tableName, indexName)
        return createIndex(tableName, indexName, columns)
    }

    String createIndex(String tableName, String indexName, boolean unique = false, String... columns) {
        def concatenatedColumns = columns.collect { it }.join(", ")
        String request = "CREATE ${unique?"UNIQUE ":""}INDEX $indexName ON $tableName($concatenatedColumns)"
        logger.info "Creating index: $request"
        sql.execute(request)
        return request
    }

    String createUniqueConstraint(String tableName, String constraintName, String... columns) {
        def concatenatedColumns = columns.collect { it }.join(", ")
        String request = "ALTER TABLE ${tableName} ADD CONSTRAINT $constraintName UNIQUE ($concatenatedColumns)"
        logger.info "Creating unique constraint: $request"
        sql.execute(request)
        return request
    }

    void renameIndex(String tableName, String oldName, String newName) {
        String query
        switch (dbVendor) {
            case POSTGRES:
            case ORACLE:
                query = "ALTER INDEX $oldName RENAME TO $newName"
                break
            case MYSQL:
                query = "ALTER TABLE $tableName RENAME INDEX $oldName TO $newName"
                break
            case SQLSERVER:
                query = """BEGIN
EXEC sp_rename N'${tableName}.${oldName}', N'${newName}', N'INDEX'
END"""
                break
        }
        logger.info "Renaming index: $query"
        sql.execute(query)
    }

    /**
     * Create new index if not already exists
     * @return create index SQl statement, or empty string if index already exists
     */
    String addIndexIfMissing(String tableName, String indexName, String... columns) {
        if (hasIndexOnTable(tableName, indexName)) {
            logger.info "Index $indexName already exists on table $tableName. Skipping creation."
            return ""
        }
        return createIndex(tableName, indexName, columns)
    }

    /**
     * remove index if exists
     */
    def dropIndexIfExists(String tableName, String indexName) {
        if (hasIndexOnTable(tableName, indexName)) {
            String query
            switch (dbVendor) {
                case POSTGRES:
                case ORACLE:
                    query = "DROP INDEX " + indexName
                    break
                case MYSQL:
                    query = "DROP INDEX " + indexName + " ON " + tableName
                    break
                case SQLSERVER:
                    query = "DROP INDEX " + tableName + "." + indexName
                    break
            }
            logger.info "Deleting index: $query"
            sql.execute(query)
        } else {
            logger.debug "Index $indexName does not exist on table $tableName. Skipping deletion."
        }
    }

    /**
     * retrieve index definition for a given table from database
     */
    IndexDefinition getIndexDefinition(String tableName, String indexName) {
        def query = getScriptContent("/database/indexDefinition", "indexDefinition")
        def indexDefinition = new IndexDefinition(tableName, indexName)
        boolean exists = false
        sql.eachRow(query, [tableName, indexName]) {
            indexDefinition.addColumn(it["column_name"] as String)
            exists = true
        }
        return !exists ? null : indexDefinition
    }

    /**
     * retrieve potential index definition for a given table and a list of columns
     */
    IndexDefinition getIndexDefinition(String tableName, boolean unique = false, String... columnNames) {
        def columns
        def result
        if (dbVendor == POSTGRES) {
            def query = """SELECT LOWER(indexname)
FROM pg_indexes
WHERE LOWER(tablename) = LOWER(?)
  AND LOWER(indexdef) LIKE LOWER(?)"""
            columns = "CREATE ${unique?'UNIQUE ':''}INDEX%(" + columnNames.collect { it }.join(", ") + ")%" as String
            result = sql.firstRow(query, [tableName, columns])
        } else if (dbVendor == MYSQL) {
            def query = """SELECT LOWER(INDEX_NAME)
FROM INFORMATION_SCHEMA.STATISTICS
WHERE NON_UNIQUE = ${unique?'0':'1'}
    AND TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = ?
    AND LOWER(COLUMN_NAME) IN (${columnNames.collect{'?'}.join(',')})
GROUP BY INDEX_NAME
HAVING MAX(SEQ_IN_INDEX) = ? AND COUNT(DISTINCT COLUMN_NAME) = ?
"""
            List<Object> params = [tableName]
            params.addAll(columnNames)
            params.add(columnNames.size())
            params.add(columnNames.size())
            result = sql.firstRow(query, params)
        } else if (dbVendor == SQLSERVER) {
            def query = """SELECT LOWER(i.name) AS index_name
FROM sys.indexes i
    JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
    JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE LOWER(i.object_id) = OBJECT_ID(?)
    AND LOWER(c.name) IN (${columnNames.collect{'?'}.join(',')})
    AND i.is_unique = ${unique?'1':'0'}
GROUP BY i.name
HAVING COUNT(DISTINCT c.name) = ? and MAX(index_column_id) = ?
"""
            List<Object> params = [tableName]
            params.addAll(columnNames)
            params.add(columnNames.size())
            params.add(columnNames.size())
            result = sql.firstRow(query, params)
        } else {
            // ORACLE
            def query = """SELECT LOWER(i.index_name)
FROM all_indexes i
    JOIN all_ind_columns ic ON i.index_name = ic.index_name AND i.table_name = ic.table_name
WHERE LOWER(i.table_name) = ?
    AND LOWER(ic.column_name) IN (${columnNames.collect{'?'}.join(',')})
    AND i.uniqueness = '${unique?'UNIQUE':'NONUNIQUE'}'
GROUP BY i.index_name
HAVING COUNT(DISTINCT ic.column_name) = ? AND MAX(column_position) = ?"""
            List<Object> params = [tableName]
            params.addAll(columnNames)
            params.add(columnNames.size())
            params.add(columnNames.size())
            result = sql.firstRow(query, params)
        }
        return !result || result.isEmpty() ? null : new IndexDefinition(tableName, result[0] as String, columnNames)
    }

    /**
     * retrieve foreign keys definition pointing to a given table
     * @param tableName table pointed by foreign keys
     * @return list of FkDefinition
     */
    List<ForeignKeyDefinition> getForeignKeyReferences(String tableName) {
        def query = getScriptContent("/database/foreignKeyReference", "foreignKeyRef")
        def fkReferences = []
        sql.eachRow(query, [tableName]) { row ->
            fkReferences.add(new ForeignKeyDefinition(row.table_name, row.constraint_name))
        }
        fkReferences
    }

    /**
     * checks if given foreign key exists on table
     * @param tableName
     * @param foreignKeyName
     * @return true if exists, false otherwise
     */
    boolean hasForeignKeyOnTable(String tableName, String foreignKeyName) {
        def query = getScriptContent("/database/foreignKey", "foreignKey")
        def firstRow = sql.firstRow(query, [tableName, foreignKeyName])
        return firstRow != null
    }

    /**
     * checks if primary key exists on table
     * @param tableName name of the table
     * @param pkName name of the primary key
     * @return true if exists, false otherwise
     */
    boolean hasPrimaryKeyOnTable(String tableName, String pkName) {
        def primaryKey = getPrimaryKey(tableName)
        switch (dbVendor) {
            case MYSQL:
                return primaryKey != null // because MySQL does not store the PK name
            default:
                return primaryKey != null && primaryKey.toLowerCase() == pkName.toLowerCase()
        }
    }

    /**
     * checks if unique key exists on table
     * @param tableName name of the table
     * @param ukName name of the unique key
     * @return true if exists, false otherwise
     */
    boolean hasUniqueKeyOnTable(String tableName, String ukName) {
        def query = getScriptContent("/database/uniqueKeyByName", "uniqueKey")
        def firstRow = sql.firstRow(query, [tableName, ukName])
        return firstRow != null
    }

    /**
     * Checks if a unique key exists on a table with the given columns
     * @param tableName table name where the unique key should be defined
     * @param columns columns of the unique key
     * @return true if exists, false otherwise
     */
    boolean hasUniqueKeyOnTableByColumns(String tableName, String... columns) {
        return getUniqueKeyByColumns(tableName, columns) != null
    }

    /**
     * get primary key name
     * @param tableName
     * @return pk name if exists, null otherwise
     */
    String getPrimaryKey(String tableName) {
        def query = getScriptContent("/database/primaryKey", "primaryKey")
        def firstRow = sql.firstRow(query, [tableName])
        if (firstRow != null) {
            return firstRow.CONSTRAINT_NAME
        }
        return null
    }

    /**
     * checks if given index exists on table
     * @param tableName
     * @param indexName
     * @return true if exists, false otherwise
     */
    boolean hasIndexOnTable(String tableName, String indexName) {
        def query
        switch (dbVendor) {
            case POSTGRES:
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

            case ORACLE:
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

            case MYSQL:
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

            case SQLSERVER:
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
    boolean hasColumnOnTable(String tableName, String columnName) {
        def query
        switch (dbVendor) {
            case POSTGRES:
            case SQLSERVER:
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

            case ORACLE:
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

            case MYSQL:
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

    /**
     * @param tableName
     * @param columnName
     */
    String getColumnType(String tableName, String columnName) {
        def query
        switch (dbVendor) {
            case POSTGRES:
            case SQLSERVER:
                query = """
                    SELECT
                        C.DATA_TYPE
                    FROM
                        INFORMATION_SCHEMA.COLUMNS C
                    WHERE
                         UPPER( C.TABLE_NAME ) = UPPER( ? )
                    AND UPPER( C.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case ORACLE:
                query = """
                   SELECT
                        c.DATA_TYPE
                    FROM
                        user_tab_cols c
                    WHERE
                         UPPER( c.TABLE_NAME ) = UPPER( ? )
                    AND UPPER( c.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case MYSQL:
                query = """
                SELECT
                    c.DATA_TYPE
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
        return firstRow.DATA_TYPE
    }

    GroovyRowResult selectFirstRow(GString string) {
        return sql.firstRow(adaptFor(string))
    }

    long getAndUpdateNextSequenceId(long sequenceId, long tenantId) {
        long nextId = (Long) selectFirstRow("SELECT nextId from sequence WHERE id = $sequenceId and tenantId = $tenantId").get("nextId")
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
        executeScript(version, folderName, scriptName)
    }

    def executeScript(String version, String folderName, String scriptName) {
        def statements = getScriptContent(getVersionFolder(version) + "/$folderName", scriptName).split("@@|GO|;")
        statements.each {
            def trimmed = it.trim()
            if (trimmed != null && !trimmed.empty) {
                logger.info "execute statement:\n${trimmed}"
                def count = sql.executeUpdate(trimmed)
                logger.info "updated $count rows"
            }
        }
    }

    private String getScriptContent(String folderName, String scriptName) {
        def scriptContent = ""
        def sqlFile = "$folderName/${dbVendor.toString().toLowerCase()}_${scriptName}.sql"
        def stream1 = this.class.getResourceAsStream(sqlFile)
        stream1.withStream { InputStream s ->
            scriptContent = s.text
        }
        scriptContent
    }

    private GString getVersionFolder(String version) {
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

    String getBlobContentAsString(Object blobValue) {
        new String(getBlobContentAsBytes(blobValue))
    }

    byte[] getBlobContentAsBytes(blobValue) {
        if (ORACLE == dbVendor) {
            return blobValue.binaryStream.bytes
        } else {
            return blobValue
        }
    }

    String booleanValue(boolean value) {
        return dbVendor == ORACLE || dbVendor == SQLSERVER ? (value ? "1" : "0") : (value ? "true" : "false")
    }

    def addSequenceOnAllTenants(int sequenceKey) {
        getAllTenants().each { tenant ->
            sql.execute("INSERT INTO sequence (tenantid, id, nextid) VALUES(${tenant.id}, $sequenceKey, 1)")
        }
    }

    def getSequenceValue(def tenantId, def sequenceId) {
        sql.firstRow("select s.tenantid,s.id,s.nextid from sequence s where s.tenantid = ${tenantId} and s.id=${sequenceId}")
    }

    def getAllTenants() {
        sql.rows("select t.id, t.name, t.status from tenant t order by t.id")
    }

    def insertSequences(Map<Long, Long> resourcesCount, Integer sequenceId) {
        if (resourcesCount.isEmpty())
            throw new IllegalStateException("There is no tenants on which insert the sequences")
        return resourcesCount.each { it ->
            sql.executeInsert("INSERT INTO sequence VALUES(${it.getKey()}, ${sequenceId}, ${it.getValue()})")
        }
    }

    /**
     * Add a real limit to the SELECT query only.<p>
     *
     * <b>Rationale</b><br>
     * The Groovy rows method with pagination retrieve all data then filters which is a pain from a performance perspective
     */
    def rows(String query, int limit) {
        sql.rows(buildLimitSelectQuery(query, limit))
    }

    // visible for testing
    @PackageScope
    String buildLimitSelectQuery(String query, int limit) {
        String limitQuery = query
        switch (dbVendor) {
            case MYSQL:
            case POSTGRES:
                limitQuery = "$query LIMIT $limit"
                break
            case ORACLE:
                limitQuery = "SELECT * FROM ( $query ) WHERE ROWNUM <= $limit"
                break
            case SQLSERVER:
            // insert top right after 'select '
                limitQuery = "SELECT TOP $limit ${query.substring('select '.length())}"
                break
        }
        limitQuery
    }

    String BOOLEAN() {
        switch (dbVendor) {
            case ORACLE:
                return "NUMBER(1)"
            case SQLSERVER:
                return "BIT"
            default:
                return "BOOLEAN"
        }
    }

    String BLOB() {
        switch (dbVendor) {
            case ORACLE:
                return "BLOB"
            case SQLSERVER:
                return "VARBINARY(MAX)"
            case MYSQL:
                return "LONGBLOB"
            default:
                return "BYTEA"
        }
    }

    String VARCHAR(int size) {
        switch (dbVendor) {
            case ORACLE:
                return "VARCHAR2($size CHAR)"
            case SQLSERVER:
                return "NVARCHAR($size)"
            default:
                return "VARCHAR($size)"
        }
    }

    String TEXT() {
        switch (dbVendor) {
            case ORACLE:
                return "VARCHAR2(1024 CHAR)"
            case SQLSERVER:
                return "NVARCHAR(MAX)"
            default:
                return "TEXT"
        }
    }
}
