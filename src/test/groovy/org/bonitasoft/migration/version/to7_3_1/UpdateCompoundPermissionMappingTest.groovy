/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_3_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.ConfigurationHelper
import spock.lang.Specification

import static org.bonitasoft.migration.version.to7_3_1.UpdateCompoundPermissionMapping.resourceName

/**
 * @author Laurent Leseigneur
 */
class UpdateCompoundPermissionMappingTest extends Specification {

    ConfigurationHelper configurationHelper = Mock()

    MigrationContext context = Mock()

    def "should update caselistingpm property"() {
        given:
        context.configurationHelper >> configurationHelper
        UpdateCompoundPermissionMapping updateCompoundPermissionMapping = new UpdateCompoundPermissionMapping()

        when:
        updateCompoundPermissionMapping.execute(context)

        then:
        1 * configurationHelper.updateKeyInAllPropertyFiles(resourceName, updateCompoundPermissionMapping.key,
                updateCompoundPermissionMapping.caseListingPmPermissions, null)

    }
}
