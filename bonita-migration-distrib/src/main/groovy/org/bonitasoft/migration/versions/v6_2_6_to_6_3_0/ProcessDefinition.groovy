/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 **
 * @since 6.2
 */
package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0

import groovy.transform.ToString;
import org.codehaus.groovy.runtime.InvokerHelper


/**
 * @author Baptiste Mesta
 *
 */
@ToString
public class ProcessDefinition {


    def Node processDefinitionXml;

    public ProcessDefinition(String content, boolean isServer) {
        if (isServer) {
            content = content.replace("http://www.bonitasoft.org/ns/process/server/6.0", "http://www.bonitasoft.org/ns/process/server/6.3");
        } else {
            content = content.replace("http://www.bonitasoft.org/ns/process/client/6.0", "http://www.bonitasoft.org/ns/process/client/6.3");

        }
        processDefinitionXml = new XmlParser().parseText(content)
    }

    public String getContent() {

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


    public List<TransientData> getTransientData() {
        def Node[] transientData = processDefinitionXml.depthFirst().grep {
            it.name().getLocalPart().endsWith("ataDefinition") && it.'@transient' == "true"
        }
        return transientData.collect {
            new TransientData(node: it, containerId: Long.valueOf(it.parent().parent().'@id'), containerName: it.parent().parent().'@name')
        };
    }

    public getName() {
        processDefinitionXml.@name
    }

    public getId() {
        def id = processDefinitionXml.@id
        if(id != null){
            return Long.valueOf(id)
        }else{
            return "";
        }
    }

    public getVersion() {
        processDefinitionXml.@version
    }


    public updateExpressionOf(TransientData data) {
        def container = processDefinitionXml.depthFirst().find {
            it.@id == String.valueOf(data.containerId)
        }
        def expressionsToUpdate = container.depthFirst().findAll {
            it.content.text() == data.name && it.@expressionType == "TYPE_VARIABLE"
        }
        expressionsToUpdate.each { Node expression ->
            expression.@expressionType = "TYPE_TRANSIENT_VARIABLE"
        }
    }

    public updateOperatorAndLeftOperandType(List<TransientData> transientData) {
        def transientDataMap = [:]
        transientData.each {
            if (!transientDataMap.containsKey(it.name)) {
                transientDataMap.put(it.name, [it]);
            } else {
                transientDataMap.get(it.name).add(it);
            }
        }
        def operations = processDefinitionXml.depthFirst().findAll {
            // "peration" pattern to match all operation xml tags:
            it.name().getLocalPart().endsWith("peration")
        }
        operations.each { Node operation ->
            def leftOperand = operation.leftOperand[0]
            if (operation.@operatorType == "DOCUMENT_CREATE_UPDATE") {
                operation.@operatorType = "ASSIGNMENT"
                leftOperand.@type = "DOCUMENT"
            } else if (operation.@operatorType == "STRING_INDEX") {
                operation.@operatorType = "ASSIGNMENT"
                leftOperand.@type = "SEARCH_INDEX"
            } else {
                def dataName = leftOperand.@name
                def containerId = getContainerId(operation)
                if (containerId != null /* container is null means we are at process level */ && isDataTransientInContext(transientDataMap, dataName, containerId)) {
                    println "A transient data named $dataName of container $containerId is updated by an operation, this is not a good design because you might loose the updated value if the server resart, consider changing the design of your process."
                    leftOperand.@type = "TRANSIENT_DATA"
                } else {
                    leftOperand.@type = "DATA"
                }
            }
        }
    }

    public Long getContainerId(Node node) {
        if (node == null) {
            // process level
            return null;
        }
        if (node.@id != null) {
            return Long.valueOf(node.@id)
        }
        def Node parent = node.parent()
        return getContainerId(parent)
    }

    public static boolean isDataTransientInContext(Map transientDataMap, String dataName, long containerId) {
        if (transientDataMap.containsKey(dataName)) {
            for (transientData in transientDataMap.get(dataName)) {
                if (transientData.containerId == containerId) {
                    //the data is transient in the current container
                    return true;
                }
            }
        }
        return false;
    }
}
