/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2017 Kuali, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.businessobject;

import edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.web.format.CurrencyFormatter;
import org.kuali.rice.core.web.format.DateFormatter;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
/**
 * Payment Request View Business Object.
 */
public class PaymentRequestView extends AbstractRelatedView {

    private String invoiceNumber;
    private Integer purchaseOrderIdentifier;
    private boolean paymentHoldIndicator;
    private boolean paymentRequestedCancelIndicator;
    private String vendorName;
    private String vendorCustomerNumber;
    private Date paymentRequestPayDate;
    private Timestamp paymentExtractedTimestamp;
    private Timestamp paymentPaidTimestamp;

    // GETTERS & SETTERS
    public Object getTotalAmount() {
        return (new CurrencyFormatter()).format(documentHeader.getFinancialDocumentTotalAmount());
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Timestamp getPaymentExtractedTimestamp() {
        return paymentExtractedTimestamp;
    }

    public void setPaymentExtractedTimestamp(Timestamp paymentExtractedTimestamp) {
        this.paymentExtractedTimestamp = paymentExtractedTimestamp;
    }

    public boolean isPaymentHoldIndicator() {
        return paymentHoldIndicator;
    }

    public void setPaymentHoldIndicator(boolean paymentHoldIndicator) {
        this.paymentHoldIndicator = paymentHoldIndicator;
    }

    public Timestamp getPaymentPaidTimestamp() {
        return paymentPaidTimestamp;
    }

    public void setPaymentPaidTimestamp(Timestamp paymentPaidTimestamp) {
        this.paymentPaidTimestamp = paymentPaidTimestamp;
    }

    public boolean isPaymentRequestedCancelIndicator() {
        return paymentRequestedCancelIndicator;
    }

    public void setPaymentRequestedCancelIndicator(boolean paymentRequestedCancelIndicator) {
        this.paymentRequestedCancelIndicator = paymentRequestedCancelIndicator;
    }

    public Object getPaymentRequestPayDate() {
        return new DateFormatter().format(paymentRequestPayDate);
    }

    public void setPaymentRequestPayDate(Date paymentRequestPayDate) {
        this.paymentRequestPayDate = paymentRequestPayDate;
    }

    public Integer getPurchaseOrderIdentifier() {
        return purchaseOrderIdentifier;
    }

    public void setPurchaseOrderIdentifier(Integer purchaseOrderIdentifier) {
        this.purchaseOrderIdentifier = purchaseOrderIdentifier;
    }

    public String getVendorCustomerNumber() {
        return vendorCustomerNumber;
    }

    public void setVendorCustomerNumber(String vendorCustomerNumber) {
        this.vendorCustomerNumber = vendorCustomerNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    /**
     * @return workflow document type for the PaymentRequestDocument
     */
    public String getDocumentType() {
        return KFSConstants.FinancialDocumentTypeCodes.PAYMENT_REQUEST;
    }

    /**
     * The next three methods are overridden but shouldnt be! If they arent overridden, they dont show up in the tag, not sure why
     * at this point! (AAP)
     */
    @Override
    public Integer getPurapDocumentIdentifier() {
        return super.getPurapDocumentIdentifier();
    }

    @Override
    public String getDocumentIdentifierString() {
        return super.getDocumentIdentifierString();
    }

    /**
     * @see org.kuali.kfs.module.purap.businessobject.AbstractRelatedView#getNotes()j
     * This is overriden to prevent duplicate fetching of the object id needed to fetch notes
     * which becomes a problem when you have a lot of associated payment requests with a
     * given purchase order
     */
    @Override
    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<Note>();
        //reverse the order of notes only when anything exists in it..
        NoteService noteService = SpringContext.getBean(NoteService.class);
        // ==== CU Customization: Use a CU-specific PREQ service method to get the note target object ID, instead of always using the doc header one. ====
        CuPaymentRequestService cuPaymentRequestService = SpringContext.getBean(CuPaymentRequestService.class);
        List<Note> tmpNotes = noteService.getByRemoteObjectId(cuPaymentRequestService.getPaymentRequestNoteTargetObjectId(getDocumentNumber()));
        notes.clear();
        // reverse the order of notes retrieved so that newest note is in the front
        for (int i = tmpNotes.size()-1; i>=0; i--) {
            Note note = tmpNotes.get(i);
            notes.add(note);
        }
        return notes;
    }

    /**
     * @see org.kuali.kfs.module.purap.businessobject.AbstractRelatedView#getUrl()
     */
    @Override
    public String getUrl() {
        return super.getUrl();
    }

    /**
     * @see org.kuali.kfs.module.purap.businessobject.AbstractRelatedView#getDocumentTypeName()
     */
    @Override
    public String getDocumentTypeName() {
        return KFSConstants.FinancialDocumentTypeCodes.PAYMENT_REQUEST;
    }

}
