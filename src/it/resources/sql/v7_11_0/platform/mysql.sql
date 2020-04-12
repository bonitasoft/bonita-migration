CREATE TABLE platform (
    id              BIGINT      NOT NULL,
    version         VARCHAR(50) NOT NULL,
    previousVersion VARCHAR(50) NOT NULL,
    initialVersion  VARCHAR(50) NOT NULL,
    created         BIGINT      NOT NULL,
    createdBy       VARCHAR(50) NOT NULL,
    information     TEXT,
    PRIMARY KEY (id)
) ENGINE = INNODB;