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

    def "should remove platform-level queriable_log rows whose id conflicts with a tenant-level row before recreating PK"() {
        given:
        // Insert queriable_log rows with conflicting ids between platform level (tenantid=-1) and tenant (tenantid=1):
        updateContext.sql.executeInsert("""INSERT INTO queriable_log(tenantid, id, log_timestamp, whatYear, whatMonth, dayOfYear, weekOfYear,
            userId, threadNumber, clusterNode, productVersion, severity, actionType, actionScope, actionStatus, rawMessage,
            callerClassName, callerMethodName, numericIndex1, numericIndex2, numericIndex3, numericIndex4, numericIndex5)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                -1L, 1L, 1000L, 2025, 1, 1, 1, 'system', 1L, null, '10.2.0', 'INFO', 'EXECUTE', null, 1, 'platform log 1', null, null, null, null, null, null, null)
        updateContext.sql.executeInsert("""INSERT INTO queriable_log(tenantid, id, log_timestamp, whatYear, whatMonth, dayOfYear, weekOfYear,
            userId, threadNumber, clusterNode, productVersion, severity, actionType, actionScope, actionStatus, rawMessage,
            callerClassName, callerMethodName, numericIndex1, numericIndex2, numericIndex3, numericIndex4, numericIndex5)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                1L, 1L, 2000L, 2025, 1, 1, 1, 'admin', 2L, null, '10.2.0', 'INFO', 'EXECUTE', null, 1, 'tenant log 1', null, null, null, null, null, null, null)
        updateContext.sql.executeInsert("""INSERT INTO queriable_log(tenantid, id, log_timestamp, whatYear, whatMonth, dayOfYear, weekOfYear,
            userId, threadNumber, clusterNode, productVersion, severity, actionType, actionScope, actionStatus, rawMessage,
            callerClassName, callerMethodName, numericIndex1, numericIndex2, numericIndex3, numericIndex4, numericIndex5)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                -1L, 2L, 3000L, 2025, 2, 32, 5, 'system', 1L, null, '10.2.0', 'WARNING', 'EXECUTE', null, 0, 'platform log 2', null, null, null, null, null, null, null)
        updateContext.sql.executeInsert("""INSERT INTO queriable_log(tenantid, id, log_timestamp, whatYear, whatMonth, dayOfYear, weekOfYear,
            userId, threadNumber, clusterNode, productVersion, severity, actionType, actionScope, actionStatus, rawMessage,
            callerClassName, callerMethodName, numericIndex1, numericIndex2, numericIndex3, numericIndex4, numericIndex5)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                1L, 2L, 4000L, 2025, 2, 32, 5, 'admin', 2L, null, '10.2.0', 'WARNING', 'EXECUTE', null, 0, 'tenant log 2', null, null, null, null, null, null, null)
        // Non-conflicting row (only exists at tenant level):
        updateContext.sql.executeInsert("""INSERT INTO queriable_log(tenantid, id, log_timestamp, whatYear, whatMonth, dayOfYear, weekOfYear,
            userId, threadNumber, clusterNode, productVersion, severity, actionType, actionScope, actionStatus, rawMessage,
            callerClassName, callerMethodName, numericIndex1, numericIndex2, numericIndex3, numericIndex4, numericIndex5)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                1L, 3L, 5000L, 2025, 3, 60, 9, 'admin', 3L, null, '10.2.0', 'INFO', 'CREATE', null, 1, 'tenant log 3', null, null, null, null, null, null, null)

        when:
        updateStep.execute(updateContext)

        then: "conflicting rows removed, 3 rows remain (tenant-level rows kept over platform-level)"
        updateContext.sql.firstRow("SELECT COUNT(*) AS cnt FROM queriable_log").cnt == 3

        and: "for each conflicting id, the tenant-level row survived and the platform-level row was removed"
        updateContext.sql.firstRow("SELECT rawMessage FROM queriable_log WHERE id = 1").rawMessage == 'tenant log 1'
        updateContext.sql.firstRow("SELECT rawMessage FROM queriable_log WHERE id = 2").rawMessage == 'tenant log 2'

        and: "PK successfully recreated on (id) only"
        updateContext.databaseHelper.hasPrimaryKeyOnTable("queriable_log", "pk_queriable_log")

        and: "tenantId column removed"
        !updateContext.databaseHelper.hasColumnOnTable("queriable_log", "tenantId")
    }
}
