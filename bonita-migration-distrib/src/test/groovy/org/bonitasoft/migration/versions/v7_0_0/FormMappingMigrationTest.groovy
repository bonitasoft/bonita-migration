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

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.junit.Test
import org.mockito.ArgumentCaptor

import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

/**
 * @author Baptiste Mesta
 */
class FormMappingMigrationTest {


    Sql sql = mock(Sql);
    File bonitaHome = mock(File)
    FormMappingMigration formMappingMigration = spy(new FormMappingMigration(sql, "postgres", bonitaHome))

    def returnId = { long id -> new GroovyRowResult(["nextId": id]) }

    @Test
    def void test_insert_form_mapping_for_process() {
        //given
        long pageMappingId1 = 445l
        long pageMappingId2 = 446l
        long formMappingId1 = 447l
        long formMappingId2 = 448l
        def returnId = { long id -> new GroovyRowResult(["nextId": id]) }
        when(sql.firstRow(any(GString.class))).thenReturn(returnId(pageMappingId1),
                returnId(formMappingId1),
                returnId(pageMappingId2),
                returnId(formMappingId2))

        //when
        formMappingMigration.insertFormMappings(15l, 1245l, "MyProcess", "1.0", [])
        //then
        def captorParams1 = new ArgumentCaptor<List>()
        def captorParams2 = new ArgumentCaptor<List>()
        verify(sql, times(2)).execute(eq("INSERT INTO page_mapping (tenantId, id, key_, urladapter, lastupdatedate, lastupdatedby) VALUES (?, ?, ?, ?, ?, ?)"), captorParams1.capture());
        verify(sql, times(2)).execute(eq("INSERT INTO form_mapping (tenantId, id, process, type, task, page_mapping_tenant_id, page_mapping_id, lastupdatedate, lastupdatedby) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"), captorParams2.capture());

        def iterator = captorParams1.allValues.iterator()
        assert iterator.next() == [15, pageMappingId1, "processInstance/MyProcess/1.0", "legacy", 0, 0];
        assert iterator.next() == [15, pageMappingId2, "process/MyProcess/1.0", "legacy", 0, 0];
        iterator = captorParams2.allValues.iterator()
        assert iterator.next() == [15, formMappingId1, 1245, 2, null, 15, pageMappingId1, 0, 0];
        assert iterator.next() == [15, formMappingId2, 1245, 1, null, 15, pageMappingId2, 0, 0];

    }

    @Test
    def void test_insert_form_mapping_for_user_task() {
        //given
        long pageMappingId1 = 445l
        long pageMappingId2 = 446l
        long pageMappingId3 = 449l
        long pageMappingId4 = 451l
        long formMappingId1 = 447l
        long formMappingId2 = 448l
        long formMappingId3 = 450l
        long formMappingId4 = 452l
        when(sql.firstRow(any(GString.class))).thenReturn(returnId(pageMappingId1),
                returnId(formMappingId1),
                returnId(pageMappingId2),
                returnId(formMappingId2),
                returnId(pageMappingId3),
                returnId(formMappingId3),
                returnId(pageMappingId4),
                returnId(formMappingId4))

        //when
        formMappingMigration.insertFormMappings(15l, 1245l, "MyProcess", "1.0", ["task1", "task2"])
        //then
        def captorParams1 = new ArgumentCaptor<List>()
        def captorParams2 = new ArgumentCaptor<List>()
        verify(sql, times(4)).execute(eq("INSERT INTO page_mapping (tenantId, id, key_, urladapter, lastupdatedate, lastupdatedby) VALUES (?, ?, ?, ?, ?, ?)"), captorParams1.capture());
        verify(sql, times(4)).execute(eq("INSERT INTO form_mapping (tenantId, id, process, type, task, page_mapping_tenant_id, page_mapping_id, lastupdatedate, lastupdatedby) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"), captorParams2.capture());

        def iterator = captorParams1.allValues.iterator()
        assert iterator.next() == [15, pageMappingId1, "processInstance/MyProcess/1.0", "legacy", 0, 0]
        assert iterator.next() == [15, pageMappingId2, "process/MyProcess/1.0", "legacy", 0, 0]
        assert iterator.next() == [15, pageMappingId3, "taskInstance/MyProcess/1.0/task1", "legacy", 0, 0]
        assert iterator.next() == [15, pageMappingId4, "taskInstance/MyProcess/1.0/task2", "legacy", 0, 0]
        iterator = captorParams2.allValues.iterator()
        assert iterator.next() == [15, formMappingId1, 1245, 2, null, 15, pageMappingId1, 0, 0]
        assert iterator.next() == [15, formMappingId2, 1245, 1, null, 15, pageMappingId2, 0, 0]
        assert iterator.next() == [15, formMappingId3, 1245, 3, "task1", 15, pageMappingId3, 0, 0]
        assert iterator.next() == [15, formMappingId4, 1245, 3, "task2", 15, pageMappingId4, 0, 0]
    }


    @Test
    def void test_addFormMappingForProcess() {
        //given
        when(sql.firstRow(any(GString.class))).thenReturn(returnId(55547))
        String processContent = """
<processDefinition  xmlns="http://www.bonitasoft.org/ns/process/server/6.3" name="MyProcess" version="1.0">
    <userTask name="task1">
        <someContent>
        </someContent>
    </userTask>
    <something>
    </something>
    <userTask name="task2"/>
</processDefinition>
"""
        //when
        formMappingMigration.addFormMappingForProcess(15l, 45l, processContent)
        //then
        def captor = new ArgumentCaptor<String>()
        def captorParams = new ArgumentCaptor<List>()
        verify(sql, times(8)).execute(captor.capture(), captorParams.capture());
    }

}