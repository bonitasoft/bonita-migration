package org.bonitasoft.migration.version.to7_8_0

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationUtil
import spock.lang.Shared
import spock.lang.Specification

import static org.bonitasoft.migration.version.to7_8_0.MigrateTo7_8_0.V6_FORMS_IN_ACTIVE_INSTANCES_OR_ENABLED_PROCESSES_PRESENT_MESSAGE

/**
 * @author Danila Mazour
 */
class MigrateTo7_8_0IT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    MigrateTo7_8_0 v7_8_0 = new MigrateTo7_8_0()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_8_0/pages_and_forms")
        migrationContext.sourceVersion = Version.valueOf("7.7.0")

    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page", "form_mapping", "page_mapping", "process_definition", "process_instance", "bar_resource"] as String[])
    }


    def "should return errors when active processes has v6 forms on a bonita in version 7.0.0"() {
        given:
        //mimic pre 7.1.0, remove 'target' column of form mapping and set source version to be before 7.1.0
        migrationContext.sourceVersion = Version.valueOf("7.0.2")
        migrationContext.databaseHelper.dropColumn("form_mapping", "target")
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 19, 'process/Pre710_process/1.0', 11, NULL, 'v6 url adapter', 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)""")
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 20, 'taskInstance/Pre710_process/1.0/Step1', 12, NULL, 'v6 url adapter', 'not used here', 0, 0)""")

        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 13, 4837119744348201769, 1, NULL, 1, 19, 0, 0)""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 14, 4837119744348201769, 1, NULL, 1, 20, 0, 0)""")

        migrationContext.sql.execute("""INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 1, 'Pre710_process', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'Pre710_process', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'Pre710_process', '', 1538483485209, NULL, NULL, 1, 3)
