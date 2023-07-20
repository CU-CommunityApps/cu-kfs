package edu.cornell.kfs.fp.document.service;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;

public interface CuDisbursementVoucherCheckStubService {

    boolean doesCheckStubNeedTruncatingForIso20022(DisbursementVoucherDocument document);

    void addNoteToDocumentRegardingCheckStubIso20022MaxLength(DisbursementVoucherDocument document);

    String createWarningMessageForCheckStubIso20022MaxLength();

    int getCheckStubMaxLengthForIso20022();

}
