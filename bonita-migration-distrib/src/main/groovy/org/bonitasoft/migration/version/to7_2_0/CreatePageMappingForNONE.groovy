package org.bonitasoft.migration.version.to7_2_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 *
 * @author Baptiste Mesta
 */
class CreatePageMappingForNONE extends MigrationStep {


    static Map authorizationRulesMap
    static int TYPE_PROCESS_START = 1;
    static int TYPE_PROCESS_OVERVIEW = 2;
    static int TYPE_TASK = 3;

    static {
        authorizationRulesMap = new HashMap<>(3);
        authorizationRulesMap.put(TYPE_PROCESS_START, ["IS_ADMIN", "IS_PROCESS_OWNER", "IS_ACTOR_INITIATOR"]);
        authorizationRulesMap.put(TYPE_PROCESS_OVERVIEW, ["IS_ADMIN", "IS_PROCESS_OWNER", "IS_PROCESS_INITIATOR", "IS_TASK_PERFORMER", "IS_INVOLVED_IN_PROCESS_INSTANCE"]);
        authorizationRulesMap.put(TYPE_TASK, ["IS_ADMIN", "IS_PROCESS_OWNER", "IS_TASK_AVAILABLE_FOR_USER"]);
    }

    @Override
    def execute(MigrationContext context) {
        def Map<Long, Long> pageMappingCount = [:]
        context.sql.eachRow("SELECT id FROM tenant") { tenant ->
            pageMappingCount.put(tenant.id as Long, context.sql.firstRow("SELECT nextId from sequence WHERE  id = ${10121} AND tenantId = $tenant.id").nextId as Long)
            context.logger.debug("Current sequence 10121 is at ${pageMappingCount.get(tenant.id)} for tenant $tenant.id")
        }
        context.sql.eachRow("SELECT * FROM form_mapping WHERE target = 'NONE'") { formMapping ->
            def pageMapping = context.sql.firstRow("SELECT * FROM page_mapping where id = $formMapping.page_mapping_id AND tenantId = $formMapping.page_mapping_tenant_id")
            context.logger.debug("Page mapping for the NONE form mapping $formMapping.id is ${pageMapping ? pageMapping.id : 'null'}")
            if (pageMapping == null) {
                def Long nextId = pageMappingCount.get(formMapping.tenantId as Long)
                pageMappingCount.put(formMapping.tenantId as Long, nextId + 1L)
                context.sql.executeInsert("INSERT INTO page_mapping VALUES($formMapping.tenantId,$nextId,${getKey(context, formMapping.tenantId as Long, formMapping.process as Long, formMapping.task, formMapping.type)},${null},${null},${null},${getAuthorizationRules(formMapping.type)},${0L},${0L})")
                context.sql.executeUpdate("UPDATE form_mapping SET page_mapping_id = $nextId, page_mapping_tenant_id = $formMapping.tenantId WHERE tenantId = $formMapping.tenantId and id = $formMapping.id")
                context.logger.debug("Inserted page mapping $nextId")
            }
        }

        updateSequence(pageMappingCount, context)
    }

    def String getAuthorizationRules(Integer type) {
        return authorizationRulesMap.get(type).inject("", { result, rule -> result += (rule + ",") })
    }

    def String getKey(MigrationContext context, long tenantId, long processDefinitionId, String task, Integer type) {
        def row = context.sql.firstRow("SELECT name,version FROM process_definition WHERE tenantid = $tenantId AND processid = $processDefinitionId")
        def String name = row.name
        def String version = row.version
        switch (type) {
            case TYPE_PROCESS_OVERVIEW:
                return "processInstance/" + name + "/" + version;
            case TYPE_PROCESS_START:
                return "process/" + name + "/" + version;
            case TYPE_TASK:
                return "taskInstance/" + name + "/" + version + "/" + task;
        }
    }

    private static Map<Long, Long> updateSequence(Map<Long, Long> pageMappingCount, MigrationContext context) {
        return pageMappingCount.each { it ->
            context.sql.executeUpdate("UPDATE sequence SET nextId = $it.value WHERE tenantId = $it.key and id = ${10121}");
            context.logger.debug("Updated sequence 10121 to $it.value for tenant $it.key")
        }
    }


    @Override
    String getDescription() {
        return "Create page mapping for none form mapping"
    }

}
