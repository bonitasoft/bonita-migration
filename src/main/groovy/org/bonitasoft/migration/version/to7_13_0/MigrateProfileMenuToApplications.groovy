package org.bonitasoft.migration.version.to7_13_0

import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import groovy.transform.Canonical
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Baptiste Mesta
 * @author Dumitru Corini
 */
class MigrateProfileMenuToApplications extends MigrationStep {

    private Set<String> postMigrationWarnings = []

    @Override
    String getWarning() {
        return postMigrationWarnings ? postMigrationWarnings.join('\n') : null
    }
    private static final Map<String, String> PROVIDED_LEGACY_PAGES_TO_APP_PAGES = [
            "licensemonitoringadmin"  : "custompage_adminLicenseBonita",
            "tasklistinguser"         : "custompage_tasklist",
            "tasklistingpm"           : "",
            "tasklistingadmin"        : "custompage_adminTaskListBonita",
            "caselistinguser"         : "custompage_userCaseListBonita",
            "caselistingadmin"        : "custompage_adminCaseListBonita",
            "caselistingpm"           : "",
            "processlistingadmin"     : "custompage_adminProcessListBonita",
            "processlistingpm"        : "",
            "userlistingadmin"        : "custompage_adminUserListBonita",
            "grouplistingadmin"       : "custompage_adminGroupListBonita",
            "rolelistingadmin"        : "custompage_adminRoleListBonita",
            "profilelisting"          : "custompage_adminProfileListBonita",
            "reportlistingadminext"   : "",
            "thememoredetailsadminext": "",
            "pagelisting"             : "custompage_adminResourceListBonita",
            "applicationslistingadmin": "custompage_adminApplicationListBonita",
            "monitoringadmin"         : "custompage_adminMonitoringBonita",
            "monitoringpm"            : "",
            "bdm"                     : "custompage_adminBDMBonita"]

    private static final Map<String, List<String>> PAGE_LINK_MAPPING = [
            "custompage_adminApplicationListBonita": ["custompage_adminApplicationDetailsBonita"],
            "custompage_adminCaseDetailsBonita"    : ["custompage_adminTaskListBonita"],
            "custompage_adminCaseListBonita"       : ["custompage_adminCaseDetailsBonita", "custompage_adminCaseVisuBonita"],
            "custompage_adminGroupListBonita"      : ["custompage_adminUserDetailsBonita"],
            "custompage_adminMonitoringBonita"     : ["custompage_adminProcessDetailsBonita", "custompage_adminCaseVisuBonita", "custompage_adminCaseListBonita"],
            "custompage_adminProcessDetailsBonita" : ["custompage_adminCaseListBonita", "custompage_adminProcessVisuBonita"],
            "custompage_adminProcessListBonita"    : ["custompage_adminProcessDetailsBonita"],
            "custompage_adminTaskListBonita"       : ["custompage_adminTaskDetailsBonita"],
            "custompage_adminUserListBonita"       : ["custompage_adminUserDetailsBonita"],
            "custompage_error403Bonita"            : ["custompage_applicationDirectoryBonita"],
            "custompage_error404Bonita"            : ["custompage_applicationDirectoryBonita"],
            "custompage_error500Bonita"            : ["custompage_applicationDirectoryBonita"],
            "custompage_userCaseDetailsBonita"     : ["custompage_tasklist"],
            "custompage_userCaseListBonita"        : ["custompage_userCaseDetailsBonita"]]

    @Canonical
    private static class Profile {
        long tenantId
        long id
        String name
        String description
        List<ProfileEntry> entries = []
    }

    @Canonical
    private static class ProfileEntry {
        long id
        String name
        String page
        List<ProfileEntry> children = []

        @Override
        String toString() {
            return new StringJoiner(", ", ProfileEntry.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .add("name='" + name + "'")
                    .add("page='" + page + "'")
                    .add("children=" + children)
                    .toString()
        }
    }

    @Override
    def execute(MigrationContext context) {
        if (!context.databaseHelper.hasTable("profileentry")) {
            context.logger.info("Migration step will not be executed, table 'profileentry' does not exist. It may have been executed already.")
        }
        def profiles = retrieveAllProfiles(context)
        profiles.each { profile ->
            context.with {
                if (profile.entries.size() == 0) {
                    logger.info("The profile \"${profile.name}\" does not have any profile entry. No application will be generated.")
                    return
                }
                def applicationNextId = createApplication(context, profile)

                profile.entries.eachWithIndex { entry, index ->
                    if (entry.children.empty) {
                        createApplicationMenuAndPageMapping(context, entry, profile, applicationNextId, index, null)
                    } else {
                        def parentApplicationMenuId = createApplicationMenu(context, profile, entry, applicationNextId, null, index, null)

                        entry.children.eachWithIndex { childEntry, childIndex ->
                            createApplicationMenuAndPageMapping(context, childEntry, profile, applicationNextId, childIndex, parentApplicationMenuId)
                        }
                    }
                }
                setApplicationHomePage(context, profile, applicationNextId)
                createOrphanPages(context, profile, applicationNextId)
            }
        }
        context.databaseHelper.dropTableIfExists("profileentry")
        context.sql.executeUpdate("DELETE FROM sequence WHERE id = ${9991} ")
    }

