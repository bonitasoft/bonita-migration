package org.bonitasoft.migration.version.v7_0_1

import groovy.sql.Sql
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Baptiste Mesta
 */
class DoSomethingStep extends MigrationStep{

    @Override
    def execute(Sql sql, DBVendor dbVendor) {
        println "executing do something step"
    }

    @Override
    String getDescription() {
        return "step that do something"
    }
}
