package org.bonitasoft.migration.version.to7_2_0
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Elias Ricken de Medeiros
 */
class MigrateTo7_2_0Test extends Specification {

    def "should contains #step"() {
        setup:
        def migration = new MigrateTo7_2_0();

        expect:
        migration.getMigrationSteps().get(index).getClass() == step

        where:
        index || step
        0     || MigrateProcessDefXml
        1     || ParametersInDatabase

    }

}
