/**
 * Copyright (C) 2024 Bonitasoft S.A.
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

import groovy.sql.Sql
import org.bonitasoft.update.core.Logger
import spock.lang.Specification
import spock.lang.Unroll

import static org.bonitasoft.update.core.UpdateStep.DBVendor.*

class DatabaseHelperTest extends Specification {

    @Unroll
    def "should build hasTable query for vendor #vendor"() {
        given:
        DatabaseHelper databaseHelper = new DatabaseHelper()
        databaseHelper.dbVendor = vendor
        databaseHelper.sql = Mock(Sql)
        databaseHelper.logger = Mock(Logger)

        when:
        databaseHelper.hasTable("my_table")

        then:
        1 * databaseHelper.sql.firstRow({ it.toString().contains(expectedFragment) }) >> null

        where:
        vendor    || expectedFragment
        POSTGRES  || "current_schemas(false)"
        ORACLE    || "user_tables"
        MYSQL     || "table_schema = DATABASE()"
        SQLSERVER || "information_schema.tables"
    }

    @Unroll
    def "should build hasColumnOnTable query for vendor #vendor"() {
        given:
        DatabaseHelper databaseHelper = new DatabaseHelper()
        databaseHelper.dbVendor = vendor
        databaseHelper.sql = Mock(Sql)
        databaseHelper.logger = Mock(Logger)

        when:
        databaseHelper.hasColumnOnTable("platform", "maintenance_enabled")

        then:
        1 * databaseHelper.sql.firstRow({ it.toString().contains(expectedFragment) }, _) >> null

        where:
        vendor    || expectedFragment
        POSTGRES  || "current_schemas(false)"
        ORACLE    || "user_tab_cols"
        MYSQL     || "TABLE_SCHEMA = DATABASE()"
        SQLSERVER || "INFORMATION_SCHEMA.COLUMNS"
    }

    @Unroll
    def "should build limit select query for vendor #vendor"() {
        given:
        DatabaseHelper databaseHelper = new DatabaseHelper()
        databaseHelper.dbVendor = vendor
        String query = "Select id from arch_contract_data"
        when:
        String limitQuery = databaseHelper.buildLimitSelectQuery(query, 300)
        then:
        limitQuery == expectedLimitQuery
        where:
        vendor    || expectedLimitQuery
        MYSQL     || "Select id from arch_contract_data LIMIT 300"
        ORACLE    || "SELECT * FROM ( Select id from arch_contract_data ) WHERE ROWNUM <= 300"
        POSTGRES  || "Select id from arch_contract_data LIMIT 300"
        SQLSERVER || "SELECT TOP 300 id from arch_contract_data"
    }
}
