package org.bonitasoft.update

import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.update.filler.FillAction
import org.bonitasoft.update.filler.FillerInitializer
import org.bonitasoft.update.filler.FillerUtils
import org.junit.Rule
/**
 * @author Baptiste Mesta.
 */
class InitializerBefore7_11_0 {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().keepPlatformOnShutdown()

    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
    }

    @FillAction
    public void fillOneUserWithTechnicalUser() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        identityAPI.createUser("john", "bpm")
        TenantAPIAccessor.getLoginAPI().logout(session)
    }

}
