DELETE FROM arch_contract_data c
WHERE
c.kind = 'PROCESS'
AND NOT EXISTS (SELECT 1
                  FROM arch_process_instance p
                  where c.tenantid = p.tenantid AND c.scopeid = p.sourceobjectid);
DELETE FROM arch_contract_data c
WHERE
c.kind = 'TASK'
AND NOT EXISTS (SELECT 1
                  FROM arch_flownode_instance f
                  where c.tenantid = f.tenantid AND c.scopeid = f.sourceobjectid);