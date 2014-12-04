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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import javax.naming.Context;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.PlatformTestUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Celine Souchet
 */
public class SimpleDatabaseChecker6_4_0 extends SimpleDatabaseChecker6_3_2 {

    private final Logger logger = LoggerFactory.getLogger(SimpleDatabaseChecker6_4_0.class);

     @Test
    public void check_profiles() throws Exception {
        final SAXReader reader = new SAXReader();
        final Document document = getProfilesXML(reader);
        final Element profiles = document.getRootElement();

        // Iterate through child elements of root with element name "profile"
        for (final Iterator<Element> rootIterator = profiles.elementIterator("profile"); rootIterator.hasNext();) {
            final Element profileElement = rootIterator.next();
            final Profile profile = checkProfile(profileElement);

            final Element profileEntriesElement = profileElement.element("profileEntries");
            if (profileEntriesElement != null) {
                for (final Iterator<Element> parentProfileEntryIterator = profileEntriesElement.elementIterator("parentProfileEntry"); parentProfileEntryIterator
                        .hasNext();) {
                    final Element parentProfileEntryElement = parentProfileEntryIterator.next();
                    final ProfileEntry profileEntry = checkProfileEntry(parentProfileEntryElement, profile.getId(), 0);

                    final Element childProfileEntriesElement = profileElement.element("childrenEntries");
                    if (childProfileEntriesElement != null) {
                        for (final Iterator<Element> childProfileEntryIterator = childProfileEntriesElement.elementIterator("profileEntry"); childProfileEntryIterator
                                .hasNext();) {
                            final Element childProfileEntryElement = childProfileEntryIterator.next();
                            checkProfileEntry(childProfileEntryElement, profile.getId(), profileEntry.getId());
                        }
                    }
                }
            }
        }
    }

    protected Document getProfilesXML(final SAXReader reader) throws Exception {
        return reader.read(SimpleDatabaseChecker6_4_0.class.getResource("profiles.xml"));
    }

    private Profile checkProfile(final Element profileElement) throws SearchException {
        final String name = profileElement.attributeValue("name");
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter("name", name);
        final List<Profile> resultProfiles = profileAPI.searchProfiles(builder.done()).getResult();
        assertEquals("Profile " + name + " not found.", 1, resultProfiles.size());

        final Profile profile = resultProfiles.get(0);
        assertEquals("The profile must be default.", Boolean.valueOf(profileElement.attributeValue("isDefault")), profile.isDefault());
        assertNotNull("The creator of the profile must be not null.", profile.getCreatedBy());
        assertNotEquals("The creator of the profile must be different of 0.", 0, profile.getCreatedBy());
        assertNotNull("The creation date of the profile must be not null.", profile.getCreationDate());
        assertNotEquals("The creation date of the profile must be different of 0.", 0, profile.getCreationDate());
        final String description = profileElement.elementText("description");
        assertEquals("The description of the profile must be equals to " + description, description, profile.getDescription());
        assertNotNull("The last update date of the profile must be not null.", profile.getLastUpdateDate());
        assertNotEquals("The last update date of the profile must be different of 0.", 0, profile.getLastUpdateDate());
        assertNotNull("The updator of the profile must be not null.", profile.getLastUpdatedBy());
        assertNotEquals("The updator of the profile must be different of 0.", 0, profile.getLastUpdatedBy());

        logger.info("The profile " + name + " is the same in database as the XML file.");

        return profile;
    }

    private ProfileEntry checkProfileEntry(final Element profileEntryElement, final long profileId, final long parentProfileEntryId) throws SearchException {
        final String name = profileEntryElement.attributeValue("name");
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort("name", Order.DESC);
        builder.filter("name", name);
        builder.filter("parentId", parentProfileEntryId);
        builder.filter("profileId", profileId);
        final List<ProfileEntry> profileEntries = profileAPI.searchProfileEntries(builder.done()).getResult();
        assertEquals("Profile entry " + name + " not found for profile " + profileId + ", and parent profile entry " + parentProfileEntryId + ".", 1,
                profileEntries.size());

        final ProfileEntry profileEntry = profileEntries.get(0);
        assertEquals("The parent id of the profile entry must be equals to " + parentProfileEntryId, parentProfileEntryId, profileEntry.getParentId());
        final Long index = Long.valueOf(profileEntryElement.elementText("index"));
        assertEquals("The index of the profile entry must be equals to " + index, index, Long.valueOf(profileEntry.getIndex()));
        final String description = profileEntryElement.elementText("description");
        assertEquals("The description of the profile entry must be equals to " + description, description, profileEntry.getDescription());
        final String type = profileEntryElement.elementText("type");
        assertEquals("The type of the profile entry must be equals to " + type, type, profileEntry.getType());
        final String page = profileEntryElement.elementText("page");
        assertEquals("The page of the profile entry must be equals to " + page, page, profileEntry.getPage());
        assertEquals("The profile id of the profile entry must be equals to " + profileId, profileId, profileEntry.getProfileId());
        assertFalse("The profile entry must be not custom.", profileEntry.isCustom());

        logger.info("The profile entry " + name + " is the same in database as the XML file.");

        return profileEntry;
    }

}
