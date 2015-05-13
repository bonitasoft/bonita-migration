/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.migration.versions.v7_0_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * @author Baptiste Mesta
 */
class ProcessDefinitionInDatabaseMigration extends DatabaseMigrationStep {

    File bonitaHome
    String s = File.separator

    ProcessDefinitionInDatabaseMigration(Sql sql, String dbVendor, File bonitaHome) {
        super(sql, dbVendor)
        this.bonitaHome = bonitaHome
    }

    @Override
    def migrate() {
        //migrate processes
        sql.eachRow "SELECT * from process_definition", { row ->
            def tenantId = row[0]
            def id = row[2]
            def long tenantIdAsLong = tenantId instanceof BigDecimal ? tenantId.longValue() : tenantId
            def long idAsLong = id instanceof BigDecimal ? id.longValue() : id
            migrateProcess tenantId, id, tenantIdAsLong, idAsLong
        }
    }

    private void migrateProcess(tenantId, id, long tenantIdAsLong, long idAsLong) {
        def clientProcessDefinitionFile = new File(bonitaHome.getAbsolutePath() + "${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}process-design.xml")
        def String processContent = clientProcessDefinitionFile.text
        //add if on expressions
        processContent = addGeneratedIdsToExpressions processContent
        //put in database
        putInDatabase processContent, tenantIdAsLong, idAsLong
        //update on FS
        clientProcessDefinitionFile.write processContent
        //delete server process definition
        new File(bonitaHome.getAbsolutePath() + "${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}server-process-definition.xml").delete()
    }

    def putInDatabase(String processContent, long tenantId, long id) {
        execute("UPDATE process_definition SET designcontent=? WHERE tenantid=? AND processid=?", [processContent, tenantId, id])
        println "Update process definition $id of tenant $tenantId with content ${processContent.length()}"
    }

    String addGeneratedIdsToExpressions(String processDefinitionContent) {
        def processDefinition = new XmlParser().parseText(processDefinitionContent)

        def executeOnExpression
        executeOnExpression = { it ->
            if (!(it instanceof Node))
                return
            if (it.@expressionType != null) {
                it.@id = generateId()
            }
            it.children().each executeOnExpression
        }
        processDefinition.children().each executeOnExpression
        return getContent(processDefinition)
    }

    public String getContent(processDefinition) {

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
        printer.print(processDefinition)

        def result = writer.toString()
        return result;
    }

    private long generateId() {
        return Math.abs(UUID.randomUUID().leastSignificantBits)
    }
}
