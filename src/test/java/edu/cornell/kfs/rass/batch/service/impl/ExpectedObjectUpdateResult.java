package edu.cornell.kfs.rass.batch.service.impl;

import java.util.function.Function;

import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;

public final class ExpectedObjectUpdateResult<E extends Enum<E>> {

    private final String primaryKey;
    private final RassObjectUpdateResultCode resultCode;
    private final E businessObjectFixture;

    public ExpectedObjectUpdateResult(E businessObjectFixture, RassObjectUpdateResultCode resultCode, Function<E, String> primaryKeyGetter) {
        this.primaryKey = primaryKeyGetter.apply(businessObjectFixture);
        this.resultCode = resultCode;
        this.businessObjectFixture = businessObjectFixture;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public RassObjectUpdateResultCode getResultCode() {
        return resultCode;
    }

    public E getBusinessObjectFixture() {
        return businessObjectFixture;
    }

    public String getExpectedMaintenanceAction() {
        switch (resultCode) {
            case SUCCESS_NEW :
                return KRADConstants.MAINTENANCE_NEW_ACTION;
            case SUCCESS_EDIT :
                return KRADConstants.MAINTENANCE_EDIT_ACTION;
            default :
                throw new IllegalStateException("No maintenance action mapping exists for result code " + resultCode);
        }
    }

}
