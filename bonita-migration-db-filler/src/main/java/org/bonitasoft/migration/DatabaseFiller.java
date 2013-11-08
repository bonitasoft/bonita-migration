package org.bonitasoft.migration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.APITestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseFiller {

    private final Logger logger = LoggerFactory.getLogger(DatabaseFiller.class);

    public static void main(final String[] args) throws Exception {
        DatabaseFiller databaseFiller = new DatabaseFiller();
        databaseFiller.execute();
    }

    private void execute() throws Exception {
        logger.info("Using bonita.home: " + System.getProperty("bonita.home"));
        TestsInitializer.beforeAll();
        logger.info("Starting to fill the database");
        APISession session = APITestUtil.loginDefaultTenant();
        ArrayList<String> stats = new ArrayList<String>();
        stats.addAll(fillOrganization(session));
        APITestUtil.logoutTenant(session);
        logger.info("Finished to fill the database");
        for (String string : stats) {
            logger.info(string);
        }
        System.exit(0);

    }

    private List<String> fillOrganization(final APISession session) throws Exception {
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        InputStream acme = this.getClass().getResourceAsStream("/org/bonitasoft/engine/identity/ACME.xml");
        identityAPI.importOrganization(IOUtil.read(acme));
        return Arrays.asList("Users: " + identityAPI.getNumberOfUsers(), "Groups: " + identityAPI.getNumberOfGroups(),
                "Roles: " + identityAPI.getNumberOfRoles());
    }

}
