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
 * @author Laurent Leseigneur
 */
class MigrateProcessDefXmlIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        migrationContext.setVersion("7.2.0")
        cleanTables()
        dbUnitHelper.createTables("7_2_0/process_content", "process_content")
    }

    def cleanup() {
        cleanTables()
    }

    def cleanTables() {
        dbUnitHelper.dropTables(["process_content"] as String[])
    }

    def "should migrate call activity with contract mapping rename nodes"() {
        setup:
        def xmlSlurper = new XmlSlurper();
        def design715 = this.class.getClassLoader().getResource("xml/process-design_7.1.5.xml").text;
        dbUnitHelper.context.sql.execute(
                """
            INSERT INTO process_content (tenantid, id, content)
            VALUES(5, 3, ${design715})
            """
        )

        when:
        new MigrateProcessDefXml().execute(migrationContext)

        then:
        def rows = dbUnitHelper.context.sql.rows("SELECT tenantid, id, content FROM process_content")

        def xmlContent = rows.get(0)["content"]
        def returnedXml = xmlSlurper.parseText(xmlContent)
        println xmlContent
        def returnChildren = returnedXml.'**'.findAll { node ->
            node.parent().name() == 'contractInput' && node.name() == 'input' && node.children().size() > 0
        }

        returnChildren.size() == 5
    }

}
