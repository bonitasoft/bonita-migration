package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class RemoveCleanInvalidSessionsJob  extends MigrationStep {

    Logger logger

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.executeScript("remove-clean-invalid-sessions-job", "")
        def String jobName = "CleanInvalidSessions"
        context.sql.eachRow("SELECT d.tenantid, d.id FROM job_desc d WHERE d.jobname = $jobName") {
            context.sql.executeUpdate("DELETE FROM job_param WHERE tenantid = ${it.tenantid} AND jobDescriptorId = ${it.id}")
            context.sql.executeUpdate("DELETE FROM job_log WHERE tenantid = ${it.tenantid} AND jobDescriptorId = ${it.id}")
        }
        context.sql.executeUpdate("DELETE FROM job_desc WHERE jobname = $jobName")
    }


    @Override
    String getDescription() {
        return "Remove CleanInvalidSessions (replaced by a spring task)"
    }


}
