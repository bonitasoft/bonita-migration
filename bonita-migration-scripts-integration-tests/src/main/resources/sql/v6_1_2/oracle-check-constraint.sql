SELECT *
FROM all_cons_columns col_cons
   INNER JOIN all_constraints all_cons
   		on col_cons.OWNER = all_cons.OWNER
where col_cons.table_name = 'DATA_MAPPING'
	AND all_cons.CONSTRAINT_TYPE = 'U'
	AND all_cons.CONSTRAINT_NAME = col_cons.CONSTRAINT_NAME