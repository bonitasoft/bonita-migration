/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.update.filler.FillAction
import org.bonitasoft.update.filler.FillerInitializer
import org.bonitasoft.update.filler.FillerUtils
/**
 * @author Emmanuel Duchastenier
 */
class CommonInitializer {

    @FillerInitializer
    void init() {
        FillerUtils.initializeEngineSystemProperties()
    }

    @FillAction
    public void fillOneUserWithTechnicalUser() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        identityAPI.createUser("john", "bpm")
        TenantAPIAccessor.getLoginAPI().logout(session)
    }

    static BusinessObjectModel buildCustomBOM() {
        return new BusinessObjectModel().with {
            modelVersion = "1.0"
            productVersion = "7.11.3"
            addBusinessObject(new BusinessObject().with {
                qualifiedName = "com.compagny.BO"
                addField(new SimpleField().with {
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
