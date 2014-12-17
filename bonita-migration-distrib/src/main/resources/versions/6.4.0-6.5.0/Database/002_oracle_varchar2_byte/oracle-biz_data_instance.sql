--
-- Ref_biz_data_inst
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE ref_biz_data_inst DISABLE UNIQUE (name, proc_inst_id, fn_inst_id, tenantId) @@
ALTER TABLE ref_biz_data_inst DROP UNIQUE (name, proc_inst_id, fn_inst_id, tenantId) @@

ALTER TABLE ref_biz_data_inst ADD kind_temp VARCHAR2(15 CHAR) @@
UPDATE ref_biz_data_inst SET kind_temp = kind @@
ALTER TABLE ref_biz_data_inst DROP COLUMN kind @@
ALTER TABLE ref_biz_data_inst RENAME COLUMN kind_temp TO kind @@
ALTER TABLE ref_biz_data_inst MODIFY kind NOT NULL @@


ALTER TABLE ref_biz_data_inst ADD name_temp VARCHAR2(255 CHAR) @@
UPDATE ref_biz_data_inst SET name_temp = name @@
ALTER TABLE ref_biz_data_inst DROP COLUMN name @@
ALTER TABLE ref_biz_data_inst RENAME COLUMN name_temp TO name @@
ALTER TABLE ref_biz_data_inst MODIFY name NOT NULL @@


ALTER TABLE ref_biz_data_inst ADD data_classname_temp VARCHAR2(255 CHAR) @@
UPDATE ref_biz_data_inst SET data_classname_temp = data_classname @@
ALTER TABLE ref_biz_data_inst DROP COLUMN data_classname @@
ALTER TABLE ref_biz_data_inst RENAME COLUMN data_classname_temp TO data_classname @@
ALTER TABLE ref_biz_data_inst MODIFY data_classname NOT NULL @@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT UK_Ref_Biz_Data_Inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantId) @@
ALTER TABLE ref_biz_data_inst ENABLE CONSTRAINT UK_Ref_Biz_Data_Inst @@