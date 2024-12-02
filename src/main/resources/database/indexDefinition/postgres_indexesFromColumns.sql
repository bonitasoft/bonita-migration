SELECT indexname as index_name
FROM pg_indexes
WHERE LOWER(tablename) = LOWER(?)
  AND LOWER(indexdef) LIKE LOWER(?)