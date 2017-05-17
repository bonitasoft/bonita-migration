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
package org.bonitasoft.migration.version.to7_5_0

import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.VersionMigration
import org.bonitasoft.migration.version.to7_3_1.FixProcessPermissionRuleScript

/**
 * @author Emmanuel Duchastenier
 */
class MigrateTo7_5_0 extends VersionMigration {

    String version = "7.5.0"

    public static final String WARN_MESSAGE_JAVA_8 = """Warning: Bonita BPM versions 7.5.0 and later only run on Java 1.8 environments.
If your JRE or JDK is older than 1.8, you need to update your target environment before starting your migrated Bonita BPM platform."""

    @Override
    List<MigrationStep> getMigrationSteps() {
        //keep one line per step to avoid false-positive merge conflict
        return [
                new SplitRestSecurityConfig(),
                new FixProcessPermissionRuleScript()
        ]
    }

    @Override
    String[] getPreMigrationWarnings() {
        [WARN_MESSAGE_JAVA_8]
    }
}
