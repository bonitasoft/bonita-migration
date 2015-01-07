DROP INDEX idx_datamapp_container ON data_mapping
@@
DROP TABLE data_mapping
@@
DROP TABLE arch_data_mapping
@@
DROP INDEX idx_datai_container ON data_instance
@@
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name)
@@
DROP INDEX idx1_arch_data_instance ON arch_data_instance
@@
CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId)
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 10021
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 20051
@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForProcessDefinition'
@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForActivityInstance'
@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForProcessInstance'
@@