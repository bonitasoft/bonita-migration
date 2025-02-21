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
package org.bonitasoft.update.version.to10_3_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

import static org.bonitasoft.update.core.UpdateStep.DBVendor.ORACLE

/**
 * Remove tenantId from tables: page, profile, application, business_app, business_app_page, business_app_menu
 *
 * @author Emmanuel Duchastenier
 */
class RemoveTenantIdFromApplicationPageProfile extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // FK on tenant table:
            dropForeignKey("business_app", "fk_app_tenantId")
            dropForeignKey("business_app_page", "fk_app_page_tenantId")
            dropForeignKey("business_app_menu", "fk_app_menu_tenantId")

            // FK on other tables:
            dropForeignKey("business_app", "fk_app_profileId")
            dropForeignKey("business_app", "fk_business_app_profileid") // to make step reentrant
            dropForeignKey("business_app", "fk_app_layoutId")
            dropForeignKey("business_app", "fk_business_app_layoutid") // to make step reentrant
            dropForeignKey("business_app", "fk_app_themeId")
            dropForeignKey("business_app", "fk_business_app_themeid") // to make step reentrant
            dropForeignKey("business_app", "fk_business_app_profileid") // to make step reentrant
            dropForeignKey("business_app_page", "fk_bus_app_id")
            dropForeignKey("business_app_page", "fk_business_app_page_applicationid") // to make step reentrant
            dropForeignKey("business_app_page", "fk_page_id")
            dropForeignKey("business_app_page", "fk_business_app_page_pageid") // to make step reentrant
            dropForeignKey("business_app_menu", "fk_app_menu_appId")
            dropForeignKey("business_app_menu", "fk_business_app_menu_applicationid") // to make step reentrant
            dropForeignKey("business_app_menu", "fk_app_menu_pageId")
            dropForeignKey("business_app_menu", "fk_business_app_menu_applicationpageid") // to make step reentrant
            dropForeignKey("business_app_menu", "fk_app_menu_parentId")
            dropForeignKey("business_app_menu", "fk_business_app_menu_parentid") // to make step reentrant
            dropForeignKey("profilemember", "fk_profilemember_profileId") // to make step reentrant on Oracle

            // FK that do not exist on Oracle
            if (dbVendor != ORACLE) {
                dropForeignKey("profile", "fk_profile_tenantId")
                dropForeignKey("profilemember", "fk_profilemember_tenantId")
            }

            dropUniqueKey("business_app_page", dbVendor == ORACLE ? "UK_Business_app_page" : "uk_app_page_appId_token") // existing name is different on Oracle
            dropUniqueKey("business_app", dbVendor == ORACLE ? "UK_Business_app" : "uk_app_token_version") // existing name is different on Oracle
            dropUniqueKeyFromColumns("profile", "tenantId", "name") // we don't know its name
            dropUniqueKeyFromColumns("profilemember", "tenantId", "profileId", "userId", "groupId", "roleId") // we don't know its name
            dropUniqueKey("page", "uk_page")

            // recreate PK (name pattern pk_<tableName>):
            recreatePrimaryKey("page")
            recreatePrimaryKey("profile")
            recreatePrimaryKey("profilemember")
            recreatePrimaryKey("business_app")
            recreatePrimaryKey("business_app_page")
            recreatePrimaryKey("business_app_menu")

            createUniqueKey("page", "uk_page_name_processdefinitionid", "name", "processDefinitionId")
            createUniqueKey("profile", "uk_profile_name", "name")
            createUniqueKey("profilemember", "uk_profilemember_profileid_userid_groupid_roleid", "profileId", "userId", "groupId", "roleId")
            createUniqueKey("business_app", "uk_business_app_token_version", "token", "version")
            createUniqueKey("business_app_page", "uk_business_app_page_applicationid_token", "applicationId", "token")

            createForeignKey("profilemember", "fk_profilemember_profileid", "profile", ["profileId"], ["id"], false)
            createForeignKey("business_app", "fk_business_app_profileid", "profile", ["profileId"], ["id"], false)
            createForeignKey("business_app", "fk_business_app_layoutid", "page", ["layoutId"], ["id"], false)
            createForeignKey("business_app", "fk_business_app_themeid", "page", ["themeId"], ["id"], false)
            createForeignKey("business_app_page", "fk_business_app_page_applicationid", "business_app", ["applicationId"], ["id"], true) // on delete cascade
            createForeignKey("business_app_page", "fk_business_app_page_pageid", "page", ["pageId"], ["id"], false)
            createForeignKey("business_app_menu", "fk_business_app_menu_applicationid", "business_app", ["applicationId"], ["id"], false)
            createForeignKey("business_app_menu", "fk_business_app_menu_applicationpageid", "business_app_page", ["applicationPageId"], ["id"], false)
            createForeignKey("business_app_menu", "fk_business_app_menu_parentid", "business_app_menu", ["parentId"], ["id"], false)

            // Finally drop the tenantid column:
            dropColumnIfExists("page", "tenantId")
            dropColumnIfExists("profile", "tenantId")
            dropColumnIfExists("profilemember", "tenantId")
            dropColumnIfExists("business_app_page", "tenantId")
            dropColumnIfExists("business_app_menu", "tenantId")
            dropColumnIfExists("business_app", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from tables: page, profile, application, business_app, business_app_page, business_app_menu"
    }
}
