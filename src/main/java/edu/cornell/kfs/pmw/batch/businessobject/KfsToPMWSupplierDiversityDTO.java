package edu.cornell.kfs.pmw.batch.businessobject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class KfsToPMWSupplierDiversityDTO {
    
    private String kfsSuppliertDiversityCode;
    private String kfsSuppliertDiversityDescription;
    private String paymentWorksSuppliertDiversityDescription;
    
    public KfsToPMWSupplierDiversityDTO(String kfsSuppliertDiversityCode, String kfsSuppliertDiversityDescription,
            String paymentWorksSuppliertDiversityDescription) {
        super();
        this.kfsSuppliertDiversityCode = kfsSuppliertDiversityCode;
        this.kfsSuppliertDiversityDescription = kfsSuppliertDiversityDescription;
        this.paymentWorksSuppliertDiversityDescription = paymentWorksSuppliertDiversityDescription;
    }

    public String getKfsSuppliertDiversityCode() {
        return kfsSuppliertDiversityCode;
    }

    public void setKfsSuppliertDiversityCode(String kfsSuppliertDiversityCode) {
        this.kfsSuppliertDiversityCode = kfsSuppliertDiversityCode;
    }

    public String getKfsSuppliertDiversityDescription() {
        return kfsSuppliertDiversityDescription;
    }

    public void setKfsSuppliertDiversityDescription(String kfsSuppliertDiversityDescription) {
        this.kfsSuppliertDiversityDescription = kfsSuppliertDiversityDescription;
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
