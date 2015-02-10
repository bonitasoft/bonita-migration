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

import java.util.Map;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.ClientEventUtil;

public class DatabaseFiller6_4_2 extends SimpleDatabaseFiller6_4_1 {

    public static void main(final String[] args) throws Exception {
        final DatabaseFiller6_4_2 databaseFiller = new DatabaseFiller6_4_2();
        databaseFiller.execute(1, 1, 1, 1);
    }

    @Override
    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments)
            throws Exception {
        logger.info("Starting to fill the database");
        final Map<String, String> stats = super.fillDatabase(nbProcessesDefinitions, nbProcessInstances, nbWaitingEvents, nbDocuments);
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        ClientEventUtil.deployCommand(apiTestUtil.getSession());
        apiTestUtil.logoutOnTenant();
        fillUserWithLoginDate();
        fillFlownodeInstanceForDeleted();
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        ClientEventUtil.undeployCommand(apiTestUtil.getSession());
        apiTestUtil.logoutOnTenant();
        logger.info("Finished to fill the database");
        return stats;
    }

    public void fillUserWithLoginDate() throws Exception {
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        final IdentityAPI identityAPI = apiTestUtil.getIdentityAPI();
        identityAPI.createUser("userWithLoginDate", "bpm");
        identityAPI.createUser("userWithoutLoginDate", "bpm");
        apiTestUtil.logoutOnTenant();
        apiTestUtil.loginOnDefaultTenantWith("userWithLoginDate", "bpm");
        apiTestUtil.logoutOnTenant();
    }

    private void fillFlownodeInstanceForDeleted() throws Exception {
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        final APISession session = apiTestUtil.getSession();
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("SimpleProcessWithDeleted", "1.0");
        builder.addActor("actor");
        final AutomaticTaskDefinitionBuilder task = builder.addAutomaticTask("auto");
        task.addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(100));
        builder.addUserTask("human", "actor");
        builder.addTransition("auto", "human");
        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        final ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
        processAPI.addUserToActor("actor", processDefinition, identityAPI.getUserByUserName("william.jobs").getId());
        processAPI.enableProcess(processDefinition.getId());

        final long instanceId = processAPI.startProcess(processDefinition.getId()).getId();
        apiTestUtil.waitForUserTask(instanceId, "human");
        apiTestUtil.logoutOnTenant();

    }

}
