package org.bonitasoft.migration.version.to7_7_0


import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.dbunit.ext.oracle.OracleConnection
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.DriverManager

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
        dbUnitHelper.context.databaseHelper.dropPrimaryKey("arch_contract_data")
        if (dbUnitHelper.context.dbVendor == MigrationStep.DBVendor.ORACLE) {
            //We see sometimes in oracle a cases where index is not deleted while the primary key is
            migrationContext.sql.execute("""create index pk_arch_contract_data on arch_contract_data (tenantid, id, scopeId)""")
            migrationContext.sql.execute("""ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId)""")
        }

        when:
        migrationStep.execute(migrationContext)

        then:
        migrationContext.sql.firstRow("SELECT count(*) FROM arch_contract_data_backup") [0] == 1
        migrationContext.sql.firstRow("SELECT count(*) FROM arch_contract_data") [0] == 0
    }
}