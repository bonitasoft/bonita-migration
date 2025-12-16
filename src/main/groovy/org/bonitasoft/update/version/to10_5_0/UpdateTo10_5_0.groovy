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
package org.bonitasoft.update.version.to10_5_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.VersionUpdate

/**
 * Migration orchestrator for Bonita 10.5.0 / 2026.1
 *
 * Main changes in this version:
 * - Ehcache 2.x to Ehcache 3.11 migration
 *   - Removes XML-based cache configuration
 *   - Removes obsolete Ehcache 2 properties
 *   - Cache configuration now fully programmatic in Java
 *
 * @author Bonita Migration Tool
 */
class UpdateTo10_5_0 extends VersionUpdate {

    @Override
    List<UpdateStep> getUpdateSteps() {
        return [new RemoveEhcache2Configuration()]
    }

    @Override
    String[] getPreUpdateWarnings(UpdateContext context) {
        return [
            "Bonita 10.5 / 2026.1 migrates from Ehcache 2.x to Ehcache 3.11",
            "The migration will:",
            "  - Remove the cache-config.xml file (no longer needed)",
            "  - Remove obsolete Ehcache 2 configuration properties",
            "  - All cache configuration is now handled programmatically in Java code",
            "",
            "If you have custom cache configurations, they will be automatically migrated to use Ehcache 3 syntax.",
            "Please refer to the Bonita documentation for details on cache customization in 10.5+."
        ]
    }
}
