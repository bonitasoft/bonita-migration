package org.bonitasoft.migration.version.to7_1_1

import org.bonitasoft.migration.version.to7_1_0.MigrateQuartzIndexes
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Elias Ricken de Medeiros
 */
@Unroll
class MigrateTo7_1_1Test extends Specification {

    def "should contains #step"() {
        def migration = new MigrateTo7_1_1();

        expect:
        migration.getMigrationSteps().get(index).getClass() == step

        where:
        index || step
        0     || MigrateQuartzRenameColumn
        1     || EnsureDroppedArchTransitionInst
        2     || MigrateQuartzIndexes
    }

}