    private long createApplication(MigrationContext migrationContext, Profile profile) {
        def appToken = generateApplicationToken(profile)
        def appName = profile.name + " Application"
        def now = System.currentTimeMillis()

        migrationContext.with {
            def applicationNextId = databaseHelper.getAndUpdateNextSequenceId(10200, profile.tenantId)
            if (applicationWithTokenExists(sql, profile, appToken)) {
                appToken = findAvailableToken(sql, appToken, profile)
            }
            def layoutId = sql.firstRow("SELECT id from page WHERE tenantId=${profile.tenantId} AND name = ${'custompage_layoutBonita'}")[0]
            def themeId = sql.firstRow("SELECT id from page WHERE tenantId=${profile.tenantId} AND name = ${'custompage_themeBonita'}")[0]
            logger.info("Generating application with token \"${appToken}\" for profile \"${profile.name}\"")
            sql.executeInsert(""" INSERT INTO business_app(tenantId, id, token, version, description, 
iconPath, creationDate, createdBy, lastUpdateDate, updatedBy, state, homePageId, profileId, layoutId, themeId, 
iconMimeType, iconContent, displayName, editable, internalProfile) 
VALUES (${profile.tenantId},${applicationNextId},${appToken},${'1.0'},${profile.description},
${null},${now},${-1L},${now},${-1L},${'ACTIVATED'},${null},${profile.id},$layoutId,$themeId,
${null},${null},${appName},${true},${null})""")

            applicationNextId
        }
    }

    private String generateApplicationToken(Profile profile) {
        def token = Strings.toKebabCase(profile.name)
        token = token.substring(0, Math.min(50 - "-app".length() - 3/*counter length*/, token.length())) + "-app"
        token.startsWith("-") ? token.substring(1) : token
    }

    private String findAvailableToken(sql, String appToken, Profile profile) {
        def counter = 1
        def newAppToken = "$appToken-$counter"
        while (applicationWithTokenExists(sql, profile, newAppToken)) {
            newAppToken = "$appToken-${++counter}"
        }
        newAppToken
    }

    private boolean applicationWithTokenExists(Sql sql, Profile profile, String appToken) {
        sql.firstRow("SELECT count(id) FROM business_app WHERE tenantId = ${profile.tenantId} AND token = ${appToken}")[0] > 0
    }

    private void createApplicationMenuAndPageMapping(MigrationContext migrationContext, ProfileEntry entry, Profile profile, long applicationNextId, index, parentApplicationMenuId) {
        migrationContext.with {
            def newPageToken = PROVIDED_LEGACY_PAGES_TO_APP_PAGES.getOrDefault(entry.page, entry.page)
            if (newPageToken.isEmpty()) {
                def warnMdg = """Portal page "${entry.page}" cannot be converted to an application page because there is no replacement page."""
                logger.warn(warnMdg)
                postMigrationWarnings.add(warnMdg)
                return
            }
            def applicationPageNextId = createApplicationPage(migrationContext, profile, applicationNextId, newPageToken)
            if (applicationPageNextId == null) {
                return
            }
            createApplicationMenu(migrationContext, profile, entry, applicationNextId, applicationPageNextId, index, parentApplicationMenuId)
        }
    }

    private Long createApplicationPage(MigrationContext migrationContext, Profile profile, long applicationNextId, newPageToken) {
        migrationContext.with {
            def page = sql.firstRow("""SELECT id FROM page WHERE tenantId = ${profile.tenantId} AND name = ${newPageToken}""")
            if (page == null) {
                def warnMdg = """Page "${newPageToken}" does not exist. It will not be present in the application associated with profile "${profile.name}" """
                logger.warn(warnMdg)
                postMigrationWarnings.add(warnMdg)
                return null
            }

            def applicationPageToken = createApplicationPageToken(newPageToken)

            def applicationPage = sql.firstRow("""SELECT id FROM business_app_page WHERE tenantId = ${profile.tenantId} AND applicationId = ${applicationNextId} AND token = ${applicationPageToken}""")
            if (applicationPage != null) {
                logger.debug("Page \"${newPageToken}\" already exists. Using it")
                return applicationPage[0]
            }

            def applicationPageNextId = databaseHelper.getAndUpdateNextSequenceId(10201, profile.tenantId)

            sql.executeInsert(""" INSERT INTO business_app_page(tenantId, id, applicationId, pageId, token) 
VALUES (${profile.tenantId},${applicationPageNextId},${applicationNextId},${page[0]},${applicationPageToken})""")

            applicationPageNextId
        }
    }

