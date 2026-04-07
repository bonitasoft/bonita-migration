/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to11_0_0

import groovy.sql.GroovyRowResult
import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddBdmQueryResponseFormatConfigIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddBdmQueryResponseFormatConfig updateStep = new AddBdmQueryResponseFormatConfig()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("11_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    def "should add the BDM serialization property to configuration file"() {
        given:
        byte[] confContent = this.getClass().getResourceAsStream("/conf/bonita-platform-community-custom.properties").text.bytes
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", 'bonita-platform-community-custom.properties', confContent)

        when:
        updateStep.execute(updateContext)
        List<GroovyRowResult> newConfRaw = updateContext.sql.rows("SELECT RESOURCE_CONTENT FROM configuration WHERE RESOURCE_NAME = 'bonita-platform-community-custom.properties'")

        then:
        newConfRaw.size() == 1
        def confString = updateContext.databaseHelper.getBlobContentAsString(newConfRaw.get(0).getProperty("resource_content"))
        confString.contains("# " + AddBdmQueryResponseFormatConfig.BDM_SERIALIZATION_COMMENT)
        confString.contains(AddBdmQueryResponseFormatConfig.DEFAULT_BDM_SERIALIZATION_ENTRY)
    }
}
