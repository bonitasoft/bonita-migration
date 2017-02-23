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

package org.bonitasoft.migration.version.to7_2_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
class MigrateProcessDefXml extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.sql.eachRow("SELECT * FROM process_content") { processContent ->
            context.logger.debug("Process Definition before migration:\n$processContent")
            def String migratedXML = migrateProcessDefinitionXML(context.databaseHelper.getClobContent(processContent.content))
            context.logger.debug("Process Definition migrated to 7.2:\n$migratedXML")
            context.sql.executeUpdate("UPDATE process_content SET content = $migratedXML WHERE tenantid=${processContent.tenantid} AND id=${processContent.id}")
        }
    }

    String migrateProcessDefinitionXML(def String processDefinitionXMLAsText) {
        def processDefinitionXml = new XmlParser().parseText(updateXmlNamespace(processDefinitionXMLAsText))
        applyChangesOnXml(processDefinitionXml)

        return getContent(processDefinitionXml)
    }

    protected applyChangesOnXml(Node processDefinitionXml) {
        removeBosVersion(processDefinitionXml)
        changeActorInitiator(processDefinitionXml)
        removeDependencies(processDefinitionXml)
        removeFlowNodes(processDefinitionXml)
        changeIdRef(processDefinitionXml)
        changeDescriptionAttributeToElement(processDefinitionXml)
        addNodeToWrapContractInputDefinitions(processDefinitionXml)
        renameCallActivityContractInputMapping(processDefinitionXml)
    }

    def renameCallActivityContractInputMapping(Node processDefinitionXml) {
        def list = processDefinitionXml.breadthFirst().findAll { node ->
            node instanceof Node && node.name() == "contractInputs" && node.parent().name() == "callActivity"
        } as Node[]
        list.each { contractInputs ->
            def callActivity = contractInputs.parent()
            def contractInput = new Node(callActivity, "contractInput", contractInputs.attributes())
            contractInputs.children().each { child ->
                new Node(contractInput, "input", child.attributes(), child.value())
            }
            callActivity.remove(contractInputs)
        }

    }

    def addNodeToWrapContractInputDefinitions(Node processDefinitionXml) {
        def list = processDefinitionXml.breadthFirst().findAll {
            it instanceof Node && it.name() == "inputDefinition"
        } as Node[]
        list.each {
            if (it.children().find { it.name() == "inputDefinitions" } == null) {
                def node = new Node(it, "inputDefinitions")
                it.append(node)
                (it.children().findAll { it.name() == "inputDefinition" } as Node[]).each { Node child ->
                    it.remove(child);
                    node.append(child)
                }
            }
        }
    }


    def void removeBosVersion(Node processDefinitionXml) {
        processDefinitionXml.attributes().remove "bos_version"
    }

    def void changeDescriptionAttributeToElement(Node processDefinitionXml) {
        def displayDescs = processDefinitionXml.breadthFirst().findAll {
            it instanceof Node && it.@displayDescription != null
        } as Node[]
        displayDescs.each { nodeWithDisplayDescription ->
            def displayDescription = nodeWithDisplayDescription.@displayDescription as String
            def node = new Node(nodeWithDisplayDescription, "displayDescription", displayDescription)
            nodeWithDisplayDescription.remove(node)
            nodeWithDisplayDescription.children().add(0, node);
            nodeWithDisplayDescription.attributes().remove "displayDescription";
        }
        def descs = processDefinitionXml.breadthFirst().findAll {
            it instanceof Node && it.@description != null
        } as Node[]
        descs.each { nodeWithDescription ->
            def description = nodeWithDescription.@description as String
            def node = new Node(nodeWithDescription, "description", description)
            nodeWithDescription.remove(node)
            nodeWithDescription.children().add(0, node);
            nodeWithDescription.attributes().remove "description"
        }
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
        return "Update process definition xml to the new format"
    }


    def static void changeActorInitiator(Node processDefinitionXml) {
        Node actorInitiator = processDefinitionXml.breadthFirst().find { Node it -> it.name() == "actorInitiator" } as Node
        if (actorInitiator != null) {
            def name = actorInitiator.@name;
            actorInitiator.setValue(name);
            actorInitiator.attributes().remove "name";
        }
    }

    def String updateXmlNamespace(String processDefinitionXMLAsText) {
        processDefinitionXMLAsText.replaceAll("http://www.bonitasoft.org/ns/process/client/\\d\\.\\d", "http://www.bonitasoft.org/ns/process/client/7.2")
                .replaceAll("<processDefinition", "<def:processDefinition")
                .replaceAll("</processDefinition", "</def:processDefinition")
                .replaceAll(" xmlns=", " xmlns:def=")
    }

    def static void removeDependencies(Node processDefinitionXml) {
        removeNode(processDefinitionXml, "dependencies")
    }

    private static void removeNode(Node processDefinitionXml, nodeName) {
        Node nodeToRemove = processDefinitionXml.breadthFirst().find { Node it -> it.name() == nodeName } as Node
        Node parent = nodeToRemove.parent()
        def nodeToRemoveNodeIndex = parent.children().indexOf(nodeToRemove)
        nodeToRemove.children().each { Node child ->
            parent.children().add(nodeToRemoveNodeIndex, child)
        }
        parent.children().remove(nodeToRemove)
    }

    def static void removeFlowNodes(Node processDefinitionXml) {
        removeNode(processDefinitionXml, "flowNodes")
    }

    def static void changeIdRef(Node processDefinitionXml) {
        def list = processDefinitionXml.breadthFirst().findAll {
            it instanceof Node && (it.name() == "incomingTransition" || it.name() == "outgoingTransition" || it.name() == "defaultTransition")
        } as Node[]
        list.each { transitionRef ->
            def idRef = transitionRef.@idref;
            transitionRef.setValue(idRef);
            transitionRef.attributes().remove "idref"
        }
    }

}
