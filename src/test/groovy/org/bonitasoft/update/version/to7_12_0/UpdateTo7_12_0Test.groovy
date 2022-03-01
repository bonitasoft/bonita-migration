package org.bonitasoft.update.version.to7_12_0

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Danila Mazour
 */
class UpdateTo7_12_0Test extends Specification {

    @Unroll
    def "should update to 7.12.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo7_12_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "ChangeProfileEntryForInstallExportOrganization",
                "AddIndexLG4LG2OnArchFlownodeInstance"
        ]

    }
}
