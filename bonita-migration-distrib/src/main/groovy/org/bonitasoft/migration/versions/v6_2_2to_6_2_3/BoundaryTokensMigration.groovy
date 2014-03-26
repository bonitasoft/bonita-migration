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
package org.bonitasoft.migration.versions.v6_2_2to_6_2_3
import groovy.sql.Sql

import org.bonitasoft.migration.core.MigrationUtil

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class BoundaryTokensMigration {

    def Map nextIds;

    public migrate(Sql sql) {
        def tokenSequenceId = 10110;
        nextIds = MigrationUtil.getNexIdsForTable(sql, tokenSequenceId);
        insertTokens(sql);
        MigrationUtil.updateNextIdsForTable(sql, tokenSequenceId, nextIds);
        updateTokenRefIds(sql);
    }

    private insertTokens(Sql sql) {
        println "Creating tokens for boundary events..."
        def int tokenCount = 0;
        sql.eachRow("SELECT * FROM flownode_instance WHERE kind = 'boundaryEvent' AND stateId IN (33, 10, 65)") { row ->
            tokenCount++;
            def TokenInfo tokenInfo
            def boolean interrupting = row.interrupting;
            if(interrupting) {
                tokenInfo = getTokenInfoForInterruptingBoundary(sql, row.tenantid, row.logicalGroup4, getTokenRefIdFromRelatedActivityInstance(sql, row.tenantid, row.activityInstanceId))
            } else {
                tokenInfo = new TokenInfo(tokenRefId:row.id);
            }
            insertToken(sql, row.tenantid, row.logicalGroup4, tokenInfo);
        }
        println "$tokenCount tokens were created."
    }

    private updateTokenRefIds(Sql sql) {
        println "Updating token ref id for completed boundary events not yet archived..."
        def int tokenCountRefCount = 0;
        sql.eachRow("SELECT * FROM flownode_instance WHERE kind = 'boundaryEvent' AND stateId = 2") { row ->
            tokenCountRefCount++;
            def boolean interrupting = row.interrupting;
            if(!interrupting) {
                updateTokenRefId(sql, row.tenantid, row.id)
            }
        }
        println "$tokenCountRefCount token ref ids were updated."
    }

    private updateTokenRefId(Sql sql, tenantId, boundaryEventId) {
        //the token ref id used to create the token was the boundary event id
        sql.executeUpdate("UPDATE flownode_instance SET token_ref_id = $boundaryEventId WHERE tenantId = $tenantId and id = $boundaryEventId");
    }

    private insertToken(Sql sql, tenantId, processInstanceId, TokenInfo tokenInfo) {
        def id = nextIds.get(tenantId);
        nextIds.put(tenantId, id +1);
        String tokenInsert = """
            INSERT INTO token (tenantid, id, processInstanceId, ref_id, parent_ref_id)
            VALUES (
            $tenantId,
            $id,
            $processInstanceId,
            $tokenInfo.tokenRefId,
            $tokenInfo.parentTokenRefId
            )
        """
        sql.executeInsert(tokenInsert);
    }

    private TokenInfo getTokenInfoForInterruptingBoundary(Sql sql, tenantId, processInstanceId, tokenRefId) {
        def TokenInfo tokenInfo = null;
        sql.eachRow("SELECT * FROM token WHERE tenantid = $tenantId AND processInstanceId = $processInstanceId AND ref_id = $tokenRefId") { row ->
            tokenInfo = new TokenInfo(tokenRefId:row.ref_id, parentTokenRefId:row.parent_ref_id);
        }
        return tokenInfo;
    }

    private getTokenRefIdFromRelatedActivityInstance(Sql sql, tenantId, activityInstanceId) {
        def tokenRefId;
        sql.eachRow("SELECT token_ref_id FROM flownode_instance WHERE tenantid = $tenantId AND id = $activityInstanceId") { row ->
            tokenRefId = row[0];
        }
        return tokenRefId;
    }
}
