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
import org.junit.Test

/**
 * @author Baptiste Mesta
 */
class MigrationRunnerTest {

    def infos = []
    def sql = [] as Sql
    def logger = [info: { String message -> infos.add(message) }] as Logger
    def boolean isStepExecuted = false

    @Test
    void testRun() {

        def migrations = [new VersionMigration() {

            @Override
            List<MigrationStep> getMigrationSteps() {
                return [new MigrationStep() {
                    @Override
                    def execute(Sql sql, DBVendor dbVendor) {
                        isStepExecuted = true
                        return null
                    }

                    @Override
                    String getDescription() {
                        return "the Description"
                    }
                }]
            }
        }
        ]
        MigrationRunner migrationRunner = new MigrationRunner(sql: sql, logger: logger, dbVendor: MigrationStep.DBVendor.POSTGRES, versionMigrations: migrations)

        migrationRunner.run()

        assert isStepExecuted

    }
}
