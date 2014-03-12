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

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.identity.ContactData;
import org.bonitasoft.engine.identity.ContactDataUpdater;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserUpdater;
import org.junit.Test;


/**
 * Check that the migrated database still supports emails
 * @author Aurelien Pupier
 *
 */
public class SimpleDatabaseChecker6_2_5 extends DatabaseCheckerInitiliazer {
    
    @Test
    public void doesEmailStillPresent() throws Exception {
        User user = identityApi.getUserByUserName("william.jobs");
        ContactData contactData = identityApi.getUserContactData(user.getId(), true);
        Assertions.assertThat(contactData.getEmail()).isEqualTo("william.jobs@gmail.com");
    }
    
    @Test
    public void testCanAssertLongEmail() throws Exception{
        User user = identityApi.getUserByUserName("william.jobs");
        final UserUpdater userUpdater = new UserUpdater();
		final ContactDataUpdater persoContactUpdater = userUpdater.getPersoContactUpdater();
		final String newLongMailAddress = "azertyuiopazertyuiopazertyuiopazertyuiopazertyuiopazertyuiopazertyuiop@gmail.com";
		persoContactUpdater.setEmail(newLongMailAddress);
		user = identityApi.updateUser(user.getId(), userUpdater);
		
		ContactData contactData = identityApi.getUserContactData(user.getId(), true);
		Assertions.assertThat(contactData.getEmail()).isEqualTo(newLongMailAddress);
    }
}
