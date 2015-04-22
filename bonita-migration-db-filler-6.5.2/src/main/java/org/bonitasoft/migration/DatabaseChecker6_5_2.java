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

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.junit.runner.JUnitCore;


public class DatabaseChecker6_5_2 extends SimpleDatabaseChecker6_5_0 {

    public static final String SUB_PROCESS_USER_TASK_NAME = "subStep";

    public static final String SUB_PROCESS_NAME = "eventSubProcess";

    public static final String PROCESS_WITH_EVENT_SUB_PROCESS = "ProcessWithEventSubProcess";
    
    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_5_2.class.getName());
    }
    
    @Override
    protected Document getProfilesXML(final SAXReader reader) throws Exception {
        return reader.read(DatabaseChecker6_5_2.class.getResource("profiles.xml"));
    }

    @Test
    public void event_sub_process_should_be_available_after_migration() throws Exception {
        getApiTestUtil().loginOnDefaultTenantWithDefaultTechnicalUser();

        //given
        long definitionId = getApiTestUtil().getProcessAPI().getProcessDefinitionId(PROCESS_WITH_EVENT_SUB_PROCESS, SimpleDatabaseFiller6_0_2.PROCESS_VERSION);
        SearchResult<ProcessInstance> searchResult = getRootProcessInstancesOfDefinition(definitionId);
        assertThat(searchResult.getResult()).hasSize(1);

        //when
        ProcessInstance processInstance = searchResult.getResult().get(0);
        final List<ActivityInstance> activities = getApiTestUtil().getProcessAPI().getActivities(processInstance.getId(), 0, 10);

        //then: task should be available after migration
        assertThat(activities).as("should have 2 activities: sub-process flow node and user task").hasSize(2);
        assertThat(activities.get(0).getName()).isEqualTo(SUB_PROCESS_NAME);
        ActivityInstance subProcUserTask = activities.get(1);
        assertThat(subProcUserTask.getName()).isEqualTo(SUB_PROCESS_USER_TASK_NAME);

        ProcessInstance subProcessInstance = getApiTestUtil().getProcessAPI().getProcessInstance(subProcUserTask.getParentProcessInstanceId());
        getApiTestUtil().assignAndExecuteStep(subProcUserTask, getApiTestUtil().getIdentityAPI().getUserByUserName("william.jobs"));
        getApiTestUtil().waitForActivityInCompletedState(processInstance, SUB_PROCESS_USER_TASK_NAME, true);
        getApiTestUtil().waitForProcessToFinish(subProcessInstance);
        getApiTestUtil().waitForActivityInCompletedState(processInstance, SUB_PROCESS_NAME, true);
        getApiTestUtil().waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

        getApiTestUtil().logoutOnTenant();
    }

    protected SearchResult<ProcessInstance> getRootProcessInstancesOfDefinition(final long processDefinitionId) throws SearchException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 100);
        builder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId);
        builder.filter(ProcessInstanceSearchDescriptor.CALLER_ID, -1);
        return getProcessAPI().searchProcessInstances(builder.done());
    }
}
