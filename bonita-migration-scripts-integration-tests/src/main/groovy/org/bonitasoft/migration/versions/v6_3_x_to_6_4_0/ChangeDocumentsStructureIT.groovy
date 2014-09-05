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
import groovy.xml.StreamingMarkupBuilder
import org.bonitasoft.migration.CustomAssertion
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.CreateApplicationTables
import org.dbunit.JdbcDatabaseTester
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSet

import static org.assertj.core.api.Assertions.assertThat

/**
 * @author Baptiste Mesta
 *
 */
class ChangeDocumentsStructureIT extends GroovyTestCase {
    final static String DBVENDOR
    final static CREATE_TABLES

    static{
        DBVENDOR = System.getProperty("db.vendor");
        CREATE_TABLES = ChangeDocumentsStructureIT.class.getClassLoader().getResource("sql/v6_4_0/${DBVENDOR}-documents.sql");
    }


    static{
        DBVENDOR = System.getProperty("db.vendor");
    }

    def checkSql =  [
        "mysql" : { tableName -> """
            SELECT * FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = $tableName
        """ },
        "oracle" : { tableName -> """
            SELECT *
            FROM all_tables
            WHERE TABLE_NAME = $tableName
        """ },
        "postgres" : { tableName ->  """
                SELECT *
                FROM information_schema.tables
                WHERE table_name = $tableName
            """ },
        "sqlserver" : { tableName ->  """
                SELECT *
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_NAME = $tableName
            """ }
    ]

    Sql sql
    JdbcDatabaseTester tester

