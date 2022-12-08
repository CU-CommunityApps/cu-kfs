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
package org.kuali.kfs.module.purap.document.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.document.service.DisbursementVoucherValidationService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.InfrastructureException;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants.NonresidentTaxParameters;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.AutoApproveExclude;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.NegativePaymentRequestApprovalLimit;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.dataaccess.PaymentRequestDao;
import org.kuali.kfs.module.purap.document.service.AccountsPayableService;
import org.kuali.kfs.module.purap.document.service.NegativePaymentRequestApprovalLimitService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurApWorkflowIntegrationService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.validation.event.AttributedContinuePurapEvent;
import org.kuali.kfs.module.purap.document.validation.event.PurchasingAccountsPayableItemPreCalculateEvent;
import org.kuali.kfs.module.purap.exception.PurError;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.module.purap.util.PurApItemUtils;
import org.kuali.kfs.module.purap.util.VendorGroupingHelper;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class provides services of use to a payment request document
 */
/**
 * CU customization: backport FINP-8270 and FINP-8283. These were backported to the 1/28/2021
 * version of this file. These changes can be removed with the 3/9/2022 upgrade.
 */
public class PaymentRequestServiceImpl implements PaymentRequestService {

    private static final Logger LOG = LogManager.getLogger();

    protected AccountsPayableService accountsPayableService;
    protected BankService bankService;
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected DisbursementVoucherValidationService disbursementVoucherValidationService;
    protected DocumentService documentService;
    protected FinancialSystemDocumentService financialSystemDocumentService;
    protected KualiRuleService kualiRuleService;
    protected NegativePaymentRequestApprovalLimitService negativePaymentRequestApprovalLimitService;
    protected NoteService noteService;
    protected ParameterService parameterService;
    protected PaymentRequestDao paymentRequestDao;
    protected PurapAccountingService purapAccountingService;
    protected PurapService purapService;
    protected PurApWorkflowIntegrationService purapWorkflowIntegrationService;
    protected PurchaseOrderService purchaseOrderService;
    protected UniversityDateService universityDateService;
    protected VendorService vendorService;

    @Override
    @Deprecated
    public Collection<PaymentRequestDocument> getPaymentRequestsToExtractByCM(String campusCode,
            VendorCreditMemoDocument cmd) {
        LOG.debug("getPaymentRequestsByCM() started");
        Date currentSqlDateMidnight = dateTimeService.getCurrentSqlDateMidnight();
        List<PaymentRequestDocument> paymentRequestIterator = paymentRequestDao.getPaymentRequestsToExtract(campusCode,
                null, null, cmd.getVendorHeaderGeneratedIdentifier(),
                cmd.getVendorDetailAssignedIdentifier(), currentSqlDateMidnight);

        return filterPaymentRequestByAppDocStatus(paymentRequestIterator,
            PaymentRequestStatuses.APPDOC_AUTO_APPROVED,
            PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED);
    }

    @Override
    public Collection<PaymentRequestDocument> getPaymentRequestsToExtractByVendor(String campusCode,
            VendorGroupingHelper vendor, Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsByVendor() started");
        Collection<PaymentRequestDocument> paymentRequestDocuments = paymentRequestDao
                .getPaymentRequestsToExtractForVendor(campusCode, vendor, onOrBeforePaymentRequestPayDate);

        return filterPaymentRequestByAppDocStatus(paymentRequestDocuments,
            PaymentRequestStatuses.APPDOC_AUTO_APPROVED,
            PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED);
    }

    @Override
    public Collection<PaymentRequestDocument> getPaymentRequestsToExtract(Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtract() started");

        Collection<PaymentRequestDocument> paymentRequestIterator =
                paymentRequestDao.getPaymentRequestsToExtract(false, null,
                        onOrBeforePaymentRequestPayDate);
        return filterPaymentRequestByAppDocStatus(paymentRequestIterator,
            PaymentRequestStatuses.STATUSES_ALLOWED_FOR_EXTRACTION);
    }

    @Override
    public Collection<PaymentRequestDocument> getPaymentRequestsToExtractSpecialPayments(String chartCode,
            Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtractSpecialPayments() started");

        Collection<PaymentRequestDocument> paymentRequestIterator =
                paymentRequestDao.getPaymentRequestsToExtract(true, chartCode,
                        onOrBeforePaymentRequestPayDate);
        return filterPaymentRequestByAppDocStatus(paymentRequestIterator,
            PaymentRequestStatuses.STATUSES_ALLOWED_FOR_EXTRACTION);
    }

    @Override
    public Collection<PaymentRequestDocument> getImmediatePaymentRequestsToExtract(String campusCode) {
        LOG.debug("getImmediatePaymentRequestsToExtract() started");

        Collection<PaymentRequestDocument> paymentRequestIterator =
                paymentRequestDao.getImmediatePaymentRequestsToExtract(campusCode);
        return filterPaymentRequestByAppDocStatus(paymentRequestIterator,
            PaymentRequestStatuses.STATUSES_ALLOWED_FOR_EXTRACTION);
    }

    @Override
    public Collection<PaymentRequestDocument> getPaymentRequestToExtractByCampus(String campusCode,
            Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestToExtractByCampus() started");

        Collection<PaymentRequestDocument> paymentRequestIterator =
                paymentRequestDao.getPaymentRequestsToExtract(false, campusCode,
                        onOrBeforePaymentRequestPayDate);
        return filterPaymentRequestByAppDocStatus(paymentRequestIterator,
            PaymentRequestStatuses.STATUSES_ALLOWED_FOR_EXTRACTION);
    }

    @Override
    public void autoApprovePaymentRequests() {
        LOG.info("Starting autoApprovePaymentRequests.");
        // should objects from existing user session be copied over

        Date todayAtMidnight = dateTimeService.getCurrentSqlDateMidnight();
        List<String> docNumbers = paymentRequestDao.getEligibleForAutoApproval(todayAtMidnight);
        LOG.info(" -- Initial filtering complete, returned {}  docs.", docNumbers.size());

        String samt = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_DEFAULT_NEGATIVE_PAYMENT_REQUEST_APPROVAL_LIMIT);
        KualiDecimal defaultMinimumLimit = new KualiDecimal(samt);
        LOG.info(" -- Using default limit value of {}.", defaultMinimumLimit);

