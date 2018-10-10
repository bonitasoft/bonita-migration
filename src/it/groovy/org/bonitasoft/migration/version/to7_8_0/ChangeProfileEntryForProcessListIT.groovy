/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_8_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

class ChangeProfileEntryForProcessListIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)

    def migrationStep = new ChangeProfileEntryForProcessList()

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.8.0")
        dbUnitHelper.createTables("7_8_0/profileentry", "profileentry")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["profileentry"] as String[])
    }

    def "should rename profileentry where page value is processlistinguser "() {
        given:
        migrationContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 1, 1, 'Processes', 'Manage processes', 0, 4, 'link', 'processlistinguser', ${dbUnitHelper.falseValue()})
""")
        migrationContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 2, 1, 'Tasks', 'Manage tasks', 0, 2, 'link', 'tasklistinguser', ${dbUnitHelper.falseValue()})
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List updatedRows = migrationContext.sql.rows("SELECT page FROM profileentry WHERE page ='custompage_processlistBonita'")
        updatedRows.size() == 1
    }

    def "should rename all profileentry where page value processlistinguser in two tenants"() {
        given:
        migrationContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 1 ,1, 'Processes', 'Manage processes', 0, 4, 'link', 'processlistinguser', ${dbUnitHelper.falseValue()})
""")
        migrationContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (2, 2, 1, 'Processes', 'Manage processes', 0, 2, 'link', 'processlistinguser', ${dbUnitHelper.falseValue()})
""")
        migrationContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 3, 1, 'Tasks', 'Manage tasks', 0, 2, 'link', 'tasklistinguser', ${dbUnitHelper.falseValue()})
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List updatedRows = migrationContext.sql.rows("SELECT tenantId FROM profileentry WHERE page ='custompage_processlistBonita'  ORDER BY tenantId")
        updatedRows.size() == 2

        updatedRows[0].tenantId == 1
        updatedRows[1].tenantId == 2
    }
}
