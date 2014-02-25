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
import static org.junit.Assert.assertNotNull;

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
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
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
    
    private static final String ADD_HANDLER_COMMAND = "addHandlerCommand";

    private static final String WAIT_SERVER_COMMAND = "waitServerCommand";

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
    
}
