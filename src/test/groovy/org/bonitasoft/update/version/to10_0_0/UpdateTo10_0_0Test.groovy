package org.bonitasoft.update.version.to10_0_0

import org.bonitasoft.update.version.to10_0_0.UpdateTo10_0_0
import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author Emmanuel Duchastenier
 */
class UpdateTo10_0_0Test extends Specification {

    @Unroll


    def "should 10.0.0 preUpdateWarnings warn about Java 17"() {
        setup:
        def version = new UpdateTo10_0_0()

        when:
        def warnings = version.getPreUpdateWarnings(null)

        then:
        warnings.size() > 0
        warnings.any {
            it.contains("Java 17")
        }
    }

}
