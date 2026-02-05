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

import java.util.regex.Pattern

/**
 * Migration step to remove Ehcache 2 configuration files and obsolete properties from community configuration.
 *
 * As part of the Ehcache 2 to Ehcache 3 migration:
 * - Removes cache-config.xml file (replaced with programmatic Java configuration)
 * - Removes ALL configuration for bonita.platform.cache.platform (obsolete cache)
 * - Removes obsolete Ehcache 2 cache configuration properties from community property files
 *
 * Note: Subscription-specific cluster configuration files are handled by RemoveEhcache2SessionCacheConfiguration
 *
 * @author Bonita Migration Tool
 */
class RemoveEhcache2Configuration extends AbstractRemoveEhcache2Properties {

    static final String CACHE_CONFIG_XML = "cache-config.xml"

    // Community configuration files that might contain Ehcache 2 properties
    private static final List<String> COMMUNITY_CONFIG_FILES = ["bonita-platform-community-custom.properties", "bonita-tenant-community-custom.properties"]

    @Override
    protected List<String> getConfigurationFiles() {
        return COMMUNITY_CONFIG_FILES
    }

    @Override
    protected String getFileTypeDescription() {
        return "community"
    }

    @Override
    protected void migrateCacheProperties(UpdateContext context, String configFileName) {
        // First, remove all bonita.platform.cache.platform properties (obsolete cache - Community only)
        removePlatformCacheProperties(context, configFileName)

        // Then, call parent method to handle common migration logic
        super.migrateCacheProperties(context, configFileName)
    }

    /**
     * Removes ALL properties for bonita.platform.cache.platform (obsolete cache).
     * This cache is obsolete in Community edition and all its configuration must be removed.
     */
    private void removePlatformCacheProperties(UpdateContext context, String configFileName) {
        def fileTypeDesc = getFileTypeDescription()

        try {
            def results = context.sql.rows("""
                SELECT content_type, resource_content
                FROM configuration
                WHERE resource_name=${configFileName}
                ORDER BY content_type
                """)

            if (!results || results.isEmpty()) {
                return
            }

            results.each { row ->
                def contentType = row.content_type as String
                String content = context.databaseHelper.getBlobContentAsString(row.resource_content)

                def platformCachePattern = Pattern.compile("^\\s*#?\\s*bonita\\.platform\\.cache\\.platform\\.[^=]+=.*\$\\R?", Pattern.MULTILINE)
                def platformMatcher = platformCachePattern.matcher(content)

                if (platformMatcher.find()) {
                    String newContent = platformMatcher.replaceAll("")
                    context.configurationHelper.noTenant.updateConfigurationFileContent(configFileName, contentType, newContent.bytes)
                    context.logger.info("Removed all `bonita.platform.cache.platform` properties from ${fileTypeDesc} file ${configFileName}")
                }
            }
        } catch (Exception e) {
            context.logger.warn("Could not remove platform cache properties from ${fileTypeDesc} file ${configFileName}: ${e.message}")
        }
    }

    @Override
    def execute(UpdateContext context) {
        // Step 1: Delete cache-config.xml file from configuration table
        context.logger.info("Removing Ehcache 2 cache-config.xml file...")
        context.configurationHelper.deleteConfigurationFileOnAnyTenantAndOfAnyType(CACHE_CONFIG_XML)
        context.logger.info("cache-config.xml file removed successfully")

        // Step 2: Remove obsolete Ehcache 2 properties from community configuration files
        super.execute(context)
    }

    @Override
    String getDescription() {
        return "Remove Ehcache 2 configuration from community files (cache-config.xml, bonita.platform.cache.platform, and obsolete properties)"
    }
}
