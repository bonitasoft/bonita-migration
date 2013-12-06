package org.bonitasoft.migration;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.naming.Context;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseChecker6_1_0 {

    private static ClassPathXmlApplicationContext springContext;

    private static ProcessAPI processAPI;

    private static IdentityAPI identityApi;

    private static APISession session;

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_1_0.class.getName());
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
        long id = identityApi.getUserByUserName("april.sanchez").getId();
        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(50, 10000, 6, id, processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            String message = "Not all task after transitions were created";
            System.out.println(message);
            throw new IllegalStateException(message);
        } else {
            System.out.println("all tasks found");
            List<HumanTaskInstance> pendingHumanTaskInstances = processAPI.getPendingHumanTaskInstances(id, 0, 100, ActivityInstanceCriterion.NAME_ASC);
            for (HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
                System.out.println("task: " + humanTaskInstance.getName());
            }
        }
    }

    @Test
    public void check_archivedProcessInstance_can_be_retrive() throws Exception {
        long processDefinitionId = processAPI.getProcessDefinitionId("ProcessThatFinish", "1.0");
        System.out.println("processDefinitionId= " + processDefinitionId);
        SearchResult<ArchivedProcessInstance> archivedProcessInstances = processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 10).filter(
                ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId).done());
        System.out.println(archivedProcessInstances.getResult());
        assertTrue(archivedProcessInstances.getCount() > 0);
    }

    @Test
    public void check_process_with_messages_still_work() throws Exception {
        long processDefinitionId = processAPI.getProcessDefinitionId("ProcessWithSendMessage", "1.0");

        User favio = TenantAPIAccessor.getIdentityAPI(session).getUserByUserName("favio.riviera");

        int pendingTaskOfFavio = Long.valueOf(processAPI.getNumberOfPendingHumanTaskInstances(favio.getId())).intValue();

        processAPI.startProcess(processDefinitionId);

        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(100, 20000, pendingTaskOfFavio + 1, favio.getId(), processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            throw new IllegalStateException("throw/catch message don't work");
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
