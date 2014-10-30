CREATE TABLE tenant (
  id BIGINT NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;
CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE arch_document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT,
  sourceObjectId BIGINT,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor BIGINT,
  documentCreationDate BIGINT NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;
CREATE TABLE document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor BIGINT,
  documentCreationDate BIGINT NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;
CREATE TABLE document_content (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  documentId VARCHAR(50) NOT NULL,
  content LONGBLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
ALTER TABLE document_content ADD CONSTRAINT fk_document_content_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);