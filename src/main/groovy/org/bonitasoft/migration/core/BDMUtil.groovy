/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.migration.core
/**
 *
 * Util class for BDM in bonita home
 *
 * @author Laurent Leseigneur
 *
 */
public class BDMUtil {

    def getBomDefinition(File clientBdmZipFile) {
        def xmlText = getBomXmlContent(clientBdmZipFile)
        def xmlRootNode = new XmlSlurper().parseText(xmlText)
        xmlRootNode

    }

    def getBomXmlContent(File clientBdmZipFile) {
        def xmlContent
        def tempDir = File.createTempDir()
        IOUtil.unzip(clientBdmZipFile.newInputStream(), tempDir)
        tempDir.listFiles().each {
            if ("bom.zip".equals(it.name)) {
                def bomTempDir = File.createTempDir()
                IOUtil.unzip(it.newInputStream(), bomTempDir)
                def bomXML = new File(bomTempDir, "bom.xml")
                xmlContent = bomXML.newInputStream().text
                bomTempDir.deleteDir()
            }
        }
        tempDir.deleteDir()
        xmlContent
    }
}