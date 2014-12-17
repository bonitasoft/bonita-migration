--
-- External_identity_mapping
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE external_identity_mapping DISABLE UNIQUE (tenantid, kind, externalId, userId, groupId, roleId) @@
ALTER TABLE external_identity_mapping DROP UNIQUE (tenantid, kind, externalId, userId, groupId, roleId) @@

ALTER TABLE external_identity_mapping ADD kind_temp VARCHAR2(25 CHAR) @@
UPDATE external_identity_mapping SET kind_temp = kind @@
ALTER TABLE external_identity_mapping DROP COLUMN kind @@
ALTER TABLE external_identity_mapping RENAME COLUMN kind_temp TO kind @@
ALTER TABLE external_identity_mapping MODIFY kind NOT NULL @@


ALTER TABLE external_identity_mapping ADD externalId_temp VARCHAR2(50 CHAR) @@
UPDATE external_identity_mapping SET externalId_temp = externalId @@
ALTER TABLE external_identity_mapping DROP COLUMN externalId @@
ALTER TABLE external_identity_mapping RENAME COLUMN externalId_temp TO externalId @@
ALTER TABLE external_identity_mapping MODIFY externalId NOT NULL @@

ALTER TABLE external_identity_mapping ADD CONSTRAINT UK_External_Identity_Mapping UNIQUE (tenantid, kind, externalId, userId, groupId, roleId) @@
ALTER TABLE external_identity_mapping ENABLE CONSTRAINT UK_External_Identity_Mapping @@



--
-- Group
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE group_ DISABLE UNIQUE (tenantid, parentPath, name) @@
ALTER TABLE group_ DROP UNIQUE (tenantid, parentPath, name) @@

ALTER TABLE group_ ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE group_ SET name_temp = name @@
ALTER TABLE group_ DROP COLUMN name @@
ALTER TABLE group_ RENAME COLUMN name_temp TO name @@
ALTER TABLE group_ MODIFY name NOT NULL @@


ALTER TABLE group_ ADD parentPath_temp VARCHAR2(255 CHAR) @@
UPDATE group_ SET parentPath_temp = parentPath @@
ALTER TABLE group_ DROP COLUMN parentPath @@
ALTER TABLE group_ RENAME COLUMN parentPath_temp TO parentPath @@


ALTER TABLE group_ ADD displayName_temp VARCHAR2(75 CHAR) @@
UPDATE group_ SET displayName_temp = displayName @@
ALTER TABLE group_ DROP COLUMN displayName @@
ALTER TABLE group_ RENAME COLUMN displayName_temp TO displayName @@


ALTER TABLE group_ ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE group_ SET description_temp = description @@
ALTER TABLE group_ DROP COLUMN description @@
ALTER TABLE group_ RENAME COLUMN description_temp TO description @@


ALTER TABLE group_ ADD iconName_temp VARCHAR2(50 CHAR) @@
UPDATE group_ SET iconName_temp = iconName @@
ALTER TABLE group_ DROP COLUMN iconName @@
ALTER TABLE group_ RENAME COLUMN iconName_temp TO iconName @@


ALTER TABLE group_ ADD iconPath_temp VARCHAR2(50 CHAR) @@
UPDATE group_ SET iconPath_temp = iconPath @@
ALTER TABLE group_ DROP COLUMN iconPath @@
ALTER TABLE group_ RENAME COLUMN iconPath_temp TO iconPath @@

ALTER TABLE group_ ADD CONSTRAINT UK_Group UNIQUE (tenantid, parentPath, name) @@
ALTER TABLE group_ ENABLE CONSTRAINT UK_Group @@



--
-- Role
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE role DISABLE UNIQUE (tenantId, name) @@
ALTER TABLE role DROP UNIQUE (tenantId, name) @@

ALTER TABLE role ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE role SET name_temp = name @@
ALTER TABLE role DROP COLUMN name @@
ALTER TABLE role RENAME COLUMN name_temp TO name @@
ALTER TABLE role MODIFY name NOT NULL @@


ALTER TABLE role ADD displayName_temp VARCHAR2(75 CHAR) @@
UPDATE role SET displayName_temp = displayName @@
ALTER TABLE role DROP COLUMN displayName @@
ALTER TABLE role RENAME COLUMN displayName_temp TO displayName @@


ALTER TABLE role ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE role SET description_temp = description @@
ALTER TABLE role DROP COLUMN description @@
ALTER TABLE role RENAME COLUMN description_temp TO description @@


