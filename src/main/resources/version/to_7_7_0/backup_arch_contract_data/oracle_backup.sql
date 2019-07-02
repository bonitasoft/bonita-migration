-- drop unnecessary constraints:
ALTER TABLE arch_contract_data DROP PRIMARY KEY;
ALTER TABLE arch_contract_data DROP CONSTRAINT uc_acd_scope_name;
-- rename table:
ALTER TABLE arch_contract_data RENAME TO arch_contract_data_backup;
-- recreate empty table...
CREATE TABLE arch_contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(20 CHAR) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val CLOB,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL
);
-- with standard constraints:
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);