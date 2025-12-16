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

import java.util.regex.Pattern

/**
 * Abstract base class for migrating Ehcache 2 properties to Ehcache 3 in configuration files.
 *
 * This class provides common functionality for:
 * - Removing obsolete Ehcache 2 properties that are no longer valid in Ehcache 3
 * - Adding new Ehcache 3 properties (offHeapSizeMB=0)
 *
 * Subclasses specify which configuration files to process.
 *
 * @author Emmanuel Duchastenier
 */
abstract class AbstractRemoveEhcache2Properties extends UpdateStep {

    // Obsolete Ehcache 2 properties (no longer used in Ehcache 3)
    static final List<String> OBSOLETE_CACHE_PROPERTIES = ["inMemoryOnly", "maxElementsOnDisk", "copyOnRead", "copyOnWrite"]

    /**
     * Returns the list of configuration file names to process.
     * Subclasses must override this to specify which files to clean.
     */
    protected abstract List<String> getConfigurationFiles()

    /**
     * Returns a descriptive name for the type of files being processed (used in log messages).
     * Example: "community", "subscription cluster"
     */
    protected abstract String getFileTypeDescription()

    @Override
    def execute(UpdateContext context) {
        getConfigurationFiles().each { configFile ->
            migrateCacheProperties(context, configFile)
        }
    }

    protected void migrateCacheProperties(UpdateContext context, String configFileName) {
        def fileTypeDesc = getFileTypeDescription()
        context.logger.info("Migrating Ehcache properties in ${fileTypeDesc} file ${configFileName}...")

        try {
            // Get all configuration files with this name
            def results = context.sql.rows("""
                SELECT content_type, resource_content
                FROM configuration
                WHERE resource_name=${configFileName}
                ORDER BY content_type
                """)

            if (!results || results.isEmpty()) {
                context.logger.debug("Configuration file ${configFileName} not found - skipping")
                return
            }

            results.each { row ->
                def contentType = row.content_type as String
                String content = context.databaseHelper.getBlobContentAsString(row.resource_content)
                String newContent = content
                boolean updated = false
                int removedCount = 0

                // Step 1: Remove lines containing obsolete Ehcache 2 properties
                OBSOLETE_CACHE_PROPERTIES.each { prop ->
                    // Match lines like: bonita.*.cache.*.inMemoryOnly=true
                    // Pattern matches: optional whitespace, optional comment marker, optional whitespace, any prefix, property name, equals, value, newline
                    def pattern = Pattern.compile("^\\s*#?\\s*bonita\\.(platform|tenant)\\.cache\\.[^=]*\\.${prop}=.*\$\\R?",
                            Pattern.MULTILINE)
                    def matcher = pattern.matcher(newContent)
                    if (matcher.find()) {
                        context.logger.info("Removing obsolete Ehcache 2 property pattern '*.${prop}' from ${fileTypeDesc} file ${configFileName}")
                        newContent = matcher.replaceAll("")
                        updated = true
                        removedCount++
                    }
                }

                // Step 2: Add offHeapSizeMB=0 to all cache configurations
                def cacheNames = extractCacheNames(newContent)
                def addedCount = 0
                cacheNames.each { scope, names ->
                    names.each { cacheName ->
                        // Escape dots in cache name for regex (e.g., business.data -> business\.data)
                        def escapedCacheName = cacheName.replace('.', '\\.')
                        if (!newContent.contains("bonita.${scope}.cache.${cacheName}.offHeapSizeMB")) {
                            // Find insertion point: after the last property of this cache
                            def insertionPattern = Pattern.compile("(bonita\\.${scope}\\.cache\\.${escapedCacheName}\\.[^=]+=.*\$)", Pattern.MULTILINE)
                            def insertionMatcher = insertionPattern.matcher(newContent)
                            String lastMatch = null
                            while (insertionMatcher.find()) {
                                lastMatch = insertionMatcher.group(0)
                            }

                            if (lastMatch) {
                                def propertyLine = "bonita.${scope}.cache.${cacheName}.offHeapSizeMB=0"
                                newContent = newContent.replace(lastMatch, lastMatch + "\n" + propertyLine)
                                context.logger.info("Adding Ehcache 3 property '${propertyLine}' to ${fileTypeDesc} file ${configFileName}")
                                updated = true
                                addedCount++
                            }
                        }
                    }
                }

                if (updated) {
                    context.configurationHelper.updateConfigurationFileContentPost10_3(configFileName, contentType, newContent.bytes)
                    context.logger.info("Migrated ${fileTypeDesc} file ${configFileName}: removed ${removedCount} obsolete properties, added ${addedCount} new Ehcache 3 properties")
                }
            }
        } catch (Exception e) {
            // Log error but don't fail migration - property changes are not critical
            context.logger.warn("Could not process ${fileTypeDesc} file ${configFileName}: ${e.message}")
        }
    }

    /**
     * Extract unique cache names from configuration content.
     * Returns a map with 'platform' and 'tenant' as keys, each containing a set of cache names.
     */
    protected static Map<String, Set<String>> extractCacheNames(String content) {
        def cacheNames = [platform: [] as Set, tenant: [] as Set]

        // Pattern to match: bonita.{scope}.cache.{cacheName}.{property}=value
        // Cache names can be multi-part (e.g., "business.data")
        // We match everything after .cache. up to the last dot before =
        def pattern = Pattern.compile("bonita\\.(platform|tenant)\\.cache\\.(.+?)\\.([^.]+)=", Pattern.MULTILINE)
        def matcher = pattern.matcher(content)

        while (matcher.find()) {
            def scope = matcher.group(1)
            def cacheName = matcher.group(2)  // e.g., "default", "session", "business.data"
            cacheNames[scope].add(cacheName)
        }

        return cacheNames
    }
}
