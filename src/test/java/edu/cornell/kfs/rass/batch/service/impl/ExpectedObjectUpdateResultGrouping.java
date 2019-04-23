package edu.cornell.kfs.rass.batch.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.rass.RassConstants.RassObjectGroupingUpdateResultCode;

public final class ExpectedObjectUpdateResultGrouping<E extends Enum<E>, R extends PersistableBusinessObject> {
    private final Class<R> businessObjectClass;
    private final RassObjectGroupingUpdateResultCode resultCode;
    private final List<ExpectedObjectUpdateResult<E>> expectedObjectUpdateResults;
    
    public ExpectedObjectUpdateResultGrouping(Class<R> businessObjectClass, RassObjectGroupingUpdateResultCode resultCode,
            ExpectedObjectUpdateResult<E>[] expectedObjectUpdateResults) {
        this.businessObjectClass = businessObjectClass;
        this.resultCode = resultCode;
        this.expectedObjectUpdateResults = Collections.unmodifiableList(Arrays.asList(expectedObjectUpdateResults));
    }

    public Class<R> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public RassObjectGroupingUpdateResultCode getResultCode() {
        return resultCode;
    }

    public List<ExpectedObjectUpdateResult<E>> getExpectedObjectUpdateResults() {
        return expectedObjectUpdateResults;
    }

}
