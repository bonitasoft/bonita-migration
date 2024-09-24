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
package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveWorkThreadPoolPropertiesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveWorkThreadPoolProperties updateStep = new RemoveWorkThreadPoolProperties()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_13_0/configuration")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }


    def "should remove work thread pool properties"() {
        def propFileContent = """
bonita.tenant.work.queueCapacity=10000
bonita.tenant.work.corePoolSize=10
#bonita.tenant.work.maximumPoolSize=10
bonita.tenant.work.keepAliveTimeSeconds=30
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_ENGINE", RemoveWorkThreadPoolProperties.CONFIG_FILE_NAME, propFileContent.bytes)
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_ENGINE", RemoveWorkThreadPoolProperties.CONFIG_FILE_NAME, propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """
bonita.tenant.work.queueCapacity=10000
"""
        List updatedRows = updateContext.sql.rows("SELECT tenant_id, content_type, resource_name, resource_content FROM configuration order by tenant_id ASC")

        updatedRows.size() == 2
        updatedRows[0].tenant_id == 0L
        updatedRows[0].content_type == "TENANT_TEMPLATE_ENGINE"
        updatedRows[0].resource_name == RemoveWorkThreadPoolProperties.CONFIG_FILE_NAME
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content)  == expectedPropFileContent
        updatedRows[1].tenant_id == 1L
        updatedRows[1].content_type == "TENANT_ENGINE"
        updatedRows[1].resource_name == RemoveWorkThreadPoolProperties.CONFIG_FILE_NAME
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[1].resource_content)  == expectedPropFileContent
    }
}
