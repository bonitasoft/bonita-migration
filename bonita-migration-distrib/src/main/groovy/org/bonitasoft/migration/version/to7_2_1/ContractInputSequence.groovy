package org.bonitasoft.migration.version.to7_2_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
/**
 *
 * parameters where in bonita-home/engine-server/work/tenants/<tenantId>/processes/<process_id>/parameters.properties
 *
 * @author Baptiste Mesta
 */
class ContractInputSequence extends MigrationStep {


    @Override
    def execute(MigrationContext context) {
        //remove  10220 and 20220
        context.sql.executeInsert("DELETE FROM sequence WHERE id = ${10220} OR id = ${20220}")

    }


    @Override
    String getDescription() {
        return "Put all contract inputs on the same sequence"
    }

}
