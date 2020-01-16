package org.bonitasoft.migration.version.to7_10_0

import org.bonitasoft.migration.version.to7_11_0.MigrateTo7_11_0
import org.bonitasoft.migration.version.to7_9_0.MigrateTo7_9_0
import spock.lang.Specification
import spock.lang.Unroll

class MigrateTo7_11_0Test extends Specification {

    @Unroll
    def "Migration to 7.11.0 should include step '#stepName'"(def stepName) {
        given:
        def migrateTo = new MigrateTo7_11_0()

        expect:
        def steps = migrateTo.migrationSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "AddIndexOnArchFlownodeInstance"
        ]

    }
}
