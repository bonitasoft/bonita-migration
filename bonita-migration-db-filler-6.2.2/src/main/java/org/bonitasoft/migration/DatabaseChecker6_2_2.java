/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * accessor program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * accessor program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with accessor program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.migration;

import javax.naming.Context;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * 
 * Check that the migrated database is ok
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * 
 */
@SuppressWarnings("deprecation")
public class DatabaseChecker6_2_2 {

    private static final byte[] buffer = new byte[1024];

    private static ClassPathXmlApplicationContext springContext;

    protected static ProcessAPI processAPI;

    protected static ProfileAPI profileAPI;

    protected static IdentityAPI identityApi;

    private static ThemeAPI themeAPI;

    protected static APISession session;

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_2_2.class.getName());
    }

    @BeforeClass
    public static void setup() throws BonitaException {
        setupSpringContext();
        PlatformSession platformSession = APITestUtil.loginPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        APITestUtil.logoutPlatform(platformSession);
        session = APITestUtil.loginDefaultTenant();
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        identityApi = TenantAPIAccessor.getIdentityAPI(session);
        profileAPI = TenantAPIAccessor.getProfileAPI(session);
        themeAPI = TenantAPIAccessor.getThemeAPI(session);
    }

    @AfterClass
    public static void teardown() throws BonitaException {
        APITestUtil.logoutTenant(session);
        final PlatformSession pSession = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        APITestUtil.stopPlatformAndTenant(platformAPI, false);
        APITestUtil.logoutPlatform(pSession);
    }

    @Test
    public void runIt() throws Exception {
        processAPI.getNumberOfProcessInstances();
    }

    private static void setupSpringContext() {
        setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");

        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
    }

    private static void setSystemPropertyIfNotSet(final String property, final String value) {
        System.setProperty(property, System.getProperty(property, value));
    }
}
