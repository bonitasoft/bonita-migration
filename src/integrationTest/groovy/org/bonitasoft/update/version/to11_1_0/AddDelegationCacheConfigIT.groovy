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

import groovy.sql.GroovyRowResult
import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddDelegationCacheConfigIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddDelegationCacheConfig updateStep = new AddDelegationCacheConfig()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("11_1_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    private void insertTenantConfigFile() {
        byte[] confContent = this.getClass().getResourceAsStream("/conf/bonita-tenant-community-custom.properties").text.bytes
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "TENANT_TEMPLATE_ENGINE", AddDelegationCacheConfig.COMMUNITY_CONF_FILE, confContent)
    }

    private String readConfFile() {
        List<GroovyRowResult> rows = updateContext.sql.rows("SELECT RESOURCE_CONTENT FROM configuration WHERE RESOURCE_NAME = '${AddDelegationCacheConfig.COMMUNITY_CONF_FILE}'")
        assert rows.size() == 1
        return updateContext.databaseHelper.getBlobContentAsString(rows.get(0).getProperty("resource_content"))
    }

    def "should add the delegation cache configuration block to the tenant custom configuration file"() {
        given:
        insertTenantConfigFile()

        when:
        updateStep.execute(updateContext)

        then:
        def confString = readConfFile()
        confString.contains(AddDelegationCacheConfig.DELEGATION_CACHE_COMMENT)
        confString.contains("#bonita.tenant.cache.delegation.maxElementsInMemory=10000")
        confString.contains("#bonita.tenant.cache.delegation.eternal=false")
        confString.contains("#bonita.tenant.cache.delegation.evictionPolicy=LRU")
        confString.contains("#bonita.tenant.cache.delegation.timeToLiveSeconds=300")
        confString.contains("#bonita.tenant.cache.delegation.readIntensive=true")
        confString.contains("#bonita.tenant.cache.delegation.offHeapSizeMB=0")
    }

    def "should be idempotent and not add the block twice"() {
        given:
        insertTenantConfigFile()

        when:
        updateStep.execute(updateContext)
        updateStep.execute(updateContext)

        then:
        def confString = readConfFile()
        confString.count(AddDelegationCacheConfig.DELEGATION_CACHE_GUARD_KEY) == 1
    }

    def "should fail when the configuration file is missing"() {
        when:
        updateStep.execute(updateContext)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains(AddDelegationCacheConfig.COMMUNITY_CONF_FILE)
    }
}
