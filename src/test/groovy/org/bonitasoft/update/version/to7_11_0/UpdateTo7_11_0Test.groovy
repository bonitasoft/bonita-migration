package org.bonitasoft.update.version.to7_11_0


import spock.lang.Specification
import spock.lang.Unroll

class UpdateTo7_11_0Test extends Specification {

    @Unroll
    def "Update to 7.11.0 should include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo7_11_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "AddIndexOnArchFlownodeInstance"
                , "RefactorPlatformColumns"
                , "UpdateBOM"
                , "AddIndexOnProcessComments"
        ]

    }
}
