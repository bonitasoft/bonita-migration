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

import java.util.Arrays;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.junit.runner.JUnitCore;


public class DatabaseChecker6_5_0 extends SimpleDatabaseChecker6_4_0 {

    public static APITestUtil apiTestUtil = new APITestUtil();

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_5_0.class.getName());
    }

    @Override
    protected Document getProfilesXML(final SAXReader reader) throws Exception {
        return reader.read(DatabaseChecker6_5_0.class.getResource("profiles.xml"));
    }

    @Test
    public void can_creatte_FlowNodeInstance_with_several_non_ascii_characters() throws Exception {
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        final User user = apiTestUtil.createUser("tom", "bpm");

        final String taskName = "Žingsnis, kuriame paraiškos te";
        //        final String taskName = "Žingsnis, kuriame paraiškos teikėjas gali laisvai užpildyti duomenis, ąčęė";
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList(taskName),
                Arrays.asList(true));
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(BuildTestUtil.PROCESS_NAME,
                BuildTestUtil.PROCESS_VERSION);
        processBuilder.addActor(BuildTestUtil.ACTOR_NAME);
        processBuilder.addUserTask("task1क्तु क्तु क्तु क्तु क्तु क्तु क्तु क्तु", BuildTestUtil.ACTOR_NAME)
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression(taskName))
                .addDescription("description");

        final ProcessDefinition processDef1 = apiTestUtil.deployAndEnableProcessWithActor(designProcessDef1, BuildTestUtil.ACTOR_NAME, user);
        processAPI.startProcess(processDef1.getId());
        final HumanTaskInstance task1 = apiTestUtil.waitForUserTask(taskName);
        assertEquals(taskName, task1.getDisplayName());

        apiTestUtil.disableAndDeleteProcess(processDef1);
        apiTestUtil.deleteUser(user);
    }

}
