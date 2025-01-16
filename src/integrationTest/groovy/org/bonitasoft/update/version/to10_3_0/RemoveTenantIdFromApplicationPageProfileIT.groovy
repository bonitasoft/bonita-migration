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

class RemoveTenantIdFromApplicationPageProfileIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromApplicationPageProfile updateStep = new RemoveTenantIdFromApplicationPageProfile()

    def "should remove tenantId from tables page, profile, business_app, business_app_page, business_app_menu"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("page", "tenantId")
            hasPrimaryKeyOnTable("page", "pk_page")
            !hasUniqueKeyOnTable("page", "uk_page")
            hasUniqueKeyOnTableWithNameAndColumns("page", "uk_page_name_processdefinitionid", "name", "processDefinitionId")

            !hasColumnOnTable("profile", "tenantId")
            hasPrimaryKeyOnTable("profile", "pk_profile")
            !hasUniqueKeyOnTableWithColumns("profile", "tenantId", "name")
            hasUniqueKeyOnTableWithNameAndColumns("profile", "uk_profile_name", "name")

            !hasColumnOnTable("business_app", "tenantId")
            hasPrimaryKeyOnTable("business_app", "pk_business_app")
            !hasUniqueKeyOnTable("business_app", "uk_app_token_version")
            !hasUniqueKeyOnTable("business_app", "UK_Business_app") // ORACLE
            hasUniqueKeyOnTableWithNameAndColumns("business_app", "uk_business_app_token_version", "token", "version")
            hasForeignKeyOnTable("business_app", "fk_business_app_profileid")
            hasForeignKeyOnTable("business_app", "fk_business_app_layoutid")
            hasForeignKeyOnTable("business_app", "fk_business_app_themeid")
            // renamed:
            !hasForeignKeyOnTable("business_app", "fk_app_profileId")
            !hasForeignKeyOnTable("business_app", "fk_app_layoutId")
            !hasForeignKeyOnTable("business_app", "fk_app_themeId")

            !hasColumnOnTable("business_app_page", "tenantId")
            hasPrimaryKeyOnTable("business_app_page", "pk_business_app_page")
            !hasUniqueKeyOnTable("business_app_page", "uk_app_page_appId_token")
            !hasUniqueKeyOnTable("business_app_page", "UK_Business_app_page") // ORACLE
            hasUniqueKeyOnTableWithNameAndColumns("business_app_page", "uk_business_app_page_applicationid_token", "applicationId", "token")
            hasForeignKeyOnTable("business_app_page", "fk_business_app_page_applicationid")
            hasForeignKeyOnTable("business_app_page", "fk_business_app_page_pageid")
            // renamed:
            !hasForeignKeyOnTable("business_app_page", "fk_bus_app_id")
            !hasForeignKeyOnTable("business_app_page", "fk_page_id")

            !hasColumnOnTable("business_app_menu", "tenantId")
            hasPrimaryKeyOnTable("business_app_menu", "pk_business_app_menu")
            hasForeignKeyOnTable("business_app_menu", "fk_business_app_menu_applicationid")
            hasForeignKeyOnTable("business_app_menu", "fk_business_app_menu_applicationpageid")
            hasForeignKeyOnTable("business_app_menu", "fk_business_app_menu_parentid")
            // renamed:
            !hasForeignKeyOnTable("business_app_menu", "fk_app_menu_appId")
            !hasForeignKeyOnTable("business_app_menu", "fk_app_menu_pageId")
            !hasForeignKeyOnTable("business_app_menu", "fk_app_menu_parentId")
        }
    }
}
