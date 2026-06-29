/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to11_1_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Surfaces the Task Delegation rules cache configuration in the tenant custom properties file.
 *
 * The engine ships these defaults (active) in {@code bonita-tenant-community.properties}, so the cache works without
 * any change. This step appends the same block, commented out, to {@code bonita-tenant-community-custom.properties}
 * so operators can discover and override it - mirroring how every other {@code bonita.tenant.cache.*} block already
 * appears in that custom file.
 */
class AddDelegationCacheConfig extends UpdateStep {

    public static final String COMMUNITY_CONF_FILE = "bonita-tenant-community-custom.properties"

    public static final String DELEGATION_CACHE_COMMENT = """# Delegation rules cache configuration (per-delegate active-rule set, permission hot path).
# Short, finite TTL: bursts of permission checks during a task-form display happen within seconds,
# while a stale entry (e.g. a future-dated rule that just activated) is corrected after this window."""

    /** Representative key used as the idempotency guard for the whole block. */
    public static final String DELEGATION_CACHE_GUARD_KEY = "bonita.tenant.cache.delegation.maxElementsInMemory"

    public static final String DELEGATION_CACHE_BLOCK = """$DELEGATION_CACHE_COMMENT
#bonita.tenant.cache.delegation.maxElementsInMemory=10000
#bonita.tenant.cache.delegation.eternal=false
#bonita.tenant.cache.delegation.evictionPolicy=LRU
#bonita.tenant.cache.delegation.timeToLiveSeconds=300
#bonita.tenant.cache.delegation.readIntensive=true
#bonita.tenant.cache.delegation.offHeapSizeMB=0"""

    @Override
    def execute(UpdateContext context) {
        context.configurationHelper.noTenant.appendCommentedPropertyBlockIfMissing(COMMUNITY_CONF_FILE, DELEGATION_CACHE_GUARD_KEY, DELEGATION_CACHE_BLOCK)
    }

    @Override
    String getDescription() {
        return "Add the Task Delegation rules cache configuration to " + COMMUNITY_CONF_FILE
    }
}
