package org.bonitasoft.migration.core

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
abstract class MigrationStep {

    enum DBVendor { ORACLE, POSTGRES, MYSQL, SQLSERVER}

    abstract execute(Sql sql, DBVendor dbVendor)

    abstract String getDescription()


}
