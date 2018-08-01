package edu.cornell.kfs.fp.batch.xml.fixture;

import org.apache.commons.lang.StringUtils;

public enum CuDisbursementVoucherDocumentFixture {
    EMPTY(),
    BASIC("DISB", "Doe, Jane");
    
    public final String bankCode;
    public final String contactName;
    
    private CuDisbursementVoucherDocumentFixture() {
        this.bankCode = StringUtils.EMPTY;
        this.contactName = StringUtils.EMPTY;
    }
    
    private CuDisbursementVoucherDocumentFixture(String bankCode, String contactName) {
        this.bankCode = bankCode;
        this.contactName = contactName;
    }
}
