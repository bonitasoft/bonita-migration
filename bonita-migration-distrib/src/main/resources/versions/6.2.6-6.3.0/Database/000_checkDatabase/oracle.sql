SELECT
    u.TABLE_NAME AS tablename ,
    u.INDEX_NAME AS indexname
FROM
    user_indexes u
WHERE
    u.TABLE_NAME IN(
        'arch_process_instance' ,
        'arch_connector_instance' ,
        'arch_flownode_instance' ,
        'arch_data_instance' ,
        'arch_data_mapping'
    ) 