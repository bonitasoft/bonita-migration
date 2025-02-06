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

import spock.lang.Specification

class UpdateTo10_3_0Test extends Specification {

    def "should update to 10.3.0 include all steps"() {
        given:
        def updateTo = new UpdateTo10_3_0()
        def expectedSteps = [
            "CreateBpmFailureTables",
            "RemoveUnusedTables",
            "RemoveTenantIdFromIndexes",
            "RemoveTenantIdFromBusinessDataAndFlowNodeInstance",
            "RemoveTenantIdFromPageAndFormMapping",
            "RemoveTenantIdFromApplicationPageProfile",
            "RemoveTenantIdFromJobTables",
            "RemoveTenantIdFromIdentityTables",
            "RemoveTenantIdFromProcessComment",
            "RemoveTenantIdFromContractDataTables",
            "RemoveTenantIdFromTriggersEventsMessages",
            "RemoveTenantIdFromCommandsBARAndTenantResources",
            "RemoveTenantIdFromDependencyTables",
            "RemoveTenantIdFromDocuments",
            "RemoveTenantIdFromQuartzGroups",
            "CleanupTenantReferencingTables",
        ]

        expect:
        def steps = updateTo.updateSteps
        steps.size() == expectedSteps.size()
        steps.collect {
            it.class.getSimpleName()
        }.containsAll(expectedSteps)
    }
}
