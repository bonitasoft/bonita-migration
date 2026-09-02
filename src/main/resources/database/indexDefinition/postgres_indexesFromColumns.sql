SELECT indexname as index_name
FROM pg_indexes
WHERE schemaname = ANY (current_schemas(false))
  AND LOWER(tablename) = LOWER(?)
  AND LOWER(indexdef) LIKE LOWER(?)