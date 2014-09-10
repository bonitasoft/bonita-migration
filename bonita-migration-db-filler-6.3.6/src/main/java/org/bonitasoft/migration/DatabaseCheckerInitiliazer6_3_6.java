/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.bonitasoft.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.naming.Context;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseCheckerInitiliazer6_3_6 {

    private static ClassPathXmlApplicationContext springContext;

    protected static ProcessAPI processAPI;

    protected static ProfileAPI profileAPI;

    protected static IdentityAPI identityApi;

    protected static CommandAPI commandApi;

    private static APITestUtil apiTestUtil;

    @BeforeClass
    public static void setup() throws BonitaException {
        setupSpringContext();
        apiTestUtil = new APITestUtil();
        PlatformSession platformSession = apiTestUtil.loginOnPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        apiTestUtil.logoutOnPlatform(platformSession);
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
        processAPI = apiTestUtil.getProcessAPI();
        identityApi = apiTestUtil.getIdentityAPI();
        profileAPI = apiTestUtil.getProfileAPI();
        commandApi = apiTestUtil.getCommandAPI();
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

    protected HumanTaskInstance waitForUserTask(final String taskName, final long processInstanceId, final int timeout) throws Exception {
        long now = System.currentTimeMillis();
        SearchResult<ActivityInstance> searchResult;
        do {
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
            builder.filter(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstanceId);
            builder.filter(ActivityInstanceSearchDescriptor.NAME, taskName);
            builder.filter(ActivityInstanceSearchDescriptor.STATE_NAME, "ready");
            searchResult = processAPI.searchActivities(builder.done());
        } while (searchResult.getCount() == 0 && now + timeout > System.currentTimeMillis());
        assertEquals(1, searchResult.getCount());
        final HumanTaskInstance getHumanTaskInstance = processAPI.getHumanTaskInstance(searchResult.getResult().get(0).getId());
        assertNotNull(getHumanTaskInstance);
        return getHumanTaskInstance;
    }

    protected void waitForProcessToFinish(final long processInstanceId, final int timeout) throws Exception {
        long now = System.currentTimeMillis();
        SearchResult<ArchivedProcessInstance> searchResult;
        do {
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
            builder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId);
            builder.filter(ArchivedProcessInstancesSearchDescriptor.STATE_ID, ProcessInstanceState.COMPLETED.getId());
            searchResult = processAPI.searchArchivedProcessInstances(builder.done());
        } while (searchResult.getCount() == 0 && now + timeout > System.currentTimeMillis());
        assertEquals(1, searchResult.getCount());
    }
}
