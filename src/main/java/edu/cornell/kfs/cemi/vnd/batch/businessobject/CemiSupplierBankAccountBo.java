package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiSupplierBankAccountBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String settlementBankAccountId;
    private String bankAccountNickname;
    private String bankAccountType;
    private String bankName;
    private String routingTransitNumber;
    private String branchId;
    private String branchName;
    private String bankAccountNumber;
    private String acceptsPaymentTypes1;
    private String acceptsPaymentTypes2;
    private String acceptsPaymentTypes3;
    private String paymentTypes1;
    private String paymentTypes2;
    private String paymentTypes3;

    public String getSettlementBankAccountId() {
        return settlementBankAccountId;
    }

    public void setSettlementBankAccountId(final String settlementBankAccountId) {
        this.settlementBankAccountId = settlementBankAccountId;
    }

    public String getBankAccountNickname() {
        return bankAccountNickname;
    }

    public void setBankAccountNickname(final String bankAccountNickname) {
        this.bankAccountNickname = bankAccountNickname;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(final String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(final String bankName) {
        this.bankName = bankName;
    }

    public String getRoutingTransitNumber() {
        return routingTransitNumber;
    }

    public void setRoutingTransitNumber(final String routingTransitNumber) {
        this.routingTransitNumber = routingTransitNumber;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(final String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(final String branchName) {
        this.branchName = branchName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(final String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getAcceptsPaymentTypes1() {
        return acceptsPaymentTypes1;
    }

    public void setAcceptsPaymentTypes1(final String acceptsPaymentTypes1) {
        this.acceptsPaymentTypes1 = acceptsPaymentTypes1;
    }

    public String getAcceptsPaymentTypes2() {
        return acceptsPaymentTypes2;
    }

    public void setAcceptsPaymentTypes2(final String acceptsPaymentTypes2) {
        this.acceptsPaymentTypes2 = acceptsPaymentTypes2;
    }

    public String getAcceptsPaymentTypes3() {
        return acceptsPaymentTypes3;
    }

    public void setAcceptsPaymentTypes3(final String acceptsPaymentTypes3) {
        this.acceptsPaymentTypes3 = acceptsPaymentTypes3;
    }

    public String getPaymentTypes1() {
        return paymentTypes1;
    }

    public void setPaymentTypes1(final String paymentTypes1) {
        this.paymentTypes1 = paymentTypes1;
    }

    public String getPaymentTypes2() {
        return paymentTypes2;
    }

    public void setPaymentTypes2(final String paymentTypes2) {
        this.paymentTypes2 = paymentTypes2;
    }

    public String getPaymentTypes3() {
        return paymentTypes3;
    }

    public void setPaymentTypes3(final String paymentTypes3) {
        this.paymentTypes3 = paymentTypes3;
    }

}
