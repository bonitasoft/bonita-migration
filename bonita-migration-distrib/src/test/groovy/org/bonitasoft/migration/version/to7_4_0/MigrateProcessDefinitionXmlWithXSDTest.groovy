package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.core.Logger
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
    }

}
