package edu.cornell.kfs.paymentworks;

public class PaymentWorksVendorUpdateResults {

    private boolean hasErrors;

    public PaymentWorksVendorUpdateResults() {
        hasErrors = false;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

}
