--
-- External_identity_mapping
-- 
ALTER TABLE external_identity_mapping MODIFY kind VARCHAR2(25 CHAR) @@
ALTER TABLE external_identity_mapping MODIFY externalId VARCHAR2(50 CHAR) @@


--
-- Group
-- 
ALTER TABLE group_ MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE group_ MODIFY parentPath VARCHAR2(255 CHAR) @@
ALTER TABLE group_ MODIFY displayName VARCHAR2(75 CHAR) @@
ALTER TABLE group_ MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE group_ MODIFY iconName VARCHAR2(50 CHAR) @@
ALTER TABLE group_ MODIFY iconPath VARCHAR2(50 CHAR) @@


--
-- Role
-- 
ALTER TABLE role MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE role MODIFY displayName VARCHAR2(75 CHAR) @@
ALTER TABLE role MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE role MODIFY iconName VARCHAR2(50 CHAR) @@
ALTER TABLE role MODIFY iconPath VARCHAR2(50 CHAR) @@



--
-- User
--
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE user_ DISABLE UNIQUE (tenantId, userName) @@
ALTER TABLE user_ DROP UNIQUE (tenantId, userName) @@ 

ALTER TABLE user_ MODIFY userName VARCHAR2(255 CHAR) @@
UPDATE user_ SET userName_temp = userName @@
ALTER TABLE user_ DROP COLUMN userName @@
ALTER TABLE user_ RENAME COLUMN userName_temp TO userName @@
ALTER TABLE user_ MODIFY userName NOT NULL @@


ALTER TABLE user_ MODIFY password VARCHAR2(60 CHAR) @@
UPDATE user_ SET password_temp = password @@
ALTER TABLE user_ DROP COLUMN password @@
ALTER TABLE user_ RENAME COLUMN password_temp TO password @@


ALTER TABLE user_ MODIFY firstName VARCHAR2(255 CHAR) @@
UPDATE user_ SET firstName_temp = firstName @@
ALTER TABLE user_ DROP COLUMN firstName @@
ALTER TABLE user_ RENAME COLUMN firstName_temp TO firstName @@


ALTER TABLE user_ MODIFY lastName VARCHAR2(255 CHAR) @@
UPDATE user_ SET lastName_temp = lastName @@
ALTER TABLE user_ DROP COLUMN lastName @@
ALTER TABLE user_ RENAME COLUMN lastName_temp TO lastName @@


ALTER TABLE user_ MODIFY title VARCHAR2(50 CHAR) @@
UPDATE user_ SET title_temp = title @@
ALTER TABLE user_ DROP COLUMN title @@
ALTER TABLE user_ RENAME COLUMN title_temp TO title @@


ALTER TABLE user_ MODIFY jobTitle VARCHAR2(255 CHAR) @@
UPDATE user_ SET jobTitle_temp = jobTitle @@
ALTER TABLE user_ DROP COLUMN jobTitle @@
ALTER TABLE user_ RENAME COLUMN jobTitle_temp TO jobTitle @@


ALTER TABLE user_ MODIFY delegeeUserName VARCHAR2(50 CHAR) @@
UPDATE user_ SET delegeeUserName_temp = delegeeUserName @@
ALTER TABLE user_ DROP COLUMN delegeeUserName @@
ALTER TABLE user_ RENAME COLUMN delegeeUserName_temp TO delegeeUserName @@


ALTER TABLE user_ MODIFY iconName VARCHAR2(50 CHAR) @@
UPDATE user_ SET iconName_temp = iconName @@
ALTER TABLE user_ DROP COLUMN iconName @@
ALTER TABLE user_ RENAME COLUMN iconName_temp TO iconName @@


ALTER TABLE user_ MODIFY iconPath VARCHAR2(50 CHAR) @@
UPDATE user_ SET iconPath_temp = iconPath @@
ALTER TABLE user_ DROP COLUMN iconPath @@
ALTER TABLE user_ RENAME COLUMN iconPath_temp TO iconPath @@

ALTER TABLE user_ ADD CONSTRAINT UK_User UNIQUE (tenantId, userName) @@
ALTER TABLE user_ ENABLE CONSTRAINT UK_User @@



--
-- User_contact_info
-- 

ALTER TABLE user_contactinfo MODIFY email VARCHAR2(255 CHAR) @@
UPDATE user_contactinfo SET email_temp = email @@
ALTER TABLE user_contactinfo DROP COLUMN email @@
ALTER TABLE user_contactinfo RENAME COLUMN email_temp TO email @@


