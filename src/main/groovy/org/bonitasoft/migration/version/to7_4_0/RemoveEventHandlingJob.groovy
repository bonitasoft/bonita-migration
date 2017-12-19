/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Baptiste Mesta
 */
class RemoveEventHandlingJob extends MigrationStep {

    Logger logger

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.executeScript("remove-event-handling-job", "")
        def String jobName = "BPMEventHandling"
        context.sql.eachRow("SELECT d.tenantid, d.id FROM job_desc d WHERE d.jobname = $jobName") {
            context.sql.executeUpdate("DELETE FROM job_param WHERE tenantid = ${it.tenantid} AND jobDescriptorId = ${it.id}")
            context.sql.executeUpdate("DELETE FROM job_log WHERE tenantid = ${it.tenantid} AND jobDescriptorId = ${it.id}")
        }
        context.sql.executeUpdate("DELETE FROM job_desc WHERE jobname = $jobName")
    }


    @Override
    String getDescription() {
        return "Remove BPMEventHandlingJob (replaced by a dedicated service)"
    }


}
