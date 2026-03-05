/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
 **/
package org.bonitasoft.update.version.to11_0_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import spock.lang.Shared
import spock.lang.Specification

import static org.junit.jupiter.api.Assumptions.assumeTrue

class AddTemporaryContentLargeObjectCleanupTriggerPostgresIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddTemporaryContentLargeObjectCleanupTriggerPostgres updateStep =
    new AddTemporaryContentLargeObjectCleanupTriggerPostgres()

    def setup() {
        // Skip if not PostgreSQL (test is PG-specific)
        assumeTrue(updateContext.dbVendor == UpdateStep.DBVendor.POSTGRES, "Only PostgreSQL is supported for this test")

        dropTrigger()

        updateContext.setVersion("11.0.0")

        // Ensure temporary_content exists (snapshot may not create it)
        if (!updateContext.databaseHelper.hasTable("temporary_content")) {
            updateContext.sql.execute("""
            CREATE TABLE temporary_content (
                id BIGINT,
                content oid
            )
        """)
        }

        // Ensure trigger/function do not exist before running the step (idempotent test baseline)
        updateContext.sql.execute("""
            DROP TRIGGER IF EXISTS trg_temporary_content_lo_cleanup ON temporary_content;
            DROP FUNCTION IF EXISTS temporary_content_lo_cleanup();
        """)
    }

    def cleanup() {
        dropTrigger()
    }

    private void dropTrigger() {
        // Drop trigger/function first (table drop might fail if trigger exists in some PG setups)
        try {
            updateContext.sql.execute("""
                DROP TRIGGER IF EXISTS trg_temporary_content_lo_cleanup ON temporary_content;
                DROP FUNCTION IF EXISTS temporary_content_lo_cleanup();
            """)
        } catch (ignored) {
            // ignore: some schemas may not exist yet
        }
    }

    def "should create function and trigger for temporary_content LO cleanup"() {
        when:
        updateStep.execute(updateContext)

        then: "function exists"
        functionExists("temporary_content_lo_cleanup")

        and: "trigger exists on temporary_content"
        triggerExistsOnTable("trg_temporary_content_lo_cleanup", "temporary_content")
    }

    def "should delete large object when row is deleted from temporary_content"() {
        given: "trigger is created"
        updateStep.execute(updateContext)

        and: "a large object is created and inserted into temporary_content"
        def loOid = updateContext.sql.firstRow("SELECT lo_create(0) AS oid").oid
        assert loOid != null : "Failed to create large object"
        updateContext.sql.execute("INSERT INTO temporary_content (id, content) VALUES (1, :oid)", [oid: loOid])

        and: "the large object exists before deletion"
        def rowsBefore = updateContext.sql.rows("SELECT 1 FROM pg_largeobject_metadata WHERE oid = :oid", [oid: loOid])
        assert !rowsBefore.isEmpty() : "Large object should exist before deletion"

        when: "the row is deleted"
        updateContext.sql.execute("DELETE FROM temporary_content WHERE id = 1")

        then: "the large object no longer exists"
        def rows = updateContext.sql.rows("SELECT 1 FROM pg_largeobject_metadata WHERE oid = :oid", [oid: loOid])
        rows.isEmpty()
    }

    def "should be idempotent - running the step twice does not fail"() {
        when:
        updateStep.execute(updateContext)
        updateStep.execute(updateContext)

        then: "function and trigger still exist"
        functionExists("temporary_content_lo_cleanup")
        triggerExistsOnTable("trg_temporary_content_lo_cleanup", "temporary_content")

        and: "no exception is thrown"
        noExceptionThrown()
    }

    def "should handle NULL content gracefully"() {
        given: "trigger is created"
        updateStep.execute(updateContext)

        and: "a row with NULL content"
        updateContext.sql.execute("INSERT INTO temporary_content (id, content) VALUES (99, NULL)")

        when: "the row is deleted"
        updateContext.sql.execute("DELETE FROM temporary_content WHERE id = 99")

        then: "no exception is thrown"
        noExceptionThrown()
    }

    def "should skip when temporary_content table does not exist"() {
        given: "temporary_content table is dropped"
        updateContext.sql.execute("DROP TRIGGER IF EXISTS trg_temporary_content_lo_cleanup ON temporary_content")
        updateContext.sql.execute("DROP TABLE IF EXISTS temporary_content")

        when:
        updateStep.execute(updateContext)

        then: "no exception is thrown"
        noExceptionThrown()

        and: "no function is created"
        !functionExists("temporary_content_lo_cleanup")
    }

    private boolean functionExists(String functionName) {
        def rows = updateContext.sql.rows("""
            SELECT 1
              FROM pg_proc
             WHERE proname = :fn
        """, [fn: functionName])
        return !rows.isEmpty()
    }

    private boolean triggerExistsOnTable(String triggerName, String tableName) {
        def rows = updateContext.sql.rows("""
            SELECT 1
              FROM pg_trigger t
              JOIN pg_class c ON c.oid = t.tgrelid
             WHERE t.tgname = :tg
               AND c.relname = :tbl
               AND NOT t.tgisinternal
        """, [tg: triggerName, tbl: tableName])
        return !rows.isEmpty()
    }
}
