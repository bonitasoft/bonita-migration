/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.update.core.database

import groovy.sql.Sql
import groovy.transform.Canonical
import org.bonitasoft.update.core.Logger

class HazelcastConfigurationHelper {

    @Canonical
    class EvictionPolicy {

        String policy = 'LRU'
        int size = 10000
        String maxSizePolicy = 'ENTRY_COUNT'
        String expiryPolicyType = 'TOUCHED'
        int durationAmount = 12
        String timeUnit = 'HOURS'
    }

    @Canonical
    class HazelcastConfiguration {

        String content
        String contentType = 'PLATFORM_ENGINE'
    }

    Sql sql
    Logger logger
    DatabaseHelper databaseHelper

    /**
     * Add an entity to the cache configuration in the Hazelcast configuration file, if it does not already exist.
     */
    HazelcastConfiguration addEntityToCache(String entityName, EvictionPolicy evictionPolicy = new EvictionPolicy()) {
        logger.debug(String.format("Update Hazelcast configuration file with new entity: %s", entityName))
        def hazelcastConfiguration = readHazelcastConfiguration()

        // make it reentrant:
        if (hazelcastConfiguration.content.contains("""<cache name="${entityName}">""")) {
            logger.warn("Entity ${entityName} already exists in Hazelcast configuration file. Skip adding it again.")
            return hazelcastConfiguration
        }
        def entityContent = """|    <cache name="${entityName}">
        |        <eviction eviction-policy="${evictionPolicy.policy}" size="${evictionPolicy.size}" max-size-policy="${evictionPolicy.maxSizePolicy}"/>
        |        <expiry-policy-factory>
        |            <timed-expiry-policy-factory expiry-policy-type="${evictionPolicy.expiryPolicyType}" duration-amount="${evictionPolicy.durationAmount}" time-unit="${evictionPolicy.timeUnit}"/>
        |        </expiry-policy-factory>
        |    </cache>
        |
        |</hazelcast>""".stripMargin().denormalize()

        hazelcastConfiguration.content = hazelcastConfiguration.content.replace("</hazelcast>", entityContent)

        sql.execute("UPDATE configuration SET resource_content = ${hazelcastConfiguration.content.bytes} WHERE content_type = ${hazelcastConfiguration.contentType} AND resource_name = 'hazelcast.xml'")

        return hazelcastConfiguration
    }

    HazelcastConfiguration readHazelcastConfiguration() {
        def count = sql.firstRow("SELECT count(*) FROM configuration WHERE resource_name = 'hazelcast.xml'")[0]
        if (count == 0) {
            throw new IllegalArgumentException('Hazelcast configuration file does not exist in database.')
        }
        logger.debug("Read Hazelcast configuration file")
        def result = sql.firstRow("SELECT resource_content FROM configuration WHERE resource_name = 'hazelcast.xml' ")
        return new HazelcastConfiguration(databaseHelper.getBlobContentAsString(result.resource_content))
    }


    HazelcastConfiguration updateXMLSchema(String xsdSchema) {
        logger.debug(String.format("Update Hazelcast configuration file XSD schema to: %s", xsdSchema))
        def hazelcastConfiguration = readHazelcastConfiguration()
        hazelcastConfiguration.content = hazelcastConfiguration.content.replaceAll('http://www.hazelcast.com/schema/config/hazelcast-config-.*.xsd',
                xsdSchema)
        sql.execute("UPDATE configuration SET resource_content = ${hazelcastConfiguration.content.bytes} WHERE content_type = ${hazelcastConfiguration.contentType} AND resource_name = 'hazelcast.xml'")
        return hazelcastConfiguration
    }

    /**
     * @param entityName the fully qualified name of the entity to remove from the cache configuration
     */
    HazelcastConfiguration removeCacheConfiguration(String entityName) {
        logger.debug(String.format("Remove cache configuration for entity: %s", entityName))
        def hazelcastConfiguration = readHazelcastConfiguration()
        hazelcastConfiguration.content = hazelcastConfiguration.content.replaceAll("(?s)<cache name=\"${entityName}\">.*?</cache>", "")
        sql.execute("UPDATE configuration SET resource_content = ${hazelcastConfiguration.content.bytes} WHERE content_type = ${hazelcastConfiguration.contentType} AND resource_name = 'hazelcast.xml'")
        return hazelcastConfiguration
    }
}
