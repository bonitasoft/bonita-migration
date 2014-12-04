/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.migration;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class SimpleDatabaseFiller6_4_0 extends SimpleDatabaseFiller6_3_1 {

    @Override
    protected APISession loginDefaultTenant() throws BonitaException {
        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        APISession session = apiTestUtil.getSession();
        tenantId = session.getTenantId();
        return session;
    }

}
