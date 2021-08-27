package org.bonitasoft.migration.version.to7_13_0


import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author Emmanuel Duchastenier
 */
class MigrateTo7_13_0Test extends Specification {

    @Unroll
    def "should migration to 7.13.0 include step '#stepName'"(def stepName) {
        given:
        def migrateTo = new MigrateTo7_13_0()

        expect:
        def steps = migrateTo.migrationSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "RemoveUselessV6formsConfiguration"
                , "UpdateApplicationSchema"
                , "UpdatePageSchema"
                , "CreateNewPages"
                , "MigrateProfileMenuToApplications"
                , "RemoveThemes"
        ]

    }

    def "should 7.13.0 preMigrationWarnings warn about Java 11"() {
        setup:
        def MigrateTo7_13_0 = new MigrateTo7_13_0()

        when:
        def warnings = MigrateTo7_13_0.getPreMigrationWarnings(null)

        then:
        warnings.size() > 0
        warnings.any {
            it.contains("Java 11")
        }
    }

    def "should not display pre migration warning regarding custom profiles before starting migration'"() {
        given:
        def migrateTo = new MigrateTo7_13_0()
        expect:
        !migrateTo.getPreMigrationWarnings(null).any {
            it.contains("Custom profiles")
        }
    }
}
