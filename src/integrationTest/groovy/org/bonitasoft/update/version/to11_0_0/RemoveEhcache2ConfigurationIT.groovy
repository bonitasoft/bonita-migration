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

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveEhcache2ConfigurationIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveEhcache2Configuration updateStep = new RemoveEhcache2Configuration()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("11_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    def "should remove cache-config.xml file"() {
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_PORTAL", "cache-config.xml", "<ehcache></ehcache>".bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def rows = updateContext.sql.rows("SELECT * FROM configuration WHERE resource_name = 'cache-config.xml'")
        rows.size() == 0
    }

    def "should remove obsolete Ehcache 2 properties and add offHeapSizeMB to platform properties file"() {
        def propFileContent = """# Cache configuration
bonita.platform.cache.default.maxElementsInMemory=10000
bonita.platform.cache.default.inMemoryOnly=true
bonita.platform.cache.default.eternal=false
bonita.platform.cache.default.evictionPolicy=LRU
bonita.platform.cache.default.timeToLiveSeconds=3600
bonita.platform.cache.default.maxElementsOnDisk=50000
bonita.platform.cache.default.copyOnRead=true
bonita.platform.cache.default.copyOnWrite=false
bonita.platform.cache.default.readIntensive=false
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", "bonita-platform-community-custom.properties", propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """# Cache configuration
bonita.platform.cache.default.maxElementsInMemory=10000
bonita.platform.cache.default.eternal=false
bonita.platform.cache.default.evictionPolicy=LRU
bonita.platform.cache.default.timeToLiveSeconds=3600
bonita.platform.cache.default.readIntensive=false
bonita.platform.cache.default.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT content_type, resource_name, resource_content FROM configuration WHERE resource_name = 'bonita-platform-community-custom.properties'")
        updatedRows.size() == 1
        updatedRows[0].content_type == "PLATFORM_ENGINE"
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPropFileContent
    }

    def "should remove obsolete Ehcache 2 properties and add offHeapSizeMB to tenant properties file"() {
        def propFileContent = """# Tenant cache configuration
bonita.tenant.cache.connector.maxElementsInMemory=5000
bonita.tenant.cache.connector.inMemoryOnly=true
bonita.tenant.cache.connector.eternal=false
bonita.tenant.cache.connector.evictionPolicy=LRU
bonita.tenant.cache.connector.timeToLiveSeconds=3600
bonita.tenant.cache.connector.maxElementsOnDisk=20000
bonita.tenant.cache.connector.copyOnRead=false
bonita.tenant.cache.connector.copyOnWrite=false
bonita.tenant.cache.connector.readIntensive=true
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "TENANT_ENGINE", "bonita-tenant-community-custom.properties", propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """# Tenant cache configuration
bonita.tenant.cache.connector.maxElementsInMemory=5000
bonita.tenant.cache.connector.eternal=false
bonita.tenant.cache.connector.evictionPolicy=LRU
bonita.tenant.cache.connector.timeToLiveSeconds=3600
bonita.tenant.cache.connector.readIntensive=true
bonita.tenant.cache.connector.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT content_type, resource_name, resource_content FROM configuration WHERE resource_name = 'bonita-tenant-community-custom.properties'")
        updatedRows.size() == 1
        updatedRows[0].content_type == "TENANT_ENGINE"
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPropFileContent
    }

    def "should add offHeapSizeMB even to files without obsolete properties"() {
        def propFileContent = """# Cache config without obsolete properties
bonita.platform.cache.default.maxElementsInMemory=10000
bonita.platform.cache.default.eternal=false
bonita.platform.cache.default.timeToLiveSeconds=3600
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", "bonita-platform-community-custom.properties", propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """# Cache config without obsolete properties
bonita.platform.cache.default.maxElementsInMemory=10000
bonita.platform.cache.default.eternal=false
bonita.platform.cache.default.timeToLiveSeconds=3600
bonita.platform.cache.default.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT resource_content FROM configuration WHERE resource_name = 'bonita-platform-community-custom.properties'")
        updatedRows.size() == 1
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPropFileContent
    }

    def "should handle commented obsolete properties and add offHeapSizeMB"() {
        def propFileContent = """# Cache configuration
bonita.platform.cache.default.maxElementsInMemory=10000
#bonita.platform.cache.default.inMemoryOnly=true
# bonita.platform.cache.default.copyOnRead=false
bonita.platform.cache.default.eternal=false
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", "bonita-platform-community-custom.properties", propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """# Cache configuration
bonita.platform.cache.default.maxElementsInMemory=10000
bonita.platform.cache.default.eternal=false
bonita.platform.cache.default.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT resource_content FROM configuration WHERE resource_name = 'bonita-platform-community-custom.properties'")
        updatedRows.size() == 1
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPropFileContent
    }

    def "should not duplicate offHeapSizeMB if it already exists"() {
        def propFileContent = """# Cache configuration with existing offHeapSizeMB
bonita.platform.cache.default.maxElementsInMemory=10000
bonita.platform.cache.default.offHeapSizeMB=100
bonita.platform.cache.default.inMemoryOnly=true
bonita.platform.cache.default.eternal=false
bonita.platform.cache.session.maxElementsInMemory=5000
bonita.platform.cache.session.offHeapSizeMB=0
bonita.platform.cache.session.copyOnRead=true
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", "bonita-platform-community-custom.properties", propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """# Cache configuration with existing offHeapSizeMB
bonita.platform.cache.default.maxElementsInMemory=10000
bonita.platform.cache.default.offHeapSizeMB=100
bonita.platform.cache.default.eternal=false
bonita.platform.cache.session.maxElementsInMemory=5000
bonita.platform.cache.session.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT resource_content FROM configuration WHERE resource_name = 'bonita-platform-community-custom.properties'")
        updatedRows.size() == 1
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPropFileContent
    }

    def "should handle all platform and tenant caches"() {
        def platformFileContent = """# Platform cache configuration
bonita.platform.cache.default.maxElementsInMemory=1000
bonita.platform.cache.default.inMemoryOnly=true
bonita.platform.cache.synchro.maxElementsInMemory=2000
bonita.platform.cache.synchro.copyOnRead=true
bonita.platform.cache.configfiles.maxElementsInMemory=3000
bonita.platform.cache.configfiles.maxElementsOnDisk=5000
"""
        def tenantFileContent = """# Tenant cache configuration
bonita.tenant.cache.connector.maxElementsInMemory=100
bonita.tenant.cache.connector.inMemoryOnly=true
bonita.tenant.cache.parameter.maxElementsInMemory=200
bonita.tenant.cache.parameter.copyOnWrite=true
bonita.tenant.cache.processdef.maxElementsInMemory=300
bonita.tenant.cache.processdef.maxElementsOnDisk=1000
bonita.tenant.cache.userfilter.maxElementsInMemory=400
bonita.tenant.cache.userfilter.copyOnRead=true
bonita.tenant.cache.groovy.maxElementsInMemory=500
bonita.tenant.cache.transientdata.maxElementsInMemory=600
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", "bonita-platform-community-custom.properties", platformFileContent.bytes)
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "TENANT_ENGINE", "bonita-tenant-community-custom.properties", tenantFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPlatformFileContent = """# Platform cache configuration
bonita.platform.cache.default.maxElementsInMemory=1000
bonita.platform.cache.default.offHeapSizeMB=0
bonita.platform.cache.synchro.maxElementsInMemory=2000
bonita.platform.cache.synchro.offHeapSizeMB=0
bonita.platform.cache.configfiles.maxElementsInMemory=3000
bonita.platform.cache.configfiles.offHeapSizeMB=0
"""
        def expectedTenantFileContent = """# Tenant cache configuration
bonita.tenant.cache.connector.maxElementsInMemory=100
bonita.tenant.cache.connector.offHeapSizeMB=0
bonita.tenant.cache.parameter.maxElementsInMemory=200
bonita.tenant.cache.parameter.offHeapSizeMB=0
bonita.tenant.cache.processdef.maxElementsInMemory=300
bonita.tenant.cache.processdef.offHeapSizeMB=0
bonita.tenant.cache.userfilter.maxElementsInMemory=400
bonita.tenant.cache.userfilter.offHeapSizeMB=0
bonita.tenant.cache.groovy.maxElementsInMemory=500
bonita.tenant.cache.groovy.offHeapSizeMB=0
bonita.tenant.cache.transientdata.maxElementsInMemory=600
bonita.tenant.cache.transientdata.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT content_type, resource_name, resource_content FROM configuration ORDER BY resource_name")
        updatedRows.size() == 2

        // Platform file
        updatedRows[0].content_type == "PLATFORM_ENGINE"
        updatedRows[0].resource_name == "bonita-platform-community-custom.properties"
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPlatformFileContent

        // Tenant file
        updatedRows[1].content_type == "TENANT_ENGINE"
        updatedRows[1].resource_name == "bonita-tenant-community-custom.properties"
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[1].resource_content) == expectedTenantFileContent
    }

    def "should remove all bonita.platform.cache.platform properties as obsolete cache"() {
        def propFileContent = """# Platform cache configuration with obsolete platform cache
bonita.platform.cache.default.maxElementsInMemory=1000
bonita.platform.cache.default.eternal=false
bonita.platform.cache.platform.maxElementsInMemory=5000
bonita.platform.cache.platform.inMemoryOnly=true
bonita.platform.cache.platform.eternal=true
bonita.platform.cache.platform.evictionPolicy=LRU
bonita.platform.cache.platform.timeToLiveSeconds=7200
bonita.platform.cache.platform.copyOnRead=false
bonita.platform.cache.synchro.maxElementsInMemory=2000
bonita.platform.cache.synchro.eternal=false
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", "bonita-platform-community-custom.properties", propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """# Platform cache configuration with obsolete platform cache
bonita.platform.cache.default.maxElementsInMemory=1000
bonita.platform.cache.default.eternal=false
bonita.platform.cache.default.offHeapSizeMB=0
bonita.platform.cache.synchro.maxElementsInMemory=2000
bonita.platform.cache.synchro.eternal=false
bonita.platform.cache.synchro.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT resource_content FROM configuration WHERE resource_name = 'bonita-platform-community-custom.properties'")
        updatedRows.size() == 1
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPropFileContent
    }

    def "should remove commented bonita.platform.cache.platform properties"() {
        def propFileContent = """# Platform cache configuration
bonita.platform.cache.default.maxElementsInMemory=1000
# bonita.platform.cache.platform.maxElementsInMemory=5000
#bonita.platform.cache.platform.eternal=true
  # bonita.platform.cache.platform.evictionPolicy=LRU
bonita.platform.cache.synchro.maxElementsInMemory=2000
"""
        given:
        updateContext.sql.executeInsert("insert into configuration(content_type, resource_name, resource_content) values (?,?,?)",
                "PLATFORM_ENGINE", "bonita-platform-community-custom.properties", propFileContent.bytes)

        when:
        updateStep.execute(updateContext)

        then:
        def expectedPropFileContent = """# Platform cache configuration
bonita.platform.cache.default.maxElementsInMemory=1000
bonita.platform.cache.default.offHeapSizeMB=0
bonita.platform.cache.synchro.maxElementsInMemory=2000
bonita.platform.cache.synchro.offHeapSizeMB=0
"""
        List updatedRows = updateContext.sql.rows("SELECT resource_content FROM configuration WHERE resource_name = 'bonita-platform-community-custom.properties'")
        updatedRows.size() == 1
        updateContext.databaseHelper.getBlobContentAsString(updatedRows[0].resource_content) == expectedPropFileContent
    }
}
