/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration
import org.bonitasoft.engine.LocalServerTestsInitializer
import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.test.PlatformTestUtil
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
/**
 * @author Laurent Leseigneur
 */
class CheckMigratedTo7_2_0 {

    @BeforeClass
    public static void beforeClass() {
        FillerUtils.initializeEngineSystemProperties()
        startNode()
    }

    private static void startNode() {
        LocalServerTestsInitializer.instance.prepareEnvironment()
        def platformTestUtil = new PlatformTestUtil()
        def platform = platformTestUtil.loginOnPlatform()
        def platformApi = platformTestUtil.getPlatformAPI(platform)
        platformApi.startNode()
        platformTestUtil.logoutOnPlatform(platform)
    }

    @Test
    def void checkProcessStillWorks() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def id = processAPI.getProcessDefinitionId("MyProcess to be migrated", "1.0-SNAPSHOT")
        def processInstance = processAPI.startProcess(id)
        Thread.sleep(2000)
        def instances = processAPI.getArchivedProcessInstances(processInstance.getId(), 0, 100)
        assert instances.size() == 3

        TenantAPIAccessor.getLoginAPI().logout(session)

    }

    @AfterClass
    public static void afterClass() {
        new PlatformTestUtil().stopPlatformAndTenant(PlatformAPIAccessor.getPlatformAPI(new PlatformTestUtil().loginOnPlatform()), true)
    }

}