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

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.Test;


/**
 * Check that the migrated database is ok
 * @author Elias Ricken de Medeiros
 *
 */
public class SimpleDatabaseChecker6_3_1 extends DatabaseCheckerInitiliazer6_3_1 {
    
    @Test
    public void can_complete_the_execution_of_previous_started_process_and_start_a_new_one() throws Exception {
        //given
        User user = identityApi.getUserByUserName("william.jobs");
        long processDefinitionId = processAPI.getProcessDefinitionId(SimpleDatabaseFiller6_0_2.PROCESS_NAME, SimpleDatabaseFiller6_0_2.PROCESS_VERSION);
        processAPI.startProcess(processDefinitionId);

        //when
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId);
        SearchResult<ProcessInstance> searchResult = processAPI.searchProcessInstances(builder.done());
        
        //then (there are two instance, one created before migration and one created after migration)
        assertThat(searchResult.getCount()).isEqualTo(2);
        
        //when
        for (ProcessInstance processInstance : searchResult.getResult()) {
            HumanTaskInstance taskInstance = waitForUserTask(SimpleDatabaseFiller6_0_2.USER_TASK_NAME, processInstance.getId(), APITestUtil.DEFAULT_TIMEOUT);
            processAPI.assignUserTask(taskInstance.getId(), user.getId());
            processAPI.executeFlowNode(taskInstance.getId());
        }
        
        //then
        for (ProcessInstance processInstance : searchResult.getResult()) {
            waitForProcessToFinish(processInstance.getId(), APITestUtil.DEFAULT_TIMEOUT);
        }
    }
    
}
