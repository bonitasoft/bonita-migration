package org.bonitasoft.migration.version.to7_2_3

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class MigrateTo7_2_3_Test extends Specification {

    @Unroll
    def "should contains #size migration steps"(def size) {
        setup:
        def migration = new MigrateTo7_2_3();

        expect:
        migration.getMigrationSteps().size() == size

        where:
        size << 0
    }

}
