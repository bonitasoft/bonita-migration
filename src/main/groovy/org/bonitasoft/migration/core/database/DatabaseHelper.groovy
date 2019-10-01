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
import groovy.transform.PackageScope
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationStep.DBVendor
import org.bonitasoft.migration.core.database.schema.ColumnDefinition
import org.bonitasoft.migration.core.database.schema.ForeignKeyDefinition
import org.bonitasoft.migration.core.database.schema.IndexDefinition

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

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


    String concat(String... argsToConcat){
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

    def dropColumn(String table, String column) {
        if (hasColumnOnTable(table, column)) {
            switch (dbVendor) {
                case ORACLE:
                    execute("ALTER TABLE $table DROP COLUMN $column")
                    break;
                case SQLSERVER:
                    execute("ALTER TABLE $table DROP COLUMN $column")
                    break;
                default:
                    execute("ALTER TABLE $table DROP $column")
            }
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
        sql.execute("""ALTER TABLE $table ADD $column $type ${defaultValue != null ? "DEFAULT $defaultValue" : ""} ${constraint != null ? constraint : ""}""" as String)
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
            AND PARENT_COLUMN_ID = (SELECT column_id FROM sys.columns
                                    WHERE NAME = N'$column'
                                    AND object_id = OBJECT_ID(N'$table'))
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
            logger.info "foreign key ${foreignKeyName} not found on table ${table}"
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

    def dropPrimaryKey(String tableName) {
        def query = getScriptContent("/database/primaryKey", "primaryKey")
        sql.eachRow(query, [tableName]) { row ->
            def request
            switch (dbVendor) {
                case MYSQL:
                    request = "ALTER TABLE " + row.TABLE_NAME + " DROP PRIMARY KEY"
                    break
                default:
                    request = "ALTER TABLE " + row.TABLE_NAME + " DROP CONSTRAINT " + row.CONSTRAINT_NAME
            }
            logger.info row as String
            logger.info "Executing request: $request"
            sql.execute(request)
        }

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
            switch (dbVendor) {
                case POSTGRES:
                case SQLSERVER:
                    sql.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT " + ukName)
                    break
                case ORACLE:
                    sql.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT " + ukName)
                    if (hasIndexOnTable(tableName, ukName)) {
                        sql.execute("DROP INDEX " + ukName)
                    }
                    break
                case MYSQL:
                    sql.execute("ALTER TABLE " + tableName + " DROP INDEX " + ukName)
            }

        }
    }

    String getUniqueKeyNameOnTable(String tableName) {
        def query = getScriptContent("/database/uniqueUnnamedKey", "uniqueKey")
        def firstRow = sql.firstRow(query, [tableName])
        if (firstRow != null) {
            return firstRow.CONSTRAINT_NAME
        }
    }

    /**
     * remove existing index if already exists and create new index
     * @param tableName
     * @param indexName
     * @param columns
     * @return create index SQl statement
     */
    String addOrReplaceIndex(String tableName, String indexName, String... columns) {
        dropIndexIfExists(tableName, indexName)

        def concatenatedColumns = columns.collect { it }.join(", ")
        String request = "CREATE INDEX $indexName ON $tableName ($concatenatedColumns)"
        logger.info "Executing request: $request"
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
            String query
            switch (dbVendor) {
                case POSTGRES:
                case ORACLE:
                    query = "DROP INDEX " + indexName
                    break
                case MYSQL:
                    query = "DROP INDEX " + indexName + " on " + tableName
                    break
                case SQLSERVER:
                    query = "DROP INDEX " + tableName + "." + indexName
                    break
            }
            logger.info "Executing request: $query"
            sql.execute(query)
        }
    }

    /**
     * retrieve index definition for a given table
     * @param tableName
     * @param indexName
     * @return
     */
    IndexDefinition getIndexDefinition(String tableName, String indexName) {
        def query = getScriptContent("/database/indexDefinition", "indexDefinition")
        def indexDefinition = new IndexDefinition(tableName, indexName)
        sql.eachRow(query, [tableName, indexName]) {
            indexDefinition.addColumn(new ColumnDefinition(it["column_name"], it["column_order"]))
        }
        indexDefinition
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
        primaryKey != null && primaryKey == pkName

    }

    /**
     * checks if unique key exists on table
     * @param tableName name of the table
     * @param ukName name of the unique key
     * @return true if exists, false otherwise
     */
    boolean hasUniqueKeyOnTable(String tableName, String ukName) {
        def query = getScriptContent("/database/uniqueKey", "uniqueKey")
        def firstRow = sql.firstRow(query, [tableName, ukName])
        return firstRow != null

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
        logger.info "execute script: $sqlFile"
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

    def insertSequences(Map<Long, Long> resourcesCount, context, Integer sequenceId) {
        if (resourcesCount.isEmpty())
            throw new IllegalStateException("There is no tenants on which insert the sequences")
        return resourcesCount.each { it ->
            context.sql.executeInsert("INSERT INTO sequence VALUES(${it.getKey()}, ${sequenceId}, ${it.getValue()})")
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

}
