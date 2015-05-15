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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when;

/**
 * @author Baptiste Mesta
 */
class ProcessDefinitionInDatabaseMigrationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    def processBeforeMigration = ProcessDefinitionInDatabaseMigrationTest.class.getResourceAsStream("client_before_expression_migration.xml").text;
    Sql sql = mock(Sql)
    String dbVendor = "postgres"
    File bonitaHome
    ProcessDefinitionInDatabaseMigration migration

    @Before
    void before(){
        bonitaHome = temporaryFolder.newFolder()
        migration = new ProcessDefinitionInDatabaseMigration(sql, dbVendor, bonitaHome)
    }

    @Test
    public void addGeneratedIdsToExpressions_should_add_ids_on_all_expressions() {
        def parsedProcessBeforeMigration = new XmlParser().parseText(processBeforeMigration)
        assert !parsedProcessBeforeMigration.breadthFirst().findAll{ it.@expressionType != null}.isEmpty()
        assert parsedProcessBeforeMigration.breadthFirst().findAll{ it.@expressionType != null && it.@id != null}.isEmpty()

        def processAfterMigration = migration.addGeneratedIdsToExpressions(processBeforeMigration)

        assert new XmlParser().parseText(processAfterMigration).breadthFirst().findAll{ it.@expressionType != null && it.@id == null}.isEmpty()
    }

    @Test
    public void migrate_write_migrated_content_from_database() {
        def processFolder = new File(bonitaHome.getAbsolutePath() + "/server/tenants/12/work/processes/25/")
        processFolder.mkdirs()
        def xmlFile = new File(processFolder, "process-design.xml")
        xmlFile.text = processBeforeMigration

        def serverProcessDef = new File(processFolder, "server-process-definition.xml")
        serverProcessDef.text = "the server process definition"


        migration.migrateProcess(12, 25)

        assert !serverProcessDef.exists()
        assert xmlFile.exists()
        assert !xmlFile.text.equals(processBeforeMigration)
        verify(sql).execute("UPDATE process_definition SET designcontent=? WHERE tenantid=? AND processid=?",[xmlFile.text, 12l, 25l])
    }

}