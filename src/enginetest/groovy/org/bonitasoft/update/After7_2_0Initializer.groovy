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
package org.bonitasoft.update

import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * @author Baptiste Mesta
 */
class After7_2_0Initializer implements MethodRule {

    @Override
    Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object target) {
        statement = BonitaEngineRule.create().reuseExistingPlatform().apply(statement, frameworkMethod, target)
        return new Statement() {
                    @Override
                    void evaluate() throws Throwable {
                        CheckerUtils.initializeEngineSystemProperties()
                        statement.evaluate()
                    }
                }
    }
}
