package org.bonitasoft.migration.version.to7_7_1

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.MYSQL
import static org.junit.Assume.assumeTrue

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class ChangeContractInputSerializationOnMysqlToCorrectFormatIfNotDoneIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    ChangeContractInputSerializationOnMysqlToCorrectFormatIfNotDone migrationStep = Spy(ChangeContractInputSerializationOnMysqlToCorrectFormatIfNotDone)

    def setup() {
        dropTestTables()
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["contract_data", "arch_contract_data"] as String[])
    }

    def "should have changed the type of the contract data on mysql only from MEDIUMTEXT to LONGTEXT"() {
        assumeTrue(migrationContext.dbVendor == MYSQL)

        given:
        dbUnitHelper.createTables("7_7_1/contract_input")

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

        then: 'contract data with String value is accessible'
        def contract_data = migrationContext.sql.firstRow("SELECT * FROM contract_data WHERE name='myInput'")
        contract_data.val == "myValue"

        and: 'contract data with NULL value is accessible'
        def null_contract_data = migrationContext.sql.firstRow("SELECT * FROM contract_data WHERE name='myInputNull'")
        null_contract_data.val == null

        and: 'archived contract data with String value is accessible'
        def arch_contract_data = migrationContext.sql.firstRow("SELECT * FROM  arch_contract_data")
        arch_contract_data.val == "myValue"

        and: 'MySql vendor: the type of the val column in the contract_data table has changed'
        def contract_data_types = migrationContext.sql.firstRow("SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'contract_data' AND COLUMN_NAME = 'val'")
        ((String)contract_data_types.get('DATA_TYPE')).equalsIgnoreCase('LONGTEXT')

        and: 'MySql vendor: the type of the val column in the arch_contract_data table has changed'
        def arch_contract_data_val_type = migrationContext.sql.firstRow("SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'arch_contract_data' AND COLUMN_NAME = 'val'")
        ((String)arch_contract_data_val_type.get('DATA_TYPE')).equalsIgnoreCase('LONGTEXT')
    }

    def "should have not changed the type of the contract data on mysql when is already LONGTEXT"() {
        given:
        dbUnitHelper.createTables("7_7_1/contract_input_already_migrated")

        String content = "myValue"
        migrationContext.sql.execute("""
insert into contract_data(tenantId , id, kind, scopeId, name, val)
values (1, 1, 'PROCESS', 1000, 'myInput', $content)
""")
        migrationContext.sql.execute("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val)
values (1, 1, 1, 1, 'PROCESS', 1000, 'myInput', $content)
""")

        when:
        migrationStep.execute(migrationContext)

        then:
        0 * migrationStep.updateValColumnTypeOnTable(_ as MigrationContext, _ as String)
    }

}