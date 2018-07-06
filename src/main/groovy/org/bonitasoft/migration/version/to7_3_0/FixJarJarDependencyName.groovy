package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Emmanuel Duchastenier
 */
class FixJarJarDependencyName extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.sql.eachRow("""
SELECT  tenantid, id, filename
FROM    dependency
WHERE   lower(filename) LIKE '%.jar.jar'
OR      lower(filename) LIKE '%.zip.jar'
""") {
            String filename = it.filename
            def newFilename = filename.substring(0, filename.length() - 4)
            context.logger.info("Fixing erroneous dependency filename '$filename' => '$newFilename' for tenantid = ${it.tenantid} AND id = ${it.id}")
            context.sql.executeUpdate("UPDATE dependency SET filename = ${newFilename} WHERE tenantid = ${it.tenantid} AND id = ${it.id}")
        }
    }

    @Override
    String getDescription() {
        return "remove unwanted double '.jar.jar' extension from the dependency filename column"
    }
}
