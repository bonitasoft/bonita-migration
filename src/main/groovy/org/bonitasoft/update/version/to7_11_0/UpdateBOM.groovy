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
package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

import static org.bonitasoft.update.core.IOUtil.unzip
import static org.bonitasoft.update.core.IOUtil.zip

class UpdateBOM extends UpdateStep {

    private static final String tagToFind = "<businessObjectModel"
    private static final String BDM_NAMESPACE = "http://documentation.bonitasoft.com/bdm-xml-schema/1.0"

    @Override
    def execute(UpdateContext context) {
        context.sql.eachRow("SELECT * FROM tenant_resource WHERE type = 'BDM' and name = 'client-bdm.zip'") { clientBdmZipRow ->
            long tenantId = clientBdmZipRow.tenantId
            long id = clientBdmZipRow.id
            def zip = context.databaseHelper.getBlobContentAsBytes(clientBdmZipRow.content)
            def clientBdmZipContentUpdated = updateBOM(zip, context, id, tenantId)
            if (clientBdmZipContentUpdated != null) {
                context.sql.executeUpdate("UPDATE tenant_resource SET content = $clientBdmZipContentUpdated where id = $id and tenantId = $tenantId")
            }
        }
    }

    protected byte[] updateBOM(byte[] clientBdmZip, UpdateContext context, long id, long tenantId) {
        def clientBdmZipAsMap = unzip(clientBdmZip)
        def bomZip = unzip(clientBdmZipAsMap."bom.zip")
        def bomXml = new String(bomZip."bom.xml")
        def clientBdmZipContentUpdated
        if (!bomXml.contains(BDM_NAMESPACE)) {
            def updatedBomXml = addNamespace(bomXml)
            bomZip."bom.xml" = updatedBomXml.bytes
            clientBdmZipAsMap."bom.zip" = zip(bomZip)
            clientBdmZipContentUpdated = zip(clientBdmZipAsMap)
            context.logger.info("Adding namespace to BDM descriptor file client-bdm.zip with id $id of tenant $tenantId")
            context.logger.debug("Bom was: $bomXml")
            context.logger.debug("Bom is now: $updatedBomXml")
        } else {
            context.logger.info("BDM descriptor file client-bdm.zip with id $id of tenant $tenantId already have the correct namespace... nothing to do.")
        }
        return clientBdmZipContentUpdated
    }

    @Override
    String getDescription() {
        "Add the namespace to the BDM xml descriptor for (bom.xml)"
    }

    String addNamespace(String xml) {
        return xml.replace(tagToFind, "$tagToFind xmlns=\"$BDM_NAMESPACE\"")
    }
}
