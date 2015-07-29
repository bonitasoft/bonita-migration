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
 * @author Baptiste Mesta
 */
class CheckMigratedTo7_0_1 {

    @BeforeClass
    public static void beforeClass(){
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
    def void theTest() {
        TenantAPIAccessor.getLoginAPI().login("john","bpm");
    }

    @AfterClass
    public static void afterClass(){
        new PlatformTestUtil().stopPlatformAndTenant(PlatformAPIAccessor.getPlatformAPI(new PlatformTestUtil().loginOnPlatform()), true)
    }
}
