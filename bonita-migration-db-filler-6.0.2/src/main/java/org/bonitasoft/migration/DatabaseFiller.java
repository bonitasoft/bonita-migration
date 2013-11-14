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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseFiller {

    private final Logger logger = LoggerFactory.getLogger(DatabaseFiller.class);

    public static void main(final String[] args) throws Exception {
        DatabaseFiller databaseFiller = new DatabaseFiller();
        databaseFiller.execute(5, 500, 50);
    }

    public void execute(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents) throws Exception {
        setup();
        Map<String, String> stats = fillDatabase(nbProcessesDefinitions, nbProcessInstances, nbWaitingEvents);
        for (Entry<String, String> entry : stats.entrySet()) {
            logger.info(entry.getKey() + ": " + entry.getValue());
        }
        shutdown();
    }

    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents) throws BonitaException,
            Exception {
        logger.info("Starting to fill the database");
        APISession session = APITestUtil.loginDefaultTenant();
        Map<String, String> stats = new HashMap<String, String>();
        stats.putAll(fillOrganization(session));
        stats.putAll(fillProfiles(session));
        stats.putAll(fillProcesses(session, nbProcessesDefinitions, nbProcessInstances));
        stats.putAll(fillProcessesWithEvents(session, nbWaitingEvents));
        APITestUtil.logoutTenant(session);
        logger.info("Finished to fill the database");
        return stats;
    }

    public void shutdown() throws BonitaException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final PlatformSession pSession = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        APITestUtil.stopPlatformAndTenant(platformAPI, false);
        APITestUtil.logoutPlatform(pSession);
    }

    private Map<String, String> fillProfiles(final APISession session) throws Exception {
        final InputStream xmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("InitProfiles.xml");
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

    private Map<String, String> fillProcessesWithEvents(final APISession session, final int nbWaitingEvents) throws Exception {
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

    private Map<String, String> fillProcesses(final APISession session, final int nbProcessesDefinitions, final int nbProcessInstances)
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
            final InputStream contentAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("PhoneConnector.impl");
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

    private Map<String, String> fillOrganization(final APISession session) throws Exception {
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        InputStream acme = this.getClass().getResourceAsStream("/org/bonitasoft/engine/identity/ACME.xml");
        identityAPI.importOrganization(IOUtil.read(acme));
        Map<String, String> map = new HashMap<String, String>(3);
        map.put("Users", String.valueOf(identityAPI.getNumberOfUsers()));
        map.put("Groups", String.valueOf(identityAPI.getNumberOfGroups()));
        map.put("Roles", String.valueOf(identityAPI.getNumberOfRoles()));
        return map;
    }

    static ConfigurableApplicationContext springContext;

    public void setup() throws BonitaException, IOException {
        logger.info("Using bonita.home: " + System.getProperty("bonita.home"));
        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");
        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
        APITestUtil.createInitializeAndStartPlatformWithDefaultTenant(false);
    }

}
