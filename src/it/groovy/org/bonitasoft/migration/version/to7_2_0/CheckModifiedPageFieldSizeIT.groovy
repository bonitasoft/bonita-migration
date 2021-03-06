/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_2_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Elias Ricken de Medeiros
 */
class CheckModifiedPageFieldSizeIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.2.0")
        dbUnitHelper.createTables("7_2_0/page", "page")
    }

    def cleanup() {
        dbUnitHelper.dropTables(["page", "profileentry"] as String[])
    }

    def "should update page name and content name field size"() {

        when:
        new IncreasePageNameField().execute(migrationContext)

        and:
        dbUnitHelper.context.sql.execute(
                """
            INSERT INTO page
            (tenantid, id, name, displayname, description, installationdate, installedby, provided, lastmodificationdate, lastupdatedby, contentname, contenttype, processdefinitionid)
            VALUES(1, 3, 'custompage_01234546789012345467890123454678901234546789012345467890123454678901234546789012345467890123454678901234546789012345467890123454678901234546789',
             'Bootstrap default theme', 'Application theme based on bootstrap "Default" theme. (see http://bootswatch.com/default/)', 1444727084195, -1, ${
                    dbUnitHelper.falseValue()
                }, 1444727084195, -1,
              'custompage_01234546789012345467890123454678901234546789012345467890123454678901234546789012345467890123454678901234546789012345467890123454678901234546789.zip', 'theme', NULL)
            """
        )

        dbUnitHelper.context.sql.execute(
                """
            INSERT INTO profileentry
            (tenantid, id, profileid, name, description, parentid, index_, type, page, custom)
            VALUES(1, 3, 1, 'Processes', 'Manage processes', 0, 4, 'link',
            'custompage_01234546789012345467890123454678901234546789012345467890123454678901234546789012345467890123454678901234546789012345467890123454678901234546789', ${
                    dbUnitHelper.falseValue()
                })
             """
        )

        then:
        dbUnitHelper.context.sql.rows("SELECT * FROM page").size() == 1
        dbUnitHelper.context.sql.rows("SELECT * FROM profileentry").size() == 1

    }

    def "should page name be not null"() {

        when:
        new IncreasePageNameField().execute(migrationContext)

        and:
        dbUnitHelper.context.sql.execute(
                """
            INSERT INTO page
            (TENANTID, ID, NAME, DISPLAYNAME, DESCRIPTION, INSTALLATIONDATE, INSTALLEDBY, PROVIDED, LASTMODIFICATIONDATE, LASTUPDATEDBY, CONTENTNAME, CONTENTTYPE, PROCESSDEFINITIONID)
            VALUES(1, 3, null,
             'Bootstrap default theme', 'Application theme based on bootstrap "Default" theme. (see http://bootswatch.com/default/)', 1444727084195, -1, ${
                    dbUnitHelper.falseValue()
                }, 1444727084195, -1,
              'bonita-bootstrap-default-theme.zip', 'theme', NULL)
            """
        )

        then:
        def ex = thrown(Exception)
        ex.message.toLowerCase().contains("null")

    }

}
