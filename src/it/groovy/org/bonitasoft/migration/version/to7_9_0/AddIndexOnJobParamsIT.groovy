package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

class AddIndexOnJobParamsIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)
    AddIndexOnJobParams migrationStep = new AddIndexOnJobParams()


    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.9.0")
        dbUnitHelper.createTables("7_9_0/job_params")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["job_param","job_desc"] as String[])
    }

    def "should have added the index during the migration"(){

        given:

        when:
        migrationStep.execute(migrationContext)

        then:
        dbUnitHelper.hasIndexOnTable("job_param", "idx_job_param_tenant_jobid")

    }

}
