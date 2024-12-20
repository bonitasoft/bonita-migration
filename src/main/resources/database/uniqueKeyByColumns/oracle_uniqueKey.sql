SELECT ac.constraint_name
FROM all_constraints ac
JOIN all_cons_columns acc
  ON ac.constraint_name = acc.constraint_name
WHERE LOWER(ac.table_name) = LOWER(?)
  AND ac.constraint_type = 'U'
  AND LOWER(acc.column_name) IN ('@COLUMN_NAMES@')
GROUP BY ac.constraint_name
HAVING COUNT(*) = ?