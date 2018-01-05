CREATE TABLE tenant_resource (
   tenantId NUMBER(19, 0) NOT NULL,
   id NUMBER(19, 0) NOT NULL,
   name VARCHAR2(255) NOT NULL,
   type VARCHAR2(16) NOT NULL,
   content BLOB NOT NULL,
   CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
   PRIMARY KEY (tenantId, id)
 );
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name);
