package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cornell.kfs.rass.RassConstants.RassResultCode;

public class RassXmlObjectGroupResult {

    private final Class<?> businessObjectClass;
    private final List<RassXmlObjectResult> objectResults;
    private final RassResultCode resultCode;

    public RassXmlObjectGroupResult(
            Class<?> businessObjectClass, List<RassXmlObjectResult> objectResults, RassResultCode resultCode) {
        this.businessObjectClass = businessObjectClass;
        this.objectResults = Collections.unmodifiableList(new ArrayList<>(objectResults));
        this.resultCode = resultCode;
    }

    public Class<?> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public List<RassXmlObjectResult> getObjectResults() {
        return objectResults;
    }

    public RassResultCode getResultCode() {
        return resultCode;
    }

}
