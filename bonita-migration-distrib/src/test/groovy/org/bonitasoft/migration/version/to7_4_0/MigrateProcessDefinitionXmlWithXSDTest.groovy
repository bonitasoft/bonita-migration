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

        when:
        def xml = migrationStep.migrateProcessDefinitionXML(new File(this.class.getResource("/to7_4_0/$xmlFile").file).text)

        then:
        migrationStep.validateXML xml

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
                    "to_740.xml"
        ]


    }

    def "should migrate to expected content"() {

        setup:
        def migratedStringWriter = new StringWriter()
        def factory = TransformerFactory.newInstance()
        def transformer = factory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("/version/to_7_4_0/ProcessDefinition.xsl")))

        when:
        transformer.transform(new StreamSource(this.getClass().getResourceAsStream("/to7_4_0/original.xml")), new StreamResult(migratedStringWriter))

        then:
        def expectedXml = new File(this.class.getResource("/to7_4_0/expectedMigrated.xml").file).text
        XMLUnit.setIgnoreWhitespace(true)
        def migratedProcessDefinition = migratedStringWriter.toString()
        println """
*****************************
migrated process definition :
*****************************
$migratedProcessDefinition
"""
        final List<Diff> allDifferences = new DetailedDiff(XMLUnit.compareXML(migratedProcessDefinition, expectedXml))
                .getAllDifferences()
        allDifferences.each {
            diff ->
                //ignore @id attribute values generated at migration time
                (diff.getProperties().get("controlNodeDetail") as org.custommonkey.xmlunit.NodeDetail).xpathLocation.endsWith("@id")
                (diff.getProperties().get("testNodeDetail") as org.custommonkey.xmlunit.NodeDetail).xpathLocation.endsWith("@id")
        }
    }

}
