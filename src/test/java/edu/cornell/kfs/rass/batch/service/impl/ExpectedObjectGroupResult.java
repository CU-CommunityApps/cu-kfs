package edu.cornell.kfs.rass.batch.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.cornell.kfs.rass.RassConstants.RassResultCode;

public final class ExpectedObjectGroupResult<E extends Enum<E>> {
    private final Class<?> businessObjectClass;
    private final RassResultCode resultCode;
    private final List<ExpectedObjectResult<E>> expectedObjectResults;
    
    public ExpectedObjectGroupResult(Class<?> businessObjectClass, RassResultCode resultCode,
            ExpectedObjectResult<E>[] expectedObjectResults) {
        this.businessObjectClass = businessObjectClass;
        this.resultCode = resultCode;
        this.expectedObjectResults = Collections.unmodifiableList(Arrays.asList(expectedObjectResults));
    }

    public Class<?> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public RassResultCode getResultCode() {
        return resultCode;
    }

    public List<ExpectedObjectResult<E>> getExpectedObjectResults() {
        return expectedObjectResults;
    }

}
