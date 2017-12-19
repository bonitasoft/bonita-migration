ALTER TABLE arch_document_mapping MODIFY COLUMN version VARCHAR(50) NOT NULL
@@
ALTER TABLE document_mapping MODIFY COLUMN version VARCHAR(50) NOT NULL
@@
ALTER TABLE arch_connector_instance MODIFY COLUMN version VARCHAR(50) NOT NULL
@@
ALTER TABLE connector_instance MODIFY COLUMN version VARCHAR(50) NOT NULL