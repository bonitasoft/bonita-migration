SELECT
    i.TABLE_NAME AS table_name,
    i.INDEX_NAME AS index_name,
    i.COLUMN_NAME AS column_name,
    i.COLUMN_POSITION AS column_order
FROM
    user_ind_columns i
WHERE
    UPPER(i.table_name) = UPPER(?)
    AND UPPER(i.INDEX_NAME) = UPPER(?)
ORDER BY i.COLUMN_POSITION
