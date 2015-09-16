--
-- Profile
-- 
ALTER TABLE profile MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE profile MODIFY description VARCHAR2(1024 CHAR) @@


--
-- Profile_entry
-- 
ALTER TABLE profileentry MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE profileentry MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE profileentry MODIFY type VARCHAR2(50 CHAR) @@
ALTER TABLE profileentry MODIFY page VARCHAR2(50 CHAR) @@
