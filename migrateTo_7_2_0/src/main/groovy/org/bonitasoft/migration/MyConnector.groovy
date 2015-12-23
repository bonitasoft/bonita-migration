package org.bonitasoft.migration

import org.bonitasoft.engine.connector.AbstractConnector
import org.bonitasoft.engine.connector.ConnectorException
import org.bonitasoft.engine.connector.ConnectorValidationException

/**
 * @author Baptiste Mesta
 */
public class MyConnector extends AbstractConnector {


    public MyConnector() {

    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        setOutputParameter("outputValue", getInputParameter("input1"))
    }

    @Override
    void validateInputParameters() throws ConnectorValidationException {

    }

}
