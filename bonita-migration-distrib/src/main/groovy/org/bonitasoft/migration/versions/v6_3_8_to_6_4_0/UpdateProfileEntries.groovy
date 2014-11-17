package org.bonitasoft.migration.versions.v6_3_8_to_6_4_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.MigrationUtil

/**
 * @author Elias Ricken de Medeiros
 */
class UpdateProfileEntries {

    public migrate(File feature, String dbVendor, Sql sql) {
        def parameters = [:]
        MigrationUtil.executeSqlFile(feature, dbVendor, "profiles", parameters, sql, true)
    }

}
