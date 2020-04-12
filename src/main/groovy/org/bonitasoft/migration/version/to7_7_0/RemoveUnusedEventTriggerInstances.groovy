/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_7_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class RemoveUnusedEventTriggerInstances extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def dbHelper = context.databaseHelper
        if (dbHelper.hasColumnOnTable('event_trigger_instance', 'kind')) { // so that this step is re-entrant
            def numberOfRows = context.sql.executeUpdate("DELETE FROM event_trigger_instance WHERE kind <> 'timer'")
            context.logger.info("removed $numberOfRows unused event trigger instances")
        }
        dbHelper.dropColumnIfExists("event_trigger_instance", "kind")
        dbHelper.dropColumnIfExists("event_trigger_instance", "messageName")
        dbHelper.dropColumnIfExists("event_trigger_instance", "targetProcess")
        dbHelper.dropColumnIfExists("event_trigger_instance", "targetFlowNode")
        dbHelper.dropColumnIfExists("event_trigger_instance", "signalName")
        dbHelper.dropColumnIfExists("event_trigger_instance", "errorCode")
    }

    @Override
    String getDescription() {
        return "Remove unused event trigger instances elements and columns"
    }

}
