/*
 * Copyright (C) 2017 Bonitasoft S.A.
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

import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class MigrateProcessDefXmlTest extends Specification {

    def context = Mock(MigrationContext)

    def logger = Mock(Logger)

    def sql = Mock(Sql)

    def databaseHelper = Mock(DatabaseHelper)

    private MigrateProcessDefXml migrateProcessDefXml

    XmlParser xmlParser

    def setup() {
        databaseHelper.sql >> sql
        context.databaseHelper >> databaseHelper
        context.logger >> logger

        migrateProcessDefXml = new MigrateProcessDefXml()
        xmlParser = migrateProcessDefXml.xmlParser
    }

    @Unroll
    def "should remove flowNodes xml element in #fileName definition"(fileName) {
        setup:
        def xmlText = this.class.getClassLoader().getResource("to7_2_0/${fileName}").text
        def rootNode = xmlParser.parseText(xmlText)

        when:
        migrateProcessDefXml.removeFlowNodes(rootNode)

        then:
        def returnChildren = rootNode.'**'.findAll { node ->
            node.name() == 'flowNodes'
        }
        returnChildren.size() == 0

        def migratedXmlContentAsString = migrateProcessDefXml.getContent(rootNode)
        def expectedXml = this.class.getClassLoader().getResource("to7_2_0/expected-${fileName}").text
        def expectedXmlAsString = migrateProcessDefXml.getContent(xmlParser.parseText(expectedXml))
        println migratedXmlContentAsString

        final List<Diff> allDifferences = new DetailedDiff(XMLUnit.compareXML(expectedXmlAsString, migratedXmlContentAsString))
                .getAllDifferences()
        assert allDifferences.size() == 0

        where:
        fileName << ["sub-process.xml", "main-process.xml", "flowNodes.xml"]

    }


}