ALTER TABLE role ADD iconName_temp VARCHAR2(50 CHAR) @@
UPDATE role SET iconName_temp = iconName @@
ALTER TABLE role DROP COLUMN iconName @@
ALTER TABLE role RENAME COLUMN iconName_temp TO iconName @@


ALTER TABLE role ADD iconPath_temp VARCHAR2(50 CHAR) @@
UPDATE role SET iconPath_temp = iconPath @@
ALTER TABLE role DROP COLUMN iconPath @@
ALTER TABLE role RENAME COLUMN iconPath_temp TO iconPath @@

ALTER TABLE role ADD CONSTRAINT UK_Role UNIQUE (tenantId, name) @@
ALTER TABLE role ENABLE CONSTRAINT UK_Role @@



--
-- User
--
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE user_ DISABLE UNIQUE (tenantId, userName) @@
ALTER TABLE user_ DROP UNIQUE (tenantId, userName) @@ 

ALTER TABLE user_ ADD userName_temp VARCHAR2(255 CHAR) @@
UPDATE user_ SET userName_temp = userName @@
ALTER TABLE user_ DROP COLUMN userName @@
ALTER TABLE user_ RENAME COLUMN userName_temp TO userName @@
ALTER TABLE user_ MODIFY userName NOT NULL @@


ALTER TABLE user_ ADD password_temp VARCHAR2(60 CHAR) @@
UPDATE user_ SET password_temp = password @@
ALTER TABLE user_ DROP COLUMN password @@
ALTER TABLE user_ RENAME COLUMN password_temp TO password @@


ALTER TABLE user_ ADD firstName_temp VARCHAR2(255 CHAR) @@
UPDATE user_ SET firstName_temp = firstName @@
ALTER TABLE user_ DROP COLUMN firstName @@
ALTER TABLE user_ RENAME COLUMN firstName_temp TO firstName @@


ALTER TABLE user_ ADD lastName_temp VARCHAR2(255 CHAR) @@
UPDATE user_ SET lastName_temp = lastName @@
ALTER TABLE user_ DROP COLUMN lastName @@
ALTER TABLE user_ RENAME COLUMN lastName_temp TO lastName @@


ALTER TABLE user_ ADD title_temp VARCHAR2(50 CHAR) @@
UPDATE user_ SET title_temp = title @@
ALTER TABLE user_ DROP COLUMN title @@
ALTER TABLE user_ RENAME COLUMN title_temp TO title @@


ALTER TABLE user_ ADD jobTitle_temp VARCHAR2(255 CHAR) @@
UPDATE user_ SET jobTitle_temp = jobTitle @@
ALTER TABLE user_ DROP COLUMN jobTitle @@
ALTER TABLE user_ RENAME COLUMN jobTitle_temp TO jobTitle @@


ALTER TABLE user_ ADD delegeeUserName_temp VARCHAR2(50 CHAR) @@
UPDATE user_ SET delegeeUserName_temp = delegeeUserName @@
ALTER TABLE user_ DROP COLUMN delegeeUserName @@
ALTER TABLE user_ RENAME COLUMN delegeeUserName_temp TO delegeeUserName @@


ALTER TABLE user_ ADD iconName_temp VARCHAR2(50 CHAR) @@
UPDATE user_ SET iconName_temp = iconName @@
ALTER TABLE user_ DROP COLUMN iconName @@
ALTER TABLE user_ RENAME COLUMN iconName_temp TO iconName @@


ALTER TABLE user_ ADD iconPath_temp VARCHAR2(50 CHAR) @@
UPDATE user_ SET iconPath_temp = iconPath @@
ALTER TABLE user_ DROP COLUMN iconPath @@
ALTER TABLE user_ RENAME COLUMN iconPath_temp TO iconPath @@

ALTER TABLE user_ ADD CONSTRAINT UK_User UNIQUE (tenantId, userName) @@
ALTER TABLE user_ ENABLE CONSTRAINT UK_User @@



--
-- User_contact_info
-- 

ALTER TABLE user_contactinfo ADD email_temp VARCHAR2(255 CHAR) @@
UPDATE user_contactinfo SET email_temp = email @@
ALTER TABLE user_contactinfo DROP COLUMN email @@
ALTER TABLE user_contactinfo RENAME COLUMN email_temp TO email @@


