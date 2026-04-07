/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to11_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.VersionUpdate

/**
 * Migration orchestrator for Bonita 11.0.0 / 2026.1
 *
 * Main changes in this version:
 * - Ehcache 2.x to Ehcache 3.11 migration
 *   - Removes XML-based cache configuration
 *   - Removes obsolete Ehcache 2 properties
 *   - Cache configuration now fully programmatic in Java
 * - new BDM query response format property that allows to choose between modern / legacy format
 *
 * @author Emmanuel Duchastenier
 * @author Christophe Vidaillac
 */
class UpdateTo11_0_0 extends VersionUpdate {

    public static final String[] WARN_MESSAGE_EHCACHE_AND_BDM_QUERY_FORMAT =
    [
        "Bonita 11.0 / 2026.1 migrates from Ehcache 2.x to Ehcache 3.11",
        "The migration will:",
        "  - Remove the cache-config.xml file (no longer needed)",
        "  - Remove obsolete Ehcache 2 configuration properties",
        "  - All cache configuration is now handled programmatically in Java code",
        "",
        "If you have custom cache configurations, they will be automatically migrated to use Ehcache 3 syntax.",
        "Please refer to the Bonita documentation for details on cache customization in 11.0+.",
        "",
        "NOTE: A new behavior is available for BDM custom query response formats.",
        "The property bonita.runtime.business-data.serialization.standard-shape.enabled will be set to false to preserve the existing behavior.",
        "Set this property to true to enable the new standardized response format for BDM custom queries.",
        "More information is available in the documentation: https://documentation.bonitasoft.com/bonita/latest/data/bdm-query-response-formats",
    ]

    public static final String[] WARN_MESSAGE_POSTGRES_LO_CLEANUP =
    [
        "NOTE: PostgreSQL Large Objects cleanup",
        "Bonita will create a PostgreSQL trigger on table 'temporary_content' to cleanup large objects (lo_unlink) when rows are deleted.",
        "This prevents orphan large objects in the database."
    ]

    @Override
    List<UpdateStep> getUpdateSteps() {
        return [
            new RemoveEhcache2Configuration(),
            new AddBdmQueryResponseFormatConfig(),
            new AddTemporaryContentLargeObjectCleanupTriggerPostgres(),
            new CreateDataRetentionConfigTable(),
            new CreateDataRetentionBdmTrackingTable(),
        ]
    }

    @Override
    String[] getPreUpdateWarnings(UpdateContext context) {
        def warnings = WARN_MESSAGE_EHCACHE_AND_BDM_QUERY_FORMAT as List
        if (context?.dbVendor == UpdateStep.DBVendor.POSTGRES) {
            warnings.addAll(WARN_MESSAGE_POSTGRES_LO_CLEANUP)
        }
        return warnings as String[]
    }
}
