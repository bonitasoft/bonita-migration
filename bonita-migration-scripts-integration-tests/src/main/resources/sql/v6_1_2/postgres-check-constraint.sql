select * 
from information_schema.constraint_column_usage 
where table_name = 'data_mapping'  and constraint_name = 'data_mapping_tenantid_containerid_containertype_dataname_key'