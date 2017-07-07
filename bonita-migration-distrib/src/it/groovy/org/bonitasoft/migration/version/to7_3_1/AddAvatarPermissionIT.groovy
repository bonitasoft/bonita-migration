/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_3_1

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AddAvatarPermissionIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.3.1")
        dbUnitHelper.createTables("7_3_1/configuration")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    def "should add avatar permission in resources-permissions-mapping.properties"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                5L, "content-type", AddAvatarPermission.RESOURCE_PERMISSIONS_MAPPING_FILE_NAME, "key=value".bytes)

        when:
        new AddAvatarPermission().migrateResourcePermissionsFiles(migrationContext)

        then:
        def count = dbUnitHelper.countPropertyInConfigFile(AddAvatarPermission.RESOURCE_PERMISSIONS_MAPPING_FILE_NAME, "GET|API/avatars", "[avatars]")
        count == 1

    }

    def "should not modify existing property value when the GET|API/avatars key is already present in resources-permissions-mapping.properties"() {
        given:
        def key = "GET|API/avatars"
        def existingValue = "[existingValue]"
        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                5L, "content-type", AddAvatarPermission.RESOURCE_PERMISSIONS_MAPPING_FILE_NAME, "${key}=${existingValue}".bytes)

        when:
        new AddAvatarPermission().migrateResourcePermissionsFiles(migrationContext)

        then:
        def count = dbUnitHelper.countPropertyInConfigFile(AddAvatarPermission.RESOURCE_PERMISSIONS_MAPPING_FILE_NAME, key, existingValue)
        count == 1
    }

    @Unroll
    def "should modify existing property #key if it is a Bonita default key and it does not contain the avatar permission in compound-permissions-mapping.properties"(key, initValue, expectedMigratedValue) {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                5L, "content-type", AddAvatarPermission.COMPOUND_PERMISSIONS_MAPPING_FILE_NAME, "${key}=${initValue}".bytes)

        when:
        new AddAvatarPermission().migrateCompoundPermissionsMappingFiles(migrationContext)

        then:
        def count = dbUnitHelper.countPropertyInConfigFile(AddAvatarPermission.COMPOUND_PERMISSIONS_MAPPING_FILE_NAME, key, expectedMigratedValue)
        count == 1

        where:
        key                             | initValue                                                    | expectedMigratedValue
        "custompage_defaultlayout"      | "[organization_visualization, profile_member_visualization]" | "[organization_visualization, profile_member_visualization, avatars]"
        "grouplistingadmin"             | "[profile_visualization, download_document"                  | "[profile_visualization, download_document, avatars]"
        "custompage_groovyexample"      | "[profile_visualization, profile_management, avatars]"       | "[profile_visualization, profile_management, avatars]"
        "businessdatamodelimport"       | "[tenant_platform_management, avatars, download_document]"   | "[tenant_platform_management, avatars, download_document]"
        "unknown_key_from_customer"     | "[profile_visualization, download_document]"                 | "[profile_visualization, download_document]"
        "custompage_htmlexample"        | "[1]"                                                        | "[1, avatars]"
        "pagelisting"                   | "[2]"                                                        | "[2, avatars]"
        "importexportorganization"      | "[3]"                                                        | "[3, avatars]"
        "tenantMaintenance"             | "[4]"                                                        | "[4, avatars]"
        "processlistinguser"            | "[5]"                                                        | "[5, avatars]"
        "processlistingpm"              | "[6]"                                                        | "[6, avatars]"
        "tasklistingadmin"              | "[7]"                                                        | "[7, avatars]"
        "userlistingadmin"              | "[8]"                                                        | "[8, avatars]"
        "rolelistingadmin"              | "[9]"                                                        | "[9, avatars]"
        "tasklistinguser"               | "[10]"                                                       | "[10, avatars]"
        "profilelisting"                | "[11]"                                                       | "[11, avatars]"
        "caselistingadmin"              | "[12]"                                                       | "[12, avatars]"
        "thememoredetailsadminext"      | "[13]"                                                       | "[13, avatars]"
        "processlistingadmin"           | "[14]"                                                       | "[14, avatars]"
        "tasklistingpm"                 | "[15]"                                                       | "[15, avatars]"
        "caselistingpm"                 | "[16]"                                                       | "[16, avatars]"
        "applicationslistingadmin"      | "[17]"                                                       | "[17, avatars]"
        "caselistinguser"               | "[18]"                                                       | "[18, avatars]"
        "reportlistingadminext"         | "[19]"                                                       | "[19, avatars]"
        "custompage_home"               | "[20]"                                                       | "[20, avatars]"
        "custompage_apiExtensionViewer" | "[21]"                                                       | "[21, avatars]"
        "custompage_tasklist"           | "[22]"                                                       | "[22, avatars]"
    }

    @Unroll
    def "should not modify existing commented property #key even if it does not contain the avatar permission in compound-permissions-mapping.properties"(key, initValue, value) {
        def initialContent = "# ${key}=${initValue}"
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                5L, "content-type", AddAvatarPermission.COMPOUND_PERMISSIONS_MAPPING_FILE_NAME, initialContent.bytes)

        when:
        new AddAvatarPermission().migrateCompoundPermissionsMappingFiles(migrationContext)

        then:
        def count = dbUnitHelper.countConfigFileWithContent(AddAvatarPermission.COMPOUND_PERMISSIONS_MAPPING_FILE_NAME, initialContent)
        count == 1

        where:
        key                 | initValue                                                    | value
        "tenantMaintenance" | "[organization_visualization, profile_member_visualization]" | "[organization_visualization, profile_member_visualization]"
        "caselistingadmin"  | "[profile_visualization, download_document"                  | "[profile_visualization, download_document"
    }

}
