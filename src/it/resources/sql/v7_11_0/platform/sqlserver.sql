CREATE TABLE platform (
    id              NUMERIC(19, 0) NOT NULL,
    version         NVARCHAR(50) NOT NULL,
    previousVersion NVARCHAR(50) NOT NULL,
    initialVersion  NVARCHAR(50) NOT NULL,
    created         NUMERIC(19, 0) NOT NULL,
    createdBy       NVARCHAR(50) NOT NULL,
    information     NVARCHAR( MAX),
    PRIMARY KEY (id)
)
GO