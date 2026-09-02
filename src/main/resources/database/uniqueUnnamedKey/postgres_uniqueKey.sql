SELECT
    c.CONSTRAINT_NAME
FROM
    information_schema.TABLE_CONSTRAINTS c
WHERE
    c.TABLE_SCHEMA = ANY (current_schemas(false))
    AND LOWER( c.TABLE_NAME ) = LOWER( ? )
    AND c.CONSTRAINT_TYPE = 'UNIQUE'
