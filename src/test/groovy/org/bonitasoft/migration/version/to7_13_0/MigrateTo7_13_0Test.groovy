package org.bonitasoft.migration.version.to7_13_0

import spock.lang.Specification
import spock.lang.Unroll;

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
                "RemoveUselessV6formsConfiguration",
                "UpdateApplicationSchema",
                "UpdatePageSchema"
                , "UpdateExistingFinalPages"
                , "CreateNewRemovablePages"
        ]

    }
}
