SELECT
    s.TABLE_NAME AS table_name,
    s.INDEX_NAME AS index_name,
    s.COLUMN_NAME AS column_name,
    s.SEQ_IN_INDEX AS column_order
FROM
    information_schema.statistics s
WHERE
    s.TABLE_SCHEMA =(
        SELECT
            DATABASE()
    )
    AND UPPER( s.TABLE_NAME ) = UPPER( ? )
    AND UPPER( s.INDEX_NAME ) = UPPER( ? )
ORDER BY s.SEQ_IN_INDEX