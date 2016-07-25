/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.codehaus.groovy.runtime.InvokerHelper

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

/**
 * @author Emmanuel Duchastenier
 */
class MigrateProcessDefinitionXmlWithXSD extends MigrationStep {

    Logger logger

    @Override
    def execute(MigrationContext context) {
        this.logger = context.logger
        context.sql.eachRow("SELECT * FROM process_content") { processContent ->
            context.logger.debug("Process Definition before migration:\n$processContent.content")
            def String migratedXML = migrateProcessDefinitionXML(context.databaseHelper.getClobContent(processContent.content))
            context.logger.debug("Process Definition migrated to 7.4:\n$migratedXML")
            validateXML(migratedXML)
            context.sql.executeUpdate("UPDATE process_content SET content = $migratedXML WHERE tenantid=${processContent.tenantid} AND id=${processContent.id}")
        }
    }

    def boolean validateXML(String xmlContent) {
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new StreamSource(this.getClass().getResourceAsStream("/version/to_7_4_0/ProcessDefinition.xsd")))
                .newValidator()
                .validate(new StreamSource(new StringReader(xmlContent)))
        true
    }

    String migrateProcessDefinitionXML(def String processDefinitionXMLAsText) {
        def processDefinitionXml = new XmlParser().parseText(updateBusinessDataDefinitionTag(updateXmlNamespace(processDefinitionXMLAsText)))
        applyChangesOnXml(processDefinitionXml)
        return getContent(processDefinitionXml)
    }

    protected applyChangesOnXml(Node processDefinitionXml) {
        addUnderscoreToAllIDs(processDefinitionXml)
        updateActorsAndRefToInitiator(processDefinitionXml)
    }

    def String updateBusinessDataDefinitionTag(String processDefinitionXMLAsText) {
        processDefinitionXMLAsText.replaceAll("BusinessDataDefinition", "businessDataDefinition")
    }

    def addUnderscoreToAllIDs(Node processDefinitionXml) {
        processDefinitionXml.breadthFirst().findAll { node ->
            node instanceof Node && (node.name() == 'outgoingTransition' || node.name() == 'incomingTransition' || node.name() == 'defaultTransition')
        }.each { Node node ->
            node.setValue('_' + node.text())
        }
        def nodesWithId = processDefinitionXml.breadthFirst().findAll { node ->
            node instanceof Node && node?.@id
        } as Node[]
        List<String> alreadySeenIds = [];
        nodesWithId.each { node ->
            String idValue = node.attributes().remove 'id'
            if (alreadySeenIds.contains(idValue)) {
                logger.debug "$idValue already seen..."
                idValue = generateId()
                logger.debug "replacing it by $idValue"
            }
            alreadySeenIds.add(idValue)
            node.attributes().put('id', '_' + idValue)
        }
        processDefinitionXml.breadthFirst().findAll { node ->
            node instanceof Node && node.name() == 'transition'
        }.each { Node node ->
            node.attributes().put('source', '_' + (node.attributes().remove('source') as String))
            node.attributes().put('target', '_' + (node.attributes().remove('target') as String))
        }
    }

    String generateId() {
        Math.abs(UUID.randomUUID().getLeastSignificantBits())
    }

    public String getContent(Node processDefinitionXml) {
        def writer = new StringWriter()
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")

        def printer = new XmlNodePrinter(new PrintWriter(writer)) {
            protected void printSimpleItem(Object value) {
                if (!preserveWhitespace) printLineBegin();
                out.print(InvokerHelper.toString(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;'));
                if (!preserveWhitespace) printLineEnd();
            }
        }
        printer.setPreserveWhitespace(true)
        printer.setExpandEmptyElements(false)
        printer.print(processDefinitionXml)

        writer.toString()
    }

    @Override
    String getDescription() {
        return "Update process definition xml to the new 7.4 format"
    }


    def updateActorsAndRefToInitiator(Node processDefinitionXml) {
        def actors = processDefinitionXml.breadthFirst().findAll {
            it instanceof Node && it.name() == "actor"
        } as Node[]
        actors.each { actor ->
            def _id = "_" + generateId()
            actor.attributes().put("id", _id);
            def initiatorAttribute = actor.attribute("initiator")
            if (initiatorAttribute != null && initiatorAttribute == 'true') {
                def init = processDefinitionXml.find { it.name() == "actorInitiator" } as Node
                init.setValue(_id) // Add the reference ID of the actor ID who is initiator
            }
        }
    }

    def String updateXmlNamespace(String processDefinitionXMLAsText) {
        processDefinitionXMLAsText.replaceAll("http://www.bonitasoft.org/ns/process/client/\\d\\.\\d", "http://www.bonitasoft.org/ns/process/client/7.4")
                .replaceAll("def:", "tns:")
                .replaceAll(" xmlns:def=", " xmlns:tns=")
    }

}
