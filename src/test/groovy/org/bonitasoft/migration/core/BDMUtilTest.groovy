package org.bonitasoft.migration.core

import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class BDMUtilTest extends Specification {


    public static
    final String CLIENT_BSM_ZIP = "/withBDM/engine-server/work/tenants/1/data-management-client/client-bdm.zip"

    def "GetBomXmlContent"() {
        setup:
        def bdmFile = new File(this.class.getResource(CLIENT_BSM_ZIP).file)
        def bdmUtil = new BDMUtil()

        when:
        def xmlBDM = bdmUtil.getBomDefinition(bdmFile)

        then:
        xmlBDM.businessObjects.businessObject[0].@qualifiedName == "com.company.model.BO1"
        xmlBDM.businessObjects.businessObject[1].@qualifiedName == "com.company.model.BO2"


    }
}
