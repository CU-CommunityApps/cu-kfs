package edu.cornell.kfs.module.ar.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;

/*
 * CUMod: KFSPTS-15342
 */
public class CustomerExtendedAttribute extends PersistableBusinessObjectExtensionBase {

    private static final long serialVersionUID = -6758800807674053445L;

    private String customerNumber;
    private Integer netTermsInDays;

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public Integer getNetTermsInDays() {
        return netTermsInDays;
    }

    public void setNetTermsInDays(Integer netTermsInDays) {
        this.netTermsInDays = netTermsInDays;
    }

}
