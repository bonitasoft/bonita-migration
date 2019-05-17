package org.bonitasoft.migration.core.database

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

import spock.lang.Specification
import spock.lang.Unroll

class DatabaseHelperTest extends Specification {

    @Unroll
    "should build limit select query for vendor #vendor"() {
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
