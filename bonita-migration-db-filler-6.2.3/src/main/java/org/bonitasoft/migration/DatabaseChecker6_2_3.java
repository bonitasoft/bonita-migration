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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.junit.runner.JUnitCore;

/**
 * 
 * 
 * Check that the migrated database is ok
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * 
 */
public class DatabaseChecker6_2_3 extends DatabaseChecker6_2_2 {
    
    private static int DEFAULT_TIMEOUT = APITestUtil.DEFAULT_TIMEOUT;
    
    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_2_3.class.getName());
    }
    

    @Override
    @Test
    public void runIt() throws Exception {
        processAPI.getNumberOfProcessInstances();
        
    }

    @Test
    public void check_process_with_dependencies_still_work() throws Exception {
        User user = identityApi.getUserByUserName("dependencyUser");

        long processDefinitionId = processAPI.getProcessDefinitionId("ProcessWithCustomData", "1.0");

        processAPI.startProcess(processDefinitionId);

        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(100, 5000, 1, user.getId(), processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            throw new IllegalStateException("process with custom jar don't work");
        }
    }
    
    @Test
    public void check_that_process_migrated_during_active_boundary_can_continue_the_execution_with_exception_flow_and_normal_flow() throws Exception {
        User user = identityApi.getUserByUserName("william.jobs");
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.NAME, "ProcessWithBoundaryToBeMigrated");
        SearchResult<ProcessInstance> searchResult = processAPI.searchProcessInstances(builder.done());
        assertEquals(2, searchResult.getCount());
        
        List<ProcessInstance> processInstances = searchResult.getResult();
        
        //execute the first process instance without triggering the boundary event
        executeStepAndWaitForProcessCompletion(user, processInstances.get(0).getId(), "step1");
        
        //trigger the boundary event for the second process instance
        //the boundary event will be caught and the execution takes the exception flow
        processAPI.sendSignal("go");
        executeStepAndWaitForProcessCompletion(user, processInstances.get(1).getId(), "exceptionStep");
        
    }

    private void executeStepAndWaitForProcessCompletion(User user, long processInstanceId, String taskName) throws Exception, UpdateException,
            FlowNodeExecutionException {
        HumanTaskInstance userTask = waitForUserTask(taskName, processInstanceId, DEFAULT_TIMEOUT);
        processAPI.assignUserTask(userTask.getId(), user.getId());
        processAPI.executeFlowNode(user.getId(), userTask.getId());
        waitForProcessToFinish(processInstanceId, DEFAULT_TIMEOUT);
    }
    
    private HumanTaskInstance waitForUserTask(final String taskName, final long processInstanceId, final int timeout) throws Exception {
        long now = System.currentTimeMillis();
        SearchResult<ActivityInstance> searchResult;
        do {
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0,1);
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
    
    private void waitForProcessToFinish(final long processInstanceId, final int timeout) throws Exception {
        long now = System.currentTimeMillis();
        SearchResult<ArchivedProcessInstance> searchResult;
        do {
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0,1);
            builder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId);
            builder.filter(ArchivedProcessInstancesSearchDescriptor.STATE_ID, ProcessInstanceState.COMPLETED.getId());
           searchResult = processAPI.searchArchivedProcessInstances(builder.done());
        } while (searchResult.getCount() == 0 && now + timeout > System.currentTimeMillis());
        assertEquals(1, searchResult.getCount());
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
