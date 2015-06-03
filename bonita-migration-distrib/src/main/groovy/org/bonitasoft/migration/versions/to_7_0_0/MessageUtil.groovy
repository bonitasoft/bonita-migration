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

/**
 * @author Elias Ricken de Medeiros
 */
class MessageUtil {

    def static lineSeparator = System.getProperty("line.separator")

    static GString buildTenantMessage(def tenantId) {
        "$lineSeparator--> Tenant ${tenantId}:$lineSeparator"
    }

    static String buildInvalidApplicationTokenHeader() {
        """
--------------------------------------------------------------------------------------------------------------------------------------------------
  /!\\ From version 7.0.0 the following words are reserved keywords and must not be used as part of an application URL: 'API', 'content', 'theme'.
  --> The following applications have invalid URL tokens. Update these application URL tokens:
--------------------------------------------------------------------------------------------------------------------------------------------------
 """
    }

    static String buildInvalidApplicationPageTokenHeader() {
        """
-------------------------------------------------------------------------------------------------------------------------------------------------------
  /!\\ From version 7.0.0 the following words are reserved keywords and must not be used as part of an application page URL: 'API', 'content', 'theme'.
  --> The following application pages have invalid URL tokens. Delete each page association and recreate it with a different URL token:
-------------------------------------------------------------------------------------------------------------------------------------------------------
  """
    }

    static GString buildApplicationMessage(Application application) {
        "----| Application[id: ${application.id}, URL token: '${application.token}', version: '${application.version}', display name: '${application.displayName}']$lineSeparator"
    }

    static GString buildApplicationPageMessage(ApplicationPage applicationPage) {
        "------| Application page[id: ${applicationPage.id}, URL token: '${applicationPage.token}', refered page: Page [name: '${applicationPage.page.name}', display name: '${applicationPage.page.displayName}']]$lineSeparator"
    }

}
