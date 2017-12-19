package org.bonitasoft.migration.version.to7_1_5
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
/**
 *
 * BS-14644
 *
 * update all null reach state date and last update date from null to 0 after an update on the hibernate mapping
 *
 * @author Baptiste Mesta
 */
class UpdateNullReachStateDate extends MigrationStep {


    @Override
    def execute(MigrationContext context) {
        context.sql.execute('update flownode_instance set reachedstatedate = 0 where reachedstatedate is null');
        context.sql.execute('update arch_flownode_instance set reachedstatedate = 0 where reachedstatedate is null');
        context.sql.execute('update flownode_instance set lastupdatedate = 0 where lastupdatedate is null');
        context.sql.execute('update arch_flownode_instance set lastupdatedate = 0 where lastupdatedate is null');
    }


    @Override
    String getDescription() {
        return "Update all null reach state date and last update date to 0 after an update on the hibernate mapping"
    }

}
