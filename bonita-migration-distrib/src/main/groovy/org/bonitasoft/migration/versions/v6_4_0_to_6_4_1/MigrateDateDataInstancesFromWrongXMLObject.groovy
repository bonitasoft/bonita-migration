/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/

package org.bonitasoft.migration.versions.v6_4_0_to_6_4_1

import groovy.sql.Sql

import org.apache.commons.io.IOUtils
import org.bonitasoft.migration.core.DatabaseMigrationStep

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.StaxDriver

/**
 * Emmanuel Duchastenier
 */
class MigrateDateDataInstancesFromWrongXMLObject extends DatabaseMigrationStep {

    MigrateDateDataInstancesFromWrongXMLObject(final Sql sql, final String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    def migrate() {
        migrateTable("data_instance", "SXMLObjectDataInstanceImpl", "SDateDataInstanceImpl");
        migrateTable("arch_data_instance", "SAXMLObjectDataInstanceImpl", "SADateDataInstanceImpl");
    }

    def migrateTable(String tableName, String formerDiscriminant, String newDiscriminant) {
        def row = sql.eachRow("SELECT tenantId, id, name, clobValue FROM "+tableName+" where DISCRIMINANT = '$formerDiscriminant' and classname='java.util.Date'"){ row ->
            def tenantId = row.tenantId
            def id = row.id
            def rowClobValue = row.clobValue
            def clobAsString = rowClobValue;
            // Special treatment of blobs in Oracle:
            println "data to be migrated : $row.id --> $row.name"
            if( rowClobValue != null && dbVendor == "oracle") {
                StringWriter w = new StringWriter();
                IOUtils.copy(rowClobValue.getCharacterStream(), w);
                clobAsString = w.toString();
            }
            if(clobAsString !=null){
                def newDate = new XmlParser().parseText(clobAsString)
                if( newDate.name().equals('date') || newDate.name().equals('null')) {
                    executeUpdate("UPDATE "+tableName+" set LONGVALUE=" + getDate(clobAsString) + ", CLOBVALUE=NULL, DISCRIMINANT='$newDiscriminant' WHERE tenantId=$tenantId AND id=$id")
                }
            }else{
                executeUpdate("UPDATE "+tableName+" set LONGVALUE=NULL, CLOBVALUE=NULL, DISCRIMINANT='$newDiscriminant' WHERE tenantId=$tenantId AND id=$id")
            }
        }
    }

    def getDate(String xmlDate) {
        def date = ((java.util.Date) new XStream(new StaxDriver()).fromXML(xmlDate))
        if(date == null) {
            return null
        } else {
            return date.getTime()
        }
    }
}
