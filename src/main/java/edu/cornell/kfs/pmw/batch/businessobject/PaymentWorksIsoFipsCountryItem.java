package edu.cornell.kfs.pmw.batch.businessobject;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

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
        StringBuilder sb = new StringBuilder();
        sb.append("isoCountryCode").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(isoCountryCode).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
        sb.append("isoCountryName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(isoCountryName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
        sb.append("fipsCountryCode").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(fipsCountryCode).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
        sb.append("fipsCountryName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(fipsCountryName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
        return sb.toString();
    }

}
