--
-- Business_app
-- 
ALTER TABLE business_app MODIFY token VARCHAR2(50 CHAR) @@
ALTER TABLE business_app MODIFY version VARCHAR2(50 CHAR) @@
ALTER TABLE business_app MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE business_app MODIFY iconPath VARCHAR2(255 CHAR) @@
ALTER TABLE business_app MODIFY state VARCHAR2(30 CHAR) @@
ALTER TABLE business_app MODIFY displayName VARCHAR2(255 CHAR) @@


--
-- Business_app_page
-- 
ALTER TABLE business_app_page MODIFY token VARCHAR2(255 CHAR) @@


--
-- Business_app_menu
-- 

ALTER TABLE business_app_menu MODIFY displayName VARCHAR2(255 CHAR) @@
