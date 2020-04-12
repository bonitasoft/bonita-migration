package org.bonitasoft.migration.version.to7_1_2

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
/**
 * @author Elias Ricken de Medeiros
 */
class EnsureDroppedArchTransitionInstIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context


    def "should drop arch_transition_instance table when exists"() {
        dbUnitHelper.createTables("7_0_0", "archTransition")

        when:
        new EnsureDroppedArchTransitionInst().execute(migrationContext)

        then:
        !migrationContext.databaseHelper.hasTable("arch_transition_instance")

        cleanup:
        dbUnitHelper.dropTables("tenant")

    }

    def "execution step should not throws exception when the table arch_transition_instance does not exist"() {
        when:
        new EnsureDroppedArchTransitionInst().execute(migrationContext)

        then:
        !migrationContext.databaseHelper.hasTable("arch_transition_instance")

    }
}
