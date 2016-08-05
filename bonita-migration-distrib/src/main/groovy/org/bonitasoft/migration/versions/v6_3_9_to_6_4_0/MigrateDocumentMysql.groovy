/*
 *
 * Copyright (C) 2016 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.migration.versions.v6_3_9_to_6_4_0
/**
 * @author Laurent Leseigneur
 */
class MigrateDocumentMysql extends MigrateDocument {

    public MigrateDocumentMysql(def sql, def tenantId, def nextId, def falseValue, def trueValue) {
        super(sql, tenantId, nextId, falseValue, trueValue)
    }

    def createTempSequence() {
        return
    }


    def createTempDocTable() {

        sql.execute("""
                        CREATE TABLE temp_doc (
                            tenant_id BIGINT NOT NULL,
                            doc_mapping_id BIGINT NOT NULL,
                            doc_id BIGINT NOT NULL AUTO_INCREMENT,
                            PRIMARY KEY (doc_id)
                            ) ENGINE = INNODB """)

        sql.execute("ALTER TABLE temp_doc AUTO_INCREMENT = " + nextId)

        sql.execute("""
                        INSERT INTO temp_doc (tenant_id, doc_mapping_id)
                        SELECT  d.tenantid, d.id
                        FROM    document_mapping d
                        WHERE   tenantid = ? AND documentHasContent = ? """, tenantId, falseValue)


        sql.execute("CREATE INDEX temp_doc_idx ON temp_doc(tenant_id,doc_mapping_id)")
    }

    def updateDocumentMapping() {
        sql.execute("""
                    UPDATE document_mapping
                        INNER JOIN temp_doc t ON tenantid = t.tenant_id AND id = t.doc_mapping_id
                    SET documentid = t.doc_id""")

    }

    def createTempArchDocTable() {

        sql.execute("""
                        CREATE TABLE temp_arch_doc (
                            tenant_id BIGINT NOT NULL,
                            arch_doc_mapping_id BIGINT NOT NULL,
                            doc_id BIGINT NOT NULL AUTO_INCREMENT,
                            PRIMARY KEY (doc_id)
                            ) ENGINE = INNODB """)


        def nextValue = sql.firstRow("SELECT MAX(doc_id) +1  AS nextValue from temp_doc").nextValue
        sql.execute("ALTER TABLE temp_arch_doc AUTO_INCREMENT = ? ", nextValue)

        sql.execute("""
                        INSERT INTO temp_arch_doc (tenant_id, arch_doc_mapping_id)
                        SELECT  d.tenantid, d.id
                        FROM    arch_document_mapping d
                        WHERE   d.tenantid = ? AND d.documentHasContent = ? """, tenantId, falseValue)

    }

    def updateArchDocumentMapping() {

        sql.execute("""
                    UPDATE arch_document_mapping
                        INNER JOIN temp_arch_doc t ON tenantid = t.tenant_id AND id = t.arch_doc_mapping_id
                    SET documentid = t.doc_id""")

    }

    def updateSequenceValue() {

        def nextValue = sql.firstRow("SELECT MAX(doc_id) +1  AS nextValue from temp_arch_doc").nextValue
        sql.execute("""
                    UPDATE sequence SET nextid = ?
                    WHERE tenantid = ?
                    AND id = 10090 """, nextValue, tenantId)


    }

    def createTempArchVersionTable() {

        sql.execute("""
                    CREATE TABLE temp_arch_version AS
                    SELECT
                        a.tenantid,
                        a.id,
                        a.sourceobjectid,
                        (   SELECT count(*) +1
                            FROM arch_document_mapping b
                            WHERE a.tenantid = a.tenantid
                                AND a.sourceobjectid = b.sourceobjectid
                                AND a.documentCreationDate >= b.documentCreationDate
                                AND a.id > b.id
                        ) as new_version
                    FROM arch_document_mapping a
                    WHERE a.tenantid = ? """, tenantId)

    }

    def createTempVersionTable() {

        sql.execute("""
                    CREATE TABLE temp_version AS
                    SELECT
                        a.tenantid,
                        a.sourceobjectid,
                        (max(a.new_version) +1 ) as new_version
                    FROM temp_arch_version a
                    WHERE a.tenantid = """ + tenantId + " GROUP BY a.tenantid, a.sourceobjectid ")

    }

    def updateDocumentMappingVersion() {

        sql.execute("""
                    UPDATE document_mapping INNER JOIN temp_version
                        ON document_mapping.tenantid = temp_version.tenantid and document_mapping.id = temp_version.sourceobjectid
                    SET version = temp_version.new_version """)

    }

    def updateArchDocumentMappingVersion() {

        sql.execute("""
                    UPDATE arch_document_mapping
                        INNER JOIN temp_arch_version ON arch_document_mapping.tenantid = temp_arch_version.tenantid
                            AND arch_document_mapping.id = temp_arch_version.id
                    SET version = temp_arch_version.new_version """)

    }

    def dropTempSequence() {
        return
    }
}