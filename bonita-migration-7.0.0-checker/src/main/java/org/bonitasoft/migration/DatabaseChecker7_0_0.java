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

import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.identity.User;
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class DatabaseChecker7_0_0 extends SimpleDatabaseChecker7_0_0 {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker7_0_0.class.getName());
    }

    @Test
    public void should_deploy_a_business_data_model() throws Exception {
        final User user = getApiTestUtil().createUser("matti", "bpm");
        new BDMDataBaseChecker7_0_0().should_deploy_a_business_data_model(getApiTestUtil(), user);
        getIdentityApi().deleteUser(user.getId());
    }

    @Test
    public void should_be_able_to_create_an_application_after_migration() throws Exception {
        //when
        Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "my application", "1.0"));

        //then
        assertThat(application).isNotNull();
        assertThat(application.getLayoutId()).isNotNull();
        assertThat(application.getThemeId()).isNotNull();
    }
}
