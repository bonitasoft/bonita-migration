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

import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.test.TestEngineImpl
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown
import org.bonitasoft.migration.filler.FillerUtils
/**
 * @author Laurent Leseigneur
 */
class FillBeforeMigratingTo7_2_2 {

    /**
     * init platform before fill actions
     */
    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
        TestEngineImpl.instance.start()
    }

    /**
     * stop platform after all fill actions
     */
    @FillerShutdown
    public void shutdown() {
        //to be replaced by BonitaEngineRule with keepPlatformOnShutdown option
        def session = PlatformAPIAccessor.getPlatformLoginAPI().login("platformAdmin", "platform")
        PlatformAPIAccessor.getPlatformAPI(session).stopNode()
        PlatformAPIAccessor.getPlatformLoginAPI().logout(session)
    }


}
