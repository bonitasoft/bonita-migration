/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.bonitasoft.engine.theme.ThemeType;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
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
public class DatabaseChecker6_2_0 {

    private static ClassPathXmlApplicationContext springContext;

    protected static ProcessAPI processAPI;

    protected static ProfileAPI profileAPI;

    protected static IdentityAPI identityApi;

    private static ThemeAPI themeAPI;

    protected static APISession session;

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_2_0.class.getName());
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
    public void verify() throws Exception {
        final long id = identityApi.getUserByUserName("april.sanchez").getId();
        final WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(50, 10000, 10, id, processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            String message = "Not all task after transitions were created";
            throw new IllegalStateException(message);
        } else {
            List<HumanTaskInstance> pendingHumanTaskInstances = processAPI.getPendingHumanTaskInstances(id, 0, 100, ActivityInstanceCriterion.NAME_ASC);
            List<String> taskNames = new ArrayList<String>();
            for (HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
                taskNames.add(humanTaskInstance.getName());
            }
            Collections.sort(taskNames);
            List<String> expected = Arrays.asList("finished_exclu2", "finished_para4", "finished_para4", "finished_para4", "finished_inclu3",
                    "finished_inclu2", "finished_para1", "finished_inclu3", "finished_para4", "finished_inclu1");
            Collections.sort(expected);
            assertEquals(expected, taskNames);
        }
    }

    @Test
    public void check_archivedProcessInstance_can_be_retrive() throws Exception {
        long processDefinitionId = processAPI.getProcessDefinitionId("ProcessThatFinish", "1.0");
        SearchResult<ArchivedProcessInstance> archivedProcessInstances = processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 10).filter(
                ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId).done());
        assertTrue(archivedProcessInstances.getCount() > 0);
    }

    @Test
    public void check_process_with_messages_still_work() throws Exception {
        final long processDefinitionId = processAPI.getProcessDefinitionId("ProcessWithSendMessage", "1.0");
        final long receiveProcess = processAPI.getProcessDefinitionId("ProcessWithIntermediateReceiveMessage", "1.0");

        final User favio = TenantAPIAccessor.getIdentityAPI(session).getUserByUserName("favio.riviera");
        final int pendingTaskOfFavio = Long.valueOf(processAPI.getNumberOfPendingHumanTaskInstances(favio.getId())).intValue();

        // there is one intermediate catch waiting + a start message
        processAPI.startProcess(processDefinitionId);

        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(100, 20000, pendingTaskOfFavio + 2, favio.getId(), processAPI);
        boolean bothReceived = waitForPendingTasks.waitUntil();
        if (!bothReceived) {
            throw new IllegalStateException("throw/catch message don't work");
        }

        // // start the intermediate catch waiting
        processAPI.startProcess(receiveProcess);
        // // there is one intermediate catch waiting + a start message
        processAPI.startProcess(processDefinitionId);
        waitForPendingTasks = new WaitForPendingTasks(100, 20000, pendingTaskOfFavio + 4, favio.getId(), processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            throw new IllegalStateException("throw/catch message don't work");
        }
    }

    @Test
    public void check_profiles() throws Exception {
        final SAXReader reader = new SAXReader();
        final Document document = getProfilesXML(reader);
        final Element profiles = document.getRootElement();

        // Iterate through child elements of root with element name "profile"
        for (Iterator<Element> rootIterator = profiles.elementIterator("profile"); rootIterator.hasNext();) {
            final Element profileElement = rootIterator.next();
            final Profile profile = checkProfile(profileElement);

            final Element profileEntriesElement = profileElement.element("profileEntries");
            if (profileEntriesElement != null) {
                for (Iterator<Element> parentProfileEntryIterator = profileEntriesElement.elementIterator("parentProfileEntry"); parentProfileEntryIterator
                        .hasNext();) {
                    final Element parentProfileEntryElement = parentProfileEntryIterator.next();
                    final ProfileEntry profileEntry = checkProfileEntry(parentProfileEntryElement, profile.getId(), 0);

                    final Element childProfileEntriesElement = profileElement.element("childrenEntries");
                    if (childProfileEntriesElement != null) {
                        for (Iterator<Element> childProfileEntryIterator = childProfileEntriesElement.elementIterator("profileEntry"); childProfileEntryIterator
                                .hasNext();) {
                            final Element childProfileEntryElement = childProfileEntryIterator.next();
                            checkProfileEntry(childProfileEntryElement, profile.getId(), profileEntry.getId());
                        }
                    }
                }
            }
        }
    }

    protected Document getProfilesXML(final SAXReader reader) throws Exception {
        return reader.read(getClass().getResource("profiles.xml"));
    }

    private Profile checkProfile(final Element profileElement) throws SearchException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort("name", Order.DESC);
        builder.filter("name", profileElement.attributeValue("name"));
        final List<Profile> resultProfiles = profileAPI.searchProfiles(builder.done()).getResult();
        assertEquals(1, resultProfiles.size());

        final Profile profile = resultProfiles.get(0);
        assertEquals(Boolean.valueOf(profileElement.attributeValue("isDefault")), profile.isDefault());
        assertNotNull(profile.getCreatedBy());
        assertNotEquals(0, profile.getCreatedBy());
        assertNotNull(profile.getCreationDate());
        assertNotEquals(0, profile.getCreationDate());
        assertEquals(profileElement.elementText("description"), profile.getDescription());
        assertEquals(profileElement.elementText("iconPath"), profile.getIconPath());
        assertNotNull(profile.getLastUpdateDate());
        assertNotEquals(0, profile.getLastUpdateDate());
        assertNotNull(profile.getLastUpdatedBy());
        assertNotEquals(0, profile.getLastUpdatedBy());

        return profile;
    }

    private ProfileEntry checkProfileEntry(final Element profileEntryElement, final long profileId, final long parentProfileEntryId) throws SearchException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort("name", Order.DESC);
        builder.filter("name", profileEntryElement.attributeValue("name"));
        builder.filter("parentId", parentProfileEntryId);
        builder.filter("profileId", profileId);
        final List<ProfileEntry> profileEntries = profileAPI.searchProfileEntries(builder.done()).getResult();
        assertEquals(1, profileEntries.size());

        final ProfileEntry profileEntry = profileEntries.get(0);
        assertEquals(parentProfileEntryId, profileEntry.getParentId());
        assertEquals(Long.valueOf(profileEntryElement.elementText("index")), Long.valueOf(profileEntry.getIndex()));
        assertEquals(profileEntryElement.elementText("description"), profileEntry.getDescription());
        assertEquals(profileEntryElement.elementText("type"), profileEntry.getType());
        assertEquals(profileEntryElement.elementText("page"), profileEntry.getPage());
        assertEquals(profileId, profileEntry.getProfileId());

        return profileEntry;
    }

    @Test
    public void check_themes() throws Exception {
        assertNotNull(themeAPI.getDefaultTheme(ThemeType.PORTAL));

        try {
            themeAPI.getDefaultTheme(ThemeType.MOBILE);
        } catch (final BonitaRuntimeException e) {
            if (!(e.getCause().getCause() instanceof SThemeNotFoundException)) {
                fail();
            }
        }
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
