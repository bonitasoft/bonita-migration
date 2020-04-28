/**
 * Copyright (C) 2018 BonitaSoft S.A.
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


import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.Rule

class FillBeforeMigratingTo7_11_0 {


    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().keepPlatformOnShutdown()

    @FillerInitializer
    void init() {
        FillerUtils.initializeEngineSystemProperties()
    }

    @FillAction
    void 'create and install BDM'() {
        def client = new APIClient()
        client.login("install", "install")
        client.tenantAdministrationAPI.pause()

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        def bOM = buildCustomBOM()
        final byte[] zip = converter.zip(bOM);
        client.tenantAdministrationAPI.installBusinessDataModel(zip)
        client.tenantAdministrationAPI.resume()
    }


    BusinessObjectModel buildCustomBOM() {
        return new BusinessObjectModel().with {
            modelVersion = "1.0"
            productVersion = "7.10.0"
            addBusinessObject(new BusinessObject().with {
                qualifiedName = "com.compagny.BO"
                addField( new SimpleField().with {
                    name = "name"
                    type = FieldType.TEXT
                    length = 10
                    return it
                })
                return it
            })
            return it
        }
    }
}

