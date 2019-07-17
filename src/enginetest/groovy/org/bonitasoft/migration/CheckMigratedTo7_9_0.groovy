package org.bonitasoft.migration

import groovy.sql.Sql
import org.bonitasoft.engine.BonitaDatabaseConfiguration
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.test.TestEngine
import org.bonitasoft.engine.test.TestEngineImpl
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.junit.BeforeClass
import org.junit.Rule
import spock.lang.Specification

class CheckMigratedTo7_9_0 extends Specification {
    private static instance = TestEngineImpl.getInstance()

    @BeforeClass
    static startEngine() {
        CheckerUtils.initializeEngineSystemProperties()
        instance.setBonitaDatabaseProperties(new BonitaDatabaseConfiguration(
                dbVendor: System.getProperty("db.vendor"),
                url: System.getProperty("db.url"),
                driverClassName: System.getProperty("db.driverClass"),
                user: System.getProperty("db.user"),
                password: System.getProperty("db.password")
        ))
        instance.dropOnStart = false
        instance.dropOnStop = false
        instance.start()
    }

    static stopEngine() {
        instance.stop()
    }

    def "verify we can login with the tenant admin"() {
        given:
        def client = new APIClient()

        when:
        client.login("install", "install")

        then:
        client.session != null
    }

    def "verify we can login with user 'john'"() {
        expect:
        TenantAPIAccessor.getLoginAPI().login("john", "bpm")
    }


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
