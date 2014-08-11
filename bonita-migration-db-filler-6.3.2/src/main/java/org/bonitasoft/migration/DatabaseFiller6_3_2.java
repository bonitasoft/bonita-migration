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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestStates;

public class DatabaseFiller6_3_2 extends SimpleDatabaseFiller6_3_1 {

    private final APITestUtil apiTestUtil = new APITestUtil();

    public static void main(final String[] args) throws Exception {
        final DatabaseFiller6_3_2 databaseFiller = new DatabaseFiller6_3_2();
        databaseFiller.execute(1, 1, 1, 1);
    }

    @Override
    protected void initializePlatform() throws BonitaException {
        apiTestUtil.createInitializeAndStartPlatformWithDefaultTenant(true);
    }

    @Override
    public void shutdown() throws Exception {
        final PlatformSession pSession = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        stopPlatformAndTenant(platformAPI);
        logoutPlatform(pSession);
    }

    public void startNode() throws BonitaException, IOException, Exception {
        final PlatformSession session = apiTestUtil.loginOnPlatform();
        final PlatformAPI platformAPI = apiTestUtil.getPlatformAPI(session);
        platformAPI.startNode();
        apiTestUtil.logoutOnPlatform(session);
    }

    public void stopNode() throws Exception {
        final PlatformSession pSession = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        platformAPI.stopNode();
        logoutPlatform(pSession);
    }

    @Override
    protected InputStream getProfilesXMLStream() {
        return getClass().getResourceAsStream("profiles.xml");
    }

    @Override
    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments)
            throws BonitaException, Exception {
        logger.info("Starting to fill the database");
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
        final APISession session = apiTestUtil.getSession();
        final Map<String, String> stats = new HashMap<String, String>();
        stats.putAll(fillOrganization(session));
        stats.putAll(fillProfiles(session));
        stats.putAll(fill_with_corrupted_gateways());
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
        logger.info("Finished to fill the database");
        return stats;
    }

    public Map<? extends String, ? extends String> fill_with_corrupted_gateways() throws Exception {
        final long williamId = apiTestUtil.getIdentityAPI().getUserByUserName("william.jobs").getId();

        final String actorName = "delivery";
        final String humanTaskName = "HumanTask";
        final String inGatewayName = "InGateway";
        final String outGatewayName = "OutGateway";
        final String autoTaskName = "AutoTask";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Process With Corrupted Gateways", PROCESS_VERSION);
        builder.addActor(actorName).addAutomaticTask(autoTaskName).addUserTask(humanTaskName, actorName).addStartEvent("Start");
        builder.addEndEvent("end");
        builder.addGateway(inGatewayName, GatewayType.PARALLEL).addGateway(outGatewayName, GatewayType.INCLUSIVE);
        builder.addTransition("Start", inGatewayName).addTransition(inGatewayName, autoTaskName).addTransition(inGatewayName, humanTaskName)
                .addTransition(autoTaskName, outGatewayName).addTransition(humanTaskName, outGatewayName).addTransition(outGatewayName,"end");

        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(builder.done(), actorName, williamId);
        // Start process with the gateway with the state "failed" & the executed human task
        final ProcessInstance processInstance = apiTestUtil.getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance humanTaskInstance = apiTestUtil.waitForUserTaskAndAssigneIt(humanTaskName, processInstance.getId(), williamId);
        waitForGateway(outGatewayName, processInstance.getId(), TestStates.getExecutingState());

        // Start process with the gateway with the state "failed" & the not executed human task
        final ProcessInstance processInstance2 = apiTestUtil.getProcessAPI().startProcess(processDefinition.getId());
        apiTestUtil.waitForUserTaskAndAssigneIt(humanTaskName, processInstance2.getId(), williamId);
        waitForGateway(outGatewayName, processInstance2.getId(), TestStates.getExecutingState());

        // Stop & Restart the node
        stopNode();
        startNode();
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();

        apiTestUtil.getProcessAPI().executeFlowNode(williamId, humanTaskInstance.getId());
        waitForGateway(outGatewayName, processInstance.getId(), TestStates.getFailedState());

        waitForGateway(outGatewayName, processInstance2.getId(), TestStates.getFailedState());

        return Collections.singletonMap("Process definitions", String.valueOf(1));
    }

    protected void waitForGateway(final String gatewayName, final long processInstanceId, final String state) throws Exception {
        long now = System.currentTimeMillis();
        SearchResult<FlowNodeInstance> searchResult;
        do {
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
            builder.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstanceId);
            builder.filter(FlowNodeInstanceSearchDescriptor.NAME, gatewayName);
            builder.filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, state);
            searchResult = apiTestUtil.getProcessAPI().searchFlowNodeInstances(builder.done());
        } while (searchResult.getCount() == 0 && now + APITestUtil.DEFAULT_TIMEOUT > System.currentTimeMillis());
        assertEquals(1, searchResult.getCount());
    }
}
