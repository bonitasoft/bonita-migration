SELECT
    c.TABLE_NAME,
    c.CONSTRAINT_NAME,
    OBJECT_NAME(k.referenced_object_id) AS r_table_name
FROM
    information_schema.TABLE_CONSTRAINTS c,
    sys.foreign_keys k
WHERE
    LOWER( OBJECT_NAME( k.referenced_object_id ) ) = LOWER( ? )
    AND c.CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND c.constraint_name = k.name