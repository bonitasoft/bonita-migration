/**
 * Copyright (C) 2024 Bonitasoft S.A.
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

class RemoveTenantIdFromTriggersEventsMessagesIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromTriggersEventsMessages updateStep = new RemoveTenantIdFromTriggersEventsMessages()

    def "should remove tenantId from tables 'event_trigger_instance', 'waiting_event', 'message_instance', 'queriable_log'"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("event_trigger_instance", "tenantId")
            hasPrimaryKeyOnTable("event_trigger_instance", "pk_event_trigger_instance")
            !hasForeignKeyOnTable("event_trigger_instance", "fk_event_trigger_instance_tenantId")
            !hasForeignKeyOnTable("event_trigger_instance", "fk_EvtTrig_tenId") // Oracle-specific

            !hasColumnOnTable("waiting_event", "tenantId")
            hasPrimaryKeyOnTable("waiting_event", "pk_waiting_event")
            !hasForeignKeyOnTable("waiting_event", "fk_waiting_event_tenantId")

            !hasColumnOnTable("message_instance", "tenantId")
            hasPrimaryKeyOnTable("message_instance", "pk_message_instance")
            !hasForeignKeyOnTable("message_instance", "fk_message_instance_tenantId")

            !hasColumnOnTable("queriable_log", "tenantId")
            hasPrimaryKeyOnTable("queriable_log", "pk_queriable_log")
        }
    }
}