    def dataSet(data) {
       new ReplacementDataSet(new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind{ dataset data }.toString())),["[NULL]":null],null)
    }

    @Override
    void setUp() {
        String driverClass =  System.getProperty("jdbc.driverClass")

        def config = [System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password")]
        sql = Sql.newInstance(*config, driverClass);
        tester = new JdbcDatabaseTester(driverClass, *config)

        CREATE_TABLES.text.split("@@").each({ stmt ->
            println "executing stmt $stmt for ${DBVENDOR}"
            sql.execute(stmt)
        })

        println ("setUp: populating table")

        tester.dataSet = dataSet {
            tenant id: 1

            //sequences
            sequence tenantid:1, id:10090, nextid:301

            document_content tenantid: 1,id:99,documentId: '-4561234568646477', content: "the old content1".getBytes()
            document_content tenantid: 1,id:100,documentId: '-4561234568646452', content: "the content1".getBytes()
            document_content tenantid: 1,id:101,documentId: '-4561485668646452', content: "the content2".getBytes()
            document_content tenantid: 1,id:102,documentId: '4561237768646452', content: "the content3".getBytes()
            document_content tenantid: 1,id:201,documentId: '6861234568646455', content: "the content4".getBytes()

            document_mapping tenantid: 1,id:300,processinstanceid:800,documentName:"mydoc1",documentAuthor:900,documentCreationDate:4567890003,documentHasContent:true,documentContentFileName:"file1.txt",documentContentMimeType:"plain/text",contentStorageId:'-4561234568646452',documentURL:"[NULL]"
            document_mapping tenantid: 1,id:301,processinstanceid:800,documentName:"mydoc2",documentAuthor:900,documentCreationDate:4567892316,documentHasContent:true,documentContentFileName:"file2.txt",documentContentMimeType:"application/octet-stream",contentStorageId:'-4561485668646452',documentURL:"[NULL]"
            document_mapping tenantid: 1,id:302,processinstanceid:801,documentName:"mydoc3",documentAuthor:901,documentCreationDate:4567892317,documentHasContent:true,documentContentFileName:"file3.txt",documentContentMimeType:"plain/text",contentStorageId:'4561237768646452',documentURL:"[NULL]"
            document_mapping tenantid: 1,id:303,processinstanceid:802,documentName:"mydoc4",documentAuthor:902,documentCreationDate:4567892318,documentHasContent:true,documentContentFileName:"file4.txt",documentContentMimeType:"plain/text",contentStorageId:'6861234568646455',documentURL:"[NULL]"
            document_mapping tenantid: 1,id:304,processinstanceid:803,documentName:"mydoc5",documentAuthor:902,documentCreationDate:4567892318,documentHasContent:false,documentContentFileName:"file5.txt",documentContentMimeType:"plain/text",contentStorageId:"[NULL]",documentURL:"http://myurl.com/thefile.txt"

            arch_document_mapping tenantid: 1,id:1300,sourceobjectid:300,processinstanceid:800,documentName:"oldmydoc1",documentAuthor:901,documentCreationDate:4567890001,documentHasContent:true,documentContentFileName:"oldfile1.txt",documentContentMimeType:"plain/text",contentStorageId:'-4561234568646477',documentURL:"[NULL]",archivedate:4567892314
            arch_document_mapping tenantid: 1,id:1301,sourceobjectid:300,processinstanceid:800,documentName:"oldmydoc1",documentAuthor:901,documentCreationDate:4567890002,documentHasContent:false,documentContentFileName:"oldfile2.txt",documentContentMimeType:"plain/text",contentStorageId:'[NULL]',documentURL:"http//myurl.com/myfile2.txt",archivedate:4567892314
            arch_document_mapping tenantid: 1,id:1302,sourceobjectid:299,processinstanceid:804,documentName:"archedDoc",documentAuthor:901,documentCreationDate:4567892325,documentHasContent:false,documentContentFileName:"archedDoc.txt",documentContentMimeType:"plain/text",contentStorageId:'[NULL]',documentURL:"http//myurl.com/myfile.txt",archivedate:4567892315

        }
        tester.onSetup();
    }

    @Override
    void tearDown() {
        tester.onTearDown();
        sql.execute("DROP TABLE document_mapping")
        sql.execute("DROP TABLE arch_document_mapping")
        sql.execute("DROP TABLE document")
        sql.execute("DROP TABLE tenant")
        sql.execute("DROP TABLE sequence")
    }

    void test_migration_documents_structure() {
        //def feature = new File("build/dist/versions/6.3.3-6.4.0/Database/003_living_applications")
        new ChangeDocumentsStructure().migrate(sql, DBVENDOR);


        def updatedDocuments = tester.connection.createDataSet("document_mapping","arch_document_mapping", "document", "sequence");

        //TODO version + document content without mapping?
        CustomAssertion.assertEquals dataSet {

            sequence tenantid:1, id:10090, nextid:304


            document tenantid: 1,id:99,author:901,creationdate:4567890001,hascontent:true,filename:"oldfile1.txt",mimetype:"plain/text", content: "the old content1".getBytes(), url:"[NULL]"
            document tenantid: 1,id:100,author:900,creationdate:4567890003,hascontent:true,filename:"file1.txt",mimetype:"plain/text", content: "the content1".getBytes(), url:"[NULL]"
            document tenantid: 1,id:101,author:900,creationdate:4567892316,hascontent:true,filename:"file2.txt",mimetype:"application/octet-stream", content: "the content2".getBytes(), url:"[NULL]"
            document tenantid: 1,id:102,author:901,creationdate:4567892317,hascontent:true,filename:"file3.txt",mimetype:"plain/text", content: "the content3".getBytes(), url:"[NULL]"
            document tenantid: 1,id:201,author:902,creationdate:4567892318,hascontent:true,filename:"file4.txt",mimetype:"plain/text", content: "the content4".getBytes(), url:"[NULL]"
            document tenantid: 1,id:301,author:902,creationdate:4567892318,hascontent:false,filename:"file5.txt",mimetype:"plain/text", content: "[NULL]", url: 'http://myurl.com/thefile.txt'
            document tenantid: 1,id:302,author:901,creationdate:4567890002,hascontent:false,filename:"oldfile2.txt",mimetype:"plain/text", content: "[NULL]", url: 'http//myurl.com/myfile2.txt'
            document tenantid: 1,id:303,author:901,creationdate:4567892325,hascontent:false,filename:"archedDoc.txt",mimetype:"plain/text", content: "[NULL]", url: 'http//myurl.com/myfile.txt'

            document_mapping tenantid: 1,id:300,processinstanceid:800,documentid:100,name:"mydoc1",description:"[NULL]",version:"3"
            document_mapping tenantid: 1,id:301,processinstanceid:800,documentid:101,name:"mydoc2",description:"[NULL]",version:"1"
            document_mapping tenantid: 1,id:302,processinstanceid:801,documentid:102,name:"mydoc3",description:"[NULL]",version:"1"
            document_mapping tenantid: 1,id:303,processinstanceid:802,documentid:201,name:"mydoc4",description:"[NULL]",version:"1"
            document_mapping tenantid: 1,id:304,processinstanceid:803,documentid:301,name:"mydoc5",description:"[NULL]",version:"1"

            arch_document_mapping tenantid: 1,id:1300,sourceobjectid:300,processinstanceid:800,documentid:99,name:"oldmydoc1",description:"[NULL]",version:"1",archivedate:4567892314
            arch_document_mapping tenantid: 1,id:1301,sourceobjectid:300,processinstanceid:800,documentid:302,name:"oldmydoc1",description:"[NULL]",version:"2",archivedate:4567892314
            arch_document_mapping tenantid: 1,id:1302,sourceobjectid:299,processinstanceid:804,documentid:303,name:"archedDoc",description:"[NULL]",version:"1",archivedate:4567892315


        }, updatedDocuments
    }
}
