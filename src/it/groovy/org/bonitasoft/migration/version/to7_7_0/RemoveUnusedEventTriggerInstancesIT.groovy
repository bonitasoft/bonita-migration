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

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class RemoveUnusedEventTriggerInstancesIT extends Specification {

    def logger = new Logger()
    def migrationContext = new MigrationContext(logger: logger)
    def dbUnitHelper = new DBUnitHelper(migrationContext)

    def migrationStep = new RemoveUnusedEventTriggerInstances()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_7_0/event_triggers")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["event_trigger_instance"] as String[])
    }

    def "should remove event trigger instances"() {
        given:
        migrationContext.sql.execute(
                "INSERT INTO event_trigger_instance (tenantId, id, kind, eventInstanceId) VALUES" +
                        " (1,1,'throwSignal',-1)")
        migrationContext.sql.execute(
                "INSERT INTO event_trigger_instance (tenantId, id, kind, eventInstanceId) VALUES" +
                        " (1,2,'throwMessage',-1)")
        migrationContext.sql.execute(
                "INSERT INTO event_trigger_instance (tenantId, id, kind, eventInstanceId) VALUES" +
                        " (1,3,'timer',-1)")

        when:
        migrationStep.execute(migrationContext)

        then:
        def allRows = migrationContext.sql.dataSet("event_trigger_instance").rows()
        allRows.size() == 1
        allRows.any { row -> row.id == 3 }
        def expectedColumns =
                ["tenantid",
                 "id",
                 "eventinstanceid",
                 "eventinstancename",
                 "executiondate",
                 "jobtriggername"]
        def realColumns = (allRows.first() as Map).keySet()
        realColumns.collect { it.toLowerCase() }.containsAll(expectedColumns)
        realColumns.size() == expectedColumns.size()
    }

}