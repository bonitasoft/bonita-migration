package org.bonitasoft.migration.version.to7_7_0

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.ORACLE
import static org.bonitasoft.migration.version.to7_7_0.ChangeContractInputSerialization.newColumnType

import java.sql.Connection

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ChangeContractInputSerializationIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

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
insert into contract_data(tenantId , id, kind, scopeId, name, val)
values (1, 2, 'PROCESS', 1001, 'myInputNull', NULL)
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

        when: 're execute migration'
        migrationStep.execute(migrationContext)
        then: 'nothing happens'
        getMigratedValue(migrationContext.sql.firstRow("SELECT * FROM contract_data WHERE name='myInput'").val) == "myValue"
        getMigratedValue(migrationContext.sql.firstRow("SELECT * FROM contract_data WHERE name='myInputNull'").val) == null
        getMigratedValue(migrationContext.sql.firstRow("SELECT * FROM  arch_contract_data").val) == "myValue"
    }

    def "should migrate multiple contract data"() {
        given:
        byte[] content = serialize("<string>some long string</string>")
        migrationContext.sql.withTransaction { Connection connection ->

            for (int i = 0; i < 50; i++) {
                migrationContext.sql.executeUpdate("""
insert into contract_data(tenantId , id, kind, scopeId, name, val)
values (1, $i, 'PROCESS', $i, 'myInput', $content)
""")
                migrationContext.sql.executeUpdate("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val)
values (1, $i, 1, $i, 'PROCESS', $i, 'myInput', $content)
""")
            }
        }
        when:
        migrationStep.execute(migrationContext)
        then:
        migrationContext.sql.rows("SELECT tenantid,id FROM contract_data").size() == 50
        migrationContext.sql.rows("SELECT tenantid,id FROM arch_contract_data").size() == 50
    }

    def "should migrate when no contract data is present"() {
        when:
        migrationStep.execute(migrationContext)
        then:
        migrationContext.sql.rows("SELECT tenantid,id FROM contract_data").size() == 0
        migrationContext.sql.rows("SELECT tenantid,id FROM arch_contract_data").size() == 0
        ['NVARCHAR', 'TEXT', 'CLOB', 'LONGTEXT'].contains(migrationContext.databaseHelper.getColumnType("contract_data", "val").toUpperCase())
        ['NVARCHAR', 'TEXT', 'CLOB', 'LONGTEXT'].contains(migrationContext.databaseHelper.getColumnType("arch_contract_data", "val").toUpperCase())
    }

    def "should migrate when only null contract data is present"() {
        given:
        migrationContext.sql.executeUpdate("""
insert into contract_data(tenantId , id, kind, scopeId, name, val)
values (1, 1, 'PROCESS', 1, 'myInput', null)
""")
        migrationContext.sql.executeUpdate("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val)
values (1, 1, 1, 1, 'PROCESS', 1, 'myInput', null)
""")
        when:
        migrationStep.execute(migrationContext)
        then:
        migrationContext.sql.rows("SELECT tenantid,id FROM contract_data").size() == 1
        migrationContext.sql.rows("SELECT tenantid,id FROM arch_contract_data").size() == 1
        ['NVARCHAR', 'TEXT', 'CLOB', 'LONGTEXT'].contains(migrationContext.databaseHelper.getColumnType("contract_data", "val").toUpperCase())
        ['NVARCHAR', 'TEXT', 'CLOB', 'LONGTEXT'].contains(migrationContext.databaseHelper.getColumnType("arch_contract_data", "val").toUpperCase())
    }

    def 'should migrate when tmp_val column has been created in a previous run'() {
        given:
        addTmpValColumn('contract_data')
        addTmpValColumn('arch_contract_data')
        byte[] nonMigratedContent = serialize("myValue")

        migrationContext.sql.execute("""
insert into contract_data(tenantId , id, kind, scopeId, name, val)
values (1, 1, 'PROCESS', 1000, 'myInput', $nonMigratedContent)
""")
        migrationContext.sql.execute("""
insert into contract_data(tenantId , id, kind, scopeId, name, val)
values (1, 2, 'PROCESS', 1001, 'myInputNull', NULL)
""")
        migrationContext.sql.execute("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val)
values (1, 1, 1, 1, 'PROCESS', 1001, 'myInput', $nonMigratedContent)
""")
        migrationContext.sql.execute("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val)
values (1, 2, 1, 2, 'PROCESS', 1002, 'myInput', $nonMigratedContent)
""")
        migrationContext.sql.execute("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val, tmp_val)
values (1, 3, 1, 3, 'PROCESS', 1003, 'myInput', $nonMigratedContent, 'already migrated value that will not be updated')
""")

        when:
        migrationStep.execute(migrationContext)

        then:
        migrationContext.sql.rows("SELECT tenantid,id FROM contract_data").size() == 2
        migrationContext.sql.rows("SELECT tenantid,id FROM arch_contract_data").size() == 3

        // already migrated value kept untouched
        getMigratedValue(migrationContext.sql.firstRow("SELECT * FROM arch_contract_data WHERE tenantId=1 AND id=3").val)== 'already migrated value that will not be updated'
    }

    def 'should migrate hundreds of arch_contract_data'() {
        given:
        final int numberOfArchContractData = 787
        migrationContext.sql.withTransaction { Connection connection ->
            for (int i = 0; i < numberOfArchContractData; i++) {
                byte[] content = i % 7 == 0 ? null: serialize("<string>some long string $i</string>")
                migrationContext.sql.executeUpdate("""
insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val)
values (1, $i, 1, $i, 'PROCESS', $i, 'myInput', $content)
""")
            }
        }
        when:
        migrationStep.execute(migrationContext)
        then:
        migrationContext.sql.rows("SELECT tenantid,id FROM arch_contract_data").size() == numberOfArchContractData
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static byte[] serialize(String myValue) {
        def baos = new ByteArrayOutputStream()
        def objectOutputStream = new ObjectOutputStream(baos)
        objectOutputStream.writeObject(myValue)
        objectOutputStream.close()
        return baos.toByteArray()
    }

    private void addTmpValColumn(String tableName) {
        migrationContext.databaseHelper.addColumn(tableName, "tmp_val", newColumnType(migrationContext), null, null)
    }

    private String getMigratedValue(def value) {
        def migratedValue = value
        if (value != null && ORACLE.equals(migrationContext.dbVendor)) {
            migratedValue = migrationContext.databaseHelper.getClobContent(value)
        }
        migratedValue
    }

}
