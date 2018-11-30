package org.bonitasoft.migration.version.to7_7_5

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DeleteOrphanArchContractDataIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def migrationStep = new DeleteOrphanArchContractData()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_7_5/orphan_contract_data")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["arch_contract_data", "arch_process_instance", "arch_flownode_instance"] as String[])
    }

    def "should have deleted orphan contract data"() {
        given:
        //process exists:
        migrationContext.sql.execute("insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val) values (1, 1, 1, 1, 'PROCESS', 4000, 'myInput', 'value')")
        //process deleted:
        migrationContext.sql.execute("insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val) values (1, 2, 1, 2, 'PROCESS', 9000, 'myInput', 'value')")
        //task exists:
        migrationContext.sql.execute("insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val) values (1, 3, 1, 3, 'TASK', 6000, 'myInput', 'value')")
        //task deleted:
        migrationContext.sql.execute("insert into arch_contract_data(tenantId , sourceobjectid, archiveDate, id, kind, scopeId, name, val) values (1, 4, 1, 4, 'TASK', 10000, 'myInput', 'value')")

        migrationContext.sql.execute("insert into arch_process_instance(tenantId, sourceObjectId, id, name) values (1,4000,5000, 'process')")
        migrationContext.sql.execute("insert into arch_flownode_instance(tenantId, sourceObjectId, id, name) values (1,6000,5001, 'task')")

        when:
        migrationStep.execute(migrationContext)

        then:
        migrationContext.sql.rows("SELECT id FROM arch_contract_data").id.sort() == [1/*contract data of the existing process*/, 3/*contract data of the existing task*/]
    }
}
