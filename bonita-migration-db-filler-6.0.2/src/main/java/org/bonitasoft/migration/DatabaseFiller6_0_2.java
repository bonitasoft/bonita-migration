/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import javax.naming.Context;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.work.WorkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseFiller6_0_2 {

    private final class SpotTransitionHandler implements SHandler<SEvent> {

        private final TenantServiceAccessor instance;

        private int hitnb = 0;

        private final Map<Long, String> transitionThatPointOnToStop;

        private SpotTransitionHandler(final TenantServiceAccessor instance, final Map<Long, String> transitionThatPointOnToStop) {
            this.instance = instance;
            this.transitionThatPointOnToStop = transitionThatPointOnToStop;
        }

        @Override
        public boolean isInterested(final SEvent event) {
            return true;
        }

        @Override
        public void execute(final SEvent event) throws SHandlerExecutionException {
            STransitionInstance transition = (STransitionInstance) event.getObject();
            if (transitionThatPointOnToStop.containsKey(transition.getProcessDefinitionId())
                    && transition.getName().endsWith(transitionThatPointOnToStop.get(transition.getProcessDefinitionId()))) {
                hitnb++;
                try {
                    // rollback the transaction so the transition is not deleted
                    instance.getTransactionService().setRollbackOnly();
                } catch (STransactionException e) {
                    throw new SHandlerExecutionException(e);
                }
                throw new RuntimeException();
            }
        }

        public int getHitnb() {
            return hitnb;
        }
    }

    public static final String BONITA_HOME = "bonita.home";

    static ConfigurableApplicationContext springContext;

    protected final Logger logger = LoggerFactory.getLogger(DatabaseFiller6_0_2.class);

    public static void main(final String[] args) throws Exception {
        DatabaseFiller6_0_2 databaseFiller = new DatabaseFiller6_0_2();
        databaseFiller.execute(1, 1, 1, 1);
    }

    public void execute(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments) throws Exception {
        // copyBonitaHome();
        setup();
        Map<String, String> stats = fillDatabase(nbProcessesDefinitions, nbProcessInstances, nbWaitingEvents, nbDocuments);
        for (Entry<String, String> entry : stats.entrySet()) {
            logger.info(entry.getKey() + ": " + entry.getValue());
        }
        shutdown();
        logger.info("Resulting bonita home is " + System.getProperty("bonita.home"));
    }

    public void copyBonitaHome() throws Exception {
        final String bonitaHome = System.getProperty(BONITA_HOME);
        String tmpdir = System.getProperty("java.io.tmpdir");
        final File destDir = new File(tmpdir + File.separatorChar + "home");
        FileUtils.deleteDirectory(destDir);
        logger.info("Copy original bonita home to " + destDir.getAbsolutePath());
        destDir.mkdir();
        FileUtils.deleteDirectory(destDir);
        FileUtils.copyDirectory(new File(bonitaHome), destDir);
        System.setProperty(BONITA_HOME, destDir.getAbsolutePath());
    }

    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments)
            throws BonitaException, Exception {
        logger.info("Starting to fill the database");
        APISession session = APITestUtil.loginDefaultTenant();
        Map<String, String> stats = new HashMap<String, String>();
        stats.putAll(fillOrganization(session));
        stats.putAll(fillProfiles(session));
        stats.putAll(fillProcesses(session, nbProcessesDefinitions, nbProcessInstances));
        stats.putAll(fillDocuments(session, nbDocuments));
        stats.putAll(fillProcessesWithEvents(session, nbWaitingEvents));
        stats.putAll(fillProcessWithTransitions(session));
        stats.putAll(fillProcessWithStartTimer(session));
        stats.putAll(fillCompletedProcess(session));
        stats.putAll(fillProcessWithMessages(session));
        APITestUtil.logoutTenant(session);
        logger.info("Finished to fill the database");
        return stats;
    }

    private Map<? extends String, ? extends String> fillProcessWithStartTimer(final APISession session) throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithStartTimer", "1.0.");
        builder.addActor("actor");
        builder.addStartEvent("start").addTimerEventTriggerDefinition(TimerType.CYCLE, new ExpressionBuilder().createConstantStringExpression("*/4 * * * * ?"));// every
                                                                                                                                                                // 3
                                                                                                                                                                // secondes
        builder.addUserTask("timerstep", "actor");
        builder.addTransition("start", "timerstep");
        BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
        User hellen = TenantAPIAccessor.getIdentityAPI(session).getUserByUserName("helen.kelly");
        processAPI.addUserToActor("actor", processDefinition, hellen.getId());
        processAPI.enableProcess(processDefinition.getId());
        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(100, 5000, 1, hellen.getId(), processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            throw new IllegalStateException("timer process did not start once");
        }
        Map<String, String> map = new HashMap<String, String>(1);
        map.put("Timer job", "1");
        return map;
    }

    private Map<? extends String, ? extends String> fillProcessWithMessages(final APISession session) throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithSendMessage", "1.0");
        builder.addStartEvent("start");
        builder.addIntermediateThrowEvent("sendToStart").addMessageEventTrigger("message",
                new ExpressionBuilder().createConstantStringExpression("ProcessWithReceiveMessage"));
        builder.addIntermediateThrowEvent("sendToIntermediate").addMessageEventTrigger("message",
                new ExpressionBuilder().createConstantStringExpression("ProcessWithIntermediateReceiveMessage"));
        builder.addTransition("start", "sendToStart");
        builder.addTransition("start", "sendToIntermediate");
        BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        ProcessDefinition sendProcess = processAPI.deploy(archiveBuilder.done());
        processAPI.enableProcess(sendProcess.getId());

        builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithReceiveMessage", "1.0");
        builder.addActor("actor");
        builder.addStartEvent("start").addMessageEventTrigger("message");
        builder.addUserTask("taskTriggeredByMessage", "actor");
        builder.addTransition("start", "taskTriggeredByMessage");
        archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        ProcessDefinition receiveProcess = processAPI.deploy(archiveBuilder.done());
        User favio = TenantAPIAccessor.getIdentityAPI(session).getUserByUserName("favio.riviera");
        processAPI.addUserToActor("actor", receiveProcess, favio.getId());
        processAPI.enableProcess(receiveProcess.getId());

        builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithIntermediateReceiveMessage", "1.0");
        builder.addActor("actor");
        builder.addStartEvent("start");
        builder.addIntermediateCatchEvent("catch").addMessageEventTrigger("message");
        builder.addUserTask("taskTriggeredByIntermediateMessage", "actor");
        builder.addTransition("start", "catch");
        builder.addTransition("catch", "taskTriggeredByIntermediateMessage");
        archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        ProcessDefinition intermediateReceiveProcess = processAPI.deploy(archiveBuilder.done());
        processAPI.addUserToActor("actor", intermediateReceiveProcess, favio.getId());
        processAPI.enableProcess(intermediateReceiveProcess.getId());
        processAPI.startProcess(intermediateReceiveProcess.getId());

        processAPI.startProcess(sendProcess.getId());

        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(100, 10000, 2, favio.getId(), processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            throw new IllegalStateException("catch message did not work");
        }
        processAPI.startProcess(intermediateReceiveProcess.getId());
        Map<String, String> map = new HashMap<String, String>(1);
        map.put("Receive message", "1");
        map.put("Send message", "1");
        return map;
    }

    protected Map<String, String> fillProcessWithTransitions(final APISession session) throws Exception {

        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        IdentityAPI identityApi = TenantAPIAccessor.getIdentityAPI(session);

        final ProcessDefinition processDefinitionEvent = deployProcessWithTransitionToEvent(processAPI);
        final ProcessDefinition processDefinitionPara1 = deployProcessWithGateways(processAPI, identityApi, GatewayType.PARALLEL, 2, "para1");
        final ProcessDefinition processDefinitionPara2 = deployProcessWithGateways(processAPI, identityApi, GatewayType.PARALLEL, 4, "para4");
        final ProcessDefinition processDefinitionInclu1 = deployProcessWithGateways(processAPI, identityApi, GatewayType.INCLUSIVE, 2, "inclu1");
        final ProcessDefinition processDefinitionInclu2 = deployProcessWithGateways(processAPI, identityApi, GatewayType.INCLUSIVE, 2, "inclu2");
        final ProcessDefinition processDefinitionInclu3 = deployProcessWithGateways(processAPI, identityApi, GatewayType.INCLUSIVE, 3, "inclu3");
        final ProcessDefinition processDefinitionExclu2 = deployProcessWithGateways(processAPI, identityApi, GatewayType.EXCLUSIVE, 2, "exclu2");
        HashMap<Long, String> transitions = new HashMap<Long, String>();
        transitions.put(processDefinitionEvent.getId(), "event");
        transitions.put(processDefinitionPara1.getId(), "gate1");
        transitions.put(processDefinitionPara2.getId(), "gate2");
        transitions.put(processDefinitionInclu1.getId(), "gate1");
        transitions.put(processDefinitionInclu2.getId(), "gate2");
        transitions.put(processDefinitionInclu3.getId(), "gate2");
        transitions.put(processDefinitionExclu2.getId(), "gate2");

        final TenantServiceAccessor instance = TenantServiceSingleton.getInstance(session.getTenantId());
        final SpotTransitionHandler userHandler = new SpotTransitionHandler(instance, transitions);
        instance.getEventService().addHandler("TRANSITIONINSTANCE_DELETED", userHandler);

        processAPI.startProcess(processDefinitionEvent.getId());
        processAPI.startProcess(processDefinitionPara1.getId());
        processAPI.startProcess(processDefinitionPara2.getId());
        processAPI.startProcess(processDefinitionInclu1.getId());
        processAPI.startProcess(processDefinitionInclu2.getId());
        processAPI.startProcess(processDefinitionInclu3.getId());
        processAPI.startProcess(processDefinitionExclu2.getId());
        boolean wait = new WaitUntil(100, 5000) {

            @Override
            protected boolean check() throws Exception {
                return userHandler.getHitnb() == 11;// 11 transition will be blocked
            }
        }.waitUntil();
        if (!wait) {
            throw new IllegalStateException("unable to fill db: transitions not reached");
        }
        return Collections.singletonMap("Transitions",
                "11");
    }

    private ProcessDefinition deployProcessWithTransitionToEvent(final ProcessAPI processAPI) throws InvalidBusinessArchiveFormatException,
            InvalidProcessDefinitionException,
            AlreadyExistsException, ProcessDeployException, ProcessDefinitionNotFoundException, ProcessEnablementException {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithTransitionsGoToEvent", "1.0");
        builder.addStartEvent("start");
        builder.addIntermediateCatchEvent("event").addSignalEventTrigger("signal");
        builder.addTransition("start", "event");
        BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.done()).done();
        final ProcessDefinition processDefinition = processAPI.deploy(businessArchive);
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private ProcessDefinition deployProcessWithGateways(final ProcessAPI processAPI, final IdentityAPI identityApi, final GatewayType gatewayType,
            final int nbBranches, final String name)
            throws InvalidBusinessArchiveFormatException,
            InvalidProcessDefinitionException,
            ProcessDeployException, ProcessDefinitionNotFoundException, ProcessEnablementException, InvalidExpressionException, UserNotFoundException,
            ActorNotFoundException, CreationException {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithTransitions" + name, "1.0");
        boolean addCondition = gatewayType != GatewayType.PARALLEL;
        builder.addStartEvent("start");
        builder.addActor("actor");
        builder.addGateway("gate1", gatewayType);
        builder.addGateway("gate2", gatewayType);
        builder.addTransition("start", "gate1");
        for (int i = 1; i <= nbBranches; i++) {
            builder.addAutomaticTask("step" + i);
            if (addCondition) {
                builder.addTransition("gate1", "step" + i, new ExpressionBuilder().createConstantBooleanExpression(i != 1));
                builder.addTransition("step" + i, "gate2", new ExpressionBuilder().createConstantBooleanExpression(i != 1));
            } else {
                builder.addTransition("gate1", "step" + i);
                builder.addTransition("step" + i, "gate2");
            }
        }
        builder.addUserTask("finished_" + name, "actor");
        builder.addEndEvent("end");
        builder.addTransition("gate2", "finished_" + name);
        builder.addTransition("finished_" + name, "end");
        BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.done()).done();
        final ProcessDefinition processDefinition = processAPI.deploy(businessArchive);
        User hellen = identityApi.getUserByUserName("april.sanchez");
        processAPI.addUserToActor("actor", processDefinition, hellen.getId());
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private Map<String, String> fillDocuments(final APISession session, final int nbDocuments) throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        ProcessInstance processInstance = processAPI.getProcessInstances(0, 1, ProcessInstanceCriterion.LAST_UPDATE_ASC).get(0);
        String text1 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore"
                + " et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip";
        String text2 = " ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat"
                + " nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        for (int i = 0; i < nbDocuments; i++) {
            processAPI.attachDocument(processInstance.getId(), "file" + i, "file" + i + ".txt", "plain/text", text1.getBytes());
            processAPI.attachNewDocumentVersion(processInstance.getId(), "file" + i, "file" + i + ".txt", "plain/text", text2.getBytes());
        }
        return Collections.singletonMap("Documents", String.valueOf(nbDocuments));
    }

    public void shutdown() throws Exception {
        final PlatformSession pSession = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        stopPlatformAndTenant(platformAPI);
        logoutPlatform(pSession);
        shutdownWorkService();
    }

    protected void logoutPlatform(final PlatformSession pSession) throws BonitaException {
        APITestUtil.logoutPlatform(pSession);
    }

    protected void stopPlatformAndTenant(final PlatformAPI platformAPI) throws BonitaException {
        APITestUtil.stopPlatformAndTenant(platformAPI, true);
    }

    protected PlatformSession loginPlatform() throws BonitaException {
        return APITestUtil.loginPlatform();
    }

    private void shutdownWorkService() throws Exception {
        WorkService workService = getWorkService();
        Field[] fields = workService.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("threadPoolExecutor")) {
                field.setAccessible(true);
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) field.get(workService);
                tpe.shutdown();
            }
        }
    }

    protected WorkService getWorkService() throws Exception {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor().getWorkService();
    }

    protected Map<String, String> fillProfiles(final APISession session) throws Exception {
        final InputStream xmlStream = getProfilesXMLStream();
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        parameters.put("xmlContent", xmlContent);
        parameters.put("importPolicy", ImportPolicy.MERGE_DUPLICATES);
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
        ProfileAPI profileAPI = TenantAPIAccessor.getProfileAPI(session);
        commandAPI.execute("importProfilesCommand", parameters);
        SearchOptions searchOptions = new SearchOptionsBuilder(0, 1).done();
        Map<String, String> map = new HashMap<String, String>(1);
        map.put("Profiles", String.valueOf(profileAPI.searchProfiles(searchOptions).getCount()));
        return map;
    }

    protected InputStream getProfilesXMLStream() {
        return getClass().getResourceAsStream("profiles.xml");
    }

    protected Map<String, String> fillProcessesWithEvents(final APISession session, final int nbWaitingEvents) throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        for (int i = 0; i < nbWaitingEvents; i++) {
            ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithSignalStart", "1.0." + i);
            builder.addShortTextData("alertType", new ExpressionBuilder().createConstantStringExpression("no_alert"));
            builder.addStartEvent("start").addSignalEventTrigger("startProcesses");
            builder.addAutomaticTask("processAlert").addOperation(
                    new OperationBuilder().createSetDataOperation("alertType", new ExpressionBuilder().createConstantStringExpression("signal")));

            BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
            archiveBuilder.setProcessDefinition(builder.done());
            ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
            processAPI.enableProcess(processDefinition.getId());
        }
        processAPI.sendSignal("startProcesses");
        Map<String, String> map = new HashMap<String, String>(1);
        map.put("Waiting events", String.valueOf(nbWaitingEvents));
        return map;
    }

    protected Map<String, String> fillProcesses(final APISession session, final int nbProcessesDefinitions, final int nbProcessInstances)
            throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        for (int i = 0; i < nbProcessesDefinitions; i++) {

            ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToBeMigrated", "1.0." + i);
            builder.addActor("delivery");
            builder.addBlobData("blobData", new ExpressionBuilder().createGroovyScriptExpression("script", "return [0,1,2,3,4];", Object.class.getName()));
            builder.addShortTextData("shortTextData", new ExpressionBuilder().createConstantStringExpression("the default value"));
            builder.addLongData("longData", new ExpressionBuilder().createConstantLongExpression(123456789));
            builder.addLongTextData("longTextData", new ExpressionBuilder().createConstantStringExpression("the default value"));
            builder.addDateData("dateData", new ExpressionBuilder().createConstantLongExpression(System.currentTimeMillis()));
            UserTaskDefinitionBuilder userTask = builder.addUserTask("ask for adress", "delivery");
            userTask.addConnector("phoneConnector", "org.bonitasoft.phoneconnector", "1.0", ConnectorEvent.ON_ENTER);

            BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
            final InputStream contentAsStream = DatabaseFiller6_0_2.class.getResourceAsStream("PhoneConnector.impl");
            final byte[] content = IOUtils.toByteArray(contentAsStream);
            archiveBuilder.addConnectorImplementation(new BarResource("PhoneConnector.impl", content));
            archiveBuilder.setProcessDefinition(builder.done());
            ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
            processAPI.addUserToActor("delivery", processDefinition, identityAPI.getUserByUserName("william.jobs").getId());
            processAPI.enableProcess(processDefinition.getId());
            for (int j = 0; j < nbProcessInstances; j++) {
                processAPI.startProcess(processDefinition.getId());
            }
        }
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("Process definitions", String.valueOf(nbProcessesDefinitions));
        map.put("Process instances", String.valueOf(nbProcessInstances));
        return map;
    }

    protected Map<String, String> fillCompletedProcess(final APISession session) throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);

        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessThatFinish", "1.0");
        builder.addAutomaticTask("step1");

        BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
        processAPI.enableProcess(processDefinition.getId());
        processAPI.startProcess(processDefinition.getId());
        return new HashMap<String, String>(2);
    }

    protected Map<String, String> fillOrganization(final APISession session) throws Exception {
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        InputStream acme = this.getClass().getResourceAsStream("/org/bonitasoft/engine/identity/ACME.xml");
        identityAPI.importOrganization(IOUtil.read(acme));
        Map<String, String> map = new HashMap<String, String>(3);
        map.put("Users", String.valueOf(identityAPI.getNumberOfUsers()));
        map.put("Groups", String.valueOf(identityAPI.getNumberOfGroups()));
        map.put("Roles", String.valueOf(identityAPI.getNumberOfRoles()));
        return map;
    }

    public void setup() throws BonitaException, IOException, Exception {
        logger.info("Using bonita.home: " + System.getProperty(BONITA_HOME));
        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");
        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
        initializePlatform();
    }

    protected void initializePlatform() throws BonitaException {
        APITestUtil.createInitializeAndStartPlatformWithDefaultTenant(false);
    }

}
