/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class MigratePageIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.3.0")
        dbUnitHelper.dropTables(["business_app_menu",
                                 "business_app_page",
                                 "business_app",
                                 "page"] as String[])
        dbUnitHelper.createTables("7_3_0/page", "page")

        dbUnitHelper.context.sql.execute(
                """
            INSERT INTO page
            (tenantid, id, name, displayname, description, installationdate, installedby, provided, lastmodificationdate, lastupdatedby,
     contentname, contenttype, processdefinitionid)
            VALUES(1, 3, 'custompage_no_process',
             'custompage_no_process', 'desc', 1444727084195, -1, ${
                    dbUnitHelper.falseValue()
                }, 1444727084195, -1,
              'content_name.zip', 'page'
              , NULL)
            """
        )
        dbUnitHelper.context.sql.execute(
                """
            INSERT INTO page
            (tenantid, id, name, displayname, description, installationdate, installedby, provided, lastmodificationdate, lastupdatedby,
     contentname, contenttype, processdefinitionid)
            VALUES(1, 17, 'custompage_with_process',
             'custompage_no_process', 'desc', 1444727084195, -1, ${
                    dbUnitHelper.falseValue()
                }, 1444727084195, -1,
              'content_name.zip', 'page'
              , 456)
            """
        )
    }

    def cleanup() {
        dbUnitHelper.dropTables(["page"] as String[])
    }

    def "should 7.3.0 platform fields renamed"() {
        when:
        new MigratePageTable().execute(migrationContext)

        then:
        def rowNoProcess = dbUnitHelper.context.sql.firstRow("SELECT * FROM page p where p.name='custompage_no_process'")
        def rowWithProcess = dbUnitHelper.context.sql.firstRow("SELECT * FROM page p where p.name='custompage_with_process'")
        rowNoProcess['processdefinitionid'] == 0L
        rowWithProcess['processdefinitionid'] == 456L
        dbUnitHelper.hasUniqueKeyOnTable("page", "uk_page") == true
    }


}
