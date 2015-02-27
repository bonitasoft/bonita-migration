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

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class DatabaseChecker6_5_0 extends SimpleDatabaseChecker6_5_0 {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_5_0.class.getName());
    }

    @Override
    protected Document getProfilesXML(final SAXReader reader) throws Exception {
        return reader.read(DatabaseChecker6_5_0.class.getResource("profiles.xml"));
    }

    @Test
    public void can_create_FlowNodeInstance_with_several_non_ascii_characters() throws Exception {
        final User user = getApiTestUtil().createUser("tom", "bpm");

        final String taskDisplayName = "Žingsnis, kuriame paraiškos teikėjas gali laisvai užpildyti duomenis, ąčęė";
        final String taskName = "task1क्तु क्तु क्तु क्तु क्तु paraiškos teikėjas Ž";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(BuildTestUtil.PROCESS_NAME,
                BuildTestUtil.PROCESS_VERSION);
        processBuilder.addActor(BuildTestUtil.ACTOR_NAME);
        processBuilder.addUserTask(taskName, BuildTestUtil.ACTOR_NAME)
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression(taskDisplayName))
                .addDescription("description");

        final ProcessDefinition processDef1 = getApiTestUtil().deployAndEnableProcessWithActor(processBuilder.done(), BuildTestUtil.ACTOR_NAME, user);
        getProcessAPI().startProcess(processDef1.getId());
        final HumanTaskInstance task1 = getApiTestUtil().waitForUserTaskAndGetIt(taskName);
        assertEquals(taskDisplayName, task1.getDisplayName());

        getApiTestUtil().disableAndDeleteProcess(processDef1);
        getApiTestUtil().deleteUser(user);
    }

    @Test
    public void user_should_keep_there_last_connection_date() throws UserNotFoundException {
        IdentityAPI identityAPI = getApiTestUtil().getIdentityAPI();
        User userWithLoginDate = identityAPI.getUserByUserName("userWithLoginDate");
        User userWithoutLoginDate = identityAPI.getUserByUserName("userWithoutLoginDate");
        assertThat(userWithLoginDate.getLastConnection()).isBefore(new Date());
        assertThat(userWithoutLoginDate.getLastConnection()).isNull();

    }

    @Test
    public void should_deleted_exists_anymore() throws Exception {
        final ProcessAPI processAPI = getApiTestUtil().getProcessAPI();
        final long processId = processAPI.getProcessDefinitionId("SimpleProcessWithDeleted", "1.0");
        final SearchResult<FlowNodeInstance> flowNodeInstanceSearchResult = processAPI.searchFlowNodeInstances(new SearchOptionsBuilder(0, 10).filter(FlowNodeInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processId).done());
        assertThat(flowNodeInstanceSearchResult.getCount()).isEqualTo(1);
        assertThat(flowNodeInstanceSearchResult.getResult().get(0).getName()).isEqualTo("human");
    }



}
