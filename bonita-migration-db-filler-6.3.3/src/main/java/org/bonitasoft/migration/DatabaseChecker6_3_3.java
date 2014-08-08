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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class DatabaseChecker6_3_3 extends DatabaseCheckerInitilizer6_3_3 {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_3_3.class.getName());
    }

    @Test
    public void check_migration_of_corrupted_gateways_with_no_waiting_task() throws Exception {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
        builder.filter(ProcessInstanceSearchDescriptor.NAME, "Process With Corrupted Gateways");
        builder.sort(ProcessInstanceSearchDescriptor.START_DATE, Order.ASC);
        final List<ProcessInstance> processInstances = apiTestUtil.getProcessAPI().searchProcessInstances(builder.done()).getResult();
        assertFalse(processInstances.isEmpty());

        // Check no failed state for the gateway
        final ProcessInstance processInstance = processInstances.get(0);
        builder = new SearchOptionsBuilder(0, 1);
        builder.filter(FlowNodeInstanceSearchDescriptor.NAME, "OutGateway");
        builder.filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getFailedState());
        builder.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstance.getId());
        List<ArchivedFlowNodeInstance> archivedFailedGateways = apiTestUtil.getProcessAPI().searchArchivedFlowNodeInstances(builder.done()).getResult();
        assertTrue(archivedFailedGateways.isEmpty());

        final List<FlowNodeInstance> failedGateways = apiTestUtil.getProcessAPI().searchFlowNodeInstances(builder.done()).getResult();
        assertTrue(failedGateways.isEmpty());

        // Check completed state for the gateway
        builder = new SearchOptionsBuilder(0, 1);
        builder.filter(FlowNodeInstanceSearchDescriptor.NAME, "OutGateway");
        builder.filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getNormalFinalState());
        builder.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstance.getId());
        archivedFailedGateways = apiTestUtil.getProcessAPI().searchArchivedFlowNodeInstances(builder.done()).getResult();
        assertFalse(archivedFailedGateways.isEmpty());
    }

    @Test
    public void check_migration_of_corrupted_gateways_with_waiting_task() throws Exception {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
        builder.filter(ProcessInstanceSearchDescriptor.NAME, "Process With Corrupted Gateways");
        builder.sort(ProcessInstanceSearchDescriptor.START_DATE, Order.DESC);
        final List<ProcessInstance> processInstances = apiTestUtil.getProcessAPI().searchProcessInstances(builder.done()).getResult();
        assertFalse(processInstances.isEmpty());

        final ProcessInstance processInstance = processInstances.get(0);
        builder = new SearchOptionsBuilder(0, 1);
        builder.filter(FlowNodeInstanceSearchDescriptor.NAME, "OutGateway");
        builder.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstance.getId());
        final List<ArchivedFlowNodeInstance> archivedFailedGateways = apiTestUtil.getProcessAPI().searchArchivedFlowNodeInstances(builder.done()).getResult();
        assertTrue(archivedFailedGateways.isEmpty());

        // Execute the human task & check the gateway is completed
        final long williamId = apiTestUtil.getIdentityAPI().getUserByUserName("william.jobs").getId();
        builder = new SearchOptionsBuilder(0, 1);
        builder.filter(HumanTaskInstanceSearchDescriptor.NAME, "HumanTask");
        builder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstance.getId());
        final List<HumanTaskInstance> humanTasks = apiTestUtil.getProcessAPI().searchHumanTaskInstances(builder.done()).getResult();
        assertFalse(humanTasks.isEmpty());
        apiTestUtil.getProcessAPI().executeFlowNode(williamId, humanTasks.get(0).getId());
        apiTestUtil.waitForFlowNodeInState(processInstance, "OutGateway", TestStates.getNormalFinalState(), true);
    }
}
