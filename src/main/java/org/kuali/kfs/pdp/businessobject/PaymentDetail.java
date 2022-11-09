/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on Jul 12, 2004
 *
 */
package org.kuali.kfs.pdp.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PaymentDetail extends PersistableBusinessObjectBase {

    private static final Logger LOG = LogManager.getLogger();
    private static KualiDecimal zero = KualiDecimal.ZERO;

    private KualiInteger id;
    private String invoiceNbr;
    private Date invoiceDate;
    private String purchaseOrderNbr;
    private String custPaymentDocNbr;
    private String financialSystemOriginCode;
    private String financialDocumentTypeCode;
    private String requisitionNbr;
    private String organizationDocNbr;
    private String customerInstitutionNumber;
    private KualiDecimal origInvoiceAmount;
    private KualiDecimal netPaymentAmount;
    private KualiDecimal invTotDiscountAmount;
    private KualiDecimal invTotShipAmount;
    private KualiDecimal invTotOtherDebitAmount;
    private KualiDecimal invTotOtherCreditAmount;
    private Boolean primaryCancelledPayment;

    private List<PaymentAccountDetail> accountDetail = new ArrayList<>();
    private List<PaymentNoteText> notes = new ArrayList<>();

    private KualiInteger paymentGroupId;
    private PaymentGroup paymentGroup;

    public PaymentDetail() {
        super();
    }

    public boolean isDetailAmountProvided() {
        return origInvoiceAmount != null || invTotDiscountAmount != null || invTotShipAmount != null
                || invTotOtherDebitAmount != null || invTotOtherCreditAmount != null;
    }

    public KualiDecimal getCalculatedPaymentAmount() {
        KualiDecimal orig_invoice_amt = origInvoiceAmount == null ? zero : origInvoiceAmount;
        KualiDecimal invoice_tot_discount_amt = invTotDiscountAmount == null ? zero : invTotDiscountAmount;
        KualiDecimal invoice_tot_ship_amt = invTotShipAmount == null ? zero : invTotShipAmount;
        KualiDecimal invoice_tot_other_debits = invTotOtherDebitAmount == null ? zero : invTotOtherDebitAmount;
        KualiDecimal invoice_tot_other_credits = invTotOtherCreditAmount == null ? zero : invTotOtherCreditAmount;

        KualiDecimal t = orig_invoice_amt.subtract(invoice_tot_discount_amt);
        t = t.add(invoice_tot_ship_amt);
        t = t.add(invoice_tot_other_debits);
        t = t.subtract(invoice_tot_other_credits);

        return t;
    }

    /**
     * Determines if the disbursement date is past the number of days old (configured in system parameter) in which
     * actions can take place.
     *
     * @return true if actions are allowed on disbursement, false otherwise
     */
    public boolean isDisbursementActionAllowed() {
        if (paymentGroup.getDisbursementDate() == null) {
            return !PdpConstants.PaymentStatusCodes.EXTRACTED.equals(paymentGroup.getPaymentStatus().getCode());
        }

        String daysStr = SpringContext.getBean(ParameterService.class).getParameterValueAsString(PaymentDetail.class,
                PdpParameterConstants.DISBURSEMENT_CANCELLATION_DAYS);
        int days = Integer.parseInt(daysStr);

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, days * -1);
        c.set(Calendar.HOUR, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.AM_PM, Calendar.AM);
        Timestamp lastDisbursementActionDate = new Timestamp(c.getTimeInMillis());

        Calendar c2 = Calendar.getInstance();
        c2.setTime(paymentGroup.getDisbursementDate());
        c2.set(Calendar.HOUR, 11);
        c2.set(Calendar.MINUTE, 59);
        c2.set(Calendar.SECOND, 59);
        c2.set(Calendar.MILLISECOND, 59);
        c2.set(Calendar.AM_PM, Calendar.PM);
        Timestamp disbursementDate = new Timestamp(c2.getTimeInMillis());

        // date is equal to or after lastActionDate Allowed
        return disbursementDate.compareTo(lastDisbursementActionDate) >= 0;
    }

    /**
     * Wrapper for addNote, loops through the list of notes passed in and calls that.
     *
     * @param pnts list of notes to add
     */
    public void addNotes(List<PaymentNoteText> pnts) {
        pnts.forEach(n -> {
            LOG.debug("addNotes() Creating check stub text note: " + n.getCustomerNoteText());
            addNote(n);
        });
    }

    /**
     * @return total of all account detail amounts.
     */
    public KualiDecimal getAccountTotal() {
        KualiDecimal acctTotal = new KualiDecimal(0.00);

        for (PaymentAccountDetail paymentAccountDetail : accountDetail) {
            if (paymentAccountDetail.getAccountNetAmount() != null) {
                acctTotal = acctTotal.add(paymentAccountDetail.getAccountNetAmount());
            }
        }

        return acctTotal;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    /**
     * Takes a {@link String} and attempt to format as {@link Timestamp} for setting the invoiceDate field.
     *
     * @param invoiceDate Timestamp as string
     */
    public void setInvoiceDate(String invoiceDate) throws ParseException {
        this.invoiceDate = SpringContext.getBean(DateTimeService.class).convertToSqlDate(invoiceDate);
    }

    public List<PaymentAccountDetail> getAccountDetail() {
        return accountDetail;
    }

    public void setAccountDetail(List<PaymentAccountDetail> ad) {
        accountDetail = ad;
    }

    public void addAccountDetail(PaymentAccountDetail pad) {
        pad.setPaymentDetail(this);
        accountDetail.add(pad);
    }

    public void deleteAccountDetail(PaymentAccountDetail pad) {
        accountDetail.remove(pad);
    }

    public List<PaymentNoteText> getNotes() {
        return notes;
    }

    public void setNotes(List<PaymentNoteText> n) {
        notes = n;
    }

    public void addNote(PaymentNoteText pnt) {
        if (!StringUtils.isBlank(pnt.getCustomerNoteText())) {
            pnt.setPaymentDetail(this);
            notes.add(pnt);
        } else {
            LOG.warn("Did not add note to payment detail build from Document #: " + (!StringUtils.isBlank(
                    custPaymentDocNbr) ? custPaymentDocNbr : "") + " because note was empty");
        }
    }

    /**
     * Constructs a new {@link PaymentNoteText} for the given payment text and adds to the detail {@link List}.
     *
     * @param paymentText note text
     */
    public void addPaymentText(String paymentText) {
        PaymentNoteText paymentNoteText = new PaymentNoteText();

        paymentNoteText.setCustomerNoteText(paymentText);
        paymentNoteText.setCustomerNoteLineNbr(new KualiInteger(this.notes.size() + 1));

        addNote(paymentNoteText);
    }

    public void deleteNote(PaymentNoteText pnt) {
        notes.remove(pnt);
    }

    @Override
    public void afterInsert() {
        super.afterInsert();
        // add extended attribute
        PaymentDetailExtendedAttribute paymentDetailExtendedAttribute = new PaymentDetailExtendedAttribute();
        paymentDetailExtendedAttribute.setId(this.getId());
        paymentDetailExtendedAttribute.setCrCancelledPayment(false);
        this.setExtension(paymentDetailExtendedAttribute);

        SpringContext.getBean(BusinessObjectService.class).save(paymentDetailExtendedAttribute);
    }

    public KualiInteger getId() {
        return id;
    }

    public String getCustPaymentDocNbr() {
        return custPaymentDocNbr;
    }

    public String getInvoiceNbr() {
        return invoiceNbr;
    }

    public KualiDecimal getInvTotDiscountAmount() {
        return invTotDiscountAmount;
    }

    public KualiDecimal getInvTotOtherCreditAmount() {
        return invTotOtherCreditAmount;
    }

    public KualiDecimal getInvTotOtherDebitAmount() {
        return invTotOtherDebitAmount;
    }

    public KualiDecimal getInvTotShipAmount() {
        return invTotShipAmount;
    }

    public KualiDecimal getNetPaymentAmount() {
        return netPaymentAmount;
    }

    public String getOrganizationDocNbr() {
        return organizationDocNbr;
    }

    public KualiDecimal getOrigInvoiceAmount() {
        return origInvoiceAmount;
    }

    public String getPurchaseOrderNbr() {
        return purchaseOrderNbr;
    }

    public String getRequisitionNbr() {
        return requisitionNbr;
    }

    public PaymentGroup getPaymentGroup() {
        return paymentGroup;
    }

    public void setCustPaymentDocNbr(String string) {
        custPaymentDocNbr = string;
    }

    public void setId(KualiInteger integer) {
        id = integer;
    }

    public void setInvoiceNbr(String string) {
        invoiceNbr = string;
    }

    public void setInvTotDiscountAmount(KualiDecimal decimal) {
        invTotDiscountAmount = decimal;
    }

    public void setInvTotDiscountAmount(String decimal) {
        invTotDiscountAmount = new KualiDecimal(decimal);
    }

    public void setInvTotOtherCreditAmount(KualiDecimal decimal) {
        invTotOtherCreditAmount = decimal;
    }

    public void setInvTotOtherCreditAmount(String decimal) {
        invTotOtherCreditAmount = new KualiDecimal(decimal);
    }

    public void setInvTotOtherDebitAmount(KualiDecimal decimal) {
        invTotOtherDebitAmount = decimal;
    }

    public void setInvTotOtherDebitAmount(String decimal) {
        invTotOtherDebitAmount = new KualiDecimal(decimal);
    }

    public void setInvTotShipAmount(KualiDecimal decimal) {
        invTotShipAmount = decimal;
    }

    public void setInvTotShipAmount(String decimal) {
        invTotShipAmount = new KualiDecimal(decimal);
    }

    public void setNetPaymentAmount(KualiDecimal decimal) {
        netPaymentAmount = decimal;
    }

    public void setNetPaymentAmount(String decimal) {
        netPaymentAmount = new KualiDecimal(decimal);
    }

    public void setOrganizationDocNbr(String string) {
        organizationDocNbr = string;
    }

    public void setOrigInvoiceAmount(KualiDecimal decimal) {
        origInvoiceAmount = decimal;
    }

    public void setOrigInvoiceAmount(String decimal) {
        origInvoiceAmount = new KualiDecimal(decimal);
    }

    public void setPurchaseOrderNbr(String string) {
        purchaseOrderNbr = string;
    }

    public void setRequisitionNbr(String string) {
        requisitionNbr = string;
    }

    public String getFinancialDocumentTypeCode() {
        return financialDocumentTypeCode;
    }

    public void setFinancialDocumentTypeCode(String financialDocumentTypeCode) {
        this.financialDocumentTypeCode = financialDocumentTypeCode;
    }

    public Boolean getPrimaryCancelledPayment() {
        return primaryCancelledPayment;
    }

    public void setPrimaryCancelledPayment(Boolean primaryCancelledPayment) {
        this.primaryCancelledPayment = primaryCancelledPayment;
    }

    public void setPaymentGroup(PaymentGroup paymentGroup) {
        this.paymentGroup = paymentGroup;
    }

    public KualiInteger getPaymentGroupId() {
        return paymentGroupId;
    }

    public void setPaymentGroupId(KualiInteger paymentGroupId) {
        this.paymentGroupId = paymentGroupId;
    }

    public String getFinancialSystemOriginCode() {
        return financialSystemOriginCode;
    }

    public void setFinancialSystemOriginCode(String financialSystemOriginCode) {
        this.financialSystemOriginCode = financialSystemOriginCode;
    }

    public String getCustomerInstitutionNumber() {
        return customerInstitutionNumber;
    }

    public void setCustomerInstitutionNumber(String customerInstitutionNumber) {
        this.customerInstitutionNumber = customerInstitutionNumber;
    }

    /**
     * @return the String representation of the payment detail notes.
     */
    public String getNotesText() {
        StringBuffer notes = new StringBuffer();
        List<PaymentNoteText> notesList = getNotes();
        for (PaymentNoteText note : notesList) {
            notes.append(note.getCustomerNoteText());
            notes.append(KFSConstants.NEWLINE);
        }
        return notes.toString();
    }

    /**
     * @return the number of payments in the payment group associated with this payment detail.
     */
    public int getNbrOfPaymentsInPaymentGroup() {
        return paymentGroup.getPaymentDetails().size();
    }

    /**
     * @return the number of payments in the disbursement associated with this payment detail.
     */
    public int getNbrOfPaymentsInDisbursement() {
        int nbrOfPaymentsInDisbursement = 0;
        if (ObjectUtils.isNotNull(paymentGroup.getDisbursementNbr())) {
            List<PaymentGroup> paymentGroupList = SpringContext.getBean(PaymentGroupService.class)
                    .getByDisbursementNumber(paymentGroup.getDisbursementNbr().intValue());
            for (PaymentGroup paymentGroup : paymentGroupList) {
                nbrOfPaymentsInDisbursement += paymentGroup.getPaymentDetails().size();
            }
        }
        return nbrOfPaymentsInDisbursement;
    }

}
