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
            context.sql.executeUpdate("UPDATE dependency SET name = ${name.replace(filename, filename.substring(1))}, filename = ${filename.substring(1)} WHERE tenantid = ${it.tenantid} AND id = ${it.id}")
        }
    }

    @Override
    String getDescription() {
        return "remove unwanted '/' from the dependencies names"
    }
}
