/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.module.ar.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerInvoiceDetail;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceWriteoffDocumentService;
import org.kuali.kfs.module.ar.document.service.InvoicePaidAppliedService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.sys.context.SpringContext;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a customer invoice detail on the customer invoice document. This class extends SourceAccountingLine since
 * each customer invoice detail has associated accounting line information.
 */
/*Cornell customization: backport redis implementation */
public class CustomerInvoiceDetail extends SourceAccountingLine implements AppliedPayment, AccountsReceivableCustomerInvoiceDetail {
    private static final Logger LOG = LogManager.getLogger();

    private BigDecimal invoiceItemQuantity;
    private BigDecimal invoiceItemUnitPrice;
    private Date invoiceItemServiceDate;
    private String invoiceItemCode;
    private String invoiceItemDescription;
    private String accountsReceivableObjectCode;
    private String accountsReceivableSubObjectCode;
    private KualiDecimal invoiceItemTaxAmount = KualiDecimal.ZERO;

    private boolean taxableIndicator;
    private boolean isDebit;
    private Integer invoiceItemDiscountLineNumber;

    private String invoiceItemUnitOfMeasureCode;
    private UnitOfMeasure unitOfMeasure;

    private SubObjectCode accountsReceivableSubObject;
    private ObjectCode accountsReceivableObject;

    private transient DocumentService documentService;
    private transient InvoicePaidAppliedService invoicePaidAppliedService;
    private transient CustomerInvoiceDocument customerInvoiceDocument;
    private transient CustomerInvoiceDetail parentDiscountCustomerInvoiceDetail;
    private transient CustomerInvoiceDetail discountCustomerInvoiceDetail;

    // fields used for CustomerInvoiceWriteoffDocument
    private KualiDecimal writeoffAmount;
    private String customerInvoiceWriteoffDocumentNumber;

    // ---- BEGIN OPEN AMOUNTS

    public KualiDecimal getAmountOpen() {
        //  if the parent isn't saved, or if its saved but not approved, we
        // need to include the discounts.  If its both saved AND approved, we do not include the discounts.
        boolean includeDiscounts = !(isParentSaved() && isParentApproved());

        KualiDecimal amount = getAmount();
        KualiDecimal applied = getAmountApplied();
        KualiDecimal a = amount.subtract(applied);

        if (includeDiscounts) {
            CustomerInvoiceDetail discount = getDiscountCustomerInvoiceDetail();
            if (ObjectUtils.isNotNull(discount)) {
                a = a.add(discount.getAmount());
            }
        }
        return a;
    }

    private boolean isParentSaved() {
        return getCustomerInvoiceDocument() != null;
    }

    private boolean isParentApproved() {
        if (getCustomerInvoiceDocument() == null) {
            return false;
        }
        return KFSConstants.DocumentStatusCodes.APPROVED.equalsIgnoreCase(getCustomerInvoiceDocument().getFinancialSystemDocumentHeader().getFinancialDocumentStatusCode());
    }

    /**
     * Retrieves the discounted amount.  This is the amount minutes any discounts that might exist.  If no discount
     * exists, then it just returns the amount.
     * <p>
     * NOTE this does not subtract PaidApplieds, only discounts.
     *
     * @return
     */
    //PAYAPP
    public KualiDecimal getAmountDiscounted() {
        KualiDecimal a = getAmount();
        CustomerInvoiceDetail discount = getDiscountCustomerInvoiceDetail();
        if (ObjectUtils.isNotNull(discount)) {
            KualiDecimal d = discount.getAmount();
            a = a.add(d);
        }
        return a;
    }

    /**
     * This method returns the amount that remained unapplied on a given date.
     *
     * @param date
     * @return
     */
    public KualiDecimal getAmountOpenByDateFromDatabase(java.sql.Date date) {
        return getAmountOpen();
    }

    public KualiDecimal getAmountOpenByDateFromDatabase(java.util.Date date) {
        return getAmountOpen();
    }

