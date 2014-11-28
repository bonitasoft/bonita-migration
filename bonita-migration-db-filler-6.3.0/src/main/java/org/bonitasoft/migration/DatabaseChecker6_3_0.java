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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.sql.DataSource;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.WaitUntil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Check that the migrated database is ok
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class DatabaseChecker6_3_0 {

    protected static ProcessAPI processAPI;

    protected static ProfileAPI profileAPI;

    protected static IdentityAPI identityAPI;

    protected static CommandAPI commandAPI;

    protected static APISession session;

    private static ClassPathXmlApplicationContext springContext;

    private final Logger logger = LoggerFactory.getLogger(DatabaseChecker6_3_0.class);

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_3_0.class.getName());
    }

    @BeforeClass
    public static void setup() throws BonitaException {
        setupSpringContext();
        final APITestUtil apiTestUtil = new APITestUtil();
        final PlatformSession platformSession = apiTestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        apiTestUtil.logoutPlatform(platformSession);
        session = apiTestUtil.loginDefaultTenant();
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        profileAPI = TenantAPIAccessor.getProfileAPI(session);
        commandAPI = TenantAPIAccessor.getCommandAPI(session);
    }

    @AfterClass
    public static void teardown() throws BonitaException {
        final APITestUtil apiTestUtil = new APITestUtil();
        apiTestUtil.logoutTenant(session);
        final PlatformSession pSession = apiTestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        apiTestUtil.stopPlatformAndTenant(platformAPI, false);
        apiTestUtil.logoutPlatform(pSession);
        springContext.close();
    }

    private static void setupSpringContext() {
        System.setProperty("sysprop.bonita.db.vendor", System.getProperty("sysprop.bonita.db.vendor", "h2"));

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");

        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
    }

    @Test
    public void ref_business_data_table_has_been_created() throws Exception {
        final DataSource bonitaDatasource = (DataSource) springContext.getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        jdbcTemplate.update("INSERT INTO ref_biz_data_inst(tenantid, id, name, proc_inst_id, data_id, data_classname) "
                + "VALUES (?, ?, ?, ?, ?, ?)", new Object[] { 1, 1, "businessdata", 1, 1, "org.bonitasoft.classname" });

        final long numberOfRefBusinessdata = countRefBusinessdata(jdbcTemplate);
        assertEquals(1, numberOfRefBusinessdata);
    }

    private long countRefBusinessdata(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForLong("SELECT COUNT(id) FROM ref_biz_data_inst");
    }

    @Test
    public void ref_business_data_sequence_have_been_created() throws Exception {
        final DataSource sequenceDatasource = (DataSource) springContext.getBean("bonitaSequenceManagerDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(sequenceDatasource);

        final long numberOfTenants = countTenants(jdbcTemplate);
        final long numberOfNewSequences = countRefBusinessDataSequences(jdbcTemplate);

        assertEquals(numberOfTenants, numberOfNewSequences);
    }

    private long countRefBusinessDataSequences(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForLong("SELECT COUNT(*) FROM sequence WHERE id = 10096");
    }

    private long countTenants(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForLong("SELECT COUNT(id) FROM tenant");
    }

    @Test
    public void runIt() throws Exception {
        processAPI.getNumberOfProcessInstances();

    }

    @Test
    public void check_jobs_work() throws Exception {
        final User user = identityAPI.getUserByUserName("john");
        assertNotNull("user is null", user);

        // wait for quartz + bpm eventHandling to have started and restarted missed timers
        assertTrue(
                "there was less than 4 task for "
                        + user.getUserName()
                        + ", he should have more than 3 because when bonita was shut down it should restart missed timers (the timer is 10 seconds, we had one task ready, we waited 60 seconds"
                        + processAPI.getPendingHumanTaskInstances(user.getId(), 0, 100, ActivityInstanceCriterion.DEFAULT).size(),
                new WaitUntil(500, 120000) {

                    @Override
                    protected boolean check() throws Exception {
                        return processAPI.getPendingHumanTaskInstances(user.getId(), 0, 100,
                                ActivityInstanceCriterion.DEFAULT).size() > 3;
                    }
                }.waitUntil());
    }

    @Test
    public void check_profiles() throws Exception {
        final SAXReader reader = new SAXReader();
        final Document document = getProfilesXML(reader);
        final Element profiles = document.getRootElement();

        // Iterate through child elements of root with element name "profile"
        for (final Iterator<Element> rootIterator = profiles.elementIterator("profile"); rootIterator.hasNext();) {
            final Element profileElement = rootIterator.next();
            final Profile profile = checkProfile(profileElement);

            final Element profileEntriesElement = profileElement.element("profileEntries");
            if (profileEntriesElement != null) {
                for (final Iterator<Element> parentProfileEntryIterator = profileEntriesElement.elementIterator("parentProfileEntry"); parentProfileEntryIterator
                        .hasNext();) {
                    final Element parentProfileEntryElement = parentProfileEntryIterator.next();
                    final ProfileEntry profileEntry = checkProfileEntry(parentProfileEntryElement, profile.getId(), 0);

                    final Element childProfileEntriesElement = profileElement.element("childrenEntries");
                    if (childProfileEntriesElement != null) {
                        for (final Iterator<Element> childProfileEntryIterator = childProfileEntriesElement.elementIterator("profileEntry"); childProfileEntryIterator
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
        final String name = profileElement.attributeValue("name");
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter("name", name);
        final List<Profile> resultProfiles = profileAPI.searchProfiles(builder.done()).getResult();
        assertEquals("Profile " + name + " not found.", 1, resultProfiles.size());

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

        logger.info("The profile " + name + " is the same in database as the XML file.");

        return profile;
    }

    private ProfileEntry checkProfileEntry(final Element profileEntryElement, final long profileId, final long parentProfileEntryId) throws SearchException {
        final String name = profileEntryElement.attributeValue("name");
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort("name", Order.DESC);
        builder.filter("name", name);
        builder.filter("parentId", parentProfileEntryId);
        builder.filter("profileId", profileId);
        final List<ProfileEntry> profileEntries = profileAPI.searchProfileEntries(builder.done()).getResult();
        assertEquals("Profile entry " + name + " not found for profile " + profileId + ", and parent profile entry " + parentProfileEntryId + ".", 1,
                profileEntries.size());

        final ProfileEntry profileEntry = profileEntries.get(0);
        assertEquals(parentProfileEntryId, profileEntry.getParentId());
        assertEquals(Long.valueOf(profileEntryElement.elementText("index")), Long.valueOf(profileEntry.getIndex()));
        assertEquals(profileEntryElement.elementText("description"), profileEntry.getDescription());
        assertEquals(profileEntryElement.elementText("type"), profileEntry.getType());
        assertEquals(profileEntryElement.elementText("page"), profileEntry.getPage());
        assertEquals(profileId, profileEntry.getProfileId());

        logger.info("The profile entry " + name + " is the same in database as the XML file.");

        return profileEntry;
    }

    @Test
    public void check_process_started_for() throws Exception {
        final User william = identityAPI.getUserByUserName("william.jobs");
        final User walter = identityAPI.getUserByUserName("walter.bates");

        // Check if william is started for, and walter is started by for the process instance
        final SearchOptionsBuilder builderBefore = new SearchOptionsBuilder(0, 1);
        builderBefore.filter(ProcessInstanceSearchDescriptor.NAME, "ProcessStartedFor");
        final List<ProcessInstance> processInstances = processAPI.searchProcessInstances(builderBefore.done()).getResult();
        assertNotNull(processInstances);
        final ProcessInstance processInstance = processInstances.get(0);
        assertEquals(processInstance.getStartedBy(), william.getId());
        assertEquals(processInstance.getStartedBySubstitute(), walter.getId());

        // Check if william is executed for, and walter is executed by for the activity instance
        final SearchOptionsBuilder builderAfter = new SearchOptionsBuilder(0, 1);
        builderAfter.filter(HumanTaskInstanceSearchDescriptor.NAME, "step1");
        builderAfter.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, "completed");
        builderAfter.filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstance.getId());
        final List<ArchivedHumanTaskInstance> archivedHumanTaskInstances = processAPI.searchArchivedHumanTasks(builderAfter.done()).getResult();
        assertNotNull(archivedHumanTaskInstances);
        final ArchivedHumanTaskInstance archivedHumanTaskInstance = archivedHumanTaskInstances.get(0);
        assertEquals(archivedHumanTaskInstance.getExecutedBy(), william.getId());
    }

    @Test
    public void can_create_custom_user_info_definition_and_values() throws Exception {
        final User user = identityAPI.createUser("first.user", "bpm");
        final CustomUserInfoDefinition skills = identityAPI.createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("Skills", "The user skills"));
        identityAPI.setCustomUserInfoValue(skills.getId(), user.getId(), "Java");

        final List<CustomUserInfo> userInfo = identityAPI.getCustomUserInfo(user.getId(), 0, 10);
        assertThat(userInfo.size()).isEqualTo(1);
        assertThat(userInfo.get(0).getDefinition().getName()).isEqualTo("Skills");
        assertThat(userInfo.get(0).getValue()).isEqualTo("Java");
    }

}
