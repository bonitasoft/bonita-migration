DECLARE @data_mapping_ukey_name nvarchar(255), @data_mapping_alter_table_sql VARCHAR(4000)
SET @data_mapping_alter_table_sql = 'ALTER TABLE data_mapping DROP CONSTRAINT |ConstraintName| '

SELECT @data_mapping_ukey_name = name FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('data_mapping')
AND type = 'UQ'

IF not @data_mapping_ukey_name IS NULL
BEGIN
	SET @data_mapping_alter_table_sql = REPLACE(@data_mapping_alter_table_sql, '|ConstraintName|', @data_mapping_ukey_name)
	EXEC (@data_mapping_alter_table_sql)
END


ALTER TABLE data_mapping ADD CONSTRAINT UNIQUE (tenantid,containerid,containertype,dataname)@@
