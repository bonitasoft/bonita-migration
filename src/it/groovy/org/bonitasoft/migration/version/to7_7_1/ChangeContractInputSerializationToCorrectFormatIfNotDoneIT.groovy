package org.bonitasoft.migration.version.to7_7_1

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

/**
 * @author Danila Mazour
 */
class ChangeContractInputSerializationToCorrectFormatIfNotDoneIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def migrationStep = new ChangeContractInputSerializationOnMysqlToCorrectFormatIfNotDone()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_7_1/contract_input")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["contract_data", "arch_contract_data"] as String[])
    }

    @Unroll
    def "should have changed the type of the contract data"() {
        given:
        String content = "myValue"

        migrationContext.sql.execute("""
insert into contract_data(tenantId , id, kind, scopeId, name, val)
values (1, 1, 'PROCESS', 1000, 'myInput', $content)
""")
        migrationContext.sql.execute("""
INSERT INTO contract_data(tenantId , id, kind, scopeId, name, val)
VALUES (1, 2, 'PROCESS', 1001, 'myInputNull', NULL)
""")
        migrationContext.sql.execute("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val)
values (1, 1, 1, 1, 'PROCESS', 1000, 'myInput', $content)
""")
        when:
        migrationStep.execute(migrationContext)

        then: 'contract data is migrated'
        def contract_data = migrationContext.sql.firstRow("SELECT * FROM contract_data WHERE name='myInput'")
        getMigratedValue(contract_data.val) == "myValue"

        then: 'contract data with NULL value is migrated'
        def null_contract_data = migrationContext.sql.firstRow("SELECT * FROM contract_data WHERE name='myInputNull'")
        getMigratedValue(null_contract_data.val) == null

        and: 'archived contract data is migrated'
        def arch_contract_data = migrationContext.sql.firstRow("SELECT * FROM  arch_contract_data")
        getMigratedValue(arch_contract_data.val) == "myValue"

        and: 'Verify type has changed'
        switch (migrationContext.dbVendor) {
            case MYSQL:
                def table_type = migrationContext.sql.firstRow("SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'contract_data' AND COLUMN_NAME = 'val'")
                table_type.containsValue("LONGTEXT")
                break
            default:
                break
        }
    }

    private String getMigratedValue(def value) {
        def migratedValue = value
        if (value != null && ORACLE.equals(migrationContext.dbVendor)) {
            migratedValue = migrationContext.databaseHelper.getClobContent(value)
        }
        migratedValue
    }
}
