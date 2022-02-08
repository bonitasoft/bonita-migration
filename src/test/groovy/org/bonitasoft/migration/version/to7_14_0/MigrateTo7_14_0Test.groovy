package org.bonitasoft.migration.version.to7_14_0

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Dumitru Corini
 */
class MigrateTo7_14_0Test extends Specification {

    @Unroll
    def "should migration to 7.14.0 include step '#stepName'"(def stepName) {
        given:
        def migrateTo = new MigrateTo7_14_0()

        expect:
        def steps = migrateTo.migrationSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "RemoveHiddenFieldFromPages",
                "RemoveReportingTables"
        ]

    }
}
