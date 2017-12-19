package org.bonitasoft.migration

import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown
import org.bonitasoft.migration.filler.FillerUtils
/**
 * @author Baptiste Mesta.
 */
class InitializerBefore7_3_0 {

    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
        def initializer = Class.forName("org.bonitasoft.engine.test.TestEngineImpl")
        def instance = initializer.metaClass.getProperty(initializer, "instance")
        instance.metaClass.invokeMethod(instance,
                "start",null)
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
        def session = PlatformAPIAccessor.getPlatformLoginAPI().login("platformAdmin", "platform")
        PlatformAPIAccessor.getPlatformAPI(session).stopNode()
        PlatformAPIAccessor.getPlatformLoginAPI().logout(session)
    }
}
