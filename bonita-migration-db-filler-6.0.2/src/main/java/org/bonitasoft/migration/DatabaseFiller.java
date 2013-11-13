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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.io.IOUtil;
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
        databaseFiller.execute();
    }

    private void execute() throws Exception {
        logger.info("Using bonita.home: " + System.getProperty("bonita.home"));
        // TestsInitializer.beforeAll();
        setup();
        logger.info("Starting to fill the database");
        APISession session = APITestUtil.loginDefaultTenant();
        ArrayList<String> stats = new ArrayList<String>();
        stats.addAll(fillOrganization(session));
        stats.addAll(fillProfiles(session));
        stats.addAll(fillProcesses(session));
        APITestUtil.logoutTenant(session);
        logger.info("Finished to fill the database");
        for (String string : stats) {
            logger.info(string);
        }
        final PlatformSession pSession = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        APITestUtil.stopPlatformAndTenant(platformAPI, false);
        APITestUtil.logoutPlatform(pSession);
        // stop node do not stop correctly all threads
        System.exit(0);
    }

    private Collection<? extends String> fillProfiles(final APISession session) throws Exception {
        final InputStream xmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("InitProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        parameters.put("xmlContent", xmlContent);
        parameters.put("importPolicy", ImportPolicy.MERGE_DUPLICATES);
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
        ProfileAPI profileAPI = TenantAPIAccessor.getProfileAPI(session);
        commandAPI.execute("importProfilesCommand", parameters);
        SearchOptions searchOptions = new SearchOptionsBuilder(0, 1).done();
        return Arrays.asList("profiles: " + profileAPI.searchProfiles(searchOptions).getCount());
    }

    private Collection<? extends String> fillProcesses(final APISession session) throws Exception {
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        builder.addActor("theActor");
        builder.addUserTask("step1", "theActor");
        BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
        processAPI.addUserToActor("theActor", processDefinition, identityAPI.getUserByUserName("william.jobs").getId());
        processAPI.enableProcess(processDefinition.getId());
        processAPI.startProcess(processDefinition.getId());
        processAPI.startProcess(processDefinition.getId());
        processAPI.startProcess(processDefinition.getId());
        return Arrays.asList("definitions: " + 1, "instances: " + 3);
    }

    private List<String> fillOrganization(final APISession session) throws Exception {
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        InputStream acme = this.getClass().getResourceAsStream("/org/bonitasoft/engine/identity/ACME.xml");
        identityAPI.importOrganization(IOUtil.read(acme));
        return Arrays.asList("Users: " + identityAPI.getNumberOfUsers(), "Groups: " + identityAPI.getNumberOfGroups(),
                "Roles: " + identityAPI.getNumberOfRoles());
    }

    static ConfigurableApplicationContext springContext;

    public void setup() throws BonitaException, IOException {
        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");
        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
        APITestUtil.createInitializeAndStartPlatformWithDefaultTenant(false);
    }

}
