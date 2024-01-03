/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.impex.xml.XmlConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.xml.KualiDecimalJaxbAdapter;
import org.kuali.kfs.kew.xml.SqlDateJaxbAdapter;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}source_doc_nbr"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}invoice_nbr" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}po_nbr" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}req_nbr" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}org_doc_nbr" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}customer_institution_identifier" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}invoice_date" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}orig_invoice_amt" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}net_payment_amt" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}invoice_tot_discount_amt" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}invoice_tot_ship_amt" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}invoice_tot_other_debits" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}invoice_tot_other_credits" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}fs_origin_cd" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}fdoc_typ_cd" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}accounting" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}payment_text" maxOccurs="26" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"custPaymentDocNbr",
    "invoiceNbr",
    "purchaseOrderNbr",
    "requisitionNbr",
    "organizationDocNbr",
    // CU customization, removed "customerInstitutionNumber",
    "invoiceDate",
    "origInvoiceAmount",
    "netPaymentAmount",
    "invTotDiscountAmount",
    "invTotShipAmount",
    "invTotOtherDebitAmount",
    "invTotOtherCreditAmount",
    "financialSystemOriginCode",
    "financialDocumentTypeCode",
    "accountDetail",
    "paymentText"})
@XmlRootElement(name = "detail", namespace = XmlConstants.PAYMENT_NAMESPACE)
public class PaymentDetail extends PersistableBusinessObjectBase {

    @XmlTransient
    private static final Logger LOG = LogManager.getLogger();
    @XmlTransient
    private static final KualiDecimal zero = KualiDecimal.ZERO;

    @XmlTransient
    private KualiInteger id;

