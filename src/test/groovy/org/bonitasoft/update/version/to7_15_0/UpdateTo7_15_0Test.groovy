package org.bonitasoft.update.version.to7_15_0

import org.bonitasoft.update.version.to7_14_0.UpdateTo7_14_0
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Dumitru Corini
 */
class UpdateTo7_15_0Test extends Specification {

    @Unroll
    def "should update to 7.15.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo7_15_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "MakeAdminPagesFinal"
        ]

    }
}
