package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Emmanuel Duchastenier
 */
class DropOrphanWaitingEventsIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        cleanTables()
        dbUnitHelper.createTables("7_3_0/waitingEvent", "createWaitingEvent")

        dbUnitHelper.context.sql.execute("""INSERT INTO waiting_event
    (tenantid, id, kind, eventType, messageName, signalName, errorCode, processName, flowNodeName, flowNodeDefinitionId, subProcessId, processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, flowNodeInstanceId, relatedActivityInstanceId, locked, active, progress, correlation1, correlation2, correlation3, correlation4, correlation5)
    VALUES(1, 0, 'SIGNAL', '', '', '', '', '', '', 0, 0, 123456789000, 0, 0, 0, 0, ${dbUnitHelper.falseValue()}, ${
            dbUnitHelper.falseValue()
        }, 0, '', '', '', '', '')""")
        dbUnitHelper.context.sql.execute("""INSERT INTO waiting_event
    (tenantid, id, kind, eventType, messageName, signalName, errorCode, processName, flowNodeName, flowNodeDefinitionId, subProcessId, processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, flowNodeInstanceId, relatedActivityInstanceId, locked, active, progress, correlation1, correlation2, correlation3, correlation4, correlation5)
    VALUES(1, 1, 'SIGNAL', '', '', '', '', '', '', 0, 0, 8888888888888, 0, 0, 0, 0, ${dbUnitHelper.falseValue()}, ${
            dbUnitHelper.falseValue()
        }, 0, '', '', '', '', '')""")
        dbUnitHelper.context.sql.execute("""INSERT INTO waiting_event
    (tenantid, id, kind, eventType, messageName, signalName, errorCode, processName, flowNodeName, flowNodeDefinitionId, subProcessId, processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, flowNodeInstanceId, relatedActivityInstanceId, locked, active, progress, correlation1, correlation2, correlation3, correlation4, correlation5)
    VALUES(1, 2, 'SIGNAL', '', '', '', '', '', '', 0, 0, 9999999999999, 0, 0, 0, 0, ${dbUnitHelper.falseValue()}, ${
            dbUnitHelper.falseValue()
        }, 0, '', '', '', '', '')""")

    }

    def cleanup() {
        cleanTables()
    }

    def cleanTables() {
        dbUnitHelper.dropTables(["waiting_event", "process_definition"] as String[])
    }

    @Unroll
    def "should drop orphan waiting events"() {
        when:
        new DropOrphanWaitingEvents().execute(migrationContext)

        def rows = migrationContext.sql.rows("SELECT processdefinitionid FROM waiting_event")

        then:
        rows.size() == 1
        rows.get(0)."processdefinitionid" == 123456789000;

    }

}
