package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.rass.RassConstants.RassObjectGroupingUpdateResultCode;

public class RassBusinessObjectUpdateResultGrouping<R extends PersistableBusinessObject> {

    private final Class<R> businessObjectClass;
    private final List<RassBusinessObjectUpdateResult<R>> objectResults;
    private final RassObjectGroupingUpdateResultCode resultCode;

    public RassBusinessObjectUpdateResultGrouping(
            Class<R> businessObjectClass, List<RassBusinessObjectUpdateResult<R>> objectResults,
            RassObjectGroupingUpdateResultCode resultCode) {
        this.businessObjectClass = businessObjectClass;
        this.objectResults = Collections.unmodifiableList(new ArrayList<>(objectResults));
        this.resultCode = resultCode;
    }

    public Class<R> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public List<RassBusinessObjectUpdateResult<R>> getObjectResults() {
        return objectResults;
    }

    public RassObjectGroupingUpdateResultCode getResultCode() {
        return resultCode;
    }

}
