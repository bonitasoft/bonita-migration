SELECT
    c.TABLE_NAME,
    u.CONSTRAINT_NAME,
    u.table_name as r_table_name
FROM
    information_schema.TABLE_CONSTRAINTS c,
    information_schema.constraint_table_usage u
WHERE
    c.TABLE_SCHEMA = ANY (current_schemas(false))
    AND LOWER( u.TABLE_NAME ) = LOWER( ? )
    AND c.CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND c.constraint_name = u.constraint_name
    AND c.constraint_schema = u.constraint_schema
