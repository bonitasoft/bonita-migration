package org.bonitasoft.migration.version.to7_4_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import org.bonitasoft.migration.version.to7_2_0.MigrateProcessDefXml
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Specification
import spock.lang.Unroll

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * @author Emmanuel Duchastenier
 */
class MigrateProcessDefinitionXmlWithXSDTest extends Specification {

    def logger = Mock(Logger)

    def context = Mock(MigrationContext)

    def sql = Mock(Sql)

    def databaseHelper = Mock(DatabaseHelper)

    MigrateProcessDefinitionXmlWithXSD migrationStep = new MigrateProcessDefinitionXmlWithXSD(logger: logger)

    MigrateProcessDefXml migrateProcessDefXml720 = new MigrateProcessDefXml()

    def setup() {
        context.logger >> logger
        context.databaseHelper >> databaseHelper
        context.sql >> sql
    }

    @Unroll
    "should migration of #xmlFile generate valid XML against 7.4 ProcessDefinition.xsd"(String xmlFile) {
        setup:
        migrationStep.logger = logger
        def xmlBeforeMigration = new File(this.class.getResource("/to7_4_0/$xmlFile").file).text

        when:
        def xmlAfterMigration = migrationStep.migrateProcessDefinitionXML(xmlBeforeMigration)

        then:
        migrationStep.migratedXmlIsValidAgainstXSD xmlAfterMigration

        where:
        xmlFile << ["original.xml",
                    "process10.xml",
                    "process11.xml",
                    "process12.xml",
                    "process24.xml",
                    "process26.xml",
                    "process101.xml",
                    "process102.xml",
                    "process103.xml",
                    "process201.xml",
                    "process202.xml",
                    "process301.xml",
                    "process303.xml",
                    "process304.xml",
                    "to_740.xml",
                    "626_740_process_def.xml",
                    "733_start_event_description.xml",
                    "BBPMC-452/process-design1.xml",
                    "BBPMC-452/process-design2.xml",
                    "BBPMC-452/process-design3.xml",
                    "BBPMC-452/process-design4.xml",
                    "BBPMC-452/process-design5.xml",
                    "BBPMC-452/process-design6.xml",
                    "bdm_multiple.xml"
        ]
    }

    @Unroll
    def "should migrate #givenXml to expected content"(givenXml, expectedXml) {

        setup:
        def migratedStringWriter = new StringWriter()
        def factory = TransformerFactory.newInstance()
        def transformer = factory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("/version/to_7_4_0/ProcessDefinition.xsl")))

        when:
        transformer.transform(new StreamSource(this.getClass().getResourceAsStream("/to7_4_0/$givenXml")), new StreamResult(migratedStringWriter))

        then:
        def expectedXmlText = new File(this.class.getResource("/to7_4_0/$expectedXml").file).text
        XMLUnit.setIgnoreWhitespace(true)
        def migratedProcessDefinition = migratedStringWriter.toString()
        println """
*****************************
migrated process definition :
*****************************
$migratedProcessDefinition
"""
        final List<Diff> allDifferences = new DetailedDiff(XMLUnit.compareXML(migratedProcessDefinition, expectedXmlText))
                .getAllDifferences()
        if (allDifferences.size() > 0) {
            allDifferences.each {
                diff ->
                    //ignore @id attribute values generated at migration time
                    def description = diff.getProperties().get("description")
                    switch (description) {
                        case "attribute value":
                            assert (diff.getProperties().get("controlNodeDetail") as org.custommonkey.xmlunit.NodeDetail).node.name == "id"
                            assert (diff.getProperties().get("testNodeDetail") as org.custommonkey.xmlunit.NodeDetail).node.name == "id"
                            break
                        case "text value":
                            //attribute id is here a text value
                            def textNodeIds = ["actorInitiator"]
                            def controlNodeName = (diff.getProperties().get("controlNodeDetail") as org.custommonkey.xmlunit.NodeDetail).node.parentNode
                            def testNodeName = (diff.getProperties().get("testNodeDetail") as org.custommonkey.xmlunit.NodeDetail).node.parentNode
                            assert controlNodeName.name == testNodeName.name
                            // this assertion is designed to display test node content when node name is not in expected textNodeIds
                            assert textNodeIds.contains(testNodeName.name) || controlNodeName.textContent == testNodeName.textContent
                            break
                        default:
                            def testNodeName = (diff.getProperties().get("testNodeDetail") as org.custommonkey.xmlunit.NodeDetail).node
                            println "ERROR: $diff not expected! ***\n${testNodeName.textContent}\n***"
                            assert diff == null
                    }

            }
        }

        where:
        givenXml                        | expectedXml
        "original.xml"                  | "expected/migrated.xml"
        "BBPMC-452/process-design1.xml" | "expected/BBPMC-452/process-design1.xml"
        "BBPMC-452/process-design2.xml" | "expected/BBPMC-452/process-design2.xml"
        "BBPMC-452/process-design3.xml" | "expected/BBPMC-452/process-design3.xml"
        "BBPMC-452/process-design4.xml" | "expected/BBPMC-452/process-design4.xml"
        "BBPMC-452/process-design5.xml" | "expected/BBPMC-452/process-design5.xml"
        "BBPMC-452/process-design6.xml" | "expected/BBPMC-452/process-design6.xml"
        "bdm_multiple.xml"              | "expected/bdm_multiple.xml"

    }

    def "should report error when process xsd validation fails"() {
        when:
        migrationStep.migrateProcessContent(context, "not XML", 1L, 2L)
        migrationStep.migrateProcessContent(context, "still not XML", 1L, 3L)

        then:
        migrationStep.errors.size() == 2
    }

    def "should throw exception and log error when process xsd validation fails"() {
        setup:
        def rows = [[
                            content : "not xml",
                            tenantid: 1L,
                            id      : 2L
                    ],
                    [
                            content : "still not xml",
                            tenantid: 1L,
                            id      : 3L
                    ]]
        sql.rows(MigrateProcessDefinitionXmlWithXSD.GET_ALL_PROCESS_CONTENT) >> rows

        when:
        migrationStep.execute(context)

        then:
        migrationStep.errors.size() == 2
        2 * migrationStep.logger.error(_)
        thrown(IllegalStateException.class)
    }

    @Unroll
    def "migrate and validate #xmlFile from v7.0.0 "(String xmlFile) {
        setup:
        migrationStep.logger = logger
        def xmlBeforeMigration = new File(this.class.getResource("/to7_4_0/v7_0_0/$xmlFile").file).text

        when:

        def xmlAfter720Migration = migrateProcessDefXml720.migrateProcessDefinitionXML(xmlBeforeMigration)
        def xmlAfter720MigrationNode = new XmlParser().parseText(xmlAfter720Migration)
        migrateProcessDefXml720.renameCallActivityContractInputMapping(xmlAfter720MigrationNode)
        def xmlAfter730Migration = migrateProcessDefXml720.getContent(xmlAfter720MigrationNode)
        def xmlAfterMigration = migrationStep.migrateProcessDefinitionXML(xmlAfter730Migration)

        then:

        println """
==================
xmlAfterMigration:
==================
$xmlAfterMigration
"""
        migrationStep.migratedXmlIsValidAgainstXSD xmlAfterMigration

        where:
        xmlFile << ["process-design-01.xml",
                    "process-design-02.xml",
                    "process-design-03.xml",
                    "process-design-04.xml",
                    "process-design-05.xml"]
    }
}
