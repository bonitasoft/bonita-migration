package org.bonitasoft.migration.version.to7_2_1

import groovy.sql.GroovyRowResult
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 *
 * parameters where in bonita-home/engine-server/work/tenants/<tenantId>/processes/<process_id>/parameters.properties
 *
 * @author Baptiste Mesta
 */
class ContractInputSequence extends MigrationStep {


    public static
    final String SQL_CHECK_SEQUENCES = "SELECT tenantid, id, nextid FROM sequence WHERE id in (10220,20220, 10210 ,20210 ) ORDER BY  tenantid, id "

    @Override
    def execute(MigrationContext context) {
        checkSequenceValues(context)

        context.sql.rows("""
                    SELECT
                        s.TENANTID,
                        10210 AS id,
                        MAX( s.NEXTID ) + 1 AS newValue
                    FROM sequence s
                    WHERE s.id IN(
                        10210,
                        10220
                    )
                    GROUP BY s.TENANTID
                """).each {
            context.sql.execute("UPDATE sequence set nextid= ? WHERE tenantid= ? and id = ?", it.newValue, it.tenantId, it.id)
        }

        context.sql.rows("""
                    SELECT
                        s.TENANTID,
                        20210 AS id,
                        MAX( s.NEXTID ) + 1 AS newValue
                    FROM sequence s
                    WHERE s.id IN(
                        20210,
                        20220
                    )
                    GROUP BY s.TENANTID
                """).each {
            context.sql.execute("UPDATE sequence set nextid= ? WHERE tenantid= ? and id = ?", it.newValue, it.tenantId, it.id)
        }

        context.sql.execute("DELETE FROM sequence WHERE id IN (10220,20220)")

        checkSequenceValues(context)
    }

    private List<GroovyRowResult> checkSequenceValues(MigrationContext context) {
        context.sql.rows(SQL_CHECK_SEQUENCES).each {
            context.logger.info "sequence value before migration: $it"
        }
    }

    @Override
    String getDescription() {
        return "Put all contract inputs on the same sequence"
    }

}
