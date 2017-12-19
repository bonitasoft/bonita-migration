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
package org.bonitasoft.migration.version.to7_3_1

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class UpdateCompoundPermissionMappingIT extends Specification {
    public static final List<String> tables = ["configuration", "tenant"]
    public static final String OTHER_PERMISSION = "otherKey=otherValue"
    public static final String TENANT_TEMPLATE_PORTAL = "TENANT_TEMPLATE_PORTAL"
    public static final String TENANT_PORTAL = "TENANT_PORTAL"
    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.3.1")
        tables.each { tableName ->
            dbUnitHelper.createTables("7_3_1/$tableName")
        }
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(tables as String[])
    }

    def "should update properties configuration file"() {
        given:
        def updateCompoundPermissionMapping = new UpdateCompoundPermissionMapping()
        migrationContext.sql.executeInsert(" INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                , 8L, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                'default', 'ACTIVATED')
        migrationContext.sql.executeInsert(" INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                , 6L, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                'default', 'ACTIVATED')

        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, TENANT_TEMPLATE_PORTAL, UpdateCompoundPermissionMapping.resourceName, "${updateCompoundPermissionMapping.key}=old content\n${OTHER_PERMISSION}".bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                8L, TENANT_PORTAL, UpdateCompoundPermissionMapping.resourceName, "${updateCompoundPermissionMapping.key}=old contentn${OTHER_PERMISSION}".bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                6L, TENANT_PORTAL, UpdateCompoundPermissionMapping.resourceName, OTHER_PERMISSION.bytes)
        when:
        updateCompoundPermissionMapping.execute(migrationContext)

        then:
        def modifiedFiles = dbUnitHelper.countPropertyInConfigFile(UpdateCompoundPermissionMapping.resourceName, updateCompoundPermissionMapping.key, updateCompoundPermissionMapping.caseListingPmPermissions)
        modifiedFiles == 3
    }


}
