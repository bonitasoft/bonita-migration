/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_13_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.VersionMigration

/**
 * @author Emmanuel Duchastenier
 */
class MigrateTo7_13_0 extends VersionMigration {

    public static final List<String> WARN_MESSAGE_JAVA_11 =
            ["Warning: Bonita versions 7.13.0 / 2021.2 and later only run on Java 11 environments.",
             "If your JRE or JDK is older than 11, you need to update your target environment before starting your migrated Bonita platform."]

    @Override
    List<MigrationStep> getMigrationSteps() {
        // keep one line per step and comma (,) at start of line to avoid false-positive merge conflict:
        return [
                new RemoveUselessV6formsConfiguration(),
                new UpdateApplicationSchema()
                , new UpdatePageSchema()
                , new CreateNewPages()
                , new MigrateProfileMenuToApplications()
                , new RemoveThemes()
        ]
    }

    @Override
    String[] getPreMigrationWarnings(MigrationContext context) {
        WARN_MESSAGE_JAVA_11
    }
}