        for (String docNumber : docNumbers) {
            PaymentRequestDocument paymentRequestDocument = getPaymentRequestByDocumentNumber(docNumber);
            if (ObjectUtils.isNotNull(paymentRequestDocument)) {
                autoApprovePaymentRequest(paymentRequestDocument, defaultMinimumLimit);
            }
        }
    }

    /**
     * NOTE: in the event of auto-approval failure, this method may throw a RuntimeException, indicating to Spring
     * transactional management that the transaction should be rolled back.
     */
    @Override
    public boolean autoApprovePaymentRequest(String docNumber, KualiDecimal defaultMinimumLimit) {
        PaymentRequestDocument paymentRequestDocument;
        paymentRequestDocument = (PaymentRequestDocument) documentService.getByDocumentHeaderId(docNumber);
        if (paymentRequestDocument.isHoldIndicator() || paymentRequestDocument.isPaymentRequestedCancelIndicator()
                || !Arrays.asList(PaymentRequestStatuses.PREQ_STATUSES_FOR_AUTO_APPROVE)
                    .contains(paymentRequestDocument.getApplicationDocumentStatus())) {
            // this condition is based on the conditions that
            // PaymentRequestDaoOjb.getEligibleDocumentNumbersForAutoApproval() uses to query the database.
            // Rechecking these conditions to ensure that the document is eligible for auto-approval, because
            // we're not running things within the same transaction anymore and changes could have occurred since
            // we called that method that make this document not auto-approvable

            // note that this block does not catch all race conditions however, this error condition is not enough
            // to make us return an error code, so just skip the document
            LOG.warn("Payment Request Document {} could not be auto-approved because it has either been placed on" +
                    " hold, requested cancel, or does not have one of the PREQ statuses for auto-approve.",
                    paymentRequestDocument.getDocumentNumber());
            return true;
        }
        if (autoApprovePaymentRequest(paymentRequestDocument, defaultMinimumLimit)) {
            LOG.info("Auto-approval for payment request successful.  Doc number: {}", docNumber);
            return true;
        } else {
            LOG.error("Payment Request Document {} could not be auto-approved.", docNumber);
            return false;
        }
    }

    /**
     * NOTE: in the event of auto-approval failure, this method may throw a RuntimeException, indicating to Spring
     * transactional management that the transaction should be rolled back.
     */
    @Override
    @Transactional
    public boolean autoApprovePaymentRequest(PaymentRequestDocument doc, KualiDecimal defaultMinimumLimit) {
        if (isEligibleForAutoApproval(doc, defaultMinimumLimit)) {
            // Much of the framework assumes that document instances that are saved via
            // DocumentService.saveDocument are those that were dynamically created by PojoFormBase (i.e., the
            // Document instance wasn't created from OJB). We need to make a deep copy and materialize
            // collections to fulfill that assumption so that collection elements will delete properly

            // TODO: maybe rewriting PurapService.calculateItemTax could be rewritten so that the a deep copy
            // doesn't need to be made by taking advantage of OJB's managed array lists
            try {
                ObjectUtils.materializeUpdateableCollections(doc);
                for (PaymentRequestItem item : (List<PaymentRequestItem>) doc.getItems()) {
                    ObjectUtils.materializeUpdateableCollections(item);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            doc = (PaymentRequestDocument) ObjectUtils.deepCopy(doc);

            // set the auto approved indicator to true so that doRouteStatus method can use to change the app doc
            // status.
            doc.setAutoApprovedIndicator(true);
            LOG.info("About to blanketApproveDocument, doc.getDocumentNumber()={}", doc.getDocumentNumber());
            // su approve rather than blanket approve, so no ACK notifications would be generated
            documentService.superUserApproveDocument(doc, "auto-approving: Total is below threshold.");
        }
        return true;
    }

    /**
     * Determines whether or not a payment request document can be automatically approved. FYI - If fiscal reviewers
     * are allowed to save account changes without the full account validation running then this method must call full
     * account validation to make sure auto approver is not blanket approving an invalid document according the the
     * accounts on the items
     *
     * @param document            The payment request document to be determined whether it can be automatically
     *                            approved.
     * @param defaultMinimumLimit The amount to be used as the minimum amount if no limit was found or the default is
     *                            less than the limit.
     * @return boolean true if the payment request document is eligible for auto approval.
     */
    protected boolean isEligibleForAutoApproval(PaymentRequestDocument document, KualiDecimal defaultMinimumLimit) {
        // Check if vendor is foreign.
        if (document.getVendorDetail().getVendorHeader().getVendorForeignIndicator()) {
            LOG.info(" -- PayReq [{}] skipped due to a Foreign Vendor.", document.getDocumentNumber());
            return false;
        }

        // check to make sure the payment request isn't scheduled to stop in tax review.
        if (purapWorkflowIntegrationService.willDocumentStopAtGivenFutureRouteNode(document,
                PaymentRequestStatuses.NODE_VENDOR_TAX_REVIEW)) {
            LOG.info(" -- PayReq [{}] skipped due to requiring Tax Review.", document.getDocumentNumber());
            return false;
        }

        // Change to not auto approve if positive approval required indicator set to Yes
        if (document.isPaymentRequestPositiveApprovalIndicator()) {
            LOG.info(" -- PayReq [{}] skipped due to a Positive Approval Required Indicator set to Yes.",
                    document.getDocumentNumber());
            return false;
        }

        // This minimum will be set to the minimum limit derived from all accounting lines on the document. If no
        // limit is determined, the default will be used.
        KualiDecimal minimumAmount = null;

        // Iterate all source accounting lines on the document, deriving a minimum limit from each according to chart,
        // chart and account, and chart and organization.
        final List<SourceAccountingLine> summaryLines = purapAccountingService.generateSummary(document.getItems());
        for (SourceAccountingLine line : summaryLines) {
            // check to make sure the account is in the auto approve exclusion list
            Map<String, Object> autoApproveMap = new HashMap<>();
            autoApproveMap.put("chartOfAccountsCode", line.getChartOfAccountsCode());
            autoApproveMap.put("accountNumber", line.getAccountNumber());
            autoApproveMap.put("active", true);
            AutoApproveExclude autoApproveExclude = businessObjectService.findByPrimaryKey(AutoApproveExclude.class,
                    autoApproveMap);
            if (autoApproveExclude != null) {
                LOG.info(" -- PayReq [{}}] skipped due to source accounting line {} using Chart/Account [{}-{}], which" +
                        " is excluded in the Auto Approve Exclusions table.",
                        document.getDocumentNumber(),
                        line.getSequenceNumber(),
                        line.getChartOfAccountsCode(),
                        line.getAccountNumber());
                return false;
            }

            minimumAmount = getMinimumLimitAmount(negativePaymentRequestApprovalLimitService.findByChart(
                    line.getChartOfAccountsCode()), minimumAmount);
            minimumAmount = getMinimumLimitAmount(negativePaymentRequestApprovalLimitService.findByChartAndAccount(
                    line.getChartOfAccountsCode(), line.getAccountNumber()), minimumAmount);
            minimumAmount = getMinimumLimitAmount(negativePaymentRequestApprovalLimitService.findByChartAndOrganization(
                    line.getChartOfAccountsCode(), line.getOrganizationReferenceId()), minimumAmount);
        }

        // If Receiving required is set, it's not needed to check the negative payment request approval limit
        if (document.isReceivingDocumentRequiredIndicator()) {
            LOG.info(" -- PayReq [{}] auto-approved (ignored dollar limit) due to Receiving Document Required" +
                    " Indicator set to Yes.", document.getDocumentNumber());
            return true;
        }

        // If no limit was found or the default is less than the limit, the default limit is used.
        if (ObjectUtils.isNull(minimumAmount) || defaultMinimumLimit.compareTo(minimumAmount) < 0) {
            minimumAmount = defaultMinimumLimit;
        }

        // The document is eligible for auto-approval if the document total is below the limit.
        final String autoApprovalLimitLabel = minimumAmount.equals(defaultMinimumLimit) ? "Default" : "Configured";
        if (document.getDocumentHeader().getFinancialDocumentTotalAmount().isLessThan(minimumAmount)) {
            LOG.info(" -- PayReq [{}] auto-approved due to document Total [{}] being less than {} Auto-Approval Limit" +
                            " of {}.",
                    document.getDocumentNumber(),
                    document.getDocumentHeader().getFinancialDocumentTotalAmount(),
                    autoApprovalLimitLabel,
                    minimumAmount);
            return true;
        }
        LOG.info(" -- PayReq [{}] skipped due to document Total [{}] being greater than {} Auto-Approval Limit of {}.",
                document.getDocumentNumber(),
                document.getDocumentHeader().getFinancialDocumentTotalAmount(),
                autoApprovalLimitLabel,
                minimumAmount);

        return false;
    }

    /**
     * This method iterates a collection of negative payment request approval limits and returns the minimum of a
     * given minimum amount and the least among the limits in the collection.
     *
     * @param limits        The collection of NegativePaymentRequestApprovalLimit to be used in determining the
     *                      minimum limit amount.
     * @param minimumAmount The amount to be compared with the collection of NegativePaymentRequestApprovalLimit to
     *                      determine the minimum limit amount.
     * @return The minimum of the given minimum amount and the least among the limits in the collection.
     */
    protected KualiDecimal getMinimumLimitAmount(Collection<NegativePaymentRequestApprovalLimit> limits,
            KualiDecimal minimumAmount) {
        for (NegativePaymentRequestApprovalLimit limit : limits) {
            KualiDecimal amount = limit.getNegativePaymentRequestApprovalLimitAmount();
            if (null == minimumAmount) {
                minimumAmount = amount;
            } else if (minimumAmount.isGreaterThan(amount)) {
                minimumAmount = amount;
            }
        }
        return minimumAmount;
    }

    /**
     * Retrieves a list of payment request documents with the given vendor id and invoice number.
     *
     * @param vendorHeaderGeneratedId The vendor header generated id.
     * @param vendorDetailAssignedId  The vendor detail assigned id.
     * @return List of payment request documents.
     */
	/*
	 * CU customization: backport FINP-8270
	 */
    @Override
    public List<PaymentRequestView> getPaymentRequestsByVendorNumber(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId) {
        LOG.debug("getActivePaymentRequestsByVendorNumber() started");
        return paymentRequestDao.getActivePaymentRequestsByVendorNumber(vendorHeaderGeneratedId,
                vendorDetailAssignedId);
    }

    /**
     * Retrieves a list of payment request documents with the given vendor id and invoice number.
     *
     * @param vendorHeaderGeneratedId The vendor header generated id.
     * @param vendorDetailAssignedId  The vendor detail assigned id.
     * @param invoiceNumber           The invoice number as entered by AP.
     * @return List of payment request document.
     */
    @Override
    public List getPaymentRequestsByVendorNumberInvoiceNumber(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId, String invoiceNumber) {
        LOG.debug("getActivePaymentRequestsByVendorNumberInvoiceNumber() started");
        return paymentRequestDao.getActivePaymentRequestsByVendorNumberInvoiceNumber(vendorHeaderGeneratedId,
                vendorDetailAssignedId, invoiceNumber);
    }

    @Override
    public HashMap<String, String> checkForDuplicatePayments(PaymentRequestDocument document) {
        HashMap<String, String> msgs = new HashMap<>();

        boolean checkForDuplicateDisbursementVouchers = parameterService.getParameterValueAsBoolean(
                PaymentRequestDocument.class,
                PurapParameterConstants.DUPLICATE_PAYMENT_CHECK_INCLUDE_DISBURSEMENT_VOUCHER_IND);
        boolean checkForDuplicatePaymentRequests = parameterService.getParameterValueAsBoolean(
                PaymentRequestDocument.class,
                PurapParameterConstants.DUPLICATE_PAYMENT_CHECK_INCLUDE_PAYMENT_REQUEST_IND);

        PurchaseOrderDocument po = document.getPurchaseOrderDocument();
        String disbVchrPayeeIdNumber = po.getVendorHeaderGeneratedIdentifier() + "-" +
                po.getVendorDetailAssignedIdentifier();

        if (checkForDuplicateDisbursementVouchers) {
            msgs.putAll(disbursementVoucherValidationService.checkForDuplicateDisbursementVouchers(
                    document.getInvoiceNumber(), disbVchrPayeeIdNumber, document.getInvoiceDate(),
                    document.getVendorInvoiceAmount(), true));
        }

        if (checkForDuplicatePaymentRequests) {
            msgs.putAll(checkForDuplicatePaymentRequests(document));
        }

        return msgs;
    }

    private HashMap<String, String> checkForDuplicatePaymentRequests(PaymentRequestDocument document) {
        HashMap<String, String> msgs = new HashMap<>();

        if (ObjectUtils.isNotNull(document.getInvoiceDate())) {
            if (purapService.isDateAYearBeforeToday(document.getInvoiceDate())) {
                msgs.put(KFSConstants.PaymentRequestDocumentConstants.DUPLICATE_INVOICE_QUESTION,
                        configurationService.getPropertyValueAsString(
                                PurapKeyConstants.MESSAGE_INVOICE_DATE_A_YEAR_OR_MORE_PAST));
            }
        }

        PurchaseOrderDocument po = document.getPurchaseOrderDocument();

        if (po != null) {
            msgs.putAll(checkForDuplicatesByVendorNumberAndInvoiceNumber(
                po.getVendorHeaderGeneratedIdentifier(), po.getVendorDetailAssignedIdentifier(),
                document.getInvoiceNumber(), PurapConstants.PREQDocumentsStrings.PAYEE_TOKEN, true));

            msgs.putAll(checkForDuplicatesByPoIdInvoiceNumberAndInvoiceDate(document.getPurchaseOrderIdentifier(),
                document.getVendorInvoiceAmount(), document.getInvoiceDate()));
        }

        return msgs;
    }

    /*
     * CU Customization: Backport FINP-8283
     */
    @Override
    public HashMap<String, String> checkForDuplicatePaymentRequests(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId, String invoiceNumber, KualiDecimal invoiceAmount, Date invoiceDate,
            String vendorToken, String specifiedSourceToken, boolean questionFormat) {
        HashMap<String, String> messages = new HashMap<>();

        if (ObjectUtils.isNotNull(invoiceDate)) {
            if (purapService.isDateAYearBeforeToday(invoiceDate)) {
                messages.put(KFSConstants.PaymentRequestDocumentConstants.DUPLICATE_INVOICE_QUESTION,
                        configurationService.getPropertyValueAsString(
                                PurapKeyConstants.MESSAGE_INVOICE_DATE_A_YEAR_OR_MORE_PAST));
            }
        }
        if (vendorHeaderGeneratedId != null && vendorDetailAssignedId != null) {
            messages.putAll(checkForDuplicatesByVendorNumberAndInvoiceNumber(vendorHeaderGeneratedId,
                    vendorDetailAssignedId, invoiceNumber, vendorToken, questionFormat
            ));
        }
        messages.putAll(checkForDuplicatesByInvoiceNumberAndInvoiceDate(vendorHeaderGeneratedId,
                vendorDetailAssignedId, invoiceAmount, invoiceDate, specifiedSourceToken, questionFormat));

        return messages;
    }

	/*
	 * CU customization: backport FINP-8270
	 */
    private Map<String, String> checkForDuplicatesByVendorNumberAndInvoiceNumber(
            final Integer vendorHeaderGeneratedId,
            final Integer vendorDetailAssignedId,
            final String invoiceNumber,
            final String vendorToken,
            final boolean questionFormat
    ) {
        final String invoiceNumberForComparison = buildInvoiceNumberForComparison(invoiceNumber);
        final List<PaymentRequestDocument> duplicatePaymentRequests = new ArrayList<>();

        final List<PaymentRequestView> possibleDuplicatePaymentRequests = getPaymentRequestsByVendorNumber(
                vendorHeaderGeneratedId, vendorDetailAssignedId);
        for (final PaymentRequestView possibleDuplicatePaymentRequest: possibleDuplicatePaymentRequests) {
            if (buildInvoiceNumberForComparison(possibleDuplicatePaymentRequest.getInvoiceNumber()).equals(
                    invoiceNumberForComparison)) {
                final PaymentRequestDocument possibleDuplicate = (PaymentRequestDocument) documentService
                        .getByDocumentHeaderId(possibleDuplicatePaymentRequest.getDocumentNumber());
                    duplicatePaymentRequests.add(possibleDuplicate);
            }
        }

        return buildWarningMessagesForDuplicatesByInvoiceNumberAndAmount(duplicatePaymentRequests, vendorToken,
                DuplicatePaymentRequestMessages.DUPLICATE_INVOICE_VENDOR_INVOICE_NUMBER, questionFormat);
    }

    // strips special characters (anything not a-z, 0-9 or space) and converts to upper case for comparison purposes
    private String buildInvoiceNumberForComparison(String invoiceNumber) {
        return invoiceNumber.replaceAll("[^\\w\\s]", "").toUpperCase(Locale.US);
    }

    private Map<String, String> checkForDuplicatesByPoIdInvoiceNumberAndInvoiceDate(Integer purchaseOrderId,
            KualiDecimal invoiceAmount, Date invoiceDate) {
        List<PaymentRequestDocument> duplicatePaymentRequests = getPaymentRequestsByPOIdInvoiceAmountInvoiceDate(
                purchaseOrderId, invoiceAmount, invoiceDate);
        return buildWarningMessagesForDuplicatesByInvoiceNumberAndAmount(duplicatePaymentRequests,
                PurapConstants.PREQDocumentsStrings.SPECIFIED_TOKEN,
                DuplicatePaymentRequestMessages.DUPLICATE_INVOICE_DATE_AMOUNT, true);
    }

    private Map<String, String> checkForDuplicatesByInvoiceNumberAndInvoiceDate(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId, KualiDecimal invoiceAmount, Date invoiceDate, String specifiedSourceToken,
            boolean questionFormat) {
        List<PaymentRequestDocument> duplicatePaymentRequests = getPaymentRequestsByInvoiceAmountInvoiceDate(
                vendorHeaderGeneratedId, vendorDetailAssignedId, invoiceAmount, invoiceDate);
        return buildWarningMessagesForDuplicatesByInvoiceNumberAndAmount(duplicatePaymentRequests,
                specifiedSourceToken, DuplicatePaymentRequestMessages.DUPLICATE_INVOICE_DATE_AMOUNT, questionFormat);
    }

    private Map<String, String> buildWarningMessagesForDuplicatesByInvoiceNumberAndAmount(
            List<PaymentRequestDocument> duplicatePaymentRequests, String specifiedSourceToken,
            DuplicatePaymentRequestMessages duplicatePaymentRequestMessages, boolean questionFormat) {
        HashMap<String, String> messages = new HashMap<>();

        if (duplicatePaymentRequests.size() > 0) {
            String messageText = StringUtils.EMPTY;
            boolean foundCanceled = false;
            boolean foundVoided = false;
            for (PaymentRequestDocument duplicatePaymentRequest: duplicatePaymentRequests) {
                if (StringUtils.equalsIgnoreCase(duplicatePaymentRequest.getApplicationDocumentStatus(),
                        PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE)) {
                    if (foundVoided) {
                        messageText = configurationService.getPropertyValueAsString(
                                duplicatePaymentRequestMessages.duplicateInvoiceCanceledOrVoidedKey);
                    } else {
                        messageText = configurationService.getPropertyValueAsString(
                                duplicatePaymentRequestMessages.duplicateInvoiceCancelledKey);
                        foundCanceled = true;
                    }
                } else if (StringUtils.equalsIgnoreCase(duplicatePaymentRequest.getApplicationDocumentStatus(),
                        PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS)) {
                    if (foundCanceled) {
                        messageText = configurationService.getPropertyValueAsString(
                                duplicatePaymentRequestMessages.duplicateInvoiceCanceledOrVoidedKey);
                    } else {
                        messageText = configurationService.getPropertyValueAsString(
                                duplicatePaymentRequestMessages.duplicateInvoiceVoidedKey);
                        foundVoided = true;
                    }
                } else {
                    messageText = configurationService.getPropertyValueAsString(
                            duplicatePaymentRequestMessages.duplicateInvoiceKey);
                    break;
                }
            }

            String formattedQuestion = questionFormat ? configurationService.getPropertyValueAsString(
                    FPKeyConstants.WARNING_DUPLICATE_INVOICE_QUESTION) : StringUtils.EMPTY;
            String formattedMessage = MessageFormat.format(messageText, specifiedSourceToken, formattedQuestion);
            messages.put(KFSConstants.PaymentRequestDocumentConstants.DUPLICATE_INVOICE_QUESTION, formattedMessage);
        }

        return messages;
    }

    @Override
    public PaymentRequestDocument getPaymentRequestByDocumentNumber(String documentNumber) {
        LOG.debug("getPaymentRequestByDocumentNumber() started");

        if (ObjectUtils.isNotNull(documentNumber)) {
            return (PaymentRequestDocument) documentService.getByDocumentHeaderId(documentNumber);
        }
        return null;
    }

    @Override
    public PaymentRequestDocument getPaymentRequestById(Integer poDocId) {
        return getPaymentRequestByDocumentNumber(paymentRequestDao.getDocumentNumberByPaymentRequestId(poDocId));
    }

    @Override
    public List<PaymentRequestDocument> getPaymentRequestsByPurchaseOrderId(Integer poDocId) {
        List<PaymentRequestDocument> preqs = new ArrayList<>();
        List<String> docNumbers = paymentRequestDao.getDocumentNumbersByPurchaseOrderId(poDocId);
        for (String docNumber : docNumbers) {
            PaymentRequestDocument preq = getPaymentRequestByDocumentNumber(docNumber);
            if (ObjectUtils.isNotNull(preq)) {
                preqs.add(preq);
            }
        }
        return preqs;
    }

    @Override
    public Map<String, String> getPaymentRequestsByStatusAndPurchaseOrderId(String applicationDocumentStatus,
            Integer purchaseOrderId) {
        List<String> paymentRequestDocNumbers = paymentRequestDao.getDocumentNumbersByPurchaseOrderId(purchaseOrderId);

        Map<String, String> paymentRequestResults = new HashMap<>();
        paymentRequestResults.put("hasInProcess", "N");
        paymentRequestResults.put("checkInProcess", "N");

        // if there are no payment request document numbers exist then there is no need to check for application
        // document status on the workflow documents....
        if (paymentRequestDocNumbers == null || paymentRequestDocNumbers.isEmpty()) {
            return paymentRequestResults;
        }

        // helper method to filter the workflow documents that are created for Preq documents. updates the map for
        // hasInProcess value to Y if records found for app doc status else sets the value of checkInProcess = Y.
        filterPaymentRequestByAppDocStatus(paymentRequestResults, paymentRequestDocNumbers, applicationDocumentStatus);

        return paymentRequestResults;
    }

    @Override
    public List<PaymentRequestDocument> getPaymentRequestsByPOIdInvoiceAmountInvoiceDate(Integer poId,
            KualiDecimal invoiceAmount, Date invoiceDate) {
        LOG.debug("getPaymentRequestsByPOIdInvoiceAmountInvoiceDate() started");
        return paymentRequestDao.getActivePaymentRequestsByPOIdInvoiceAmountInvoiceDate(poId, invoiceAmount,
                invoiceDate);
    }

    private List<PaymentRequestDocument> getPaymentRequestsByInvoiceAmountInvoiceDate(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId, KualiDecimal invoiceAmount, Date invoiceDate) {
        LOG.debug("getPaymentRequestsByInvoiceAmountInvoiceDate() started");
        return paymentRequestDao.getActivePaymentRequestsByInvoiceAmountInvoiceDate(vendorHeaderGeneratedId,
                vendorDetailAssignedId, invoiceAmount, invoiceDate);
    }

    @Override
    public boolean isInvoiceDateAfterToday(Date invoiceDate) {
        // Check invoice date to make sure it is today or before
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, 11);
        now.set(Calendar.MINUTE, 59);
        now.set(Calendar.SECOND, 59);
        now.set(Calendar.MILLISECOND, 59);
        Timestamp nowTime = new Timestamp(now.getTimeInMillis());
        Calendar invoiceDateC = Calendar.getInstance();
        invoiceDateC.setTime(invoiceDate);
        // set time to midnight
        invoiceDateC.set(Calendar.HOUR, 0);
        invoiceDateC.set(Calendar.MINUTE, 0);
        invoiceDateC.set(Calendar.SECOND, 0);
        invoiceDateC.set(Calendar.MILLISECOND, 0);
        Timestamp invoiceDateTime = new Timestamp(invoiceDateC.getTimeInMillis());
        return invoiceDateTime.compareTo(nowTime) > 0;
    }

    @Override
    public java.sql.Date calculatePayDate(Date invoiceDate, PaymentTermType terms) {
        LOG.debug("calculatePayDate() started");
        // calculate the invoice + processed calendar
        Calendar invoicedDateCalendar = dateTimeService.getCalendar(invoiceDate);
        Calendar processedDateCalendar = dateTimeService.getCurrentCalendar();

        // add default number of days to processed
        String defaultDays = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PREQ_PAY_DATE_DEFAULT_NUMBER_OF_DAYS);
        processedDateCalendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(defaultDays));

        if (ObjectUtils.isNull(terms) || StringUtils.isEmpty(terms.getVendorPaymentTermsCode())) {
            invoicedDateCalendar.add(Calendar.DAY_OF_MONTH, PurapConstants.PREQ_PAY_DATE_EMPTY_TERMS_DEFAULT_DAYS);
            return returnLaterDate(invoicedDateCalendar, processedDateCalendar);
        }

        // Retrieve pay date variation parameter (currently defined as 2).  See parameter description for explanation
        // of it's use.
        String payDateVariance = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PREQ_PAY_DATE_VARIANCE);
        int payDateVarianceInt = Integer.parseInt(payDateVariance);

        Integer discountDueNumber = terms.getVendorDiscountDueNumber();
        Integer netDueNumber = terms.getVendorNetDueNumber();
        if (ObjectUtils.isNotNull(discountDueNumber)) {
            // Decrease discount due number by the pay date variance
            discountDueNumber -= payDateVarianceInt;
            if (discountDueNumber < 0) {
                discountDueNumber = 0;
            }
            String discountDueTypeDescription = terms.getVendorDiscountDueTypeDescription();
            paymentTermsDateCalculation(discountDueTypeDescription, invoicedDateCalendar, discountDueNumber);
        } else if (ObjectUtils.isNotNull(netDueNumber)) {
            // Decrease net due number by the pay date variance
            netDueNumber -= payDateVarianceInt;
            if (netDueNumber < 0) {
                netDueNumber = 0;
            }
            String netDueTypeDescription = terms.getVendorNetDueTypeDescription();
            paymentTermsDateCalculation(netDueTypeDescription, invoicedDateCalendar, netDueNumber);
        } else {
            throw new RuntimeException("Neither discount or net number were specified for this payment terms type");
        }

        // return the later date
        return returnLaterDate(invoicedDateCalendar, processedDateCalendar);
    }

    /**
     * Returns whichever date is later, the invoicedDateCalendar or the processedDateCalendar.
     *
     * @param invoicedDateCalendar  One of the dates to be used in determining which date is later.
     * @param processedDateCalendar The other date to be used in determining which date is later.
     * @return The date which is the later of the two given dates in the input parameters.
     */
    protected java.sql.Date returnLaterDate(Calendar invoicedDateCalendar, Calendar processedDateCalendar) {
        if (invoicedDateCalendar.after(processedDateCalendar)) {
            return new java.sql.Date(invoicedDateCalendar.getTimeInMillis());
        } else {
            return new java.sql.Date(processedDateCalendar.getTimeInMillis());
        }
    }

    /**
     * Calculates the paymentTermsDate given the dueTypeDescription, invoicedDateCalendar and the dueNumber.
     *
     * @param dueTypeDescription   The due type description of the payment term.
     * @param invoicedDateCalendar The Calendar object of the invoice date.
     * @param dueNumber            Either the vendorDiscountDueNumber or the vendorDiscountDueNumber of the payment
     *                             term.
     */
    protected void paymentTermsDateCalculation(String dueTypeDescription, Calendar invoicedDateCalendar,
            Integer dueNumber) {
        if (StringUtils.equals(dueTypeDescription, PurapConstants.PREQ_PAY_DATE_DATE)) {
            // date specified set to date in next month
            invoicedDateCalendar.add(Calendar.MONTH, 1);
            invoicedDateCalendar.set(Calendar.DAY_OF_MONTH, dueNumber);
        } else if (StringUtils.equals(PurapConstants.PREQ_PAY_DATE_DAYS, dueTypeDescription)) {
            // days specified go forward that number
            invoicedDateCalendar.add(Calendar.DAY_OF_MONTH, dueNumber);
        } else {
            // improper string
            throw new RuntimeException("missing payment terms description or not properly entered on payment term " +
                    "maintenance doc");
        }
    }

    @Override
    public void calculatePaymentRequest(PaymentRequestDocument paymentRequest, boolean updateDiscount) {
        LOG.debug("calculatePaymentRequest() started");

        // general calculation, i.e. for the whole preq document
        if (ObjectUtils.isNull(paymentRequest.getPaymentRequestPayDate())) {
            paymentRequest.setPaymentRequestPayDate(calculatePayDate(paymentRequest.getInvoiceDate(),
                    paymentRequest.getVendorPaymentTerms()));
        }

        distributeAccounting(paymentRequest);

        purapService.calculateTax(paymentRequest);

        // do proration for full order and trade in
        purapService.prorateForTradeInAndFullOrderDiscount(paymentRequest);

        // do proration for payment terms discount
        if (updateDiscount) {
            calculateDiscount(paymentRequest);
        }

        distributeAccounting(paymentRequest);
    }

    /**
     * Calculates the discount item for this paymentRequest.
     *
     * @param paymentRequestDocument The payment request document whose discount to be calculated.
     */
    protected void calculateDiscount(PaymentRequestDocument paymentRequestDocument) {
        PaymentRequestItem discountItem = findDiscountItem(paymentRequestDocument);
        // find out if we really need the discount item
        PaymentTermType pt = paymentRequestDocument.getVendorPaymentTerms();
        if (pt != null && pt.getVendorPaymentTermsPercent() != null
                && BigDecimal.ZERO.compareTo(pt.getVendorPaymentTermsPercent()) != 0) {
            if (discountItem == null) {
                // set discountItem and add to items this is probably not the best way of doing it but should work for
                // now if we start excluding discount from below we will need to manually add
                purapService.addBelowLineItems(paymentRequestDocument);

                // fix up below the line items
                removeIneligibleAdditionalCharges(paymentRequestDocument);

                discountItem = findDiscountItem(paymentRequestDocument);
            }

            // Deleted the discountItem.getExtendedPrice() null and isZero
            PaymentRequestItem fullOrderItem = findFullOrderDiscountItem(paymentRequestDocument);
            KualiDecimal fullOrderAmount = KualiDecimal.ZERO;
            KualiDecimal fullOrderTaxAmount = KualiDecimal.ZERO;

            if (fullOrderItem != null) {
                fullOrderAmount = ObjectUtils.isNotNull(fullOrderItem.getExtendedPrice()) ?
                        fullOrderItem.getExtendedPrice() : KualiDecimal.ZERO;
                fullOrderTaxAmount = ObjectUtils.isNotNull(fullOrderItem.getItemTaxAmount()) ?
                        fullOrderItem.getItemTaxAmount() : KualiDecimal.ZERO;
            }
            KualiDecimal totalCost = paymentRequestDocument.getTotalPreTaxDollarAmountAboveLineItems()
                    .add(fullOrderAmount);
            PurApItem tradeInItem = paymentRequestDocument.getTradeInItem();
            if (ObjectUtils.isNotNull(tradeInItem)) {
                totalCost = totalCost.add(tradeInItem.getTotalAmount());
            }
            BigDecimal discountAmount = pt.getVendorPaymentTermsPercent().multiply(totalCost.bigDecimalValue())
                    .multiply(new BigDecimal(PurapConstants.PREQ_DISCOUNT_MULT));

            // do we really need to set both, not positive, but probably won't hurt
            discountItem.setItemUnitPrice(discountAmount.setScale(2, KualiDecimal.ROUND_BEHAVIOR));
            discountItem.setExtendedPrice(new KualiDecimal(discountAmount));

            // set tax amount
            boolean salesTaxInd = parameterService.getParameterValueAsBoolean(
                    KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_SALES_TAX_IND);
            boolean useTaxIndicator = paymentRequestDocument.isUseTaxIndicator();

            if (salesTaxInd && !useTaxIndicator) {
                KualiDecimal totalTax = paymentRequestDocument.getTotalTaxAmountAboveLineItems()
                        .add(fullOrderTaxAmount);
                BigDecimal discountTaxAmount;
                if (totalCost.isNonZero()) {
                    discountTaxAmount = discountAmount.divide(totalCost.bigDecimalValue())
                            .multiply(totalTax.bigDecimalValue());
                } else {
                    discountTaxAmount = BigDecimal.ZERO;
                }

                discountItem.setItemTaxAmount(new KualiDecimal(discountTaxAmount.setScale(KualiDecimal.SCALE,
                        KualiDecimal.ROUND_BEHAVIOR)));
            }

            // set document
            discountItem.setPurapDocument(paymentRequestDocument);
        } else {
            // no discount
            if (discountItem != null) {
                paymentRequestDocument.getItems().remove(discountItem);
            }
        }
    }

    @Override
    public void clearTax(PaymentRequestDocument document) {
        // remove all existing tax items added by previous calculation
        removeTaxItems(document);
        // reset values
        document.setTaxClassificationCode(null);
        document.setTaxFederalPercent(null);
        document.setTaxStatePercent(null);
        document.setTaxCountryCode(null);
        document.setTaxNQIId(null);

        document.setTaxForeignSourceIndicator(false);
        document.setTaxExemptTreatyIndicator(false);
        document.setTaxOtherExemptIndicator(false);
        document.setTaxGrossUpIndicator(false);
        document.setTaxUSAIDPerDiemIndicator(false);
        document.setTaxSpecialW4Amount(null);
    }

    @Override
    public void calculateTaxArea(PaymentRequestDocument preq) {
        LOG.debug("calculateTaxArea() started");

        // remove all existing tax items added by previous calculation
        removeTaxItems(preq);

        // don't need to calculate tax items if TaxClassificationCode is N (Non_Reportable)
        if (StringUtils.equalsIgnoreCase(preq.getTaxClassificationCode(), "N")) {
            return;
        }

        // reserve the grand total excluding any tax amount, to be used as the base to compute all tax items
        // if we don't reserve this, the pre-tax total could be changed as new tax items are added
        BigDecimal taxableAmount = preq.getGrandPreTaxTotal().bigDecimalValue();

        // generate and add state tax gross up item and its accounting line, update total amount,
        // if gross up indicator is true and tax rate is non-zero
        if (preq.getTaxGrossUpIndicator() && preq.getTaxStatePercent().compareTo(new BigDecimal(0)) != 0) {
            addTaxItem(preq, ItemTypeCodes.ITEM_TYPE_STATE_GROSS_CODE, taxableAmount);
        }

        // generate and add state tax item and its accounting line, update total amount, if tax rate is non-zero
        if (preq.getTaxStatePercent().compareTo(new BigDecimal(0)) != 0) {
            addTaxItem(preq, ItemTypeCodes.ITEM_TYPE_STATE_TAX_CODE, taxableAmount);
        }

        // generate and add federal tax gross up item and its accounting line, update total amount,
        // if gross up indicator is true and tax rate is non-zero
        if (preq.getTaxGrossUpIndicator() && preq.getTaxFederalPercent().compareTo(new BigDecimal(0)) != 0) {
            addTaxItem(preq, ItemTypeCodes.ITEM_TYPE_FEDERAL_GROSS_CODE, taxableAmount);
        }

        // generate and add federal tax item and its accounting line, update total amount, if tax rate is non-zero
        if (preq.getTaxFederalPercent().compareTo(new BigDecimal(0)) != 0) {
            addTaxItem(preq, ItemTypeCodes.ITEM_TYPE_FEDERAL_TAX_CODE, taxableAmount);
        }

        // FIXME if user request to add zero tax lines and remove them after tax approval,
        // then remove the conditions above when adding the tax lines, and
        // add a branch in PaymentRequestDocument.processNodeChange to call PurapService.deleteUnenteredItems
    }

    /**
     * Removes all existing Nonresident tax items from the specified payment request.
     *
     * @param preq The payment request from which all tax items are to be removed.
     */
    protected void removeTaxItems(PaymentRequestDocument preq) {
        ((List<PurApItem>) preq.getItems())
                .removeIf(item -> ItemTypeCodes.ITEM_TYPE_FEDERAL_TAX_CODE.equals(item.getItemTypeCode())
                        || ItemTypeCodes.ITEM_TYPE_STATE_TAX_CODE.equals(item.getItemTypeCode())
                        || ItemTypeCodes.ITEM_TYPE_FEDERAL_GROSS_CODE.equals(item.getItemTypeCode())
                        || ItemTypeCodes.ITEM_TYPE_STATE_GROSS_CODE.equals(item.getItemTypeCode()));
    }

    /**
     * Generates a Nonresident tax item and adds to the specified payment request, according to the specified item type code.
     *
     * @param preq          The payment request the tax item will be added to.
     * @param itemTypeCode  The item type code for the tax item.
     * @param taxableAmount The amount to which tax is computed against.
     */
    protected void addTaxItem(PaymentRequestDocument preq, String itemTypeCode, BigDecimal taxableAmount) {
        PurApItem taxItem;

        try {
            taxItem = (PurApItem) preq.getItemClass().getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException e) {
            throw new InfrastructureException("Unable to access itemClass", e);
        } catch (ReflectiveOperationException e) {
            throw new InfrastructureException("Unable to instantiate itemClass", e);
        }

        // add item to preq before adding the accounting line
        taxItem.setItemTypeCode(itemTypeCode);
        preq.addItem(taxItem);

        // generate and add tax accounting line
        PurApAccountingLine taxLine = addTaxAccountingLine(taxItem, taxableAmount);

        // set extended price amount as now it's calculated when accounting line is generated
        taxItem.setItemUnitPrice(taxLine.getAmount().bigDecimalValue());
        taxItem.setExtendedPrice(taxLine.getAmount());

        // use item type description as the item description
        ItemType itemType = new ItemType();
        itemType.setItemTypeCode(itemTypeCode);
        itemType = (ItemType) businessObjectService.retrieve(itemType);
        taxItem.setItemType(itemType);
        taxItem.setItemDescription(itemType.getItemTypeDescription());
    }

    /**
     * Generates a PurAP accounting line and adds to the specified tax item.
     *
     * @param taxItem       The specified tax item the accounting line will be associated with.
     * @param taxableAmount The amount to which tax is computed against.
     * @return A fully populated PurApAccountingLine instance for the specified tax item.
     */
    protected PurApAccountingLine addTaxAccountingLine(PurApItem taxItem, BigDecimal taxableAmount) {
        PaymentRequestDocument preq = taxItem.getPurapDocument();
        PurApAccountingLine taxLine;

        try {
            taxLine = (PurApAccountingLine) taxItem.getAccountingLineClass().getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException e) {
            throw new InfrastructureException("Unable to access sourceAccountingLineClass", e);
        } catch (ReflectiveOperationException e) {
            throw new InfrastructureException("Unable to instantiate sourceAccountingLineClass", e);
        }

        // tax item type indicators
        boolean isFederalTax = ItemTypeCodes.ITEM_TYPE_FEDERAL_TAX_CODE.equals(taxItem.getItemTypeCode());
        boolean isFederalGross = ItemTypeCodes.ITEM_TYPE_FEDERAL_GROSS_CODE.equals(taxItem.getItemTypeCode());
        boolean isStateTax = ItemTypeCodes.ITEM_TYPE_STATE_TAX_CODE.equals(taxItem.getItemTypeCode());
        boolean isStateGross = ItemTypeCodes.ITEM_TYPE_STATE_GROSS_CODE.equals(taxItem.getItemTypeCode());
        // true for federal tax/gross; false for state tax/gross
        boolean isFederal = isFederalTax || isFederalGross;
        // true for federal/state gross, false for federal/state tax
        boolean isGross = isFederalGross || isStateGross;

        // obtain accounting line info according to tax item type code
        String taxChart = null;
        String taxAccount = null;
        String taxObjectCode = null;

        if (isGross) {
            // for gross up tax items, use preq's first item's first accounting line, which shall exist at this point
            AccountingLine line1 = preq.getFirstAccount();
            taxChart = line1.getChartOfAccountsCode();
            taxAccount = line1.getAccountNumber();
            taxObjectCode = line1.getFinancialObjectCode();
        } else if (isFederalTax) {
            // for federal tax item, get chart, account, object code info from parameters
            taxChart = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                    NonresidentTaxParameters.FEDERAL_TAX_PARM_PREFIX + NonresidentTaxParameters.TAX_PARM_CHART_SUFFIX);
            taxAccount = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                    NonresidentTaxParameters.FEDERAL_TAX_PARM_PREFIX + NonresidentTaxParameters.TAX_PARM_ACCOUNT_SUFFIX);
            taxObjectCode = parameterService.getSubParameterValueAsString(PaymentRequestDocument.class,
                    NonresidentTaxParameters.FEDERAL_TAX_PARM_PREFIX + NonresidentTaxParameters.TAX_PARM_OBJECT_BY_INCOME_CLASS_SUFFIX,
                    preq.getTaxClassificationCode());
            if (StringUtils.isBlank(taxChart) || StringUtils.isBlank(taxAccount) || StringUtils.isBlank(taxObjectCode)) {
                LOG.error("Unable to retrieve federal tax parameters.");
                throw new RuntimeException("Unable to retrieve federal tax parameters.");
            }
        } else if (isStateTax) {
            // for state tax item, get chart, account, object code info from parameters
            taxChart = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                    NonresidentTaxParameters.STATE_TAX_PARM_PREFIX + NonresidentTaxParameters.TAX_PARM_CHART_SUFFIX);
            taxAccount = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                    NonresidentTaxParameters.STATE_TAX_PARM_PREFIX + NonresidentTaxParameters.TAX_PARM_ACCOUNT_SUFFIX);
            taxObjectCode = parameterService.getSubParameterValueAsString(PaymentRequestDocument.class,
                    NonresidentTaxParameters.STATE_TAX_PARM_PREFIX + NonresidentTaxParameters.TAX_PARM_OBJECT_BY_INCOME_CLASS_SUFFIX,
                    preq.getTaxClassificationCode());
            if (StringUtils.isBlank(taxChart) || StringUtils.isBlank(taxAccount) || StringUtils.isBlank(taxObjectCode)) {
                LOG.error("Unable to retrieve state tax parameters.");
                throw new RuntimeException("Unable to retrieve state tax parameters.");
            }
        }

        // calculate tax amount according to gross up indicator and federal/state tax type
        /*
            The formula of tax and gross up amount are as follows:
            if (not gross up) gross not existing taxFederal/State = - amount * rateFederal/State
            otherwise gross up grossFederal/State = amount * rateFederal/State / (1 - rateFederal - rateState) tax = - gross
         */

        // pick federal/state tax rate
        BigDecimal taxPercentFederal = preq.getTaxFederalPercent();
        BigDecimal taxPercentState = preq.getTaxStatePercent();
        BigDecimal taxPercent = isFederal ? taxPercentFederal : taxPercentState;

        // divider value according to gross up or not
        BigDecimal taxDivider = new BigDecimal(100);
        if (preq.getTaxGrossUpIndicator()) {
            taxDivider = taxDivider.subtract(taxPercentFederal.add(taxPercentState));
        }

        // tax = amount * rate / divider
        BigDecimal taxAmount = taxableAmount.multiply(taxPercent);
        taxAmount = taxAmount.divide(taxDivider, 5, RoundingMode.HALF_UP);

        // tax is always negative, since it reduces the total amount; while gross up is always the positive of tax
        if (!isGross) {
            taxAmount = taxAmount.negate();
        }

        // populate necessary accounting line fields
        taxLine.setDocumentNumber(preq.getDocumentNumber());
        taxLine.setSequenceNumber(preq.getNextSourceLineNumber());
        taxLine.setChartOfAccountsCode(taxChart);
        taxLine.setAccountNumber(taxAccount);
        taxLine.setFinancialObjectCode(taxObjectCode);
        taxLine.setAmount(new KualiDecimal(taxAmount));

        // add the accounting line to the item
        taxLine.setItemIdentifier(taxItem.getItemIdentifier());
        taxLine.setPurapItem(taxItem);
        taxItem.getSourceAccountingLines().add(taxLine);

        return taxLine;
    }

    /**
     * Finds the discount item of the payment request document.
     *
     * @param paymentRequestDocument The payment request document to be used to find the discount item.
     * @return The discount item if it exists.
     */
    protected PaymentRequestItem findDiscountItem(PaymentRequestDocument paymentRequestDocument) {
        PaymentRequestItem discountItem = null;
        for (PaymentRequestItem preqItem : (List<PaymentRequestItem>) paymentRequestDocument.getItems()) {
            if (StringUtils.equals(preqItem.getItemTypeCode(),
                    PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)) {
                discountItem = preqItem;
                break;
            }
        }
        return discountItem;
    }

    /**
     * Finds the full order discount item of the payment request document.
     *
     * @param paymentRequestDocument The payment request document to be used to find the full order discount item.
     * @return The discount item if it exists.
     */
    protected PaymentRequestItem findFullOrderDiscountItem(PaymentRequestDocument paymentRequestDocument) {
        PaymentRequestItem discountItem = null;
        for (PaymentRequestItem preqItem : (List<PaymentRequestItem>) paymentRequestDocument.getItems()) {
            if (StringUtils.equals(preqItem.getItemTypeCode(),
                    PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) {
                discountItem = preqItem;
                break;
            }
        }
        return discountItem;
    }

    /**
     * Distributes accounts for a payment request document.
     */
    protected void distributeAccounting(PaymentRequestDocument paymentRequestDocument) {
        // update the account amounts before doing any distribution
        purapAccountingService.updateAccountAmounts(paymentRequestDocument);

        for (PaymentRequestItem item : (List<PaymentRequestItem>) paymentRequestDocument.getItems()) {
            KualiDecimal totalAmount;
            List<PurApAccountingLine> distributedAccounts = null;
            List<SourceAccountingLine> summaryAccounts;

            // skip above the line
            if (item.getItemType().isLineItemIndicator()) {
                continue;
            }

            if (item.getSourceAccountingLines().isEmpty() && ObjectUtils.isNotNull(item.getExtendedPrice())
                    && KualiDecimal.ZERO.compareTo(item.getExtendedPrice()) != 0) {
                if (StringUtils.equals(ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE,
                            item.getItemType().getItemTypeCode())
                        && paymentRequestDocument.getGrandTotal() != null
                        && KualiDecimal.ZERO.compareTo(paymentRequestDocument.getGrandTotal()) != 0) {
                    // No discount is applied to other item types other than item line

                    // total amount should be the line item total, not the grand total
                    totalAmount = paymentRequestDocument.getLineItemTotal();

                    // prorate item line accounts only
                    Set<String> includedItemTypeCodes = new HashSet<>();
                    includedItemTypeCodes.add(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE);
                    includedItemTypeCodes.add(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE);

                    summaryAccounts = purapAccountingService.generateSummaryIncludeItemTypesAndNoZeroTotals(
                            paymentRequestDocument.getItems(), includedItemTypeCodes);
                    //if summaryAccount is empty then do not call generateAccountDistributionForProration as
                    //there is a check in that method to throw NPE if accounts percents == 0..
                    if (summaryAccounts != null) {
                        distributedAccounts = purapAccountingService.generateAccountDistributionForProration(
                                summaryAccounts, totalAmount, PurapConstants.PRORATION_SCALE,
                                PaymentRequestAccount.class);
                    }

                    boolean rulePassed = kualiRuleService.applyRules(
                            new PurchasingAccountsPayableItemPreCalculateEvent(paymentRequestDocument, item));

                    if (rulePassed) {
                        purapAccountingService.updatePreqProporationalAccountAmountsWithTotal(distributedAccounts,
                                item.getTotalAmount());
                    }
                } else {
                    PurchaseOrderItem poi = item.getPurchaseOrderItem();
                    if (poi != null && poi.getSourceAccountingLines() != null
                            && !poi.getSourceAccountingLines().isEmpty()
                            && poi.getExtendedPrice() != null
                            && KualiDecimal.ZERO.compareTo(poi.getExtendedPrice()) != 0) {
                        // use accounts from purchase order item matching this item
                        // account list of current item is already empty
                        item.generateAccountListFromPoItemAccounts(poi.getSourceAccountingLines());
                    } else {
                        totalAmount = paymentRequestDocument.getPurchaseOrderDocument()
                                .getTotalDollarAmountAboveLineItems();
                        purapAccountingService.updateAccountAmounts(paymentRequestDocument.getPurchaseOrderDocument());
                        summaryAccounts = purapAccountingService.generateSummary(PurApItemUtils.getAboveTheLineOnly(
                                paymentRequestDocument.getPurchaseOrderDocument().getItems()));
                        //if summaryAccount is empty then do not call generateAccountDistributionForProration as
                        //there is a check in that method to throw NPE if accounts percents == 0..
                        if (summaryAccounts != null) {
                            distributedAccounts = purapAccountingService.generateAccountDistributionForProration(
                                    summaryAccounts, totalAmount, Integer.valueOf("6"), PaymentRequestAccount.class);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(distributedAccounts)
                        && CollectionUtils.isEmpty(item.getSourceAccountingLines())) {
                    item.setSourceAccountingLines(distributedAccounts);
                }
            }
        }

        // update again now that distribute is finished. (Note: we may not need this anymore now that I added
        // updateItem line above leave the call below since we need to this when sequential method is used on the
        // document.
        purapAccountingService.updateAccountAmounts(paymentRequestDocument);
    }

    @Override
    public PaymentRequestDocument addHoldOnPaymentRequest(PaymentRequestDocument document, String note) {
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setHoldIndicator(true);
        document.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        purapService.saveDocumentNoValidation(document);

        return document;
    }

    @Override
    public PaymentRequestDocument removeHoldOnPaymentRequest(PaymentRequestDocument document, String note) {
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setHoldIndicator(false);
        document.setLastActionPerformedByPersonId(null);
        purapService.saveDocumentNoValidation(document);

        return document;
    }

    @Override
    public void requestCancelOnPaymentRequest(PaymentRequestDocument document, String note) {
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setPaymentRequestedCancelIndicator(true);
        document.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        document.setAccountsPayableRequestCancelIdentifier(GlobalVariables.getUserSession().getPerson()
                .getPrincipalId());
        purapService.saveDocumentNoValidation(document);
    }

    @Override
    public void removeRequestCancelOnPaymentRequest(PaymentRequestDocument document, String note) {
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        clearRequestCancelFields(document);
        purapService.saveDocumentNoValidation(document);
    }

    /**
     * Clears the request cancel fields.
     *
     * @param document The payment request document whose request cancel fields to be cleared.
     */
    protected void clearRequestCancelFields(PaymentRequestDocument document) {
        document.setPaymentRequestedCancelIndicator(false);
        document.setLastActionPerformedByPersonId(null);
        document.setAccountsPayableRequestCancelIdentifier(null);
    }

    @Override
    public boolean isExtracted(PaymentRequestDocument document) {
        return !ObjectUtils.isNull(document.getExtractedTimestamp());
    }

    @Override
    public void cancelExtractedPaymentRequest(PaymentRequestDocument paymentRequest, String note) {
        LOG.debug("cancelExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("cancelExtractedPaymentRequest() ended");
            return;
        }

        try {
            Note cancelNote = documentService.createNoteFromDocument(paymentRequest, note);
            paymentRequest.addNote(cancelNote);
            noteService.save(cancelNote);
        } catch (Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE, e);
        }

        // cancel extracted should not reopen PO
        paymentRequest.setReopenPurchaseOrderIndicator(false);

        // Performs save, so no explicit save is necessary
        accountsPayableService.cancelAccountsPayableDocument(paymentRequest, "");

        LOG.debug("cancelExtractedPaymentRequest() PREQ {} Cancelled Without Workflow",
                paymentRequest.getPurapDocumentIdentifier());
        LOG.debug("cancelExtractedPaymentRequest() ended");
    }

    @Override
    public void resetExtractedPaymentRequest(PaymentRequestDocument paymentRequest, String note) {
        LOG.debug("resetExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("resetExtractedPaymentRequest() ended");
            return;
        }
        paymentRequest.setExtractedTimestamp(null);
        paymentRequest.setPaymentPaidTimestamp(null);
        String noteText = "This Payment Request is being reset for extraction by PDP " + note;
        try {
            Note resetNote = documentService.createNoteFromDocument(paymentRequest, noteText);
            paymentRequest.addNote(resetNote);
            noteService.save(resetNote);
        } catch (Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE + " " + e);
        }
        purapService.saveDocumentNoValidation(paymentRequest);
        LOG.debug("resetExtractedPaymentRequest() PREQ {} Reset from Extracted status",
                paymentRequest.getPurapDocumentIdentifier());
    }

    @Override
    public void populatePaymentRequest(PaymentRequestDocument paymentRequestDocument) {
        PurchaseOrderDocument purchaseOrderDocument = paymentRequestDocument.getPurchaseOrderDocument();

        // make a call to search for expired/closed accounts
        HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList =
                accountsPayableService.getExpiredOrClosedAccountList(paymentRequestDocument);

        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(purchaseOrderDocument,
                expiredOrClosedAccountList);

        paymentRequestDocument.getDocumentHeader().setDocumentDescription(createPreqDocumentDescription(
                paymentRequestDocument.getPurchaseOrderIdentifier(), paymentRequestDocument.getVendorName()));

        // write a note for expired/closed accounts if any exist and add a message stating there were expired/closed
        // accounts at the top of the document
        accountsPayableService.generateExpiredOrClosedAccountNote(paymentRequestDocument, expiredOrClosedAccountList);

        // set indicator so a message is displayed for accounts that were replaced due to expired/closed status
        if (!expiredOrClosedAccountList.isEmpty()) {
            paymentRequestDocument.setContinuationAccountIndicator(true);
        }

        // add discount item
        calculateDiscount(paymentRequestDocument);
        // distribute accounts (i.e. proration)
        distributeAccounting(paymentRequestDocument);

        // set bank code to default bank code in the system parameter
        Bank defaultBank = bankService.getDefaultBankByDocType(paymentRequestDocument.getClass());
        if (defaultBank != null) {
            paymentRequestDocument.setBankCode(defaultBank.getBankCode());
            paymentRequestDocument.setBank(defaultBank);
        }
    }

    @Override
    public String createPreqDocumentDescription(Integer purchaseOrderIdentifier, String vendorName) {
        StringBuilder descr = new StringBuilder();
        descr.append("PO: ");
        descr.append(purchaseOrderIdentifier);
        descr.append(" Vendor: ");
        descr.append(StringUtils.trimToEmpty(vendorName));

        int noteTextMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class,
                KRADPropertyConstants.DOCUMENT_DESCRIPTION);
        if (noteTextMaxLength >= descr.length()) {
            return descr.toString();
        } else {
            return descr.substring(0, noteTextMaxLength);
        }
    }

    @Override
    public void populateAndSavePaymentRequest(PaymentRequestDocument preq) {
        try {
            preq.updateAndSaveAppDocStatus(PaymentRequestStatuses.APPDOC_IN_PROCESS);
            documentService.saveDocument(preq, AttributedContinuePurapEvent.class);
        } catch (ValidationException ve) {
            preq.updateAndSaveAppDocStatus(PaymentRequestStatuses.APPDOC_INITIATE);
        }
    }

    /**
     * If the full document entry has been completed and the status of the related purchase order document is closed,
     * return true, otherwise return false.
     *
     * @param apDoc The AccountsPayableDocument to be determined whether its purchase order should be reversed.
     * @return boolean true if the purchase order should be reversed.
     */
    @Override
    public boolean shouldPurchaseOrderBeReversed(AccountsPayableDocument apDoc) {
        PurchaseOrderDocument po = apDoc.getPurchaseOrderDocument();
        if (ObjectUtils.isNull(po)) {
            throw new RuntimeException("po should never be null on PREQ");
        }
        // if past full entry and already closed return true
        return purapService.isFullDocumentEntryCompleted(apDoc)
                && StringUtils.equalsIgnoreCase(PurchaseOrderStatuses.APPDOC_CLOSED,
                    po.getApplicationDocumentStatus());
    }

    @Override
    public Person getPersonForCancel(AccountsPayableDocument apDoc) {
        PaymentRequestDocument preqDoc = (PaymentRequestDocument) apDoc;
        Person user = null;
        if (preqDoc.isPaymentRequestedCancelIndicator()) {
            user = preqDoc.getLastActionPerformedByUser();
        }
        return user;
    }

    @Override
    public void takePurchaseOrderCancelAction(AccountsPayableDocument apDoc) {
        PaymentRequestDocument preqDocument = (PaymentRequestDocument) apDoc;
        if (preqDocument.isReopenPurchaseOrderIndicator()) {
            String docType = PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_REOPEN_DOCUMENT;
            purchaseOrderService.createAndRoutePotentialChangeDocument(
                    preqDocument.getPurchaseOrderDocument().getDocumentNumber(), docType,
                    "reopened by Credit Memo " + apDoc.getPurapDocumentIdentifier() + "cancel",
                    new ArrayList(), PurchaseOrderStatuses.APPDOC_PENDING_REOPEN);
        }
    }

    @Override
    public String updateStatusByNode(String currentNodeName, AccountsPayableDocument apDoc) {
        return updateStatusByNode(currentNodeName, (PaymentRequestDocument) apDoc);
    }

    /**
     * Updates the status of the payment request document.
     *
     * @param currentNodeName The current node name.
     * @param preqDoc         The payment request document whose status to be updated.
     * @return The canceled status code.
     */
    protected String updateStatusByNode(String currentNodeName, PaymentRequestDocument preqDoc) {
        // remove request cancel if necessary
        clearRequestCancelFields(preqDoc);

        // update the status on the document

        String cancelledStatus;
        if (StringUtils.isEmpty(currentNodeName)) {
            // if empty probably not coming from workflow
            cancelledStatus = PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE;
        } else {
            cancelledStatus = PaymentRequestStatuses.getPaymentRequestAppDocDisapproveStatuses()
                    .get(currentNodeName);
        }

        if (StringUtils.isNotBlank(cancelledStatus)) {
            preqDoc.updateAndSaveAppDocStatus(cancelledStatus);
            purapService.saveDocumentNoValidation(preqDoc);
        } else {
            logAndThrowRuntimeException("No status found to set for document being disapproved in node '" +
                    currentNodeName + "'");
        }
        return cancelledStatus;
    }

    @Override
    public void markPaid(PaymentRequestDocument pr, Date processDate) {
        LOG.debug("markPaid() started");

        pr.setPaymentPaidTimestamp(new Timestamp(processDate.getTime()));
        purapService.saveDocumentNoValidation(pr);
    }

    @Override
    public boolean hasDiscountItem(PaymentRequestDocument preq) {
        return ObjectUtils.isNotNull(findDiscountItem(preq));
    }

    @Override
    public boolean poItemEligibleForAp(AccountsPayableDocument apDoc, PurchaseOrderItem poi) {
        if (ObjectUtils.isNull(poi)) {
            throw new RuntimeException("item null in purchaseOrderItemEligibleForPayment ... this should never happen");
        }
        // if the po item is not active... skip it
        if (!poi.isItemActiveIndicator()) {
            return false;
        }

        ItemType poiType = poi.getItemType();
        if (ObjectUtils.isNull(poiType)) {
            return false;
        }

        if (poiType.isQuantityBasedGeneralLedgerIndicator()) {
            return poi.getItemQuantity().isGreaterThan(poi.getItemInvoicedTotalQuantity());
        } else {
            // not quantity based
            // As long as it contains a number (whether it's 0, negative or positive number), we'll have to return
            // true. This is so that the OutstandingEncumberedAmount and the Original Amount from PO column would
            // appear on the page for Trade In.
            return poi.getItemOutstandingEncumberedAmount() != null;
        }
    }

    @Override
    public void removeIneligibleAdditionalCharges(PaymentRequestDocument document) {
        List<PaymentRequestItem> itemsToRemove = new ArrayList<>();

        for (PaymentRequestItem item : (List<PaymentRequestItem>) document.getItems()) {
            // if no extended price and its an order discount or trade in, remove
            if (ObjectUtils.isNull(item.getPurchaseOrderItemUnitPrice())
                    && (ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE.equals(item.getItemTypeCode())
                    || ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE.equals(item.getItemTypeCode()))) {
                itemsToRemove.add(item);
                continue;
            }

            // if a payment terms discount exists but not set on teh doc, remove
            if (StringUtils.equals(item.getItemTypeCode(),
                    PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)) {
                PaymentTermType pt = document.getVendorPaymentTerms();
                if (pt == null || pt.getVendorPaymentTermsPercent() == null
                        || BigDecimal.ZERO.compareTo(pt.getVendorPaymentTermsPercent()) == 0) {
                    // remove discount
                    itemsToRemove.add(item);
                }
            }
        }

        // remove items marked for removal
        for (PaymentRequestItem item : itemsToRemove) {
            document.getItems().remove(item);
        }
    }

    @Override
    public void changeVendor(PaymentRequestDocument preq, Integer headerId, Integer detailId) {
        VendorDetail primaryVendor = vendorService.getVendorDetail(preq.getOriginalVendorHeaderGeneratedIdentifier(),
                preq.getOriginalVendorDetailAssignedIdentifier());

        if (primaryVendor == null) {
            LOG.error("useAlternateVendor() primaryVendorDetail from database for header id {} and detail id {} is null",
                    headerId, detailId);
            throw new PurError("AlternateVendor: VendorDetail from database for header id " + headerId +
                    " and detail id " + detailId + "is null");
        }

        // set vendor detail
        VendorDetail vd = vendorService.getVendorDetail(headerId, detailId);
        if (vd == null) {
            LOG.error("changeVendor() VendorDetail from database for header id {} and detail id {} is null",
                    headerId, detailId);
            throw new PurError("changeVendor: VendorDetail from database for header id " + headerId +
                    " and detail id " + detailId + "is null");
        }
        preq.setVendorDetail(vd);
        preq.setVendorName(vd.getVendorName());
        preq.setVendorNumber(vd.getVendorNumber());
        preq.setVendorHeaderGeneratedIdentifier(vd.getVendorHeaderGeneratedIdentifier());
        preq.setVendorDetailAssignedIdentifier(vd.getVendorDetailAssignedIdentifier());
        preq.setVendorPaymentTermsCode(vd.getVendorPaymentTermsCode());
        preq.setVendorShippingPaymentTermsCode(vd.getVendorShippingPaymentTermsCode());
        preq.setVendorShippingTitleCode(vd.getVendorShippingTitleCode());
        preq.refreshReferenceObject("vendorPaymentTerms");
        preq.refreshReferenceObject("vendorShippingPaymentTerms");

        // Set vendor address
        String deliveryCampus = preq.getPurchaseOrderDocument().getDeliveryCampusCode();
        VendorAddress va = vendorService.getVendorDefaultAddress(headerId, detailId,
                VendorConstants.AddressTypes.REMIT, deliveryCampus);
        if (va == null) {
            va = vendorService.getVendorDefaultAddress(headerId, detailId,
                    VendorConstants.AddressTypes.PURCHASE_ORDER, deliveryCampus);
        }
        if (va == null) {
            LOG.error("changeVendor() VendorAddress from database for header id {} and detail id {} is null",
                    headerId, detailId);
            throw new PurError("changeVendor  VendorAddress from database for header id " + headerId +
                    " and detail id " + detailId + "is null");
        }

        setVendorAddress(va, preq);

        // change document description
        preq.getDocumentHeader().setDocumentDescription(createPreqDocumentDescription(
                preq.getPurchaseOrderIdentifier(), preq.getVendorName()));
    }

    /**
     * Set the Vendor address of the given ID.
     *
     * @param va   vendor address to set
     * @param preq PaymentRequest to set in
     */
    protected void setVendorAddress(VendorAddress va, PaymentRequestDocument preq) {
        if (va != null) {
            preq.setVendorAddressGeneratedIdentifier(va.getVendorAddressGeneratedIdentifier());
            preq.setVendorAddressInternationalProvinceName(va.getVendorAddressInternationalProvinceName());
            preq.setVendorLine1Address(va.getVendorLine1Address());
            preq.setVendorLine2Address(va.getVendorLine2Address());
            preq.setVendorCityName(va.getVendorCityName());
            preq.setVendorStateCode(va.getVendorStateCode());
            preq.setVendorPostalCode(va.getVendorZipCode());
            preq.setVendorCountryCode(va.getVendorCountryCode());
        }
    }

    /**
     * Records the specified error message into the Log file and throws a runtime exception.
     *
     * @param errorMessage the error message to be logged.
     */
    protected void logAndThrowRuntimeException(String errorMessage) {
        LOG.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }

    /**
     * The given document here actually needs to be a Payment Request.
     */
    @Override
    public void generateGLEntriesCreateAccountsPayableDocument(AccountsPayableDocument apDocument) {
        PaymentRequestDocument paymentRequest = (PaymentRequestDocument) apDocument;
        // JHK: this is not being injected because it would cause a circular reference in the Spring definitions
        SpringContext.getBean(PurapGeneralLedgerService.class).generateEntriesCreatePaymentRequest(paymentRequest);
    }

    @Override
    public boolean hasActivePaymentRequestsForPurchaseOrder(Integer purchaseOrderIdentifier) {
        int activePaymentRequestCount = paymentRequestDao.getActivePaymentRequestCountForPurchaseOrder(
                purchaseOrderIdentifier);
        return activePaymentRequestCount > 0;
    }

    /**
     * This method was added as part of the move to rice20 as a way to get at application doc status. Since
     * this data has been moved back into KFS this function is no longer necessary.  The code will be removed
     * in the 6.0 release.
     */
    @Deprecated
    protected List<String> getPaymentRequestDocNumberForAutoApprove() {
        Date todayAtMidnight = dateTimeService.getCurrentSqlDateMidnight();
        return paymentRequestDao.getEligibleForAutoApproval(todayAtMidnight);
    }

    /**
     * Filter the results by application doc status
     */
    protected void filterPaymentRequestByAppDocStatus(Map<String, String> paymentRequestResults,
            List<String> lookupDocNumbers, String... applicationDocumentStatus) {
        boolean hasInProcess = false;
        boolean checkInProcess = false;

        for (String docId : lookupDocNumbers) {
            DocumentHeader hdr = financialSystemDocumentService.findByDocumentNumber(docId);
            if (Arrays.asList(applicationDocumentStatus).contains(hdr.getApplicationDocumentStatus())) {
                hasInProcess = true;
            } else {
                checkInProcess = true;
            }
            if (hasInProcess && checkInProcess) {
                break;
            }
        }

        if (hasInProcess) {
            paymentRequestResults.put("hasInProcess", "Y");
        }

        if (checkInProcess) {
            paymentRequestResults.put("checkInProcess", "Y");
        }
    }

    /**
     * Wrapper class to the filterPaymentRequestByAppDocStatus
     * <p>
     * This class first extract the payment request document numbers from the Payment Request Collections,
     * then perform the filterPaymentRequestByAppDocStatus function.  Base on the filtered payment request
     * doc number, reconstruct the filtered Payment Request Collection
     */
    protected Collection<PaymentRequestDocument> filterPaymentRequestByAppDocStatus(
            Collection<PaymentRequestDocument> paymentRequestDocuments, String... appDocStatus) {
        Collection<PaymentRequestDocument> filteredPaymentRequestDocuments = new ArrayList<>();
        List<String> status = Arrays.asList(appDocStatus);
        for (PaymentRequestDocument paymentRequest : paymentRequestDocuments) {
            long start = System.currentTimeMillis();
            if (status.contains(paymentRequest.getApplicationDocumentStatus())) {
                filteredPaymentRequestDocuments.add(paymentRequest);
            }
            LOG.debug("{} ms to check app doc status", System.currentTimeMillis() - start);
        }

        return filteredPaymentRequestDocuments;
    }

    @Override
    @Transactional
    public void processPaymentRequestInReceivingStatus() {
        List<PaymentRequestDocument> preqs = paymentRequestDao.getPaymentRequestInReceivingStatus();

        for (PaymentRequestDocument preqDoc : preqs) {
            if (ObjectUtils.isNotNull(preqDoc) && preqDoc.isReceivingRequirementMet()) {
                documentService.approveDocument(preqDoc, "Approved by Receiving Required PREQ job", null);
            }
        }
    }

    @Override
    public boolean allowBackpost(PaymentRequestDocument paymentRequestDocument) {
        int allowBackpost = Integer.parseInt(parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapRuleConstants.ALLOW_BACKPOST_DAYS));

        Calendar today = dateTimeService.getCurrentCalendar();
        Integer currentFY = universityDateService.getCurrentUniversityDate().getUniversityFiscalYear();
        java.util.Date priorClosingDateTemp = universityDateService.getLastDateOfFiscalYear(currentFY - 1);
        Calendar priorClosingDate = Calendar.getInstance();
        priorClosingDate.setTime(priorClosingDateTemp);

        // adding 1 to set the date to midnight the day after backpost is allowed so that preqs allow backpost on the
        // last day
        Calendar allowBackpostDate = Calendar.getInstance();
        allowBackpostDate.setTime(priorClosingDate.getTime());
        allowBackpostDate.add(Calendar.DATE, allowBackpost + 1);

        Calendar preqInvoiceDate = Calendar.getInstance();
        preqInvoiceDate.setTime(paymentRequestDocument.getInvoiceDate());

        // if today is after the closing date but before/equal to the allowed backpost date and the invoice date is
        // for the prior year, set the year to prior year
        if (today.compareTo(priorClosingDate) > 0 && today.compareTo(allowBackpostDate) <= 0
                && preqInvoiceDate.compareTo(priorClosingDate) <= 0) {
            LOG.debug("allowBackpost() within range to allow backpost; posting entry to period 12 of previous FY");
            return true;
        }

        LOG.debug("allowBackpost() not within range to allow backpost; posting entry to current FY");
        return false;
    }

    @Override
    public boolean isPurchaseOrderValidForPaymentRequestDocumentCreation(PaymentRequestDocument paymentRequestDocument,
            PurchaseOrderDocument po) {
        boolean valid = true;

        PurchaseOrderDocument purchaseOrderDocument = paymentRequestDocument.getPurchaseOrderDocument();
        if (ObjectUtils.isNull(purchaseOrderDocument)) {
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER,
                    PurapKeyConstants.ERROR_PURCHASE_ORDER_NOT_EXIST);
            valid = false;
        } else if (purchaseOrderDocument.isPendingActionIndicator()) {
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER,
                    PurapKeyConstants.ERROR_PURCHASE_PENDING_ACTION);
            valid = false;
        } else if (!StringUtils.equals(purchaseOrderDocument.getApplicationDocumentStatus(),
                PurchaseOrderStatuses.APPDOC_OPEN)) {
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER,
                    PurapKeyConstants.ERROR_PURCHASE_ORDER_NOT_OPEN);
            valid = false;
            // if the PO is pending and it is not a Retransmit, we cannot generate a Payment Request for it
        }

        return valid;
    }

    @Override
    public boolean encumberedItemExistsForInvoicing(PurchaseOrderDocument document) {
        boolean zeroDollar = true;
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);
        for (PurchaseOrderItem poi : (List<PurchaseOrderItem>) document.getItems()) {
            // Quantity-based items
            if (poi.getItemType().isLineItemIndicator() && poi.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                KualiDecimal encumberedQuantity = poi.getItemOutstandingEncumberedQuantity() == null ?
                        KualiDecimal.ZERO : poi.getItemOutstandingEncumberedQuantity();
                if (encumberedQuantity.compareTo(KualiDecimal.ZERO) > 0) {
                    zeroDollar = false;
                    break;
                }
            } else if (poi.getItemType().isAmountBasedGeneralLedgerIndicator()
                    || poi.getItemType().isAdditionalChargeIndicator()) {
                // Service Items or Below-the-line Items
                KualiDecimal encumberedAmount = poi.getItemOutstandingEncumberedAmount() == null ? KualiDecimal.ZERO :
                        poi.getItemOutstandingEncumberedAmount();
                if (encumberedAmount.compareTo(KualiDecimal.ZERO) > 0) {
                    zeroDollar = false;
                    break;
                }
            }
        }

        return !zeroDollar;
    }

    public void setAccountsPayableService(AccountsPayableService accountsPayableService) {
        this.accountsPayableService = accountsPayableService;
    }

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setDisbursementVoucherValidationService(
            DisbursementVoucherValidationService disbursementVoucherValidationService) {
        this.disbursementVoucherValidationService = disbursementVoucherValidationService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setKualiRuleService(KualiRuleService kualiRuleService) {
        this.kualiRuleService = kualiRuleService;
    }

    public void setNegativePaymentRequestApprovalLimitService(
            NegativePaymentRequestApprovalLimitService negativePaymentRequestApprovalLimitService) {
        this.negativePaymentRequestApprovalLimitService = negativePaymentRequestApprovalLimitService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPaymentRequestDao(PaymentRequestDao paymentRequestDao) {
        this.paymentRequestDao = paymentRequestDao;
    }

    public void setPurapAccountingService(PurapAccountingService purapAccountingService) {
        this.purapAccountingService = purapAccountingService;
    }

    public void setPurapService(PurapService purapService) {
        this.purapService = purapService;
    }

    public void setPurapWorkflowIntegrationService(PurApWorkflowIntegrationService purapWorkflowIntegrationService) {
        this.purapWorkflowIntegrationService = purapWorkflowIntegrationService;
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    private enum DuplicatePaymentRequestMessages {
        DUPLICATE_INVOICE_DATE_AMOUNT(
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE_DATE_AMOUNT,
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE_DATE_AMOUNT_CANCELLEDORVOIDED,
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE_DATE_AMOUNT_CANCELLED,
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE_DATE_AMOUNT_VOIDED),
        DUPLICATE_INVOICE_VENDOR_INVOICE_NUMBER(
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE,
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE_CANCELLEDORVOIDED,
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE_CANCELLED,
            PurapKeyConstants.MESSAGE_DUPLICATE_INVOICE_VOIDED);

        final String duplicateInvoiceKey;
        final String duplicateInvoiceCanceledOrVoidedKey;
        final String duplicateInvoiceCancelledKey;
        final String duplicateInvoiceVoidedKey;

        DuplicatePaymentRequestMessages(String duplicateInvoiceKey, String duplicateInvoiceCanceledOrVoidedKey,
                String duplicateInvoiceCancelledKey, String duplicateInvoiceVoidedKey) {
            this.duplicateInvoiceKey = duplicateInvoiceKey;
            this.duplicateInvoiceCanceledOrVoidedKey = duplicateInvoiceCanceledOrVoidedKey;
            this.duplicateInvoiceCancelledKey = duplicateInvoiceCancelledKey;
            this.duplicateInvoiceVoidedKey = duplicateInvoiceVoidedKey;
        }
    }
}
