--
-- Data_instance
-- 

ALTER TABLE data_instance ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE data_instance SET name_temp = name @@
ALTER TABLE data_instance DROP COLUMN name @@
ALTER TABLE data_instance RENAME COLUMN name_temp TO name @@


ALTER TABLE data_instance ADD description_temp VARCHAR2(50 CHAR) @@
UPDATE data_instance SET description_temp = description @@
ALTER TABLE data_instance DROP COLUMN description @@
ALTER TABLE data_instance RENAME COLUMN description_temp TO description @@


ALTER TABLE data_instance ADD className_temp VARCHAR2(100 CHAR) @@
UPDATE data_instance SET className_temp = className @@
ALTER TABLE data_instance DROP COLUMN className @@
ALTER TABLE data_instance RENAME COLUMN className_temp TO className @@


ALTER TABLE data_instance ADD containerType_temp VARCHAR2(60 CHAR) @@
UPDATE data_instance SET containerType_temp = containerType @@
ALTER TABLE data_instance DROP COLUMN containerType @@
ALTER TABLE data_instance RENAME COLUMN containerType_temp TO containerType @@


ALTER TABLE data_instance ADD namespace_temp VARCHAR2(100 CHAR) @@
UPDATE data_instance SET namespace_temp = namespace @@
ALTER TABLE data_instance DROP COLUMN namespace @@
ALTER TABLE data_instance RENAME COLUMN namespace_temp TO namespace @@


ALTER TABLE data_instance ADD element_temp VARCHAR2(60 CHAR) @@
UPDATE data_instance SET element_temp = element @@
ALTER TABLE data_instance DROP COLUMN element @@
ALTER TABLE data_instance RENAME COLUMN element_temp TO element @@


ALTER TABLE data_instance ADD shortTextValue_temp VARCHAR2(255 CHAR) @@
UPDATE data_instance SET shortTextValue_temp = shortTextValue @@
ALTER TABLE data_instance DROP COLUMN shortTextValue @@
ALTER TABLE data_instance RENAME COLUMN shortTextValue_temp TO shortTextValue @@


ALTER TABLE data_instance ADD discriminant_temp VARCHAR2(50 CHAR) @@
UPDATE data_instance SET discriminant_temp = discriminant @@
ALTER TABLE data_instance DROP COLUMN discriminant @@
ALTER TABLE data_instance RENAME COLUMN discriminant_temp TO discriminant @@
ALTER TABLE data_instance MODIFY discriminant NOT NULL @@


--
-- Data_mapping
--
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE data_mapping DISABLE UNIQUE (tenantId, containerId, containerType, dataName) @@
ALTER TABLE data_mapping DROP UNIQUE (tenantId, containerId, containerType, dataName) @@

ALTER TABLE data_mapping ADD containerType_temp VARCHAR2(60 CHAR) @@
UPDATE data_mapping SET containerType_temp = containerType @@
ALTER TABLE data_mapping DROP COLUMN containerType @@
ALTER TABLE data_mapping RENAME COLUMN containerType_temp TO containerType @@


ALTER TABLE data_mapping ADD dataName_temp VARCHAR2(50 CHAR) @@
UPDATE data_mapping SET dataName_temp = dataName @@
ALTER TABLE data_mapping DROP COLUMN dataName @@
ALTER TABLE data_mapping RENAME COLUMN dataName_temp TO dataName @@

ALTER TABLE data_mapping ADD CONSTRAINT UK_Data_mapping UNIQUE (tenantId, containerId, containerType, dataName) @@
ALTER TABLE data_mapping ENABLE CONSTRAINT UK_Data_mapping @@



--
-- Archived_data_instance
-- 

ALTER TABLE arch_data_instance ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE arch_data_instance SET name_temp = name @@
ALTER TABLE arch_data_instance DROP COLUMN name @@
ALTER TABLE arch_data_instance RENAME COLUMN name_temp TO name @@


ALTER TABLE arch_data_instance ADD description_temp VARCHAR2(50 CHAR) @@
UPDATE arch_data_instance SET description_temp = description @@
ALTER TABLE arch_data_instance DROP COLUMN description @@
ALTER TABLE arch_data_instance RENAME COLUMN description_temp TO description @@


ALTER TABLE arch_data_instance ADD className_temp VARCHAR2(100 CHAR) @@
UPDATE arch_data_instance SET className_temp = className @@
ALTER TABLE arch_data_instance DROP COLUMN className @@
ALTER TABLE arch_data_instance RENAME COLUMN className_temp TO className @@


ALTER TABLE arch_data_instance ADD containerType_temp VARCHAR2(60 CHAR) @@
UPDATE arch_data_instance SET containerType_temp = containerType @@
ALTER TABLE arch_data_instance DROP COLUMN containerType @@
ALTER TABLE arch_data_instance RENAME COLUMN containerType_temp TO containerType @@


ALTER TABLE arch_data_instance ADD namespace_temp VARCHAR2(100 CHAR) @@
UPDATE arch_data_instance SET namespace_temp = namespace @@
ALTER TABLE arch_data_instance DROP COLUMN namespace @@
ALTER TABLE arch_data_instance RENAME COLUMN namespace_temp TO namespace @@


ALTER TABLE arch_data_instance ADD element_temp VARCHAR2(60 CHAR) @@
UPDATE arch_data_instance SET element_temp = element @@
ALTER TABLE arch_data_instance DROP COLUMN element @@
ALTER TABLE arch_data_instance RENAME COLUMN element_temp TO element @@


ALTER TABLE arch_data_instance ADD shortTextValue_temp VARCHAR2(255 CHAR) @@
UPDATE arch_data_instance SET shortTextValue_temp = shortTextValue @@
ALTER TABLE arch_data_instance DROP COLUMN shortTextValue @@
ALTER TABLE arch_data_instance RENAME COLUMN shortTextValue_temp TO shortTextValue @@


ALTER TABLE arch_data_instance ADD discriminant_temp VARCHAR2(50 CHAR) @@
UPDATE arch_data_instance SET discriminant_temp = discriminant @@
ALTER TABLE arch_data_instance DROP COLUMN discriminant @@
ALTER TABLE arch_data_instance RENAME COLUMN discriminant_temp TO discriminant @@
ALTER TABLE arch_data_instance MODIFY discriminant NOT NULL @@


--
-- Archived_data_mapping
--

ALTER TABLE arch_data_mapping ADD containerType_temp VARCHAR2(60 CHAR) @@
UPDATE arch_data_mapping SET containerType_temp = containerType @@
ALTER TABLE arch_data_mapping DROP COLUMN containerType @@
ALTER TABLE arch_data_mapping RENAME COLUMN containerType_temp TO containerType @@


ALTER TABLE arch_data_mapping ADD dataName_temp VARCHAR2(50 CHAR) @@
UPDATE arch_data_mapping SET dataName_temp = dataName @@
ALTER TABLE arch_data_mapping DROP COLUMN dataName @@
ALTER TABLE arch_data_mapping RENAME COLUMN dataName_temp TO dataName @@