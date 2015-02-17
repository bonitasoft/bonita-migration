--
-- Document
-- 

ALTER TABLE document ADD filename_temp VARCHAR2(255 CHAR) @@
UPDATE document SET filename_temp = filename @@
ALTER TABLE document DROP COLUMN filename @@
ALTER TABLE document RENAME COLUMN filename_temp TO filename @@


ALTER TABLE document ADD mimetype_temp VARCHAR2(255 CHAR) @@
UPDATE document SET mimetype_temp = mimetype @@
ALTER TABLE document DROP COLUMN mimetype @@
ALTER TABLE document RENAME COLUMN mimetype_temp TO mimetype @@


ALTER TABLE document ADD url_temp VARCHAR2(1024 CHAR) @@
UPDATE document SET url_temp = url @@
ALTER TABLE document DROP COLUMN url @@
ALTER TABLE document RENAME COLUMN url_temp TO url @@


--
-- Document_mapping
-- 

ALTER TABLE document_mapping ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE document_mapping SET name_temp = name @@
ALTER TABLE document_mapping DROP COLUMN name @@
ALTER TABLE document_mapping RENAME COLUMN name_temp TO name @@
ALTER TABLE document_mapping MODIFY name NOT NULL @@


ALTER TABLE document_mapping ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE document_mapping SET description_temp = description @@
ALTER TABLE document_mapping DROP COLUMN description @@
ALTER TABLE document_mapping RENAME COLUMN description_temp TO description @@


ALTER TABLE document_mapping ADD version_temp VARCHAR2(10 CHAR) @@
UPDATE document_mapping SET version_temp = version @@
ALTER TABLE document_mapping DROP COLUMN version @@
ALTER TABLE document_mapping RENAME COLUMN version_temp TO version @@
ALTER TABLE document_mapping MODIFY version NOT NULL @@


--
-- Archived_document_mapping
-- 

ALTER TABLE arch_document_mapping ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE arch_document_mapping SET name_temp = name @@
ALTER TABLE arch_document_mapping DROP COLUMN name @@
ALTER TABLE arch_document_mapping RENAME COLUMN name_temp TO name @@
ALTER TABLE arch_document_mapping MODIFY name NOT NULL @@


ALTER TABLE arch_document_mapping ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE arch_document_mapping SET description_temp = description @@
ALTER TABLE arch_document_mapping DROP COLUMN description @@
ALTER TABLE arch_document_mapping RENAME COLUMN description_temp TO description @@


ALTER TABLE arch_document_mapping ADD version_temp VARCHAR2(10 CHAR) @@
UPDATE arch_document_mapping SET version_temp = version @@
ALTER TABLE arch_document_mapping DROP COLUMN version @@
ALTER TABLE arch_document_mapping RENAME COLUMN version_temp TO version @@
ALTER TABLE arch_document_mapping MODIFY version NOT NULL @@
