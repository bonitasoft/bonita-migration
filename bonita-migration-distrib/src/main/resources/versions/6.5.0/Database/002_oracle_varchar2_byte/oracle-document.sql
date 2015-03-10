--
-- Document
-- 

ALTER TABLE document MODIFY filename VARCHAR2(255 CHAR) @@
ALTER TABLE document MODIFY mimetype VARCHAR2(255 CHAR) @@
ALTER TABLE document MODIFY url VARCHAR2(1024 CHAR) @@

--
-- Document_mapping
-- 
ALTER TABLE document_mapping MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE document_mapping MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE document_mapping MODIFY version VARCHAR2(10 CHAR) @@

--
-- Archived_document_mapping
-- 
ALTER TABLE arch_document_mapping MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE arch_document_mapping MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE arch_document_mapping MODIFY version VARCHAR2(10 CHAR) @@
