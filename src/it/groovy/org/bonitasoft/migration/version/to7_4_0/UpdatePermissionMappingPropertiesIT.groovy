/*
 * Copyright (C) 2017 Bonitasoft S.A.
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
 */
package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class UpdatePermissionMappingPropertiesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.4.0")
        dbUnitHelper.createTables("7_4_0/configuration", "api")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    @Unroll
    def "should add key #key with #value in resources-permission-mapping.properties"(key, value) {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                5L, "content-type", UpdatePermissionMappingProperties.configFile, "key=value".bytes)

        when:
        new UpdatePermissionMappingProperties().execute(migrationContext)

        then:
        def count = dbUnitHelper.countPropertyInConfigFile(UpdatePermissionMappingProperties.configFile, key, value)
        count == 1

        where:
        key                                                | value
        "POST|portal/custom-page/API/formFileUpload"       | "[form_file_upload]"
        "GET|portal/custom-page/API/avatars"               | "[avatars]"
        "GET|portal/custom-page/API/documentDownload"      | "[download_document]"
        "GET|API/formsDocumentImage"                       | "[download_document]"
        "GET|portal/custom-page/API/formsDocumentImage"    | "[download_document]"
        "GET|portal/formsDocumentImage"                    | "[download_document]"
        "GET|portal/custom-page/API/formsDocumentDownload" | "[download_document]"
        "GET|portal/custom-page/API/downloadDocument"      | "[download_document]"

    }

    @Unroll
    def "should not modify existing property #key if it is already present in resources-permission-mapping.properties"(key) {
        given:
        def existingValue = "[existingValue]"
        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                5L, "content-type", UpdatePermissionMappingProperties.configFile, "${key}=${existingValue}".bytes)

        when:
        new UpdatePermissionMappingProperties().execute(migrationContext)

        then:
        def count = dbUnitHelper.countPropertyInConfigFile(UpdatePermissionMappingProperties.configFile, key, existingValue)
        count == 1

        where:
        key << ["POST|portal/custom-page/API/formFileUpload",
                "GET|portal/custom-page/API/avatars",
                "GET|portal/custom-page/API/documentDownload",
                "GET|API/formsDocumentImage",
                "GET|portal/custom-page/API/formsDocumentImage",
                "GET|portal/formsDocumentImage",
                "GET|portal/custom-page/API/formsDocumentDownload",
                "GET|portal/custom-page/API/downloadDocument"]

    }

}
