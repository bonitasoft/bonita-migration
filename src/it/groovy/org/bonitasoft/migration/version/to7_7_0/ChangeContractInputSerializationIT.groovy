package org.bonitasoft.migration.version.to7_7_0

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.ORACLE

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ChangeContractInputSerializationIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def migrationStep = new ChangeContractInputSerialization()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_7_0/contract_input")
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
        byte[] content = serialize("myValue")

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

        then: 'contract data is migrated'
        def contract_data = migrationContext.sql.firstRow("SELECT * FROM contract_data")
        getMigratedValue(contract_data.val) == "myValue"

        and: 'archived contract data is migrated'
        def arch_contract_data = migrationContext.sql.firstRow("SELECT * FROM  arch_contract_data")
        getMigratedValue(arch_contract_data.val) == "myValue"
    }

    private static byte[] serialize(String myValue) {
        def bytes = new ByteArrayOutputStream()
        def objectOutputStream = new ObjectOutputStream(bytes)
        objectOutputStream.writeObject(myValue)
        objectOutputStream.close()
        return bytes.toByteArray()
    }

    private String getMigratedValue(def value) {
        def migratedValue = value
        if (ORACLE.equals(migrationContext.dbVendor)) {
            migratedValue = migrationContext.databaseHelper.getClobContent(value)
        }
        migratedValue

    }
}