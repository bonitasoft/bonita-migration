package org.bonitasoft.migration.version.to7_7_5

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class DeleteOrphanArchContractData extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        //7.7.5 version hard coded because step is launched in 7.7.0 and 7.7.5 but the scripts are in the 7.7.5 folder
        context.databaseHelper.executeScript("7.7.5","remove_orphan_contract_data", "")
        return null
    }

    @Override
    String getDescription() {
        return "Delete archived contract data that are orphan (they were not deleted)"
    }
}
