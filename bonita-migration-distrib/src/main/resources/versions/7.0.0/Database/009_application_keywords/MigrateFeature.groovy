import org.bonitasoft.migration.versions.to_7_0_0.*

new ApplicationTokenChecker(reporter,
        new ApplicationMessageBuilder(new ApplicationRetriever(sql, dbVendor)),
                new ApplicationPageMessageBuilder(new ApplicationPageRetriever(sql, dbVendor))).processInvalidTokens()


