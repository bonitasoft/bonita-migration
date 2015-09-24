/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration.core

import groovy.sql.Sql
import spock.lang.Specification
/**
 * @author Baptiste Mesta
 */
class MigrationRunnerTest extends  Specification {

    def infos = []
    def logger = [info: { String message -> infos.add(message) }] as Logger

    def VersionMigration versionMigration = Mock(VersionMigration);
    def MigrationContext migrationContext = Mock(MigrationContext)
    def Sql sql = Mock(Sql)

    def "run should execute migration step"() {
        migrationContext.sql >> sql
        def MigrationStep migrationStep = Mock(MigrationStep);
        versionMigration.getMigrationSteps() >> [migrationStep]

        MigrationRunner migrationRunner = new MigrationRunner(versionMigrations: [versionMigration], context: migrationContext, logger: logger)

        when:
        migrationRunner.run(false)

        then:
        1 * migrationStep.execute(migrationContext)

    }
}
