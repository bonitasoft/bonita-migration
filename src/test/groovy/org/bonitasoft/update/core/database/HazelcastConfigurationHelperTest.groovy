/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.update.core.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.bonitasoft.update.core.Logger
import spock.lang.Specification

class HazelcastConfigurationHelperTest extends Specification {

    def "should add entity to cache configuration, and ensure 'addEntityToCache' method is reentrant"() {
        given:
        def hazelcastConfigurationHelper = new HazelcastConfigurationHelper()
        def sql = Mock(Sql)

        // mock the first call to return an empty configuration:
        sql.firstRow(_ as String) >>> [
            new GroovyRowResult([resource_content: """<hazelcast>
</hazelcast>""".bytes]),
            // mock the second call to return the configuration with the entity already added:
            new GroovyRowResult([resource_content: """<hazelcast>
    <cache name="org.bonitasoft.engine.core.process.instance.model.SBPMFailure">
        <eviction eviction-policy="LRU" size="10000" max-size-policy="ENTRY_COUNT"/>
        <expiry-policy-factory>
            <timed-expiry-policy-factory expiry-policy-type="TOUCHED" duration-amount="12" time-unit="HOURS"/>
        </expiry-policy-factory>
    </cache>

</hazelcast>""".bytes])
        ]

        hazelcastConfigurationHelper.logger = Mock(Logger)
        hazelcastConfigurationHelper.sql = sql
        hazelcastConfigurationHelper.databaseHelper = new DatabaseHelper()

        when:
        hazelcastConfigurationHelper.addEntityToCache('org.bonitasoft.engine.core.process.instance.model.SBPMFailure')
        // make sure the configuration is not added twice, aka the method is reentrant:
        def configuration = hazelcastConfigurationHelper.addEntityToCache('org.bonitasoft.engine.core.process.instance.model.SBPMFailure')

        then:
        configuration.content.endsWith('''|
            |    <cache name="org.bonitasoft.engine.core.process.instance.model.SBPMFailure">
            |        <eviction eviction-policy="LRU" size="10000" max-size-policy="ENTRY_COUNT"/>
            |        <expiry-policy-factory>
            |            <timed-expiry-policy-factory expiry-policy-type="TOUCHED" duration-amount="12" time-unit="HOURS"/>
            |        </expiry-policy-factory>
            |    </cache>
            |
            |</hazelcast>'''.stripMargin().normalize())

        // check that the entity is not added twice:
        (configuration.content =~ """<cache name="org.bonitasoft.engine.core.process.instance.model.SBPMFailure">""").findAll().size() == 1
    }

    def "should update XSD schema"() {
        given:
        def hazelcastConfigurationHelper = new HazelcastConfigurationHelper()
        def sql = Mock(Sql)
        sql.firstRow(_ as String) >> new GroovyRowResult([resource_content: readExistingConfigurationFile()])

        hazelcastConfigurationHelper.logger = Mock(Logger)
        hazelcastConfigurationHelper.sql = sql
        hazelcastConfigurationHelper.databaseHelper = new DatabaseHelper()

        when:
        def configuration = hazelcastConfigurationHelper.updateXMLSchema('http://www.hazelcast.com/schema/config/hazelcast-config-5.4.xsd')

        then:
        assert configuration.content.contains('''<hazelcast xmlns="http://www.hazelcast.com/schema/config"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.hazelcast.com/schema/config
           http://www.hazelcast.com/schema/config/hazelcast-config-5.4.xsd">'''.stripMargin().normalize())
        assert !configuration.content.contains('http://www.hazelcast.com/schema/config/hazelcast-config-5.3.xsd')
    }

    byte[] readExistingConfigurationFile() {
        return HazelcastConfigurationHelperTest.class.getResourceAsStream('/hazelcast.xml').bytes
    }

    def "should remove entity cache configuration"() {
        given:
        def hazelcastConfigurationHelper = new HazelcastConfigurationHelper()
        def sql = Mock(Sql)
        sql.firstRow(_ as String) >> new GroovyRowResult([resource_content: readExistingConfigurationFile()])

        hazelcastConfigurationHelper.logger = Mock(Logger)
        hazelcastConfigurationHelper.sql = sql
        hazelcastConfigurationHelper.databaseHelper = new DatabaseHelper()

        when:
        def configuration = hazelcastConfigurationHelper.removeCacheConfiguration('org.bonitasoft.engine.queriablelogger.model.SQueriableLogParameter')

        then:
        assert !configuration.content.contains('org.bonitasoft.engine.queriablelogger.model.SQueriableLogParameter')
    }
}
