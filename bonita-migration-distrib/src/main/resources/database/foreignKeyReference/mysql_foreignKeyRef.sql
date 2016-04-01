SELECT
    DISTINCT c.TABLE_NAME AS table_name,
    c.CONSTRAINT_NAME,
    c.REFERENCED_TABLE_NAME AS r_table_name
FROM
    information_schema.KEY_COLUMN_USAGE c,
    information_schema.TABLE_CONSTRAINTS t
WHERE
    LOWER( c.REFERENCED_TABLE_NAME ) = LOWER( ? )
    AND t.CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND t.TABLE_NAME = c.table_name
    AND c.CONSTRAINT_NAME = t.CONSTRAINT_NAME
    AND c.CONSTRAINT_SCHEMA =(
        SELECT
            DATABASE()
    )

