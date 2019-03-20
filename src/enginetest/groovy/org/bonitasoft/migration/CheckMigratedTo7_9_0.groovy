package org.bonitasoft.migration

import groovy.sql.Sql
import org.junit.Rule
import spock.lang.Specification

class CheckMigratedTo7_9_0 extends Specification {
    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    def "should not have the CleanInvalidSessionsJob anymore"() {
        given:
        def dburl = System.getProperty("db.url")
        def dbDriverClassName = System.getProperty("db.driverClass")
        def dbUser = System.getProperty("db.user")
        def dbPassword = System.getProperty("db.password")
        def sql = Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)
        expect:
        sql.firstRow("select count(*) from QRTZ_CRON_TRIGGERS where TRIGGER_NAME in (select t.TRIGGER_NAME from QRTZ_TRIGGERS t where t.JOB_NAME = 'CleanInvalidSessions')")[0] == 0
        sql.firstRow("select count(*) from QRTZ_FIRED_TRIGGERS where TRIGGER_NAME in (select t.TRIGGER_NAME from QRTZ_TRIGGERS t where t.JOB_NAME = 'CleanInvalidSessions')")[0] == 0
        sql.firstRow("select count(*) from QRTZ_TRIGGERS t where t.JOB_NAME = 'CleanInvalidSessions'")[0] == 0
        sql.firstRow("select count(*) from QRTZ_JOB_DETAILS t where t.JOB_NAME = 'CleanInvalidSessions'")[0] == 0
    }

}
