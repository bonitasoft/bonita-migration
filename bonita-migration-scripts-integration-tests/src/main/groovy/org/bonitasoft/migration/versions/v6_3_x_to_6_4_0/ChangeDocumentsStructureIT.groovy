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
import org.bonitasoft.migration.CustomAssertion
import org.bonitasoft.migration.versions.v6_3_9_to_6_4_0.ChangeDocumentsStructure
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*

/**
 * @author Baptiste Mesta
 *
 */
class ChangeDocumentsStructureIT extends GroovyTestCase {



    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "documents")

        tester.dataSet = dataSet {
            tenant id: 1

            //sequences
            sequence tenantid: 1, id: 10090, nextid: 304

            document_content tenantid: 1, id: 99, documentId: '-4561234568646477', content: "the old content1".getBytes()
            document_content tenantid: 1, id: 100, documentId: '-4561234568646452', content: "the content1".getBytes()
            document_content tenantid: 1, id: 101, documentId: '-4561485668646452', content: "the content2".getBytes()
            document_content tenantid: 1, id: 102, documentId: '4561237768646452', content: "the content3".getBytes()
            document_content tenantid: 1, id: 201, documentId: '6861234568646455', content: "the content4".getBytes()

            document_mapping tenantid: 1, id: 300, processinstanceid: 800, documentName: "mydoc1", documentAuthor: 900, documentCreationDate: 4567890003, documentHasContent: trueValue(), documentContentFileName: "file1.txt", documentContentMimeType: "plain/text", contentStorageId: '-4561234568646452', documentURL: "[NULL]"
            document_mapping tenantid: 1, id: 301, processinstanceid: 800, documentName: "mydoc2", documentAuthor: 900, documentCreationDate: 4567892316, documentHasContent: trueValue(), documentContentFileName: "file2.txt", documentContentMimeType: "application/octet-stream", contentStorageId: '-4561485668646452', documentURL: "[NULL]"
            document_mapping tenantid: 1, id: 302, processinstanceid: 801, documentName: "mydoc3", documentAuthor: 901, documentCreationDate: 4567892317, documentHasContent: trueValue(), documentContentFileName: "file3.txt", documentContentMimeType: "plain/text", contentStorageId: '4561237768646452', documentURL: "[NULL]"
            document_mapping tenantid: 1, id: 303, processinstanceid: 802, documentName: "mydoc4", documentAuthor: 902, documentCreationDate: 4567892318, documentHasContent: trueValue(), documentContentFileName: "file4.txt", documentContentMimeType: "plain/text", contentStorageId: '6861234568646455', documentURL: "[NULL]"
            document_mapping tenantid: 1, id: 304, processinstanceid: 803, documentName: "mydoc5", documentAuthor: 902, documentCreationDate: 4567892318, documentHasContent: falseValue(), documentContentFileName: "file5.txt", documentContentMimeType: "plain/text", contentStorageId: "[NULL]", documentURL: "http://myurl.com/thefile.txt"

            arch_document_mapping tenantid: 1, id: 1300, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890001, documentHasContent: trueValue(), documentContentFileName: "oldfile1.txt", documentContentMimeType: "plain/text", contentStorageId: '-4561234568646477', documentURL: "[NULL]", archivedate: 4567892314
            arch_document_mapping tenantid: 1, id: 1301, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile2.txt", archivedate: 4567892101
            arch_document_mapping tenantid: 1, id: 1302, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile21.txt", archivedate: 4567892102
            arch_document_mapping tenantid: 1, id: 1303, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile22.txt", archivedate: 4567892103
            arch_document_mapping tenantid: 1, id: 1304, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile23.txt", archivedate: 4567892104
            arch_document_mapping tenantid: 1, id: 1305, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile24.txt", archivedate: 4567892105
            arch_document_mapping tenantid: 1, id: 1306, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile25.txt", archivedate: 4567892106
            arch_document_mapping tenantid: 1, id: 1307, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile26.txt", archivedate: 4567892107
            arch_document_mapping tenantid: 1, id: 1308, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile27.txt", archivedate: 4567892108
            arch_document_mapping tenantid: 1, id: 1309, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile28.txt", archivedate: 4567892109
            arch_document_mapping tenantid: 1, id: 1310, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile29.txt", archivedate: 4567892110
            arch_document_mapping tenantid: 1, id: 1311, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile210.txt", archivedate: 4567892111
            arch_document_mapping tenantid: 1, id: 1312, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile211.txt", archivedate: 4567892112
            arch_document_mapping tenantid: 1, id: 1313, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile212.txt", archivedate: 4567892113
            arch_document_mapping tenantid: 1, id: 1314, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile213.txt", archivedate: 4567892114
            arch_document_mapping tenantid: 1, id: 1315, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile214.txt", archivedate: 4567892115
            arch_document_mapping tenantid: 1, id: 1316, sourceobjectid: 300, processinstanceid: 800, documentName: "oldmydoc1", documentAuthor: 901, documentCreationDate: 4567890002, documentHasContent: falseValue(), documentContentFileName: "oldfile2.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile215.txt", archivedate: 4567892116
            arch_document_mapping tenantid: 1, id: 1317, sourceobjectid: 299, processinstanceid: 804, documentName: "archedDoc", documentAuthor: 901, documentCreationDate: 4567892325, documentHasContent: falseValue(), documentContentFileName: "archedDoc.txt", documentContentMimeType: "plain/text", contentStorageId: '[NULL]', documentURL: "http//myurl.com/myfile.txt", archivedate: 4567892315
        }
        tester.onSetup();
    }



    @Override
    void tearDown() {
        tester.onTearDown();

        def String[]  strings = ["document_mapping",
                       "arch_document_mapping",
                       "document",
                       "tenant",
                       "sequence"]
        dropTables(sql, strings)
    }


    void test_migration_documents_structure() {
        println "====start test test_migration_documents_structure"
        new ChangeDocumentsStructure(sql, dbVendor()).migrate();

        println "====executed migration"
        def updatedDocuments = tester.connection.createDataSet("document_mapping", "arch_document_mapping", "document", "sequence");

        println "====result"
        updatedDocuments.tableNames.each { table ->


            for (int i = 0; i < updatedDocuments.getTable(table).getRowCount(); i++) {
                print table+" "
                updatedDocuments.getTable(table).getTableMetaData().getColumns().each { column ->
                    print column.getColumnName()+ ": "
                    print updatedDocuments.getTable(table).getValue(i,column.getColumnName())
                    print ", "
                }
                print "\n"
            }
        }

        //TODO document content without mapping?
        CustomAssertion.assertEquals dataSet {

            sequence tenantid: 1, id: 10090, nextid: 322

            document tenantid: 1, id: 99, author: 901, creationdate: 4567890001, hascontent: trueValue(), filename: "oldfile1.txt", mimetype: "plain/text", content: "the old content1".getBytes(), url: "[NULL]"
            document tenantid: 1, id: 100, author: 900, creationdate: 4567890003, hascontent: trueValue(), filename: "file1.txt", mimetype: "plain/text", content: "the content1".getBytes(), url: "[NULL]"
            document tenantid: 1, id: 101, author: 900, creationdate: 4567892316, hascontent: trueValue(), filename: "file2.txt", mimetype: "application/octet-stream", content: "the content2".getBytes(), url: "[NULL]"
            document tenantid: 1, id: 102, author: 901, creationdate: 4567892317, hascontent: trueValue(), filename: "file3.txt", mimetype: "plain/text", content: "the content3".getBytes(), url: "[NULL]"
            document tenantid: 1, id: 201, author: 902, creationdate: 4567892318, hascontent: trueValue(), filename: "file4.txt", mimetype: "plain/text", content: "the content4".getBytes(), url: "[NULL]"
            document tenantid: 1, id: 304, author: 902, creationdate: 4567892318, hascontent: falseValue(), filename: "file5.txt", mimetype: "plain/text", content: "[NULL]", url: 'http://myurl.com/thefile.txt'
            document tenantid: 1, id: 305, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile2.txt'
            document tenantid: 1, id: 306, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile21.txt'
            document tenantid: 1, id: 307, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile22.txt'
            document tenantid: 1, id: 308, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile23.txt'
            document tenantid: 1, id: 309, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile24.txt'
            document tenantid: 1, id: 310, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile25.txt'
            document tenantid: 1, id: 311, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile26.txt'
            document tenantid: 1, id: 312, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile27.txt'
            document tenantid: 1, id: 313, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile28.txt'
            document tenantid: 1, id: 314, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile29.txt'
            document tenantid: 1, id: 315, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile210.txt'
            document tenantid: 1, id: 316, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile211.txt'
            document tenantid: 1, id: 317, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile212.txt'
            document tenantid: 1, id: 318, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile213.txt'
            document tenantid: 1, id: 319, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile214.txt'
            document tenantid: 1, id: 320, author: 901, creationdate: 4567890002, hascontent: falseValue(), filename: "oldfile2.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile215.txt'
            document tenantid: 1, id: 321, author: 901, creationdate: 4567892325, hascontent: falseValue(), filename: "archedDoc.txt", mimetype: "plain/text", content: "[NULL]", url: 'http//myurl.com/myfile.txt'



            document_mapping tenantid: 1, id: 300, processinstanceid: 800, documentid: 100, name: "mydoc1", description: "[NULL]", version: "18", index_: -1
            document_mapping tenantid: 1, id: 301, processinstanceid: 800, documentid: 101, name: "mydoc2", description: "[NULL]", version: "1", index_: -1
            document_mapping tenantid: 1, id: 302, processinstanceid: 801, documentid: 102, name: "mydoc3", description: "[NULL]", version: "1", index_: -1
            document_mapping tenantid: 1, id: 303, processinstanceid: 802, documentid: 201, name: "mydoc4", description: "[NULL]", version: "1", index_: -1
            document_mapping tenantid: 1, id: 304, processinstanceid: 803, documentid: 304, name: "mydoc5", description: "[NULL]", version: "1", index_: -1



            arch_document_mapping tenantid: 1, id: 1300, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892314, documentid: 99, description: "[NULL]", version: "1", index_: -1
            arch_document_mapping tenantid: 1, id: 1301, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892101, documentid: 305, description: "[NULL]", version: "2", index_: -1
            arch_document_mapping tenantid: 1, id: 1302, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892102, documentid: 306, description: "[NULL]", version: "3", index_: -1
            arch_document_mapping tenantid: 1, id: 1303, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892103, documentid: 307, description: "[NULL]", version: "4", index_: -1
            arch_document_mapping tenantid: 1, id: 1304, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892104, documentid: 308, description: "[NULL]", version: "5", index_: -1
            arch_document_mapping tenantid: 1, id: 1305, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892105, documentid: 309, description: "[NULL]", version: "6", index_: -1
            arch_document_mapping tenantid: 1, id: 1306, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892106, documentid: 310, description: "[NULL]", version: "7", index_: -1
            arch_document_mapping tenantid: 1, id: 1307, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892107, documentid: 311, description: "[NULL]", version: "8", index_: -1
            arch_document_mapping tenantid: 1, id: 1308, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892108, documentid: 312, description: "[NULL]", version: "9", index_: -1
            arch_document_mapping tenantid: 1, id: 1309, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892109, documentid: 313, description: "[NULL]", version: "10", index_: -1
            arch_document_mapping tenantid: 1, id: 1310, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892110, documentid: 314, description: "[NULL]", version: "11", index_: -1
            arch_document_mapping tenantid: 1, id: 1311, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892111, documentid: 315, description: "[NULL]", version: "12", index_: -1
            arch_document_mapping tenantid: 1, id: 1312, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892112, documentid: 316, description: "[NULL]", version: "13", index_: -1
            arch_document_mapping tenantid: 1, id: 1313, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892113, documentid: 317, description: "[NULL]", version: "14", index_: -1
            arch_document_mapping tenantid: 1, id: 1314, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892114, documentid: 318, description: "[NULL]", version: "15", index_: -1
            arch_document_mapping tenantid: 1, id: 1315, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892115, documentid: 319, description: "[NULL]", version: "16", index_: -1
            arch_document_mapping tenantid: 1, id: 1316, sourceobjectid: 300, processinstanceid: 800, name: "oldmydoc1", archivedate: 4567892116, documentid: 320, description: "[NULL]", version: "17", index_: -1
            arch_document_mapping tenantid: 1, id: 1317, sourceobjectid: 299, processinstanceid: 804, name: "archedDoc", archivedate: 4567892315, documentid: 321, description: "[NULL]", version: "1", index_: -1

        }, updatedDocuments
    }
}
