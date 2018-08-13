package org.bonitasoft.migration.version.to7_7_4

import org.bonitasoft.migration.version.to7_7_3.MigrateTo7_7_3
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Danila Mazour
 */
class MigrateTo7_7_4Test extends Specification {

    @Unroll
    def "should migration to 7.7.4 include step '#stepName'"(def stepName) {
        given:
        def migrateTo = new MigrateTo7_7_3()

        expect:
        def steps = migrateTo.migrationSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
                "ChangeContractInputSerializationOnMysqlToCorrectFormatIfNotDone"
        ]

    }

}