    private long createApplicationMenu(MigrationContext migrationContext, Profile profile, ProfileEntry entry, long applicationNextId, applicationPageNextId, index, parentApplicationMenuId) {
        migrationContext.with {
            def applicationMenuNextId = databaseHelper.getAndUpdateNextSequenceId(10202, profile.tenantId)
            sql.executeInsert(""" INSERT INTO business_app_menu(tenantId, id, displayName, applicationId,
applicationPageId, parentId, index_) 
VALUES (${profile.tenantId},${applicationMenuNextId},${entry.name},${applicationNextId},
${applicationPageNextId},${parentApplicationMenuId},${index + 1})""")

            applicationMenuNextId
        }
    }

    private void setApplicationHomePage(MigrationContext migrationContext, Profile profile, long applicationNextId) {
        migrationContext.with {
            def homepageId = sql.firstRow("SELECT applicationPageId FROM business_app_menu WHERE tenantId = ${profile.tenantId} AND applicationId = $applicationNextId AND parentId IS NULL ORDER BY index_ ASC")[0]
            sql.executeUpdate("UPDATE business_app SET homepageId = $homepageId WHERE tenantId = ${profile.tenantId} AND id = $applicationNextId")
        }
    }


    private void createOrphanPages(MigrationContext migrationContext, Profile profile, long applicationNextId) {
        migrationContext.with {
            Set<String> pageNames = sql.rows("SELECT p.name FROM business_app_page a, page p WHERE p.tenantId = ${profile.tenantId} AND a.tenantId = ${profile.tenantId} AND a.applicationId = ${applicationNextId} AND a.pageId = p.id").name
            Set<String> orphanPagesToAdd = []

            pageNames.each { pageName ->
                addOrphanPagesRecursively(pageName, orphanPagesToAdd)
            }

            orphanPagesToAdd.removeAll(pageNames)

            logger.debug("Adding orphan page to the application \"$applicationNextId\": $orphanPagesToAdd")
            orphanPagesToAdd.each {
                createApplicationPage(migrationContext, profile, applicationNextId, it)
            }
        }
    }

    private void addOrphanPagesRecursively(String pageName, orphanPagesToAdd) {
        def orphanPages = PAGE_LINK_MAPPING.getOrDefault(pageName, [])
        orphanPages.each {
            if (orphanPagesToAdd.add(it)) {
                addOrphanPagesRecursively(it, orphanPagesToAdd)
            }
        }
    }

    private List<Profile> retrieveAllProfiles(MigrationContext context) {
        List<Profile> allProfiles = []
        context.databaseHelper.with {
            sql.eachRow("SELECT id from tenant") { tenant ->
                def tenantId = tenant.id
                sql.eachRow("SELECT * FROM profile WHERE tenantid = ${tenantId} AND isdefault = ${false}") { profileRow ->

                    def profile = new Profile(tenantId: profileRow.tenantId, id: profileRow.id, name: profileRow.name, description: profileRow.description)
                    logger.debug("Found profile to migrate \"${profileRow.name}\"")
                    allProfiles.add(profile)
                    sql.eachRow("SELECT * FROM profileentry WHERE tenantid = ${profile.tenantId} AND profileid = ${profile.id} AND parentid = ${0L} ORDER BY index_ ASC") { profileEntryRow ->
                        def profileEntry = toProfileEntry(context, profile, profileEntryRow)
                        profile.entries.add(profileEntry)
                    }
                }
            }
            allProfiles
        }
    }

    private ProfileEntry toProfileEntry(MigrationContext context, Profile profile, GroovyResultSet profileEntryRow) {
        context.with {
            def entry = new ProfileEntry(
                    id: profileEntryRow['id'],
                    name: profileEntryRow['name'],
                    page: profileEntryRow['page']
            )
            sql.eachRow("SELECT * FROM profileentry WHERE tenantid = ${profile.tenantId} AND profileid = ${profile.id} AND parentid = ${entry.id} ORDER BY index_ ASC") { childEntryRow ->
                entry.children.add(toProfileEntry(context, profile, childEntryRow))
            }
            return entry
        }
    }

    private String createApplicationPageToken(String page) {
        return Strings.slugify(Strings.splitCamelCase(removeBonitaSuffix(removeUserPrefix(removeCustomPagePrefix(page)))))
    }

    private static String removeUserPrefix(String input) {
        return input.startsWith("user") ? input.substring("user".length()) : input;
    }

    private static String removeCustomPagePrefix(String input) {
        return input.startsWith("custompage_") ? input.substring("custompage_".length()) : input
    }

    private static String removeBonitaSuffix(String input) {
        return input.endsWith("Bonita") ? input.substring(0, input.length() - "Bonita".length()) : input
    }

    @Override
    String getDescription() {
        return "Migrate existing custom profiles navigation to applications"
    }
}