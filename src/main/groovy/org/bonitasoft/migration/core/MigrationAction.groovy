package org.bonitasoft.migration.core

interface MigrationAction {

    void run(boolean isSp)

    List<VersionMigration> getMigrationVersions()

    void setMigrationVersions(List<VersionMigration> versionMigrations)

    MigrationContext getContext()

    List<String> getBannerAndGlobalWarnings()

    String getDescription()
}