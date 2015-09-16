DROP INDEX idx_datamapp_container on data_mapping@@
DROP TABLE data_mapping@@
DROP TABLE arch_data_mapping@@
DROP INDEX idx_datai_container on data_instance@@
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name)@@
DROP INDEX idx1_arch_data_instance on arch_data_instance@@
CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId)@@
DELETE FROM sequence WHERE id = 10021@@
DELETE FROM sequence WHERE id = 20051@@
DELETE FROM command WHERE name = 'getUpdatedVariableValuesForProcessDefinition'@@
DELETE FROM command WHERE name = 'getUpdatedVariableValuesForActivityInstance'@@
DELETE FROM command WHERE name = 'getUpdatedVariableValuesForProcessInstance'@@