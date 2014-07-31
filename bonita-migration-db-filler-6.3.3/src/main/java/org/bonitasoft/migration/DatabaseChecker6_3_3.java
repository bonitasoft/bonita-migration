/**
 * Copyright (C) 214 BonitaSoft S.A.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.junit.runner.JUnitCore;

public class DatabaseChecker6_3_3 extends DatabaseChecker6_3_2 {

    private static APITestUtil apiTestUtil;

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_3_3.class.getName());

        apiTestUtil = new APITestUtil();
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
    }

    public void check_migration_of_corrupted_gateways() throws Exception {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
        builder.filter(ProcessInstanceSearchDescriptor.NAME, "Process With Corrupted Gateways");
        final List<ProcessInstance> processInstances = apiTestUtil.getProcessAPI().searchProcessInstances(builder.done()).getResult();
        assertFalse(processInstances.isEmpty());

        apiTestUtil.waitForFlowNodeInState(processInstances.get(0), "OutGateway", "completed", true);
        builder = new SearchOptionsBuilder(0, 1);
        builder.filter(FlowNodeInstanceSearchDescriptor.NAME, "OutGateway");
        builder.and().filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getFailedState());
        final List<ArchivedFlowNodeInstance> archivedFailedGateways = apiTestUtil.getProcessAPI().searchArchivedFlowNodeInstances(builder.done()).getResult();
        assertTrue(archivedFailedGateways.isEmpty());
    }
}
