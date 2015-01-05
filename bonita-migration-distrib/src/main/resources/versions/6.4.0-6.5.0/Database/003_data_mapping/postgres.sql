DROP INDEX idx_datamapp_container@@
DROP TABLE data_mapping@@
DROP TABLE arch_data_mapping@@
DELETE FROM sequence WHERE id = 10021@@
DELETE FROM sequence WHERE id = 20051@@
DELETE FROM command WHERE name = getUpdatedVariableValuesForProcessDefinition@@
DELETE FROM command WHERE name = getUpdatedVariableValuesForActivityInstance@@
DELETE FROM command WHERE name = getUpdatedVariableValuesForProcessInstance@@