package edu.cornell.kfs.paymentworks.xmlObjects;

public class PaymentWorksUpdateVendorStatus {

    private Integer id;
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer requestStatus) {
        this.status = requestStatus;
    }

}
