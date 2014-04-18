SELECT
    i.tablename ,
    i.indexname ,
    i. *
FROM
    pg_indexes i
WHERE
    i.tablename IN(
        'arch_process_instance' ,
        'arch_connector_instance' ,
        'arch_flownode_instance' ,
        'arch_data_instance' ,
        'arch_data_mapping'
    )
    AND i.indexname NOT LIKE '%_pkey'
