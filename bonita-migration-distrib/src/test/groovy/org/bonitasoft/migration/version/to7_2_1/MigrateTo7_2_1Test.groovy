package org.bonitasoft.migration.version.to7_2_1

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Emmanuel Duchastenier
 */
class MigrateTo7_2_1Test extends Specification {

    @Unroll
    def "should contains #size migration steps"(def size) {
        setup:
        def migration = new MigrateTo7_2_1();

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
