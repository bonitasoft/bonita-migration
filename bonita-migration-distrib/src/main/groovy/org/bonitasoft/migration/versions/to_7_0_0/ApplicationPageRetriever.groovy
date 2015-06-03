/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.versions.to_7_0_0
import groovy.sql.Sql
/**
 * @author Elias Ricken de Medeiros
 */
class ApplicationPageRetriever  {

    private final String dbVendor
    private final Sql sql

    ApplicationPageRetriever(final Sql sql, final String dbVendor) {
        this.sql = sql
        this.dbVendor = dbVendor
    }

    def retrieveApplicationsWithInvalidPages() {
        def allApplications = []
        sql.eachRow("SELECT id FROM tenant", { tenantRow ->
            Map<?, Application>  idToApplicationMap = [:]
            Map<?, Page> idToPageMap = [:]
            Map<?, ApplicationPage>  applicationPages = [:]

            def tenantId = tenantRow.id
            def tenantApplications = new TenantApplications(tenantId: tenantId)
            def query = """
                        SELECT tenantId, id, applicationId, token, pageId
                        FROM business_app_page
                        WHERE UPPER(token) IN ('CONTENT', 'THEME', 'API')
                        AND tenantId = ${tenantId}
                        ORDER BY id
                        """
            sql.eachRow(query, { applicationPageRow ->
                def applicationId = applicationPageRow.applicationId
                ensureApplicationIsLoaded(idToApplicationMap, applicationId, tenantId)
                ensurePageIsLoaded(idToPageMap, applicationPageRow.pageId, tenantId)
                addApplicationPageToMap(applicationPageRow, applicationPages, idToPageMap)
            })

            bindPagesToApplications(applicationPages, idToApplicationMap, tenantApplications)
            if(!tenantApplications.applications.isEmpty()) {
                allApplications.add(tenantApplications)
            }

        })
        return allApplications
    }

    private void addApplicationPageToMap(applicationPageRow, Map<?, ApplicationPage> applicationPages, Map<?, Page> idToPageMap) {
        def applicationId = applicationPageRow.applicationId
        if (applicationPages[applicationId] == null) {
            applicationPages.put(applicationId, [])
        }
        applicationPages[applicationId].add(new ApplicationPage(id: applicationPageRow.id, token: applicationPageRow.token, page: idToPageMap[applicationPageRow.pageId]))
    }

    private void ensureApplicationIsLoaded(Map<?, Application>  idToApplicationMap, def applicationId, def tenantId){
        if(idToApplicationMap[applicationId] == null){
            idToApplicationMap.put(applicationId, retrieveApplication(tenantId, applicationId))
        }
    }

    private void ensurePageIsLoaded(Map<?, Page> idToPageMap, def pageId, def tenantId){
        if(idToPageMap[pageId] == null){
            idToPageMap.put(pageId, retrievePage(tenantId, pageId))
        }
    }

    private void bindPagesToApplications(Map<?, ApplicationPage> applicationPages, Map<?, Application> idToApplicationMap, tenantApplications) {
        applicationPages.each { entry ->
            idToApplicationMap[entry.key].setApplicationPages(applicationPages[entry.key])
            tenantApplications.applications.add(idToApplicationMap[entry.key],)
        }
    }

    private Application retrieveApplication(def tenantId, def applicationId) {
        def applicationQuery = """
                        SELECT tenantId, id, token, version, displayName
                        FROM business_app
                        WHERE id = $applicationId
                        AND tenantId = $tenantId
                       """

        def applicationRow = sql.firstRow(applicationQuery)
        return new Application(tenantId: applicationRow.tenantId, id: applicationRow.id, token: applicationRow.token, version: applicationRow.version, displayName: applicationRow.displayName)
    }

    private Page retrievePage(def tenantId, def pageId) {
        def pageQuery = """
                        SELECT tenantId, id, name, displayName
                        FROM page
                        WHERE id = $pageId
                        AND tenantId = $tenantId
                       """

        def pageRow = sql.firstRow(pageQuery)
        return new Page(name: pageRow.name, displayName: pageRow.displayName)
    }
}