    public KualiDecimal getAmountApplied() {
        return getActiveInvoicePaidAppliedsMatchingAnyDocumentFromDatabase(true)
                .stream()
                .map(InvoicePaidApplied::getInvoiceItemAppliedAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
    }

    public KualiDecimal getAmountAppliedIncludingPendingPayments() {
        return getActiveInvoicePaidAppliedsMatchingAnyDocumentFromDatabase(false)
                .stream()
                .map(InvoicePaidApplied::getInvoiceItemAppliedAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
    }

    /**
     * @param documentNumber
     * @return the sum of applied amounts from the document identified by the provided doc number.
     */
    public KualiDecimal getAmountAppliedBy(String documentNumber) {
        List<InvoicePaidApplied> invoicePaidApplieds;
        if (StringUtils.isBlank(documentNumber)) {
            invoicePaidApplieds = getMatchingInvoicePaidAppliedsMatchingAnyDocumentFromDatabase();
        } else {
            invoicePaidApplieds = getMatchingInvoicePaidAppliedsMatchingDocument(documentNumber);
        }
        KualiDecimal appliedAmount = new KualiDecimal(0);
        for (InvoicePaidApplied invoicePaidApplied : invoicePaidApplieds) {
            appliedAmount = appliedAmount.add(invoicePaidApplied.getInvoiceItemAppliedAmount());
        }
        return appliedAmount;
    }

    /**
     * @param documentNumber
     * @return the sum of applied amounts according to the database, excluding any amounts applied by
     *         the provided document number
     */
    public KualiDecimal getAmountAppliedExcludingAnyAmountAppliedBy(String documentNumber) {
        List<InvoicePaidApplied> invoicePaidApplieds = getMatchingInvoicePaidAppliedsMatchingAnyDocumentFromDatabase();
        KualiDecimal appliedAmount = new KualiDecimal(0);
        for (InvoicePaidApplied invoicePaidApplied : invoicePaidApplieds) {
            // Exclude any amounts applied by paymentApplicationDocument
            if (StringUtils.isBlank(documentNumber)
                    || !documentNumber.equalsIgnoreCase(invoicePaidApplied.getDocumentNumber())) {
                appliedAmount = appliedAmount.add(invoicePaidApplied.getInvoiceItemAppliedAmount());
            }
        }
        return appliedAmount;
    }

    /**
     * @return the writeoff amount. If writeoff document hasn't been approved yet, display the open amount. Else
     *         display the amount applied from the specific approved writeoff document.
     */
    public KualiDecimal getWriteoffAmount() {
        if (SpringContext.getBean(CustomerInvoiceWriteoffDocumentService.class)
                .isCustomerInvoiceWriteoffDocumentApproved(customerInvoiceWriteoffDocumentNumber)) {
            //TODO this probably isn't right ... in the case of discounts and/or credit memos, the getAmount() isn't
            // the amount that the writeoff document will have written off

            // using the accounting line amount ... see comments at top of class
            return super.getAmount();
        } else {
            return getAmountOpen();
        }
    }

    public KualiDecimal getInvoiceItemPreTaxAmount() {
        if (ObjectUtils.isNotNull(invoiceItemUnitPrice) && ObjectUtils.isNotNull(invoiceItemQuantity)) {
            BigDecimal bd = invoiceItemUnitPrice.multiply(invoiceItemQuantity);
            bd = bd.setScale(KualiDecimal.SCALE, KualiDecimal.ROUND_BEHAVIOR);
            return new KualiDecimal(bd);
        } else {
            return KualiDecimal.ZERO;
        }
    }

    public String getAccountsReceivableObjectCode() {
        return accountsReceivableObjectCode;
    }

    @Override
    public void setAccountsReceivableObjectCode(String accountsReceivableObjectCode) {
        this.accountsReceivableObjectCode = accountsReceivableObjectCode;
    }

    public String getAccountsReceivableSubObjectCode() {
        return accountsReceivableSubObjectCode;
    }

    public void setAccountsReceivableSubObjectCode(String accountsReceivableSubObjectCode) {
        this.accountsReceivableSubObjectCode = accountsReceivableSubObjectCode;
    }

    public BigDecimal getInvoiceItemQuantity() {
        return invoiceItemQuantity;
    }

    @Override
    public void setInvoiceItemQuantity(BigDecimal invoiceItemQuantity) {
        this.invoiceItemQuantity = invoiceItemQuantity;
    }

    public String getInvoiceItemUnitOfMeasureCode() {
        return invoiceItemUnitOfMeasureCode;
    }

    public void setInvoiceItemUnitOfMeasureCode(String invoiceItemUnitOfMeasureCode) {
        this.invoiceItemUnitOfMeasureCode = invoiceItemUnitOfMeasureCode;
    }

    public BigDecimal getInvoiceItemUnitPrice() {
        return invoiceItemUnitPrice;
    }

    @Override
    public void setInvoiceItemUnitPrice(KualiDecimal invoiceItemUnitPrice) {
        if (ObjectUtils.isNotNull(invoiceItemUnitPrice)) {
            this.invoiceItemUnitPrice = invoiceItemUnitPrice.bigDecimalValue();
        } else {
            this.invoiceItemUnitPrice = BigDecimal.ZERO;
        }
    }

    public void setInvoiceItemUnitPrice(BigDecimal invoiceItemUnitPrice) {
        this.invoiceItemUnitPrice = invoiceItemUnitPrice;
    }

    public Date getInvoiceItemServiceDate() {
        return invoiceItemServiceDate;
    }

    public void setInvoiceItemServiceDate(Date invoiceItemServiceDate) {
        this.invoiceItemServiceDate = invoiceItemServiceDate;
    }

    public String getInvoiceItemCode() {
        return invoiceItemCode;
    }

    public void setInvoiceItemCode(String invoiceItemCode) {
        this.invoiceItemCode = invoiceItemCode;
    }

    public String getInvoiceItemDescription() {
        return invoiceItemDescription;
    }

    public void setInvoiceItemDescription(String invoiceItemDescription) {
        this.invoiceItemDescription = invoiceItemDescription;
    }

    public KualiDecimal getInvoiceItemTaxAmount() {
        return invoiceItemTaxAmount;
    }

    public void setInvoiceItemTaxAmount(KualiDecimal invoiceItemTaxAmount) {
        this.invoiceItemTaxAmount = invoiceItemTaxAmount;
    }

    public Integer getInvoiceItemDiscountLineNumber() {
        return invoiceItemDiscountLineNumber;
    }

    public void setInvoiceItemDiscountLineNumber(Integer invoiceItemDiscountLineNumber) {
        this.invoiceItemDiscountLineNumber = invoiceItemDiscountLineNumber;
    }

    public SubObjectCode getAccountsReceivableSubObject() {
        return accountsReceivableSubObject;
    }

    @Deprecated
    public void setAccountsReceivableSubObject(SubObjectCode accountsReceivableSubObject) {
        this.accountsReceivableSubObject = accountsReceivableSubObject;
    }

    public ObjectCode getAccountsReceivableObject() {
        return accountsReceivableObject;
    }

    @Deprecated
    public void setAccountsReceivableObject(ObjectCode accountsReceivableObject) {
        this.accountsReceivableObject = accountsReceivableObject;
    }

    /**
     * Update line amount based on quantity and unit price
     */
    @Override
    public void updateAmountBasedOnQuantityAndUnitPrice() {
        setAmount(getInvoiceItemPreTaxAmount());
    }

    public boolean isTaxableIndicator() {
        return taxableIndicator;
    }

    // yes this is redundant, its required for the JSP on the accounting line checkbox field
    public boolean getTaxableIndicator() {
        return taxableIndicator;
    }

    public void setTaxableIndicator(boolean taxableIndicator) {
        this.taxableIndicator = taxableIndicator;
    }

    public boolean isDebit() {
        return isDebit;
    }

    public void setDebit(boolean isDebit) {
        this.isDebit = isDebit;
    }

    /**
     * @return true if customer invoice detail has a corresponding discount line
     */
    public boolean isDiscountLineParent() {
        return ObjectUtils.isNotNull(getInvoiceItemDiscountLineNumber());
    }

    /**
     * This method should only be used to determine if detail is discount line in JSP. If you want to determine if invoice detail is
     * a detail line use CustomerInvoiceDocument.isDiscountLineBasedOnSequenceNumber() instead.
     *
     * @return
     */
    public boolean isDiscountLine() {
        return ObjectUtils.isNotNull(parentDiscountCustomerInvoiceDetail);
    }

    /**
     * This method sets the amount to negative if it isn't already negative
     *
     * @return
     */
    public void setInvoiceItemUnitPriceToNegative() {
        // if unit price is positive
        if (invoiceItemUnitPrice.compareTo(BigDecimal.ZERO) == 1) {
            invoiceItemUnitPrice = invoiceItemUnitPrice.negate();
        }
    }

    public CustomerInvoiceDetail getParentDiscountCustomerInvoiceDetail() {
        return parentDiscountCustomerInvoiceDetail;
    }

    public void setParentDiscountCustomerInvoiceDetail(CustomerInvoiceDetail parentDiscountCustomerInvoiceDetail) {
        this.parentDiscountCustomerInvoiceDetail = parentDiscountCustomerInvoiceDetail;
    }

    public CustomerInvoiceDetail getDiscountCustomerInvoiceDetail() {
        return discountCustomerInvoiceDetail;
    }

    public void setDiscountCustomerInvoiceDetail(CustomerInvoiceDetail discountCustomerInvoiceDetail) {
        this.discountCustomerInvoiceDetail = discountCustomerInvoiceDetail;
    }

    /**
     * This method takes into account the possibility of an Invoice Paid applied to be adjusted. If a given
     * InvoicePaidApplied has additional InvoicePaidApplied in adjuster documents, then we ignore the amount applied to
     * that invoice and use the last non-adjusted application
     *
     * @return A list of InvoicePaidApplied that do not have amounts that have been adjusted by other InvoicePaidApplieds
     */
    public List<InvoicePaidApplied> getActiveInvoicePaidAppliedsMatchingAnyDocumentFromDatabase(boolean onlyApprovedPayments) {
        return getInvoicePaidAppliedService().filterInvoicePaidAppliedsToOnlyActive(
                getMatchingInvoicePaidAppliedsMatchingAnyDocumentFromDatabase(onlyApprovedPayments));
    }

    /**
     * @return matching approved InvoicePaidApplieds from the database if they exist
     */
    public List<InvoicePaidApplied> getMatchingInvoicePaidAppliedsMatchingAnyDocumentFromDatabase() {
        return getMatchingInvoicePaidAppliedsMatchingAnyDocumentFromDatabase(true);
    }

    public List<InvoicePaidApplied> getMatchingInvoicePaidAppliedsMatchingAnyDocumentFromDatabase(boolean onlyApprovedPayments) {
        BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);

        // assuming here that you never have a PaidApplied against a Discount line
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("invoiceItemNumber", getInvoiceItemNumber());
        criteria.put("financialDocumentReferenceInvoiceNumber", getDocumentNumber());

        if (onlyApprovedPayments) {
            criteria.put("documentHeader.financialDocumentStatusCode", KFSConstants.DocumentStatusCodes.APPROVED);
        }

        List<InvoicePaidApplied> invoicePaidApplieds = (List<InvoicePaidApplied>) businessObjectService.findMatching(InvoicePaidApplied.class, criteria);
        if (ObjectUtils.isNull(invoicePaidApplieds)) {
            invoicePaidApplieds = new ArrayList<>();
        }
        return invoicePaidApplieds;
    }

    /**
     * @param documentNumber
     * @return the List of matching InvoicePaidApplieds.
     * If documentNumber is null invoicePaidApplieds matching any document will be returned.
     * If documentNumber is not null only the invoicePaidApplieds that match on that document will be returned.
     */
    private List<InvoicePaidApplied> getMatchingInvoicePaidAppliedsMatchingDocument(String documentNumber) {
        if (StringUtils.isBlank(documentNumber)) {
            return getMatchingInvoicePaidAppliedsMatchingAnyDocumentFromDatabase();
        }

        BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("documentNumber", documentNumber);
        criteria.put("invoiceItemNumber", getSequenceNumber());
        criteria.put("financialDocumentReferenceInvoiceNumber", getDocumentNumber());

        List<InvoicePaidApplied> invoicePaidApplieds = (List<InvoicePaidApplied>) businessObjectService.findMatching(InvoicePaidApplied.class, criteria);
        if (ObjectUtils.isNull(invoicePaidApplieds)) {
            invoicePaidApplieds = new ArrayList<>();
        }
        return invoicePaidApplieds;
    }

    public CustomerInvoiceDocument getCustomerInvoiceDocument() {
        if (customerInvoiceDocument == null) {
            customerInvoiceDocument = (CustomerInvoiceDocument) getDocumentService().getByDocumentHeaderId(getDocumentNumber());
        }
        return customerInvoiceDocument;
    }

    public void setCustomerInvoiceDocument(CustomerInvoiceDocument customerInvoiceDocument) {
        this.customerInvoiceDocument = customerInvoiceDocument;
    }

    public String getCustomerInvoiceWriteoffDocumentNumber() {
        return customerInvoiceWriteoffDocumentNumber;
    }

    public void setCustomerInvoiceWriteoffDocumentNumber(String customerInvoiceWriteoffDocumentNumber) {
        this.customerInvoiceWriteoffDocumentNumber = customerInvoiceWriteoffDocumentNumber;
    }

    public void setWriteoffAmount(KualiDecimal writeoffAmount) {
        this.writeoffAmount = writeoffAmount;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    /**
     * If the detail is a discount customer invoice detail, return the parent customer invoice detail's sequence
     * number instead
     */
    @Override
    public Integer getInvoiceItemNumber() {
        if (isDiscountLine()) {
            return parentDiscountCustomerInvoiceDetail.getSequenceNumber();
        } else {
            return this.getSequenceNumber();
        }
    }

    /**
     * If detail is part of an invoice that is a reversal, return the invoice that is being corrected. Else return the
     * customer details document number.
     */
    @Override
    public String getInvoiceReferenceNumber() {
        return getDocumentNumber();
    }

    @Override
    public void refresh() {
        super.refresh();
        this.updateAmountBasedOnQuantityAndUnitPrice();
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        super.setDocumentNumber(documentNumber);
    }

    public InvoicePaidAppliedService getInvoicePaidAppliedService() {
        if (invoicePaidAppliedService == null) {
            invoicePaidAppliedService = SpringContext.getBean(InvoicePaidAppliedService.class);
        }
        return invoicePaidAppliedService;
    }

    public DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = SpringContext.getBean(DocumentService.class);
        }
        return documentService;
    }
}

