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
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.session.APISession;

public class DatabaseFiller6_4_0 extends SimpleDatabaseFiller6_4_0 {

    public static void main(final String[] args) throws Exception {
        final DatabaseFiller6_4_0 databaseFiller = new DatabaseFiller6_4_0();
        databaseFiller.execute(1, 1, 1, 1);
    }

    @Override
    protected InputStream getProfilesXMLStream() {
        return DatabaseFiller6_4_0.class.getResourceAsStream("profiles.xml");
    }
    
    @Override
    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments)
            throws Exception {
        logger.info("Starting to fill the database");
        final Map<String, String> stats = super.fillDatabase(nbProcessesDefinitions, nbProcessInstances, nbWaitingEvents, nbDocuments);
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        final APISession session = apiTestUtil.getSession();
        stats.putAll(fillDataInstance(session, 10));
        stats.putAll(fillArchivedDataInstance(session, 10));
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        logger.info("Finished to fill the database");
        return stats;
    }

    /**
     * @param session
     * @return
     */
    private Map<String, String> fillDataInstance(final APISession session, final int nbProcessInstances) throws Exception {
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("DateDataVariableProcessToBeMigrated", "1.0.");
        builder.addActor("delivery");
        builder.addDateData("dateData", new ExpressionBuilder().createConstantDateExpression("2013-07-18T14:49:26.86+02:00"));
        builder.addDateData("nullDateData", new ExpressionBuilder().createConstantDateExpression("2013-07-18T14:49:26.86+02:00"));
        builder.addUserTask("ask for adress", "delivery");

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        final ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
        processAPI.addUserToActor("delivery", processDefinition, identityAPI.getUserByUserName("william.jobs").getId());
        processAPI.enableProcess(processDefinition.getId());
        for (int j = 0; j < nbProcessInstances; j++) {
            final ProcessInstance pi = processAPI.startProcess(processDefinition.getId());
            processAPI.updateProcessDataInstance("nullDateData", pi.getId(), null);
        }
        final Map<String, String> map = new HashMap<String, String>(2);
        map.put("Process instances", String.valueOf(nbProcessInstances));
        return map;
    }

    /**
     * @param session
     * @return
     */
    private Map<String, String> fillArchivedDataInstance(final APISession session, final int nbProcessInstances) throws Exception {
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ArchivedDateDataVariableProcessToBeMigrated", "1.0.");
        builder.addActor("delivery");
        builder.addDateData("dateData", new ExpressionBuilder().createConstantDateExpression("2013-07-18T14:49:26.86+02:00"));
        builder.addDateData("nullDateData", new ExpressionBuilder().createConstantDateExpression("2013-07-18T14:49:26.86+02:00"));
        builder.addAutomaticTask("ask for nothing");

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.setProcessDefinition(builder.done());
        final ProcessDefinition processDefinition = processAPI.deploy(archiveBuilder.done());
        processAPI.addUserToActor("delivery", processDefinition, identityAPI.getUserByUserName("william.jobs").getId());
        processAPI.enableProcess(processDefinition.getId());
        for (int j = 0; j < nbProcessInstances; j++) {
            final ProcessInstance pi = processAPI.startProcess(processDefinition.getId());
            processAPI.updateProcessDataInstance("nullDateData", pi.getId(), null);
        }
        final Map<String, String> map = new HashMap<String, String>(2);
        map.put("Archived Process instances", String.valueOf(nbProcessInstances));
        return map;
    }

}
