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
import org.bonitasoft.migration.core.VersionMigration

/**
 * @author Danila Mazour
 */
class MigrateTo7_9_0 extends VersionMigration {

    public final static String[] CONNECTOR_MIGRATION_MESSAGE =
            ["The Bonita platform, from 7.9.0 upwards, supports running on java 11.",
             "In order to make the process of migrating a Bonita Platform to Java 11 as smooth as possible, the 7.9.0 migration step will try to migrate the",
             "CMIS, Webservice & Email",
             "connectors of all the deployed processes, along with their dependencies, to a version that supports java 11.",
             "The migration tool will try to do the connector migration on a best effort basis. ",
             "If the dependencies of these connectors are used in other connectors, the corresponding connectors will not be migrated.",
             "It means that, potentially, some of those connectors will not be migrated and will have to be updated manually after the migration tool completes",
             "If some Groovy scripts use the same dependencies as the old connectors' versions, they might be broken after migration, requiring a manual update",
             "On a separated note, the SAP connector will not work anymore on Java 11. If you are using it, and plan to migrate to Java 11, it might be best to postpone your migration, and contact Bonita support",
             "To know more, check the documentation (http://www.bonitasoft.com/bos_redirect.php?bos_redirect_id=686&bos_redirect_product=bos&bos_redirect_major_version=7.9)."]

    @Override
    String[] getPreMigrationWarnings(MigrationContext context) {
        return CONNECTOR_MIGRATION_MESSAGE
    }

    @Override
    List<MigrationStep> getMigrationSteps() {
        // keep one line per step to avoid false-positive merge conflict:
        return [
                new RemoveCleanInvalidSessionsJob()
                , new ChangeProfileEntryForOrganizationImport()
                , new UpdateConnectorDefinitionsForJava11()
                , new AddMessageAndWaitingEventDBIndices()
                , new RenameCommandSystemColumn()
                , new RenameBonitaDefaultTheme()
                , new AddDeprecatedToLivingApplicationLayout()
                , new AddCreationDateOnMessageInstance()
                , new AddIndexLogicalGroupOnFlownodeInstance()
        ]
    }

}
