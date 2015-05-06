/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
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

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep
import org.bonitasoft.migration.core.IOUtil
import org.bonitasoft.migration.core.MigrationUtil

/**
 *
 * Migration of documents structure
 *
 *
 * @author Baptiste Mesta
 */
class ChangeDocumentsStructure extends DatabaseMigrationStep {

    ChangeDocumentsStructure(Sql sql, String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    public migrate() {

        def tenantsId = MigrationUtil.getTenantsId(dbVendor, sql)
        IOUtil.executeWrappedWithTabs {

            /*
             *     remove foreign key
             */
            if(!dbVendor.equals("oracle")){//not on oracle
                dropForeignKey("document_content","fk_document_content_tenantId")
            }

            /*
             *     rename table
             */
            renameTable("document_content","document")

            /*
             *     add columns
             */
            //document_mapping
            addColumn("document_mapping","documentid","BIGINT","0","NOT NULL")
            addColumn("document_mapping","description","TEXT",null,null)
            addColumn("document_mapping","version","VARCHAR(10)","'1'","NOT NULL")
            renameColumn("document_mapping", "documentName", "name","VARCHAR(50) NOT NULL")
            //arch_document_mapping
            addColumn("arch_document_mapping","documentid","BIGINT","0","NOT NULL")
            addColumn("arch_document_mapping","description","TEXT",null,null)
            addColumn("arch_document_mapping","version","VARCHAR(10)","'1'","NOT NULL")
            renameColumn("arch_document_mapping", "documentName", "name", "VARCHAR(50) NOT NULL")
            //document
            addColumn("document","author","BIGINT",null,null)
            addColumn("document","creationdate","BIGINT","0","NOT NULL")
            addColumn("document","hascontent","BOOLEAN","true","NOT NULL")
            addColumn("document","filename","VARCHAR(255)",null,null)
            addColumn("document","mimetype","VARCHAR(255)",null,null)
            addColumn("document","url","VARCHAR(1024)",null,"NULL")

            /*
             *     move data
             */
            executeUpdate(getUpdateDocumentQuery("document_mapping"))
            executeUpdate(getUpdateDocumentQuery("arch_document_mapping"))
            //for archive
            executeUpdate(getUpdateDocumentMappingQuery("document_mapping"))
            executeUpdate(getUpdateDocumentMappingQuery("arch_document_mapping"))

            tenantsId.each { tenantId ->
                //get max id for document (=10090)
                def nextId = sql.firstRow("SELECT nextid FROM sequence WHERE tenantid = $tenantId AND id = 10090").nextid
                //create new document when mapping is an url
                sql.eachRow("SELECT * FROM document_mapping WHERE tenantid = $tenantId AND documentURL IS NOT NULL") { row ->
                    sql.executeInsert("INSERT INTO document (tenantid,id,author,creationdate,hascontent,filename,mimetype,url,documentid) VALUES ($tenantId,$nextId,$row.documentAuthor,$row.documentCreationDate,$row.documentHasContent,$row.documentContentFileName,$row.documentContentMimeType,$row.documentURL,'temp')")
                    executeUpdate("UPDATE document_mapping SET documentid = $nextId WHERE document_mapping.id = $row.id AND document_mapping.tenantid = $tenantId")
                    nextId++
                }
                sql.eachRow("SELECT * FROM arch_document_mapping WHERE tenantid = $tenantId AND documentURL IS NOT NULL") { row ->
                    sql.executeInsert("INSERT INTO document (tenantid,id,author,creationdate,hascontent,filename,mimetype,url,documentid) VALUES ($tenantId,$nextId,$row.documentAuthor,$row.documentCreationDate,$row.documentHasContent,$row.documentContentFileName,$row.documentContentMimeType,$row.documentURL,'temp')")
                    executeUpdate("UPDATE arch_document_mapping SET documentid = $nextId WHERE arch_document_mapping.id = $row.id AND arch_document_mapping.tenantid = $tenantId")
                    nextId++
                }
                executeUpdate("UPDATE sequence SET nextid = $nextId WHERE tenantid=$tenantId AND id = 10090")
                sql.eachRow("SELECT DISTINCT sourceobjectid FROM arch_document_mapping WHERE tenantid = $tenantId") { row ->
                    def version = "1";
                    sql.eachRow("SELECT id from arch_document_mapping WHERE tenantid = $tenantId AND sourceobjectid = $row.sourceobjectid ORDER BY documentCreationDate ASC") { archmapping ->
                        if (version == "1") {
                            version = "2"
                            return
                        }
                        executeUpdate("UPDATE arch_document_mapping SET version = $version WHERE tenantid = $tenantId AND id = $archmapping.id ")
                        version = String.valueOf(Integer.valueOf(version)+1)
                    }
                    if (version != "1") {
                        executeUpdate("UPDATE document_mapping SET version = $version WHERE tenantid = $tenantId AND id = $row.sourceobjectid ")
                    }
                }
            }

            /*
             *     remove old columns
             */
            //document
            dropColumn("document","documentId")

            //document_mapping
            dropColumn("document_mapping","documentAuthor")
            dropColumn("document_mapping","documentCreationDate")
            dropColumn("document_mapping","documentHasContent")
            dropColumn("document_mapping","documentContentFileName")
            dropColumn("document_mapping","documentContentMimeType")
            dropColumn("document_mapping","contentStorageId")
            dropColumn("document_mapping","documentURL")
            //arch_document_mapping
            dropColumn("arch_document_mapping","documentAuthor")
            dropColumn("arch_document_mapping","documentCreationDate")
            dropColumn("arch_document_mapping","documentHasContent")
            dropColumn("arch_document_mapping","documentContentFileName")
            dropColumn("arch_document_mapping","documentContentMimeType")
            dropColumn("arch_document_mapping","contentStorageId")
            dropColumn("arch_document_mapping","documentURL")

            /*
             *     add new foreign keys
             */
            execute("ALTER TABLE document_mapping ADD CONSTRAINT fk_docmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE")
            execute("ALTER TABLE arch_document_mapping ADD CONSTRAINT fk_archdocmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE")
            if(!dbVendor.equals("oracle")) {//not on oracle
                execute("ALTER TABLE document ADD CONSTRAINT fk_document_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id)")
            }

            addColumn("document_mapping","index_","INT","'-1'","NOT NULL")
            addColumn("arch_document_mapping","index_","INT","'-1'","NOT NULL")

        }

        /*
         *  Add column to support list of documents
         */

    }

    private GString getUpdateDocumentQuery(tableName) {
        switch (dbVendor) {
            case "mysql":
                return "UPDATE document INNER JOIN $tableName ON ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid  SET document.author = ${tableName}.documentAuthor, document.creationdate = ${tableName}.documentCreationDate, document.hascontent = ${tableName}.documentHasContent, document.filename = ${tableName}.documentContentFileName, document.mimetype = ${tableName}.documentContentMimeType, document.url = ${tableName}.documentURL"
            case "sqlserver":
                return "UPDATE document SET document.author = ${tableName}.documentAuthor, document.creationdate = ${tableName}.documentCreationDate, document.hascontent = ${tableName}.documentHasContent, document.filename = ${tableName}.documentContentFileName, document.mimetype = ${tableName}.documentContentMimeType, document.url = ${tableName}.documentURL FROM $tableName, document WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid"
            case "oracle":
                return "UPDATE document SET (author, creationdate, hascontent, filename, mimetype, url) = (SELECT ${tableName}.documentAuthor, ${tableName}.documentCreationDate, ${tableName}.documentHasContent, ${tableName}.documentContentFileName, ${tableName}.documentContentMimeType, ${tableName}.documentURL FROM ${tableName} WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid) WHERE EXISTS (SELECT ${tableName}.id FROM ${tableName} WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid)"
            default:
                return "UPDATE document SET (author, creationdate, hascontent, filename, mimetype, url) = (${tableName}.documentAuthor, ${tableName}.documentCreationDate, ${tableName}.documentHasContent, ${tableName}.documentContentFileName, ${tableName}.documentContentMimeType, ${tableName}.documentURL) FROM ${tableName} WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid"
        }
    }

    private GString getUpdateDocumentMappingQuery(tableName) {
        switch (dbVendor) {
            case "mysql":
                return "UPDATE ${tableName} INNER JOIN document ON ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid SET ${tableName}.documentid = document.id"
            case "sqlserver":
                return "UPDATE ${tableName} SET ${tableName}.documentid = document.id FROM ${tableName}, document WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid "
            case "oracle":
                return "UPDATE ${tableName} SET documentid = ( SELECT document.id FROM document WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid )   WHERE EXISTS (SELECT document.id FROM document WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid)"
            default:
                return "UPDATE ${tableName} SET documentid = document.id FROM document WHERE ${tableName}.contentStorageId = document.documentId AND ${tableName}.tenantid = document.tenantid"
        }
    }
}
