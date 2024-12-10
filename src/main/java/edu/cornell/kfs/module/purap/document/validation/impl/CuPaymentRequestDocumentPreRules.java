package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.document.validation.impl.PaymentRequestDocumentPreRules;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.pdp.service.CuCheckStubService;

public class CuPaymentRequestDocumentPreRules extends PaymentRequestDocumentPreRules {

    private CuCheckStubService cuCheckStubService;

    @Override
    public boolean doPrompts(final Document document) {
        boolean preRulesOK = true;

        preRulesOK &= getCuCheckStubService().performPreRulesValidationOfIso20022CheckStubLength(document, this);
        preRulesOK &= super.doPrompts(document);
        return preRulesOK;
    }

    public CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }

}
