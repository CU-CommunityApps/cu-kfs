package edu.cornell.kfs.pmw.batch.businessobject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PaymentWorksIsoFipsCountryItem {

    private String isoCountryCode;
    private String isoCountryName;
    private String fipsCountryCode;
    private String fipsCountryName;
    
    public PaymentWorksIsoFipsCountryItem() {
        this.isoCountryCode = null;
        this.isoCountryName = null;
        this.fipsCountryCode = null;
        this.fipsCountryName = null;
    }
    
    public PaymentWorksIsoFipsCountryItem(String isoCountryCode, String isoCountryName, String fipsCountryCode, String fipsCountryName) {
        this.isoCountryCode = isoCountryCode;
        this.isoCountryName = isoCountryName;
        this.fipsCountryCode = fipsCountryCode;
        this.fipsCountryName = fipsCountryName;
    }

    public String getIsoCountryCode() {
        return isoCountryCode;
    }

    public void setIsoCountryCode(String isoCountryCode) {
        this.isoCountryCode = isoCountryCode;
    }

    public String getIsoCountryName() {
        return isoCountryName;
    }

    public void setIsoCountryName(String isoCountryName) {
        this.isoCountryName = isoCountryName;
    }

    public String getFipsCountryCode() {
        return fipsCountryCode;
    }

    public void setFipsCountryCode(String fipsCountryCode) {
        this.fipsCountryCode = fipsCountryCode;
    }

    public String getFipsCountryName() {
        return fipsCountryName;
    }

    public void setFipsCountryName(String fipsCountryName) {
        this.fipsCountryName = fipsCountryName;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

}
