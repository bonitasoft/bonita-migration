CREATE TABLE contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val BLOB
)
@@
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id)
@@
CREATE UNIQUE INDEX idx_cd_scope_name ON contract_data (scopeId, name, tenantid)
@@

CREATE TABLE arch_contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val BLOB,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL
)
@@
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id)
@@
CREATE UNIQUE INDEX idx_acd_scope_name ON arch_contract_data (scopeId, name, tenantid)
@@
