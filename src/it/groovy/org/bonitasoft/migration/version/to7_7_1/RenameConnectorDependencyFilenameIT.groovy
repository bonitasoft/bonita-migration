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
package org.bonitasoft.migration.version.to7_7_1

import org.bonitasoft.migration.DBUnitHelper
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class RenameConnectorDependencyFilenameIT extends Specification {

    def dbUnitHelper = DBUnitHelper.getInstance()
    def migrationContext = dbUnitHelper.context

    def migrationStep = new RenameConnectorDependencyFilename()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_7_1/dependency")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["dependencymapping", "dependency"] as String[])
    }

    def "should remove PROCESS definition ID prefix in dependency FILENAME column"() {
        given:
        def content = "some binary content"

        migrationContext.sql.execute("""
INSERT INTO dependency (tenantid, id,  name,                                 description, filename,                             value_)
                VALUES (1,        302, '5335973144527898509_commons-io.jar', ${null},     '5335973144527898509_commons-io.jar', $content.bytes )
""")
        // name should not be changed, as the query should exclude filename's not starting with PROCESS_ID_xxxxxxxxxx:
        migrationContext.sql.execute("""
INSERT INTO dependency (tenantid, id, name,                             description, filename,     value_)
                VALUES (2,       306, '3354875451256512551_hsqldb.jar', ${null},     'hsqldb.jar', $content.bytes )
""")
        // name should not be changed, as the query should exclude artifacts with type different from 'PROCESS':
        migrationContext.sql.execute("""
INSERT INTO dependency (tenantid, id, name,                             description, filename,                         value_)
                VALUES (2,       310, '3354875451256512551_custom.jar', ${null},     '3354875451256512551_custom.jar', $content.bytes )
""")

        migrationContext.sql.execute("INSERT INTO dependencymapping (tenantid, id, artifactid, artifacttype, dependencyid)" +
                "                       VALUES (1, 100, 5335973144527898509, 'PROCESS', 302)")
        migrationContext.sql.execute("INSERT INTO dependencymapping (tenantid, id, artifactid, artifacttype, dependencyid)" +
                "                       VALUES (2, 101, 3354875451256512551, 'PROCESS', 306)")
        migrationContext.sql.execute("INSERT INTO dependencymapping (tenantid, id, artifactid, artifacttype, dependencyid)" +
                "                       VALUES (2, 102, 3354875451256512551, 'TENANT', 310)") // type == TENANT

        when:
        migrationStep.execute(migrationContext)

        then:
        List allRows = migrationContext.sql.rows("SELECT name, filename FROM dependency ORDER BY id")
        allRows.size() == 3

        allRows[0].name == '5335973144527898509_commons-io.jar'
        allRows[0].filename == 'commons-io.jar'

        allRows[1].name == '3354875451256512551_hsqldb.jar'
        allRows[1].filename == 'hsqldb.jar'

        allRows[2].name == '3354875451256512551_custom.jar'
        allRows[2].filename == '3354875451256512551_custom.jar'
    }

}
