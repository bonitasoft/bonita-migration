package org.bonitasoft.migration.version.to7_1_0
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
/**
 * @author Baptiste Mesta
 */
class MigrateFormMapping extends MigrationStep{
    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.addColumn("form_mapping","target","VARCHAR(16)","'NONE'","NOT NULL")

        context.sql.eachRow("SELECT * FROM form_mapping"){ formMapping ->
            def pageMapping = context.sql.firstRow("SELECT * FROM page_mapping where id = $formMapping.page_mapping_id AND tenantId = $formMapping.page_mapping_tenant_id")
            String target = "NONE"
            if(pageMapping != null){
                if(pageMapping.url != null && !pageMapping.url.isEmpty()){
                    target = "URL"
                }else if (pageMapping.urladapter != null && !pageMapping.urladapter.isEmpty()){
                    target = "LEGACY"
                }else{
                    target = "INTERNAL"
                }
            }
            context.sql.executeUpdate("UPDATE form_mapping SET target=$target WHERE tenantId = $formMapping.tenantId and id = $formMapping.id")
        }
    }

    @Override
    String getDescription() {
        return "Migrate form mappings to add a the undefined/none state"
    }
}
