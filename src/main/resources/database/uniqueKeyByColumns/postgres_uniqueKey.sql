SELECT tc.constraint_name
FROM information_schema.table_constraints tc
JOIN information_schema.constraint_column_usage ccu
  ON tc.constraint_name = ccu.constraint_name
  AND tc.constraint_schema = ccu.constraint_schema
WHERE tc.table_schema = ANY (current_schemas(false))
  AND LOWER(tc.table_name) = LOWER(?)
  AND tc.constraint_type = 'UNIQUE'
  AND LOWER(ccu.column_name) IN ('@COLUMN_NAMES@')
GROUP BY tc.constraint_name
HAVING COUNT(*) = ?