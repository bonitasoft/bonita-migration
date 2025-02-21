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

import java.sql.SQLException

import static org.bonitasoft.update.core.UpdateStep.DBVendor.*

class DatabaseHelper {

    Sql sql
    DBVendor dbVendor
    String version
    Logger logger

    private void logQuery(boolean logging, String query, List<Object> params) {
        if (logging) {
            logger.debug("Executing query: ${query.stripIndent()}")
            if (params) {
                logger.debug("With query parameters: $params")
            }
        }
    }

    /**
     * Log and execute the given SQL query
     * @param query the SQL query to execute
     * @param logging if true, the query will be logged (default)
     * @return <code>true</code> if the first result is a <code>ResultSet</code> object;
     *         <code>false</code> if it is an update count or there are no results
     * @see Sql#execute(String)
     */
    boolean executeQuery(String query, boolean logging = true) {
        logQuery(logging, query, null)
        return sql.execute(query)
    }

    /**
     * Log and execute the given SQL update query
     * @param query the SQL update query to execute
     * @param logging if true, the query will be logged (default)
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @see Sql#executeUpdate(String)
     */
    int executeUpdateQuery(String query, boolean logging = true) {
        logQuery(logging, query, null)
        return sql.executeUpdate(query)
    }

    /**
     * Log and execute the given SQL insert query
     * @param query the SQL insert query to execute
     * @param logging if true, the query will be logged (default)
     * @return a list of the auto-generated column values for each inserted row (typically auto-generated keys)
     * @see Sql#executeInsert(String)
     */
    List<List<Object>> executeInsertQuery(String query, boolean logging = true) {
        logQuery(logging, query, null)
        return sql.executeInsert(query)
    }

    /**
     * Log and execute the given SQL query and return the first row of the result set.
     * The query may contain GString expressions.
     * @param query a GString containing the SQL query
     * @param logging if true, the query will be logged (default)
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @see Sql#firstRow(GString)
     */
    GroovyRowResult selectFirstRow(GString query, boolean logging = true) {
        logQuery(logging, query, null)
        return sql.firstRow(query)
    }

    /**
     * Log and execute the given SQL query and return the first row of the result set.
     * @param query the SQL query to execute
     * @param params optional parameters to bind to the query, can be <code>null</code>
     * @param logging if true, the query will be logged
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @see Sql#firstRow(String)
     * @see Sql#firstRow(String, List)
     */
    GroovyRowResult selectFirstRow(String query, List<Object> params = null, boolean logging = true) {
        logQuery(logging, query, params)
        return params == null ? sql.firstRow(query) : sql.firstRow(query, params)
    }

    def renameColumn(String table, String oldName, String newName, String newType) {
        logger.info("Renaming column '$oldName' to '$newName' on table '$table'")
        def sql = "ALTER TABLE $table RENAME $oldName TO $newName"
        switch (dbVendor) {
            case ORACLE:
                sql = "ALTER TABLE ${table} RENAME COLUMN ${oldName} TO ${newName}"
                break
            case MYSQL:
                sql = "ALTER TABLE $table CHANGE COLUMN `${oldName}` `${newName}` ${newType}"
                break
            case SQLSERVER:
                sql = """
                    BEGIN
                    EXEC sp_rename '${table}.${oldName}', '${newName}', 'COLUMN'
                    END
                    """
        }
        executeQuery(sql as String)
    }

