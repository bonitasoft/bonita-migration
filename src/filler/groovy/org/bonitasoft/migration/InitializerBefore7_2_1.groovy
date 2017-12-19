package org.bonitasoft.migration

import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.test.PlatformTestUtil
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown
import org.bonitasoft.migration.filler.FillerUtils

/**
 * @author Baptiste Mesta.
 */
class InitializerBefore7_2_1 {

    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
        def testInitializer = Class.forName("org.bonitasoft.engine.LocalServerTestsInitializer")
        testInitializer.metaClass.invokeMethod(testInitializer,
                "beforeAll",null)
    }

    @FillAction
    public void fillOneUserWithTechnicalUser() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        identityAPI.createUser("john", "bpm")
        TenantAPIAccessor.getLoginAPI().logout(session)
    }


    @FillerShutdown
    public void shutdown() {
        new PlatformTestUtil().stopPlatformAndTenant(PlatformAPIAccessor.getPlatformAPI(new PlatformTestUtil().loginOnPlatform()), true)
    }
}
