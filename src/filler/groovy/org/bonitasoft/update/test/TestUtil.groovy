/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.update.test

import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.identity.ContactDataCreator
import org.bonitasoft.engine.identity.GroupCreator
import org.bonitasoft.engine.identity.RoleCreator
import org.bonitasoft.engine.identity.UserCreator
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchOptionsBuilder

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.awaitility.Awaitility.await

/**
 * @author Baptiste Mesta
 */
class TestUtil {

    public
    static byte[] createTestPageContent(String pageName, String displayName, String description) throws Exception {
        ByteArrayOutputStream e = new ByteArrayOutputStream()
        ZipOutputStream zos = new ZipOutputStream(e)
        zos.putNextEntry(new ZipEntry("Index.groovy"))
        zos.write("return \"\";".getBytes())
        zos.putNextEntry(new ZipEntry("page.properties"))
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("name=")
        stringBuilder.append(pageName)
        stringBuilder.append("\n")
        stringBuilder.append("displayName=")
        stringBuilder.append(displayName)
        stringBuilder.append("\n")
        stringBuilder.append("description=")
        stringBuilder.append(description)
        stringBuilder.append("\n")
        zos.write(stringBuilder.toString().getBytes())
        zos.closeEntry()
        return e.toByteArray()
    }

    static UserCreator buildUserHelenKelly(long managerId = 0) {
        def user = new UserCreator("helen.kelly", "bpm")
                .setFirstName("Helen")
                .setLastName("Kelly")
                .setTitle("Mrs")
                .setJobTitle("Human resources manager")
                .setIcon("icon.png", "icon".getBytes())
                .setProfessionalContactData(new ContactDataCreator()
                .setEmail("helen.kelly@acme.com")
                .setPhoneNumber("484-302-5000")
                .setFaxNumber("484-302-0000")
                .setBuilding("70")
                .setAddress("Renwick Drive")
                .setZipCode("19108")
                .setCity("Philadelphia")
                .setState("PA")
                .setCountry("United States"))
        if (managerId > 0) {
            user.setManagerUserId(managerId)
        }
        return user
    }

    static UserCreator buildUserWalterBates(long managerId = 0) {
        def user = new UserCreator("walter.bates", "bpm")
                .setFirstName("Walter")
                .setLastName("Bates")
                .setTitle("Mr")
                .setJobTitle("Human resources benefits")
                .setIcon("icon.png", "icon".getBytes())
                .setProfessionalContactData(new ContactDataCreator()
                .setEmail("walter.bates@acme.com")
                .setPhoneNumber("484-302-5000")
                .setFaxNumber("484-302-0000")
                .setBuilding("70")
                .setAddress("Renwick Drive")
                .setZipCode("19108")
                .setCity("Philadelphia")
                .setState("PA")
                .setCountry("United States"))
        if (managerId > 0) {
            user.setManagerUserId(managerId)
        }
        return user
    }

    static GroupCreator buildGroupAcme() {
        return new GroupCreator("acme")
                .setDisplayName("Acme")
                .setDescription("This group represents the acme department of the ACME organization")
                .setIcon("icon.png", "icon".getBytes())
    }

    static RoleCreator buildRoleMember() {
        return new RoleCreator("member")
                .setDisplayName("Member")
                .setDescription("This role is for members of the acme group")
                .setIcon("icon.png", "icon".getBytes())
    }

    static void sendMessage(final String messageName, final String targetProcessName,
            final String targetFlowNodeName, ProcessAPI processAPI) throws Exception {
        processAPI.sendMessage(messageName, new ExpressionBuilder().createConstantStringExpression(targetProcessName),
                new ExpressionBuilder().createConstantStringExpression(targetFlowNodeName), null)
    }

    static void waitForUserTask(String taskName, ProcessAPI processAPI) {
        waitForUserTask(taskName, processAPI, 1L)
    }

    static void waitForUserTask(String taskName, ProcessAPI processAPI, long numberOfTaskInstances) {
        await().until({ processAPI.searchHumanTaskInstances(getSearchOptionsForTask(taskName)).count == numberOfTaskInstances })
    }

    static SearchOptions getSearchOptionsForTask(String taskName) {
        new SearchOptionsBuilder(0, 1)
                .filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, "ready")
                .filter(HumanTaskInstanceSearchDescriptor.NAME, taskName).done()
    }
}
