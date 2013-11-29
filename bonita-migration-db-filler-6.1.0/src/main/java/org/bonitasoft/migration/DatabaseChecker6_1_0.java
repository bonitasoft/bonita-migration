package org.bonitasoft.migration;

import javax.naming.Context;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseChecker6_1_0 {

    private static ClassPathXmlApplicationContext springContext;

    public static void main(final String[] args) throws Exception {
        verify();
    }

    public static void verify() throws Exception {

        setupSpringContext();
        System.out.println("in 6.1.0 version checker");
        PlatformSession platformSession = APITestUtil.loginPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        APITestUtil.logoutPlatform(platformSession);
        APISession session = APITestUtil.loginDefaultTenant();
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        System.out.println(processAPI.getNumberOfProcessInstances());
        APITestUtil.logoutTenant(session);
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
