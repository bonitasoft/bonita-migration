/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to7_14_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.version.to7_14_0.MakeAdminPagesFinal
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Dumitru Corini
 */
class MakeAdminPagesFinalIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private MakeAdminPagesFinal updateStep = new MakeAdminPagesFinal()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_13_0/pages")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page", "sequence", "tenant"] as String[])
    }


    def "should set removable and editable field to false"() {
        given:
        assert updateContext.databaseHelper.hasColumnOnTable("page", "editable")
        assert updateContext.databaseHelper.hasColumnOnTable("page", "removable")
        insertPages()

        when:
        updateStep.execute(updateContext)

        then:
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminCaseDetailsBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminCaseDetailsBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminCaseListBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminCaseListBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminCaseVisuBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminCaseVisuBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminMonitoringBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminMonitoringBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminProcessDetailsBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminProcessDetailsBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminProcessListBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminProcessListBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminProcessVisuBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminProcessVisuBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminTaskDetailsBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminTaskDetailsBonita' """).get(0).get("editable")

        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminTaskListBonita' """).get(0).get("removable")
        !updateContext.sql.rows("""SELECT page.removable, page.editable FROM page WHERE page.name = 'custompage_adminTaskListBonita' """).get(0).get("editable")
    }

    private void insertPages() {
        switch (updateContext.dbVendor) {
            case UpdateStep.DBVendor.SQLSERVER:
                dbUnitHelper.context.sql.execute(
                        """
                         INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided,
                         editable, removable, hidden, lastModificationDate, lastUpdatedBy, contentName, content,
                         contentType, processDefinitionId) VALUES (1, 24, N'custompage_adminCaseDetailsBonita', N'Bonita Admin Case Details',
        N'This page provides the detail information of a case. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an ''id'' parameter to display some content.',
        1660030270259, -1, 1, 1, 1, 0, 1660030270259, -1, N'page-admin-case-details-7.13.5.zip', NULL, N'page', 0),
       (1, 25, N'custompage_adminCaseListBonita', N'Bonita Admin Case List',
        N'This page provides the list of open and archived cases. It is dedicated to admin-like profiles. You can export it and edit it in the UI Designer.',
        1660030270274, -1, 1, 1, 1, 0, 1660030270274, -1, N'page-admin-case-list-7.13.5.zip', NULL, N'page', 0),
       (1, 26, N'custompage_adminCaseVisuBonita', N'Bonita Admin Case Visualization',
        N'This page provides the BPMN visualization of a case with its execution status. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1660030270282, -1, 1, 1, 1, 0, 1660030270282, -1, N'page-admin-case-visu-7.13.5.zip', NULL, N'page', 0),
       (1, 27, N'custompage_adminMonitoringBonita', N'Bonita Admin Monitoring',
        N'This page provides monitoring information on cases and processes execution. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1660030270288, -1, 1, 1, 1, 0, 1660030270288, -1, N'page-admin-monitoring-7.13.5.zip', NULL, N'page', 0),
       (1, 28, N'custompage_adminProcessDetailsBonita', N'Bonita Admin Process Details',
        N'This page provides detailed information about a process. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1660030270294, -1, 1, 1, 1, 0, 1660030270294, -1, N'page-admin-process-details-7.13.5.zip', NULL, N'page', 0),
       (1, 29, N'custompage_adminProcessListBonita', N'Bonita Admin Process List',
        N'This page lists all the processes. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',
        1660030270304, -1, 1, 1, 1, 0, 1660030270304, -1, N'page-admin-process-list-7.13.5.zip', NULL, N'page', 0),
       (1, 30, N'custompage_adminProcessVisuBonita', N'Bonita Admin Process Visualization',
        N'This page provides the BPMN visualization and execution data of all open cases of a process visualization. It is dedicated to Admin-like profiles. Default admin process visualization. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1660030270311, -1, 1, 1, 1, 0, 1660030270311, -1, N'page-admin-process-visu-7.13.5.zip', NULL, N'page', 0),
       (1, 31, N'custompage_adminTaskDetailsBonita', N'Bonita Admin Task Details',
        N'This page provides the detail information of a task. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an ''id'' parameter to display some content.',
        1660030270324, -1, 1, 1, 1, 0, 1660030270324, -1, N'page-admin-task-details-7.13.5.zip', NULL, N'page', 0),
       (1, 32, N'custompage_adminTaskListBonita', N'Bonita Admin Task List',
        N'This page lists all the tasks. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',
        1660030270338, -1, 1, 1, 1, 0, 1660030270338, -1, N'page-admin-task-list-7.13.5.zip', NULL, N'page', 0);
                    """)
                return
            case UpdateStep.DBVendor.ORACLE:

                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,24,'custompage_adminCaseDetailsBonita','Bonita Admin Case Details','This page provides the detail information of a case. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an ''id'' parameter to display some content.',1659975428683,-1,1,1,1,0,1659975428683,-1,'page-admin-case-details-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,25,'custompage_adminCaseListBonita','Bonita Admin Case List','This page provides the list of open and archived cases. It is dedicated to admin-like profiles. You can export it and edit it in the UI Designer.',1659975428700,-1,1,1,1,0,1659975428700,-1,'page-admin-case-list-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,26,'custompage_adminCaseVisuBonita','Bonita Admin Case Visualization','This page provides the BPMN visualization of a case with its execution status. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',1659975428710,-1,1,1,1,0,1659975428710,-1,'page-admin-case-visu-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,27,'custompage_adminMonitoringBonita','Bonita Admin Monitoring','This page provides monitoring information on cases and processes execution. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',1659975428721,-1,1,1,1,0,1659975428721,-1,'page-admin-monitoring-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,28,'custompage_adminProcessDetailsBonita','Bonita Admin Process Details','This page provides detailed information about a process. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',1659975428727,-1,1,1,1,0,1659975428727,-1,'page-admin-process-details-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,29,'custompage_adminProcessListBonita','Bonita Admin Process List','This page lists all the processes. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',1659975428741,-1,1,1,1,0,1659975428741,-1,'page-admin-process-list-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,30,'custompage_adminProcessVisuBonita','Bonita Admin Process Visualization','This page provides the BPMN visualization and execution data of all open cases of a process visualization. It is dedicated to Admin-like profiles. Default admin process visualization. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',1659975428750,-1,1,1,1,0,1659975428750,-1,'page-admin-process-visu-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,31,'custompage_adminTaskDetailsBonita','Bonita Admin Task Details','This page provides the detail information of a task. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an ''id'' parameter to display some content.',1659975428770,-1,1,1,1,0,1659975428770,-1,'page-admin-task-details-7.13.5.zip',NULL,'page',0)""")
                dbUnitHelper.context.sql.execute("""INSERT INTO PAGE (TENANTID,ID,NAME,DISPLAYNAME,DESCRIPTION,INSTALLATIONDATE,INSTALLEDBY,PROVIDED,EDITABLE,REMOVABLE,HIDDEN,LASTMODIFICATIONDATE,LASTUPDATEDBY,CONTENTNAME,CONTENT,CONTENTTYPE,PROCESSDEFINITIONID) VALUES (1,32,'custompage_adminTaskListBonita','Bonita Admin Task List','This page lists all the tasks. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',1659975428786,-1,1,1,1,0,1659975428786,-1,'page-admin-task-list-7.13.5.zip',NULL,'page',0)""")
                return
            case UpdateStep.DBVendor.POSTGRES:
                dbUnitHelper.context.sql.execute(
                        """
                         INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided,
                         editable, removable, hidden, lastModificationDate, lastUpdatedBy, contentName, content,
                         contentType, processDefinitionId) VALUES (1, 24, 'custompage_adminCaseDetailsBonita', 'Bonita Admin Case Details',
        'This page provides the detail information of a case. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an id parameter to display some content.',
        1659943756652, -1, true, false, false, false, 1659943756652, -1, 'page-admin-case-details-7.13.5.zip', NULL,
        'page', 0),
       (1, 25, 'custompage_adminCaseListBonita', 'Bonita Admin Case List',
        'This page provides the list of open and archived cases. It is dedicated to admin-like profiles. You can export it and edit it in the UI Designer.',
        1659943756674, -1, true, false, false, false, 1659943756674, -1, 'page-admin-case-list-7.13.5.zip', NULL, 'page',
        0),
       (1, 26, 'custompage_adminCaseVisuBonita', 'Bonita Admin Case Visualization',
        'This page provides the BPMN visualization of a case with its execution status. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659943756688, -1, true, false, false, false, 1659943756688, -1, 'page-admin-case-visu-7.13.5.zip', NULL, 'page',
        0),
       (1, 27, 'custompage_adminMonitoringBonita', 'Bonita Admin Monitoring',
        'This page provides monitoring information on cases and processes execution. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659943756700, -1, true, false, false, false, 1659943756700, -1, 'page-admin-monitoring-7.13.5.zip', NULL, 'page',
        0),
       (1, 28, 'custompage_adminProcessDetailsBonita', 'Bonita Admin Process Details',
        'This page provides detailed information about a process. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659943756704, -1, true, false, false, false, 1659943756704, -1, 'page-admin-process-details-7.13.5.zip', NULL,
        'page', 0),
       (1, 29, 'custompage_adminProcessListBonita', 'Bonita Admin Process List',
        'This page lists all the processes. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',
        1659943756714, -1, true, false, false, false, 1659943756714, -1, 'page-admin-process-list-7.13.5.zip', NULL,
        'page', 0),
       (1, 30, 'custompage_adminProcessVisuBonita', 'Bonita Admin Process Visualization',
        'This page provides the BPMN visualization and execution data of all open cases of a process visualization. It is dedicated to Admin-like profiles. Default admin process visualization. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659943756719, -1, true, false, false, false, 1659943756719, -1, 'page-admin-process-visu-7.13.5.zip', NULL,
        'page', 0),
       (1, 31, 'custompage_adminTaskDetailsBonita', 'Bonita Admin Task Details',
        'This page provides the detail information of a task. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an id parameter to display some content.',
        1659943756735, -1, true, false, false, false, 1659943756735, -1, 'page-admin-task-details-7.13.5.zip', NULL,
        'page', 0),
       (1, 32, 'custompage_adminTaskListBonita', 'Bonita Admin Task List',
        'This page lists all the tasks. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',
        1659943756746, -1, true, false, false, false, 1659943756746, -1, 'page-admin-task-list-7.13.5.zip', NULL, 'page',
        0);""")
                return
            case UpdateStep.DBVendor.MYSQL:
                dbUnitHelper.context.sql.execute(
                        """
                         INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided,
                         editable, removable, hidden, lastModificationDate, lastUpdatedBy, contentName, content,
                         contentType, processDefinitionId) VALUES (1, 24, 'custompage_adminCaseDetailsBonita', 'Bonita Admin Case Details',
        'This page provides the detail information of a case. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an ''id'' parameter to display some content.',
        1659972698692, -1, 1, 1, 1, 0, 1659972698692, -1, 'page-admin-case-details-7.13.5.zip', NULL, 'page', 0),
       (1, 25, 'custompage_adminCaseListBonita', 'Bonita Admin Case List',
        'This page provides the list of open and archived cases. It is dedicated to admin-like profiles. You can export it and edit it in the UI Designer.',
        1659972698713, -1, 1, 1, 1, 0, 1659972698713, -1, 'page-admin-case-list-7.13.5.zip', NULL, 'page', 0),
       (1, 26, 'custompage_adminCaseVisuBonita', 'Bonita Admin Case Visualization',
        'This page provides the BPMN visualization of a case with its execution status. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659972698744, -1, 1, 1, 1, 0, 1659972698744, -1, 'page-admin-case-visu-7.13.5.zip', NULL, 'page', 0),
        (1, 27, 'custompage_adminMonitoringBonita', 'Bonita Admin Monitoring',
        'This page provides monitoring information on cases and processes execution. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659972698762, -1, 1, 1, 1, 0, 1659972698762, -1, 'page-admin-monitoring-7.13.5.zip', NULL, 'page', 0),
       (1, 28, 'custompage_adminProcessDetailsBonita', 'Bonita Admin Process Details',
        'This page provides detailed information about a process. It is dedicated to Admin-like profiles. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659972698793, -1, 1, 1, 1, 0, 1659972698793, -1, 'page-admin-process-details-7.13.5.zip', NULL, 'page', 0),
       (1, 29, 'custompage_adminProcessListBonita', 'Bonita Admin Process List',
        'This page lists all the processes. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',
        1659972698810, -1, 1, 1, 1, 0, 1659972698810, -1, 'page-admin-process-list-7.13.5.zip', NULL, 'page', 0),
       (1, 30, 'custompage_adminProcessVisuBonita', 'Bonita Admin Process Visualization',
        'This page provides the BPMN visualization and execution data of all open cases of a process visualization. It is dedicated to Admin-like profiles. Default admin process visualization. This page has not been created with the UI Designer but it can still be used as a custom page in any application.',
        1659972698823, -1, 1, 1, 1, 0, 1659972698823, -1, 'page-admin-process-visu-7.13.5.zip', NULL, 'page', 0),
       (1, 31, 'custompage_adminTaskDetailsBonita', 'Bonita Admin Task Details',
        'This page provides the detail information of a task. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer. This page can only be used as an orphan page as it needs an ''id'' parameter to display some content.',
        1659972698851, -1, 1, 1, 1, 0, 1659972698851, -1, 'page-admin-task-details-7.13.5.zip', NULL, 'page', 0),
       (1, 32, 'custompage_adminTaskListBonita', 'Bonita Admin Task List',
        'This page lists all the tasks. It is dedicated to Admin-like profiles. You can export it and edit it in the UI Designer.',
        1659972698867, -1, 1, 1, 1, 0, 1659972698867, -1, 'page-admin-task-list-7.13.5.zip', NULL, 'page', 0);
""")
                return
        }
    }
}
