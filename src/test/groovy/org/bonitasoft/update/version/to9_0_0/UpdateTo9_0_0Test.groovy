package org.bonitasoft.update.version.to9_0_0

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Anthony Birembaut
 */
class UpdateTo9_0_0Test extends Specification {

    @Unroll
    def "should update to 9.0.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo9_0_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "CreateTemporaryContentTable"
        ]

    }
}
