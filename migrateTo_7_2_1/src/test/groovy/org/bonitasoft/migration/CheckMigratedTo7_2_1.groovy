/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.migration

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * @author Laurent Leseigneur
 */
class CheckMigratedTo7_2_1 {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().reuseExistingPlatform()


    @BeforeClass
    public static void beforeClass() {
        FillerUtils.initializeEngineSystemProperties()
    }

    @Test
    def void "should processs and task contract still work with updated sequence"(){
        def client = new APIClient()
        client.login("userForContractInput","bpm")

        def processDefinitionId = client.processAPI.getProcessDefinitionId("ProcessWithContractInput", "7.2.0")
        client.processAPI.startProcessWithInputs(processDefinitionId, [processInput: true])
        def instances
        def timeout = System.currentTimeMillis() + 3000
        while ((instances = client.processAPI.getPendingHumanTaskInstances(client.session.userId, 0, 10, ActivityInstanceCriterion.DEFAULT)).size() < 2 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
            println "wait 200"
        }
        client.processAPI.assignUserTask(instances.get(0).id, client.session.userId)
        client.processAPI.executeUserTask(instances.get(0).id, [taskInput: true])
        client.logout()
    }


}