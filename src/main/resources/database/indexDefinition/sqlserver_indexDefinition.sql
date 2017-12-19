SELECT
    t.name AS table_name,
    ind.name AS index_name,
    col.name AS column_name,
    ic.index_column_id AS column_order
FROM
    sys.indexes ind INNER JOIN sys.index_columns ic
        ON ind.object_id = ic.object_id
    AND ind.index_id = ic.index_id INNER JOIN sys.columns col
        ON ic.object_id = col.object_id
    AND ic.column_id = col.column_id INNER JOIN sys.tables t
        ON ind.object_id = t.object_id
WHERE
    UPPER(t.name) = UPPER(?)
    AND UPPER(ind.name) = UPPER(?)
    AND ind.is_primary_key = 0
    AND ind.is_unique = 0
    AND ind.is_unique_constraint = 0
    AND t.is_ms_shipped = 0
ORDER BY ic.index_column_id