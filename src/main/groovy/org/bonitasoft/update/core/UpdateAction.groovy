package org.bonitasoft.update.core

interface UpdateAction {

    void run(boolean isSp)

    List<VersionUpdate> getVersionUpdates()

    void setVersionUpdates(List<VersionUpdate> versionUpdates)

    UpdateContext getContext()

    List<String> getBannerAndGlobalWarnings()

    String getDescription()
}