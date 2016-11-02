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
class MigrateDocumentOracle extends MigrateDocument {

    public MigrateDocumentOracle(def sql, def tenantId, def nextId, def falseValue, def trueValue) {
        super(sql, tenantId, nextId, falseValue, trueValue)
    }

    def createTempSequence() {
        sql.execute "CREATE SEQUENCE seq_document START WITH " + nextId + " CACHE 1000 NOCYCLE"

    }

    def createTempDocTable() {
        sql.execute("""
                        CREATE TABLE temp_doc AS
                        SELECT  d.tenantid AS tenant_id, d.id AS doc_mapping_id, seq_document.nextval AS doc_id
                        FROM    document_mapping d
                        WHERE   tenantid = """ + tenantId + " AND documentHasContent = " + falseValue)


        sql.execute("CREATE INDEX temp_doc_idx ON temp_doc(tenant_id,doc_mapping_id)")
    }

    def updateDocumentMapping() {
        sql.execute("""
                    UPDATE document_mapping d SET d.documentid =(
                        SELECT  t.doc_id
                        FROM temp_doc t
                        WHERE d.tenantid = t.tenant_id
                        AND d.id = t.doc_mapping_id)
                    WHERE tenantid = ? AND documentHasContent = ? """, tenantId, falseValue)

    }

    def createTempArchDocTable() {
        sql.execute("""
                        CREATE TABLE temp_arch_doc AS
                        SELECT  d.tenantid AS tenant_id, d.id AS arch_doc_mapping_id, seq_document.nextval AS doc_id
                        FROM    arch_document_mapping d
                        WHERE   d.tenantid = """ + tenantId + " AND d.documentHasContent = " + falseValue)

    }

    def updateArchDocumentMapping() {
        sql.execute("""
                    UPDATE arch_document_mapping a SET a.documentid = (
                        SELECT  t.doc_id
                        FROM temp_arch_doc t
                        WHERE a.tenantid = t.tenant_id
                        AND a.id = t.arch_doc_mapping_id )
                     WHERE tenantid = ? AND documentHasContent = ? """, tenantId, falseValue)


    }

    def updateSequenceValue() {
        sql.execute("""
                    UPDATE sequence SET nextid = seq_document.nextval
                    WHERE tenantid = $tenantId
                    AND id = 10090 """)


    }

    def createTempArchVersionTable() {
        sql.execute("""
                    CREATE TABLE temp_arch_version AS
                    SELECT
                        a.tenantid,
                        a.id,
                        a.sourceobjectid,
                        rank() OVER(
                                PARTITION BY a.tenantid, a.sourceobjectid
                                ORDER BY documentCreationDate,id ASC
                        ) as new_version
                    FROM arch_document_mapping a
                    WHERE a.tenantid =  """ + tenantId)


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
                    UPDATE document_mapping SET version = (
                        SELECT temp_version.new_version
                        FROM temp_version
                        WHERE document_mapping.tenantid = temp_version.tenantid
                        AND document_mapping.id = temp_version.sourceobjectid )
                    WHERE document_mapping.TENANTID= """ + tenantId + """
                    AND EXISTS
                        (SELECT 1
                        FROM temp_version
                        WHERE document_mapping.tenantid = temp_version.tenantid
                        AND document_mapping.id = temp_version.sourceobjectid )""")

    }

    def updateArchDocumentMappingVersion() {
        sql.execute("""
                    UPDATE arch_document_mapping SET version = (
                        SELECT temp_arch_version.new_version
                        FROM temp_arch_version
                        WHERE arch_document_mapping.tenantid = temp_arch_version.tenantid
                        AND arch_document_mapping.id = temp_arch_version.id )
                    WHERE arch_document_mapping.tenantid= """ + tenantId)

    }

    def dropTempSequence() {
        sql.execute("drop sequence seq_document")

    }

}
