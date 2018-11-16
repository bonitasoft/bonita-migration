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
package org.bonitasoft.migration.version.to7_3_0

import groovy.json.JsonOutput
import groovy.sql.GroovyRowResult
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class MigrateAutoLoginConfigIT extends Specification {

    public static final long TENANT_ID_WITH_AUTO_LOGIN = 45L
    public static final long TENANT_ID_WITHOUT_AUTO_LOGIN = 32L
    public static final long PROCESS_1 = 6941637432798465017L
    public static final long PROCESS_2 = 4756258087450718363L
    public static final long PROCESS_3 = 9656258087450718363L
    public static final long PROCESS_4 = 1648258087450718363L

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.3.0")
        dropTables()
        dbUnitHelper.createTables("7_3_0/autologin", "autologin")

        [TENANT_ID_WITHOUT_AUTO_LOGIN, TENANT_ID_WITH_AUTO_LOGIN].each {
            dbUnitHelper.context.sql.execute("INSERT INTO tenant VALUES($it)")
            migrationContext.configurationHelper.insertConfigurationFile("autologin-v6.json", it, 'TENANT_PORTAL', "[]".bytes)
        }

        def uniqId = 0L
        insertProcessDefinition(TENANT_ID_WITH_AUTO_LOGIN, uniqId++, PROCESS_1, "Pool", "1.0", "ENABLED")
        insertProcessDefinition(TENANT_ID_WITH_AUTO_LOGIN, uniqId++, PROCESS_2, "Enabled Pool", "1.5", "ENABLED")
        insertProcessDefinition(TENANT_ID_WITH_AUTO_LOGIN, uniqId++, PROCESS_3, "Disable Pool", "1.5", "DISABLED")

        insertProcessDefinition(TENANT_ID_WITHOUT_AUTO_LOGIN, uniqId++, PROCESS_4, "no auto login Pool", "1.3", "ENABLED")

        insertBarResource(TENANT_ID_WITH_AUTO_LOGIN, uniqId++, PROCESS_1, "process.bpmn", "content".bytes)
        insertBarResource(TENANT_ID_WITH_AUTO_LOGIN, uniqId++, PROCESS_1, "forms/security-config.properties", """
            forms.application.login.auto.password=secret
            forms.application.login.auto=true
            forms.application.login.auto.username=walter.bates""".bytes)
        insertBarResource(TENANT_ID_WITH_AUTO_LOGIN, uniqId++, PROCESS_2, "forms/security-config.properties", """
            forms.application.login.auto.password=password
            forms.application.login.auto=true
            forms.application.login.auto.username=william.jobs""".bytes)
        insertBarResource(TENANT_ID_WITH_AUTO_LOGIN, uniqId++, PROCESS_3, "forms/security-config.properties", """
            forms.application.login.auto.password=bpm
            forms.application.login.auto=true
            forms.application.login.auto.username=hellen.kelly""".bytes)
    }

    def cleanup() {
        dropTables()
    }

    def "should 7.3.0 create auto login configuration based on existing bar resources"() {
        when:
        new MigrateAutoLoginConfig().execute(migrationContext)

        then:
        def migratedWithAutoLogin = getAutologinRow(TENANT_ID_WITH_AUTO_LOGIN)
        def migratedWithoutAutoLogin = getAutologinRow(TENANT_ID_WITHOUT_AUTO_LOGIN)

        def bytesWithAutoLogin
        def bytesWithoutAutoLogin

        if (MigrationStep.DBVendor.ORACLE.equals(migrationContext.dbVendor)) {
            bytesWithAutoLogin = (migratedWithAutoLogin['resource_content']).binaryStream.bytes
            bytesWithoutAutoLogin = (migratedWithoutAutoLogin['resource_content']).binaryStream.bytes
        } else {
            bytesWithAutoLogin = migratedWithAutoLogin['resource_content']
            bytesWithoutAutoLogin = migratedWithoutAutoLogin['resource_content']
        }

        JsonOutput.prettyPrint(new String(bytesWithAutoLogin)) == JsonOutput.prettyPrint("""
            [
                {
                    "processname": "Enabled Pool",
                    "processversion": "1.5",
                    "username": "william.jobs",
                    "password": "password"
                },
                {
                    "processname": "Pool",
                    "processversion": "1.0",
                    "username": "walter.bates",
                    "password": "secret"
                }
            ]""")
        JsonOutput.prettyPrint(new String(bytesWithoutAutoLogin)) == JsonOutput.prettyPrint("[]")
    }

    private String[] dropTables() {
        dbUnitHelper.dropTables(["bar_resource",
                                 "process_definition",
                                 "configuration",
                                 "tenant"
        ] as String[])
    }

    private GroovyRowResult getAutologinRow(tenantId) {
        dbUnitHelper.context.sql.firstRow("SELECT * FROM configuration c WHERE c.tenant_id=${tenantId} AND c.content_type='TENANT_PORTAL' AND c.resource_name='autologin-v6.json'")
    }

    private boolean insertProcessDefinition(tenantId, id, processDefinitionId, processName, processVersion, activationState) {
        dbUnitHelper.context.sql.execute(
                """
           INSERT INTO process_definition
(tenantid, id, processid, name, version, description, deploymentdate, deployedby, activationstate, configurationstate, displayname, displaydescription, lastupdatedate, categoryid, iconpath, content_tenantid, content_id)
VALUES(${tenantId}, ${id}, ${processDefinitionId}, ${processName}, ${processVersion}, '', 1466595046638, 4, ${
                    activationState
                }, 'UNRESOLVED', 'Pool', '', 0, NULL, NULL, 1, 2)

            """
        )
    }

    private boolean insertBarResource(tenantId, id, processDefinitionId, resourceName, content) {
        dbUnitHelper.context.sql.execute(
                """
                INSERT INTO bar_resource (tenantid, id, process_id, name, type, content)
                VALUES(${tenantId}, ${id}, ${processDefinitionId}, ${resourceName}, 'EXTERNAL', ${content})

            """
        )
    }


}
