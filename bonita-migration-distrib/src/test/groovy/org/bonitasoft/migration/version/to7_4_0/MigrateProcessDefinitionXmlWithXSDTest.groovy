package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.core.Logger
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
    def MigrateProcessDefinitionXmlWithXSD migrationStep = new MigrateProcessDefinitionXmlWithXSD(logger: logger)

    def setup() {
    }

    @Unroll
    def "should migration of #xmlFile generate valid XML against 7.4 ProcessDefinition.xsd"(String xmlFile) {
        setup:
        migrationStep.logger = logger
        def xmlBeforeMigration = new File(this.class.getResource("/to7_4_0/$xmlFile").file).text

        when:
        def xmlAfterMigration = migrationStep.migrateProcessDefinitionXML(xmlBeforeMigration)

        then:
        println """
******
 process before migration: 
******
$xmlBeforeMigration
================================================

******
 process after migration: 
******
$xmlAfterMigration  
================================================
"""

        migrationStep.validateXML xmlAfterMigration

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
                    "733_start_event_description.xml"
        ]


    }

    @Unroll
    def "should migrate #givenXml to expected content"(def givenXml,def expectedXml) {

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
                        assert (diff.getProperties().get("controlNodeDetail") as org.custommonkey.xmlunit.NodeDetail).node.parentNode.name == "actorInitiator"
                        assert (diff.getProperties().get("testNodeDetail") as org.custommonkey.xmlunit.NodeDetail).node.parentNode.name == "actorInitiator"
                        break
                    default:
                        println "ERROR: $diff not expected!"
                        assert diff == null

                }

        }

        where:
        givenXml             | expectedXml
        "original.xml"       | "expectedMigrated.xml"

    }

}
