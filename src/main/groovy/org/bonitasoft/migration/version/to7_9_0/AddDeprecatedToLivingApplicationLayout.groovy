/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Dumitru Corini
 */
class AddDeprecatedToLivingApplicationLayout extends MigrationStep {

    public static final DEFAULT_LAYOUT_NEW_DISPLAY_NAME = 'Default living application layout (deprecated)'
    public static final DEFAULT_LAYOUT_NEW_DESCRIPTION = 'This is the default layout V5 definition for a newly-created application. It was created using the UI designer, so you can export it and edit it with the UI designer. It contains a horizontal menu widget and an iframe widget. The menu structure  is defined in the application navigation. The application pages are displayed in the iframe. This layout is deprecated. Use the Bonita Layout instead.'
    public static final DEFAULT_LAYOUT_NAME = 'custompage_defaultlayout'

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.executeUpdate("UPDATE page SET displayName='${DEFAULT_LAYOUT_NEW_DISPLAY_NAME}', description='${DEFAULT_LAYOUT_NEW_DESCRIPTION}' WHERE name='${DEFAULT_LAYOUT_NAME}'")
    }

    @Override
    String getDescription() {
        return "Deprecate the old bonita default layout"
    }

}
