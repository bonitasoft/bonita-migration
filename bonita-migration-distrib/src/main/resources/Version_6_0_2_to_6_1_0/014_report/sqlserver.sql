DECLARE @report_ukey_name nvarchar(255), @report_alter_table_sql VARCHAR(4000)
SET @report_alter_table_sql = 'ALTER TABLE report DROP CONSTRAINT |ConstraintName| '

SELECT @report_ukey_name = name FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('report')
AND type = 'UQ'

IF not @report_ukey_name IS NULL
BEGIN
	SET @report_alter_table_sql = REPLACE(@report_alter_table_sql, '|ConstraintName|', @report_ukey_name)
	EXEC (@report_alter_table_sql)
END
@@
ALTER TABLE report ALTER COLUMN name NVARCHAR(50)
@@
ALTER TABLE report ADD CONSTRAINT UQ_Report UNIQUE (tenantId, name); 
@@
ALTER TABLE report ALTER COLUMN description NVARCHAR(MAX)
@@
ALTER TABLE report ADD lastModificationDate NUMERIC(19,0) NOT NULL
@@
ALTER TABLE report ADD screenshot VARBINARY(MAX)
@@
ALTER TABLE report ADD content VARBINARY(MAX)
@@