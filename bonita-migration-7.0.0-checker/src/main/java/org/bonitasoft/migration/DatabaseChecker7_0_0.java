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

import org.bonitasoft.engine.identity.User;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class DatabaseChecker7_0_0 extends SimpleDatabaseChecker7_0_0 {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker7_0_0.class.getName());
    }

    @Override
    protected Document getProfilesXML(final SAXReader reader) throws Exception {
        return reader.read(DatabaseChecker7_0_0.class.getResource("profiles.xml"));
    }

    @Override
    // This method is because there is no need to check profile migration anymore, as they are updated at Engine restart.
    public void check_profiles() throws Exception {
        System.out.println("Profiles do not need to be migrated anymore (from 7.0.0 on). Nothing to check here.");
    }

    @Test
    public void should_deploy_a_business_data_model() throws Exception {
        final User user = getApiTestUtil().createUser("matti", "bpm");
        new BDMDataBaseChecker7_0_0().should_deploy_a_business_data_model(getApiTestUtil(), user);
        getIdentityApi().deleteUser(user.getId());
    }

    @Override
    public void commands_are_up_to_date() throws Exception {
        //no check, auto update
    }
}
