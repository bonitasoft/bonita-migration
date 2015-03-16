import org.bonitasoft.migration.versions.v6_3_2_to_6_3_3.ResetFailedGateways;


/*
 * Fix gateways that are in failed state because of wrong restart of them on server restart
 * see BS-9367
 */

new ResetFailedGateways().migrate(dbVendor,sql)
