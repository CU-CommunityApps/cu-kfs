package edu.cornell.kfs.module.purap.service.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.service.impl.PdpExtractServiceImpl;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.kfs.kim.api.identity.Person;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class CuPdpExtractServiceImpl extends PdpExtractServiceImpl {
    private DocumentTypeService documentTypeService;
    
    @Override
    protected void updatePaymentRequest(PaymentRequestDocument paymentRequestDocument, Person puser, Date processRunDate) {
        PaymentRequestDocument doc = (PaymentRequestDocument) documentService.getByDocumentHeaderId(paymentRequestDocument.getDocumentNumber());
        doc.setExtractedTimestamp(new Timestamp(processRunDate.getTime()));
        getPurapService().saveDocumentNoValidation(doc);
        
        DocumentType documentType = documentTypeService.getDocumentTypeByName(doc.getFinancialDocumentTypeCode());
        DocumentAttributeIndexingQueue queue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
        queue.indexDocument(doc.getDocumentNumber());
    }
    
    @Override
    protected void addNotes(AccountsPayableDocument accountsPayableDocument, PaymentDetail paymentDetail) {
        int count = 1;

        if (accountsPayableDocument instanceof PaymentRequestDocument) {
            PaymentRequestDocument prd = (PaymentRequestDocument) accountsPayableDocument;

            if (prd.getSpecialHandlingInstructionLine1Text() != null) {
                PaymentNoteText pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
                pnt.setCustomerNoteText(CUPurapConstants.SPECIAL_HANDLING_NOTE_LINE_1_NAME +  prd.getSpecialHandlingInstructionLine1Text());
                paymentDetail.addNote(pnt);
            }

            if (prd.getSpecialHandlingInstructionLine2Text() != null) {
                PaymentNoteText pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
                pnt.setCustomerNoteText(CUPurapConstants.SPECIAL_HANDLING_NOTE_LINE_2_ADDRESS + prd.getSpecialHandlingInstructionLine2Text());
                paymentDetail.addNote(pnt);
            }

            if (prd.getSpecialHandlingInstructionLine3Text() != null) {
                PaymentNoteText pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
                pnt.setCustomerNoteText(CUPurapConstants.SPECIAL_HANDLING_NOTE_LINE_3_CITY_STATE_ZIP + prd.getSpecialHandlingInstructionLine3Text());
                paymentDetail.addNote(pnt);
            }
        }

        if (accountsPayableDocument.getNoteLine1Text() != null) {
            PaymentNoteText pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
            pnt.setCustomerNoteText(CUPurapConstants.PURAP_NOTES_IDENTIFIER + accountsPayableDocument.getNoteLine1Text());
            paymentDetail.addNote(pnt);
        }

        if (accountsPayableDocument.getNoteLine2Text() != null) {
            PaymentNoteText pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
            pnt.setCustomerNoteText(CUPurapConstants.PURAP_NOTES_IDENTIFIER + accountsPayableDocument.getNoteLine2Text());
            paymentDetail.addNote(pnt);
        }

        if (accountsPayableDocument.getNoteLine3Text() != null) {
            PaymentNoteText pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
            pnt.setCustomerNoteText(CUPurapConstants.PURAP_NOTES_IDENTIFIER + accountsPayableDocument.getNoteLine3Text());
            paymentDetail.addNote(pnt);
        }

        PaymentNoteText pnt = new PaymentNoteText();
        pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
        pnt.setCustomerNoteText("Sales Tax: " + accountsPayableDocument.getTotalRemitTax());
    }
    
    @Override
    protected PaymentGroup populatePaymentGroup(PaymentRequestDocument paymentRequestDocument, Batch batch) {
        PaymentGroup paymentGroup = super.populatePaymentGroup(paymentRequestDocument, batch);
        
        if (paymentGroup.isPayableByACH()) {
            paymentGroup.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.ACH);
        } else {
            paymentGroup.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.CHECK);
        }
        
        return paymentGroup;
    }
    
    @Override
    protected PaymentGroup populatePaymentGroup(VendorCreditMemoDocument creditMemoDocument, Batch batch) {
        PaymentGroup paymentGroup = super.populatePaymentGroup(creditMemoDocument, batch);
        
        if (paymentGroup.isPayableByACH()) {
            paymentGroup.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.ACH);
        } else {
            paymentGroup.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.CHECK);
        }
        
        return paymentGroup;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }
}
