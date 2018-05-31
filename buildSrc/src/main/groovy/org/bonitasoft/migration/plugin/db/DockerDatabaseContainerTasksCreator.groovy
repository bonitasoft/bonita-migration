package org.bonitasoft.migration.plugin.db

import static java.lang.String.format

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerInspectContainer
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.extras.DockerWaitHealthyContainer
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import org.gradle.api.Project


/**
 * Gradle plugin to start docker database containers and performed tests with them
 */
class DockerDatabaseContainerTasksCreator {

    def static vendors = [
            [name           : 'oracle',
             repository     : 'registry.rd.lan/bonitasoft/oracle-11g',
             tag            : '1.2.0',
             portBinding    : 1521,
             uriTemplate    : 'jdbc:oracle:thin:@//%s:%s/xe',
             driverClassName: 'oracle.jdbc.OracleDriver',
             rootUser       : 'sys as sysdba',
             rootPassword   : 'oracle'
            ],
            [name           : 'postgres',
             repository     : 'registry.rd.lan/bonitasoft/postgres-9.3',
             tag            : '1.2.3',
             portBinding    : 5432,
             uriTemplate    : 'jdbc:postgresql://%s:%s/bonita',
             driverClassName: 'org.postgresql.Driver',
             rootUser       : 'postgres',
             rootPassword   : 'postgres',
             databaseName   : 'bonita'
            ],
            [name           : 'mysql',
             repository     : 'registry.rd.lan/bonitasoft/mysql-5.5.49',
             tag            : '1.1.1',
             portBinding    : 3306,
             uriTemplate    : 'jdbc:mysql://%s:%s/bonita?allowMultiQueries=true',
             driverClassName: 'com.mysql.jdbc.Driver',
             rootUser       : 'root',
             rootPassword   : 'root'
            ]
    ]

    static List<String> supportedVendors() {
        ['oracle','postgres','mysql']
    }

    private static String getDockerHost() {
        def dockerHost = System.getenv('DOCKER_HOST')
        if (dockerHost?.trim()) {
            return new URI(dockerHost).host
        }
        return 'localhost'
    }

    def static createTaks(Project project) {
        // required to have the environment correctly setup: see https://github.com/bmuschko/gradle-docker-plugin/issues/575#issuecomment-383704012
        project.plugins.apply('com.bmuschko.docker-remote-api')
        vendors.each { vendor ->
            def uniqueName = "${vendor.name.capitalize()}"
            def pullImage = project.tasks.create("pull${uniqueName}Image", DockerPullImage) {
                description "Pull docker image for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                repository = vendor.repository
                tag = vendor.tag
            }

            def createContainer = project.tasks.create("create${uniqueName}Container", DockerCreateContainer) {
                description "Create a docker container for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                dependsOn pullImage
                portBindings = [":$vendor.portBinding"]
                targetImageId { pullImage.getImageId() }
                if (vendor.name == 'oracle') {
                    // 1Go
                    shmSize = 1099511627776
                }
            }

            def startContainer = project.tasks.create("start${uniqueName}Container", DockerStartContainer) {
                description "Start a docker container for $uniqueName db vendor"
                group "docker"

                dependsOn createContainer
                targetContainerId { createContainer.getContainerId() }
            }

            def waitForContainerStartup = project.tasks.create("waitFor${uniqueName}ContainerStartup", DockerWaitHealthyContainer) {
                description "Wait for a started docker container for $vendor.name db vendor to be healthy"
                group null // do not show task when running `gradle tasks`

                dependsOn startContainer
                targetContainerId { startContainer.getContainerId() }
                timeout = 240
            }

            def inspectContainer = project.tasks.create("inspect${uniqueName}ContainerUrl", DockerInspectContainer) {
                description "Get url of a docker container for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                dependsOn waitForContainerStartup
                targetContainerId { startContainer.getContainerId() }

                onNext {
                    it.networkSettings.ports.getBindings().each { exposedPort, bindingArr ->
                        if (exposedPort.port == vendor.portBinding) {
                            int portBinding = bindingArr.first().hostPortSpec as int
                            def dockerHost = getDockerHost()
                            def url = format(vendor.uriTemplate, dockerHost, portBinding)
                            project.logger.info "Container url: ${url}"
                            project.tasks["${uniqueName}Configuration"].doFirst {
                                DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension)
                                extension.dburl = url
                                extension.dbServerName = dockerHost
                                extension.dbServerPort = portBinding
                                extension.dbDatabaseName = vendor.databaseName
                                project.logger.quiet("db.url set to ${extension.dburl}")
                            }
                        }
                    }
                }
            }

            def removeContainer = project.tasks.create("remove${uniqueName}Container", DockerRemoveContainer) {
                description "Remove a docker container for $uniqueName db vendor"
                group "docker"

                force = true
                removeVolumes = true
                targetContainerId { createContainer.getContainerId() }
            }

            project.tasks.create("${uniqueName}Configuration") {
                description "Setup database connection parameter for $uniqueName"
                group 'docker'
                dependsOn inspectContainer

                doFirst {
                    DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension)
                    extension.dbvendor = vendor.name
                    extension.dbdriverClass = vendor.driverClassName
                    extension.dbRootUser= vendor.rootUser
                    extension.dbRootPassword= vendor.rootPassword
                    // Common to all databases
                    extension.dbuser = 'bonita'
                    extension.dbpassword = 'bpm'

                    project.logger.quiet("db.vendor set to ${extension.dbvendor}")
                    project.logger.quiet("db.driver set to ${extension.dbdriverClass}")
                }
            }

            //container should be removed even when there is a failure
            startContainer.finalizedBy(removeContainer)
        }
    }
}