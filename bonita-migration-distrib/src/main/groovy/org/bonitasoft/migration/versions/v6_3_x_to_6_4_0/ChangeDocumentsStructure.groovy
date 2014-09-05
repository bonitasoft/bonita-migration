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
package org.bonitasoft.migration.versions.v6_3_x_to_6_4_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.IOUtil
import org.bonitasoft.migration.core.MigrationUtil

/**
 *
 * Migration of documents structure
 *
 *
 * @author Baptiste Mesta
 */
class ChangeDocumentsStructure {


    public migrate(Sql sql, String dbVendor) {

        def tenantsId = MigrationUtil.getTenantsId(dbVendor, sql)
        IOUtil.executeWrappedWithTabs {

            /*
             *     remove foreign key
             */
            sql.execute("ALTER TABLE document_content DROP " + (dbVendor == "mysql"?"FOREIGN KEY":"CONSTRAINT") +
                    " fk_document_content_tenantId")

            /*
             *     rename table
             */
            sql.execute("ALTER TABLE document_content RENAME TO document")

            /*
             *     add columns
             */
            //document_mapping
            sql.execute("ALTER TABLE document_mapping ADD documentid BIGINT DEFAULT 0 NOT NULL")
            sql.execute("ALTER TABLE document_mapping ADD description TEXT")
            sql.execute("ALTER TABLE document_mapping ADD version VARCHAR(10) DEFAULT '1' NOT NULL")
            sql.execute("ALTER TABLE document_mapping RENAME documentName TO name")
            //arch_document_mapping
            sql.execute("ALTER TABLE arch_document_mapping ADD documentid BIGINT DEFAULT 0 NOT NULL")
            sql.execute("ALTER TABLE arch_document_mapping ADD description TEXT")
            sql.execute("ALTER TABLE arch_document_mapping ADD version VARCHAR(10) DEFAULT '1' NOT NULL")
            sql.execute("ALTER TABLE arch_document_mapping RENAME documentName TO name")
            //document
            sql.execute("ALTER TABLE document ADD author BIGINT")
            sql.execute("ALTER TABLE document ADD creationdate BIGINT DEFAULT 0 NOT NULL")
            sql.execute("ALTER TABLE document ADD hascontent BOOLEAN DEFAULT true NOT NULL")
            sql.execute("ALTER TABLE document ADD filename VARCHAR(255)")
            sql.execute("ALTER TABLE document ADD mimetype VARCHAR(255)")
            sql.execute("ALTER TABLE document ADD url VARCHAR(1024) NULL")
            sql.execute("ALTER TABLE document ALTER COLUMN content DROP NOT NULL")

            /*
             *     move data
             */
            sql.executeUpdate('''UPDATE document SET (author, creationdate, hascontent, filename, mimetype, url)
= (document_mapping.documentAuthor, document_mapping.documentCreationDate, document_mapping.documentHasContent, document_mapping.documentContentFileName, document_mapping.documentContentMimeType, document_mapping.documentURL)
FROM document_mapping WHERE document_mapping.contentStorageId = document.documentId AND document_mapping.tenantid = document.tenantid''')
            sql.executeUpdate('''UPDATE document SET (author, creationdate, hascontent, filename, mimetype, url)
= (arch_document_mapping.documentAuthor, arch_document_mapping.documentCreationDate, arch_document_mapping.documentHasContent, arch_document_mapping.documentContentFileName, arch_document_mapping.documentContentMimeType, arch_document_mapping.documentURL)
FROM arch_document_mapping WHERE arch_document_mapping.contentStorageId = document.documentId AND arch_document_mapping.tenantid = document.tenantid''')
            //for archive
            sql.executeUpdate('''UPDATE document_mapping SET documentid = document.id
FROM document WHERE document_mapping.contentStorageId = document.documentId AND document_mapping.tenantid = document.tenantid''')
            sql.executeUpdate('''UPDATE arch_document_mapping SET documentid = document.id
FROM document WHERE arch_document_mapping.contentStorageId = document.documentId AND arch_document_mapping.tenantid = document.tenantid''')

            tenantsId.each { tenantId ->
                //get max id for document (=10090)
                def nextId = sql.firstRow("SELECT nextid FROM sequence WHERE tenantid = $tenantId AND id = 10090").nextid
                //create new document when mapping is an url
                sql.eachRow("SELECT * FROM document_mapping WHERE tenantid = $tenantId AND documentURL IS NOT NULL") { row ->
                    sql.executeInsert("INSERT INTO document (tenantid,id,author,creationdate,hascontent,filename,mimetype,url,documentid) VALUES ($tenantId,$nextId,$row.documentAuthor,$row.documentCreationDate,$row.documentHasContent,$row.documentContentFileName,$row.documentContentMimeType,$row.documentURL,'temp')")
                    sql.executeUpdate("UPDATE document_mapping SET documentid = $nextId WHERE document_mapping.id = $row.id AND document_mapping.tenantid = $tenantId")
                    nextId++
                }
                sql.eachRow("SELECT * FROM arch_document_mapping WHERE tenantid = $tenantId AND documentURL IS NOT NULL") { row ->
                    sql.executeInsert("INSERT INTO document (tenantid,id,author,creationdate,hascontent,filename,mimetype,url,documentid) VALUES ($tenantId,$nextId,$row.documentAuthor,$row.documentCreationDate,$row.documentHasContent,$row.documentContentFileName,$row.documentContentMimeType,$row.documentURL,'temp')")
                    sql.executeUpdate("UPDATE arch_document_mapping SET documentid = $nextId WHERE arch_document_mapping.id = $row.id AND arch_document_mapping.tenantid = $tenantId")
                    nextId++
                }
                sql.executeUpdate("UPDATE sequence SET nextid = $nextId WHERE tenantid=$tenantId AND id = 10090")
                sql.eachRow("SELECT DISTINCT sourceobjectid FROM arch_document_mapping WHERE tenantid = $tenantId") { row ->
                    def version = "1";
                    sql.eachRow("SELECT id from arch_document_mapping WHERE tenantid = $tenantId AND sourceobjectid = $row.sourceobjectid ORDER BY documentCreationDate ASC") { archmapping ->
                        if (version == "1") {
                            version++
                            return
                        }
                        sql.executeUpdate("UPDATE arch_document_mapping SET version = $version WHERE tenantid = $tenantId AND id = $archmapping.id ")
                        version++
                    }
                    if (version != "1") {
                        sql.executeUpdate("UPDATE document_mapping SET version = $version WHERE tenantid = $tenantId AND id = $row.sourceobjectid ")
                    }
                }
            }

            /*
             *     remove old columns
             */
            //document
            sql.execute("ALTER TABLE document DROP documentId")
            //document_mapping
            sql.execute("ALTER TABLE document_mapping DROP documentAuthor")
            sql.execute("ALTER TABLE document_mapping DROP documentCreationDate")
            sql.execute("ALTER TABLE document_mapping DROP documentHasContent")
            sql.execute("ALTER TABLE document_mapping DROP documentContentFileName")
            sql.execute("ALTER TABLE document_mapping DROP documentContentMimeType")
            sql.execute("ALTER TABLE document_mapping DROP contentStorageId")
            sql.execute("ALTER TABLE document_mapping DROP documentURL")
            //arch_document_mapping
            sql.execute("ALTER TABLE arch_document_mapping DROP documentAuthor")
            sql.execute("ALTER TABLE arch_document_mapping DROP documentCreationDate")
            sql.execute("ALTER TABLE arch_document_mapping DROP documentHasContent")
            sql.execute("ALTER TABLE arch_document_mapping DROP documentContentFileName")
            sql.execute("ALTER TABLE arch_document_mapping DROP documentContentMimeType")
            sql.execute("ALTER TABLE arch_document_mapping DROP contentStorageId")
            sql.execute("ALTER TABLE arch_document_mapping DROP documentURL")

            /*
             *     add new foreign keys
             */
            sql.execute("ALTER TABLE document_mapping ADD CONSTRAINT fk_docmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE;")
            sql.execute("ALTER TABLE arch_document_mapping ADD CONSTRAINT fk_archdocmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE;")

        }
    }

}
