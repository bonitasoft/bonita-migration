CREATE TABLE platform (
    id              INT8        NOT NULL,
    version         VARCHAR(50) NOT NULL,
    previousVersion VARCHAR(50) NOT NULL,
    initialVersion  VARCHAR(50) NOT NULL,
    created         INT8        NOT NULL,
    createdBy       VARCHAR(50) NOT NULL,
    information     TEXT,
    PRIMARY KEY (id)
);
