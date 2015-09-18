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

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;

public class DatabaseFiller6_3_9 extends SimpleDatabaseFiller6_3_1 {

    public static void main(final String[] args) throws Exception {
        final DatabaseFiller6_3_9 databaseFiller = new DatabaseFiller6_3_9();
        databaseFiller.execute(1, 1, 1, 1);
    }

    @Override
    protected void initializePlatform() throws BonitaException {
        getAPITestUtil().createInitializeAndStartPlatformWithDefaultTenant(true);
    }

    @Override
    protected void shutdownWorkService() throws Exception {
        System.out.println("no need to stop workService, already stop by tenant");
    }

    @Override
    protected Map<String, String> fillProfiles(final APISession session) throws Exception {
        final InputStream xmlStream = getProfilesXMLStream();
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        parameters.put("xmlContent", xmlContent);
        parameters.put("importPolicy", ImportPolicy.MERGE_DUPLICATES);
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
        final ProfileAPI profileAPI = TenantAPIAccessor.getProfileAPI(session);
        commandAPI.execute("importProfilesCommand", parameters);
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 1).done();
        final Map<String, String> map = new HashMap<String, String>(1);
        map.put("Profiles", String.valueOf(profileAPI.searchProfiles(searchOptions).getCount()));
        return map;
    }

    @Override
    protected InputStream getProfilesXMLStream() {
        return DatabaseFiller6_3_9.class.getResourceAsStream("profiles.xml");
    }

    @Override
    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments)
            throws Exception {
        logger.info("Starting to fill the database");
        getAPITestUtil().loginOnDefaultTenantWithDefaultTechnicalLogger();
        final APISession session = getAPITestUtil().getSession();
        final Map<String, String> stats = new HashMap<String, String>();
        stats.putAll(fillOrganization(session));
        stats.putAll(fillSimpleProcess(session, nbProcessInstances));
        stats.putAll(fillProfiles(session));
        stats.putAll(fillDocuments());
        fillOthers(session);
        getAPITestUtil().loginOnDefaultTenantWithDefaultTechnicalLogger();
        logger.info("Finished to fill the database");
        return stats;
    }

    protected void fillOthers(final APISession session) throws Exception, ServerAPIException, UnknownAPITypeException {

    }


    private Map<? extends String, ? extends String> fillDocuments() throws Exception {
        final User user = getAPITestUtil().createUser("userForDocuments", "bpm");

        final ProcessDefinitionBuilder processWithDocuments = new ProcessDefinitionBuilder().createNewInstance("ProcessWithDocuments", "1.0");
        processWithDocuments.addStartEvent("start");
        processWithDocuments.addUserTask("step1", "actor")
                .addOperation(new OperationBuilder()
                        .createSetDocument("doc1", new ExpressionBuilder()
                                .createGroovyScriptExpression("update doc1",
                                        "return new org.bonitasoft.engine.bpm.document.DocumentValue(\"newContent1\".getBytes(),\"plain/text\",\"file2.txt\")",
                                        DocumentValue.class.getName())))
                .addOperation(new OperationBuilder()
                        .createSetDocument("doc2", new ExpressionBuilder()
                                .createGroovyScriptExpression("create doc2",
                                        "return new org.bonitasoft.engine.bpm.document.DocumentValue(\"newContent2\".getBytes(),\"plain/text\",\"newFile.txt\")",
                                        DocumentValue.class.getName())));
        processWithDocuments.addUserTask("step2", "actor")
                .addTransition("start", "step1").addTransition("step1", "step2");
        processWithDocuments.addDocumentDefinition("doc1").addContentFileName("file.txt").addFile("file.txt").addMimeType("plain/text").addDescription("It is a text file");
        processWithDocuments.addActor("actor");
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.addDocumentResource(new BarResource("file.txt", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.".getBytes()));
        businessArchiveBuilder.setProcessDefinition(processWithDocuments.done());

        final ProcessDefinition processDefinition = getAPITestUtil().deployAndEnableProcessWithActor(businessArchiveBuilder.done(), "actor", user);

        //finished instance
        final ProcessInstance inst1 = getAPITestUtil().getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance step1 = getAPITestUtil().waitForUserTask("step1", inst1);
        getAPITestUtil().assignAndExecuteStep(step1, user.getId());
        final HumanTaskInstance step2 = getAPITestUtil().waitForUserTask("step2", inst1);
        getAPITestUtil().assignAndExecuteStep(step2, user.getId());
        getAPITestUtil().waitForProcessToFinish(inst1);

        //instance with step1 having operations executed
        final ProcessInstance inst2 = getAPITestUtil().getProcessAPI().startProcess(processDefinition.getId());
        step1 = getAPITestUtil().waitForUserTask("step1", inst2);
        getAPITestUtil().assignAndExecuteStep(step1, user.getId());
        getAPITestUtil().waitForUserTask("step2", inst2);

        //instance with document attached using api
        final ProcessInstance inst3 = getAPITestUtil().getProcessAPI().startProcess(processDefinition.getId());
        getAPITestUtil().waitForUserTask("step1", inst3);
        getAPITestUtil().getProcessAPI().attachDocument(inst3.getId(), "documentAttachedUsingAPI", "doc.txt", "plain/text",
                "The content of the file attached using the api".getBytes());
        getAPITestUtil().getProcessAPI().attachDocument(inst3.getId(), "urlDocumentAttachedUsingAPI", "doc.txt", "plain/text", "http://MyWebSite.com/file.txt");
        getAPITestUtil().getProcessAPI().attachNewDocumentVersion(inst3.getId(), "documentAttachedUsingAPI", "doc2.txt", "plain/text",
                "The content of the file attached using the api2".getBytes());
        getAPITestUtil().getProcessAPI().attachNewDocumentVersion(inst3.getId(), "urlDocumentAttachedUsingAPI", "doc2.txt", "plain/text",
                "http://MyWebSite.com/file2.txt");


        // just started instance
        final ProcessInstance inst4 = getAPITestUtil().getProcessAPI().startProcess(processDefinition.getId());
        getAPITestUtil().waitForUserTask("step1", inst4);


        return Collections.emptyMap();
    }


}
