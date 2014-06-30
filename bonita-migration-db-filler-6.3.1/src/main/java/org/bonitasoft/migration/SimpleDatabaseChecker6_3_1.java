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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;


/**
 * Check that the migrated database is ok
 * @author Elias Ricken de Medeiros
 *
 */
public class SimpleDatabaseChecker6_3_1 extends DatabaseCheckerInitiliazer6_3_1 {
    
    @Test
    public void can_complete_the_execution_of_previous_started_process_and_start_a_new_one() throws Exception {
        //given
        User user = identityApi.getUserByUserName("william.jobs");
        long processDefinitionId = processAPI.getProcessDefinitionId(SimpleDatabaseFiller6_0_2.PROCESS_NAME, SimpleDatabaseFiller6_0_2.PROCESS_VERSION);
        processAPI.startProcess(processDefinitionId);

        //when
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId);
        SearchResult<ProcessInstance> searchResult = processAPI.searchProcessInstances(builder.done());
        
        //then (there are two instance, one created before migration and one created after migration)
        assertThat(searchResult.getCount()).isEqualTo(2);
        
        //when
        for (ProcessInstance processInstance : searchResult.getResult()) {
            HumanTaskInstance taskInstance = waitForUserTask(SimpleDatabaseFiller6_0_2.USER_TASK_NAME, processInstance.getId(), APITestUtil.DEFAULT_TIMEOUT);
            processAPI.assignUserTask(taskInstance.getId(), user.getId());
            processAPI.executeFlowNode(taskInstance.getId());
        }
        
        //then
        for (ProcessInstance processInstance : searchResult.getResult()) {
            waitForProcessToFinish(processInstance.getId(), APITestUtil.DEFAULT_TIMEOUT);
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
}
