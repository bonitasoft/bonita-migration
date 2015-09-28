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
 * @author Laurent Leseigneur
 */
class MigrateProcessDefXml extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.sql.eachRow("SELECT * FROM process_content") { processContent ->
            def String migratedXML = migrateProcessDefinitionXML(processContent.content)
            context.sql.executeUpdate("UPDATE process_content SET content = $migratedXML WHERE tenantid=${processContent.tenantid} AND id=${processContent.id}")
        }
    }

    String migrateProcessDefinitionXML(def String processDefinitionXMLAsText) {
        def processDefinitionXml = new XmlParser().parseText(processDefinitionXMLAsText)
        changeActorInitiator(processDefinitionXml)
        removeDependencies(processDefinitionXml)
        removeFlowNodes(processDefinitionXml)
        changeIdRef(processDefinitionXml)
        return removeXmlns(getContent(processDefinitionXml))
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

        def result = writer.toString()
        return result;
    }

    @Override
    String getDescription() {
        return "Update process definition xml to the new format"
    }


    def static void changeActorInitiator(Node processDefinitionXml) {
        Node actorInitiator = processDefinitionXml.breadthFirst().find { Node it -> it.name().getLocalPart() == "actorInitiator" } as Node
        if(actorInitiator != null){
            def name = actorInitiator.@name;
            actorInitiator.setValue(name);
            actorInitiator.@name = null;
        }
    }

    def static String removeXmlns(String text) {
        //FIXME fix xmlns
        def iterator = text.indexOf("xmlns")
        def iterator2 = iterator
        if (iterator == -1) {
            return text
        }
        while (text[iterator2] != ' ') {
            iterator2++
        }
        def textPart1 = text.subSequence(0, iterator)
        def textPart2 = text.subSequence(iterator2, text.length() - 1)
        return textPart1 + textPart2
    }

    def static void removeDependencies(Node processDefinitionXml) {
        removeNode(processDefinitionXml, "dependencies")
    }

    private static void removeNode(Node processDefinitionXml, nodeName) {
        Node nodeToRemove = processDefinitionXml.breadthFirst().find { Node it -> it.name().getLocalPart() == nodeName } as Node
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
            it instanceof Node && (it.name().getLocalPart() == "incomingTransition" || it.name().getLocalPart() == "outgoingTransition" || it.name().getLocalPart() == "defaultTransition")
        }
        list.each { transitionRef ->
            def idRef = transitionRef.@idref;
            transitionRef.setValue(idRef);
            transitionRef.@idref = null;
        }
    }

}