    def dropTableIfExists(String tableName) {
        logger.info("Dropping table '$tableName'")
        switch (dbVendor) {
            //same script for Postgres and MySQL
            case POSTGRES:
            case MYSQL:
                executeQuery("DROP TABLE IF EXISTS $tableName")
                break

            case ORACLE:
                def query = """
                    SELECT 1
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                if (selectFirstRow(query) != null) {
                    executeQuery("DROP TABLE $tableName")
                }
                break

            case SQLSERVER:
                executeQuery("""
                    IF OBJECT_ID('$tableName', 'U') IS NOT NULL
                    DROP TABLE $tableName;
                    """)
                break
        }
    }

    boolean hasTable(String tableName) {
        GString query
        switch (dbVendor) {
            case POSTGRES:
                query = """
                    SELECT 1
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_type = 'BASE TABLE'
                      AND UPPER(table_name) = UPPER($tableName)
                    """
                break

            case ORACLE:
                query = """
                    SELECT 1
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                break

            case MYSQL:
                query = """
                    SELECT 1
                    FROM information_schema.tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    AND table_schema = DATABASE()
                    """
                break

            case SQLSERVER:
                query = """
                    SELECT 1
                    FROM information_schema.tables
                    WHERE UPPER(TABLE_NAME) = UPPER($tableName)
                    """
                break
        }
        return selectFirstRow(query) != null
    }

    def renameTable(String table, String newName) {
        logger.info("Renaming table '$table' to '$newName'")
        switch (dbVendor) {
            case MYSQL:
                executeQuery("RENAME TABLE $table TO $newName")
                break
            case SQLSERVER:
                executeQuery("sp_rename $table , $newName")
                break
            default:
                executeQuery("ALTER TABLE $table RENAME TO $newName")
        }
    }

    def dropNotNull(String table, String column, String type) {
        logger.info("Dropping NOT NULL constraint on column '$column' from table '$table'")
        switch (dbVendor) {
            case ORACLE:
                executeQuery("ALTER TABLE $table MODIFY $column NULL")
                break
            case MYSQL:
                executeQuery("ALTER TABLE $table MODIFY $column $type NULL")
                break
            case SQLSERVER:
                executeQuery("ALTER TABLE $table ALTER COLUMN $column $type NULL")
                break
            default:
                executeQuery("ALTER TABLE $table ALTER COLUMN $column DROP NOT NULL")
        }
    }

