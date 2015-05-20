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
import org.bonitasoft.migration.core.MigrationUtil
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * @author Baptiste Mesta
 */
class ProcessDefinitionInDatabaseMigration extends DatabaseMigrationStep {

    static long SEQUENCE_ID = 10310
    File bonitaHome
    String s = File.separator

    ProcessDefinitionInDatabaseMigration(Sql sql, String dbVendor, File bonitaHome) {
        super(sql, dbVendor)
        this.bonitaHome = bonitaHome
    }

    @Override
    def migrate() {
        sql.eachRow "SELECT id FROM tenant ORDER BY id ASC" ,{row ->
            def tenantId = row[0]
            def long tenantIdAsLong = tenantId instanceof BigDecimal ? tenantId.longValue() : tenantId
            execute("INSERT INTO sequence VALUES(?, 10310, 1)",[tenantIdAsLong])
        }
        //migrate processes
        sql.eachRow "SELECT * from process_definition", { row ->
            def tenantId = row[0]
            def id = row[2]
            def long tenantIdAsLong = tenantId instanceof BigDecimal ? tenantId.longValue() : tenantId
            def long idAsLong = id instanceof BigDecimal ? id.longValue() : id
            migrateProcess tenantIdAsLong, idAsLong
        }
    }

    def migrateProcess(long tenantId, long id) {
        def clientProcessDefinitionFile = new File(bonitaHome.getAbsolutePath() + "${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}process-design.xml")
        def String processContent = clientProcessDefinitionFile.text
        //add if on expressions
        processContent = addGeneratedIdsToExpressions processContent
        //put in database
        putInDatabase processContent, tenantId, id
        //update on FS
        clientProcessDefinitionFile.write processContent
        //delete server process definition
        new File(bonitaHome.getAbsolutePath() + "${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}server-process-definition.xml").delete()
    }

    def putInDatabase(String processContent, long tenantId, long id) {
        def processContentId = getAndUpdateNextSequenceId(SEQUENCE_ID, tenantId)
        execute("INSERT INTO process_content (tenantId, id, content) VALUES (?, ?, ?)", [tenantId, processContentId, processContent])
        execute("UPDATE process_definition SET content_tenantid=?, content_id=?  WHERE tenantid=? AND processid=?", [tenantId, processContentId, tenantId, id])
        println "Create process content with id $processContentId for  process definition $id of tenant $tenantId"
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
