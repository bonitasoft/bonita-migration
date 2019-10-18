/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
 **/
package org.bonitasoft.migration.version.to7_9_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import spock.lang.Specification
import spock.lang.Unroll

class MigrateTo7_9_0Test extends Specification {

    Sql sql = Mock(Sql)
    MigrationContext migrationContext = new MigrationContext([sql: ThreadLocal.<Sql> withInitial({ sql })])

    @Unroll
    def "should migration to 7.9.0 include step '#stepName'"(def stepName) {
        given:
        def migrateTo = new MigrateTo7_9_0()

        expect:
        def steps = migrateTo.migrationSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "RemoveCleanInvalidSessionsJob"
                , "ChangeProfileEntryForOrganizationImport"
                , "UpdateConnectorDefinitionsForJava11"
                , "AddMessageAndWaitingEventDBIndices"
                , "RenameCommandSystemColumn"
                , "RenameBonitaDefaultTheme"
                , "AddDeprecatedToLivingApplicationLayout"
                , "AddCreationDateOnMessageInstance"
                , "AddIndexLogicalGroupOnFlownodeInstance"
                , "AddIndexActivityKindOnFlownodeInstance"
                , "AddIndexOnJobParams"
        ]

    }

    @Unroll
    def "should have blocking message (#hasBlockingMessage) when oracle version is #oracleVersion "() {
        given:
        def migrateTo = new MigrateTo7_9_0()
        migrationContext.dbVendor = MigrationStep.DBVendor.ORACLE
        sql.rows("SELECT * FROM PRODUCT_COMPONENT_VERSION") >> [[PRODUCT: "Oracle Database", VERSION: oracleVersion]]
        when:
        def messages = migrateTo.getPreMigrationBlockingMessages(migrationContext)
        then:
        (messages.size() == 0) == !hasBlockingMessage
        where:
        oracleVersion || hasBlockingMessage
        "12.1.0.1"    || true
        "11.1.1.1"    || true
        "12.2.0.1"    || false
        "12.3.0.1"    || false
        "14.1.0.1"    || false
    }

}
