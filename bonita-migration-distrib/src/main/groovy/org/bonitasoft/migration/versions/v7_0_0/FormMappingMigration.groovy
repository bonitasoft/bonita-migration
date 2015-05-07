/**
 * Copyright (C) 2015 BonitaSoft S.A.
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


package org.bonitasoft.migration.versions.v7_0_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep
import org.bonitasoft.migration.core.MigrationUtil

/**
 * @author Baptiste Mesta
 *
 */
public class FormMappingMigration extends DatabaseMigrationStep {

    static long FORM_MAPPING_SEQUENCE = 10300
    static long PAGE_MAPPING_SEQUENCE = 10121
    int TYPE_PROCESS_START = 1;
    int TYPE_PROCESS_OVERVIEW = 2;
    int TYPE_TASK = 3;

    File bonitaHome
    String s = File.separator
    def legacy = "legacy"

    FormMappingMigration(Sql sql, String dbVendor, File bonitaHome) {
        super(sql, dbVendor)
        this.bonitaHome = bonitaHome
    }

    @Override
    def migrate() {
        MigrationUtil.getNexIdsForTable(sql, PAGE_MAPPING_SEQUENCE)
        sql.eachRow "SELECT * from process_definition", { row ->
            def tenantId = row[0]
            def id = row[2]
            def long tenantIdAsLong = tenantId instanceof BigDecimal ? tenantId.longValue() : tenantId
            def long idAsLong = id instanceof BigDecimal ? id.longValue() : id
            def String processContent = new File(bonitaHome.getAbsolutePath() + "${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}server-process-definition.xml").text
            addFormMappingForProcess tenantIdAsLong, idAsLong, processContent
        }
    }

    def addFormMappingForProcess(long tenantId, long id, String processContent) {
        def Node processDefinition = new XmlParser().parseText processContent
        String processName = processDefinition.@name
        String processVersion = processDefinition.@version
        def userTasks = processDefinition.depthFirst().grep { it.name().getLocalPart().equals("userTask") }.collect {
            it.@name
        }
        insertFormMappings(tenantId, id, processName, processVersion, userTasks)

    }

    def insertFormMappings(long tenantId, long id, String processName, String processVersion, List userTasks) {
        //add processOverview
        insertFormMapping(tenantId, id, TYPE_PROCESS_OVERVIEW, null, processName, processVersion)
        //add process start
        insertFormMapping(tenantId, id, TYPE_PROCESS_START, null, processName, processVersion)
        //add user tasks
        userTasks.each { String it ->
            insertFormMapping(tenantId, id, TYPE_TASK, it, processName, processVersion)
        }
    }

    def insertFormMapping(long tenantId, long processId, int type, String taskName, String processName, String processVersion) {
        def pageMappingId = getAndUpdateNextSequenceId(PAGE_MAPPING_SEQUENCE, tenantId)
        def formMappingId = getAndUpdateNextSequenceId(FORM_MAPPING_SEQUENCE, tenantId)
        def key = generateKey(type, taskName, processName, processVersion)

        execute("INSERT INTO page_mapping (tenantId, id, key_, urladapter, lastupdatedate, lastupdatedby) VALUES (?, ?, ?, ?, ?, ?)", [tenantId, pageMappingId, key, legacy, 0, 0])
        execute("INSERT INTO form_mapping (tenantId, id, process, type, task, page_mapping_tenant_id, page_mapping_id, lastupdatedate, lastupdatedby) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", [tenantId, formMappingId, processId, type, taskName, tenantId, pageMappingId, 0, 0])
    }

    String generateKey(int type, String taskName, String processName, String processVersion) {
        switch (type) {
            case TYPE_PROCESS_OVERVIEW:
                return "processInstance/" + processName + "/" + processVersion;
            case TYPE_PROCESS_START:
                return "process/" + processName + "/" + processVersion;
            case TYPE_TASK:
                return "taskInstance/" + processName + "/" + processVersion + "/" + taskName;
        }
        return null;
    }
}
