import org.bonitasoft.migration.versions.to_7_0_0.ApplicationTokenChecker
import org.bonitasoft.migration.versions.to_7_0_0.GlobalApplicationsMessageBuilder
import org.bonitasoft.migration.versions.to_7_0_0.InvalidApplicationPageTokenRetriever
import org.bonitasoft.migration.versions.to_7_0_0.InvalidApplicationTokenRetriever
import org.bonitasoft.migration.versions.to_7_0_0.TenantApplicationsMessageBuilder

def messageBuilder = new GlobalApplicationsMessageBuilder(
        [
                new InvalidApplicationTokenRetriever(sql, dbVendor),
                new InvalidApplicationPageTokenRetriever(sql, dbVendor)
        ], new TenantApplicationsMessageBuilder()
)
new ApplicationTokenChecker(reporter, messageBuilder).processInvalidTokens()


