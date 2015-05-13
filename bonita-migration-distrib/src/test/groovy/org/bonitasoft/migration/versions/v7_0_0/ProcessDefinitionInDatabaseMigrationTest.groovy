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
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
class ProcessDefinitionInDatabaseMigrationTest {

    def processBeforeMigration = ProcessDefinitionInDatabaseMigrationTest.class.getResourceAsStream("client_before_expression_migration.xml").text;
    Sql sql
    String dbVendor
    File bonitaHome
    ProcessDefinitionInDatabaseMigration migration = new ProcessDefinitionInDatabaseMigration(sql, dbVendor, bonitaHome)


    @Test
    public void test() {

        def processAfterMigration = migration.addGeneratedIdsToExpressions(processBeforeMigration)


        assert new XmlParser().parseText(processAfterMigration).breadthFirst().findAll{ it.@expressionType != null && it.@id == null}.isEmpty()
    }

}