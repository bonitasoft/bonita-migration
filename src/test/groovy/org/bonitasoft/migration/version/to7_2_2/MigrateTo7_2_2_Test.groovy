package org.bonitasoft.migration.version.to7_2_2

import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author Laurent Leseigneur
 */
class MigrateTo7_2_2_Test extends Specification {

    @Unroll
    def "should contains #size migration steps"(def size) {
        setup:
        def migration = new MigrateTo7_2_2();

        expect:
        migration.getMigrationSteps().size() == size

        where:
        size << 0
    }

//    @Unroll
//    def "should contains #step"(def index, def step) {
//        setup:
//        def migration = new MigrateTo7_2_0();
//
//        expect:
//        migration.getMigrationSteps().get(index).getClass() == step
//
//        where:
//        index || step
//        0     || MigrateProcessDefXml
//    }

}
