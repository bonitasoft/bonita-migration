package org.bonitasoft.engine.migration

import org.bonitasoft.engine.migration.tables.ArchContractDataPre7_0Table
import org.bonitasoft.engine.migration.tables.ArchContractDataTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.*


class ArchContractDataRepository {


    fun read(fileName: String): List<CSVRecord> {
        var content = ArchContractDataRepository::class.java.getResourceAsStream("/$fileName")
        return CSVFormat.DEFAULT.parse(InputStreamReader(content)).toList()
    }

    fun createArchContractDataPre7_0(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchContractDataPre7_0Table.tenantId] = record.get(0).toLong()
        it[ArchContractDataPre7_0Table.id] = record.get(1).toLong()
        it[ArchContractDataPre7_0Table.kind] = record.get(2)
        it[ArchContractDataPre7_0Table.scopeId] = record.get(3).toLong()
        it[ArchContractDataPre7_0Table.name] = record.get(4)
        it[ArchContractDataPre7_0Table.val_] = serialize(record.get(5))
        it[ArchContractDataPre7_0Table.archiveDate] = record.get(6).toLong()
        it[ArchContractDataPre7_0Table.sourceObjectId] = record.get(7).toLong()
    }

    fun createArchContractData(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchContractDataTable.tenantId] = record.get(0).toLong()
        it[ArchContractDataTable.id] = record.get(1).toLong()
        it[ArchContractDataTable.kind] = record.get(2)
        it[ArchContractDataTable.scopeId] = record.get(3).toLong()
        it[ArchContractDataTable.name] = record.get(4)
        it[ArchContractDataTable.val_] = record.get(5)
        it[ArchContractDataTable.archiveDate] = record.get(6).toLong()
        it[ArchContractDataTable.sourceObjectId] = record.get(7).toLong()
    }

    fun getValuesOf_arch_contract_data_after_migration(): List<List<String>> {
        return read("arch_contract_data_after_migration.csv").map { record ->
            listOf(record.get(0),
                    record.get(1),
                    record.get(2),
                    record.get(3),
                    record.get(4),
                    record.get(5),
                    record.get(6),
                    record.get(7))
        }
    }


    private fun serialize(myValue: String): Blob {
        val baos = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(baos)
        objectOutputStream.writeObject(myValue)
        objectOutputStream.close()
        return SerialBlob(baos.toByteArray())
    }

    fun insert_arch_contract_data_backup_before_migration() {
        read("arch_contract_data_backup_before_migration.csv").forEach { entry ->
            ArchContractDataPre7_0Table.insert {
                createArchContractDataPre7_0(it, entry)
            }
        }
    }

    fun insert_arch_contract_data_before_migration() {
        read("arch_contract_data_before_migration.csv").forEach { entry ->
            ArchContractDataTable.insert {
                createArchContractData(it, entry)
            }
        }
    }

}