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
package org.bonitasoft.migration.version.to7_6_0

import static org.bonitasoft.migration.version.to7_6_0.RemoveDefaultGroovyScriptsAndUpdateTheirConfiguration.DYNAMIC_PERMISSIONS_FILE

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class UpdateDynamicPermissionChecksCustomIT extends Specification {

    final
    static COUNT_SCRIPTS_SQL = "SELECT count(*) AS cpt FROM configuration WHERE tenant_id=? AND content_type=? and resource_name='ActorMemberPermissionRule.groovy'"

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    byte[] defaultRuleContent = this.class.getResource("/to7_6_0/ActorMemberPermissionRule.groovy.txt").bytes

    private RemoveDefaultGroovyScriptsAndUpdateTheirConfiguration migrationStep = new RemoveDefaultGroovyScriptsAndUpdateTheirConfiguration()

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

    def "should update dynamic permissions with package org.bonitasoft.permissions for default rules"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE, 'GET|bpm/actorMember=[profile|Administrator, check|ActorMemberPermissionRule]'.bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_SECURITY_SCRIPTS", 'ActorMemberPermissionRule.groovy', defaultRuleContent)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE, 'GET|custom/customAPI=[user|john.doe, check|ActorMemberPermissionRule]'.bytes)
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_SECURITY_SCRIPTS", 'ActorMemberPermissionRule.groovy', defaultRuleContent)

        assert 1 == countConfigFiles(1L, 'TENANT_SECURITY_SCRIPTS')
        assert 1 == countConfigFiles(0L, 'TENANT_TEMPLATE_SECURITY_SCRIPTS')

        when:
        migrationStep.execute(migrationContext)

        then:
        1 == dbUnitHelper.countConfigFileWithContent("TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|bpm/actorMember=[profile|Administrator, check|org.bonitasoft.permissions.ActorMemberPermissionRule]")
        1 == dbUnitHelper.countConfigFileWithContent("TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|custom/customAPI=[user|john.doe, check|org.bonitasoft.permissions.ActorMemberPermissionRule]")
        0 == countConfigFiles(1L, 'TENANT_SECURITY_SCRIPTS')
        0 == countConfigFiles(0L, 'TENANT_TEMPLATE_SECURITY_SCRIPTS')

        !migrationStep.warnings.contains('groovy script ActorMemberPermissionRule.groovy (from tenant 0)')
        !migrationStep.warnings.contains('groovy script ActorMemberPermissionRule.groovy (from tenant 1)')
        !migrationStep.getWarning().contains('you will not benefit from the potential fixes')
    }

    private int countConfigFiles(Long tenantId, String type) {
        migrationContext.sql.firstRow(COUNT_SCRIPTS_SQL, tenantId, type).get("cpt") as int
    }

    def "should not update dynamic permissions with new package for custom rules"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                2L, "TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE, 'GET|bpm/actorMember=[profile|Administrator, check|MyCustomRule]'.bytes)
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                2L, "TENANT_SECURITY_SCRIPTS", 'ActorMemberPermissionRule.groovy', defaultRuleContent)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE, 'GET|custom/customAPI=[user|john.doe, check|MyCustomRule]'.bytes)
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_SECURITY_SCRIPTS", 'ActorMemberPermissionRule.groovy', defaultRuleContent)

        assert 1 == countConfigFiles(2L, 'TENANT_SECURITY_SCRIPTS')
        assert 1 == countConfigFiles(0L, 'TENANT_TEMPLATE_SECURITY_SCRIPTS')

        when:
        migrationStep.execute(migrationContext)

        then:
        0 == dbUnitHelper.countConfigFileWithContent("TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|bpm/actorMember=[profile|Administrator, check|org.bonitasoft.permissions.MyCustomRule]")
        1 == dbUnitHelper.countConfigFileWithContent("TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|bpm/actorMember=[profile|Administrator, check|MyCustomRule]")

        0 == dbUnitHelper.countConfigFileWithContent("TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|custom/customAPI=[user|john.doe, check|org.bonitasoft.permissions.MyCustomRule]")
        1 == dbUnitHelper.countConfigFileWithContent("TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|custom/customAPI=[user|john.doe, check|MyCustomRule]")
        0 == countConfigFiles(1L, 'TENANT_SECURITY_SCRIPTS')
        0 == countConfigFiles(0L, 'TENANT_TEMPLATE_SECURITY_SCRIPTS')

        !migrationStep.warnings.contains('groovy script ActorMemberPermissionRule.groovy (from tenant 0)')
        !migrationStep.warnings.contains('groovy script ActorMemberPermissionRule.groovy (from tenant 1)')
        !migrationStep.getWarning().contains('you will not benefit from the potential fixes')
    }

    def "should not update dynamic permissions with new package for MODIFIED default rules"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE, 'GET|bpm/actorMember=[profile|Administrator, check|ActorMemberPermissionRule]'.bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_SECURITY_SCRIPTS", 'ActorMemberPermissionRule.groovy', 'Default file name but with modified content'.bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE, 'GET|custom/customAPI=[user|john.doe, check|ActorMemberPermissionRule]'.bytes)
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                0L, "TENANT_TEMPLATE_SECURITY_SCRIPTS", 'ActorMemberPermissionRule.groovy', 'Default file name but with modified content'.bytes)

        assert 1 == countConfigFiles(1L, 'TENANT_SECURITY_SCRIPTS')
        assert 1 == countConfigFiles(0L, 'TENANT_TEMPLATE_SECURITY_SCRIPTS')

        when:
        migrationStep.execute(migrationContext)

        then:
        0 == dbUnitHelper.countConfigFileWithContent("TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|bpm/actorMember=[profile|Administrator, check|org.bonitasoft.permissions.ActorMemberPermissionRule]")
        1 == dbUnitHelper.countConfigFileWithContent("TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|bpm/actorMember=[profile|Administrator, check|ActorMemberPermissionRule]")

        0 == dbUnitHelper.countConfigFileWithContent("TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|custom/customAPI=[user|john.doe, check|org.bonitasoft.permissions.ActorMemberPermissionRule]")
        1 == dbUnitHelper.countConfigFileWithContent("TENANT_TEMPLATE_PORTAL", DYNAMIC_PERMISSIONS_FILE,
                "GET|custom/customAPI=[user|john.doe, check|ActorMemberPermissionRule]")

        // content is not default, so the files should not have been removed from database:
        1 == countConfigFiles(1L, 'TENANT_SECURITY_SCRIPTS')
        1 == countConfigFiles(0L, 'TENANT_TEMPLATE_SECURITY_SCRIPTS')

        migrationStep.warnings.contains('groovy script ActorMemberPermissionRule.groovy (from tenant 0)')
        migrationStep.warnings.contains('groovy script ActorMemberPermissionRule.groovy (from tenant 1)')
        migrationStep.getWarning().contains('you will not benefit from the potential fixes')
    }

    def "should not warn twice for the same MODIFIED default rule"() {
        given:
        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_PORTAL", DYNAMIC_PERMISSIONS_FILE, '''
GET|bpm/actorMember=[profile|Administrator, check|ActorMemberPermissionRule]
PUT|bpm/case=[profile|User, check|ActorMemberPermissionRule]
'''.bytes)

        migrationContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                1L, "TENANT_SECURITY_SCRIPTS", 'ActorMemberPermissionRule.groovy', 'Default file name but with modified content'.bytes)

        def searchedWarning = 'groovy script ActorMemberPermissionRule.groovy (from tenant 1)'

        when:
        migrationStep.execute(migrationContext)

        then:
        // check that the warning for a given (script + tenant) only occurs once:
        1 == migrationStep.warnings.count(searchedWarning)
    }

}
