/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.fp.batch.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.report.ReportInfo;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.core.web.format.CurrencyFormatter;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;

import com.rsmart.kuali.kfs.fp.FPConstants;
import com.rsmart.kuali.kfs.fp.FPKeyConstants;
import com.rsmart.kuali.kfs.fp.FPPropertyConstants;
import com.rsmart.kuali.kfs.fp.batch.service.DisbursementVoucherDocumentBatchService;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatch;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchDefault;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchFeed;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchStatus;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchSummaryLine;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherDocumentExtension;
import com.rsmart.kuali.kfs.fp.document.BatchDisbursementVoucherDocument;
import com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService;

/**
 * @see com.rsmart.kuali.kfs.fp.batch.service.DisbursementVoucherDocumentBatchService
 */
public class DisbursementVoucherDocumentBatchServiceImpl implements DisbursementVoucherDocumentBatchService {
	private static final Logger LOG = LogManager.getLogger(DisbursementVoucherDocumentBatchServiceImpl.class);

    private String attachmentsPath;

    private BatchInputFileService batchInputFileService;
    private BatchInputFileType disbursementVoucherInputFileType;
    private BusinessObjectService businessObjectService;
    private DocumentService documentService;
    private DataDictionaryService dataDictionaryService;
    private SequenceAccessorService sequenceAccessorService;
    private DateTimeService dateTimeService;
    private PersonService personService;
    private ConfigurationService kualiConfigurationService;
    private UniversityDateService universityDateService;
    private PersistenceService persistenceService;
    private ReportInfo disbursementVoucherBatchReportInfo;
    private VendorService vendorService;
    private BatchFeedHelperService batchFeedHelperService;

    /**
     * @see com.rsmart.kuali.kfs.fp.batch.service.DisbursementVoucherDocumentBatchService#processDisbursementVoucherFiles()
     */
    public void processDisbursementVoucherFiles() {
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(disbursementVoucherInputFileType);

        for (String incomingFileName : fileNamesToLoad) {
            try {
                LOG.debug("processDisbursementVoucherFiles  () Processing " + incomingFileName);

                // batchStatus holds audit report fields
                DisbursementVoucherBatchStatus batchStatus = new DisbursementVoucherBatchStatus();
                DisbursementVoucherBatchFeed batchFeed = (DisbursementVoucherBatchFeed) batchFeedHelperService.parseBatchFile(disbursementVoucherInputFileType, incomingFileName, batchStatus);

                if (batchFeed != null && !batchFeed.getBatchDisbursementVoucherDocuments().isEmpty() && StringUtils.isBlank(batchStatus.getXmlParseExceptionMessage())) {
                    loadDisbursementVouchers(batchFeed, batchStatus, incomingFileName, GlobalVariables.getMessageMap());
                }

                generateAuditReport(batchStatus);
            }
            catch (RuntimeException e) {
                LOG.error("Caught exception trying to load disbursement voucher file: " + incomingFileName, e);
                throw new RuntimeException("Caught exception trying to load disbursement voucher file: " + incomingFileName, e);
            }
        }
    }

    /**
     * @see com.rsmart.kuali.kfs.fp.batch.service.DisbursementVoucherDocumentBatchService#loadDisbursementVouchers(com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchFeed,
     *      com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchStatus, java.lang.String,
     *      org.kuali.kfs.kns.util.MessageMap)
     */
    public void loadDisbursementVouchers(DisbursementVoucherBatchFeed batchFeed, DisbursementVoucherBatchStatus batchStatus, String incomingFileName, MessageMap MessageMap) {
        // get new batch record for the load
        DisbursementVoucherBatch disbursementVoucherBatch = getNewDisbursementVoucherBatch();
        businessObjectService.save(disbursementVoucherBatch);

        batchStatus.setUnitCode(batchFeed.getUnitCode());

        boolean batchHasErrors = false;
        for (BatchDisbursementVoucherDocument batchDisbursementVoucherDocument : batchFeed.getBatchDisbursementVoucherDocuments()) {
            batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_DV_RECORDS_READ, 1);
            batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_ACCOUNTING_RECORDS_READ, batchDisbursementVoucherDocument.getSourceAccountingLines().size());

