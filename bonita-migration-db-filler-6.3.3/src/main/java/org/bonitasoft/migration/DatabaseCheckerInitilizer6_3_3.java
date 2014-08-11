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

import javax.naming.Context;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.ClientEventUtil;
import org.bonitasoft.engine.test.PlatformTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class DatabaseCheckerInitilizer6_3_3 {

    private static ClassPathXmlApplicationContext springContext;

    private static PlatformTestUtil platformTestUtil = new PlatformTestUtil();

    protected static APITestUtil apiTestUtil = new APITestUtil();

    @BeforeClass
    public static void setup() throws BonitaException {
        setupSpringContext();
        PlatformSession platformSession = platformTestUtil.loginOnPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        platformTestUtil.logoutOnPlatform(platformSession);

        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
        // new version of class AddHandlerCommand.java in 6.3.3, that is useful for gatewayinstance_created event:
        ClientEventUtil.undeployCommand(apiTestUtil.getSession());
        ClientEventUtil.deployCommand(apiTestUtil.getSession());
    }

    @AfterClass
    public static void teardown() throws BonitaException {
        apiTestUtil.logoutOnTenant();
        final PlatformSession pSession = apiTestUtil.loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        apiTestUtil.stopPlatformAndTenant(platformAPI, false);
        apiTestUtil.logoutOnPlatform(pSession);
        closeSpringContext();
    }

    private static void setupSpringContext() {
        setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");

        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
    }

    private static void closeSpringContext() {
        springContext.close();
    }

    private static void setSystemPropertyIfNotSet(final String property, final String value) {
        System.setProperty(property, System.getProperty(property, value));
    }

}
