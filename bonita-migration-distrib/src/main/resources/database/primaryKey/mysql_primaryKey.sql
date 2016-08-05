SELECT
    c.TABLE_NAME,
    c.CONSTRAINT_NAME
FROM
    information_schema.TABLE_CONSTRAINTS c
WHERE
    c.CONSTRAINT_SCHEMA =(
        SELECT
            DATABASE()
    )
    AND LOWER( c.TABLE_NAME ) = LOWER( ? )
    and c.CONSTRAINT_TYPE='PRIMARY KEY'
