/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.update

import static org.bonitasoft.update.test.TestUtil.createTestPageContent

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.business.application.ApplicationCreator
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.update.filler.FillAction
import org.junit.Rule

class FillBeforeUpdatingTo7_13_0 {
    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().keepPlatformOnShutdown()


    @FillAction
    void "create application without icon"() {
        def client = new APIClient()
        client.login("install", "install")
        client.customPageAPI.createPage("layout.zip",
                createTestPageContent("custompage_layoutBonita", "LayoutBonita", "The default layout"))
        client.customPageAPI.createPage("theme.zip",
                createTestPageContent("custompage_bootstrapdefaulttheme", "defaultTheme", "The default theme"))
        client.applicationAPI.createApplication(new ApplicationCreator("myAppWithoutIcon", "My app without icon", "1.0").with {
            it.iconPath = "the icon path.png"
            return it
        })
    }
    @FillAction
    void "create page before update"() {
        def client = new APIClient()
        client.login("install", "install")
        client.customPageAPI.createPage("page-before-7.13.zip",
                createTestPageContent("custompage_PageBefore713", "PageBefore713", "A page created before 7.13"))
    }
}
