/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.migration.versions.v6_0_4to_6_1_0;

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


/**
 *
 *
 * @author Baptiste Mesta
 *
 */
@EqualsAndHashCode
@ToString
public class GatewayInstance {
    def tenantid
    def id
    def flownodeDefinitionId
    def kind
    def rootContainerId
    def parentContainerId
    def name
    def stateId
    def prev_state_id
    def stateName
    def terminal
    def stable
    def stateCategory
    def gatewayType
    def hitBys
    def logicalGroup1
    def logicalGroup2
    def logicalGroup3
    def logicalGroup4
    def tokenCount
    def token_ref_id
}
