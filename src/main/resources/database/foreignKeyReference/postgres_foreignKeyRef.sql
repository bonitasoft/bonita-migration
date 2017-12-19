SELECT
    c.TABLE_NAME,
    u.CONSTRAINT_NAME,
    u.table_name as r_table_name
FROM
    information_schema.TABLE_CONSTRAINTS c,
    information_schema.constraint_table_usage u
WHERE
    LOWER( u.TABLE_NAME ) = LOWER( ? )
    AND c.CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND c.constraint_name = u.constraint_name
