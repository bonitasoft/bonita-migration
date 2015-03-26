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
ALTER TABLE user_ MODIFY userName VARCHAR2(255 CHAR) @@
ALTER TABLE user_ MODIFY password VARCHAR2(60 CHAR) @@
ALTER TABLE user_ MODIFY firstName VARCHAR2(255 CHAR) @@
ALTER TABLE user_ MODIFY lastName VARCHAR2(255 CHAR) @@
ALTER TABLE user_ MODIFY title VARCHAR2(50 CHAR) @@
ALTER TABLE user_ MODIFY jobTitle VARCHAR2(255 CHAR) @@
ALTER TABLE user_ MODIFY delegeeUserName VARCHAR2(50 CHAR) @@
ALTER TABLE user_ MODIFY iconName VARCHAR2(50 CHAR) @@
ALTER TABLE user_ MODIFY iconPath VARCHAR2(50 CHAR) @@


--
-- User_contact_info
-- 
ALTER TABLE user_contactinfo MODIFY email VARCHAR2(255 CHAR) @@
ALTER TABLE user_contactinfo MODIFY phone VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY mobile VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY fax VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY building VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY room VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY address VARCHAR2(255 CHAR) @@
ALTER TABLE user_contactinfo MODIFY zipCode VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY city VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY state VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY country VARCHAR2(50 CHAR) @@
ALTER TABLE user_contactinfo MODIFY website VARCHAR2(50 CHAR) @@


--
-- Custom_usr_inf_def
-- 
ALTER TABLE custom_usr_inf_def MODIFY name VARCHAR2(75 CHAR) @@
ALTER TABLE custom_usr_inf_def MODIFY description VARCHAR2(1024 CHAR) @@


--
-- Custom_usr_inf_val
-- 
ALTER TABLE custom_usr_inf_val MODIFY value VARCHAR2(255 CHAR) @@
