/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to7_12_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class ChangeProfileEntryForInstallExportOrganizationIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def updateContext = new UpdateContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(updateContext)

    def updateStep = new ChangeProfileEntryForInstallExportOrganization()

    def setup() {
        dropTestTables()
        updateContext.setVersion("7.12.0")
        dbUnitHelper.createTables("7_12_0/profileentry", "profileentry")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["profileentry"] as String[])
    }

    def "should rename profileentry where page value is custompage_installExportOrganizationBonita "() {
        given:
        updateContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 1, 1, 'Install Organization', 'Install Export Organization', 0, 4, 'link', 'custompage_installExportOrganizationBonita', ${dbUnitHelper.falseValue()})
""")
        updateContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 2, 1, 'Tasks', 'Manage tasks', 0, 2, 'link', 'tasklistinguser', ${dbUnitHelper.falseValue()})
""")
        when:
        updateStep.execute(updateContext)

        then:
        List updatedRows = updateContext.sql.rows("SELECT page FROM profileentry WHERE page ='custompage_adminInstallExportOrganizationBonita'")
        updatedRows.size() == 1
    }

    def "should rename all profileentry where page value custompage_installExportOrganizationBonita in two tenants"() {
        given:
        updateContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 1 ,1, 'Install Organization', 'Install Export Organization', 0, 4, 'link', 'custompage_installExportOrganizationBonita', ${dbUnitHelper.falseValue()})
""")
        updateContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (2, 2, 1, 'Install Organization', 'Install Export Organization', 0, 2, 'link', 'custompage_installExportOrganizationBonita', ${dbUnitHelper.falseValue()})
""")
        updateContext.sql.execute("""
            INSERT INTO profileentry (tenantid, id,  profileid, name, description, parentid, index_, type, page, custom)
              VALUES (1, 3, 1, 'Tasks', 'Manage tasks', 0, 2, 'link', 'tasklistinguser', ${dbUnitHelper.falseValue()})
""")
        when:
        updateStep.execute(updateContext)

        then:
        List updatedRows = updateContext.sql.rows("SELECT tenantId FROM profileentry WHERE page ='custompage_adminInstallExportOrganizationBonita'  ORDER BY tenantId")
        updatedRows.size() == 2

        updatedRows[0].tenantId == 1
        updatedRows[1].tenantId == 2
    }
}
