/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_7_5

import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper

import spock.lang.Specification

class DeleteOrphanArchContractDataTest extends Specification {

    def 'should only delete orphan ArchContractData once'() {
        given:
        def migrationStep = Spy(DeleteOrphanArchContractData)
        def migrationContext = newMigrationContext()
        when:
        migrationStep.execute(migrationContext)
        migrationStep.execute(migrationContext)
        then:
        1 * migrationStep.performDeletion(migrationContext)
    }

    private MigrationContext newMigrationContext() {
        def migrationContext = Mock(MigrationContext)
        migrationContext.databaseHelper >> Mock(DatabaseHelper)
        migrationContext.logger >> Mock(Logger)
        migrationContext
    }

}
