package org.bonitasoft.migration.core.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import spock.lang.Specification

/**
 * Created by laurentleseigneur on 01/06/2017.
 */
class ConfigurationHelperTest extends Specification {

    Sql sql = Mock(Sql)
    Logger logger = Mock(Logger)
    DatabaseHelper databaseHelper = Mock(DatabaseHelper)

    ConfigurationHelper configurationHelper


    def setup() {
        configurationHelper = new ConfigurationHelper(sql: sql, logger: logger, databaseHelper: databaseHelper)
    }


    def "should insert configuration content"() {
        when:
        configurationHelper.insertConfigurationFile("fileName", 5L, "type", "content".bytes)

        then:
        1 * logger.debug(_)
        1 * sql.executeInsert("INSERT INTO configuration(tenant_id,content_type,resource_name,resource_content) VALUES (5, type, fileName, ${'content'.bytes})")

    }

    def "should update configuration content"() {
        when:
        configurationHelper.updateConfigurationFileContent("fileName", 5L, "type", "content".bytes)

        then:
        1 * logger.debug(_)
        1 * sql.execute("UPDATE configuration SET resource_content = ${'content'.bytes} WHERE tenant_id = 5 AND content_type = type AND resource_name = fileName")


    }

    def "should throw exception when configuration file does not exists"() {
        setup:
        def fileName = "phantom"
        GroovyRowResult count = Mock()
        sql.firstRow("SELECT count(*) FROM configuration WHERE resource_name = ${fileName}") >> count
        count.equals(0) >> true

        when:
        configurationHelper.appendToAllConfigurationFilesWithName(fileName, "add this")

        then:
        IllegalArgumentException argumentException = thrown()
        argumentException.message == "Configuration file ${fileName} does not exist in database."

    }

