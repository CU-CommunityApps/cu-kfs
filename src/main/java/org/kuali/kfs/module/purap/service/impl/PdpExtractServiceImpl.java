/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.module.purap.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.api.parameter.Parameter.Builder;
import org.kuali.kfs.coreservice.api.parameter.ParameterType;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.batch.service.PurapRunDateService;
import org.kuali.kfs.module.purap.businessobject.CreditMemoItem;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.CreditMemoService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.service.PdpExtractService;
import org.kuali.kfs.module.purap.util.VendorGroupingHelper;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.service.CustomerProfileService;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.pdp.service.PaymentFileService;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class PdpExtractServiceImpl implements PdpExtractService {

    private static final Logger LOG = LogManager.getLogger();

    protected PaymentRequestService paymentRequestService;
    protected BusinessObjectService businessObjectService;
    protected PaymentFileService paymentFileService;
    protected ParameterService parameterService;
    protected CustomerProfileService customerProfileService;
    protected DateTimeService dateTimeService;
    protected PersonService personService;
    protected PaymentGroupService paymentGroupService;
    protected PaymentDetailService paymentDetailService;
    protected CreditMemoService creditMemoService;
    protected DocumentService documentService;
    protected PurapRunDateService purapRunDateService;
    protected PdpEmailService paymentFileEmailService;
    protected BankService bankService;
    protected DataDictionaryService dataDictionaryService;
    protected PurapAccountingServiceImpl purapAccountingService;
    protected List<String> lockedDocuments;
    //CU customization change from private to protected
    protected PurapService purapService;

    @Override
    public void extractImmediatePaymentsOnly() {
        LOG.debug("extractImmediatePaymentsOnly() started");
        Date processRunDate = dateTimeService.getCurrentDate();
        lockUnlockDocuments(true);
        try {
            extractPayments(true, processRunDate);
        } finally {
            lockUnlockDocuments(false);
        }
    }

    @Override
    public void extractPayments(Date runDate) {
        LOG.debug("extractPayments() started");
        extractPayments(false, runDate);
    }

    /**
     * Extracts payments from the database
     *
     * @param immediateOnly  whether to pick up immediate payments only
     * @param processRunDate time/date to use to put on the {@link Batch} that's created; and when immediateOnly is
     *                       false, is also used as the maximum allowed PREQ pay date when searching PREQ documents
     *                       eligible to have payments extracted
     */
    protected void extractPayments(boolean immediateOnly, Date processRunDate) {
        LOG.debug("extractPayments() started");

        Person uuser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        if (uuser == null) {
            LOG.error("extractPayments() Unable to find user " + KFSConstants.SYSTEM_USER);
            throw new IllegalArgumentException("Unable to find user " + KFSConstants.SYSTEM_USER);
        }
        LOG.debug("fetched system user");
        List<String> campusesToProcess = getChartCodes(immediateOnly, processRunDate);
        for (String campus : campusesToProcess) {
            extractPaymentsForCampus(campus, uuser, processRunDate, immediateOnly);
        }
    }

    /**
     * Handle a single campus
     *
     * @param campusCode
     * @param puser
     * @param processRunDate
     */
    protected void extractPaymentsForCampus(String campusCode, Person puser, Date processRunDate,
            boolean immediateOnly) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractPaymentsForCampus() started for campus: " + campusCode);
        }

        Batch batch = createBatch(campusCode, puser, processRunDate);

        Integer count = 0;
        KualiDecimal totalAmount = KualiDecimal.ZERO;

        // Do all the special ones
        Totals totals = extractSpecialPaymentsForChart(campusCode, puser, processRunDate, batch, immediateOnly);
        count = count + totals.count;
        totalAmount = totalAmount.add(totals.totalAmount);

        if (!immediateOnly) {
            // Do all the regular ones (including credit memos)
            totals = extractRegularPaymentsForChart(campusCode, puser, processRunDate, batch);
            count = count + totals.count;
            totalAmount = totalAmount.add(totals.totalAmount);
        }

        batch.setPaymentCount(new KualiInteger(count));
        batch.setPaymentTotalAmount(totalAmount);

        businessObjectService.save(batch);
        paymentFileEmailService.sendLoadEmail(batch);
    }

    /**
     * Get all the payments that could be combined with credit memos
     *
     * @param campusCode
     * @param puser
     * @param processRunDate
     * @param batch
     * @return Totals
     */
    protected Totals extractRegularPaymentsForChart(String campusCode, Person puser, Date processRunDate, Batch batch) {
        LOG.debug("START - extractRegularPaymentsForChart()");

        Totals totals = new Totals();

        java.sql.Date onOrBeforePaymentRequestPayDate = KfsDateUtils.convertToSqlDate(
                purapRunDateService.calculateRunDate(processRunDate));

        List<String> preqsWithOutstandingCreditMemos = new ArrayList<>();

        Set<VendorGroupingHelper> vendors = creditMemoService.getVendorsOnCreditMemosToExtract(campusCode);
        for (VendorGroupingHelper vendor : vendors) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing Vendor: " + vendor);
            }

            Map<String, List<PaymentRequestDocument>> bankCodePaymentRequests = new HashMap<>();
            Map<String, List<VendorCreditMemoDocument>> bankCodeCreditMemos = new HashMap<>();

            // Get all the matching credit memos
            Collection<VendorCreditMemoDocument> vendorMemos = creditMemoService.getCreditMemosToExtractByVendor(
                    campusCode, vendor);
            for (VendorCreditMemoDocument cmd : vendorMemos) {
                List<VendorCreditMemoDocument> bankMemos = new ArrayList<>();
                if (bankCodeCreditMemos.containsKey(cmd.getBankCode())) {
                    bankMemos = bankCodeCreditMemos.get(cmd.getBankCode());
                }

                bankMemos.add(cmd);
                bankCodeCreditMemos.put(cmd.getBankCode(), bankMemos);
            }

            // get all matching payment requests
            Collection<PaymentRequestDocument> vendorPreqs =
                    paymentRequestService.getPaymentRequestsToExtractByVendor(campusCode, vendor,
                            onOrBeforePaymentRequestPayDate);
            for (PaymentRequestDocument prd : vendorPreqs) {
                List<PaymentRequestDocument> bankPreqs = new ArrayList<>();
                if (bankCodePaymentRequests.containsKey(prd.getBankCode())) {
                    bankPreqs = bankCodePaymentRequests.get(prd.getBankCode());
                }

                bankPreqs.add(prd);
                bankCodePaymentRequests.put(prd.getBankCode(), bankPreqs);
            }

            // if bank functionality enabled, create bundles by bank, else just by vendor
            if (bankService.isBankSpecificationEnabled()) {
                for (String bankCode : bankCodePaymentRequests.keySet()) {
                    // if we have credit memos with bank code, process together, else let the preq go and will get
                    // picked up below and processed as a single payment group
                    if (bankCodeCreditMemos.containsKey(bankCode)) {
                        processPaymentBundle(bankCodePaymentRequests.get(bankCode), bankCodeCreditMemos.get(bankCode),
                                totals, preqsWithOutstandingCreditMemos, puser, processRunDate, batch);
                    }
                }
            } else {
                if (!vendorMemos.isEmpty()) {
                    processPaymentBundle((List<PaymentRequestDocument>) vendorPreqs,
                            (List<VendorCreditMemoDocument>) vendorMemos, totals, preqsWithOutstandingCreditMemos,
                            puser, processRunDate, batch);
                }
            }
        }

        LOG.debug("processing PREQs without CMs");

        for (PaymentRequestDocument prd : paymentRequestService.getPaymentRequestToExtractByChart(
                campusCode, onOrBeforePaymentRequestPayDate)) {
            // if in the list created above, don't create the payment group
            if (!preqsWithOutstandingCreditMemos.contains(prd.getDocumentNumber())) {
                PaymentGroup paymentGroup = processSinglePaymentRequestDocument(prd, batch, puser, processRunDate);
                totals.count = totals.count + paymentGroup.getPaymentDetails().size();
                totals.totalAmount = totals.totalAmount.add(paymentGroup.getNetPaymentAmount());
            }
        }

        LOG.debug("END - extractRegularPaymentsForChart()");
        return totals;
    }

    /**
     * Processes the list of payment requests and credit memos as a payment group pending
     *
     * @param paymentRequests
     * @param creditMemos
     * @param totals
     * @param preqsWithOutstandingCreditMemos
     * @param puser
     * @param processRunDate
     * @param batch
     */
    protected void processPaymentBundle(List<PaymentRequestDocument> paymentRequests,
            List<VendorCreditMemoDocument> creditMemos, Totals totals, List<String> preqsWithOutstandingCreditMemos,
            Person puser, Date processRunDate, Batch batch) {
        KualiDecimal paymentRequestAmount = KualiDecimal.ZERO;
        for (PaymentRequestDocument paymentRequestDocument : paymentRequests) {
            paymentRequestAmount = paymentRequestAmount.add(paymentRequestDocument.getGrandTotal());
        }

        KualiDecimal creditMemoAmount = KualiDecimal.ZERO;
        for (VendorCreditMemoDocument creditMemoDocument : creditMemos) {
            creditMemoAmount = creditMemoAmount.add(creditMemoDocument.getCreditMemoAmount());
        }

        // if payment amount greater than credit, create bundle
        boolean bundleCreated = false;
        if (paymentRequestAmount.compareTo(creditMemoAmount) > 0) {
            PaymentGroup paymentGroup = buildPaymentGroup(paymentRequests, creditMemos, batch);

            if (validatePaymentGroup(paymentGroup)) {
                this.businessObjectService.save(paymentGroup);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created PaymentGroup: " + paymentGroup.getId());
                }

                totals.count++;
                totals.totalAmount = totals.totalAmount.add(paymentGroup.getNetPaymentAmount());

                // mark the CMs and PREQs as processed
                for (VendorCreditMemoDocument cm : creditMemos) {
                    updateCreditMemo(cm, puser, processRunDate);
                }

                for (PaymentRequestDocument pr : paymentRequests) {
                    updatePaymentRequest(pr, puser, processRunDate);
                }

                bundleCreated = true;
            }
        }

        if (!bundleCreated) {
            // add payment request doc numbers to list so they don't get picked up later
            for (PaymentRequestDocument doc : paymentRequests) {
                preqsWithOutstandingCreditMemos.add(doc.getDocumentNumber());
            }
        }
    }

    /**
     * Handle a single payment request with no credit memos
     *
     * @param paymentRequestDocument
     * @param batch
     * @param puser
     * @param processRunDate
     * @return PaymentGroup
     */
    protected PaymentGroup processSinglePaymentRequestDocument(PaymentRequestDocument paymentRequestDocument,
            Batch batch, Person puser, Date processRunDate) {
        List<PaymentRequestDocument> prds = new ArrayList<>();
        List<VendorCreditMemoDocument> cmds = new ArrayList<>();
        prds.add(paymentRequestDocument);

        PaymentGroup paymentGroup = buildPaymentGroup(prds, cmds, batch);
        if (validatePaymentGroup(paymentGroup)) {
            this.businessObjectService.save(paymentGroup);
            updatePaymentRequest(paymentRequestDocument, puser, processRunDate);
        }

        return paymentGroup;
    }

    /**
     * Get all the special payments for a campus and process them
     *
     * @param campusCode
     * @param puser
     * @param processRunDate
     * @param batch
     * @return Totals
     */
    protected Totals extractSpecialPaymentsForChart(String campusCode, Person puser, Date processRunDate, Batch batch,
            boolean immediatesOnly) {
        Totals totals = new Totals();

        Iterator<PaymentRequestDocument> paymentRequests;
        if (immediatesOnly) {
            paymentRequests = paymentRequestService.getImmediatePaymentRequestsToExtract(campusCode).iterator();
        } else {
            java.sql.Date onOrBeforePaymentRequestPayDate = KfsDateUtils.convertToSqlDate(
                    purapRunDateService.calculateRunDate(processRunDate));
            paymentRequests = paymentRequestService.getPaymentRequestsToExtractSpecialPayments(campusCode,
                    onOrBeforePaymentRequestPayDate).iterator();
        }

        while (paymentRequests.hasNext()) {
            PaymentRequestDocument prd = paymentRequests.next();
            PaymentGroup pg = processSinglePaymentRequestDocument(prd, batch, puser, processRunDate);

            totals.count = totals.count + pg.getPaymentDetails().size();
            totals.totalAmount = totals.totalAmount.add(pg.getNetPaymentAmount());
        }

        return totals;
    }

    /**
     * Mark a credit memo as extracted
     *
     * @param creditMemoDocument
     * @param puser
     * @param processRunDate
     */
    protected void updateCreditMemo(VendorCreditMemoDocument creditMemoDocument, Person puser, Date processRunDate) {
        try {
            VendorCreditMemoDocument doc = (VendorCreditMemoDocument) documentService.getByDocumentHeaderId(
                    creditMemoDocument.getDocumentNumber());
            doc.setExtractedTimestamp(new Timestamp(processRunDate.getTime()));
            purapService.saveDocumentNoValidation(doc);
        } catch (WorkflowException e) {
            throw new IllegalArgumentException("Unable to retrieve credit memo: " +
                    creditMemoDocument.getDocumentNumber());
        }
    }

    /**
     * Mark a payment request as extracted
     *
     * @param paymentRequestDocument
     * @param puser
     * @param processRunDate
     */
    protected void updatePaymentRequest(PaymentRequestDocument paymentRequestDocument, Person puser,
            Date processRunDate) {
        try {
            PaymentRequestDocument doc = (PaymentRequestDocument) documentService.getByDocumentHeaderId(
                    paymentRequestDocument.getDocumentNumber());
            doc.setExtractedTimestamp(new Timestamp(processRunDate.getTime()));
            purapService.saveDocumentNoValidation(doc);
        } catch (WorkflowException e) {
            throw new IllegalArgumentException("Unable to retrieve payment request: " +
                    paymentRequestDocument.getDocumentNumber());
        }
    }

    /**
     * Create the PDP payment group from a list of payment requests & credit memos
     *
     * @param paymentRequests
     * @param creditMemos
     * @param batch
     * @return PaymentGroup
     */
    protected PaymentGroup buildPaymentGroup(List<PaymentRequestDocument> paymentRequests,
            List<VendorCreditMemoDocument> creditMemos, Batch batch) {
        // There should always be at least one Payment Request Document in the list.
        PaymentGroup paymentGroup;
        if (creditMemos.size() > 0) {
            VendorCreditMemoDocument firstCmd = creditMemos.get(0);
            paymentGroup = populatePaymentGroup(firstCmd, batch);
        } else {
            PaymentRequestDocument firstPrd = paymentRequests.get(0);
            paymentGroup = populatePaymentGroup(firstPrd, batch);
        }

        for (PaymentRequestDocument pr : paymentRequests) {
            PaymentDetail pd = populatePaymentDetail(pr, batch);
            paymentGroup.addPaymentDetails(pd);
        }
        for (VendorCreditMemoDocument cm : creditMemos) {
            PaymentDetail pd = populatePaymentDetail(cm, batch);
            paymentGroup.addPaymentDetails(pd);
        }

        return paymentGroup;
    }

    /**
     * Checks payment group note lines does not exceed the maximum allowed
     *
     * @param paymentGroup group to validate
     * @return true if group is valid, false otherwise
     */
    protected boolean validatePaymentGroup(PaymentGroup paymentGroup) {
        // Check to see if the payment group has too many note lines to be printed on a check
        List<PaymentDetail> payDetails = paymentGroup.getPaymentDetails();

        int noteLines = 0;
        for (PaymentDetail paymentDetail : payDetails) {
            // Add one for each payment detail; summary of each detail is included on check and counts as a line
            noteLines++;
            // get all the possible notes for a given detail
            noteLines += paymentDetail.getNotes().size();
        }

        int maxNoteLines = getMaxNoteLines();
        if (noteLines > maxNoteLines) {
            // compile list of all doc numbers that make up payment group
            List<String> preqDocIds = new ArrayList<>();
            List<String> cmDocIds = new ArrayList<>();

            List<PaymentDetail> pds = paymentGroup.getPaymentDetails();
            for (PaymentDetail payDetail : pds) {
                if (KFSConstants.FinancialDocumentTypeCodes.VENDOR_CREDIT_MEMO.equals(
                        payDetail.getFinancialDocumentTypeCode())) {
                    cmDocIds.add(payDetail.getCustPaymentDocNbr());
                } else {
                    preqDocIds.add(payDetail.getCustPaymentDocNbr());
                }
            }

            // send warning email and prevent group from being processed by returning false
            paymentFileEmailService.sendExceedsMaxNotesWarningEmail(cmDocIds, preqDocIds, noteLines, maxNoteLines);

            return false;
        }

        return true;
    }

    /**
     * @return configured maximum number of note lines allowed
     */
    protected int getMaxNoteLines() {
        String maxLines = parameterService.getParameterValueAsString(KfsParameterConstants.PRE_DISBURSEMENT_ALL.class,
                PdpParameterConstants.MAX_NOTE_LINES);
        if (StringUtils.isBlank(maxLines)) {
            throw new RuntimeException("System parameter for max note lines is blank");
        }

        return Integer.parseInt(maxLines);
    }

    /**
     * Create a PDP payment detail record from a Credit Memo document
     *
     * @param creditMemoDocument Credit Memo to use for payment detail
     * @param batch              current PDP batch object
     * @return populated PaymentDetail line
     */
    protected PaymentDetail populatePaymentDetail(VendorCreditMemoDocument creditMemoDocument, Batch batch) {
        PaymentDetail paymentDetail = new PaymentDetail();

        String invoiceNumber = creditMemoDocument.getCreditMemoNumber();
        if (invoiceNumber.length() > 25) {
            invoiceNumber = invoiceNumber.substring(0, 25);
        }
        paymentDetail.setInvoiceNbr(invoiceNumber);
        paymentDetail.setCustPaymentDocNbr(creditMemoDocument.getDocumentNumber());

        if (creditMemoDocument.getPurchaseOrderIdentifier() != null) {
            paymentDetail.setPurchaseOrderNbr(creditMemoDocument.getPurchaseOrderIdentifier().toString());
        }

        if (creditMemoDocument.getPurchaseOrderDocument() != null) {
            if (creditMemoDocument.getPurchaseOrderDocument().getRequisitionIdentifier() != null) {
                paymentDetail.setRequisitionNbr(creditMemoDocument.getPurchaseOrderDocument()
                        .getRequisitionIdentifier().toString());
            }

            if (creditMemoDocument.getDocumentHeader().getOrganizationDocumentNumber() != null) {
                paymentDetail.setOrganizationDocNbr(creditMemoDocument.getDocumentHeader()
                        .getOrganizationDocumentNumber());
            }
        }

        paymentDetail.setCustomerInstitutionNumber(StringUtils.defaultString(
                creditMemoDocument.getVendorCustomerNumber()));

        final String creditMemoDocType = getDataDictionaryService().getDocumentTypeNameByClass(
                creditMemoDocument.getClass());
        paymentDetail.setFinancialDocumentTypeCode(creditMemoDocType);
        paymentDetail.setFinancialSystemOriginCode(KFSConstants.ORIGIN_CODE_KUALI);

        paymentDetail.setInvoiceDate(creditMemoDocument.getCreditMemoDate());
        paymentDetail.setOrigInvoiceAmount(creditMemoDocument.getCreditMemoAmount().negated());
        paymentDetail.setNetPaymentAmount(creditMemoDocument.getFinancialSystemDocumentHeader()
                .getFinancialDocumentTotalAmount().negated());

        KualiDecimal shippingAmount = KualiDecimal.ZERO;
        KualiDecimal discountAmount = KualiDecimal.ZERO;
        KualiDecimal creditAmount = KualiDecimal.ZERO;
        KualiDecimal debitAmount = KualiDecimal.ZERO;

        for (Object docItem : creditMemoDocument.getItems()) {
            CreditMemoItem item = (CreditMemoItem) docItem;

            KualiDecimal itemAmount = KualiDecimal.ZERO;
            if (item.getExtendedPrice() != null) {
                itemAmount = item.getExtendedPrice();
            }
            if (PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE.equals(item.getItemTypeCode())) {
                discountAmount = discountAmount.subtract(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_SHIP_AND_HAND_CODE.equals(item.getItemTypeCode())) {
                shippingAmount = shippingAmount.subtract(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_FREIGHT_CODE.equals(item.getItemTypeCode())) {
                shippingAmount = shippingAmount.add(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_MIN_ORDER_CODE.equals(item.getItemTypeCode())) {
                debitAmount = debitAmount.subtract(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_MISC_CODE.equals(item.getItemTypeCode())) {
                if (itemAmount.isNegative()) {
                    creditAmount = creditAmount.subtract(itemAmount);
                } else {
                    debitAmount = debitAmount.subtract(itemAmount);
                }
            }
        }

        paymentDetail.setInvTotDiscountAmount(discountAmount);
        paymentDetail.setInvTotShipAmount(shippingAmount);
        paymentDetail.setInvTotOtherCreditAmount(creditAmount);
        paymentDetail.setInvTotOtherDebitAmount(debitAmount);
        paymentDetail.setPrimaryCancelledPayment(Boolean.FALSE);

        addAccounts(creditMemoDocument, paymentDetail, creditMemoDocType);
        addNotes(creditMemoDocument, paymentDetail);

        return paymentDetail;
    }

    /**
     * Create a PDP Payment Detail record from a Payment Request document
     *
     * @param paymentRequestDocument Payment Request to use for Payment Detail
     * @param batch                  current PDP batch object
     * @return populated PaymentDetail line
     */
    protected PaymentDetail populatePaymentDetail(PaymentRequestDocument paymentRequestDocument, Batch batch) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Populating details for payment request document #" +
                    paymentRequestDocument.getDocumentNumber());
        }
        PaymentDetail paymentDetail = new PaymentDetail();

        paymentDetail.setCustPaymentDocNbr(paymentRequestDocument.getDocumentNumber());

        String invoiceNumber = paymentRequestDocument.getInvoiceNumber();
        if (invoiceNumber.length() > 25) {
            invoiceNumber = invoiceNumber.substring(0, 25);
        }
        paymentDetail.setInvoiceNbr(invoiceNumber);

        if (paymentRequestDocument.getPurchaseOrderIdentifier() != null) {
            paymentDetail.setPurchaseOrderNbr(paymentRequestDocument.getPurchaseOrderIdentifier().toString());
        }

        if (paymentRequestDocument.getPurchaseOrderDocument().getRequisitionIdentifier() != null) {
            paymentDetail.setRequisitionNbr(paymentRequestDocument.getPurchaseOrderDocument()
                    .getRequisitionIdentifier().toString());
        }

        if (paymentRequestDocument.getDocumentHeader().getOrganizationDocumentNumber() != null) {
            paymentDetail.setOrganizationDocNbr(paymentRequestDocument.getDocumentHeader()
                    .getOrganizationDocumentNumber());
        }

        paymentDetail.setCustomerInstitutionNumber(StringUtils.defaultString(
                paymentRequestDocument.getVendorCustomerNumber()));

        final String paymentRequestDocType = getDataDictionaryService().getDocumentTypeNameByClass(
                paymentRequestDocument.getClass());
        paymentDetail.setFinancialDocumentTypeCode(paymentRequestDocType);
        paymentDetail.setFinancialSystemOriginCode(KFSConstants.ORIGIN_CODE_KUALI);

        paymentDetail.setInvoiceDate(paymentRequestDocument.getInvoiceDate());
        paymentDetail.setOrigInvoiceAmount(paymentRequestDocument.getVendorInvoiceAmount());
        if (paymentRequestDocument.isUseTaxIndicator()) {
            // including discounts
            paymentDetail.setNetPaymentAmount(paymentRequestDocument.getGrandPreTaxTotal());
        } else {
            // including discounts
            paymentDetail.setNetPaymentAmount(paymentRequestDocument.getGrandTotal());
        }

        KualiDecimal shippingAmount = KualiDecimal.ZERO;
        KualiDecimal discountAmount = KualiDecimal.ZERO;
        KualiDecimal creditAmount = KualiDecimal.ZERO;
        KualiDecimal debitAmount = KualiDecimal.ZERO;

        for (Object docItem : paymentRequestDocument.getItems()) {
            PaymentRequestItem item = (PaymentRequestItem) docItem;

            KualiDecimal itemAmount = KualiDecimal.ZERO;
            if (item.getTotalRemitAmount() != null) {
                itemAmount = item.getTotalRemitAmount();
            }
            if (PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE.equals(item.getItemTypeCode())) {
                discountAmount = discountAmount.add(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_SHIP_AND_HAND_CODE.equals(item.getItemTypeCode())) {
                shippingAmount = shippingAmount.add(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_FREIGHT_CODE.equals(item.getItemTypeCode())) {
                shippingAmount = shippingAmount.add(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_MIN_ORDER_CODE.equals(item.getItemTypeCode())) {
                debitAmount = debitAmount.add(itemAmount);
            } else if (PurapConstants.ItemTypeCodes.ITEM_TYPE_MISC_CODE.equals(item.getItemTypeCode())) {
                if (itemAmount.isNegative()) {
                    creditAmount = creditAmount.add(itemAmount);
                } else {
                    debitAmount = debitAmount.add(itemAmount);
                }
            }
        }

        paymentDetail.setInvTotDiscountAmount(discountAmount);
        paymentDetail.setInvTotShipAmount(shippingAmount);
        paymentDetail.setInvTotOtherCreditAmount(creditAmount);
        paymentDetail.setInvTotOtherDebitAmount(debitAmount);
        paymentDetail.setPrimaryCancelledPayment(Boolean.FALSE);

        addAccounts(paymentRequestDocument, paymentDetail, paymentRequestDocType);
        addNotes(paymentRequestDocument, paymentDetail);

        return paymentDetail;
    }

    /**
     * Add accounts to a PDP Payment Detail
     *
     * @param accountsPayableDocument
     * @param paymentDetail
     * @param documentType
     */
    protected void addAccounts(AccountsPayableDocument accountsPayableDocument, PaymentDetail paymentDetail,
            String documentType) {
        String creditMemoDocType = getDataDictionaryService().getDocumentTypeNameByClass(
                VendorCreditMemoDocument.class);
        List<SourceAccountingLine> sourceAccountingLines =
                purapAccountingService.generateSourceAccountsForVendorRemit(accountsPayableDocument);
        for (SourceAccountingLine sourceAccountingLine : sourceAccountingLines) {
            KualiDecimal lineAmount = sourceAccountingLine.getAmount();
            PaymentAccountDetail paymentAccountDetail = new PaymentAccountDetail();
            paymentAccountDetail.setAccountNbr(sourceAccountingLine.getAccountNumber());

            if (creditMemoDocType.equals(documentType)) {
                lineAmount = lineAmount.negated();
            }
            paymentAccountDetail.setAccountNetAmount(lineAmount);
            paymentAccountDetail.setFinChartCode(sourceAccountingLine.getChartOfAccountsCode());
            paymentAccountDetail.setFinObjectCode(sourceAccountingLine.getFinancialObjectCode());

            paymentAccountDetail.setFinSubObjectCode(StringUtils.defaultIfEmpty(
                    sourceAccountingLine.getFinancialSubObjectCode(), KFSConstants.getDashFinancialSubObjectCode()));
            paymentAccountDetail.setOrgReferenceId(sourceAccountingLine.getOrganizationReferenceId());
            paymentAccountDetail.setProjectCode(StringUtils.defaultIfEmpty(sourceAccountingLine.getProjectCode(),
                    KFSConstants.getDashProjectCode()));
            paymentAccountDetail.setSubAccountNbr(StringUtils.defaultIfEmpty(
                    sourceAccountingLine.getSubAccountNumber(), KFSConstants.getDashSubAccountNumber()));
            paymentDetail.addAccountDetail(paymentAccountDetail);
        }
    }

    /**
     * Add Notes to a PDP Payment Detail
     *
     * @param accountsPayableDocument
     * @param paymentDetail
     */
    protected void addNotes(AccountsPayableDocument accountsPayableDocument, PaymentDetail paymentDetail) {
        int count = 1;

        if (accountsPayableDocument instanceof PaymentRequestDocument) {
            PaymentRequestDocument prd = (PaymentRequestDocument) accountsPayableDocument;

            if (prd.getSpecialHandlingInstructionLine1Text() != null) {
                PaymentNoteText pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
                pnt.setCustomerNoteText(prd.getSpecialHandlingInstructionLine1Text());
                paymentDetail.addNote(pnt);
            }

            if (prd.getSpecialHandlingInstructionLine2Text() != null) {
                PaymentNoteText pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
                pnt.setCustomerNoteText(prd.getSpecialHandlingInstructionLine2Text());
                paymentDetail.addNote(pnt);
            }

            if (prd.getSpecialHandlingInstructionLine3Text() != null) {
                PaymentNoteText pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
                pnt.setCustomerNoteText(prd.getSpecialHandlingInstructionLine3Text());
                paymentDetail.addNote(pnt);
            }
        }

        if (accountsPayableDocument.getNoteLine1Text() != null) {
            PaymentNoteText pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
            pnt.setCustomerNoteText(accountsPayableDocument.getNoteLine1Text());
            paymentDetail.addNote(pnt);
        }

        if (accountsPayableDocument.getNoteLine2Text() != null) {
            PaymentNoteText pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
            pnt.setCustomerNoteText(accountsPayableDocument.getNoteLine2Text());
            paymentDetail.addNote(pnt);
        }

        if (accountsPayableDocument.getNoteLine3Text() != null) {
            PaymentNoteText pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
            pnt.setCustomerNoteText(accountsPayableDocument.getNoteLine3Text());
            paymentDetail.addNote(pnt);
        }

        PaymentNoteText pnt = new PaymentNoteText();
        pnt.setCustomerNoteLineNbr(new KualiInteger(count++));
        pnt.setCustomerNoteText("Sales Tax: " + accountsPayableDocument.getTotalRemitTax());
    }

    /**
     * Populate the PDP Payment Group from fields on a payment request
     *
     * @param paymentRequestDocument
     * @param batch
     * @return PaymentGroup
     */
    protected PaymentGroup populatePaymentGroup(PaymentRequestDocument paymentRequestDocument, Batch batch) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("populatePaymentGroup() payment request documentNumber: " +
                    paymentRequestDocument.getDocumentNumber());
        }

        PaymentGroup paymentGroup = new PaymentGroup();

        paymentGroup.setBatchId(batch.getId());
        paymentGroup.setPaymentStatusCode(KFSConstants.PdpConstants.PAYMENT_OPEN_STATUS_CODE);
        paymentGroup.setBankCode(paymentRequestDocument.getBankCode());

        String postalCode = paymentRequestDocument.getVendorPostalCode();
        if (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(paymentRequestDocument.getVendorCountry())) {
            // Add a dash in the zip code if necessary
            if (postalCode.length() > 5) {
                postalCode = postalCode.substring(0, 5) + "-" + postalCode.substring(5);
            }
        }

        paymentGroup.setPayeeName(paymentRequestDocument.getVendorName());
        paymentGroup.setPayeeId(paymentRequestDocument.getVendorHeaderGeneratedIdentifier() + "-" +
                paymentRequestDocument.getVendorDetailAssignedIdentifier());
        paymentGroup.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);

        if (paymentRequestDocument.getVendorDetail().getVendorHeader().getVendorOwnershipCode() != null) {
            paymentGroup.setPayeeOwnerCd(paymentRequestDocument.getVendorDetail().getVendorHeader()
                    .getVendorOwnershipCode());
        }

        paymentGroup.setLine1Address(paymentRequestDocument.getVendorLine1Address());
        paymentGroup.setLine2Address(paymentRequestDocument.getVendorLine2Address());
        paymentGroup.setLine3Address("");
        paymentGroup.setLine4Address("");

        paymentGroup.setCity(paymentRequestDocument.getVendorCityName());
        paymentGroup.setState(paymentRequestDocument.getVendorStateCode());
        paymentGroup.setZipCd(postalCode);
        paymentGroup.setCountry(paymentRequestDocument.getVendorCountryCode());
        paymentGroup.setCampusAddress(Boolean.FALSE);

        if (paymentRequestDocument.getPaymentRequestPayDate() != null) {
            paymentGroup.setPaymentDate(paymentRequestDocument.getPaymentRequestPayDate());
        }

        paymentGroup.setPymtAttachment(paymentRequestDocument.getPaymentAttachmentIndicator());
        paymentGroup.setProcessImmediate(paymentRequestDocument.getImmediatePaymentIndicator());
        paymentGroup.setPymtSpecialHandling(StringUtils.isNotBlank(
                    paymentRequestDocument.getSpecialHandlingInstructionLine1Text())
                || StringUtils.isNotBlank(paymentRequestDocument.getSpecialHandlingInstructionLine2Text())
                || StringUtils.isNotBlank(paymentRequestDocument.getSpecialHandlingInstructionLine3Text()));
        paymentGroup.setTaxablePayment(Boolean.FALSE);
        paymentGroup.setNraPayment(paymentRequestDocument.getVendorDetail().getVendorHeader()
                .getVendorForeignIndicator());
        paymentGroup.setCombineGroups(Boolean.TRUE);

        return paymentGroup;
    }

    /**
     * Populates a PaymentGroup record from a Credit Memo document
     *
     * @param creditMemoDocument
     * @param batch
     * @return PaymentGroup
     */
    protected PaymentGroup populatePaymentGroup(VendorCreditMemoDocument creditMemoDocument, Batch batch) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("populatePaymentGroup() credit memo documentNumber: " +
                    creditMemoDocument.getDocumentNumber());
        }

        PaymentGroup paymentGroup = new PaymentGroup();
        paymentGroup.setBatchId(batch.getId());
        paymentGroup.setPaymentStatusCode(KFSConstants.PdpConstants.PAYMENT_OPEN_STATUS_CODE);
        paymentGroup.setBankCode(creditMemoDocument.getBankCode());

        String postalCode = creditMemoDocument.getVendorPostalCode();
        if (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(creditMemoDocument.getVendorCountry())) {
            // Add a dash in the zip code if necessary
            if (postalCode.length() > 5) {
                postalCode = postalCode.substring(0, 5) + "-" + postalCode.substring(5);
            }
        }

        paymentGroup.setPayeeName(creditMemoDocument.getVendorName());
        paymentGroup.setPayeeId(creditMemoDocument.getVendorHeaderGeneratedIdentifier() + "-" +
                creditMemoDocument.getVendorDetailAssignedIdentifier());
        paymentGroup.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);

        if (creditMemoDocument.getVendorDetail().getVendorHeader().getVendorOwnershipCode() != null) {
            paymentGroup.setPayeeOwnerCd(creditMemoDocument.getVendorDetail().getVendorHeader()
                    .getVendorOwnershipCode());
        }

        paymentGroup.setLine1Address(creditMemoDocument.getVendorLine1Address());
        paymentGroup.setLine2Address(creditMemoDocument.getVendorLine2Address());
        paymentGroup.setLine3Address("");
        paymentGroup.setLine4Address("");

        paymentGroup.setCity(creditMemoDocument.getVendorCityName());
        paymentGroup.setState(creditMemoDocument.getVendorStateCode());
        paymentGroup.setZipCd(postalCode);
        paymentGroup.setCountry(creditMemoDocument.getVendorCountryCode());
        paymentGroup.setCampusAddress(Boolean.FALSE);

        if (creditMemoDocument.getCreditMemoDate() != null) {
            paymentGroup.setPaymentDate(creditMemoDocument.getCreditMemoDate());
        }

        paymentGroup.setPymtAttachment(Boolean.FALSE);
        paymentGroup.setProcessImmediate(Boolean.FALSE);
        paymentGroup.setPymtSpecialHandling(Boolean.FALSE);
        paymentGroup.setTaxablePayment(Boolean.FALSE);
        paymentGroup.setNraPayment(creditMemoDocument.getVendorDetail().getVendorHeader().getVendorForeignIndicator());
        paymentGroup.setCombineGroups(Boolean.TRUE);

        return paymentGroup;
    }

    /**
     * Create a new PDP batch
     *
     * @param campusCode
     * @param puser
     * @param processRunDate
     * @return Batch
     */
    protected Batch createBatch(String campusCode, Person puser, Date processRunDate) {
        String orgCode = parameterService.getParameterValueAsString(KfsParameterConstants.PURCHASING_BATCH.class,
                KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_ORG_CODE);
        String subUnitCode = parameterService.getParameterValueAsString(KfsParameterConstants.PURCHASING_BATCH.class,
                KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_SUB_UNIT_CODE);
        CustomerProfile customer = customerProfileService.get(campusCode, orgCode, subUnitCode);
        if (customer == null) {
            throw new IllegalArgumentException("Unable to find customer profile for " + campusCode + "/" + orgCode +
                    "/" + subUnitCode);
        }

        // Create the group for this campus
        Batch batch = new Batch();
        batch.setCustomerProfile(customer);
        batch.setCustomerFileCreateTimestamp(new Timestamp(processRunDate.getTime()));
        batch.setFileProcessTimestamp(new Timestamp(processRunDate.getTime()));
        batch.setPaymentFileName(PurapConstants.PDP_PURAP_EXTRACT_FILE_NAME);
        batch.setSubmiterUserId(puser.getPrincipalId());

        // Set these for now, we will update them later
        batch.setPaymentCount(KualiInteger.ZERO);
        batch.setPaymentTotalAmount(KualiDecimal.ZERO);

        businessObjectService.save(batch);

        return batch;
    }

    /**
     * Find all the campuses that have payments to process
     *
     * @return List<String>
     */
    protected List<String> getChartCodes(boolean immediatesOnly, Date processRunDate) {
        LOG.debug("Getting Chart Codes()");
        List<String> output = new ArrayList<>();

        Iterator<PaymentRequestDocument> paymentRequests;
        if (immediatesOnly) {
            paymentRequests = paymentRequestService.getImmediatePaymentRequestsToExtract(null).iterator();
        } else {
            java.sql.Date onOrBeforePaymentRequestPayDate = KfsDateUtils.convertToSqlDate(
                    purapRunDateService.calculateRunDate(processRunDate));
            paymentRequests = paymentRequestService.getPaymentRequestsToExtract(onOrBeforePaymentRequestPayDate)
                    .iterator();
        }

        while (paymentRequests.hasNext()) {
            PaymentRequestDocument prd = paymentRequests.next();
            if (!output.contains(prd.getProcessingCampusCode())) {
                output.add(prd.getProcessingCampusCode());
            }
        }

        return output;
    }

    protected synchronized void lockUnlockDocuments(boolean locked) {
        for (String documentType : lockedDocuments) {
            Class<? extends Document> documentClass = dataDictionaryService.getDocumentClassByTypeName(documentType);
            if (parameterService.parameterExists(documentClass, KFSConstants.DOCUMENT_LOCKOUT_PARAM_NM)) {
                Parameter existingParam = parameterService.getParameter(documentClass,
                        KFSConstants.DOCUMENT_LOCKOUT_PARAM_NM);
                Parameter.Builder updatedParam = Builder.create(existingParam);
                updatedParam.setValue(locked ? "Y" : "N");
                parameterService.updateParameter(updatedParam.build());
            } else {
                String namespace = KRADServiceLocatorWeb.getKualiModuleService().getNamespaceCode(documentClass);
                String detailType = KRADServiceLocatorWeb.getKualiModuleService().getComponentCode(documentClass);
                Parameter.Builder newParam = Builder.create(KFSConstants.APPLICATION_NAMESPACE_CODE, namespace,
                        detailType, KFSConstants.DOCUMENT_LOCKOUT_PARAM_NM,
                        ParameterType.Builder.create(KfsParameterConstants.PARAMETER_CONFIG_TYPE_CODE));
                newParam.setValue("Y");
                newParam.setDescription(KFSConstants.DOCUMENT_LOCKOUT_PARAM_DESC);
                parameterService.createParameter(newParam.build());
            }
        }
    }

    public void setPaymentRequestService(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPaymentFileService(PaymentFileService paymentFileService) {
        this.paymentFileService = paymentFileService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setCustomerProfileService(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

    public void setPaymentDetailService(PaymentDetailService paymentDetailService) {
        this.paymentDetailService = paymentDetailService;
    }

    public void setCreditMemoService(CreditMemoService creditMemoService) {
        this.creditMemoService = creditMemoService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setPurapRunDateService(PurapRunDateService purapRunDateService) {
        this.purapRunDateService = purapRunDateService;
    }

    public void setPaymentFileEmailService(PdpEmailService paymentFileEmailService) {
        this.paymentFileEmailService = paymentFileEmailService;
    }

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setPurapAccountingService(PurapAccountingServiceImpl purapAccountingService) {
        this.purapAccountingService = purapAccountingService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setPurapService(PurapService purapService) {
        this.purapService = purapService;
    }

    public void setLockedDocuments(List<String> lockedDocuments) {
        this.lockedDocuments = lockedDocuments;
    }

    /**
     * Holds total count and amount for extract
     */
    protected class Totals {
        public Integer count = 0;
        public KualiDecimal totalAmount = KualiDecimal.ZERO;
    }
}