""")

        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        !blockingMessages.contains("* process/")
        !blockingMessages.contains("* taskInstance/")
        blockingMessages.contains("* Pre710_process/1.0 (Instantiation form)")
        blockingMessages.findAll { it == "* Pre710_process/1.0/Step1 (Task form)" }.size() == 1
        verifyGeneralErrorMessage(blockingMessages)
    }



    def "should return no errors when there is no active form v6 on a bonita in version 7.0.0"() {
        given:
        //mimic pre 7.1.0, remove 'target' column of form mapping and set source version to be before 7.1.0
        migrationContext.sourceVersion = Version.valueOf("7.0.2")
        migrationContext.databaseHelper.dropColumn("form_mapping", "target")
        //URLADAPTER is null, no legacy v6 forms
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 19, 'process/Pre710_process/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)""")
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 20, 'taskInstance/Pre710_process/1.0/Step1', 12, NULL, '', 'not used here', 0, 0)""")

        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 13, 4837119744348201769, 1, NULL, 1, 19, 0, 0)""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 14, 4837119744348201769, 1, NULL, 1, 20, 0, 0)""")

        migrationContext.sql.execute("""INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 1, 'Pre710_process', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'Pre710_process', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'Pre710_process', '', 1538483485209, NULL, NULL, 1, 3)
""")

        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        !blockingMessages.contains("* process/")
        !blockingMessages.contains("* taskInstance/")
        !blockingMessages.contains("* Pre710_process/1.0 (Instantiation form)")
        blockingMessages.findAll { it == "* Pre710_process/1.0/Step1 (Task form)" }.size() == 0
    }

    def "should return process name when enabled process has running instances with legacy forms v6"() {
        given:
        migrationContext.sql.execute("""INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 19, 'process/MessageReceiverProcess_733/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)
""")
        migrationContext.sql.execute("""INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 13, 4837119744348201769, 1, NULL, 1, 19, 0, 0, 'LEGACY')
""")
        migrationContext.sql.execute("""INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 1, 'MessageReceiverProcess_733', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")

        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        !blockingMessages.contains("* process/")
        !blockingMessages.contains("* taskInstance/")
        blockingMessages.contains("* MessageReceiverProcess_733/1.0 (Instantiation form)")
        verifyGeneralErrorMessage(blockingMessages)
    }

    def "should return process and task name when enabled process has running instances with legacy task forms v6"() {
        given:
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 19, 'taskInstance/MessageReceiverProcess_733/1.0/MessageReceivedAckTask', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)
""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 13, 4837119744348201769, 3, NULL, 1, 19, 0, 0, 'LEGACY')
""")
        migrationContext.sql.execute("""
INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 1, 'MessageReceiverProcess_733', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")
        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        !blockingMessages.contains("* process/")
        !blockingMessages.contains("* taskInstance/")
        blockingMessages.contains("* MessageReceiverProcess_733/1.0/MessageReceivedAckTask (Task form)")
        verifyGeneralErrorMessage(blockingMessages)
    }

    def "should not return anything when enabled process has only v6 case overview with running instances "() {
        given:
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 19, 'processInstance/MessageReceiverProcess_733/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)
""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 13, 4837119744348201769, 2, NULL, 1, 19, 0, 0, 'LEGACY')
""")
        migrationContext.sql.execute("""
INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 1, 'MessageReceiverProcess_733', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")
        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        blockingMessages.size() == 0
    }

    def "should not return anything when enabled process has only v7 forms or tasks forms"() {
        given:
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 19, 'process/MessageReceiverProcess_733/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)
""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 13, 4837119744348201769, 1, NULL, 1, 19, 0, 0, 'INTERNAL')
""")
        migrationContext.sql.execute("""
INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 1, 'MessageReceiverProcess_733', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")

        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        blockingMessages.size() == 0
    }

    def "should return process and task name only once when enabled process has several running instances with legacy task forms v6"() {
        given:
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 19, 'process/MessageReceiverProcess_733/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)
""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 13, 4837119744348201769, 1, NULL, 1, 19, 0, 0, 'LEGACY')
""")
        migrationContext.sql.execute("""
INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 1, 'MessageReceiverProcess_733', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")
        migrationContext.sql.execute("""
INSERT INTO process_instance
(TENANTID, ID, NAME, PROCESSDEFINITIONID, DESCRIPTION, STARTDATE, STARTEDBY, STARTEDBYSUBSTITUTE, ENDDATE, STATEID, STATECATEGORY, LASTUPDATE, CONTAINERID, ROOTPROCESSINSTANCEID, CALLERID, CALLERTYPE, INTERRUPTINGEVENTID, STRINGINDEX1, STRINGINDEX2, STRINGINDEX3, STRINGINDEX4, STRINGINDEX5)
VALUES(1, 2, 'MessageReceiverProcess_733', 4837119744348201769, '', 1538483571174, 4, 4, 0, 1, 'NORMAL', 1538483571174, 0, 1, -1, NULL, -1, NULL, NULL, NULL, NULL, NULL)
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")
        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        !blockingMessages.contains("* process/")
        !blockingMessages.contains("* taskInstance/")
        blockingMessages.findAll { it == "* MessageReceiverProcess_733/1.0 (Instantiation form)" }.size() == 1
        verifyGeneralErrorMessage(blockingMessages)
    }

    def "should not return anything when disabled process has v6 forms or tasks forms and no running instances"() {
        given:
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 19, 'process/MessageReceiverProcess_733/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)
""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 13, 4837119744348201769, 1, NULL, 1, 19, 0, 0, 'LEGACY')
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'DISABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")
        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        blockingMessages.size() == 0
    }

    def "should return process and task name when enabled process has v6 forms or tasks forms and no running instances"() {
        given:
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 19, 'process/MessageReceiverProcess_733/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)""")
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES (1, 20, 'taskInstance/MessageReceiverProcess_733/1.0/Step1', 12, NULL, NULL, 'not used here', 0, 0)""")

        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES (1, 13, 4837119744348201769, 1, NULL, 1, 19, 0, 0, 'LEGACY')""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES (1, 14, 4837119744348201769, 1, NULL, 1, 20, 0, 0, 'LEGACY')""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")
        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        !blockingMessages.contains("* process/")
        !blockingMessages.contains("* taskInstance/")
        blockingMessages.findAll { it == "* MessageReceiverProcess_733/1.0 (Instantiation form)" }.size() == 1
        blockingMessages.findAll { it == "* MessageReceiverProcess_733/1.0/Step1 (Task form)" }.size() == 1
        verifyGeneralErrorMessage(blockingMessages)
    }

    def "should not return anything when enabled archived process has v6 overview and no running instances"() {
        given:
        migrationContext.sql.execute("""
INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 19, 'process/MessageReceiverProcess_733/1.0', 11, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,', 0, 0)
""")
        migrationContext.sql.execute("""
INSERT INTO form_mapping
(TENANTID, ID, PROCESS, TYPE, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 13, 4837119744348201769, 2, NULL, 1, 19, 0, 0, 'LEGACY')
""")

        migrationContext.sql.execute("""INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 3, 4837119744348201769, 'MessageReceiverProcess_733', '1.0', '', 1538483484523, 4, 'ENABLED', 'RESOLVED', 'MessageReceiverProcess_733', '', 1538483485209, NULL, NULL, 1, 3)
""")
        when:
        def blockingMessages = v7_8_0.getPreMigrationBlockingMessages(migrationContext)

        then:
        blockingMessages.size() == 0
    }

    private static String[] verifyGeneralErrorMessage(String[] blockingMessages) {
        V6_FORMS_IN_ACTIVE_INSTANCES_OR_ENABLED_PROCESSES_PRESENT_MESSAGE.eachWithIndex { String msg, int i ->
            assert msg == blockingMessages[i]
        }
    }
}
