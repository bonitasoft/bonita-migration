package org.bonitasoft.migration.version.to7_3_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Baptiste Mesta
 */
class FixDependenciesName extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        context.sql.eachRow("SELECT tenantid, id, name, filename FROM dependency WHERE filename LIKE '/%'") {
            def String name = it.name
            def String filename = it.filename
            def newName = nameWithoutSlash(name, filename)

            //We might have a previous dependency with the same name that was not replaced correctly
            def duplicatedDependency = context.sql.firstRow("SELECT tenantid, id, name, filename FROM dependency WHERE tenantid = ${it.tenantid} AND name = ${newName}")

            if (duplicatedDependency != null) {
                context.logger.info("Found duplicated dependencies, will keep the second one only: ")
                context.logger.info(duplicatedDependency.toMapString())
                context.logger.info(it.toRowResult().toMapString())
                context.sql.executeUpdate("DELETE FROM dependencymapping WHERE tenantid = ${it.tenantid} AND dependencyid = ${duplicatedDependency.id}")
                context.sql.executeUpdate("DELETE FROM dependency WHERE tenantid = ${it.tenantid} AND id = ${duplicatedDependency.id}")
            }
            context.sql.executeUpdate("UPDATE dependency SET name = ${newName}, filename = ${fileNameWithoutSlash(filename)} WHERE tenantid = ${it.tenantid} AND id = ${it.id}")
        }
    }

    private String fileNameWithoutSlash(String filename) {
        filename.substring(1)
    }

    private String nameWithoutSlash(String name, String filename) {
        name.replace(filename, filename.substring(1))
    }

    @Override
    String getDescription() {
        return "remove unwanted '/' from the dependencies names"
    }
}
