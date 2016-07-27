/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.migration.versions.v7_0_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep

/**
 * @author Laurent Leseigneur
 */
class PageTableMigration extends DatabaseMigrationStep {


    PageTableMigration(Sql sql, String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    def migrate() {
        if ("oracle".equals(dbVendor)) {
            sql.eachRow("select u.CONSTRAINT_NAME from user_constraints u where upper(TABLE_NAME)='PAGE' and constraint_type='U'") { row ->
                def constraintName = row[0]
                println "removing unique constraint PAGE.${constraintName} (oracle only)"
                sql.execute("ALTER TABLE page DROP CONSTRAINT " + constraintName)
                sql.eachRow("""
            SELECT
                i.INDEX_NAME
            FROM
                user_indexes i
            WHERE
                upper(i.TABLE_NAME) = 'PAGE'
                AND i.INDEX_NAME = $constraintName
            """) { rowIdx ->
                    def indexNameName = rowIdx[0]
                    println "index found : ${indexNameName} on table PAGE (oracle only)"
                    println "removing unique index ${indexNameName} on table PAGE (oracle only)"
                    sql.execute("DROP INDEX " + indexNameName)
                }
            }
        }

        getSqlStatements(dbVendor).split("@@").each {
            if (!it.trim().empty) {
                println "executing SQL statement:\n${it.trim()}"
                sql.execute(it)
            }
        }

    }

    def getSqlStatements(String dbVendor) {
        switch (dbVendor) {
            case "mysql":
                return """
                    CREATE TABLE page2 (
                      tenantId BIGINT NOT NULL,
                      id BIGINT NOT NULL,
                      name VARCHAR(50) NOT NULL,
                      displayName VARCHAR(255) NOT NULL,
                      description TEXT,
                      installationDate BIGINT NOT NULL,
                      installedBy BIGINT NOT NULL,
                      provided BOOLEAN,
                      lastModificationDate BIGINT NOT NULL,
                      lastUpdatedBy BIGINT NOT NULL,
                      contentName VARCHAR(50) NOT NULL,
                      content LONGBLOB,
                      contentType VARCHAR(50) NOT NULL,
                      processDefinitionId BIGINT
                    ) ENGINE = INNODB
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id)
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId)
                    @@

                    ALTER TABLE business_app_page DROP FOREIGN KEY fk_page_id
                    @@

                    INSERT INTO page2(tenantid, id, name, displayName, description, installationDate, installedBy,
                        provided,lastModificationDate,lastUpdatedBy,contentName,content,contentType,processDefinitionId)
                    SELECT tenantid, id, name, displayName, description, installationDate, installedBy,
                               provided,lastModificationDate,lastUpdatedBy,contentName,content,'page',null
                    FROM page
                    @@

                    ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page2 (tenantid, id)
                    @@

                    DROP TABLE page
                    @@

                    RENAME TABLE  page2 TO page
                    @@ """
            case "postgres":
                return """
                    CREATE TABLE page2 (
                      tenantId INT8 NOT NULL,
                      id INT8 NOT NULL,
                      name VARCHAR(50) NOT NULL,
                      displayName VARCHAR(255) NOT NULL,
                      description TEXT,
                      installationDate INT8 NOT NULL,
                      installedBy INT8 NOT NULL,
                      provided BOOLEAN,
                      lastModificationDate INT8 NOT NULL,
                      lastUpdatedBy INT8 NOT NULL,
                      contentName VARCHAR(50) NOT NULL,
                      content BYTEA,
                      contentType VARCHAR(50) NOT NULL,
                      processDefinitionId INT8
                    )
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);
                    @@

                    ALTER TABLE business_app_page DROP CONSTRAINT fk_page_id
                    @@

                    INSERT INTO page2(tenantid, id, name, displayName, description, installationDate, installedBy,
                        provided,lastModificationDate,lastUpdatedBy,contentName,content,contentType,processDefinitionId)
                    SELECT tenantid, id, name, displayName, description, installationDate, installedBy,
                               provided,lastModificationDate,lastUpdatedBy,contentName,content,'page',null
                    FROM page
                    @@

                    ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page2 (tenantid, id)
                    @@

                    DROP TABLE page
                    @@

                    ALTER TABLE page2 RENAME TO page
                    @@"""
            case "oracle":
                return """
                    CREATE TABLE page2 (
                      tenantId NUMBER(19, 0) NOT NULL,
                      id NUMBER(19, 0) NOT NULL,
                      name VARCHAR2(50 CHAR) NOT NULL,
                      displayName VARCHAR2(255 CHAR) NOT NULL,
                      description VARCHAR2(1024 CHAR),
                      installationDate NUMBER(19, 0) NOT NULL,
                      installedBy NUMBER(19, 0) NOT NULL,
                      provided NUMBER(1),
                      lastModificationDate NUMBER(19, 0) NOT NULL,
                      lastUpdatedBy NUMBER(19, 0) NOT NULL,
                      contentName VARCHAR2(50 CHAR) NOT NULL,
                      content BLOB,
                      contentType VARCHAR2(50 CHAR),
                      processDefinitionId NUMBER(19, 0)
                    )
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id)
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId)
                    @@

                    ALTER TABLE business_app_page DROP CONSTRAINT fk_page_id
                    @@

                    INSERT INTO page2(tenantid, id, name, displayName, description, installationDate, installedBy,
                        provided,lastModificationDate,lastUpdatedBy,contentName,content,contentType,processDefinitionId)
                    SELECT tenantid, id, name, displayName, description, installationDate, installedBy,
                               provided,lastModificationDate,lastUpdatedBy,contentName,content,'page',null
                    FROM page
                    @@

                    ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page2 (tenantid, id)
                    @@

                    DROP TABLE page
                    @@

                    RENAME page2 TO page
                    @@"""
            case "sqlserver":
                return """
                    CREATE TABLE page2 (
                      tenantId NUMERIC(19, 0) NOT NULL,
                      id NUMERIC(19, 0) NOT NULL,
                      name NVARCHAR(50) NOT NULL,
                      displayName NVARCHAR(255) NOT NULL,
                      description NVARCHAR(MAX),
                      installationDate NUMERIC(19, 0) NOT NULL,
                      installedBy NUMERIC(19, 0) NOT NULL,
                      provided BIT,
                      lastModificationDate NUMERIC(19, 0) NOT NULL,
                      lastUpdatedBy NUMERIC(19, 0) NOT NULL,
                      contentName NVARCHAR(50) NOT NULL,
                      content VARBINARY(MAX),
                      contentType NVARCHAR(50) NOT NULL,
                      processDefinitionId NUMERIC(19,0)
                    )
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id)
                    @@

                    ALTER TABLE page2 ADD CONSTRAINT  uk_page UNIQUE (tenantId, name, processDefinitionId)
                    @@

                    ALTER TABLE business_app_page DROP CONSTRAINT fk_page_id
                    @@

                    INSERT INTO page2(tenantid, id, name, displayName, description, installationDate, installedBy,
                        provided,lastModificationDate,lastUpdatedBy,contentName,content,contentType,processDefinitionId)
                    SELECT tenantid, id, name, displayName, description, installationDate, installedBy,
                               provided,lastModificationDate,lastUpdatedBy,contentName,content,'page',null
                    FROM page
                    @@

                    ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page2 (tenantid, id)
                    @@

                    DROP TABLE page
                    @@

                    sp_rename page2 , page
                    @@"""
            default:
                throw new IllegalArgumentException("wrong db vendor:" + dbVendor)


        }
    }
}
