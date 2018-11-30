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
package org.bonitasoft.migration.version.to7_3_0

import oracle.sql.CLOB
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class MigrateProcessDefXmlCallActivityIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

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

    @Unroll
    def "should migrate call activity with contract mapping rename nodes for #fileName"(def fileName, def id) {
        setup:
        def xmlSlurper = new XmlSlurper();
        def design = this.class.getClassLoader().getResource("xml/${fileName}").text;
        dbUnitHelper.context.sql.execute(
                """
            INSERT INTO process_content (tenantid, id, content)
            VALUES(5, ${id}, ${design})
            """
        )

        when:
        new MigrateProcessDefXmlCallActivity().execute(migrationContext)


        then:
        def rows = dbUnitHelper.context.sql.rows("SELECT tenantid, id, content FROM process_content")

        def xmlContent = rows.get(0)["content"]
        def returnedXml
        if (MigrationStep.DBVendor.ORACLE.equals(migrationContext.dbVendor)) {
            returnedXml = xmlSlurper.parseText(((CLOB) xmlContent).asciiStream.text)
        } else {
            returnedXml = xmlSlurper.parseText(xmlContent)
        }
        def returnChildren = returnedXml.'**'.findAll { node ->
            node.parent().name() == 'contractInput' && node.name() == 'input' && node.children().size() > 0
        }

        returnChildren.size() == 5

        where:
        fileName                   | id
        "process-design_7.1.5.xml" | 2
        "process-design_7.2.0.xml" | 3

    }

}
