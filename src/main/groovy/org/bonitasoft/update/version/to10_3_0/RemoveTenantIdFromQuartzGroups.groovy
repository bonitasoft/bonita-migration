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
 * Remove tenantId from group notion in Quartz tables
 */
class RemoveTenantIdFromQuartzGroups extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            dropForeignKey("QRTZ_CRON_TRIGGERS", "FK_QRTZ_CRON_TRIGGERS")
            dropForeignKey("QRTZ_TRIGGERS", "FK_QRTZ_TRIGGERS")
            dropForeignKey("QRTZ_SIMPLE_TRIGGERS", "FK_QRTZ_SIMPLE_TRIGGERS")
            dropForeignKey("QRTZ_SIMPROP_TRIGGERS", "FK_QRTZ_SIMPROP_TRIGGERS")

            executeUpdateQuery("update QRTZ_TRIGGERS set trigger_group = 'DEFAULT' where trigger_group is not null")
            executeUpdateQuery("update QRTZ_TRIGGERS set job_group = 'DEFAULT' where job_group is not null")
            executeUpdateQuery("update QRTZ_CRON_TRIGGERS set trigger_group = 'DEFAULT' where trigger_group is not null")
            executeUpdateQuery("update QRTZ_FIRED_TRIGGERS set trigger_group = 'DEFAULT' where trigger_group is not null")
            executeUpdateQuery("update QRTZ_FIRED_TRIGGERS set job_group = 'DEFAULT' where job_group is not null")
            executeUpdateQuery("update QRTZ_PAUSED_TRIGGER_GRPS set trigger_group = 'DEFAULT' where trigger_group is not null")
            executeUpdateQuery("update QRTZ_SIMPLE_TRIGGERS set trigger_group = 'DEFAULT' where trigger_group is not null")
            executeUpdateQuery("update QRTZ_SIMPROP_TRIGGERS set trigger_group = 'DEFAULT' where trigger_group is not null")
            executeUpdateQuery("update QRTZ_BLOB_TRIGGERS set trigger_group = 'DEFAULT' where trigger_group is not null")
            executeUpdateQuery("update QRTZ_JOB_DETAILS set job_group = 'DEFAULT'")

            createForeignKey("QRTZ_TRIGGERS", "FK_QRTZ_TRIGGERS", "QRTZ_JOB_DETAILS", ["SCHED_NAME", "JOB_NAME", "JOB_GROUP"], ["SCHED_NAME", "JOB_NAME", "JOB_GROUP"], false)
            createForeignKey("QRTZ_CRON_TRIGGERS", "FK_QRTZ_CRON_TRIGGERS", "QRTZ_TRIGGERS", ["SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP"], ["SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP"], true)
            createForeignKey("QRTZ_SIMPLE_TRIGGERS", "FK_QRTZ_SIMPLE_TRIGGERS", "QRTZ_TRIGGERS", ["SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP"], ["SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP"], true)
            createForeignKey("QRTZ_SIMPROP_TRIGGERS", "FK_QRTZ_SIMPROP_TRIGGERS", "QRTZ_TRIGGERS", ["SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP"], ["SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP"], true)
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from group notion in Quartz tables"
    }
}
