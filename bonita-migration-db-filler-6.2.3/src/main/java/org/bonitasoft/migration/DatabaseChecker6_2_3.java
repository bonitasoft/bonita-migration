/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * accessor program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * accessor program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with accessor program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.migration;

import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.junit.Test;
import org.junit.runner.JUnitCore;

/**
 * 
 * 
 * Check that the migrated database is ok
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * 
 */
public class DatabaseChecker6_2_3 extends DatabaseChecker6_2_2 {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_2_3.class.getName());
    }

    @Override
    @Test
    public void runIt() throws Exception {
        processAPI.getNumberOfProcessInstances();

    }

    @Test
    public void check_process_with_dependencies_still_work() throws Exception {
        User user = identityApi.getUserByUserName("dependencyUser");

        long processDefinitionId = processAPI.getProcessDefinitionId("ProcessWithCustomData", "1.0");

        processAPI.startProcess(processDefinitionId);

        WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(100, 5000, 1, user.getId(), processAPI);
        if (!waitForPendingTasks.waitUntil()) {
            throw new IllegalStateException("process with custom jar don't work");
        }
    }
}
