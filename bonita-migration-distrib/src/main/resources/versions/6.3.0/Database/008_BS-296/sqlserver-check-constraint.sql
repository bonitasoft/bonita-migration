SELECT COUNT(TABLE_NAME) nbColumn, COUNT(distinct TABLE_NAME) as nbConstraint
FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE col_cons
    INNER JOIN sys.key_constraints all_cons
     ON col_cons.CONSTRAINT_NAME = all_cons.name
WHERE col_cons.TABLE_NAME = 'data_mapping'
    AND all_cons.type = 'UQ'