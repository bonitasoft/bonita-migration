/**
 * Copyright (C) 2016 BonitaSoft S.A.
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
package org.bonitasoft.migration

import static org.assertj.core.api.Assertions.assertThat

import java.nio.file.Files

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bpm.bar.BarResource
import org.bonitasoft.engine.bpm.bar.BusinessArchive
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.identity.UserCreator
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerBdmInitializer
import org.bonitasoft.migration.test.TestUtil
/**
 * @author Baptiste Mesta
 */
class FillBeforeMigratingTo7_3_0 {



    def static BusinessObjectModel createBusinessObjectModel() {
        final SimpleField firstName = new SimpleField()
        firstName.setName("name")
        firstName.setType(FieldType.STRING)
        firstName.setLength(Integer.valueOf(100))
        final BusinessObject testEntity = new BusinessObject()
        testEntity.setQualifiedName("com.company.model.TestEntity")
        testEntity.addField(firstName)
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(testEntity)
        model
    }

    @FillerBdmInitializer
    def deployBDM() {
        def businessObjectModel = createBusinessObjectModel()
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def tenantAdministrationAPI = TenantAPIAccessor.getTenantAdministrationAPI(session)

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter()
        final byte[] zip = converter.zip(businessObjectModel)

        tenantAdministrationAPI.pause()
        tenantAdministrationAPI.installBusinessDataModel(zip)
        tenantAdministrationAPI.resume()
    }

    @FillAction
    public void createUserWithIcons() {

        def login = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def identityAPI = TenantAPIAccessor.getIdentityAPI(login)
        identityAPI.createUser(new UserCreator("userWithIcon", "bpm").setIconPath("/users/myIcon.png"))
        identityAPI.createUser(new UserCreator("userWithoutIcon", "bpm"))
        def iconDir = new File(System.getProperty("bonita.home")).toPath().resolve("client").resolve("tenants").resolve(String.valueOf(login.tenantId)).resolve("work").resolve("icons").resolve("users")
        Files.createDirectories(iconDir)
        iconDir.resolve("myIcon.png").write("the icon content")
    }


    @FillAction
    public void createProcessWithCallActivities() {
        def client = new APIClient()
        client.login("install", "install")
        client.identityAPI.createUser("userForProcessWithCallActivity", "bpm")
        def builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithCallActivity", "1.0")
        builder.addActor("myActor")
        builder.addUserTask("userTask1", "myActor")
        builder.addCallActivity("callMySelf", new ExpressionBuilder().createConstantStringExpression("ProcessWithCallActivity"), new ExpressionBuilder().createConstantStringExpression("1.0"))
        builder.addTransition("userTask1", "callMySelf")

        def mapping = new ActorMapping()
        def actor = new Actor("myActor")
        actor.addUser("userForProcessWithCallActivity")
        mapping.addActor(actor)
        def businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setActorMapping(mapping).setProcessDefinition(builder.done()).done()


        def processDefinition = client.processAPI.deploy(businessArchive)
        client.processAPI.enableProcess(processDefinition.getId())


        client.processAPI.startProcess(processDefinition.getId())
    }


    @FillAction
    public void "create a page to check migration of not null process definition id in page mapping"() {
        def client = new APIClient()
        client.login("install", "install")
        def content = TestUtil.createTestPageContent("custompage_APIBirthdayBonita", "APIBirthdayBonita", "a custom page to test the not null pdefid")
        def page = client.customPageAPI.createPage("mypage.zip", content)
        assertThat(page).isNotNull()
    }

    @FillAction
    public void "create processes with auto login resource in bar in different states"() {
        def client = new APIClient()
        client.login("install", "install")
        client.identityAPI.createUser("userForAutoLogin", "bpm")
        client.processAPI.deploy(buildProcessWithAutoLogin("DisabledProcess"))
        client.processAPI.deployAndEnableProcess(buildProcessWithAutoLogin("EnabledProcess"))
    }

    private BusinessArchive buildProcessWithAutoLogin(String processName) {
        def builder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0")
        builder.addActor("myActor")
        builder.addUserTask("userTask1", "myActor")

        def mapping = new ActorMapping()
        def actor = new Actor("myActor")
        actor.addUser("userForAutoLogin")
        mapping.addActor(actor)

        final byte[] autoLoginProperties = """
                #Wed Jun 15 20:02:10 CEST 2016
                security.password.validator=org.bonitasoft.web.rest.server.api.organization.password.validator.DefaultPasswordValidator
                forms.application.login.auto.password=secret
                security.rest.api.authorizations.check.enabled=true
                forms.application.login.auto=true
                forms.application.login.auto.username=autologin-user
                security.rest.api.authorizations.check.debug=false
                """.bytes

        def businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setActorMapping(mapping)
                .setProcessDefinition(builder.done())
                .addExternalResource(new BarResource("forms/security-config.properties", autoLoginProperties)).done()
        businessArchive
    }
}

