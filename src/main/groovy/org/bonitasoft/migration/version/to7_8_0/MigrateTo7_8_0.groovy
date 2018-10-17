/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

package org.bonitasoft.migration.version.to7_8_0

import groovy.sql.GroovyRowResult
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.VersionMigration

/**
 * @author Anthony Birembaut
 * @author Danila Mazour
 * @author Emmanuel Duchastenier
 */
class MigrateTo7_8_0 extends VersionMigration {

    public final static String[] V6_FORMS_IN_ACTIVE_INSTANCES_OR_ENABLED_PROCESSES_PRESENT_MESSAGE =
            ["Bonita 7.8 does not support 6.x Legacy forms and overview pages based on Google Web Toolkit (GWT) anymore.",
             "In order for processes with v6 forms to be migrated, they must be disabled and all their cases must be archived.",
             "Some of the processes to migrate contain 6.x forms and are either enabled, or disabled with instances still running.",
             "Migration cannot be performed.",
             "To continue running these processes in Bonita 7.8 and more, forms must be replaced by forms using the UI Designer and the new contract mechanism,",
             "in a Studio version prior to 7.8.",
             "To know more, check the (documentation) (link: https://documentation.bonitasoft.com/bonita/7.8/migrate-a-form-from-6-x).",
             "The latest Bonita version to support 6.x forms and overview pages is Bonita 7.7.x, supported until June 2020.",
             "If you have any questions, contact the Bonita Support Team. The problematic V6 forms are:"]


    @Override
    List<MigrationStep> getMigrationSteps() {
        //keep one line per step to avoid false-positive merge conflict
        return [
                new AddHiddenFieldToPages()
                , new ChangeProfileEntryForProcessList()
                , new MigrateV6Forms()
        ]
    }

    @Override
    String[] getPreMigrationBlockingMessages(MigrationContext context) {
        List<String> blockingMessage = []
        // Check if a process with active instances has either v6 instantiation forms or task forms, ignoring overviews
        // UNION
        // Check if an enabled process who has either v6 instantiation forms or task forms
        def v6forms = context.sql.rows("""SELECT DISTINCT KEY_
FROM process_instance pi, form_mapping f, page_mapping p
WHERE pi.processdefinitionid = f.process
AND f.target = 'LEGACY'
AND f.TYPE <> 2
AND p.ID = f.page_mapping_id
AND p.tenantid = f.page_mapping_tenant_id
UNION
SELECT DISTINCT KEY_
FROM process_definition pd, form_mapping f, page_mapping p
WHERE pd.PROCESSID = f.PROCESS
AND f.TARGET = 'LEGACY'
AND f.TYPE <> 2
AND pd.ACTIVATIONSTATE = 'ENABLED'
AND p.ID = f.page_mapping_id
AND p.tenantid = f.page_mapping_tenant_id
""")
        if (v6forms.size() != 0) {
            blockingMessage.addAll(V6_FORMS_IN_ACTIVE_INSTANCES_OR_ENABLED_PROCESSES_PRESENT_MESSAGE)
            for (GroovyRowResult v6form in v6forms) {
                blockingMessage.add(normalizeNamed(v6form[0] as String))
            }
        }

        return blockingMessage
    }

    static String normalizeNamed(String name) {
        if (name.startsWith("process/")) {
            return name.replaceFirst("process/", "") + " (Instantiation form)"
        }
        if (name.startsWith("taskInstance/")) {
            return name.replaceFirst("taskInstance/", "") + " (Task form)"
        }
        return name
    }

    String[] getPreMigrationWarnings(MigrationContext context) {
        // only display warning if there are some v6 Case Overview page:
        if (context.sql.firstRow("SELECT count(*) FROM form_mapping f WHERE f.target = 'LEGACY' AND f.TYPE = 2")[0] > 0) {
            // This message is overridden by a specific SP version:
            return ["Bonita 7.8 does not support 6.x Legacy forms and overview pages based on Google Web Toolkit (GWT) technology anymore.",
                    "In order for processes with v6 forms to be migrated, they must be disabled and all their cases must be archived.",
                    "Some of the processes to migrate fall in this category. After migration, it will be impossible to enable those processes again.",
                    "Moreover, the overview page of all cases of those processes will be replaced by Bonita auto-generated overview page.",
                    "The latest Bonita version to support 6.x forms and overview pages is Bonita 7.7.x. It will be supported until June 2020.",
                    "If you have any questions, contact the Bonita Support Team.",
                    "To know more, check the (documentation) (link: https://documentation.bonitasoft.com/bonita/7.8/migrate-a-form-from-6-x)."
            ]
        }
        return []
    }

}
