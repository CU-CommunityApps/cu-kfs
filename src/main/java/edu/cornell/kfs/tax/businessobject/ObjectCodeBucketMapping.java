package edu.cornell.kfs.tax.businessobject;

import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

/**
 * Business object that maps a KFS object code to a 1099 tax bucket.
 * 
 * financialObjectCode and dvPaymentReasonCode form the primary key.
 * If mapping to any or no payment reason, use "*" as the value.
 */
public class ObjectCodeBucketMapping extends PersistableBusinessObjectBase implements MutableInactivatable {
    private static final long serialVersionUID = 4077748575570651411L;

    private String financialObjectCode;
    private String dvPaymentReasonCode;
    private String boxNumber;
    private boolean active;
    private String formType;

    public ObjectCodeBucketMapping() {
        super();
    }



    /**
     * Returns the object code to be mapped.
     */
    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    /**
     * Returns the DV payment reason code that the object mapping should be limited to,
     * or "*" to match any or no payment reason.
     */
    public String getDvPaymentReasonCode() {
        return dvPaymentReasonCode;
    }

    public void setDvPaymentReasonCode(String dvPaymentReasonCode) {
        this.dvPaymentReasonCode = dvPaymentReasonCode;
    }

    /**
     * Returns the tax box number that the object code should be mapped to.
     */
    public String getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

}
