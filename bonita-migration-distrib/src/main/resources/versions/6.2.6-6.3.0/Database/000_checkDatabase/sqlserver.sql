SELECT
    DISTINCT TableName = t.name ,
    IndexName = ind.name
FROM
    sys.indexes ind INNER JOIN sys.tables t
        ON ind.object_id = t.object_id
WHERE
    ind.is_primary_key = 0
    AND ind.is_unique = 0
    AND ind.is_unique_constraint = 0
    AND t.is_ms_shipped = 0
    AND LOWER(t.name) IN(
        'arch_process_instance' ,
        'arch_connector_instance' ,
        'arch_flownode_instance' ,
        'arch_data_instance' ,
        'arch_data_mapping'
    )