SELECT
    DISTINCT TABLE_NAME AS tablename ,
    INDEX_NAME AS indexname
FROM
    INFORMATION_SCHEMA.STATISTICS
WHERE
    LOWER(table_name) IN(
        'arch_process_instance' ,
        'arch_connector_instance' ,
        'arch_flownode_instance' ,
        'arch_data_instance' ,
        'arch_data_mapping'
    )
    AND INDEX_NAME != 'PRIMARY'
    AND INDEX_NAME NOT LIKE 'fk%'
    AND TABLE_SCHEMA = DATABASE()