/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration.version.to7_1_0

import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.VersionMigration

/**
 * @author Baptiste Mesta
 */
class MigrateTo7_1_0 extends VersionMigration {
    @Override
    def List<MigrationStep> getMigrationSteps() {
        //keep one line per step to avoid false-positive merge conflict
        return [
                new MigratePlatform()
                , new MigrateFormMapping()
                , new MigrateQuartzIndexes()
        ]
    }
}