ALTER TABLE user_contactinfo ADD phone_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET phone_temp = phone @@
ALTER TABLE user_contactinfo DROP COLUMN phone @@
ALTER TABLE user_contactinfo RENAME COLUMN phone_temp TO phone @@


ALTER TABLE user_contactinfo ADD mobile_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET mobile_temp = mobile @@
ALTER TABLE user_contactinfo DROP COLUMN mobile @@
ALTER TABLE user_contactinfo RENAME COLUMN mobile_temp TO mobile @@


ALTER TABLE user_contactinfo ADD fax_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET fax_temp = fax @@
ALTER TABLE user_contactinfo DROP COLUMN fax @@
ALTER TABLE user_contactinfo RENAME COLUMN fax_temp TO fax @@


ALTER TABLE user_contactinfo ADD building_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET building_temp = building @@
ALTER TABLE user_contactinfo DROP COLUMN building @@
ALTER TABLE user_contactinfo RENAME COLUMN building_temp TO building @@


ALTER TABLE user_contactinfo ADD room_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET room_temp = room @@
ALTER TABLE user_contactinfo DROP COLUMN room @@
ALTER TABLE user_contactinfo RENAME COLUMN room_temp TO room @@


ALTER TABLE user_contactinfo ADD address_temp VARCHAR2(255 CHAR) @@
UPDATE user_contactinfo SET address_temp = address @@
ALTER TABLE user_contactinfo DROP COLUMN address @@
ALTER TABLE user_contactinfo RENAME COLUMN address_temp TO address @@


ALTER TABLE user_contactinfo ADD zipCode_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET zipCode_temp = zipCode @@
ALTER TABLE user_contactinfo DROP COLUMN zipCode @@
ALTER TABLE user_contactinfo RENAME COLUMN zipCode_temp TO zipCode @@


ALTER TABLE user_contactinfo ADD city_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET city_temp = city @@
ALTER TABLE user_contactinfo DROP COLUMN city @@
ALTER TABLE user_contactinfo RENAME COLUMN city_temp TO city @@


ALTER TABLE user_contactinfo ADD state_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET state_temp = state @@
ALTER TABLE user_contactinfo DROP COLUMN state @@
ALTER TABLE user_contactinfo RENAME COLUMN state_temp TO state @@


ALTER TABLE user_contactinfo ADD country_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET country_temp = country @@
ALTER TABLE user_contactinfo DROP COLUMN country @@
ALTER TABLE user_contactinfo RENAME COLUMN country_temp TO country @@


ALTER TABLE user_contactinfo ADD website_temp VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET website_temp = website @@
ALTER TABLE user_contactinfo DROP COLUMN website @@
ALTER TABLE user_contactinfo RENAME COLUMN website_temp TO website @@



--
-- Custom_usr_inf_def
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE custom_usr_inf_def DISABLE UNIQUE (tenantId, name) @@
ALTER TABLE custom_usr_inf_def DROP UNIQUE (tenantId, name) @@ 

ALTER TABLE custom_usr_inf_def ADD name_temp VARCHAR2(75 CHAR) @@
UPDATE custom_usr_inf_def SET name_temp = name @@
ALTER TABLE custom_usr_inf_def DROP COLUMN name @@
ALTER TABLE custom_usr_inf_def RENAME COLUMN name_temp TO name @@
ALTER TABLE custom_usr_inf_def MODIFY name NOT NULL @@


ALTER TABLE custom_usr_inf_def ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE custom_usr_inf_def SET description_temp = description @@
ALTER TABLE custom_usr_inf_def DROP COLUMN description @@
ALTER TABLE custom_usr_inf_def RENAME COLUMN description_temp TO description @@

ALTER TABLE custom_usr_inf_def ADD CONSTRAINT UK_Custom_Usr_Inf_Def UNIQUE (tenantId, name) @@
ALTER TABLE custom_usr_inf_def ENABLE CONSTRAINT UK_Custom_Usr_Inf_Def @@



--
-- Custom_usr_inf_val
-- 

ALTER TABLE custom_usr_inf_val ADD value_temp VARCHAR2(255 CHAR) @@
UPDATE custom_usr_inf_val SET value_temp = value @@
ALTER TABLE custom_usr_inf_val DROP COLUMN value @@
ALTER TABLE custom_usr_inf_val RENAME COLUMN value_temp TO value @@
