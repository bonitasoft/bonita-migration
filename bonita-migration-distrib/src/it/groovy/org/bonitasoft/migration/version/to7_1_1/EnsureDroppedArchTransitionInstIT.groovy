package org.bonitasoft.migration.version.to7_1_1
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
/**
 * @author Elias Ricken de Medeiros
 */
class EnsureDroppedArchTransitionInstIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def "should drop arch_transition_instance table when exists"() {
        dbUnitHelper.createTables("7_0_0", "archTransition")

        when:
        new EnsureDroppedArchTransitionInst().execute(migrationContext)

        then:
        dbUnitHelper.hasTable("arch_transition_instance") == false

        //clean up
        dbUnitHelper.dropTables("tenant")

    }

    def "execution step should not throws exception when the table arch_transition_instance does not exist"() {
        when:
        new EnsureDroppedArchTransitionInst().execute(migrationContext)

        then:
        dbUnitHelper.hasTable("arch_transition_instance") == false

    }
}
