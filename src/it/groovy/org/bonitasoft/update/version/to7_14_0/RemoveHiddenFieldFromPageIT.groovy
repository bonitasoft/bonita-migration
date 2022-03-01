/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to7_14_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Dumitru Corini
 */
class RemoveHiddenFieldFromPageIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveHiddenFieldFromPages updateStep = new RemoveHiddenFieldFromPages()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_13_0/pages")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page", "sequence", "tenant"] as String[])
    }


    def "should remove hidden field from pages"() {
        given:
        assert updateContext.databaseHelper.hasColumnOnTable("page", "hidden")

        when:
        updateStep.execute(updateContext)

        then:
        !updateContext.databaseHelper.hasColumnOnTable("page", "hidden")
    }
}