    /**
     * checks if given column exists on table
     * @return true if exists, false otherwise
     */
    boolean hasColumnOnTable(String tableName, String columnName) {
        def query
        switch (dbVendor) {
            case POSTGRES:
            case SQLSERVER:
                query = """
                    SELECT C.TABLE_NAME, C.COLUMN_NAME
                    FROM INFORMATION_SCHEMA.COLUMNS C
                    WHERE UPPER( C.TABLE_NAME ) = UPPER( ? )
                      AND UPPER( C.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case ORACLE:
                query = """
                    SELECT c.TABLE_NAME, c.COLUMN_NAME
                    FROM user_tab_cols c
                    WHERE UPPER( c.TABLE_NAME ) = UPPER( ? )
                      AND UPPER( c.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case MYSQL:
                query = """
                    SELECT c.TABLE_NAME, c.COLUMN_NAME
                    FROM INFORMATION_SCHEMA.COLUMNS c
                    WHERE c.TABLE_SCHEMA = DATABASE()
                      AND UPPER( c.TABLE_NAME ) = UPPER( ? )
                      AND UPPER( c.COLUMN_NAME ) = UPPER( ? )
                    """
                break
        }

        return selectFirstRow(query, [tableName, columnName]) != null
    }

    def dropColumnIfExists(String table, String column) {
        if (!hasColumnOnTable(table, column)) {
            logger.info("Column '$column' does not exist on table '$table'. Skipping DROP instruction.")
            return
        }
        logger.info("Dropping column '$column' from table '$table'")
        try {
            switch (dbVendor) {
                case ORACLE:
                case SQLSERVER:
                    executeQuery("ALTER TABLE $table DROP COLUMN $column")
                    break
                default:
                    executeQuery("ALTER TABLE $table DROP $column")
            }
        } catch (Exception e) {
            throw new SQLException("Unable to drop column '$column' from table '$table'. $e.message", e)
        }
    }


    /**
     * <b>IMPORTANT</b>: see {@link #addColumn(String, String, String, String, String)}
     * for warnings about the default value.
     */
    def addColumnIfNotExist(String table, String columnName, String type, String defaultValue, String constraint) {
        if (!hasColumnOnTable(table, columnName)) {
            addColumn(table, columnName, type, defaultValue, constraint)
        }
    }

    /**
     * <b>IMPORTANT</b>: the default value is only used to fill the column when creating it. The default instruction is
     * then removed from the column as Bonita is always responsible for setting field values: we never rely on the
     * database to set field values.
     */
    def addColumn(String table, String column, String type, String defaultValue, String constraint) {
        logger.info("Adding column '$column' with type '$type' to table '$table'")

        def defaultValueClause = defaultValue != null ? "DEFAULT $defaultValue" : ""
        def constraintClause = constraint != null ? constraint : ""
        executeQuery("ALTER TABLE $table ADD $column $type $defaultValueClause $constraintClause")

        // in this case, sqlserver sets the constraint but lets the column with a null value, so set the value by hand
        if (dbVendor == SQLSERVER && defaultValue != null && constraint == null) {
            executeUpdateQuery("UPDATE $table SET $column = $defaultValue")
        }

        dropColumnDefaultValueIfExists(table, column)
    }

    void dropColumnDefaultValueIfExists(String table, String column) {
        logger.info("Dropping default value on column '$column' from table '$table'")
        switch (dbVendor) {
            case ORACLE:
                executeQuery("ALTER TABLE $table MODIFY $column DEFAULT NULL")
                break
            case SQLSERVER:
                def defaultConstraintName = getSqlServerDefaultValueConstraintName(table, column)
                if (defaultConstraintName != null) {
                    executeQuery("ALTER TABLE $table DROP CONSTRAINT $defaultConstraintName")
                }
                break
            case MYSQL:
                String defaultColumnValue = getMysqlColumnDefaultValue(table, column)
                if (defaultColumnValue != null) {
                    executeQuery("ALTER TABLE $table ALTER COLUMN $column DROP DEFAULT")
                }
                break
            default:
                executeQuery("ALTER TABLE $table ALTER COLUMN $column DROP DEFAULT")
        }
    }

    private String getSqlServerDefaultValueConstraintName(String table, String column) {
        return selectFirstRow("""
            SELECT name
            FROM SYS.DEFAULT_CONSTRAINTS
            WHERE PARENT_OBJECT_ID = OBJECT_ID($table)
              AND PARENT_COLUMN_ID = (
                SELECT column_id
                FROM sys.columns
                WHERE NAME = $column
                  AND object_id = OBJECT_ID($table)
              )
            """)?.get('name')
    }

    private String getMysqlColumnDefaultValue(String table, String column) {
        return selectFirstRow("""
            SELECT column_default
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE table_name = $table
              AND column_name = $column
              AND column_default IS NOT NULL
            """)?.get('column_default')
    }

    def dropForeignKey(String table, String foreignKeyName) {
        if (!hasForeignKeyOnTable(table, foreignKeyName)) {
            logger.warn("Foreign key '$foreignKeyName' does not exist on table '$table'")
            return
        }

        logger.info("Dropping foreign key '$foreignKeyName' from table '$table'")
        if (dbVendor == MYSQL) {
            executeQuery("ALTER TABLE $table DROP FOREIGN KEY $foreignKeyName")
            // In the case of Mysql, an index is automatically created with the same name as the foreign key:
            dropIndexIfExists(table, foreignKeyName)
        } else {
            executeQuery("ALTER TABLE $table DROP CONSTRAINT $foreignKeyName")
        }
    }

    /**
     * Drop all foreign keys found on table
     */
    def dropAllForeignKeys(String tableName) {
        logger.info("Dropping all foreign keys from table '$tableName'")
        def query = getScriptContent("/database/allForeignKeys", "foreignKey")
        sql.eachRow(query, [tableName]) { row ->
            dropForeignKey(row.table_name, row.constraint_name)
        }
    }

    def createForeignKey(String referencingTableName, String foreignKeyName, String referencedTableName,
            List<String> referencingColumns, List<String> referencedColumns, boolean onDeleteCascade) {
        logger.info("Creating foreign key '$foreignKeyName' from table '$referencingTableName' to table '$referencedTableName'")
        def referencingCols = referencingColumns.collect { it }.join(", ")
        def referencedCols = referencedColumns.collect { it }.join(", ")
        executeQuery("""
            ALTER TABLE $referencingTableName ADD CONSTRAINT $foreignKeyName FOREIGN KEY ($referencingCols)
              REFERENCES $referencedTableName ($referencedCols) ${onDeleteCascade ? "ON DELETE CASCADE" : ""}
            """)
    }

    def dropPrimaryKey(String tableName) {
        logger.info("Dropping primary key on table '$tableName'")
        def query = getScriptContent("/database/primaryKey", "primaryKey")
        sql.eachRow(query, [tableName]) { row ->
            if (dbVendor == MYSQL) {
                executeQuery("ALTER TABLE ${row.TABLE_NAME} DROP PRIMARY KEY")
            } else {
                executeQuery("ALTER TABLE ${row.TABLE_NAME} DROP CONSTRAINT ${row.CONSTRAINT_NAME}")
            }
        }
    }

    /**
     * By convention, the primary key created is named `pk_$tableName`
     */
    def createPrimaryKey(String tableName, String... columns) {
        createPrimaryKeyWithName(tableName, "pk_$tableName", columns)
    }

    def createPrimaryKeyWithName(String tableName, String pkName, String... columns) {
        logger.info("Creating primary key '$pkName' on table '$tableName'")
        def concatenatedColumns = columns.collect { it }.join(", ")
        executeQuery("ALTER TABLE $tableName ADD CONSTRAINT $pkName PRIMARY KEY ($concatenatedColumns)")
    }

    def recreatePrimaryKey(String tableName, String... columns = ["id"]) {
        dropPrimaryKey(tableName)
        createPrimaryKey(tableName, columns)
    }

    /**
     * remove unique constraint on table.
     * specific to oracle:
     *  in case index has been modified after constraint creation
     *  such as tablespace rebuild, table import export
     *  add drop of index
     */
    def dropUniqueKey(String tableName, String ukName) {
        if (hasUniqueKeyOnTable(tableName, ukName)) {
            doDropExistingUniqueKey(tableName, ukName)
        } else {
            logger.warn("Unique key '$ukName' not found on table '$tableName'")
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
            logger.warn("Unique key not found on table '$tableName' in name list $ukNameList")
        }
    }

    private doDropExistingUniqueKey(String tableName, String ukName) {
        logger.info("Dropping unique contraint '$ukName' on table '$tableName'")
        switch (dbVendor) {
            case ORACLE:
                executeQuery("ALTER TABLE $tableName DROP CONSTRAINT $ukName")
                if (hasIndexOnTable(tableName, ukName)) {
                    executeQuery("DROP INDEX $ukName")
                }
                break
            case MYSQL:
                executeQuery("ALTER TABLE $tableName DROP INDEX $ukName")
                break
            default:
                executeQuery("ALTER TABLE $tableName DROP CONSTRAINT $ukName")
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
            logger.warn("No unique key found on table '$tableName' for columns $columns")
        }
    }

    def createUniqueKey(String tableName, String constraintName, String... columns) {
        logger.info("Creating unique constraint '$constraintName' on table '$tableName'")
        def concatenatedColumns = columns.collect { it }.join(", ")
        executeQuery("ALTER TABLE $tableName ADD CONSTRAINT $constraintName UNIQUE ($concatenatedColumns)")
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
        def result = selectFirstRow(query, [tableName, columns.size()])
        return result == null || result.isEmpty() ? null : result[0] as String
    }

    String getUniqueKeyNameOnTable(String tableName) {
        def query = getScriptContent("/database/uniqueUnnamedKey", "uniqueKey")
        def result = selectFirstRow(query, [tableName])
        return result == null || result.isEmpty() ? null : result[0] as String
    }

    /**
     * Remove existing index if already exists and create new index
     */
    def addOrReplaceIndex(String tableName, String indexName, String... columns) {
        dropIndexIfExists(tableName, indexName)
        createIndex(tableName, indexName, columns)
    }

    /**
     * Create new index if not already exists
     */
    def addIndexIfMissing(String tableName, String indexName, String... columns) {
        if (hasIndexOnTable(tableName, indexName)) {
            logger.info "Index '$indexName' already exists on table '$tableName'. Skipping creation."
        } else {
            createIndex(tableName, indexName, columns)
        }
    }

    def createIndex(String tableName, String indexName, boolean unique = false, String... columns) {
        logger.info("Creating ${unique ? "unique " : ""}index '$indexName' on table '$tableName'")
        def concatenatedColumns = columns.collect { it }.join(", ")
        executeQuery("CREATE ${unique ? "UNIQUE " : ""}INDEX $indexName ON $tableName($concatenatedColumns)")
    }

    void renameIndex(String tableName, String oldName, String newName) {
        logger.info("Renaming index '$oldName' to '$newName' on table '$tableName'")
        switch (dbVendor) {
            case POSTGRES:
            case ORACLE:
                executeQuery("ALTER INDEX $oldName RENAME TO $newName")
                break
            case MYSQL:
                executeQuery("ALTER TABLE $tableName RENAME INDEX $oldName TO $newName")
                break
            case SQLSERVER:
                executeQuery("""
                    BEGIN
                    EXEC sp_rename N'${tableName}.${oldName}', N'${newName}', N'INDEX'
                    END
                    """)
                break
        }
    }

    /**
     * remove index if exists
     */
    def dropIndexIfExists(String tableName, String indexName) {
        if (!hasIndexOnTable(tableName, indexName)) {
            logger.info "Index '$indexName' does not exist on table '$tableName'. Skipping deletion."
            return
        }
        logger.info("Dropping index '$indexName' on table '$tableName'")
        switch (dbVendor) {
            case POSTGRES:
            case ORACLE:
                executeQuery("DROP INDEX $indexName")
                break
            case MYSQL:
                executeQuery("DROP INDEX $indexName ON $tableName")
                break
            case SQLSERVER:
                executeQuery("DROP INDEX $tableName.$indexName")
                break
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
        GroovyRowResult result
        switch (dbVendor) {
            case POSTGRES:
                String query = """
                    SELECT LOWER(indexname)
                    FROM pg_indexes
                    WHERE LOWER(tablename) = LOWER(?)
                      AND LOWER(indexdef) LIKE LOWER(?)
                    """
                def concatenatedColumns = columnNames.collect { it }.join(", ")
                def indexdef = "CREATE ${unique ? "UNIQUE " : ""}INDEX%($concatenatedColumns)%" as String
                result = selectFirstRow(query, [tableName, indexdef])
                break

            case MYSQL:
                String query = """
                    SELECT LOWER(INDEX_NAME)
                    FROM INFORMATION_SCHEMA.STATISTICS
                    WHERE NON_UNIQUE = ${unique ? "0" : "1"}
                      AND TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND LOWER(COLUMN_NAME) IN (${columnNames.collect { "?" }.join(",")})
                    GROUP BY INDEX_NAME
                    HAVING MAX(SEQ_IN_INDEX) = ? AND COUNT(DISTINCT COLUMN_NAME) = ?
                    """
                List<Object> params = [tableName]
                params.addAll(columnNames)
                params.add(columnNames.size())
                params.add(columnNames.size())
                result = selectFirstRow(query, params)
                break

            case SQLSERVER:
                String query = """
                    SELECT LOWER(i.name) AS index_name
                    FROM sys.indexes i
                    JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
                    JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
                    WHERE LOWER(i.object_id) = OBJECT_ID(?)
                      AND LOWER(c.name) IN (${columnNames.collect { "?" }.join(",")})
                      AND i.is_unique = ${unique ? "1" : "0"}
                    GROUP BY i.name
                    HAVING COUNT(DISTINCT c.name) = ? and MAX(index_column_id) = ?
                    """
                List<Object> params = [tableName]
                params.addAll(columnNames)
                params.add(columnNames.size())
                params.add(columnNames.size())
                result = selectFirstRow(query, params)
                break

            case ORACLE:
                String query = """
                    SELECT LOWER(i.index_name)
                    FROM all_indexes i
                    JOIN all_ind_columns ic ON i.index_name = ic.index_name AND i.table_name = ic.table_name
                    WHERE LOWER(i.table_name) = ?
                      AND LOWER(ic.column_name) IN (${columnNames.collect { "?" }.join(",")})
                      AND i.uniqueness = '${unique ? "UNIQUE" : "NONUNIQUE"}'
                    GROUP BY i.index_name
                    HAVING COUNT(DISTINCT ic.column_name) = ? AND MAX(column_position) = ?
                    """
                List<Object> params = [tableName]
                params.addAll(columnNames)
                params.add(columnNames.size())
                params.add(columnNames.size())
                result = selectFirstRow(query, params)
                break
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
        return fkReferences
    }

    /**
     * checks if given foreign key exists on table
     * @param tableName name of the table
     * @param foreignKeyName name of the foreign key
     * @return true if exists, false otherwise
     */
    boolean hasForeignKeyOnTable(String tableName, String foreignKeyName) {
        def query = getScriptContent("/database/foreignKey", "foreignKey")
        return selectFirstRow(query, [tableName, foreignKeyName]) != null
    }

    /**
     * checks if primary key exists on table
     * @param tableName name of the table
     * @param pkName name of the primary key
     * @return true if exists, false otherwise
     */
    boolean hasPrimaryKeyOnTable(String tableName, String pkName) {
        def primaryKey = getPrimaryKey(tableName)
        if (dbVendor == MYSQL) {
            // because MySQL does not store the PK name
            return primaryKey != null
        } else {
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
        return selectFirstRow(query, [tableName, ukName]) != null
    }

    /**
     * Checks if a unique key exists on a table with the given columns
     * @param tableName table name where the unique key should be defined
     * @param columns columns of the unique key
     * @return true if exists, false otherwise
     */
    boolean hasUniqueKeyOnTableWithColumns(String tableName, String... columns) {
        return getUniqueKeyByColumns(tableName, columns) != null
    }

    /**
     * Checks if a unique key exists on a table with the given name and columns
     * @param tableName table name where the unique key should be defined
     * @param ukName unique key name
     * @param columns columns of the unique key
     * @return true if exists, false otherwise
     */
    boolean hasUniqueKeyOnTableWithNameAndColumns(String tableName, String ukName, String... columns) {
        return hasUniqueKeyOnTable(tableName, ukName) && hasUniqueKeyOnTableWithColumns(tableName, columns)
    }

    /**
     * get primary key name
     * @param tableName
     * @return pk name if exists, null otherwise
     */
    String getPrimaryKey(String tableName) {
        def query = getScriptContent("/database/primaryKey", "primaryKey")
        def firstRow = selectFirstRow(query, [tableName])
        return firstRow != null ? firstRow.CONSTRAINT_NAME : null
    }

    /**
     * checks if given index exists on table
     * @param tableName
     * @param indexName
     * @return true if exists, false otherwise
     */
    boolean hasIndexOnTable(String tableName, String indexName) {
        String query
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
                    WHERE pg_class.oid = pg_index.indrelid
                      AND pg2.oid = pg_index.indexrelid
                      AND UPPER(pg_class.relname) = UPPER(?)
                      AND UPPER(pg2.relname) = UPPER(?)
                    """
                break

            case ORACLE:
                query = """
                    SELECT i.TABLE_NAME, i.INDEX_NAME
                    FROM USER_INDEXES i
                    WHERE LOWER(i.TABLE_NAME) = LOWER(?)
                      AND LOWER(i.index_name) = LOWER(?)
                    """
                break

            case MYSQL:
                query = """
                    SELECT DISTINCT s.TABLE_NAME, s.INDEX_NAME
                    FROM INFORMATION_SCHEMA.STATISTICS s
                    WHERE s.TABLE_SCHEMA = DATABASE()
                      AND UPPER(s.table_name) = UPPER(?)
                      AND UPPER(s.index_name) = UPPER(?)
                    """
                break

            case SQLSERVER:
                query = """
                    SELECT t.name, i.name
                    FROM sys.tables t
                    JOIN sys.indexes i ON i.object_id = t.object_id
                    WHERE UPPER(t.name) = UPPER(?)
                      AND UPPER(i.name) = UPPER(?)
                    """
                break
        }

        return selectFirstRow(query, [tableName, indexName]) != null
    }

    String getColumnType(String tableName, String columnName) {
        String query
        switch (dbVendor) {
            case POSTGRES:
            case SQLSERVER:
                query = """
                    SELECT C.DATA_TYPE
                    FROM INFORMATION_SCHEMA.COLUMNS C
                    WHERE UPPER( C.TABLE_NAME ) = UPPER( ? )
                      AND UPPER( C.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case ORACLE:
                query = """
                    SELECT c.DATA_TYPE
                    FROM user_tab_cols c
                    WHERE UPPER( c.TABLE_NAME ) = UPPER( ? )
                      AND UPPER( c.COLUMN_NAME ) = UPPER( ? )
                    """
                break

            case MYSQL:
                query = """
                    SELECT c.DATA_TYPE
                    FROM INFORMATION_SCHEMA.COLUMNS c
                    WHERE c.TABLE_SCHEMA = DATABASE()
                      AND UPPER( c.TABLE_NAME ) = UPPER( ? )
                      AND UPPER( c.COLUMN_NAME ) = UPPER( ? )
                    """
                break
        }

        def firstRow = selectFirstRow(query, [tableName, columnName])
        return firstRow != null ? firstRow.DATA_TYPE : null
    }

    long getAndUpdateNextSequenceId(long sequenceId, long tenantId) {
        long nextId = (Long) selectFirstRow("SELECT nextId FROM sequence WHERE id = $sequenceId AND tenantId = $tenantId").get("nextId")
        executeUpdateQuery("UPDATE sequence SET nextId = ${nextId + 1} WHERE tenantId = $tenantId AND id = $sequenceId")
        return nextId
    }

    /**
     * get a script from the resources and execute it
     *
     * the script should be located in the src/main/resources/version/to_<version>/<dbvendor>_<scriptName>.sql
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
        return scriptContent
    }

    private GString getVersionFolder(String version) {
        return "/version/to_${version.replace('.', '_')}"
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

    boolean readBoolean(Object valueFromDb){
        return dbVendor == ORACLE || dbVendor == SQLSERVER ? (valueFromDb == "1") : valueFromDb as boolean
    }

    def addSequenceOnAllTenants(int sequenceKey) {
        getAllTenants().each { tenant ->
            executeInsertQuery("INSERT INTO sequence (tenantid, id, nextid) VALUES(${tenant.id}, $sequenceKey, 1)")
        }
    }

    def getSequenceValue(def tenantId, def sequenceId) {
        selectFirstRow("""
            SELECT s.tenantid, s.id, s.nextid
            FROM sequence s
            WHERE s.tenantid = $tenantId AND s.id = $sequenceId
            """)
    }

    def getAllTenants() {
        sql.rows("SELECT id, name, status FROM tenant ORDER BY id")
    }

    def insertSequences(Map<Long, Long> resourcesCount, Integer sequenceId) {
        if (resourcesCount.isEmpty()) {
            throw new IllegalStateException("There is no tenant on which to insert the sequences")
        }
        resourcesCount.each { it ->
            executeInsertQuery("INSERT INTO sequence VALUES(${it.getKey()}, ${sequenceId}, ${it.getValue()})")
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
        switch (dbVendor) {
            case MYSQL:
            case POSTGRES:
                return "$query LIMIT $limit"
            case ORACLE:
                return "SELECT * FROM ( $query ) WHERE ROWNUM <= $limit"
            case SQLSERVER:
            // insert top right after 'select '
                return "SELECT TOP $limit ${query.substring('select '.length())}"
            default:
                return query
        }
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
