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
package org.bonitasoft.update

import groovy.sql.Sql
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchOptionsBuilder

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static java.util.Collections.emptyMap
import static org.awaitility.Awaitility.await

/**
 * @author Baptiste Mesta
 */
class TestUtil {

    static Sql connection

    static byte[] createTestPageContent(String pageName, String displayName, String description) throws Exception {
        ByteArrayOutputStream e = new ByteArrayOutputStream()
        ZipOutputStream zos = new ZipOutputStream(e)
        zos.putNextEntry(new ZipEntry("Index.groovy"))
        zos.write("return \"\";".getBytes())
        zos.putNextEntry(new ZipEntry("page.properties"))
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("name=")
        stringBuilder.append(pageName)
        stringBuilder.append("\n")
        stringBuilder.append("displayName=")
        stringBuilder.append(displayName)
        stringBuilder.append("\n")
        stringBuilder.append("description=")
        stringBuilder.append(description)
        stringBuilder.append("\n")
        zos.write(stringBuilder.toString().getBytes())
        zos.closeEntry()
        return e.toByteArray()
    }


    static getSql() {
        if (!connection) {
            def dburl = System.getProperty("db.url")
            def dbDriverClassName = System.getProperty("db.driverClass")
            def dbUser = System.getProperty("db.user")
            def dbPassword = System.getProperty("db.password")
            connection = Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)
        }
        connection
    }

    static boolean hasTable(String tableName) {
        def query

        def dbVendor = System.getProperty("db.vendor")
        switch (dbVendor) {
            case "postgres":
                query = """
                    SELECT *
                     FROM information_schema.tables
                     WHERE table_schema='public'
                       AND table_type='BASE TABLE'
                       AND UPPER(table_name) = UPPER($tableName)
                    """
                break

            case "oracle":
                query = """
                    SELECT *
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                break

            case "mysql":
                query = """
                    SELECT *
                    FROM information_schema.tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    AND table_schema = DATABASE()
                    """
                break

            case "sqlserver":
                query = """
                    SELECT * FROM information_schema.tables
                    WHERE UPPER(TABLE_NAME) = UPPER($tableName)
                    """
                break
            default:
                throw new IllegalStateException("db vendor invalid: $dbVendor")
        }
        def firstRow = sql.firstRow(query)
        return firstRow != null
    }

    static void sendMessage(final String messageName, final String targetProcessName,
            final String targetFlowNodeName, ProcessAPI processAPI) throws Exception {
        processAPI.sendMessage(messageName, new ExpressionBuilder().createConstantStringExpression(targetProcessName),
                new ExpressionBuilder().createConstantStringExpression(targetFlowNodeName), null)
    }

    static void waitForUserTask(String taskName, ProcessAPI processAPI) {
        waitForUserTask(taskName, processAPI, 1L)
    }

    static void waitForUserTask(String taskName, ProcessAPI processAPI, long numberOfTaskInstances) {
        await().until({ processAPI.searchHumanTaskInstances(getSearchOptionsForTask(taskName)).count == numberOfTaskInstances })
    }

    static SearchOptions getSearchOptionsForTask(String taskName) {
        new SearchOptionsBuilder(0, 1)
                .filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, "ready")
                .filter(HumanTaskInstanceSearchDescriptor.NAME, taskName).done()
    }

    static void assignAndExecuteUserTask(long processInstanceId, String taskName, long userId, ProcessAPI processAPI) {
        def taskInstance = processAPI
                .getHumanTaskInstances(processInstanceId, taskName, 0, 1)
                .get(0)
        processAPI.assignAndExecuteUserTask(userId, taskInstance.id, emptyMap())
    }

    static void waitForProcessToFinish(long processInstanceId, ProcessAPI processAPI) {
        await().until({
            processAPI.getArchivedProcessInstances(processInstanceId, 0, 1).size() == 1
        })
    }

    /**
     * Waits for a document to be visible in archived state after process completion.
     * <p>
     * Both documents and process instances are archived within the same transaction using before-commit callables.
     * However, on Oracle, read consistency issues can cause archived documents to not be immediately visible
     * even after the archived process instance is queryable. This can be due to:
     * <ul>
     *   <li>Read replica replication lag (e.g., Oracle Data Guard)</li>
     *   <li>Transaction isolation level differences between queries</li>
     *   <li>Cursor consistency - different queries may see different data versions</li>
     * </ul>
     * This method explicitly waits until the archived document is readable, preventing
     * ArchivedDocumentNotFoundException in tests.
     *
     * @param documentId the ID of the document to wait for archiving
     * @param processAPI the process API to use for checking
     */
    static void waitForArchivedDocument(long documentId, ProcessAPI processAPI) {
        await().until({
            try {
                processAPI.getArchivedVersionOfProcessDocument(documentId)
                return true
            } catch (Exception ignored) {
                return false
            }
        })
    }
}
