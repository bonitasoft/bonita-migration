import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.CreateApplicationTables
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.UpdateProfileEntries

new CreateApplicationTables().migrate(feature, dbVendor, sql)

new UpdateProfileEntries().migrate(feature, dbVendor, sql)
