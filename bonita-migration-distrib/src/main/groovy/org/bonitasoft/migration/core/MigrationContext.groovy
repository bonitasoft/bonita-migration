package org.bonitasoft.migration.core

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
class MigrationContext {

    def MigrationStep.DBVendor dbVendor
    def Sql sql
    def File bonitaHome
}