            // get defaults for DV chart/org
            DisbursementVoucherBatchDefault batchDefault = null;
            if (StringUtils.isNotBlank(batchFeed.getUnitCode())) {
                batchDefault = getDisbursementVoucherBatchDefault(batchFeed.getUnitCode());
            }

            MessageMap documentMessageMap = new MessageMap();
            batchFeedHelperService.performForceUppercase(DisbursementVoucherDocument.class.getName(), batchDisbursementVoucherDocument);

            // create and route doc as system user
            // create and route doc as system user
            UserSession actualUserSession = GlobalVariables.getUserSession();
            GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
            MessageMap globalMessageMap = GlobalVariables.getMessageMap();
            GlobalVariables.setMessageMap(documentMessageMap);

            DisbursementVoucherDocument disbursementVoucherDocument = null;
            try {
                disbursementVoucherDocument = populateDisbursementVoucherDocument(disbursementVoucherBatch, batchDisbursementVoucherDocument, batchDefault, documentMessageMap);

                // if the document is valid create GLPEs, Save and Approve
                if (documentMessageMap.hasNoErrors()) {
                    businessObjectService.save(disbursementVoucherDocument.getExtension());
                    documentService.routeDocument(disbursementVoucherDocument, "", null);

                    if (documentMessageMap.hasNoErrors()) {
                        batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_DV_RECORDS_WRITTEN, 1);
                        batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_ACCOUNTING_RECORDS_WRITTEN, disbursementVoucherDocument.getSourceAccountingLines().size());
                        batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_GLPE_RECORDS_WRITTEN, disbursementVoucherDocument.getGeneralLedgerPendingEntries().size());

