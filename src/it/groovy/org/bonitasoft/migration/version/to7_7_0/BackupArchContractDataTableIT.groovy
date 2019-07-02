package org.bonitasoft.migration.version.to7_7_0


import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class BackupArchContractDataTableIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def migrationStep = new BackupArchContractDataTable()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_7_0/contract_input")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["contract_data", "arch_contract_data", "arch_contract_data_backup"] as String[])
    }

    @Unroll
    def "should back up table arch_contract_data into arch_contract_data_backup"() {
        given:
        byte[] someContent = "someBinaryContent".bytes
        migrationContext.sql.execute("""
insert into arch_contract_data(tenantId, id, kind, scopeId, name, val, archiveDate, sourceobjectid)
VALUES (1, 1, 'task', 12, 'name', $someContent, 12143421, 212)
""")

        when:
        migrationStep.execute(migrationContext)

        then:
        migrationContext.sql.firstRow("SELECT count(*) FROM arch_contract_data_backup")[0] == 1
        migrationContext.sql.firstRow("SELECT count(*) FROM arch_contract_data")[0] == 0
    }

}