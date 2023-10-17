/**
 * Copyright (C) 2017-2021 Bonitasoft S.A.
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
package org.bonitasoft.update.core.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.bonitasoft.update.core.Logger

/**
 * @author Laurent Leseigneur
 */
class ConfigurationHelper {

    Sql sql
    Logger logger
    DatabaseHelper databaseHelper

    def insertConfigurationFile(String fileName, long tenantId, String type, byte[] content) {
        logger.debug(String.format("insert configuration file | tenant id: %3d | type: %-25s | file name: %s", tenantId, type, fileName))
        sql.executeInsert("INSERT INTO configuration(tenant_id,content_type,resource_name,resource_content) VALUES (${tenantId}, ${type}, ${fileName}, ${ensureContentNeverEmpty(content)})")
    }

    def updateConfigurationFileContent(String fileName, long tenantId, String type, byte[] content) {
        logger.debug(String.format("update configuration file | tenant id: %3d | type: %-25s | file name: %s", tenantId, type, fileName))
        sql.execute("UPDATE configuration SET resource_content = ${ensureContentNeverEmpty(content)} WHERE tenant_id = ${tenantId} AND content_type = ${type} AND resource_name = ${fileName}")
    }

    def static ensureContentNeverEmpty(byte[] content) {
        return content.length == 0 ? System.lineSeparator().bytes : content
    }

    def deleteConfigurationFile(String fileName, long tenantId, String type) {
        logger.debug(String.format("removing configuration file | tenant id: %3d | type: %-25s | file name: %s", tenantId, type, fileName))
        sql.execute("DELETE FROM configuration WHERE tenant_id = ${tenantId} AND content_type = ${type} AND resource_name = ${fileName}")
    }

    def deleteConfigurationFileOnAnyTenantAndOfAnyType(String fileName) {
        logger.debug(String.format("removing all configuration files with name: %s", fileName))
        def nbRowsDeleted = sql.execute("DELETE FROM configuration WHERE resource_name = ${fileName}")
        logger.debug(String.format("deleted %s rows", nbRowsDeleted))
    }

    def appendToAllConfigurationFilesWithName(String filename, String toAppend) {
        def count = sql.firstRow("SELECT count(*) FROM configuration WHERE resource_name = ${filename}")
        if (count == 0) {
            throw new IllegalArgumentException('Configuration file ' + filename + ' does not exist in database.')
        }
        def results = sql.rows("SELECT tenant_id, content_type, resource_content FROM configuration WHERE resource_name = ${filename} ")
        results.each {
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            String content = databaseHelper.getBlobContentAsString(it.resource_content)

            content += "\n${toAppend}"
            updateConfigurationFileContent(filename, tenantId, contentType, content.bytes)
        }
    }

