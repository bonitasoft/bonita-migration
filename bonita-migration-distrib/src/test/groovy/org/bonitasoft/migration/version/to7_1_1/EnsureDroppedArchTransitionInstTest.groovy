package org.bonitasoft.migration.version.to7_1_1
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification
/**
 * @author Elias Ricken de Medeiros
 */
class EnsureDroppedArchTransitionInstTest extends Specification {

    def EnsureDroppedArchTransitionInst migrationStep = new EnsureDroppedArchTransitionInst()
    def databaseHelper = Mock(DatabaseHelper)
    def context = Mock(MigrationContext)

    def setup () {
        context.databaseHelper >> databaseHelper
    }


    def "execute should call dropTableIfExits"() {
        when:
        migrationStep.execute(context)

        then:
        1 * databaseHelper.dropTableIfExists("arch_transition_instance")

    }

    def "GetDescription should return the description"() {

        when:
        def description = migrationStep.getDescription()

        then:
        description == "drop table arch_transition_instance if exists"

    }
}
