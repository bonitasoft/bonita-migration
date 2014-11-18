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
import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentsSearchDescriptor;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.impl.DocumentListDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.home.BonitaHomeClient;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseChecker6_4_0 extends DatabaseCheckerInitiliazer6_4_0 {

    private static final String KIND = "012345678912345";

    private static final String CLASSNAME = "org.bonitasoft.classname";

    private static final int PROCESS_INSTANCE_ID1 = 10000;

    private static final int PROCESS_INSTANCE_ID2 = 10001;

    private static final int FLOWNODE_INSTANCE_ID1 = 10000;

    private static final int FLOWNODE_INSTANCE_ID2 = 10001;

    private static final String SQL_INSERT_PROCESS_INSTANCE = "INSERT INTO process_instance(tenantid, id, name, processdefinitionid, description, startdate, startedby, startedbysubstitute,"
            + " enddate, stateid, statecategory, lastupdate, containerid, rootprocessinstanceid, callerid, callertype, interruptingeventid,"
            + " migration_plan, stringindex1, stringindex2, stringindex3, stringindex4, stringindex5)"
            + " VALUES(?, ?, 'name', 0, 'desc', 0, 0, 0, 0, 0, 'a', 0, 0, 0, 0, 'b', 0, 0, 'c', 'd', 'e', 'f', 'g')";

    private static final String SQL_INSERT_FLOWNODE = "INSERT INTO flownode_instance(tenantid, id, flownodedefinitionid, kind, rootcontainerid, parentcontainerid, name, displayname,"
            + " displaydescription, stateid, statename, prev_state_id, terminal, stable, actorid, assigneeid, reachedstatedate, lastupdatedate, expectedenddate, claimeddate,"
            + " priority, gatewaytype, hitbys, statecategory, logicalgroup1, logicalgroup2, logicalgroup3, logicalgroup4, loop_counter, loop_max, description, sequential,"
            + " loopdatainputref, loopdataoutputref, datainputitemref, dataoutputitemref, loopcardinality, nbactiveinst, nbcompletedinst, nbterminatedinst, executedby,"
            + " executedbysubstitute, activityinstanceid, state_executing, abortedbyboundary, triggeredbyevent, interrupting, deleted, tokencount, token_ref_id)"
            + " VALUES(?, ?, 0, ?, 0, 0, 'name', 'a', 'b', 0, 'c', 0, ?, ?, 0, 0, 0, 0, 0, 0, 0, 'd', 'e', 'f', 0, 0, 0, 0, 0, 0, 'g', ?, 'h', 'i', 'j', 'k',"
            + " 0, 0, 0, 0, 0, 0, 0, ?, 0, ?, ?, ?, 0, 0)";

    private static final String SQL_INSERT_TENANT = " INSERT INTO tenant(id, created, createdby, description, defaulttenant, iconname, iconpath, name, status)"
            + " VALUES(?, 0, 'a', 'b', ?, 'c', 'd', 'e', 'f')";

    private static final int TENANT_ID = 4567;

    private static Logger logger = LoggerFactory.getLogger(DatabaseChecker6_4_0.class);

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_4_0.class.getName());
    }

    @AfterClass
    public static void teardown() throws BonitaException {
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        // clean up
        jdbcTemplate.update("DELETE FROM multi_biz_data where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM ref_biz_data_inst where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM flownode_instance where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM process_instance where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM event_trigger_instance where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM tenant where id = ?", new Object[]{TENANT_ID});
    }

    @Test
    public void check_security_is_off() throws Exception {
        String path = BonitaHomeClient.getInstance().getBonitaHomeClientFolder() + "/platform/tenant-template/conf/security-config.properties";
        Properties properties = new Properties();
        FileInputStream inStream = new FileInputStream(path);
        properties.load(inStream);
        inStream.close();
        assertThat(properties.getProperty("security.rest.api.authorizations.check.enabled")).isEqualTo("false");
    }

    @Test
    public void kind_field_has_been_created() throws Exception {
        logger.info("check field kind is present in table ref_biz_data_inst");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        // given
        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);
        assertEquals(0, countMultiBusinessdata(jdbcTemplate));

        // when
        jdbcTemplate.update("INSERT INTO ref_biz_data_inst(tenantid, id, name, data_id, data_classname, kind) "
                + " VALUES (?, ?, ?, ?, ?, ?) ", new Object[] { TENANT_ID, 12020, "businessdata", 1, CLASSNAME, KIND });

        // then
        assertEquals(countRefBusinessdata + 1, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        emptyRefBizDataTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));
    }

    @Test
    public void ref_biz_data_inst_flownode_id_check() throws Exception {
        logger.info("check nullable fields on table ref_biz_data_inst");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        // given
        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);

        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        jdbcTemplate.update(SQL_INSERT_PROCESS_INSTANCE, new Object[]{TENANT_ID, PROCESS_INSTANCE_ID1});
        jdbcTemplate.update(SQL_INSERT_PROCESS_INSTANCE, new Object[] { TENANT_ID, PROCESS_INSTANCE_ID2 });

        // when
        final String sqlInsertRefBizData = "INSERT INTO ref_biz_data_inst(tenantid, id, name, proc_inst_id, fn_inst_id, data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlInsertRefBizData, new Object[]{TENANT_ID, 38484, "businessdata", PROCESS_INSTANCE_ID1, null, 1, CLASSNAME,
                KIND});

        jdbcTemplate.update(sqlInsertRefBizData, new Object[]{TENANT_ID, 4959595, "businessdata", PROCESS_INSTANCE_ID2, null, 1, CLASSNAME,
                KIND
        });

        // then
        assertEquals(countRefBusinessdata + 2, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        emptyProcessTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));

    }

    @Test
    public void flownode_fk_constraint_check() throws Exception {
        logger.info("check nullable fields on table ref_biz_data_inst");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        // given
        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);

        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        jdbcTemplate
                .update(SQL_INSERT_FLOWNODE
                        , new Object[]{TENANT_ID, FLOWNODE_INSTANCE_ID1, "kind", false, false, false, false, false, false, false});

        jdbcTemplate
                .update(SQL_INSERT_FLOWNODE
                        , new Object[]{TENANT_ID, FLOWNODE_INSTANCE_ID2, "kind", false, false, false, false, false, false, false});

        // when

        final String sqlInsertRefBizzData = "INSERT INTO ref_biz_data_inst(tenantid, id, name, proc_inst_id, fn_inst_id, data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlInsertRefBizzData, new Object[] { TENANT_ID, 45678, "businessdata", null, FLOWNODE_INSTANCE_ID1, 1, CLASSNAME,
                KIND });

        jdbcTemplate.update(sqlInsertRefBizzData, new Object[] { TENANT_ID, 8754, "businessdata", null, FLOWNODE_INSTANCE_ID2, 1, CLASSNAME,
                KIND });

        // then
        assertEquals(countRefBusinessdata + 2, countRefBusinessdata(jdbcTemplate, TENANT_ID));

        // cleanup
        emptyFlowNodeTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));

    }

    @Test
    public void new_table_has_been_created() throws Exception {
        logger.info("check table multi_biz_data");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);
        assertEquals(0, countMultiBusinessdata(jdbcTemplate));

        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        jdbcTemplate.update("INSERT INTO ref_biz_data_inst(tenantid, id, name,  data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?)", new Object[]{TENANT_ID, 298989, "businessdata", 1, CLASSNAME, "multi_ref"});
        logger.info("insert first multiple data");
        jdbcTemplate.update("INSERT INTO multi_biz_data(tenantid, id, idx, data_id) "
                + "VALUES (?, ?, ?, ?)", new Object[] { TENANT_ID, 298989, 1, 1 });
        logger.info("insert second multiple data");
        jdbcTemplate.update("INSERT INTO multi_biz_data(tenantid, id, idx, data_id) "
                + "VALUES (?, ?, ?, ?)", new Object[] { TENANT_ID, 298989, 2, 2 });

        // then
        assertEquals(countRefBusinessdata + 1, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        assertEquals(2, countMultiBusinessdata(jdbcTemplate));
        logger.info("check delete cascade works");
        emptyRefBizDataTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        assertEquals(0, countMultiBusinessdata(jdbcTemplate));
    }

    private void emptyRefBizDataTable(final JdbcTemplate jdbcTemplate) {
        logger.info("clean table ref_biz_data_inst");
        jdbcTemplate.update("DELETE FROM ref_biz_data_inst where tenantid = ?", new Object[] { TENANT_ID });
    }

    private void emptyProcessTable(final JdbcTemplate jdbcTemplate) {
        logger.info("clean table process_instance");
        jdbcTemplate.update("DELETE FROM process_instance where tenantid = ?", new Object[] { TENANT_ID });
    }

    private void emptyFlowNodeTable(final JdbcTemplate jdbcTemplate) {
        logger.info("clean table flownode_instance");
        jdbcTemplate.update("DELETE FROM flownode_instance where tenantid = ?", new Object[] { TENANT_ID });
    }

    private long countRefBusinessdata(final JdbcTemplate jdbcTemplate, final long tenantId) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM ref_biz_data_inst where tenantid=" + tenantId);
    }

    private long countTenant(final JdbcTemplate jdbcTemplate, final long tenantId) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM tenant where id=" + tenantId);
    }

    private long countMultiBusinessdata(final JdbcTemplate jdbcTemplate) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM multi_biz_data");
    }

    private long getCount(final JdbcTemplate jdbcTemplate, final String sql) {
        final long count = jdbcTemplate.queryForLong(sql);
        logger.info("getCount:" + sql + ":" + count);
        return count;
    }

    private void createTenantIfNotExists(final JdbcTemplate jdbcTemplate, final long tenantId) {
        if (countTenant(jdbcTemplate, tenantId) == 0) {
            jdbcTemplate.update(SQL_INSERT_TENANT, new Object[] { tenantId, false });
        }
    }

    @Test
    public void event_trigger_instance_table_has_been_updated() throws Exception {
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        jdbcTemplate.update("INSERT INTO event_trigger_instance(tenantid, id, kind, eventInstanceId,  eventInstanceName, executionDate, jobTriggerName) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)", new Object[] { TENANT_ID, 298989, "toto", 669, "eventInstanceName", new Date().getTime(), "jobTriggerName" });

        // then
        assertEquals(1, getCount(jdbcTemplate, "SELECT COUNT(id) FROM event_trigger_instance where kind='toto'"));
    }

    @Test
    public void checkDocumentsAreMigrated() throws Exception {
        SearchResult<ArchivedProcessInstance> archivedProcessInstances = processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 100).filter(ArchivedProcessInstancesSearchDescriptor.NAME, "ProcessWithDocuments").sort(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, Order.ASC).done());
        SearchResult<ProcessInstance> processInstances = processAPI.searchProcessInstances(new SearchOptionsBuilder(0, 100).filter(ProcessInstanceSearchDescriptor.NAME, "ProcessWithDocuments").sort(ProcessInstanceSearchDescriptor.ID, Order.ASC).done());
        Set<Long> ids = new HashSet<Long>();
        for (ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances.getResult()) {
            ids.add(archivedProcessInstance.getSourceObjectId());
        }
        for (ProcessInstance processInstance : processInstances.getResult()) {
            ids.add(processInstance.getId());
        }
        List<Long> sortedIds = new ArrayList<Long>(ids);
        Collections.sort(sortedIds);
        assertThat(sortedIds).hasSize(4).describedAs("Can't find all processes");

        Long inst1 = sortedIds.get(0);
        Long inst2 = sortedIds.get(1);
        Long inst3 = sortedIds.get(2);
        Long inst4 = sortedIds.get(3);

        //check on finished instance that we have 3 archived document with expected values
        SearchResult<ArchivedDocument> archivedDocumentsOfInstance1 = processAPI.searchArchivedDocuments(new SearchOptionsBuilder(0, 100).filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID, inst1).sort(ArchivedDocumentsSearchDescriptor.ARCHIVE_DATE,Order.ASC).done());
        SearchResult<Document> documentsOfInstance1 = processAPI.searchDocuments(new SearchOptionsBuilder(0, 100).filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, inst1).done());

        List<ArchivedDocument> archivedDocuments1 = archivedDocumentsOfInstance1.getResult();
        assertThat(archivedDocuments1).hasSize(3).describedAs("should have this number of archived documents for " + inst1);
        assertThat(archivedDocuments1.get(0).getName()).isEqualTo("doc1");
        assertThat(processAPI.getDocumentContent(archivedDocuments1.get(0).getContentStorageId())).isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.".getBytes());
        assertThat(archivedDocuments1.get(1).getName()).isEqualTo("doc1");
        assertThat(processAPI.getDocumentContent(archivedDocuments1.get(1).getContentStorageId())).isEqualTo("newContent1".getBytes());
        assertThat(archivedDocuments1.get(2).getName()).isEqualTo("doc2");
        assertThat(processAPI.getDocumentContent(archivedDocuments1.get(2).getContentStorageId())).isEqualTo("newContent2".getBytes());
        List<Document> documents1 = documentsOfInstance1.getResult();
        assertThat(documents1).hasSize(0);

        //check on instance with step1 executed that we have 1 archived document and 2 documents with expected values
        SearchResult<ArchivedDocument> archivedDocumentsOfInstance2 = processAPI.searchArchivedDocuments(new SearchOptionsBuilder(0,100).filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID,inst2).done());
        SearchResult<Document> documentsOfInstance2 = processAPI.searchDocuments(new SearchOptionsBuilder(0, 100).filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, inst2).sort(DocumentsSearchDescriptor.DOCUMENT_CREATIONDATE, Order.ASC).done());

        List<ArchivedDocument> archivedDocuments2 = archivedDocumentsOfInstance2.getResult();
        assertThat(archivedDocuments2).hasSize(1).describedAs("should have this number of archived documents for "+inst2);
        List<Document> documents2 = documentsOfInstance2.getResult();
        assertThat(documents2).hasSize(2);

        assertThat(archivedDocuments2.get(0).getName()).isEqualTo("doc1");
        assertThat(processAPI.getDocumentContent(archivedDocuments2.get(0).getContentStorageId())).isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.".getBytes());
        assertThat(documents2.get(0).getName()).isEqualTo("doc1");
        assertThat(processAPI.getDocumentContent(documents2.get(0).getContentStorageId())).isEqualTo("newContent1".getBytes());
        assertThat(documents2.get(1).getName()).isEqualTo("doc2");
        assertThat(processAPI.getDocumentContent(documents2.get(1).getContentStorageId())).isEqualTo("newContent2".getBytes());



        //check on instance with document added using api that we have 3 documents and 2 archived document with expected values
        SearchResult<ArchivedDocument> archivedDocumentsOfInstance3 = processAPI.searchArchivedDocuments(new SearchOptionsBuilder(0,100).filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID,inst3).sort(ArchivedDocumentsSearchDescriptor.ARCHIVE_DATE, Order.ASC).done());
        SearchResult<Document> documentsOfInstance3 = processAPI.searchDocuments(new SearchOptionsBuilder(0, 100).filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, inst3).sort(DocumentsSearchDescriptor.DOCUMENT_CREATIONDATE, Order.ASC).done());

        List<ArchivedDocument> archivedDocuments3 = archivedDocumentsOfInstance3.getResult();
        assertThat(archivedDocuments3).hasSize(2);
        List<Document> documents3 = documentsOfInstance3.getResult();
        assertThat(documents3).hasSize(3);

        assertThat(archivedDocuments3.get(0).getName()).isEqualTo("documentAttachedUsingAPI");
        assertThat(processAPI.getDocumentContent(archivedDocuments3.get(0).getContentStorageId())).isEqualTo("The content of the file attached using the api".getBytes());
        assertThat(archivedDocuments3.get(1).getName()).isEqualTo("urlDocumentAttachedUsingAPI");
        assertThat(archivedDocuments3.get(1).getDocumentURL()).isEqualTo("http://MyWebSite.com/file.txt");
        assertThat(documents3.get(0).getName()).isEqualTo("doc1");
        assertThat(processAPI.getDocumentContent(documents3.get(0).getContentStorageId())).isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.".getBytes());
        assertThat(documents3.get(1).getName()).isEqualTo("documentAttachedUsingAPI");
        assertThat(processAPI.getDocumentContent(documents3.get(1).getContentStorageId())).isEqualTo("The content of the file attached using the api2".getBytes());
        assertThat(documents3.get(2).getName()).isEqualTo("urlDocumentAttachedUsingAPI");
        assertThat(documents3.get(2).getUrl()).isEqualTo("http://MyWebSite.com/file2.txt");

        //check on just started instance that we have only one document and that we have 3 archived document after execution
        SearchResult<Document> documentsOfInstance4 = processAPI.searchDocuments(new SearchOptionsBuilder(0, 100).filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, inst4).done());

        List<Document> documents4 = documentsOfInstance4.getResult();
        assertThat(documents4).hasSize(1);

        long processWithDocuments = processAPI.getProcessDefinitionId("ProcessWithDocuments", "1.0");
        processAPI.startProcess(processWithDocuments);

        //check we can read the process-design.xml
        processAPI.getDesignProcessDefinition(processWithDocuments);

    }

    @Test
    public void checkListsWorks() throws Exception {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithListOfDoc", "1.0");
        builder.addActor("john");
        builder.addLongData("doc1Id", null);
        builder.addLongData("doc2Id", null);
        builder.addUserTask("step1", "john");
        Expression scriptExpression1 = new ExpressionBuilder()
                .createGroovyScriptExpression(
                        "updateDocs",
                        "[new org.bonitasoft.engine.bpm.document.DocumentValue(doc2Id), " +
                                "new org.bonitasoft.engine.bpm.document.DocumentValue(\"newFile\".getBytes(),\"plain/text\",\"file.txt\")," +
                                "new org.bonitasoft.engine.bpm.document.DocumentValue(doc1Id,\"updatedDocFromUrl\".getBytes(),\"plain/text\",\"file.txt\")]",
                        List.class.getName(),
                        new ExpressionBuilder().createDataExpression("doc1Id", Long.class.getName()),
                        new ExpressionBuilder().createDataExpression("doc2Id", Long.class.getName()));
        Expression scriptExpression2 = new ExpressionBuilder()
                .createGroovyScriptExpression(
                        "updateDocs",
                        "[new org.bonitasoft.engine.bpm.document.DocumentValue(\"updatedDoc\".getBytes(),\"plain/text\",\"file.txt\")]",
                        List.class.getName(),
                        new ExpressionBuilder().createDataExpression("doc2Id", Long.class.getName()));
        UserTaskDefinitionBuilder userTaskDefinitionBuilder = builder.addUserTask("updateStep", "john");
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDocumentList("invoices", scriptExpression1));
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDocumentList("emptyList", scriptExpression2));
        //        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDocumentList("unknown", scriptExpression2));
        UserTaskDefinitionBuilder verifyStepBuilder = builder.addUserTask("verifyStep", "john");
        verifyStepBuilder.addDisplayDescription(new ExpressionBuilder().createGroovyScriptExpression("getInvoicesListSize",
                "String.valueOf(invoices.size())",
                String.class.getName(),
                new ExpressionBuilder().createDocumentListExpression("invoices")));
        builder.addTransition("step1", "updateStep");
        builder.addTransition("updateStep", "verifyStep");
        DocumentListDefinitionBuilder invoices = builder.addDocumentListDefinition("invoices");
        invoices.addDescription("My invoices");
        String script = "[new org.bonitasoft.engine.bpm.document.DocumentValue(\"http://www.myrul.com/mydoc.txt\"), " +
                "new org.bonitasoft.engine.bpm.document.DocumentValue(\"hello1\".getBytes(),\"plain/text\",\"file.txt\")," +
                "new org.bonitasoft.engine.bpm.document.DocumentValue(\"hello2\".getBytes(),\"plain/text\",\"file.txt\")," +
                "new org.bonitasoft.engine.bpm.document.DocumentValue(\"hello3\".getBytes(),\"plain/text\",\"file.txt\")" +
                "]";
        invoices.addInitialValue(new ExpressionBuilder().createGroovyScriptExpression("initialDocs",
                script,
                List.class.getName()));
        builder.addDocumentListDefinition("emptyList");
        User john = identityApi.getUserByUserName("userForDocuments");
        ProcessDefinition processDefinition = processAPI.deploy(builder.done());
        processAPI.addUserToActor(processAPI.getActors(processDefinition.getId(),0,1,null).get(0).getId(),john.getId());
        processAPI.enableProcess(processDefinition.getId());
        ProcessInstance processInstance = processAPI.startProcess(processDefinition.getId());


        //we have a process with an initialized list and a non initialized list

        //check with api methods
        List<Document> invoices1 = processAPI.getDocumentList(processInstance.getId(), "invoices", 0, 100);
        assertThat(invoices1).hasSize(4);
        Document urlDocument = invoices1.get(0);
        assertThat(urlDocument.getUrl()).isEqualTo("http://www.myrul.com/mydoc.txt");
        Document fileDocument = invoices1.get(1);
        assertThat(fileDocument.hasContent()).isTrue();
        assertThat(fileDocument.getContentFileName()).isEqualTo("file.txt");
        assertThat(processAPI.getDocumentContent(fileDocument.getContentStorageId())).isEqualTo("hello1".getBytes());
        List<Document> emptyList = processAPI.getDocumentList(processInstance.getId(), "emptyList", 0, 100);
        assertThat(emptyList).isEmpty();
    }

    @Test
    public void profile_entry_should_be_renamed_from_apps_to_processes() throws Exception {
        //when
        SearchResult<ProfileEntry> appsEntries = profileAPI.searchProfileEntries(buildSearchOptions("Apps"));
        SearchResult<ProfileEntry> processEntries = profileAPI.searchProfileEntries(buildSearchOptions("Process"));

        SearchResult<Profile> profilesSearchResult = profileAPI.searchProfiles(buildSearchOptionsForProfile("Administrator"));
        Profile admin = profilesSearchResult.getResult().get(0);

        profilesSearchResult = profileAPI.searchProfiles(buildSearchOptionsForProfile("User"));
        Profile user = profilesSearchResult.getResult().get(0);

        //then
        assertThat(appsEntries.getCount()).isEqualTo(0);
        assertThat(processEntries.getCount()).isEqualTo(3);
        assertThat(admin.getDescription()).isEqualTo("The administrator can install a process, manage the organization, and handle some errors (for example, by replaying a task).");
        assertThat(user.getDescription()).isEqualTo("The user can view and perform tasks and can start a new case of a process.");
    }

    protected SearchOptions buildSearchOptionsForProfile(final String profileName) {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 100);
        builder.filter(ProfileSearchDescriptor.NAME, profileName);
        SearchOptions options = builder.done();
        return options;
    }

    protected SearchOptions buildSearchOptions(final String name) {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm(name);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        SearchOptions options = builder.done();
        return options;
    }
}
