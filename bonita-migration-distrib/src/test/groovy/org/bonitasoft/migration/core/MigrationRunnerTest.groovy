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
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Baptiste Mesta
 */
class MigrationRunnerTest extends Specification {

    def infos = []
    def logger = [info: { String message -> infos.add(message) }] as Logger
    def VersionMigration versionMigration = Mock(VersionMigration);
    def MigrationStep migrationStep1 = Mock(MigrationStep)
    def MigrationStep migrationStep2 = Mock(MigrationStep)
    def List<MigrationStep> migrationStepList = [migrationStep1, migrationStep2]
    def MigrationContext migrationContext = new MigrationContext()
    def DatabaseHelper databaseHelper = Mock(DatabaseHelper)
    def MigrationRunner migrationRunner
    def Sql sql = Mock(Sql)

    def setup() {
        versionMigration.getMigrationSteps() >> migrationStepList
        migrationContext.sql = sql
        migrationContext.databaseHelper = databaseHelper
        migrationRunner = new MigrationRunner(versionMigrations: [versionMigration], context: migrationContext, logger: logger)
    }

    @Unroll
    def "run should #migrateText execute migrate bonita home for version #version"() {
        given:
        versionMigration.getVersion() >> version
        when:
        migrationRunner.run(true)
        then:
        migrate * versionMigration.migrateBonitaHome(_)
        where:
        version || migrate
        "7.2.8" || 1
        "7.3.0" || 0
        "7.3.1" || 0
        migrateText = migrate == 1 ? "" : "not"
    }

    def "run should execute migration steps in order"() {
        given:
        versionMigration.getVersion() >> "7.3.1"
        when:
        migrationRunner.run(true)
        then:
        1 * migrationStep1.execute(migrationContext)
        then:
        1 * migrationStep2.execute(migrationContext)
    }

    def "run should change platform version in database"() {
        given:
        versionMigration.getVersion() >> "7.3.1"
        when:
        migrationRunner.run(true)
        then:
        1 * sql.executeUpdate("UPDATE platform SET previousVersion = version")
        1 * sql.executeUpdate("UPDATE platform SET version = ${"7.3.1"}")
    }

}
