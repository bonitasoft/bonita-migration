/*
 * Copyright (C) 2018 BonitaSoft S.A.
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
 */

package org.bonitasoft.migration.core.database

import groovy.sql.GroovyRowResult
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

class DatabaseHelperIT extends Specification {

    private static final TABLE_NAME = 'table_origin'

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        dropTestTables()
        String folder = 'core/column_default_value'
        logger.info("Create tables from sql file in $folder")
        dbUnitHelper.executeScript(DBUnitHelper.class.getClassLoader().getResource("sql/$folder/${migrationContext.dbVendor.name().toLowerCase()}.sql"))
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables([TABLE_NAME] as String[])
    }

    def "should remove default value after adding column on mandatory table"() {
        given:
        DatabaseHelper databaseHelper = migrationContext.databaseHelper
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
        migrationContext.sql.firstRow("SELECT count(*) AS cpt FROM $TABLE_NAME" as String).get("cpt") as int
    }

}
