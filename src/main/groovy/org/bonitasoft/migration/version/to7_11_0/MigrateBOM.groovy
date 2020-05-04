package org.bonitasoft.migration.version.to7_11_0

import static org.bonitasoft.migration.core.IOUtil.unzip
import static org.bonitasoft.migration.core.IOUtil.zip

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class MigrateBOM extends MigrationStep {

    private static final String tagToFind = "<businessObjectModel";
    private static final String BDM_NAMESPACE = "http://documentation.bonitasoft.com/bdm-xml-schema/1.0";

    @Override
    def execute(MigrationContext context) {
        context.sql.eachRow("SELECT * FROM tenant_resource WHERE type = 'BDM' and name = 'client-bdm.zip'") { clientBdmZipRow ->
            long tenantId = clientBdmZipRow.tenantId
            long id = clientBdmZipRow.id
            def zip = context.databaseHelper.getBlobContentAsBytes(clientBdmZipRow.content)
            def clientBdmZipContentMigrated = migrateBOM(zip, context, id, tenantId)
            if (clientBdmZipContentMigrated != null) {
                context.sql.executeUpdate("UPDATE tenant_resource SET content = $clientBdmZipContentMigrated where id = $id and tenantId = $tenantId")
            }
        }
    }

    private byte[] migrateBOM(byte[] clientBdmZip, MigrationContext context, long id, long tenantId) {
        def clientBdmZipAsMap = unzip(clientBdmZip)
        def bomZip = unzip(clientBdmZipAsMap."bom.zip")
        def bomXml = new String(bomZip."bom.xml")
        def clientBdmZipContentMigrated
        if (!bomXml.contains(BDM_NAMESPACE)) {
            def migratedBomXml = addNamespace(bomXml)
            bomZip."bom.xml" = migratedBomXml.bytes
            clientBdmZipAsMap."bom.zip" = zip(bomZip)
            clientBdmZipContentMigrated = zip(clientBdmZipAsMap)
            context.logger.info("Adding namespace to BDM descriptor file client-bdm.zip with id $id of tenant $tenantId")
            context.logger.debug("Bom was: $bomXml")
            context.logger.debug("Bom is now: $migratedBomXml")
        } else {
            context.logger.info("BDM descriptor file client-bdm.zip with id $id of tenant $tenantId already have the correct namespace... nothing to do.")
        }
        return clientBdmZipContentMigrated
    }

    @Override
    String getDescription() {
        "Add the namespace to the BDM xml descriptor for (bom.xml)"
    }

    String addNamespace(String xml) {
        return xml.replace(tagToFind, "$tagToFind xmlns=\"$BDM_NAMESPACE\"")
    }
}
