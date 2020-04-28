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
            def clientBdmZip = unzip(context.databaseHelper.getBlobContentAsBytes(clientBdmZipRow.content))
            def bomZip = unzip(clientBdmZip."bom.zip")
            def bomXml = new String(bomZip."bom.xml")
            def migratedBomXml = migrateBomXml(bomXml)
            bomZip."bom.xml" = migratedBomXml.bytes
            clientBdmZip."bom.zip" = zip(bomZip)
            def clientBdmZipContentMigrated = zip(clientBdmZip)

            context.logger.info("Adding namespace to BDM descriptor file $clientBdmZipRow.name with id $id of tenant $tenantId")
            context.logger.debug("Bom was: $bomXml")
            context.logger.debug("Bom is now: $migratedBomXml")

            context.sql.executeUpdate("UPDATE tenant_resource SET content = $clientBdmZipContentMigrated where id = $id and tenantId = $tenantId")
        }
    }

    @Override
    String getDescription() {
        "Add the namespace to the BDM xml descriptor for (bom.xml)"
    }

    def String migrateBomXml(String xml) {
        return xml.replace(tagToFind, "$tagToFind xmlns=\"$BDM_NAMESPACE\"")
    }
}
