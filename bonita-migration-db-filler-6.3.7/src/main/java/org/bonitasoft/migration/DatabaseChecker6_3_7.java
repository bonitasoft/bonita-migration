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

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.identity.ContactDataCreator;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserWithContactData;
import org.bonitasoft.engine.session.APISession;
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class DatabaseChecker6_3_7 extends SimpleDatabaseChecker6_3_2 {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_3_7.class.getName());
    }

    //BS-8991
    @Test
    public void can_create_user_with_255_char_in_fields() throws Exception {
        //given
        final String username = completeWithZeros("user");
        final UserCreator creator = new UserCreator(username, "bpm");
        creator.setJobTitle(completeWithZeros("Engineer"));
        creator.setFirstName(completeWithZeros("First"));
        creator.setLastName(completeWithZeros("Last"));

        final ContactDataCreator contactDataCreator = new ContactDataCreator();
        contactDataCreator.setAddress(completeWithZeros("32 Rue Gustave Eiffel"));
        creator.setProfessionalContactData(contactDataCreator);

        //when
        final User user = getIdentityApi().createUser(creator);
        //should be able to login using this user
        final APISession session2 = TenantAPIAccessor.getLoginAPI().login(username, "bpm");
        TenantAPIAccessor.getLoginAPI().logout(session2);

        //then
        assertThat(user).isNotNull();
        assertThat(user.getUserName()).hasSize(255);
        assertThat(user.getFirstName()).hasSize(255);
        assertThat(user.getLastName()).hasSize(255);
        assertThat(user.getJobTitle()).hasSize(255);

        //when
        final UserWithContactData userWithContactData = getIdentityApi().getUserWithProfessionalDetails(user.getId());

        //then
        assertThat(userWithContactData).isNotNull();
        assertThat(userWithContactData.getContactData().getAddress()).hasSize(255);

    }

    private String completeWithZeros(final String prefix) {

        final StringBuilder stb = new StringBuilder(prefix);
        for (int i = 0; i < 255 - prefix.length(); i++) {
            stb.append("0");
        }

        return stb.toString();
    }

    @Test
    public void should_allow_documents_with_null_content() throws Exception {
        final ProcessInstance instance = getProcessAPI().startProcess(getProcessAPI().getProcessDefinitionId(
                SimpleDatabaseFiller6_0_2.PROCESS_NAME,
                SimpleDatabaseFiller6_0_2.PROCESS_VERSION));

        final Document document = getProcessAPI().attachDocument(
                instance.getId(),
                "Document Name",
                "File Name",
                "Mime",
                (byte[]) null);

        assertThat(getProcessAPI().getDocumentContent(document.getContentStorageId())).isEqualTo(new byte[0]);

        getProcessAPI().deleteProcessInstance(instance.getId());
    }

}
