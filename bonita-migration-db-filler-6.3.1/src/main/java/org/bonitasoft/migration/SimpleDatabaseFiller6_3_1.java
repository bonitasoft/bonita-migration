/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.migration;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.PlatformTestUtil;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class SimpleDatabaseFiller6_3_1 extends SimpleDatabaseFiller6_0_2 {

    private APITestUtil apiTestUtil = new APITestUtil();
    private PlatformTestUtil platformTestUtil = new PlatformTestUtil();
    
    private long tenantId;

    @Override
    protected APISession loginDefaultTenant() throws BonitaException {
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
        APISession session = apiTestUtil.getSession();
        tenantId = session.getTenantId();
        return session;
    }
    
    @Override
    protected void logoutTenant(APISession session) throws BonitaException {
        apiTestUtil.logoutOnTenant();
    }
    
    protected void initializePlatform() throws BonitaException {
        platformTestUtil.createInitializeAndStartPlatformWithDefaultTenant(false);
    }
    
    @Override
    protected PlatformSession loginPlatform() throws BonitaException {
        return platformTestUtil.loginOnPlatform();
    }
    
    @Override
    protected void stopPlatformAndTenant(PlatformAPI platformAPI) throws BonitaException {
        platformTestUtil.stopPlatformAndTenant(platformAPI, true);
    }
    
    @Override
    protected void logoutPlatform(PlatformSession pSession) throws BonitaException {
        platformTestUtil.logoutOnPlatform(pSession);
    }
    
    @Override
    protected WorkService getWorkService() throws Exception {
        return ServiceAccessorFactory.getInstance().createTenantServiceAccessor(tenantId).getWorkService();
    }
    

}
