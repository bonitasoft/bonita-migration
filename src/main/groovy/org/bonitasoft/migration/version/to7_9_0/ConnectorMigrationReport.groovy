package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.core.Logger

class ConnectorMigrationReport {


    private class SuccessfulConnectorMigrationReport {
        List<String> connectorIds = []
        String newVersion
        List<String> removed
        List<String> added
    }

    private class FailedConnectorMigrationReport {
        boolean dependencyError = false
        String error
        List<String> connectorIds = []
        String newVersion
        Map<String, List<String>> usedBy = [:]

    }

    private class ConnectorMigrationReportOfProcess {
        private List<SuccessfulConnectorMigrationReport> okConnectors = []
        private List<FailedConnectorMigrationReport> koConnectors = []

    }

    Map<ProcessDefinition, ConnectorMigrationReportOfProcess> processes = [:]
    Set<String> processesWithFailedConnectorMigration = new LinkedHashSet<>()


    def reportFailureCausedByDependencyError(ProcessDefinition processDefinition, List<ConnectorImplementation> newImplementations,
                                             List<ConnectorImplementation> connectorsUsingDependencies, List<String> dependenciesToRemove) {
        processes.computeIfAbsent(processDefinition, { p -> new ConnectorMigrationReportOfProcess() })
        def report = new FailedConnectorMigrationReport()
        report.dependencyError = true
        report.connectorIds = newImplementations.id
        report.newVersion = newImplementations.first().version
        def usedDependencies = dependenciesToRemove.findAll { connectorsUsingDependencies.jarDependencies.flatten().contains(it) }
        usedDependencies.each {
            report.usedBy.put(it, connectorsUsingDependencies.findAll {c-> c.jarDependencies.contains(it)}.collect {c->"$c.id in version $c.version".toString()})
        }
        processes.get(processDefinition).koConnectors.add(report)
    }

    void reportGenericFailure(ProcessDefinition processDefinition, ConnectorImplementation koImpl, String error){
        processes.computeIfAbsent(processDefinition, { p -> new ConnectorMigrationReportOfProcess() })
        def failedConnectorReport = new FailedConnectorMigrationReport()
        failedConnectorReport.connectorIds.add(koImpl.id)
        failedConnectorReport.error = error
        processes.get(processDefinition).koConnectors.add(failedConnectorReport)
    }

    void reportSuccess(ProcessDefinition processDefinition, List<ConnectorImplementation> newImplementations,
                       List<String> dependenciesToRemove, List<String> dependenciesToAdd) {
        processes.computeIfAbsent(processDefinition, { p -> new ConnectorMigrationReportOfProcess() })
        def report = new SuccessfulConnectorMigrationReport()
        report.connectorIds = newImplementations.id
        report.newVersion = newImplementations.first().version
        report.added = dependenciesToAdd
        report.removed = dependenciesToRemove
        processes.get(processDefinition).okConnectors.add(report)
    }


    //sample output:

    //INFO Migration of connectors of process 'My process' 1.1.0:
    //INFO connectors cmis-upload, cmis-download migrated to version 1.2.0:
    //INFO      added:
    //INFO      -  email.1.1.2.jar
    //INFO      -  toto.jar
    //INFO      removed:
    //INFO      -  email.1.0.0.jar
    //INFO      -  tata.jar
    //WARN  Unable to migrate connector cmis-upload,cmis-download to version 1.2.0:
    //WARN     dependency email.1.4.5.jar is used by:
    //WARN          - custom-connector 1.2.0
    //WARN          - custom-connector 1.3.0
    //WARN     dependency tata.jar is used by:
    //WARN          - custom-connector 1.4.0

    String printGlobalReport(Logger logger) {
        processes.each { processEntry ->
            def process = processEntry.key
            def report = processEntry.value
            logger.info("Migration of connectors of process '$process.name' in version $process.version:")
            if (!report.okConnectors.empty) {
                report.okConnectors.each { okConnector ->
                    logger.info("  Connector(s) $okConnector.connectorIds migrated to version $okConnector.newVersion:")
                    logger.info("    added")
                    okConnector.added.each { added ->
                        logger.info("    - $added")
                    }
                    logger.info("    removed")
                    okConnector.removed.each { added ->
                        logger.info("    - $added")
                    }
                }
            }
            if (!report.koConnectors.empty) {
                processesWithFailedConnectorMigration.add(process.name + " (${process.version})")
                report.koConnectors.findAll { it -> it.dependencyError }
                        .each { koConnector ->
                            logger.warn("  Unable to migrate connector(s) $koConnector.connectorIds to version $koConnector.newVersion:")
                            koConnector.usedBy.each { entry ->
                                logger.warn("    dependency $entry.key is already used by:")
                                entry.value.each {
                                    logger.warn("    - $it")
                                }
                            }
                        }
                report.koConnectors.findAll { it -> !it.dependencyError }
                        .each { koConnector ->
                            logger.warn("  Unable to migrate connector(s) $koConnector.connectorIds . The reason is $koConnector.error")
                        }
            }

        }
    }

    boolean hasFailure() {
        processes.any { p -> p.value.any { !it.koConnectors.empty } }
    }

    String getFailureReport() {
        def formattedListOfProcessesWithFailedConnectorMigration = "- " + processesWithFailedConnectorMigration.join("\n- ")
        return """
We could not migrate some connectors on the following processes:
${formattedListOfProcessesWithFailedConnectorMigration}


These connectors will not work on a Java 11 platform.
You will need to replace them manually.

Check your logs for details.
"""
    }
}
