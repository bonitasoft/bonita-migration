/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_3_3

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class FixProcessSupervisorPermissionRuleScriptIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    public static final List<String> tables = ["configuration", "tenant"]


    def setup() {
        dropTables()
        migrationContext.setVersion("7.3.3")
        tables.each { tableName ->
            dbUnitHelper.createTables("7_3_1/$tableName")
        }
    }

    def cleanup() {
        dropTables()
    }

    private String[] dropTables() {
        dbUnitHelper.dropTables(tables as String[])
    }

    def "should update supervisor permission rule script "() {
        given:
        def fixProcessSupervisorPermissionRuleScript = new FixProcessSupervisorPermissionRuleScript()
        def newContent = fixProcessSupervisorPermissionRuleScript.getNewContent()
        def resourceName = FixProcessSupervisorPermissionRuleScript.resourceName
        migrationContext.sql.executeInsert(" INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                , 8L, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                'default', 'ACTIVATED')
        migrationContext.sql.executeInsert(" INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                , 7L, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                'default', 'ACTIVATED')
        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_SECURITY_SCRIPTS", resourceName, "old content".bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                7L, "TENANT_SECURITY_SCRIPTS", resourceName, "old content".bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id,content_type, resource_name, resource_content) values (?,?,?,?)",
                8L, "TENANT_SECURITY_SCRIPTS", resourceName, "old content".bytes)
        when:
        fixProcessSupervisorPermissionRuleScript.execute(migrationContext)

        then:
        def modifiedFiles = dbUnitHelper.countConfigFileWithContent(resourceName, new String(newContent))
        modifiedFiles == 3

    }


}
