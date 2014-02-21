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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * Launch Database filler but with 6.1.0 dependencies
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * 
 */
@SuppressWarnings("deprecation")
public class DatabaseFiller6_2_2 extends DatabaseFiller6_0_2 {

    public static void main(final String[] args) throws Exception {
        DatabaseFiller6_2_2 databaseFiller = new DatabaseFiller6_2_2();
        databaseFiller.execute(1, 1, 1, 1);
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.migration.DatabaseFiller6_0_2#fillDatabase(int, int, int, int)
     */
    @Override
    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments)
            throws BonitaException,
            Exception {
        logger.info("Starting to fill the database");
        APISession session = APITestUtil.loginDefaultTenant();
        Map<String, String> stats = new HashMap<String, String>();
        stats.putAll(fillOrganization(session));
        stats.putAll(fillProcesses(session, nbProcessesDefinitions, nbProcessInstances));
        stats.putAll(fillProcessesWithEvents(session, nbWaitingEvents));
        stats.putAll(fillCompletedProcess(session));

        // 6.2.3 specific
        stats.putAll(fillDependencies(session));

        APITestUtil.logoutTenant(session);
        logger.info("Finished to fill the database");
        return stats;
    }

    /**
     * 
     * @param session
     * @return
     * @throws Exception
     */
    private Map<? extends String, ? extends String> fillDependencies(final APISession session) throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        User user = identityAPI.createUser("dependencyUser", "dependencyUser");

        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final InputStream stream = this.getClass().getResourceAsStream("/custom-0.1.jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        builder.addClasspathResource(new BarResource("custom-0.1.jar", byteArray));

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("ProcessWithCustomData", "1.0");
        designProcessDefinition.addData("adress", "org.bonitasoft.custom.Address", new ExpressionBuilder().createGroovyScriptExpression("create adress",
                "new org.bonitasoft.custom.Address(\"name1\",\"Rue ampère\",\"38000\",\"Grenoble\",\"France\")", "org.bonitasoft.custom.Address"));
        designProcessDefinition
                .addActor("Workers")
                .addUserTask("step1", "Workers")
                .addOperation(
                        new OperationBuilder().createJavaMethodOperation("adress", "setName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression("myAddress")));
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers").addTransition("start", "step1").addTransition("step1", "step2");
        builder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition = processAPI.deploy(builder.done());
        processAPI.addUserToActor("Workers", processDefinition, user.getId());
        processAPI.enableProcess(processDefinition.getId());
        processAPI.startProcess(processDefinition.getId());

        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(100, 5000, 1, user.getId(), processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            throw new IllegalStateException("process with custom jar don't work");
        }
        CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
        commandAPI.addDependency("MyDep", new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        return Collections.emptyMap();
    }

    @Override
    public void setup() throws BonitaException, IOException, Exception {
        setupSpringContext();
        APITestUtil.createPlatformStructure();
        APITestUtil.initializeAndStartPlatformWithDefaultTenant(true);
    }

    @Override
    public void shutdown() throws BonitaException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final PlatformSession pSession = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        APITestUtil.stopPlatformAndTenant(platformAPI, false);
        APITestUtil.logoutPlatform(pSession);
        closeSpringContext();
        System.out.println("=======");
        System.out.println("Still active threads after shutdown:");
        Iterator<Thread> iterator = Thread.getAllStackTraces().keySet().iterator();
        while (iterator.hasNext()) {
            Thread thread = iterator.next();
            System.out.println(thread.getName());
        }
        System.out.println("=======");
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
