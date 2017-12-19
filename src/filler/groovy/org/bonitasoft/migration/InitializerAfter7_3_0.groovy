package org.bonitasoft.migration

import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.Rule
/**
 * @author Baptiste Mesta.
 */
class InitializerAfter7_3_0 {

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
