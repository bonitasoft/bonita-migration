package org.bonitasoft.migration.versions.v6_5_1_to_6_5_2

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep

/**
 * @author Elias Ricken de Medeiros
 */
class UpdateEventSubProcessStableFlag extends DatabaseMigrationStep {


    UpdateEventSubProcessStableFlag(final Sql sql, final String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    def migrate() {
        def updatedFlowNodes = executeUpdate("update flownode_instance set stable = true where kind = 'subProc' and stateId = 31 and stable = false")
        println "$updatedFlowNodes rows updated."
        return updatedFlowNodes
    }
}
