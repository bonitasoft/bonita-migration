/*
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class IncreaseVersionFieldIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.4.0")
        dbUnitHelper.createTables("7_4_0/${subfolderSqlScriptResource()}", "size")
    }

    protected String subfolderSqlScriptResource() {
        'version'
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["document_mapping", "arch_document_mapping", "connector_instance", "arch_connector_instance"] as String[])
    }

    def "should insert version with 50 char"() {
        given:
        def version = "Римский император Константин I Великий по достоинс"

        when:
        new IncreaseVersionField().execute(migrationContext)

        and:
        insertData(version)

        then:
        dbUnitHelper.context.sql.rows("SELECT * FROM connector_instance").size() == 1
        dbUnitHelper.context.sql.rows("SELECT * FROM arch_connector_instance").size() == 1
        dbUnitHelper.context.sql.rows("SELECT * FROM document_mapping").size() == 1
        dbUnitHelper.context.sql.rows("SELECT * FROM arch_document_mapping").size() == 1

    }

    def "should not insert null version in document mapping"() {

        when:
        new IncreaseVersionField().execute(migrationContext)

        and:
        insertDocumentMapping(null)

        then:
        thrown(Exception)
        dbUnitHelper.context.sql.rows("SELECT * FROM document_mapping").size() == 0

    }

    def "should not insert null version in arch document mapping"() {

        when:
        new IncreaseVersionField().execute(migrationContext)

        and:
        insertArchDocumentMapping(null)

        then:
        thrown(Exception)
        dbUnitHelper.context.sql.rows("SELECT * FROM arch_document_mapping").size() == 0

    }

    def "should not insert null version in arch connector instance"() {

        when:
        new IncreaseVersionField().execute(migrationContext)

        and:
        insertArchConnectorInstance(null)

        then:
        thrown(Exception)
        dbUnitHelper.context.sql.rows("SELECT * FROM arch_connector_instance").size() == 0

    }

    def "should not insert null version in connector instance"() {

        when:
        new IncreaseVersionField().execute(migrationContext)

        and:
        insertConnectorInstance(null)

        then:
        thrown(Exception)
        dbUnitHelper.context.sql.rows("SELECT * FROM connector_instance").size() == 0

    }


    private boolean insertData(version) {
        insertDocumentMapping(version)
        insertArchDocumentMapping(version)
        insertArchConnectorInstance(version)
        insertConnectorInstance(version)
    }

    private boolean insertConnectorInstance(version) {
        dbUnitHelper.context.sql.execute("""
           INSERT INTO connector_instance
           (    tenantid,
                id,
                containerId,
                containerType,
                connectorId,
                version,
                name)
           VALUES (1,2,3,'type',4,${version},'name')""")
    }

    private boolean insertArchConnectorInstance(version) {
        dbUnitHelper.context.sql.execute("""
           INSERT INTO arch_connector_instance
           (    tenantid,
                id,
                containerId,
                containerType,
                connectorId,
                version,
                name,
                activationEvent,
                state,
                sourceObjectId,
                archiveDate)
            VALUES (1,2,3,'type',4,${version},'name','activationEvent','state',5,6)""")
    }

    private boolean insertArchDocumentMapping(version) {
        dbUnitHelper.context.sql.execute("""
            INSERT INTO arch_document_mapping
            (   tenantid,
                id,
                sourceObjectId,
                processinstanceid,
                documentid,
                name,
                description,
                version,
                index_,
                archiveDate)
            VALUES (1,2,3,4,5,'name','description',${version},6,7)""")
    }

    private boolean insertDocumentMapping(version) {
        dbUnitHelper.context.sql.execute("""
            INSERT INTO document_mapping
            (   tenantid,
                id,
                processinstanceid,
                documentid,
                name,
                description,
                version,
                index_)
            VALUES (1,2,3,4,'name','description',${version},5)""")
    }

}
