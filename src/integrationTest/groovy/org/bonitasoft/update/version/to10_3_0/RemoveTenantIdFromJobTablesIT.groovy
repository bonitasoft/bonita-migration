/**
 * Copyright (C) 2025 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.update.version.to10_3_0

class RemoveTenantIdFromJobTablesIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromJobTables updateStep = new RemoveTenantIdFromJobTables()

    def "should remove tenantId from job_desc, job_param and job_log tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("job_desc", "tenantId")
            hasPrimaryKeyOnTable("job_desc", "pk_job_desc")
            !hasIndexOnTable("job_desc", "idx_job_desc_id")

            !hasColumnOnTable("job_param", "tenantId")
            hasPrimaryKeyOnTable("job_param", "pk_job_param")
            hasIndexOnTable("job_param", "idx_job_param_jobid")
            !hasForeignKeyOnTable("job_param", "fk_job_param_jobid")
            hasForeignKeyOnTable("job_param", "fk_job_param_jobdescriptorid")

            !hasColumnOnTable("job_log", "tenantId")
            hasPrimaryKeyOnTable("job_log", "pk_job_log")
            !hasIndexOnTable("job_log", "idx_job_log_jobdescid")
            !hasUniqueKeyOnTableWithColumns("job_log", "tenantId", "jobDescriptorId")
            hasUniqueKeyOnTableWithNameAndColumns("job_log", "uk_job_log_jobdescriptorid", "jobDescriptorId")
            !hasForeignKeyOnTable("job_log", "fk_job_log_jobid")
            hasForeignKeyOnTable("job_log", "fk_job_log_jobdescriptorid")
        }
    }
}
