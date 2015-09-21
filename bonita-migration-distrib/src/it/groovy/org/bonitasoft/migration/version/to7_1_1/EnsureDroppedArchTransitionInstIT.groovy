package org.bonitasoft.migration.version.to7_1_1
import groovy.sql.Sql
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.version.DBUnitMigrationContext
import spock.lang.Shared
import spock.lang.Specification
/**
 * @author Elias Ricken de Medeiros
 */
class EnsureDroppedArchTransitionInstIT extends Specification {

    @Shared
    Sql sql = DBUnitHelper.createSqlConnection()

    def "should drop arch_transition_instance table when exists"() {
        DBUnitHelper.createTables(sql, "7_0_0", "archTransition")

        when:
        new EnsureDroppedArchTransitionInst().execute(new DBUnitMigrationContext(sql))

        then:
        DBUnitHelper.hasTable(sql, "arch_transition_instance") == false

        //clean up
        DBUnitHelper.dropTables(sql, "tenant")

    }

    def "execution step should not throws exception when the table arch_transition_instance does not exist"() {
        when:
        new EnsureDroppedArchTransitionInst().execute(new DBUnitMigrationContext(sql))

        then:
        DBUnitHelper.hasTable(sql, "arch_transition_instance") == false

    }
}
