package edu.cornell.kfs.module.purap.document.service;

import org.kuali.kfs.module.purap.document.RequisitionDocument;

public interface CuB2BShoppingErrorEmailService {

    void sendDuplicateRequisitionAccountErrorEmail(RequisitionDocument requisitionDocument, String errorMessage);
}
