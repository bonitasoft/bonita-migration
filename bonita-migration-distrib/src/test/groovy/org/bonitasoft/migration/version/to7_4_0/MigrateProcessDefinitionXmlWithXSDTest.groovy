package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.core.Logger
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class MigrateProcessDefinitionXmlWithXSDTest extends Specification {

    def logger = Mock(Logger)
    def MigrateProcessDefinitionXmlWithXSD migrationStep = new MigrateProcessDefinitionXmlWithXSD(logger: logger)

    def setup() {
    }

    def "migrateProcessDefinitionXML should generate valid XML"() {
        given:
        migrationStep.logger = logger

        when:
        def xml = migrationStep.migrateProcessDefinitionXML(new File(this.class.getResource("/to7_4_0/original.xml").file).text)

        then:
        migrationStep.validateXML xml

        def expectedXml = new File(this.class.getResource("/to7_4_0/expectedMigrated.txt").file).text
        final List<Diff> allDifferences = new DetailedDiff(XMLUnit.compareXML(xml, expectedXml))
                .getAllDifferences();
        allDifferences.each {
            diff ->
                //ignore @id attribute generated at migration time
                (diff.getProperties().get("controlNodeDetail") as org.custommonkey.xmlunit.NodeDetail).xpathLocation.endsWith("@id")
                (diff.getProperties().get("testNodeDetail") as org.custommonkey.xmlunit.NodeDetail).xpathLocation.endsWith("@id")
        }


    }

}
