package org.bonitasoft.engine.migration.tables

import org.jetbrains.exposed.sql.Table


object ArchContractDataPre7_0Table : Table("arch_contract_data_backup") {
    val tenantId = long("tenantid")
    val id = long("id")
    val kind = varchar("kind", 20)
    val scopeId = long("scopeid")
    val name = varchar("name", 50)
    val val_ = blob("val").nullable()
    val archiveDate = long("archivedate")
    val sourceObjectId = long("sourceobjectid")

}
object ArchContractDataTable : Table("arch_contract_data") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val kind = varchar("kind", 20)
    val scopeId = long("scopeid")
    val name = varchar("name", 50)
    val val_ = text("val").nullable()
    val archiveDate = long("archivedate")
    val sourceObjectId = long("sourceobjectid")
}