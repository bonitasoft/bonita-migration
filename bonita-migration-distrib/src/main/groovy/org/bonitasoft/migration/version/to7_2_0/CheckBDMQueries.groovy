/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_2_0

import org.bonitasoft.migration.core.BDMUtil
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 *
 * check if BDM is deployed on tenant
 * BDM is located in bonita-home/engine-server/work/tenants/<tenantId>/data-management-client/client-bdm.zip
 *
 * @author Laurent Leseigneur
 */
class CheckBDMQueries extends MigrationStep {

    def bdmUtil = new BDMUtil()

    @Override
    def execute(MigrationContext context) {
        context.logger.info "Since 7.2.0, new queries named \"countFor*\" are added in BDM to add content-range metadata."
        context.logger.info "For more information, search for BDM/queries in Bonita documentation "
        context.databaseHelper.sql.rows("select t.id,t.name, t.status from tenant t order by t.id").each { row ->
            executeOnTenant(context.logger, context.bonitaHome, row)
        }
    }

    def executeOnTenant(Logger logger, File bonitaHomeFolder, Map tenant) {
        def tenantId = tenant["id"]
        logger.info "analyzing tenant [id:$tenantId, name:${tenant["name"]}, status:${tenant["status"]}]"
        def bdmFile = new File("${bonitaHomeFolder.absolutePath}/engine-server/work/tenants/$tenantId/data-management-client/client-bdm.zip")
        if (bdmFile.exists()) {
            def xmlBDM = bdmUtil.getBomDefinition(bdmFile)
            def nodeList = xmlBDM.businessObjects.businessObject.queries.query
            def potentialConflict = false
            nodeList.each { node ->
                def queryName = node.@name as String
                def boNode = node.parent().parent()
                def boName = boNode.@qualifiedName as String
                if (queryName.startsWith("countFor")) {
                    potentialConflict = true
                    logger.warn "Business Object $boName has a custom query named $queryName. This may conflict with default countFor queries."
                }
            }
            if (potentialConflict) {
                logger.warn "To avoid this risk, those queries should be renamed then the BDM should be redeployed."
                logger.info "Please report this message to BDM administrator. "
            }
        } else {
            logger.info "no BDM found on this tenant"
        }
    }

    @Override
    String getDescription() {
        "Check custom query names for potential conflict if BDM is deployed on tenant"
    }

}
