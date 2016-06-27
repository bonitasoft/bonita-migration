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
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class MigrateAutoLoginConfig extends MigrationStep {

    public static final String USERNAME_KEY = "forms.application.login.auto.username"
    public static final String PASSWORD_KEY = "forms.application.login.auto.password"

    @Override
    def execute(MigrationContext context) {

        context.sql.eachRow("SELECT id from tenant ORDER BY id") {
            def tenantId = it.id as long
            def tenantAutologins = []
            context.sql.eachRow("""
                    SELECT
                        d.name as process_name,
                        d.version as process_version,
                        b.tenantid,
                        b.content as props,
                        d.activationstate
                    FROM
                        bar_resource b
                        INNER JOIN process_definition d ON
                        (
                                b.process_id = d.processid
                                AND b.tenantid = d.tenantid
                        )
                    WHERE
                    b.name = 'forms/security-config.properties'
                    AND b.type = 'EXTERNAL'
                    AND d.activationstate = 'ENABLED'
                    AND d.tenantid = ${tenantId}  """) { row ->
                Properties properties = new Properties()
                def stream = new ByteArrayInputStream(row["props"])
                properties.load(stream)
                def autologinProp = [processname: row["process_name"], processversion: row["process_version"], username: properties.get(USERNAME_KEY), password: properties.get(PASSWORD_KEY)]
                tenantAutologins.add(autologinProp)
            }

            def json = JsonOutput.toJson(tenantAutologins)
            context.databaseHelper.updateConfigurationFileContent('autologin-v6.json', tenantId, 'TENANT_PORTAL', json.toString().bytes)
        }
        return null
    }


    protected insert(MigrationContext context, String fileName, long tenantId, String type) {
        this.class.getResourceAsStream("/version/to_7_3_0/platform-resources/" + fileName).withStream {
            context.databaseHelper.insertConfigurationFile(fileName.split('/')[1], tenantId, type, it.bytes)
        }
    }

    @Override
    String getDescription() {
        return "Add auto login settings from process to tenant portal configuration file"
    }
}
