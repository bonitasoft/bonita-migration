package org.bonitasoft.migration.version.to7_10_0

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Danila Mazour
 */
class MigrateTo7_10_0Test extends Specification {
    @Unroll
    def "should migration to 7.10.0 include step '#stepName'"(def stepName) {
        given:
        def migrateTo = new MigrateTo7_10_0()

        expect:
        def steps = migrateTo.migrationSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "ReplaceIndexMysqlUTF8MB4Compatibility"
        ]

    }
}