    def "should append configuration file content"() {
        setup:
        def captured = []
        def fileName = "fileName"
        GroovyRowResult count = Mock()
        count.equals(0) >> false
        databaseHelper.getBlobContentAsString(_) >> "content"
        sql.firstRow("SELECT count(*) FROM configuration WHERE resource_name = ${fileName}") >> count
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: "content".bytes],
                       [tenant_id: 5L, content_type: "type", resource_content: "content".bytes],
                       [tenant_id: 8L, content_type: "type", resource_content: "content".bytes]]
        sql.rows("SELECT tenant_id, content_type, resource_content FROM configuration WHERE resource_name = " +
                "${fileName} ") >> results


        when:
        configurationHelper.appendToAllConfigurationFilesWithName(fileName, "add this")

        then:
        3 * sql.execute({
            captured.add(it)
        })
        captured == ["UPDATE configuration SET resource_content = ${"content\nadd this".bytes} WHERE tenant_id = 0 " +
                             "AND content_type = template_type AND resource_name = fileName",
                     "UPDATE configuration SET resource_content = ${"content\nadd this".bytes} WHERE tenant_id = 5 " +
                             "AND content_type = type AND resource_name = fileName",
                     "UPDATE configuration SET resource_content = ${"content\nadd this".bytes} WHERE tenant_id = 8 " +
                             "AND content_type = type AND resource_name = fileName"]

    }

    def "should throw exception when updating key in non existing file"() {
        setup:
        def fileName = "phantom"
        databaseHelper.getBlobContentAsString(_) >> "key=value"
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> []

        when:
        configurationHelper.updateKeyInAllPropertyFiles(fileName, "propertyKey", "newValue", "comment")

        then:
        IllegalArgumentException argumentException = thrown()
        argumentException.message == "configuration file ${fileName} not found in database."

    }

    def "should add entry in property file"() {
        setup:
        def captured = []
        def fileName = "existingFile"
        def existingContent = "# existing comment\nkey=value"
        databaseHelper.getBlobContentAsString(_) >> existingContent
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: existingContent.bytes],
                       [tenant_id: 5L, content_type: "type", resource_content: existingContent.bytes],
                       [tenant_id: 8L, content_type: "type", resource_content: existingContent.bytes]]
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> results

        when:
        configurationHelper.updateKeyInAllPropertyFiles(fileName, "newKey", "newValue", "new comment")

        then:
        3 * sql.execute({
            captured.add(it)
        })
        def expectedContent = "${existingContent}\n# new comment\nnewKey=newValue"
        captured == ["UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 0 " +
                             "AND content_type = template_type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 5 " +
                             "AND content_type = type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 8 " +
                             "AND content_type = type AND resource_name = ${fileName}"]

    }

    def "should update property for existing key in property file"() {
        setup:
        def captured = []
        def fileName = "existingFile"
        def existingContent = "# existing comment\nkey=value\nkey2=value2"
        databaseHelper.getBlobContentAsString(_) >> existingContent
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: existingContent.bytes],
                       [tenant_id: 5L, content_type: "type", resource_content: existingContent.bytes],
                       [tenant_id: 8L, content_type: "type", resource_content: existingContent.bytes]]
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> results

        when:
        configurationHelper.updateKeyInAllPropertyFiles(fileName, "key", "newValue", "new comment")

        then:
        3 * sql.execute({
            captured.add(it)
        })
        def expectedContent = "# existing comment\n# new comment\nkey=newValue\nkey2=value2"
        captured == ["UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 0 " +
                             "AND content_type = template_type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 5 " +
                             "AND content_type = type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 8 " +
                             "AND content_type = type AND resource_name = ${fileName}"]

    }

    def "should not update up to date property in property file"() {
        setup:
        def fileName = "existingFile"
        def existingContent = "#mycomment\nkey=value\nkey2=value2"
        databaseHelper.getBlobContentAsString(_) >> existingContent
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: existingContent.bytes],
                       [tenant_id: 5L, content_type: "type", resource_content: existingContent.bytes],
                       [tenant_id: 8L, content_type: "type", resource_content: existingContent.bytes]]
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> results

        when:
        configurationHelper.updateKeyInAllPropertyFiles(fileName, "key", "value", "comment")

        then:
        0 * sql.execute(_)
        3 * logger.info("property file is already up to date ")
    }

    def "updateKeyInAllPropertyFiles should not add comment if comment is null"() {
        setup:
        def captured = []
        def fileName = "existingFile"
        def existingContent = "#mycomment\nkey2=value2"
        databaseHelper.getBlobContentAsString(_) >> existingContent
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: existingContent.bytes]]
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> results

        when:
        configurationHelper.updateKeyInAllPropertyFiles(fileName, "key", "value", null)

        then:
        1 * sql.execute({ captured.add(it) })
        def expectedContent = "${existingContent}\nkey=value"
        captured == ["UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 0 AND content_type = template_type AND resource_name = ${fileName}"]
    }

    def "should throw exception when adding key in non existing file"() {
        setup:
        def fileName = "phantom"
        databaseHelper.getBlobContentAsString(_) >> "key=value"
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> []

        when:
        configurationHelper.appendToAllConfigurationFilesIfPropertyIsMissing(fileName, "propertyKey", "newValue")

        then:
        IllegalArgumentException argumentException = thrown()
        argumentException.message == "configuration file ${fileName} not found in database."

    }

    def "should not change up to date property in property file"() {
        setup:
        def captured = []
        def fileName = "existingFile"
        def existingContent = "#mycomment\nkey=value\nkey2=value2"
        databaseHelper.getBlobContentAsString(_) >> existingContent
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: existingContent.bytes],
                       [tenant_id: 5L, content_type: "type", resource_content: existingContent.bytes],
                       [tenant_id: 8L, content_type: "type", resource_content: existingContent.bytes]]
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> results

        when:
        configurationHelper.appendToAllConfigurationFilesIfPropertyIsMissing(fileName, "key", "value")

        then:
        0 * sql.execute(_)
        3 * logger.info({ captured.add(it) })
        def message = "configuration file already up to date | tenant id: %3d | type: %-25s | file name:" +
                " %s"
        captured == [String.format(message, 0L, "template_type", fileName),
                     String.format(message, 5L, "type", fileName),
                     String.format(message, 8L, "type", fileName)
        ]


    }


    def "should append new entry in property file"() {
        setup:
        def captured = []
        def fileName = "existingFile"
        def existingContent = "#mycomment\nkey=value\nkey2=value2"
        databaseHelper.getBlobContentAsString(_) >> existingContent
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: existingContent.bytes],
                       [tenant_id: 5L, content_type: "type", resource_content: existingContent.bytes],
                       [tenant_id: 8L, content_type: "type", resource_content: existingContent.bytes]]
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> results

        when:
        configurationHelper.appendToAllConfigurationFilesIfPropertyIsMissing(fileName, "newKey", "newValue")

        then:
        3 * sql.execute({ captured.add(it) })
        def expectedContent = "${existingContent}\nnewKey=newValue"
        captured == ["UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 0 " +
                             "AND content_type = template_type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 5 " +
                             "AND content_type = type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 8 " +
                             "AND content_type = type AND resource_name = ${fileName}"]

    }

    def "should update entry if permission value is missing in property file"() {
        setup:
        def captured = []
        def fileName = "existingFile"
        def existingContent = """#comment key0=[perm0]
wrong line
key1=[perm1]
key2=[perm1,permToAdd,perm2]
key3=[perm1,perm2
unknown_key=[perm1]
"""

        databaseHelper.getBlobContentAsString(_) >> existingContent
        def results = [[tenant_id: 0L, content_type: "template_type", resource_content: existingContent.bytes],
                       [tenant_id: 5L, content_type: "type", resource_content: existingContent.bytes],
                       [tenant_id: 8L, content_type: "type", resource_content: existingContent.bytes]]
        sql.rows("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${fileName}       
                ORDER BY content_type, tenant_id 
                """) >> results

        when:
        configurationHelper.updateAllConfigurationFilesIfPermissionValueIsMissing(fileName, ['key1', 'key2', 'key3'], "permToAdd")

        then:
        3 * sql.execute({ captured.add(it) })
        def expectedContent = """#comment key0=[perm0]
wrong line
key1=[perm1, permToAdd]
key2=[perm1,permToAdd,perm2]
key3=[perm1,perm2, permToAdd]
unknown_key=[perm1]"""

        captured == ["UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 0 " +
                             "AND content_type = template_type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 5 " +
                             "AND content_type = type AND resource_name = ${fileName}",
                     "UPDATE configuration SET resource_content = ${expectedContent.bytes} WHERE tenant_id = 8 " +
                             "AND content_type = type AND resource_name = ${fileName}"]

    }

}
