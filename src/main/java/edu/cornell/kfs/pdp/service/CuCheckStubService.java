package edu.cornell.kfs.pdp.service;

import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;

public interface CuCheckStubService {

    boolean doesCheckStubNeedTruncatingForIso20022(Document document);

    void addNoteToDocumentRegardingCheckStubIso20022MaxLength(Document document);

    void addIso20022CheckStubLengthWarningToDocumentIfNecessary(Document document);

    String createWarningMessageForCheckStubIso20022MaxLength(Document document);

    boolean performPreRulesValidationOfIso20022CheckStubLength(Document document,
            PromptBeforeValidationBase documentPreRules);

    String getFullCheckStub(Document document);

    String getFullCheckStub(PaymentDetail paymentDetail);

    int getCheckStubMaxLengthForIso20022();

}
