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
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.StartEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.test.ClientEventUtil;
import org.bonitasoft.engine.test.TestStates;

public class DatabaseFiller6_5_1 extends DatabaseFiller6_4_2 {

    public static final String SUB_PROCESS_START_NAME = "subProcessStart";

    public static final String SUB_PROCESS_USER_TASK_NAME = "subStep";

    public static final String SUB_PROCESS_NAME = "eventSubProcess";

    public static final String PARENT_PROCESS_USER_TASK_NAME = "step1";

    public static final String PARENT_END = "end";

    public static final String ACTOR_NAME = "delivery";

    public static final String SIGNAL_NAME = "canStart";

    public static final String PROCESS_WITH_EVENT_SUB_PROCESS = "ProcessWithEventSubProcess";

    public static void main(final String[] args) throws Exception {
        DatabaseFiller6_5_1 databaseFiller = new DatabaseFiller6_5_1();
        databaseFiller.execute(1, 1, 1, 1);
    }

    @Override
    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments) throws Exception {
        Map<String, String> stats = super.fillDatabase(nbProcessesDefinitions, nbProcessInstances, nbWaitingEvents, nbDocuments);
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        ClientEventUtil.deployCommand(apiTestUtil.getSession());

        startProcessWithSignalEventSubProcess();

        ClientEventUtil.undeployCommand(apiTestUtil.getSession());
        apiTestUtil.logoutOnTenant();
        return stats;
    }

    public void startProcessWithSignalEventSubProcess() throws Exception {
        // given
        final ProcessDefinition process = apiTestUtil.deployAndEnableProcessWithActor(buildSubProcessDefinition(buildParentProcessDefinition()).done(),
                ACTOR_NAME, apiTestUtil.getIdentityAPI().getUserByUserName("william.jobs"));
        final ProcessInstance processInstance = apiTestUtil.getProcessAPI().startProcess(process.getId());
        apiTestUtil.waitForFlowNodeInReadyState(processInstance, PARENT_PROCESS_USER_TASK_NAME, true);

        // when
        apiTestUtil.getProcessAPI().sendSignal(SIGNAL_NAME);

        // then
        apiTestUtil.waitForFlowNodeInState(processInstance, PARENT_PROCESS_USER_TASK_NAME, TestStates.ABORTED, true);
        apiTestUtil.waitForFlowNodeInReadyState(processInstance, SUB_PROCESS_USER_TASK_NAME, true);

        final List<ActivityInstance> activities = apiTestUtil.getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertThat(activities).as("should have 2 activities: sub-process flow node and user task").hasSize(2);
        assertThat(activities.get(0).getName()).isEqualTo(SUB_PROCESS_NAME);
        assertThat(activities.get(1).getName()).isEqualTo(SUB_PROCESS_USER_TASK_NAME);

        // execute migration
    }

    public ProcessDefinitionBuilder buildParentProcessDefinition()
            throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_WITH_EVENT_SUB_PROCESS, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask(PARENT_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        builder.addEndEvent(PARENT_END);
        builder.addTransition("start", PARENT_PROCESS_USER_TASK_NAME);
        builder.addTransition(PARENT_PROCESS_USER_TASK_NAME, PARENT_END);
        return builder;
    }

    public ProcessDefinitionBuilder buildSubProcessDefinition(final ProcessDefinitionBuilder builder) throws InvalidExpressionException {
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(SUB_PROCESS_NAME, true).getSubProcessBuilder();
        final StartEventDefinitionBuilder startEventDefinitionBuilder = subProcessBuilder.addStartEvent(SUB_PROCESS_START_NAME);
        startEventDefinitionBuilder.addSignalEventTrigger(SIGNAL_NAME);
        subProcessBuilder.addUserTask(SUB_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(SUB_PROCESS_START_NAME, SUB_PROCESS_USER_TASK_NAME);
        subProcessBuilder.addTransition(SUB_PROCESS_USER_TASK_NAME, "endSubProcess");
        return builder;
    }

}
