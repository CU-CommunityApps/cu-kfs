package edu.cornell.kfs.pmw.batch.businessobject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class KfsToPMWSupplierDiversityDTO {
    
    private String kfsSupplierDiversityCode;
    private String kfsSupplierDiversityDescription;
    private String paymentWorksSuppliertDiversityDescription;
    
    public KfsToPMWSupplierDiversityDTO(String kfsSupplierDiversityCode, String kfsSupplierDiversityDescription,
            String paymentWorksSuppliertDiversityDescription) {
        super();
        this.kfsSupplierDiversityCode = kfsSupplierDiversityCode;
        this.kfsSupplierDiversityDescription = kfsSupplierDiversityDescription;
        this.paymentWorksSuppliertDiversityDescription = paymentWorksSuppliertDiversityDescription;
    }

    public String getKfsSupplierDiversityCode() {
        return kfsSupplierDiversityCode;
    }

    public void setKfsSupplierDiversityCode(String kfsSupplierDiversityCode) {
        this.kfsSupplierDiversityCode = kfsSupplierDiversityCode;
    }

    public String getKfsSupplierDiversityDescription() {
        return kfsSupplierDiversityDescription;
    }

    public void setKfsSupplierDiversityDescription(String kfsSupplierDiversityDescription) {
        this.kfsSupplierDiversityDescription = kfsSupplierDiversityDescription;
    }

    public String getPaymentWorksSuppliertDiversityDescription() {
        return paymentWorksSuppliertDiversityDescription;
    }

    public void setPaymentWorksSuppliertDiversityDescription(String paymentWorksSuppliertDiversityDescription) {
        this.paymentWorksSuppliertDiversityDescription = paymentWorksSuppliertDiversityDescription;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

}
