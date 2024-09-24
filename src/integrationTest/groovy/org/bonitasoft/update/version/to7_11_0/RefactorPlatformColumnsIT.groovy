/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class RefactorPlatformColumnsIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def updateContext = new UpdateContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(updateContext)
    RefactorPlatformColumns updateStep = new RefactorPlatformColumns()

    def setup() {
        dropTestTables()
        updateContext.setVersion("7.11.0")
        dbUnitHelper.createTables("7_11_0/platform")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["platform"] as String[])
    }

    def "should update all 3 columns from 'platform' table"() {
        when:
        updateStep.execute(updateContext)

        then:

        dbUnitHelper.hasColumnOnTable("platform", "version")
        dbUnitHelper.hasColumnOnTable("platform", "initial_bonita_version")
        dbUnitHelper.hasColumnOnTable("platform", "created_by")

        !dbUnitHelper.hasColumnOnTable("platform", "initialVersion")
        !dbUnitHelper.hasColumnOnTable("platform", "previousVersion")
    }
}
