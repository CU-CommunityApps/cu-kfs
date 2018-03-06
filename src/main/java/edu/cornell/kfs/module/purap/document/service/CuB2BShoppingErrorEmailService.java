package edu.cornell.kfs.module.purap.document.service;

import java.util.ArrayList;

import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.RequisitionDocument;

public interface CuB2BShoppingErrorEmailService {

    void sendDuplicateRequisitionAccountErrorEmail(RequisitionDocument requisitionDocument, String errorMessage);
}