    @XmlElement(name = "invoice_nbr", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String invoiceNbr;

    @XmlElement(name = "invoice_date", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(SqlDateJaxbAdapter.class)
    private Date invoiceDate;

    @XmlElement(name = "po_nbr", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String purchaseOrderNbr;

    @XmlElement(name = "source_doc_nbr", namespace = XmlConstants.PAYMENT_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String custPaymentDocNbr;

    @XmlElement(name = "fs_origin_cd", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String financialSystemOriginCode;

    @XmlElement(name = "fdoc_typ_cd", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String financialDocumentTypeCode;

    @XmlElement(name = "req_nbr", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String requisitionNbr;

    @XmlElement(name = "org_doc_nbr", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String organizationDocNbr;

    // CU customization; changed this field to transient as it was moved on PaymentGroup
    @XmlTransient
    private String customerInstitutionNumber;

    @XmlElement(name = "orig_invoice_amt", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalJaxbAdapter.class)
    private KualiDecimal origInvoiceAmount;

    @XmlElement(name = "net_payment_amt", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalJaxbAdapter.class)
    private KualiDecimal netPaymentAmount;

    @XmlElement(name = "invoice_tot_discount_amt", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalJaxbAdapter.class)
    private KualiDecimal invTotDiscountAmount;

    @XmlElement(name = "invoice_tot_ship_amt", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalJaxbAdapter.class)
    private KualiDecimal invTotShipAmount;

    @XmlElement(name = "invoice_tot_other_debits", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalJaxbAdapter.class)
    private KualiDecimal invTotOtherDebitAmount;

    @XmlElement(name = "invoice_tot_other_credits", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalJaxbAdapter.class)
    private KualiDecimal invTotOtherCreditAmount;

    @XmlTransient
    private Boolean primaryCancelledPayment;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE, required = true, name = "accounting")
    private List<PaymentAccountDetail> accountDetail = new ArrayList<>();

    @XmlElement(name = "payment_text", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected List<String> paymentText;

    @XmlTransient
    private List<PaymentNoteText> notes = new ArrayList<>();
    @XmlTransient
    private KualiInteger paymentGroupId;
    @XmlTransient
    private PaymentGroup paymentGroup;

    public PaymentDetail() {
    }

    public boolean isDetailAmountProvided() {
        return origInvoiceAmount != null || invTotDiscountAmount != null || invTotShipAmount != null
               || invTotOtherDebitAmount != null || invTotOtherCreditAmount != null;
    }

    public KualiDecimal getCalculatedPaymentAmount() {
        final KualiDecimal orig_invoice_amt = origInvoiceAmount == null ? zero : origInvoiceAmount;
        final KualiDecimal invoice_tot_discount_amt = invTotDiscountAmount == null ? zero : invTotDiscountAmount;
        final KualiDecimal invoice_tot_ship_amt = invTotShipAmount == null ? zero : invTotShipAmount;
        final KualiDecimal invoice_tot_other_debits = invTotOtherDebitAmount == null ? zero : invTotOtherDebitAmount;
        final KualiDecimal invoice_tot_other_credits = invTotOtherCreditAmount == null ? zero : invTotOtherCreditAmount;

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

        final String daysStr = SpringContext.getBean(ParameterService.class)
                .getParameterValueAsString(PaymentDetail.class, PdpParameterConstants.DISBURSEMENT_CANCELLATION_DAYS);
        final int days = Integer.parseInt(daysStr);

        final ZonedDateTime lastDisbursementActionDateTime = getDateTimeService()
                .getLocalDateTimeAtStartOfDay(getDateTimeService().getLocalDateNow().minusDays(days))
                .atZone(ZoneId.systemDefault());
        final Timestamp lastDisbursementActionDate =
                new Timestamp(lastDisbursementActionDateTime.toInstant().toEpochMilli());

        final ZonedDateTime disbursementDateTime = getDateTimeService()
                .getLocalDateTimeAtEndOfDay(getDateTimeService().getLocalDate(paymentGroup.getDisbursementDate()))
                .atZone(ZoneId.systemDefault());
        final Timestamp disbursementDate =
                new Timestamp(disbursementDateTime.toInstant().toEpochMilli());

        // date is equal to or after lastActionDate Allowed
        return disbursementDate.compareTo(lastDisbursementActionDate) >= 0;
    }

    /**
     * Wrapper for addNote, loops through the list of notes passed in and calls that.
     *
     * @param pnts list of notes to add
     */
    public void addNotes(final List<PaymentNoteText> pnts) {
        pnts.forEach(n -> {
            LOG.debug("addNotes() Creating check stub text note: {}", n::getCustomerNoteText);
            addNote(n);
        });
    }

    /**
     * @return total of all account detail amounts.
     */
    public KualiDecimal getAccountTotal() {
        KualiDecimal acctTotal = new KualiDecimal(0.00);

        for (final PaymentAccountDetail paymentAccountDetail : accountDetail) {
            if (paymentAccountDetail.getAccountNetAmount() != null) {
                acctTotal = acctTotal.add(paymentAccountDetail.getAccountNetAmount());
            }
        }

        return acctTotal;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(final Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    /**
     * Takes a {@link String} and attempt to format as {@link Timestamp} for setting the invoiceDate field.
     *
     * @param invoiceDate Timestamp as string
     */
    public void setInvoiceDate(final String invoiceDate) throws ParseException {
        this.invoiceDate = SpringContext.getBean(DateTimeService.class).convertToSqlDate(invoiceDate);
    }

    public List<PaymentAccountDetail> getAccountDetail() {
        return accountDetail;
    }

    public void setAccountDetail(final List<PaymentAccountDetail> accountDetail) {
        this.accountDetail = accountDetail;
    }

    public void addAccountDetail(final PaymentAccountDetail paymentAccountDetail) {
        paymentAccountDetail.setPaymentDetail(this);
        accountDetail.add(paymentAccountDetail);
    }

    public void deleteAccountDetail(final PaymentAccountDetail paymentAccountDetail) {
        accountDetail.remove(paymentAccountDetail);
    }

    public List<PaymentNoteText> getNotes() {
        return notes;
    }

    public void setNotes(final List<PaymentNoteText> notes) {
        this.notes = notes;
    }

    public void addNote(final PaymentNoteText paymentNoteText) {
        if (StringUtils.isNotBlank(paymentNoteText.getCustomerNoteText())) {
            paymentNoteText.setPaymentDetail(this);
            notes.add(paymentNoteText);
        } else {
            LOG.warn(
                    "Did not add note to payment detail build from Document #: {} because note was empty",
                    StringUtils.isNotBlank(custPaymentDocNbr) ? custPaymentDocNbr : ""
            );
        }
    }

    /**
     * Constructs a new {@link PaymentNoteText} for the given payment text and adds to the detail {@link List}.
     *
     * @param paymentText note text
     */
    public void addPaymentText(final String paymentText) {
        final PaymentNoteText paymentNoteText = new PaymentNoteText();

        paymentNoteText.setCustomerNoteText(paymentText);
        paymentNoteText.setCustomerNoteLineNbr(new KualiInteger(notes.size() + 1));

        addNote(paymentNoteText);
    }

    public void deleteNote(final PaymentNoteText paymentNoteText) {
        notes.remove(paymentNoteText);
    }

    // CU customization to add extended attribute
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

    public void setCustPaymentDocNbr(final String custPaymentDocNbr) {
        this.custPaymentDocNbr = custPaymentDocNbr;
    }

    public void setId(final KualiInteger id) {
        this.id = id;
    }

    public void setInvoiceNbr(final String invoiceNbr) {
        this.invoiceNbr = invoiceNbr;
    }

    public void setInvTotDiscountAmount(final KualiDecimal invTotDiscountAmount) {
        this.invTotDiscountAmount = invTotDiscountAmount;
    }

    public void setInvTotDiscountAmount(final String invTotDiscountAmount) {
        this.invTotDiscountAmount = new KualiDecimal(invTotDiscountAmount);
    }

    public void setInvTotOtherCreditAmount(final KualiDecimal invTotOtherCreditAmount) {
        this.invTotOtherCreditAmount = invTotOtherCreditAmount;
    }

    public void setInvTotOtherCreditAmount(final String invTotOtherCreditAmount) {
        this.invTotOtherCreditAmount = new KualiDecimal(invTotOtherCreditAmount);
    }

    public void setInvTotOtherDebitAmount(final KualiDecimal invTotOtherDebitAmount) {
        this.invTotOtherDebitAmount = invTotOtherDebitAmount;
    }

    public void setInvTotOtherDebitAmount(final String invTotOtherDebitAmount) {
        this.invTotOtherDebitAmount = new KualiDecimal(invTotOtherDebitAmount);
    }

    public void setInvTotShipAmount(final KualiDecimal invTotShipAmount) {
        this.invTotShipAmount = invTotShipAmount;
    }

    public void setInvTotShipAmount(final String invTotShipAmount) {
        this.invTotShipAmount = new KualiDecimal(invTotShipAmount);
    }

    public void setNetPaymentAmount(final KualiDecimal netPaymentAmount) {
        this.netPaymentAmount = netPaymentAmount;
    }

    public void setNetPaymentAmount(final String netPaymentAmount) {
        this.netPaymentAmount = new KualiDecimal(netPaymentAmount);
    }

    public void setOrganizationDocNbr(final String organizationDocNbr) {
        this.organizationDocNbr = organizationDocNbr;
    }

    public void setOrigInvoiceAmount(final KualiDecimal origInvoiceAmount) {
        this.origInvoiceAmount = origInvoiceAmount;
    }

    public void setOrigInvoiceAmount(final String origInvoiceAmount) {
        this.origInvoiceAmount = new KualiDecimal(origInvoiceAmount);
    }

    public void setPurchaseOrderNbr(final String purchaseOrderNbr) {
        this.purchaseOrderNbr = purchaseOrderNbr;
    }

    public void setRequisitionNbr(final String requisitionNbr) {
        this.requisitionNbr = requisitionNbr;
    }

    public String getFinancialDocumentTypeCode() {
        return financialDocumentTypeCode;
    }

    public void setFinancialDocumentTypeCode(final String financialDocumentTypeCode) {
        this.financialDocumentTypeCode = financialDocumentTypeCode;
    }

    public Boolean getPrimaryCancelledPayment() {
        return primaryCancelledPayment;
    }

    public void setPrimaryCancelledPayment(final Boolean primaryCancelledPayment) {
        this.primaryCancelledPayment = primaryCancelledPayment;
    }

    public void setPaymentGroup(final PaymentGroup paymentGroup) {
        this.paymentGroup = paymentGroup;
    }

    public KualiInteger getPaymentGroupId() {
        return paymentGroupId;
    }

    public void setPaymentGroupId(final KualiInteger paymentGroupId) {
        this.paymentGroupId = paymentGroupId;
    }

    public String getFinancialSystemOriginCode() {
        return financialSystemOriginCode;
    }

    public void setFinancialSystemOriginCode(final String financialSystemOriginCode) {
        this.financialSystemOriginCode = financialSystemOriginCode;
    }

    public String getCustomerInstitutionNumber() {
        return customerInstitutionNumber;
    }

    public void setCustomerInstitutionNumber(final String customerInstitutionNumber) {
        this.customerInstitutionNumber = customerInstitutionNumber;
    }

    /**
     * @return the String representation of the payment detail notes.
     */
    public String getNotesText() {
        final StringBuffer notes = new StringBuffer();
        final List<PaymentNoteText> notesList = this.notes;
        for (final PaymentNoteText note : notesList) {
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
            final List<PaymentGroup> paymentGroupList = SpringContext.getBean(PaymentGroupService.class)
                    .getByDisbursementNumber(paymentGroup.getDisbursementNbr().intValue());
            for (final PaymentGroup paymentGroup : paymentGroupList) {
                nbrOfPaymentsInDisbursement += paymentGroup.getPaymentDetails().size();
            }
        }
        return nbrOfPaymentsInDisbursement;
    }

    /**
     * Gets the value of the paymentText property.
     */
    public List<String> getXmlPaymentText() {
        if (paymentText == null) {
            paymentText = new ArrayList<>();
        }
        return Collections.unmodifiableList(this.paymentText);
    }

}
