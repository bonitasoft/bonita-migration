/*
 * Copyright (C) 2019 Bonitasoft S.A.
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
 */
package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class AddCreationDateOnMessageInstanceIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()

    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.9.0")
        dbUnitHelper.createTables("7_9_0/message_couples")
    }

    def cleanup() {
        dbUnitHelper.dropTables(['message_instance', 'waiting_event'] as String[])
    }

    def "should add column 'creationDate' on table 'message_instance' with current datetime value"() {
        given:
        long initDate = System.currentTimeMillis()
        migrationContext.sql.execute("""
INSERT INTO message_instance (tenantid, id, messageName, targetProcess, targetFlowNode, locked, handled, processDefinitionId)
VALUES (1, 3, 'msg', 'myProcess', 'myFlownode', ${dbUnitHelper.falseValue()}, ${dbUnitHelper.falseValue()}, 1)
""")

        when:
        new AddCreationDateOnMessageInstance().execute(migrationContext)

        then:
        dbUnitHelper.hasColumnOnTable("message_instance", "creationDate")

        long endDate = System.currentTimeMillis()
        def res = migrationContext.sql.firstRow("SELECT creationDate from message_instance")
        res.creationDate > initDate
        res.creationDate < endDate
    }

}
