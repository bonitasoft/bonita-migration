package org.bonitasoft.migration.version.to7_3_0

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Emmanuel Duchastenier
 */
class MigrateTo7_3_0Test extends Specification {

    @Unroll
    def "should contains #size migration steps"(def size) {
        setup:
        def migration = new MigrateTo7_3_0();

        expect:
        migration.getMigrationSteps().size() == size

        where:
        size << 4
    }

}
