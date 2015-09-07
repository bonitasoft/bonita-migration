package org.bonitasoft.migration.version.to7_1_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class MigrateQuartzIndexes extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def String sql
        def sqlFile = "/version/to_7_1_0/quartz/" + context.dbVendor.toString().toLowerCase() + "_quartz.sql"
        def stream1 = this.class.getResourceAsStream(sqlFile)
        stream1.withStream { InputStream s ->
            sql = s.getText()
        }
        def statements = sql.split("@@")
        statements.each {
            it -> context.databaseHelper.executeDbVendorStatement(it)
        }


    }


    @Override
    String getDescription() {
        return "add indexes on Quartz tables"
    }
}
