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
package org.bonitasoft.update.plugin

import org.bonitasoft.update.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.tasks.JavaExec
/**
 * @author Emmanuel Duchastenier
 */
class RunMsSqlserverXARecoveryTask extends JavaExec {

    @Override
    void exec() {
        def testValues = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
        logger.info "Current project: $project.name"

        def toolSystemProperties = [:]
        toolSystemProperties.put('host', testValues['db.server.name'])
        toolSystemProperties.put('port', testValues['db.server.port'])
        toolSystemProperties.put('database', testValues['db.database.name'])
        toolSystemProperties.put('user', testValues['db.user'])
        toolSystemProperties.put('password', testValues['db.password'])

        systemProperties toolSystemProperties
        logger.info "Calling MS SQL Server XARecovery tool (to initialize MSDTC module) using system properties $systemProperties"
        logger.debug "using classpath:"
        classpath(project.getConfigurations().getByName("xarecovery"))
        super.exec()
    }

}