ALTER TABLE user_contactinfo MODIFY phone VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET phone_temp = phone @@
ALTER TABLE user_contactinfo DROP COLUMN phone @@
ALTER TABLE user_contactinfo RENAME COLUMN phone_temp TO phone @@


ALTER TABLE user_contactinfo MODIFY mobile VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET mobile_temp = mobile @@
ALTER TABLE user_contactinfo DROP COLUMN mobile @@
ALTER TABLE user_contactinfo RENAME COLUMN mobile_temp TO mobile @@


ALTER TABLE user_contactinfo MODIFY fax VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET fax_temp = fax @@
ALTER TABLE user_contactinfo DROP COLUMN fax @@
ALTER TABLE user_contactinfo RENAME COLUMN fax_temp TO fax @@


ALTER TABLE user_contactinfo MODIFY building VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET building_temp = building @@
ALTER TABLE user_contactinfo DROP COLUMN building @@
ALTER TABLE user_contactinfo RENAME COLUMN building_temp TO building @@


ALTER TABLE user_contactinfo MODIFY room VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET room_temp = room @@
ALTER TABLE user_contactinfo DROP COLUMN room @@
ALTER TABLE user_contactinfo RENAME COLUMN room_temp TO room @@


ALTER TABLE user_contactinfo MODIFY address VARCHAR2(255 CHAR) @@
UPDATE user_contactinfo SET address_temp = address @@
ALTER TABLE user_contactinfo DROP COLUMN address @@
ALTER TABLE user_contactinfo RENAME COLUMN address_temp TO address @@


ALTER TABLE user_contactinfo MODIFY zipCode VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET zipCode_temp = zipCode @@
ALTER TABLE user_contactinfo DROP COLUMN zipCode @@
ALTER TABLE user_contactinfo RENAME COLUMN zipCode_temp TO zipCode @@


ALTER TABLE user_contactinfo MODIFY city VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET city_temp = city @@
ALTER TABLE user_contactinfo DROP COLUMN city @@
ALTER TABLE user_contactinfo RENAME COLUMN city_temp TO city @@


ALTER TABLE user_contactinfo MODIFY state VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET state_temp = state @@
ALTER TABLE user_contactinfo DROP COLUMN state @@
ALTER TABLE user_contactinfo RENAME COLUMN state_temp TO state @@


ALTER TABLE user_contactinfo MODIFY country VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET country_temp = country @@
ALTER TABLE user_contactinfo DROP COLUMN country @@
ALTER TABLE user_contactinfo RENAME COLUMN country_temp TO country @@


ALTER TABLE user_contactinfo MODIFY website VARCHAR2(50 CHAR) @@
UPDATE user_contactinfo SET website_temp = website @@
ALTER TABLE user_contactinfo DROP COLUMN website @@
ALTER TABLE user_contactinfo RENAME COLUMN website_temp TO website @@



--
-- Custom_usr_inf_def
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE custom_usr_inf_def DISABLE UNIQUE (tenantId, name) @@
ALTER TABLE custom_usr_inf_def DROP UNIQUE (tenantId, name) @@ 

ALTER TABLE custom_usr_inf_def MODIFY name VARCHAR2(75 CHAR) @@
UPDATE custom_usr_inf_def SET name_temp = name @@
ALTER TABLE custom_usr_inf_def DROP COLUMN name @@
ALTER TABLE custom_usr_inf_def RENAME COLUMN name_temp TO name @@
ALTER TABLE custom_usr_inf_def MODIFY name NOT NULL @@


ALTER TABLE custom_usr_inf_def MODIFY description VARCHAR2(1024 CHAR) @@
UPDATE custom_usr_inf_def SET description_temp = description @@
ALTER TABLE custom_usr_inf_def DROP COLUMN description @@
ALTER TABLE custom_usr_inf_def RENAME COLUMN description_temp TO description @@

ALTER TABLE custom_usr_inf_def ADD CONSTRAINT UK_Custom_Usr_Inf_Def UNIQUE (tenantId, name) @@
ALTER TABLE custom_usr_inf_def ENABLE CONSTRAINT UK_Custom_Usr_Inf_Def @@



--
-- Custom_usr_inf_val
-- 

ALTER TABLE custom_usr_inf_val MODIFY value VARCHAR2(255 CHAR) @@
UPDATE custom_usr_inf_val SET value_temp = value @@
ALTER TABLE custom_usr_inf_val DROP COLUMN value @@
ALTER TABLE custom_usr_inf_val RENAME COLUMN value_temp TO value @@
