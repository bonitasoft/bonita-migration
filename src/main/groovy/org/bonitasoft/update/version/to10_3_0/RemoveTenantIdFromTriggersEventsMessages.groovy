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

import static org.bonitasoft.update.core.UpdateStep.DBVendor.ORACLE

/**
 * Remove tenantId from tables 'event_trigger_instance', 'waiting_event', 'message_instance'
 */
class RemoveTenantIdFromTriggersEventsMessages extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            dropForeignKey("event_trigger_instance", dbVendor == ORACLE ? "fk_EvtTrig_tenId" : "fk_event_trigger_instance_tenantId")
            dropForeignKey("waiting_event", "fk_waiting_event_tenantId")
            dropForeignKey("message_instance", "fk_message_instance_tenantId")

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            // no index on those tables to drop

            // recreate PK:
            dropPrimaryKey("event_trigger_instance")
            createPrimaryKey("event_trigger_instance", "id")
            dropPrimaryKey("waiting_event")
            createPrimaryKey("waiting_event", "id")
            dropPrimaryKey("message_instance")
            createPrimaryKey("message_instance", "id")

            // recreate UK:
            // No UK for those tables

            // recreate FK:
            // no FK to recreate for those tables

            // drop the columns:
            dropColumnIfExists("event_trigger_instance", "tenantId")
            dropColumnIfExists("waiting_event", "tenantId")
            dropColumnIfExists("message_instance", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'event_trigger_instance', 'waiting_event', 'message_instance' tables"
    }
}
