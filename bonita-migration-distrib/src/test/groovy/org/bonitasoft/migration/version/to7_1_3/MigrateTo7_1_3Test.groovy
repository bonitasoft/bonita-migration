package org.bonitasoft.migration.version.to7_1_3

import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class MigrateTo7_1_3Test extends Specification {

    def "should contains #step"() {
        setup:
        def migrateTo7_1_3 = new MigrateTo7_1_3()

        when:
        def steps = migrateTo7_1_3.migrationSteps

        then:
        steps.size() == 1

    }

}
