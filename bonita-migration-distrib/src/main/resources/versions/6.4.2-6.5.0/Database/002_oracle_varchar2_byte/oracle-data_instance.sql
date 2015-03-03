--
-- Data_instance
-- 

ALTER TABLE data_instance MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE data_instance MODIFY description VARCHAR2(50 CHAR) @@
ALTER TABLE data_instance MODIFY className VARCHAR2(100 CHAR) @@
ALTER TABLE data_instance MODIFY containerType VARCHAR2(60 CHAR) @@
ALTER TABLE data_instance MODIFY element VARCHAR2(60 CHAR) @@
ALTER TABLE data_instance MODIFY namespace VARCHAR2(100 CHAR) @@
ALTER TABLE data_instance MODIFY shortTextValue VARCHAR2(255 CHAR) @@
ALTER TABLE data_instance MODIFY discriminant VARCHAR2(50 CHAR) @@


--
-- Data_mapping
--
ALTER TABLE data_mapping MODIFY containerType VARCHAR2(60 CHAR) @@
ALTER TABLE data_mapping MODIFY dataName VARCHAR2(50 CHAR) @@


--
-- Archived_data_instance
-- 
ALTER TABLE arch_data_instance MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE arch_data_instance MODIFY description VARCHAR2(50 CHAR) @@
ALTER TABLE arch_data_instance MODIFY className VARCHAR2(100 CHAR) @@
ALTER TABLE arch_data_instance MODIFY containerType VARCHAR2(60 CHAR) @@
ALTER TABLE arch_data_instance MODIFY namespace VARCHAR2(100 CHAR) @@
ALTER TABLE arch_data_instance MODIFY element VARCHAR2(60 CHAR) @@
ALTER TABLE arch_data_instance MODIFY shortTextValue VARCHAR2(255 CHAR) @@
ALTER TABLE arch_data_instance MODIFY discriminant VARCHAR2(50 CHAR) @@


--
-- Archived_data_mapping
--
ALTER TABLE arch_data_mapping MODIFY containerType VARCHAR2(60 CHAR) @@
ALTER TABLE arch_data_mapping MODIFY dataName VARCHAR2(50 CHAR) @@