    def updateKeyInAllPropertyFiles(String fileName, String propertyKey, String newPropertyValue, String comment) {
        def updatedFiles = 0
        def results = sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """)
        results.each {
            String content = databaseHelper.getBlobContentAsString(it.resource_content)
            def properties = new Properties()
            def stringReader = new StringReader(content)
            def tenantId = it.tenant_id as long
            properties.load(stringReader)
            def newEntry = newPropertyEntry(propertyKey, newPropertyValue, "=", comment)
            if (!properties.containsKey(propertyKey)) {
                content += "\n$newEntry"
                updateConfigurationFileContent(fileName, tenantId, it.content_type, content.bytes)
                logger.info(String.format("add new property in configuration file | tenant id: %3d | type: %-25s | file name: %s | new property: %s", tenantId, it.content_type, fileName, newEntry))
            } else {
                def oldValue = properties.get(propertyKey)
                if (oldValue != newPropertyValue) {
                    def oldEntry = "${propertyKey}[ ]*=[ ]*${oldValue}"
                    def newContent = content.replaceAll(oldEntry, newEntry)
                    updateConfigurationFileContent(fileName, tenantId, it.content_type, newContent.bytes)
                    logger.info(String.format("update property in configuration file | tenant id: %3d | type: %-25s | file name: %s | new property: %s", tenantId, it.content_type, fileName, "${propertyKey}=${newPropertyValue}"))
                } else {
                    logger.info(String.format("property file is already up to date "))
                }
            }
            updatedFiles++
        }
        if (updatedFiles == 0) {
            throw new IllegalArgumentException("configuration file ${fileName} not found in database.")
        }
    }

    def appendToSpecificConfigurationFileIfPropertyIsMissing(String contentType, String fileName, String propertyKey, String propertyValue, String keyValueSeparator, String comment) {
        def results = sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}
                AND content_type=${contentType}      
                ORDER BY tenant_id 
                """)
        def foundFiles = appendToConfigurationFilesWithCommentIfPropertyIsMissing(results, propertyKey, keyValueSeparator, propertyValue, fileName, comment)
        if (foundFiles == 0) {
            throw new IllegalArgumentException("configuration file ${fileName} not found in database.")
        }
    }

    def appendToAllConfigurationFilesIfPropertyIsMissing(String fileName, String propertyKey, String propertyValue, String keyValueSeparator) {
        def results = sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """)
        def foundFiles = appendToConfigurationFilesWithCommentIfPropertyIsMissing(results, propertyKey, keyValueSeparator, propertyValue, fileName, null)
        if (foundFiles == 0) {
            throw new IllegalArgumentException("configuration file ${fileName} not found in database.")
        }
    }

    private int appendToConfigurationFilesWithCommentIfPropertyIsMissing(List<GroovyRowResult> results, String propertyKey, String keyValueSeparator, String newPropertyValue, String fileName, String comment) {
        def foundFiles = 0
        results.each {
            foundFiles++
            String content = databaseHelper.getBlobContentAsString(it.resource_content)
            def properties = new Properties()
            def stringReader = new StringReader(content)
            def tenantId = it.tenant_id as long
            properties.load(stringReader)
            if (!properties.containsKey(propertyKey)) {
                def newEntry = newPropertyEntry(propertyKey, newPropertyValue, keyValueSeparator, comment)
                content += "\n$newEntry"
                updateConfigurationFileContent(fileName, tenantId, it.content_type, content.bytes)
                logger.info(String.format("update configuration file | tenant id: %3d | type: %-25s | file name: %s | new property: %s", tenantId, it.content_type, fileName, newEntry))
            } else {
                logger.info(String.format("configuration file already up to date | tenant id: %3d | type: %-25s | file name: %s", tenantId, it.content_type, fileName))
            }
        }
        foundFiles
    }

    protected
    static GString newPropertyEntry(String propertyKey, String newPropertyValue, String keyValueSeparator, String comment) {
        comment ? "# ${comment}\n${propertyKey}${keyValueSeparator}${newPropertyValue}" : "${propertyKey}${keyValueSeparator}${newPropertyValue}"
    }

    def appendToAllConfigurationFilesIfPropertyIsMissing(String fileName, String propertyKey, String propertyValue) {
        appendToAllConfigurationFilesIfPropertyIsMissing(fileName, propertyKey, propertyValue, "=")
    }

    void updateAllConfigurationFilesIfPermissionValueIsMissing(String fileName, List<String> keysToConsider, String permissionValue) {
        def foundFiles = 0
        def results = sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """)
        results.each {
            foundFiles++
            def currentProperties = databaseHelper.getBlobContentAsString(it.resource_content).split("\n")
            def newProperties = new ArrayList<String>();
            def tenantId = it.tenant_id as long
            currentProperties.each { String line ->
                if (!line.startsWith("#") && !line.contains(permissionValue)) {
                    def keySeparatorIndex = line.indexOf("=")
                    if (keySeparatorIndex < 0 || !keysToConsider.contains(line.substring(0, keySeparatorIndex))) {
                        newProperties.add(line)
                    } else {
                        // Regular values end with ]
                        // for instance no_avatar_permission=[organization_visualization, profile_member_visualization]
                        def lastBracketIndex = line.lastIndexOf("]")
                        if (lastBracketIndex < 0) {
                            lastBracketIndex = line.length()
                        }
                        newProperties.add(line.substring(0, lastBracketIndex) + ", " + permissionValue + "]")
                    }
                } else {
                    newProperties.add(line)
                }
            }
            updateConfigurationFileContent(fileName, tenantId, it.content_type, newProperties.join("\n").bytes)
            logger.info(String.format("update configuration file | tenant id: %3d | type: %-25s | file name: %s", tenantId, it.content_type, fileName))
        }
        if (foundFiles == 0) {
            throw new IllegalArgumentException("configuration file ${fileName} not found in database.")
        }
    }
}
