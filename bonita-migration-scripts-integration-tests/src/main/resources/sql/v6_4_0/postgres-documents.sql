CREATE TABLE tenant (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processinstanceid INT8,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor INT8,
  documentCreationDate INT8 NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  PRIMARY KEY (tenantid, ID)
);
CREATE TABLE document_content (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  documentId VARCHAR(50) NOT NULL,
  content BYTEA NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  sourceObjectId INT8,
  processinstanceid INT8 NOT NULL,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor INT8,
  documentCreationDate INT8 NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, ID)
);
ALTER TABLE document_content ADD CONSTRAINT fk_document_content_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);