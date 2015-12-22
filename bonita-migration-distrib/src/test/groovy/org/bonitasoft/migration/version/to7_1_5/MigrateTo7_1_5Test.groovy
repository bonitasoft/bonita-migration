package org.bonitasoft.migration.version.to7_1_5

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Elias Ricken de Medeiros
 */
@Unroll
class MigrateTo7_1_5Test extends Specification {

    def "should contains step #step"() {
        def migration = new MigrateTo7_1_5();

        expect:
        migration.getMigrationSteps().get(index).getClass() == step

        where:
        index || step
        0     || MigrateArchProcessCommentIndex
        1     || MigrateActorMember
        2     || MigratePendingMapping
        3     || MigrateArchiveFlownodeInstanceIndex
        4     || UpdateNullReachStateDate
    }

}
