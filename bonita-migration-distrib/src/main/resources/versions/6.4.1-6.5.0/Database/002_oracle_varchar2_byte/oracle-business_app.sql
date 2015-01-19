--
-- Business_app
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE business_app DISABLE CONSTRAINT uk_app_token_version @@
ALTER TABLE business_app DROP CONSTRAINT uk_app_token_version @@

ALTER TABLE business_app ADD token_temp VARCHAR2(50 CHAR) @@
UPDATE business_app SET token_temp = token @@
ALTER TABLE business_app DROP COLUMN token @@
ALTER TABLE business_app RENAME COLUMN token_temp TO token @@
ALTER TABLE business_app MODIFY token NOT NULL @@


ALTER TABLE business_app ADD version_temp VARCHAR2(50 CHAR) @@
UPDATE business_app SET version_temp = version @@
ALTER TABLE business_app DROP COLUMN version @@
ALTER TABLE business_app RENAME COLUMN version_temp TO version @@
ALTER TABLE business_app MODIFY version NOT NULL @@


ALTER TABLE business_app ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE business_app SET description_temp = description @@
ALTER TABLE business_app DROP COLUMN description @@
ALTER TABLE business_app RENAME COLUMN description_temp TO description @@


ALTER TABLE business_app ADD iconPath_temp VARCHAR2(255 CHAR) @@
UPDATE business_app SET iconPath_temp = iconPath @@
ALTER TABLE business_app DROP COLUMN iconPath @@
ALTER TABLE business_app RENAME COLUMN iconPath_temp TO iconPath @@


ALTER TABLE business_app ADD state_temp VARCHAR2(30 CHAR) @@
UPDATE business_app SET state_temp = state @@
ALTER TABLE business_app DROP COLUMN state @@
ALTER TABLE business_app RENAME COLUMN state_temp TO state @@
ALTER TABLE business_app MODIFY state NOT NULL @@


ALTER TABLE business_app ADD displayName_temp VARCHAR2(255 CHAR) @@
UPDATE business_app SET displayName_temp = displayName @@
ALTER TABLE business_app DROP COLUMN displayName @@
ALTER TABLE business_app RENAME COLUMN displayName_temp TO displayName @@
ALTER TABLE business_app MODIFY displayName NOT NULL @@

ALTER TABLE business_app ADD CONSTRAINT UK_Business_app UNIQUE (tenantId, token, version) @@
ALTER TABLE business_app ENABLE CONSTRAINT UK_Business_app @@



--
-- Business_app_page
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE business_app_page DISABLE CONSTRAINT uk_app_page_appId_token @@
ALTER TABLE business_app_page DROP CONSTRAINT uk_app_page_appId_token @@

ALTER TABLE business_app_page ADD token_temp VARCHAR2(255 CHAR) @@
UPDATE business_app_page SET token_temp = token @@
ALTER TABLE business_app_page DROP COLUMN token @@
ALTER TABLE business_app_page RENAME COLUMN token_temp TO token @@
ALTER TABLE business_app_page MODIFY token NOT NULL @@

ALTER TABLE business_app_page ADD CONSTRAINT UK_Business_app_page UNIQUE (tenantId, applicationId, token) @@
ALTER TABLE business_app_page ENABLE CONSTRAINT UK_Business_app_page @@



--
-- Business_app_menu
-- 

ALTER TABLE business_app_menu ADD displayName_temp VARCHAR2(255 CHAR) @@
UPDATE business_app_menu SET displayName_temp = displayName @@
ALTER TABLE business_app_menu DROP COLUMN displayName @@
ALTER TABLE business_app_menu RENAME COLUMN displayName_temp TO displayName @@
ALTER TABLE business_app_menu MODIFY displayName NOT NULL @@