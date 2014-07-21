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
 **/
package org.bonitasoft.migration.versions.v6_3_1_to_6_3_2

import groovy.sql.Sql

import org.bonitasoft.migration.core.MigrationUtil


/**
 * @author Elias Ricken de Medeiros
 *
 */
class UpdatedDefaultCommands {

    def Map nextIds;


    public migrate(Sql sql, List<CommandDescriptor> commandsToInsert) {
        migrate(sql, commandsToInsert, [])
    }
    public migrate(Sql sql, List<CommandDescriptor> commandsToInsert, List<String> commandNamesToDelete) {
        def commandSequenceId = 90;
        nextIds = MigrationUtil.getNexIdsForTable(sql, commandSequenceId);

        insertCommands(commandsToInsert, sql)

        MigrationUtil.updateNextIdsForTable(sql, commandSequenceId, nextIds);

        deleteCommands(commandNamesToDelete, sql);
    }

    private insertCommands(List commandsToInsert, Sql sql) {
        commandsToInsert.each { commandToInsert ->
            sql.eachRow("SELECT id FROM tenant") { result ->
                def tenantId = result.id;
                if(hasSystemCommand(sql, tenantId, commandToInsert.name)) {
                    println "The system command '$commandToInsert.name' already exists on tenant $tenantId. Nothing to do."
                } else {
                    insertSystemCommmand(sql, commandToInsert, tenantId);
                }
            }
        }
    }

    private deleteCommands(List<String> commandNamesToDelete, Sql sql) {
        commandNamesToDelete.each { commandName ->
            println "Deleting system command '$commandName' for all tenants"
            println sql.executeUpdate("DELETE FROM command WHERE name = $commandName") + " row(s) deleted."
        }
    }

    private boolean hasSystemCommand(Sql sql, tenantId, name) {
        def firstRow = sql.firstRow("""
                SELECT id
                FROM command
                WHERE tenantid = $tenantId
                       AND name = $name
        """)
        return firstRow != null;
    }

    private void insertSystemCommmand(Sql sql, CommandDescriptor descriptor, tenantId) {
        println "Inserting sytem command '$descriptor.name' for tenant $tenantId"
        def id = nextIds.get(tenantId);
        nextIds.put(tenantId, id +1);

        boolean system = true;
        def insertCommand = """
            INSERT INTO command (tenantid, id, name, description, IMPLEMENTATION, system)
            VALUES (
                $tenantId,
                $id,
                $descriptor.name,
                $descriptor.description,
                $descriptor.implementation,
                $system
            )
        """
        sql.executeInsert(insertCommand);
    }
}
