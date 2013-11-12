/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.APITestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatCollector {

    private final Logger logger = LoggerFactory.getLogger(StatCollector.class);

    public static void main(final String[] args) throws Exception {
        StatCollector statCollector = new StatCollector();
        statCollector.execute();
    }

    public void execute() throws Exception {
        APISession session = APITestUtil.loginDefaultTenant();
        ArrayList<String> stats = new ArrayList<String>();
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        stats.addAll(organizationStats(identityAPI));
        stats.addAll(processStats(processAPI));
        APITestUtil.logoutTenant(session);
        for (String string : stats) {
            logger.info(string);
        }
    }

    public static List<String> processStats(final ProcessAPI processAPI) {
        return Arrays.asList("process definitions: " + processAPI.getNumberOfProcessDeploymentInfos(),
                "process instances: " + processAPI.getNumberOfProcessInstances());
    }

    public static List<String> organizationStats(final IdentityAPI identityAPI) {
        return Arrays.asList("Users: " + identityAPI.getNumberOfUsers(), "Groups: " + identityAPI.getNumberOfGroups(),
                "Roles: " + identityAPI.getNumberOfRoles());
    }

}
