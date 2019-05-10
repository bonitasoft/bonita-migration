package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class AddIndexOnJobParams extends MigrationStep {


    @Override
    Object execute(MigrationContext context) {
        return context.getDatabaseHelper().addOrReplaceIndex("job_param", "idx_job_param_tenant_jobid", "tenantid", "jobDescriptorId")

    }

    @Override
    String getDescription() {
        return "Add new index 'idx_job_param_tenant_jobid' on 'job_param' table"
    }
}
