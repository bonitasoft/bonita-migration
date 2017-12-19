SELECT
    t.relname AS table_name,
    i.relname AS index_name,
    ai.attname AS column_name,
    ai.attnum AS column_order
FROM
    pg_class t,
    pg_class i,
    pg_index ix,
    pg_attribute ai
WHERE
    t.oid = ix.indrelid
    AND i.oid = ix.indexrelid
    AND ai.attrelid = i.oid
    AND t.relkind = 'r'
    AND UPPER(t.relname) = UPPER(?)
    AND UPPER(i.relname) = UPPER(?)
ORDER BY ai.attnum