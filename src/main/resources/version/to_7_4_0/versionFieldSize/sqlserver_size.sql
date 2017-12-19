ALTER TABLE arch_document_mapping ALTER COLUMN version VARCHAR(50) NOT NULL
@@
ALTER TABLE document_mapping ALTER COLUMN version VARCHAR(50) NOT NULL
@@
ALTER TABLE arch_connector_instance ALTER COLUMN version VARCHAR(50) NOT NULL
@@
ALTER TABLE connector_instance ALTER COLUMN version VARCHAR(50) NOT NULL