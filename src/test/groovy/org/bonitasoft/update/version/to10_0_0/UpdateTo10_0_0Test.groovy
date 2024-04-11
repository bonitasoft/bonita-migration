package org.bonitasoft.update.version.to10_0_0

import org.bonitasoft.update.core.UpdateContext
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Emmanuel Duchastenier
 */
class UpdateTo10_0_0Test extends Specification {

    @Unroll


    def "should 10.0.0 preUpdateWarnings warn about Java 17 & wordSearchExclusionMapping & dynamic authorization"() {
        setup:
        def version = Spy(UpdateTo10_0_0.class)
        version.wordSearchExclusionMappingsExist(null) >> true

        when:
        def warnings = version.getPreUpdateWarnings(null)

        then:
        warnings.size() > 0
        warnings.any {
            it.contains("Java 17")
            it.contains("wordSearchExclusionMapping")
            it.contains("dynamic REST API authorizations")
        }
    }

    @Unroll
    def "should update to 10.0.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo10_0_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "RemoveEnableWordSearchConfig",
                "AddEnableDynamicCheckConfig"
        ]
    }

}
