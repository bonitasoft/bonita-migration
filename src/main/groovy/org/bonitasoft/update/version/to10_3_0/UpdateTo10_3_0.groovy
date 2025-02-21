/**
 * Copyright (C) 2024-2025 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.update.version.to10_3_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.VersionUpdate
import org.bonitasoft.update.version.to9_0_0.RemoveTenantIdFromIndexes

class UpdateTo10_3_0 extends VersionUpdate {

    public static final List<String> WARN_MESSAGE_TENANT_COLUMN_REMOVAL =
    [
        "In Bonita 10.3 / 2025.1, the 'tenantId' column is removed from various tables.",
        "The Update Tool automatically handles this removal, including the re-creation of built-in indexes essential for Bonita's functionality that previously referenced the 'tenantId' column.",
        "However, custom indexes referencing the 'tenantId' column are not managed by the Update Tool. In some cases, particularly with MS SQL Server, these custom indexes may block the column's removal.",
        "If this occurs, the Update Tool will stop and display a detailed error message identifying the index causing the issue.",
        "To resolve:",
        "  - Manually remove the problematic index.",
        "  - Restart the Update Tool while keeping the original target version (restoring the original database snapshot is not required, as the Update Tool will resume from where it stopped).",
        "  - Repeat if necessary until the update completes successfully.",
        "Once the update is complete, you may choose to recreate the custom indexes without 'tenantId'."
    ]

    @Override
    List<UpdateStep> getUpdateSteps() {
        return [
            new CreateBpmFailureTables(),
            new RemoveUnusedTables(),
            new RemoveTenantIdFromIndexes(),
            new RemoveTenantIdFromProcessDefinition(),
            new RemoveTenantIdFromBusinessDataAndFlowNodeInstance(),
            new RemoveTenantIdFromPageAndFormMapping(),
            new RemoveTenantIdFromApplicationPageProfile(),
            new RemoveTenantIdFromActorAndActorMember(),
            new RemoveTenantIdFromJobTables(),
            new RemoveTenantIdFromIdentityTables(),
            new RemoveTenantIdFromProcessComment(),
            new RemoveTenantIdFromContractDataTables(),
            new RemoveTenantIdFromTriggersEventsMessages(),
            new RemoveTenantIdFromCommandsBARAndTenantResources(),
            new RemoveTenantIdFromDependencyTables(),
            new RemoveTenantIdFromDocuments(),
            new RemoveTenantIdFromQuartzGroups(),
            new RemoveTenantIdFromConfiguration(),
            new CleanupTenantReferencingTables(),
        ]
    }

    @Override
    String[] getPreUpdateWarnings(UpdateContext context) {
        WARN_MESSAGE_TENANT_COLUMN_REMOVAL
    }
}
