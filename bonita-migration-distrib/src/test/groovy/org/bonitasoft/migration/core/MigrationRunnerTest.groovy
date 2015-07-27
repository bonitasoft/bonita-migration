package org.bonitasoft.migration.core
import groovy.sql.Sql
import org.junit.Test
/**
 * @author Baptiste Mesta
 */
class MigrationRunnerTest {

    def infos = []
    def sql = [] as Sql
    def logger = [info:{ String message -> infos.add(message)}] as Logger
    def boolean isStepExecuted = false

    @Test
    void testRun() {

        def migrations = [new VersionMigration(){

            @Override
            List<MigrationStep> getMigrationSteps() {
                return [new MigrationStep() {
                    @Override
                    def execute(Sql sql, MigrationStep.DBVendor dbVendor) {
                        isStepExecuted = true
                        return null
                    }

                    @Override
                    String getDescription() {
                        return "the Description"
                    }
                }]
            }
        }
        ]
        MigrationRunner migrationRunner = new MigrationRunner(sql: sql, logger: logger, dbVendor: MigrationStep.DBVendor.POSTGRES, versionMigrations: migrations)

        migrationRunner.run()

        assert isStepExecuted

    }
}
