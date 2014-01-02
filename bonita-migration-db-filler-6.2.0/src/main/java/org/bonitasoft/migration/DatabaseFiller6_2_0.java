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

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 
 * Launch Database filler but with 6.1.0 dependencies
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * 
 */
public class DatabaseFiller6_2_0 extends DatabaseFiller {

    public static void main(final String[] args) throws Exception {
        DatabaseFiller6_2_0 databaseFiller = new DatabaseFiller6_2_0();
        databaseFiller.execute(1, 1, 1, 1);
    }

    @Override
    public void shutdown() throws BonitaException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final PlatformSession pSession = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        APITestUtil.stopPlatformAndTenant(platformAPI, false);
        APITestUtil.logoutPlatform(pSession);
    }

    @Override
    protected Map<String, String> fillProfiles(final APISession session) throws Exception {
        final InputStream xmlStream = getProfilesXMLStream();
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        parameters.put("xmlContent", xmlContent);
        parameters.put("importPolicy", ImportPolicy.MERGE_DUPLICATES);
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
        ProfileAPI profileAPI = TenantAPIAccessor.getProfileAPI(session);
        commandAPI.execute("importProfilesCommand", parameters);
        SearchOptions searchOptions = new SearchOptionsBuilder(0, 1).done();
        Map<String, String> map = new HashMap<String, String>(1);
        map.put("Profiles", String.valueOf(profileAPI.searchProfiles(searchOptions).getCount()));
        return map;
    }

    protected InputStream getProfilesXMLStream() {
        return getClass().getResourceAsStream("profiles.xml");
    }

    @Override
    protected Map<String, String> fillProcessWithTransitions(final APISession session) throws Exception {
        // no transition in 6.1.0
        return Collections.emptyMap();
    }

    static ConfigurableApplicationContext springContext;

    @Override
    public void setup() throws Exception {
        TestsInitializer.beforeAll();
    }

}
