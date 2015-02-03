DELETE FROM flownode_instance WHERE deleted = 1@@
DROP INDEX idx_fn_lg2_state_tenant_del ON flownode_instance@@
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid)@@

-- SQL Server generates default constraints that must be deleted before we drop the column. The name is noy known so we have to find it...
-- first define variables
declare @default sysname, @sql nvarchar(max)
-- get name of default constraint
select @default = name
from sys.default_constraints
where parent_object_id = object_id('flownode_instance')
AND type = 'D'
AND parent_column_id = (
    select column_id
	from sys.columns
    where object_id = object_id('flownode_instance')
	and name = 'deleted'
)
-- create alter table command as string and run it
set @sql = N'alter table flownode_instance drop constraint ' + @default
exec sp_executesql @sql

-- now we can finally drop column
ALTER TABLE flownode_instance DROP COLUMN deleted@@
