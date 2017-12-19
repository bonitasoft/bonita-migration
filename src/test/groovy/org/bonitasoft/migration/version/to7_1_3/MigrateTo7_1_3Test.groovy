package org.bonitasoft.migration.version.to7_1_3

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
@Unroll
class MigrateTo7_1_3Test extends Specification {

    def "should contains #step"() {
        setup:
        def migrateTo7_1_3 = new MigrateTo7_1_3()

        expect:
        migrateTo7_1_3.getMigrationSteps().get(index).getClass() == step

        where:
        index || step
        0     || MigrateArchFlowNodeInstanceIndex

    }

}
