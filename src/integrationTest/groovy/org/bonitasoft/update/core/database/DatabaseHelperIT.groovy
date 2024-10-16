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
import spock.lang.Specification

class DatabaseHelperIT extends Specification {

    private static final TABLE_NAME = 'table_origin'

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
        databaseHelper.execute("INSERT INTO $TABLE_NAME(tenantid, id) VALUES(1, 1)")
        assert countRows() == 1

        databaseHelper.addColumnIfNotExist(TABLE_NAME, "version", "VARCHAR(10)", "'1'", "NOT NULL")
        databaseHelper.addColumnIfNotExist(TABLE_NAME, "name", "VARCHAR(30)", "'unknown'", "NOT NULL")

        // ensure default values have been set
        GroovyRowResult result = databaseHelper.selectFirstRow("SELECT version, name FROM $TABLE_NAME")
        assert result.get('version') == '1'
        assert result.get('name') == 'unknown'


        when:
        // should fail as we do not pass mandatory column values
        databaseHelper.execute("INSERT INTO $TABLE_NAME(tenantid, id) VALUES(1, 2)")

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
        databaseHelper.execute("CREATE INDEX idx_test ON $TABLE_NAME (id)")

        when:
        databaseHelper.addOrReplaceIndex(TABLE_NAME, 'idx_test', 'id')

        then:
        1 * spiedLogger.info({
            // written like this as the query is not formatted the same on all DB vendors:
            it.contains('DROP INDEX') && it.contains('idx_test')
        })
    }
}
