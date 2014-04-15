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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.APITestUtil;

public class DatabaseFiller6_2_6 extends SimpleDatabaseFiller6_0_2 {

    public static void main(final String[] args) throws Exception {
        DatabaseFiller6_2_6 databaseFiller = new DatabaseFiller6_2_6();
        databaseFiller.execute(1, 1, 1, 1);
    }

    @Override
    public Map<String, String> fillDatabase(final int nbProcessesDefinitions, final int nbProcessInstances, final int nbWaitingEvents, final int nbDocuments)
            throws BonitaException,
            Exception {
        logger.info("Starting to fill the database");
        APISession session = APITestUtil.loginDefaultTenant();
        Map<String, String> stats = new HashMap<String, String>();
        stats.putAll(fillOrganization(session));
        stats.putAll(fillProcesses(session, nbProcessesDefinitions, nbProcessInstances));
        stats.putAll(fillProcessesWithEvents(session, nbWaitingEvents));
        stats.putAll(fillCompletedProcess(session));

        // 6.2.3 specific
        stats.putAll(fillProsessesWithMessageAndTimer(session));
        stats.putAll(fillExtensions(session));

        APITestUtil.logoutTenant(session);
        logger.info("Finished to fill the database");
        return stats;
    }

    /**
     * @param session
     * @return
     * @throws Exception
     */
    protected Map<? extends String, ? extends String> fillExtensions(final APISession session) throws Exception {
        return Collections.emptyMap();
    }

    @Override
    protected InputStream getProfilesXMLStream() {
        return getClass().getResourceAsStream("profiles.xml");
    }
}
