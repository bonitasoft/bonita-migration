DELETE arch_contract_data
FROM arch_contract_data
        LEFT JOIN
    arch_process_instance ON
    arch_process_instance.tenantid = arch_contract_data.tenantid
    AND arch_process_instance.sourceobjectid = arch_contract_data.scopeid
WHERE
arch_contract_data.kind = 'PROCESS'
AND arch_process_instance.sourceobjectid IS NULL;
DELETE arch_contract_data
FROM arch_contract_data
        LEFT JOIN
    arch_flownode_instance ON
    arch_flownode_instance.tenantid = arch_contract_data.tenantid
    AND arch_flownode_instance.sourceobjectid = arch_contract_data.scopeid
WHERE
arch_contract_data.kind = 'TASK'
AND arch_flownode_instance.sourceobjectid IS NULL;