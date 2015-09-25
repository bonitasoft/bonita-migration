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

/**
 * @author Laurent Leseigneur
 */
class MigrateProcessDefXml extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.sql.eachRow("SELECT * FROM process_content"){ processContent ->
            def String migratedXML = migrateProcessDefinitionXML(processContent.content)
            context.sql.executeUpdate("UPDATE process_content SET content = $migratedXML WHERE tenantid=${processContent.tenantid} AND id=${processContent.id}")
        }
    }

    String migrateProcessDefinitionXML(def String processDefinitionXML) {
        processDefinitionXML = removeXmlns(processDefinitionXML)
        processDefinitionXML = changeActorInitiator(processDefinitionXML)
        processDefinitionXML = removeDependencies(processDefinitionXML)
        processDefinitionXML = removeFlowNodes(processDefinitionXML)
        processDefinitionXML = changeIdRef(processDefinitionXML)
        return processDefinitionXML
    }

    @Override
    String getDescription() {
        return "Update process definition xml to the new format"
    }


    private String changeActorInitiator(String text){
        def iterator = text.indexOf("<actorInitiator name=")
        if (iterator == -1) {
            return text
        }
        def iterator2 = iterator
        while (text[iterator2] != '"'){
            iterator2++
        }
        def iterator3 = iterator2+1
        while (text[iterator3] != '"'){
            iterator3++
        }
        def textPart1 = text.subSequence(0, iterator)
        def NomActeur = text.subSequence(iterator2+1,iterator3)
        return textPart1 + "<actorInitiator>" + NomActeur + "</actorInitiator>" + text.subSequence(iterator3 + 3, text.length())
    }
    private String removeXmlns(String text) {
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

    private String removeDependencies(String text) {
        def iterator = text.indexOf("<dependencies>")
        def iterator2 = text.indexOf("</dependencies>")
        if (iterator == -1) {
            return text
        }
        def textPart1 = text.subSequence(0, iterator - 5)
        def textPart2 = text.subSequence(iterator2 + 16, text.length())
        def textPart3 = text.subSequence(iterator + 12, iterator2)
        return textPart1 + textPart3 + textPart2
    }

    private String removeFlowNodes(String text) {
        def iterator = text.indexOf("<flowNodes>")
        def newText = text
        def textPart1
        def textPart2
        while (iterator != -1) {
            textPart1 = newText.subSequence(0, iterator - 4)
            textPart2 = newText.subSequence(iterator + 12, newText.length())
            newText = textPart1 + textPart2
            iterator = newText.indexOf("<flowNodes>")
        }
        iterator = newText.indexOf("</flowNodes>")
        while (iterator != -1) {
            textPart1 = newText.subSequence(0, iterator - 4)
            textPart2 = newText.subSequence(iterator + 13, newText.length())
            newText = textPart1 + textPart2
            iterator = newText.indexOf("</flowNodes>")
        }
        return newText
    }

    private String changeIdRef(String text) {
        def textLocal = text
        while (textLocal.contains("<incomingTransition idref=") | textLocal.contains("<outgoingTransition idref=") | textLocal.contains("<defaultTransition idref=")) {
            if (textLocal.contains("<incomingTransition idref=")) {
                def iterator = textLocal.indexOf("<incomingTransition idref=")
                def iterator3 = iterator + "<incomingTransition idref=".toString().length() + 1
                def iterator2 = iterator3
                while (textLocal[iterator2] != '>') {
                    iterator2++
                }
                def Id = textLocal.subSequence(iterator3, iterator2 - 2)
                def textPart1 = textLocal.subSequence(0, iterator)
                def textPart2 = textLocal.subSequence(iterator2, textLocal.length())
                textLocal = textPart1 + "<incomingTransition>" + Id + "</incomingTransition" + textPart2
            }
            if (textLocal.contains("<outgoingTransition idref=")) {
                def iterator = textLocal.indexOf("<outgoingTransition idref=")
                def iterator3 = iterator + "<outgoingTransition idref=".toString().length() + 1
                def iterator2 = iterator3
                while (textLocal[iterator2] != '>') {
                    iterator2++
                }
                def Id = textLocal.subSequence(iterator3, iterator2 - 2)
                def textPart1 = textLocal.subSequence(0, iterator)
                def textPart2 = textLocal.subSequence(iterator2, textLocal.length())
                textLocal = textPart1 + "<outgoingTransition>" + Id + "</outgoingTransition" + textPart2
            }
            if (textLocal.contains("<defaultTransition idref=")){
                def iterator = textLocal.indexOf("<defaultTransition idref=")
                def iterator3 = iterator +"<defaultTransition idref=".toString().length() + 1
                def iterator2 = iterator3
                while (textLocal[iterator2] != '>') {
                    iterator2++
                }
                def Id = textLocal.subSequence(iterator3, iterator2 - 2)
                def textPart1 = textLocal.subSequence(0, iterator)
                def textPart2 = textLocal.subSequence(iterator2, textLocal.length())
                textLocal = textPart1 +"<defaultTransition>" + Id + "</defaultTransition" + textPart2
            }
        }
        return textLocal
    }

}
