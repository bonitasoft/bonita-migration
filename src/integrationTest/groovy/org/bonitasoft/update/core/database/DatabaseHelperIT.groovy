/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Requires
import spock.lang.Specification

class DatabaseHelperIT extends Specification {

    private static final String TABLE_NAME = 'table_origin'

    def spiedLogger = Spy(Logger.class)
    UpdateContext updateContext = new UpdateContext(logger: spiedLogger)
    DBUnitHelper dbUnitHelper = new DBUnitHelper(updateContext)

    def setup() {
        dropTestTables()
        String folder = 'core/column_default_value'
        updateContext.logger.info("Create tables from sql file in $folder")
        dbUnitHelper.executeScript(DBUnitHelper.class.getClassLoader().getResource("sql/$folder/${updateContext.dbVendor.name().toLowerCase()}.sql"))
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables([TABLE_NAME] as String[])
    }

    def "should remove default value after adding column on mandatory table"() {
        given:
        DatabaseHelper databaseHelper = updateContext.databaseHelper
        databaseHelper.executeQuery("INSERT INTO $TABLE_NAME(tenantid, id) VALUES(1, 1)")
        assert countRows() == 1

        databaseHelper.addColumnIfNotExist(TABLE_NAME, "version", "VARCHAR(10)", "'1'", "NOT NULL")
        databaseHelper.addColumnIfNotExist(TABLE_NAME, "name", "VARCHAR(30)", "'unknown'", "NOT NULL")

        // ensure default values have been set
        GroovyRowResult result = databaseHelper.selectFirstRow("SELECT version, name FROM $TABLE_NAME" as String)
        assert result.get('version') == '1'
        assert result.get('name') == 'unknown'


        when:
        // should fail as we do not pass mandatory column values
        databaseHelper.executeQuery("INSERT INTO $TABLE_NAME(tenantid, id) VALUES(1, 2)")

        then:
        thrown(Exception)
        // row not added
        assert countRows() == 1
    }

    private int countRows() {
        updateContext.sql.firstRow("SELECT count(*) AS cpt FROM $TABLE_NAME" as String).get("cpt") as int
    }

    def "should drop index before adding it if already exists"() {
        given:
        DatabaseHelper databaseHelper = updateContext.databaseHelper
        databaseHelper.executeQuery("CREATE INDEX idx_test ON $TABLE_NAME (id)")

        when:
        databaseHelper.addOrReplaceIndex(TABLE_NAME, 'idx_test', 'id')

        then:
        1 * spiedLogger.info({
            it == "Dropping index 'idx_test' on table '$TABLE_NAME'"
        })
    }

    @Requires({ System.getProperty("db.vendor", "postgres") == "postgres" })
    def "should find table, column, constraints and index reachable only via a non-first search_path entry"() {
        given: "a table with constraints and an index that exists only in a custom schema"
        def schema = "bpa557_test_schema"
        def table = "bpa557_test_table"
        def decoy = "bpa557_test_decoy"
        def sql = updateContext.sql
        sql.execute("DROP SCHEMA IF EXISTS ${schema} CASCADE" as String)
        sql.execute("DROP TABLE IF EXISTS public.${decoy}" as String)
        sql.execute("CREATE SCHEMA ${schema}" as String)
        sql.execute("""
            CREATE TABLE ${schema}.${table} (
                id BIGINT, parent_id BIGINT, name VARCHAR(50), code VARCHAR(50),
                CONSTRAINT pk_bpa557 PRIMARY KEY (id),
                CONSTRAINT uk_bpa557 UNIQUE (name, code),
                CONSTRAINT fk_bpa557 FOREIGN KEY (parent_id) REFERENCES ${schema}.${table} (id))
            """ as String)
        sql.execute("CREATE INDEX idx_bpa557 ON ${schema}.${table} (name)" as String)

        and: "a decoy table in public carrying the same constraint names, as left behind by a copy of a Bonita schema"
        sql.execute("""
            CREATE TABLE public.${decoy} (
                id BIGINT, parent_id BIGINT, name VARCHAR(50), code VARCHAR(50),
                CONSTRAINT pk_bpa557 PRIMARY KEY (id),
                CONSTRAINT uk_bpa557 UNIQUE (name, code),
                CONSTRAINT fk_bpa557 FOREIGN KEY (parent_id) REFERENCES public.${decoy} (id))
            """ as String)

        and: "a context whose every pooled connection resolves search_path = public, <schema>"
        def customSchemaContext = new UpdateContext(logger: new Logger())
        customSchemaContext.start()
        customSchemaContext.loadConfiguration()
        def url = customSchemaContext.dbConfig.dburl
        customSchemaContext.dbConfig.dburl = url + (url.contains('?') ? '&' : '?') + "currentSchema=public,${schema}"
        customSchemaContext.openSqlConnection()
        def helper = customSchemaContext.databaseHelper

        expect: "table and column lookups resolve through the whole search_path"
        helper.hasTable(table)
        helper.hasColumnOnTable(table, "name")
        !helper.hasColumnOnTable(table, "nonexistent")
        helper.getColumnType(table, "name") == "character varying"

        and: "constraint lookups are scoped to the same schema on both sides of their joins"
        helper.hasPrimaryKeyOnTable(table, "pk_bpa557")
        helper.hasUniqueKeyOnTableWithColumns(table, "name", "code")
        helper.hasForeignKeyOnTable(table, "fk_bpa557")
        helper.getForeignKeyReferences(table)*.foreignKeyName == ["fk_bpa557"]

        and: "index lookups are scoped as well"
        helper.hasIndexOnTable(table, "idx_bpa557")
        helper.getIndexDefinition(table, false, "name")?.indexName == "idx_bpa557"

        cleanup:
        customSchemaContext?.closeSqlConnection()
        sql.execute("DROP SCHEMA IF EXISTS ${schema} CASCADE" as String)
        sql.execute("DROP TABLE IF EXISTS public.${decoy}" as String)
    }
}
