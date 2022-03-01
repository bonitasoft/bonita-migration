package org.bonitasoft.update.version.to7_13_0


import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author Emmanuel Duchastenier
 */
class UpdateTo7_13_0Test extends Specification {

    @Unroll
    def "should update to 7.13.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo7_13_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "RemoveUselessV6formsConfiguration"
                , "UpdateApplicationSchema"
                , "UpdatePageSchema"
                , "CreateNewPages"
                , "UpdateProfileMenuToApplications"
                , "RemoveThemes"
        ]

    }

    def "should 7.13.0 preUpdateWarnings warn about Java 11"() {
        setup:
        def UpdateTo7_13_0 = new UpdateTo7_13_0()

        when:
        def warnings = UpdateTo7_13_0.getPreUpdateWarnings(null)

        then:
        warnings.size() > 0
        warnings.any {
            it.contains("Java 11")
        }
    }

    def "should not display pre update warning regarding custom profiles before starting update'"() {
        given:
        def updateTo = new UpdateTo7_13_0()
        expect:
        !updateTo.getPreUpdateWarnings(null).any {
            it.contains("Custom profiles")
        }
    }
}
