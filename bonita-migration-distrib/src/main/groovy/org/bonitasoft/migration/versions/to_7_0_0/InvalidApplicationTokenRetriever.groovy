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
class InvalidApplicationTokenRetriever implements ApplicationRetriever {

    private final Sql sql
    private final String dbVendor

    InvalidApplicationTokenRetriever(final Sql sql, final String dbVendor) {
        this.dbVendor = dbVendor
        this.sql = sql
    }

    @Override
    String getHeader() {
        return MessageUtil.buildInvalidApplicationTokenHeader()
    }

    List<TenantApplications> retrieveApplications() {
        def allApplications = []
        sql.eachRow("SELECT id FROM tenant", { tenantRow ->

            def tenantApplications = new TenantApplications(tenantId: tenantRow.id)
            def query = """
                        SELECT tenantId, id, token, version, displayName
                        FROM business_app
                        WHERE UPPER(token) IN ('CONTENT', 'THEME', 'API')
                        AND tenantId = $tenantRow.id
                        ORDER BY id
                        """
            sql.eachRow(query, { applicationRow ->
                tenantApplications.applications.add(new Application(tenantId: applicationRow.tenantId, id: applicationRow.id, token: applicationRow.token, version: applicationRow.version, displayName: applicationRow.displayName))
            })
            if(!tenantApplications.applications.isEmpty()) {
                allApplications.add(tenantApplications)
            }
        })
        return allApplications
    }
}
