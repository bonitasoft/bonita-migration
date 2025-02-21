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

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Remove tenantId from job_desc, job_param and job_log tables
 */
class RemoveTenantIdFromJobTables extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            dropForeignKey("job_desc", "fk_job_desc_tenantId")
            dropForeignKey("job_param", "fk_job_param_tenantId")
            dropForeignKey("job_param", "fk_job_param_jobid")
            dropForeignKey("job_param", "fk_job_param_jobdescriptorid") // to make step reentrant
            dropForeignKey("job_log", "fk_job_log_jobid")
            dropForeignKey("job_log", "fk_job_log_jobdescriptorid") // to make step reentrant

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            dropIndexIfExists("job_desc", "idx_job_desc_id")
            dropIndexIfExists("job_log", "idx_job_log_jobdescid")

            // recreate PK:
            recreatePrimaryKey("job_desc")
            recreatePrimaryKey("job_param")
            recreatePrimaryKey("job_log")

            // recreate UK:
            dropUniqueKeyFromColumns("job_log", "tenantId", "jobDescriptorId")
            createUniqueKey("job_log", "uk_job_log_jobdescriptorid", "jobDescriptorId")

            // recreate FK:
            createForeignKey("job_param", "fk_job_param_jobdescriptorid", "job_desc", ["jobDescriptorId"], ["id"], true)
            createForeignKey("job_log", "fk_job_log_jobdescriptorid", "job_desc", ["jobDescriptorId"], ["id"], true)

            // drop the columns:
            dropColumnIfExists("job_desc", "tenantId")
            dropColumnIfExists("job_param", "tenantId")
            dropColumnIfExists("job_log", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'job_desc', 'job_param' and 'job_log' tables"
    }
}