                        batchStatus.getBatchDisbursementVoucherDocuments().add(disbursementVoucherDocument);
                    }
                }
            }
            catch (ValidationException e1) {
                // will be reported in audit report
            }
            finally {
                GlobalVariables.setUserSession(actualUserSession);
                GlobalVariables.setMessageMap(globalMessageMap);
            }

            if (documentMessageMap.hasErrors()) {
                batchHasErrors = true;
            }

            // populate summary line and add to report
            DisbursementVoucherBatchSummaryLine batchSummaryLine = populateBatchSummaryLine(disbursementVoucherDocument, documentMessageMap);
            batchStatus.getBatchSummaryLines().add(batchSummaryLine);
        }

        // indicate in global map there were errors (for batch upload screen)
        if (batchHasErrors) {
            MessageMap.putError(KFSConstants.GLOBAL_ERRORS, FPKeyConstants.ERROR_BATCH_DISBURSEMENT_VOUCHER_ERRORS_NOTIFICATION);
        }

        batchFeedHelperService.removeDoneFile(incomingFileName);
    }

    /**
     * @see com.rsmart.kuali.kfs.fp.batch.service.DisbursementVoucherDocumentBatchService#generateAuditReport(com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchStatus)
     */
    public void generateAuditReport(DisbursementVoucherBatchStatus batchStatus) {
        batchFeedHelperService.generateAuditReport(disbursementVoucherBatchReportInfo, batchStatus);
    }

    /**
     * Creates and populates a batch summary line for the disbursement voucher document
     * 
     * @param disbursementVoucherDocument batch disbursement voucher document
     * @param MessageMap MessageMap containing errors encountered while populating document
     * @return PurchaseOrderBatchSummaryLine
     */
    protected DisbursementVoucherBatchSummaryLine populateBatchSummaryLine(DisbursementVoucherDocument disbursementVoucherDocument, MessageMap MessageMap) {
        DisbursementVoucherBatchSummaryLine batchSummaryLine = new DisbursementVoucherBatchSummaryLine();
        DisbursementVoucherPayeeDetail dvPayeeDetail = disbursementVoucherDocument.getDvPayeeDetail();
        //dvPayeeDetail.refresh();

        Date currentDate = dateTimeService.getCurrentDate();
        batchSummaryLine.setDisbVchrCreateDate(dateTimeService.toDateString(currentDate));

        batchSummaryLine.setDisbVchrPayeeId(dvPayeeDetail.getDisbVchrPayeeIdNumber() + " (" + dvPayeeDetail.getDisbVchrPayeePersonName() + " )");

        if (disbursementVoucherDocument.getDisbVchrCheckTotalAmount() != null) {
            String totalDollarAmount = (String) (new CurrencyFormatter()).formatForPresentation(disbursementVoucherDocument.getDisbVchrCheckTotalAmount());
            batchSummaryLine.setDisbVchrAmount(totalDollarAmount);
        }

        if (disbursementVoucherDocument.getDisbursementVoucherDueDate() != null) {
            batchSummaryLine.setDisbursementVoucherDueDate(disbursementVoucherDocument.getDisbursementVoucherDueDate().toString());
        }

        if (ObjectUtils.isNotNull(dvPayeeDetail.getDisbVchrPaymentReason())) {
            String paymentReasonDescription = dvPayeeDetail.getDisbVchrPaymentReason().getDescription();
            if (paymentReasonDescription.length() >= 50) {
                paymentReasonDescription = paymentReasonDescription.substring(0, 49);
            }
            batchSummaryLine.setDisbVchrPaymentReason(dvPayeeDetail.getDisbVchrPaymentReasonCode() + "-" + paymentReasonDescription);
        }

        batchSummaryLine.setAuditMessage(batchFeedHelperService.getAuditMessage(FPKeyConstants.MESSAGE_AUDIT_DV_SUCCESSFULLY_GENERATED, disbursementVoucherDocument.getDocumentNumber(), MessageMap));

        return batchSummaryLine;
    }

    /**
     * Creates and populates a new DisbursementVoucherBatch instance
     * 
     * @return DisbursementVoucherBatch
     */
    protected DisbursementVoucherBatch getNewDisbursementVoucherBatch() {
        DisbursementVoucherBatch disbursementVoucherBatch = new DisbursementVoucherBatch();

        // get next available batch id
        KualiInteger batchId = new KualiInteger(sequenceAccessorService.getNextAvailableSequenceNumber(FPConstants.DV_BATCH_ID_SEQUENCE_NAME));
        disbursementVoucherBatch.setBatchId(batchId);

        disbursementVoucherBatch.setProcessPrincipalId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        disbursementVoucherBatch.setProcessTimestamp(dateTimeService.getCurrentTimestamp());

        return disbursementVoucherBatch;
    }

    /**
     * Retrieves the setup batch default record for the unit code
     * 
     * @param unitCode unit code on batch default record to retrieve
     * @return DisbursementVoucherBatchDefault containing default values for DV
     */
    protected DisbursementVoucherBatchDefault getDisbursementVoucherBatchDefault(String unitCode) {
        Map<String, String> pkValues = new HashMap<String, String>();
        pkValues.put(FPPropertyConstants.UNIT_CODE, unitCode);

        BusinessObject batchDefaults = businessObjectService.findByPrimaryKey(DisbursementVoucherBatchDefault.class, pkValues);

        return (DisbursementVoucherBatchDefault) batchDefaults;
    }

    /**
     * Creates a new DV document and populates from the batch instance
     * 
     * @param disbursementVoucherBatch
     * @param batchDisbursementVoucherDocument batch dv document to pull values from
     * @param batchDefault contains default values to use if value in feed is empty
     * @param MessageMap MessageMap for adding encountered errors
     * @return DisbursementVoucherDocument created and populated DV document
     */
    protected DisbursementVoucherDocument populateDisbursementVoucherDocument(DisbursementVoucherBatch disbursementVoucherBatch, BatchDisbursementVoucherDocument batchDisbursementVoucherDocument, DisbursementVoucherBatchDefault batchDefault, MessageMap MessageMap) {
        DisbursementVoucherDocument disbursementVoucherDocument = null;
        disbursementVoucherDocument = (DisbursementVoucherDocument) documentService.getNewDocument(DisbursementVoucherDocument.class);

        // populate extension with batch id
        DisbursementVoucherDocumentExtension disbursementVoucherDocumentExtension = new DisbursementVoucherDocumentExtension();
        disbursementVoucherDocumentExtension.setDocumentNumber(disbursementVoucherDocument.getDocumentNumber());
        disbursementVoucherDocumentExtension.setBatchId(disbursementVoucherBatch.getBatchId());
        disbursementVoucherDocument.setExtension(disbursementVoucherDocumentExtension);

        populateDisbursementVoucherFields(disbursementVoucherDocument, batchDisbursementVoucherDocument, batchDefault, MessageMap);
        batchFeedHelperService.loadDocumentAttachments(disbursementVoucherDocument, batchDisbursementVoucherDocument.getAttachments(), attachmentsPath, "", MessageMap);

        return disbursementVoucherDocument;
    }

    /**
     * Populates fields on the disbursement voucher document and performs validation
     * 
     * @param disbursementVoucherDocument disbursement voucher document to populate
     * @param batchDisbursementVoucherDocument batch dv document to pull values from
     * @param batchDefault contains default values to use if value in feed is empty
     * @param MessageMap MessageMap for adding encountered errors
     */
    protected void populateDisbursementVoucherFields(DisbursementVoucherDocument disbursementVoucherDocument, BatchDisbursementVoucherDocument batchDisbursementVoucherDocument, DisbursementVoucherBatchDefault batchDefault, MessageMap MessageMap) {
        disbursementVoucherDocument.getDocumentHeader().setDocumentDescription(kualiConfigurationService.getPropertyValueAsString(FPKeyConstants.MESSAGE_DV_BATCH_DOCUMENT_DESCRIPTION));

        // populate fields of document from batch instance
        disbursementVoucherDocument.getDocumentHeader().setExplanation(batchDisbursementVoucherDocument.getDocumentHeader().getExplanation());
        disbursementVoucherDocument.getDocumentHeader().setOrganizationDocumentNumber(batchDisbursementVoucherDocument.getDocumentHeader().getOrganizationDocumentNumber());
        disbursementVoucherDocument.setDisbVchrContactPersonName(batchDisbursementVoucherDocument.getDisbVchrContactPersonName());
        disbursementVoucherDocument.setDisbVchrContactPhoneNumber(batchDisbursementVoucherDocument.getDisbVchrContactPhoneNumber());
        disbursementVoucherDocument.setDisbVchrContactEmailId(batchDisbursementVoucherDocument.getDisbVchrContactEmailId());
        disbursementVoucherDocument.setDisbursementVoucherDueDate(batchDisbursementVoucherDocument.getDisbursementVoucherDueDate());
        disbursementVoucherDocument.setDisbVchrAttachmentCode(batchDisbursementVoucherDocument.isDisbVchrAttachmentCode());
        disbursementVoucherDocument.setDisbVchrSpecialHandlingCode(batchDisbursementVoucherDocument.isDisbVchrSpecialHandlingCode());
        disbursementVoucherDocument.setDisbVchrCheckTotalAmount(batchDisbursementVoucherDocument.getDisbVchrCheckTotalAmount());
        disbursementVoucherDocument.setDisbursementVoucherDocumentationLocationCode(batchDisbursementVoucherDocument.getDisbursementVoucherDocumentationLocationCode());
        disbursementVoucherDocument.setDisbVchrPaymentMethodCode(batchDisbursementVoucherDocument.getDisbVchrPaymentMethodCode());
        disbursementVoucherDocument.setCampusCode(batchDisbursementVoucherDocument.getCampusCode());
        disbursementVoucherDocument.setDisbVchrCheckStubText(batchDisbursementVoucherDocument.getDisbVchrCheckStubText());
        disbursementVoucherDocument.setDisbVchrBankCode(batchDisbursementVoucherDocument.getDisbVchrBankCode());

        disbursementVoucherDocument.getDvPayeeDetail().setDocumentNumber(disbursementVoucherDocument.getDocumentNumber());
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPaymentReasonCode(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPaymentReasonCode());

        String payeeTypeCode = batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode();
        String payeeIdNumber = batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber();
        if (KFSConstants.PaymentPayeeTypes.VENDOR.equals(payeeTypeCode)) {
            if (StringUtils.contains(payeeIdNumber, "-")) {
                VendorDetail vendorDetail = vendorService.getVendorDetail(payeeIdNumber);
                if (vendorDetail != null) {
                    VendorAddress vendorAddress = vendorService.getVendorDefaultAddress(vendorDetail.getVendorHeaderGeneratedIdentifier(), vendorDetail.getVendorDetailAssignedIdentifier(), VendorConstants.AddressTypes.PURCHASE_ORDER, disbursementVoucherDocument.getCampusCode());
                    if (vendorAddress == null) {
                        vendorAddress = vendorService.getVendorDefaultAddress(vendorDetail.getVendorHeaderGeneratedIdentifier(), vendorDetail.getVendorDetailAssignedIdentifier(), VendorConstants.AddressTypes.REMIT, disbursementVoucherDocument.getCampusCode());
                    }
                    if (vendorAddress != null) {
                        disbursementVoucherDocument.templateVendor(vendorDetail, vendorAddress);
                    }
                }
                else {
                    batchFeedHelperService.addExistenceError(KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER, payeeIdNumber, MessageMap);
                }
            }
            else {
                batchFeedHelperService.addExistenceError(KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER, payeeIdNumber, MessageMap);
            }
        }
        else if (KFSConstants.PaymentPayeeTypes.EMPLOYEE.equals(payeeTypeCode)) {
            Person person = personService.getPersonByEmployeeId(payeeIdNumber);
            if (person != null) {
                disbursementVoucherDocument.templateEmployee(person);
            }
            else {
                batchFeedHelperService.addExistenceError(KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER, payeeIdNumber, MessageMap);
            }
        }

        // override payee details with values from feed if given (not blank)
        if (StringUtils.isNotBlank(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeLine1Addr())) {
            disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeLine1Addr());
        }
        if (StringUtils.isNotBlank(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeLine2Addr())) {
            disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeLine2Addr());
        }
        if (StringUtils.isNotBlank(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeCityName())) {
            disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeCityName(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeCityName());
        }
        if (StringUtils.isNotBlank(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeStateCode())) {
            disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeStateCode(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeStateCode());
        }
        if (StringUtils.isNotBlank(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeZipCode())) {
            disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeZipCode(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeZipCode());
        }
        if (StringUtils.isNotBlank(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeCountryCode())) {
            disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeCountryCode(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeCountryCode());
        }

        // set special handling fields
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrSpecialHandlingPersonName(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrSpecialHandlingPersonName());
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrSpecialHandlingLine1Addr(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrSpecialHandlingLine1Addr());
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrSpecialHandlingLine2Addr(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrSpecialHandlingLine2Addr());
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrSpecialHandlingCityName(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrSpecialHandlingCityName());
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrSpecialHandlingZipCode(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrSpecialHandlingZipCode());
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrSpecialHandlingStateCode(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrSpecialHandlingStateCode());
        disbursementVoucherDocument.getDvPayeeDetail().setDisbVchrSpecialHandlingCountryCode(batchDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrSpecialHandlingCountryCode());

        disbursementVoucherDocument.setDvNonresidentTax(batchDisbursementVoucherDocument.getDvNonresidentTax());
        disbursementVoucherDocument.getDvNonresidentTax().setDocumentNumber(disbursementVoucherDocument.getDocumentNumber());

        /* UPGRADE-911
        disbursementVoucherDocument.setDvWireTransfer(batchDisbursementVoucherDocument.getDvWireTransfer());
        disbursementVoucherDocument.getDvWireTransfer().setDocumentNumber(disbursementVoucherDocument.getDocumentNumber());
        */
        
        // set defaults
        if (batchDefault != null) {
            if (StringUtils.isBlank(disbursementVoucherDocument.getDisbVchrContactPersonName())) {
                disbursementVoucherDocument.setDisbVchrContactPersonName(batchDefault.getDisbVchrContactPersonName());
            }
            if (StringUtils.isBlank(disbursementVoucherDocument.getDisbVchrContactPhoneNumber())) {
                disbursementVoucherDocument.setDisbVchrContactPhoneNumber(batchDefault.getDisbVchrContactPhoneNumber());
            }
            if (StringUtils.isBlank(disbursementVoucherDocument.getDisbVchrContactEmailId())) {
                disbursementVoucherDocument.setDisbVchrContactEmailId(batchDefault.getDisbVchrContactEmailId());
            }
            if (StringUtils.isBlank(disbursementVoucherDocument.getCampusCode())) {
                disbursementVoucherDocument.setCampusCode(batchDefault.getCampusCode());
            }
            if (StringUtils.isBlank(disbursementVoucherDocument.getDisbVchrPaymentMethodCode())) {
                disbursementVoucherDocument.setDisbVchrPaymentMethodCode(batchDefault.getDisbVchrPaymentMethodCode());
            }
            if (StringUtils.isBlank(disbursementVoucherDocument.getDisbursementVoucherDocumentationLocationCode())) {
                disbursementVoucherDocument.setDisbursementVoucherDocumentationLocationCode(batchDefault.getDisbursementVoucherDocumentationLocationCode());
            }
            if (StringUtils.isBlank(disbursementVoucherDocument.getDisbVchrBankCode())) {
                disbursementVoucherDocument.setDisbVchrBankCode(batchDefault.getDisbVchrBankCode());
            }
        }

        // set accounting
        for (Iterator iterator = batchDisbursementVoucherDocument.getSourceAccountingLines().iterator(); iterator.hasNext();) {
            SourceAccountingLine batchAccountingLine = (SourceAccountingLine) iterator.next();

            SourceAccountingLine accountingLine = new SourceAccountingLine();
            accountingLine.setChartOfAccountsCode(batchAccountingLine.getChartOfAccountsCode());
            accountingLine.setAccountNumber(batchAccountingLine.getAccountNumber());

            if (StringUtils.isNotBlank(batchAccountingLine.getSubAccountNumber())) {
                accountingLine.setSubAccountNumber(batchAccountingLine.getSubAccountNumber());
            }
            accountingLine.setFinancialObjectCode(batchAccountingLine.getFinancialObjectCode());
            if (StringUtils.isNotBlank(batchAccountingLine.getFinancialSubObjectCode())) {
                accountingLine.setFinancialSubObjectCode(batchAccountingLine.getFinancialSubObjectCode());
            }
            if (StringUtils.isNotBlank(batchAccountingLine.getProjectCode())) {
                accountingLine.setProjectCode(batchAccountingLine.getProjectCode());
            }

            accountingLine.setOrganizationReferenceId(batchAccountingLine.getOrganizationReferenceId());
            accountingLine.setFinancialDocumentLineDescription(batchAccountingLine.getFinancialDocumentLineDescription());
            accountingLine.setAmount(batchAccountingLine.getAmount());

            // set accounting defaults
            if (batchDefault != null) {
                if (StringUtils.isBlank(accountingLine.getChartOfAccountsCode())) {
                    accountingLine.setChartOfAccountsCode(batchDefault.getChartOfAccountsCode());
                }
                if (StringUtils.isBlank(accountingLine.getAccountNumber())) {
                    accountingLine.setAccountNumber(batchDefault.getAccountNumber());
                }
                if (StringUtils.isBlank(accountingLine.getFinancialObjectCode())) {
                    accountingLine.setFinancialObjectCode(batchDefault.getFinancialObjectCode());
                }
                if (StringUtils.isBlank(accountingLine.getFinancialDocumentLineDescription())) {
                    accountingLine.setFinancialDocumentLineDescription(batchDefault.getFinancialDocumentLineDescription());
                }
            }

            accountingLine.setPostingYear(disbursementVoucherDocument.getPostingYear());
            accountingLine.setDocumentNumber(disbursementVoucherDocument.getDocumentNumber());
            disbursementVoucherDocument.addSourceAccountingLine(accountingLine);
        }

        // set notes
        for (Iterator iterator = batchDisbursementVoucherDocument.getNotes().iterator(); iterator.hasNext();) {
            Note note = (Note) iterator.next();
            note.setRemoteObjectIdentifier(disbursementVoucherDocument.getObjectId());
            note.setAuthorUniversalIdentifier(batchFeedHelperService.getSystemUser().getPrincipalId());
            note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
            note.setNotePostedTimestampToCurrent();
            
            disbursementVoucherDocument.addNote(note); 
        }
    }

    /**
     * @return Returns the attachmentsPath.
     */
    protected String getAttachmentsPath() {
        return attachmentsPath;
    }

    /**
     * @param attachmentsPath The attachmentsPath to set.
     */
    public void setAttachmentsPath(String attachmentsPath) {
        this.attachmentsPath = attachmentsPath;
    }

    /**
     * @return Returns the batchInputFileService.
     */
    protected BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * @param batchInputFileService The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * @return Returns the disbursementVoucherInputFileType.
     */
    protected BatchInputFileType getDisbursementVoucherInputFileType() {
        return disbursementVoucherInputFileType;
    }

    /**
     * @param disbursementVoucherInputFileType The disbursementVoucherInputFileType to set.
     */
    public void setDisbursementVoucherInputFileType(BatchInputFileType disbursementVoucherInputFileType) {
        this.disbursementVoucherInputFileType = disbursementVoucherInputFileType;
    }

    /**
     * @return Returns the businessObjectService.
     */
    protected BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * @return Returns the documentService.
     */
    protected DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * @param documentService The documentService to set.
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * @return Returns the dataDictionaryService.
     */
    protected DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    /**
     * @param dataDictionaryService The dataDictionaryService to set.
     */
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    /**
     * @return Returns the sequenceAccessorService.
     */
    protected SequenceAccessorService getSequenceAccessorService() {
        return sequenceAccessorService;
    }

    /**
     * @param sequenceAccessorService The sequenceAccessorService to set.
     */
    public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService) {
        this.sequenceAccessorService = sequenceAccessorService;
    }

    /**
     * @return Returns the dateTimeService.
     */
    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * @return Returns the personService.
     */
    protected PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService The personService to set.
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return Returns the kualiConfigurationService.
     */
    protected ConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    /**
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * @return Returns the universityDateService.
     */
    protected UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    /**
     * @param universityDateService The universityDateService to set.
     */
    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    /**
     * @return Returns the persistenceService.
     */
    protected PersistenceService getPersistenceService() {
        return persistenceService;
    }

    /**
     * @param persistenceService The persistenceService to set.
     */
    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /**
     * @return Returns the disbursementVoucherBatchReportInfo.
     */
    protected ReportInfo getDisbursementVoucherBatchReportInfo() {
        return disbursementVoucherBatchReportInfo;
    }

    /**
     * @param disbursementVoucherBatchReportInfo The disbursementVoucherBatchReportInfo to set.
     */
    public void setDisbursementVoucherBatchReportInfo(ReportInfo disbursementVoucherBatchReportInfo) {
        this.disbursementVoucherBatchReportInfo = disbursementVoucherBatchReportInfo;
    }

    /**
     * @return Returns the vendorService.
     */
    protected VendorService getVendorService() {
        return vendorService;
    }

    /**
     * @param vendorService The vendorService to set.
     */
    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /**
     * @return Returns the batchFeedHelperService.
     */
    protected BatchFeedHelperService getBatchFeedHelperService() {
        return batchFeedHelperService;
    }

    /**
     * @param batchFeedHelperService The batchFeedHelperService to set.
     */
    public void setBatchFeedHelperService(BatchFeedHelperService batchFeedHelperService) {
        this.batchFeedHelperService = batchFeedHelperService;
    }

}