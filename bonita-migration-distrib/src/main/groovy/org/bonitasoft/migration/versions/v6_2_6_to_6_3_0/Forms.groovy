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
package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0;

import org.codehaus.groovy.runtime.InvokerHelper





/**
 * @author Baptiste Mesta
 *
 */
public class Forms {

    def groovy.util.Node formsXml;

    /*
     * the map contains transient name -> list of activities having them
     */
    def Map transientDataMap

    static formIdRegex = /[^-]+--[^-]+-?-?([^-]*)\$/

    public Node findParent(Node node,Closure closure){
        def parent = node.parent()
        if(parent == null){
            return null
        }
        if(closure(parent)){
            return parent
        }
        return findParent(parent,closure)
    }

    public Forms(String content, List<TransientData> transientData, formsVersion){
        this.transientDataMap = [:]
        transientData.each {
            if(!transientDataMap.containsKey(it.name)){
                transientDataMap.put(it.name, [])
            }
            transientDataMap.get(it.name).add(it.containerName)
        }
        formsXml = new XmlParser().parseText(content)
        formsXml.breadthFirst().find(){
            it.name() ==  "migration-product-version"
        }.setValue(formsVersion)//TODO sp / bos
    }

    public String getContent(){
        def writer = new StringWriter()
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
        def printer = new XmlNodePrinter(new PrintWriter(writer) ){
                    protected void printSimpleItem(Object value) {
                        if (!preserveWhitespace) printLineBegin();
                        out.print(InvokerHelper.toString(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('\r', '&#13;'));
                        if (!preserveWhitespace) printLineEnd();
                    }
                }
        printer.setPreserveWhitespace(true)
        printer.setExpandEmptyElements(false)
        printer.print(formsXml)
        def result = writer.toString()
        return result;
    }


    public void updateExpressions(){
        formsXml.breadthFirst().findAll{it.name() == "expression-type" && it.text()=="TYPE_VARIABLE"}.each { def Node it ->
            def dataName = it.parent().children().find{ sib ->
                sib.name() == "name"
            }.text();
            if(isDataTransientInContext(it, dataName)){
                it.setValue("TYPE_TRANSIENT_VARIABLE")
            }
        }
    }

    private boolean isDataTransientInContext(Node element, String dataName) {
        def Node form = findParent(element){ parent ->
            parent.name() == "form"
        }
        def parentId = form.attribute("id")
        def elementName = getElementName(parentId)
        if(!elementName.isEmpty()){
            //if it is process: no transient data
            //if the data name in the container is transient, we change the type
            if(transientDataMap.containsKey(dataName) && transientDataMap.get(dataName).contains(elementName)){
                //it is transient in this context
                return true
            }
        }
        return false
    }

    protected static getElementName(parentId) {
        def matcher = (parentId =~ formIdRegex);
        def elementName = matcher[0][1]
        return elementName
    }

    public void updateActions(){
        formsXml.breadthFirst().findAll{it.name() == "output-operation" || (it.name() == "action" && it.@type != "EXECUTE_CONNECTOR") }.each { def Node it ->
            //input
            def originalType = it.@type
            def Node isExternalNode = it.find{child->child.name() == "is-external" };
            def external = isExternalNode != null && isExternalNode.text() == "true"
            def Node variableNode = it.find{child->child.name() == "variable"}
            def dataName = (variableNode != null)? variableNode.text() : null
            //migrated values
            def type = "ASSIGNMENT"
            def String variableType = "DATA"
            if(originalType == "DOCUMENT_CREATE_UPDATE"){
                variableType = "DOCUMENT"
            }else if (originalType == "STRING_INDEX"){
                variableType = "SEARCH_INDEX"
            } else if( external ){
                variableType = "EXTERNAL_DATA"
            } else if(dataName != null && isDataTransientInContext(it, dataName)){
                println "A transient data named $dataName is updated by a form action, this is not a good design because you might loose the updated value if the server resart, consider changing the design of your process."
                variableType = "TRANSIENT_DATA"
            }
            //replace the is-external by the variable-type
            if(isExternalNode != null){
                isExternalNode.replaceNode { node -> 'variable-type'(variableType) }
            }
            //update the type
            it.@type = type

        }
    }
}
