/**
 * Copyright (C) 2018 BonitaSoft S.A.
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
package org.bonitasoft.migration

import static org.assertj.core.api.Assertions.assertThat

import org.bonitasoft.engine.BonitaDatabaseConfiguration
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bpm.bar.BarResource
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder
import org.bonitasoft.engine.bpm.process.ConfigurationState
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.form.FormMappingTarget
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.test.TestEngineImpl
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown
import org.bonitasoft.migration.filler.FillerUtils
import org.bonitasoft.migration.test.TestUtil
import org.junit.Rule

/**
 * @author Danila Mazour
 * @author Emmanuel Duchastenier
 */
class FillBeforeMigratingTo7_9_1 {
    private static engine = TestEngineImpl.getInstance()

    @FillerInitializer
    void startEngine() {
        FillerUtils.initializeEngineSystemProperties()
        def databaseConfiguration = new BonitaDatabaseConfiguration(
                dbVendor: System.getProperty("db.vendor"),
                url: System.getProperty("db.url"),
                driverClassName: System.getProperty("db.driverClass"),
                user: System.getProperty("db.user"),
                password: System.getProperty("db.password")
        )
        engine.setBonitaDatabaseProperties(databaseConfiguration)
        println "database configuration: $databaseConfiguration"
        engine.dropOnStart = false
        engine.dropOnStop = false
        engine.start()
    }

    @FillerShutdown
    void stopEngine() {
        try {
            engine.stop()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    @FillAction
    void fillOneUserWithTechnicalUser() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        identityAPI.createUser("john", "bpm")
        TenantAPIAccessor.getLoginAPI().logout(session)
    }

}

