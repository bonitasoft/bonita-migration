package org.bonitasoft.migration.version.to7_9_0

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Anthony Birembaut
 */
class MigrateTo7_9_0Test extends Specification {

    @Unroll
    def "should migration to 7.9.0 include step '#stepName'"(def stepName) {
        given:
        def migrateTo = new MigrateTo7_9_0()

        expect:
        def steps = migrateTo.migrationSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "RemoveCleanInvalidSessionsJob"
                , "ChangeProfileEntryForOrganizationImport"
        ]

    }

}
