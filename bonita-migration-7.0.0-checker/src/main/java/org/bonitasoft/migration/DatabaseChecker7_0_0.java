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
 */
package org.bonitasoft.migration;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingSearchDescriptor;
import org.bonitasoft.engine.identity.User;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Ignore;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
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

	@Test
    public void verify_migration_of_legacy_form_add_form_mapping() throws Exception {
        getApiTestUtil().loginOnDefaultTenantWithDefaultTechnicalUser();
        long processWithForms = getApiTestUtil().getProcessAPI().getProcessDefinitionId("ProcessWithLegacyForms", "5.0");

        final SearchResult<FormMapping> formMappingSearchResult = getApiTestUtil().getProcessConfigurationAPI().searchFormMappings(
                new SearchOptionsBuilder(0, 10).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, processWithForms).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(3);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processWithForms);
        Thread.sleep(2000);
        final ActivityInstance activityInstance = getProcessAPI().getOpenActivityInstances(processInstance.getId(), 0, 1, ActivityInstanceCriterion.DEFAULT)
                .get(0);

        final String url = getProcessConfigurationAPI().resolvePageOrURL("processInstance/ProcessWithLegacyForms/5.0",
                getStringSerializableMap(processInstance.getId()), true).getUrl();
        final String url1 = getProcessConfigurationAPI().resolvePageOrURL("process/ProcessWithLegacyForms/5.0", getStringSerializableMap(processWithForms),
                true).getUrl();
        final String url2 = getProcessConfigurationAPI().resolvePageOrURL("taskInstance/ProcessWithLegacyForms/5.0/myUserTask",
                getStringSerializableMap(activityInstance.getId()), true).getUrl();
        assertThat(url).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=" + processWithForms + "#mode=form&form=ProcessWithLegacyForms--5.0%24recap&instance="
                        + processInstance.getId() + "&recap=true");
        assertThat(url1).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=" + processWithForms + "#mode=form&form=ProcessWithLegacyForms--5.0%24entry&process="
                        + processWithForms);
        assertThat(url2).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=" + processWithForms + "#mode=form&form=ProcessWithLegacyForms--5.0--myUserTask%24entry&task="
                        + activityInstance.getId());
    }

    Map<String, Serializable> getStringSerializableMap(long id) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("queryParameters", (Serializable) Collections.<String, Serializable> singletonMap("id", new String[] { String.valueOf(id) }));
        map.put("contextPath", "/bonita");
        map.put("locale", "en");
        return map;
    }

}
