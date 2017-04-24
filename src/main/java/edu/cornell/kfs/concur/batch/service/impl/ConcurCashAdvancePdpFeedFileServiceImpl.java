package edu.cornell.kfs.concur.batch.service.impl;
import org.apache.commons.collections.CollectionUtils;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurCashAdvancePdpFeedFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;

public class ConcurCashAdvancePdpFeedFileServiceImpl implements ConcurCashAdvancePdpFeedFileService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurCashAdvancePdpFeedFileServiceImpl.class);
    protected String paymentImportDirectory;
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Transactional
    public boolean createPdpFeedFileForValidatedDetailFileLines(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData) {
        boolean pdpFileSuccessfullyCreated = false;
        PdpFeedFileBaseEntry pdpFeedFileDataObject = buildPdpFeedBaseEntry(requestExtractFile, reportData);
        if (pdpFeedFileDataObject.getTrailer().getDetailCount().intValue() != 0) {
            String fullyQualifiedPdpFileName = getConcurBatchUtilityService().buildFullyQualifiedPdpOutputFileName(getPaymentImportDirectory(), requestExtractFile.getFileName());
            pdpFileSuccessfullyCreated = getConcurBatchUtilityService().createPdpFeedFile(pdpFeedFileDataObject, fullyQualifiedPdpFileName);
            if (pdpFileSuccessfullyCreated) {
                LOG.info("createPdpFeedFileForValidatedDetailFileLines: fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "]  was created for requestExtractFile [" + requestExtractFile.getFileName() + "]");
                requestExtractFile.setFullyQualifiedPdpFileName(fullyQualifiedPdpFileName);
            }
            else {
                LOG.error("createPdpFeedFileForValidatedDetailFileLines: FAILED TO CREATE: fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "] for requestExtractFile [" + requestExtractFile.getFileName() + "]");
            }
        }
        return pdpFileSuccessfullyCreated;
    }

    private PdpFeedFileBaseEntry buildPdpFeedBaseEntry(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData) {
        int totalPdpDetailRecordsCount = 0;
        KualiDecimal totalPdpDetailRecordsAmount = KualiDecimal.ZERO;
        int totalClonedCashAdvanceLinesCount = 0;
        KualiDecimal totalClonedCashAdvanceLinesAmount = KualiDecimal.ZERO;
        int totalCashAdvancesUseInExpenseReportCount = 0;
        KualiDecimal totalCashAdvancesUseInExpenseReportAmount= KualiDecimal.ZERO;
        int totalDuplicateCashAdvanceCount = 0;
        KualiDecimal totalDuplicateCashAdvanceAmount = KualiDecimal.ZERO;
        int totalNonCashAdvanceRecordsCount = 0;
        KualiDecimal totalNonCashAdvanceRecordsAmount = KualiDecimal.ZERO;
        int totalCashAdvancesNotProcessedDueToValidationErrorsCount = 0;
        KualiDecimal totalCashAdvancesNotProcessedDueToValidationErrorsAmount = KualiDecimal.ZERO;
        int totalProcessingAttemptedLineCount = 0;
        KualiDecimal totalProcessingAttemptedLineAmount = KualiDecimal.ZERO;

        PdpFeedFileBaseEntry pdpBaseEntry = new PdpFeedFileBaseEntry();
        pdpBaseEntry.setHeader(buildPdpFeedHeaderEntry(requestExtractFile.getBatchDate()));
        List<PdpFeedGroupEntry> groupEntries = new ArrayList<PdpFeedGroupEntry>();

        for (ConcurRequestExtractRequestDetailFileLine detailFileLine : requestExtractFile.getRequestDetails()) {
            if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isValidCashAdvanceLine()) {
                PdpFeedDetailEntry pdpDetailEntry = buildPdpFeedDetailEntry(detailFileLine, buildPdpFeedAccountingEntry(detailFileLine));
                List<PdpFeedDetailEntry> pdpDetailEntries = new ArrayList<PdpFeedDetailEntry>();
                pdpDetailEntries.add(pdpDetailEntry);
                groupEntries.add(buildPdpFeedGroupEntry(detailFileLine, buildPdpFeedPayeeIdEntry(detailFileLine), pdpDetailEntries));
                recordCashAdvanceGenerationInDuplicateTrackingTable(detailFileLine, pdpDetailEntry.getSourceDocNbr(), requestExtractFile.getFileName());
                totalPdpDetailRecordsCount++;
                totalPdpDetailRecordsAmount = totalPdpDetailRecordsAmount.add(detailFileLine.getRequestAmount());
            }
            else if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isClonedCashAdvance()) {
                totalClonedCashAdvanceLinesCount++;
                totalClonedCashAdvanceLinesAmount = totalClonedCashAdvanceLinesAmount.add(detailFileLine.getRequestAmount());
            }
            else if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isCashAdvanceUsedInExpenseReport()) {
                totalCashAdvancesUseInExpenseReportCount++;
                totalCashAdvancesUseInExpenseReportAmount = totalCashAdvancesUseInExpenseReportAmount.add(detailFileLine.getRequestAmount());
            }
            else if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isDuplicatedCashAdvanceLine()) {
                totalDuplicateCashAdvanceCount++;
                totalDuplicateCashAdvanceAmount = totalDuplicateCashAdvanceAmount.add(detailFileLine.getRequestAmount());
                updateReportDataWithFileLineValidationError(reportData, detailFileLine);
            }
            else if (detailFileLine.getValidationResult().isNotCashAdvanceLine()) {
                totalNonCashAdvanceRecordsCount++;
                totalNonCashAdvanceRecordsAmount = totalNonCashAdvanceRecordsAmount.add(detailFileLine.getRequestAmount());
            }
            else if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isNotValidCashAdvanceLine()) {
                totalCashAdvancesNotProcessedDueToValidationErrorsCount++;
                totalCashAdvancesNotProcessedDueToValidationErrorsAmount = totalCashAdvancesNotProcessedDueToValidationErrorsAmount.add(detailFileLine.getRequestAmount());
                updateReportDataWithFileLineValidationError(reportData, detailFileLine);
                LOG.info("buildPdpFeedBaseEntry: Cash Advance was detected but validation failed for:  " + KFSConstants.NEWLINE + detailFileLine.toString());
            }
            totalProcessingAttemptedLineCount++;
            totalProcessingAttemptedLineAmount = totalProcessingAttemptedLineAmount.add(detailFileLine.getRequestAmount());
        }
        pdpBaseEntry.setGroup(groupEntries);
        pdpBaseEntry.setTrailer(buildPdpFeedTrailerEntry(totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount));
        pdpBaseEntry.setVersion(ConcurConstants.FEED_FILE_ENTRY_HEADER_VERSION);

        updateReportDataWithProcessingTotals(reportData, totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount,
                totalClonedCashAdvanceLinesCount, totalClonedCashAdvanceLinesAmount,
                totalCashAdvancesUseInExpenseReportCount, totalCashAdvancesUseInExpenseReportAmount,
                totalDuplicateCashAdvanceCount, totalDuplicateCashAdvanceAmount,
                totalNonCashAdvanceRecordsCount, totalNonCashAdvanceRecordsAmount,
                totalCashAdvancesNotProcessedDueToValidationErrorsCount, totalCashAdvancesNotProcessedDueToValidationErrorsAmount,
                totalProcessingAttemptedLineCount, totalProcessingAttemptedLineAmount);
        return pdpBaseEntry;
    }

    private void recordCashAdvanceGenerationInDuplicateTrackingTable(ConcurRequestExtractRequestDetailFileLine detailFileLine, String sourceDocumentNumber, String requestExtractFileName) {
        ConcurRequestedCashAdvance duplicateTrackingCashAdvance =
                new ConcurRequestedCashAdvance(detailFileLine.getRequestId(),
                                               detailFileLine.getEmployeeId(),
                                               detailFileLine.getRequestAmount(),
                                               detailFileLine.getBatchDate(),
                                               sourceDocumentNumber,
                                               detailFileLine.getCashAdvanceKey(),
                                               detailFileLine.getChart(),
                                               detailFileLine.getAccountNumber(),
                                               StringUtils.defaultIfBlank(detailFileLine.getSubAccountNumber(), KFSConstants.EMPTY_STRING),
                                               getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE),
                                               StringUtils.defaultIfBlank(detailFileLine.getSubObjectCode(), KFSConstants.EMPTY_STRING),
                                               StringUtils.defaultIfBlank(detailFileLine.getProjectCode(), KFSConstants.EMPTY_STRING),
                                               StringUtils.defaultIfBlank(detailFileLine.getOrgRefId(), KFSConstants.EMPTY_STRING),
                                               requestExtractFileName);

        getConcurRequestedCashAdvanceService().saveConcurRequestedCashAdvance(duplicateTrackingCashAdvance);
    }

    private PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine, PdpFeedPayeeIdEntry pdpPayeeIdEntry, List<PdpFeedDetailEntry> pdpDetailEntries) {
        PdpFeedGroupEntry pdpGroupEntry = new PdpFeedGroupEntry();
        pdpGroupEntry.setPayeeName(getConcurBatchUtilityService().formatPdpPayeeName(detailFileLine.getLastName(), detailFileLine.getFirstName(), detailFileLine.getMiddleInitial()));
        pdpGroupEntry.setPayeeId(pdpPayeeIdEntry);
        pdpGroupEntry.setCustomerInstitutionIdentifier(StringUtils.EMPTY);
        pdpGroupEntry.setPaymentDate(getConcurBatchUtilityService().formatDate_MMddyyyy(detailFileLine.getBatchDate()));
        pdpGroupEntry.setCombineGroupInd(ConcurConstants.COMBINED_GROUP_INDICATOR);
        pdpGroupEntry.setBankCode(ConcurConstants.BANK_CODE);
        pdpGroupEntry.setDetail(pdpDetailEntries);
        return pdpGroupEntry;
    }

    private PdpFeedDetailEntry buildPdpFeedDetailEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine, PdpFeedAccountingEntry pdpAccountingEntry) {
        PdpFeedDetailEntry pdpDetailEntry  = new PdpFeedDetailEntry();
        pdpDetailEntry.setSourceDocNbr(getConcurBatchUtilityService().formatSourceDocumentNumber(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_REQUEST_EXTRACT_PDP_DOCUMENT_TYPE), detailFileLine.getRequestId()));
        pdpDetailEntry.setInvoiceNbr(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_PDP_DEFAULT_INVOICE_NUMBER));
        pdpDetailEntry.setPoNbr(StringUtils.EMPTY);
        pdpDetailEntry.setInvoiceDate(getConcurBatchUtilityService().formatDate_MMddyyyy(detailFileLine.getBatchDate()));
        pdpDetailEntry.setNetPaymentAmt(detailFileLine.getRequestAmount());
        pdpDetailEntry.setFsOriginCd(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_AP_PDP_ORIGINATION_CODE));
        pdpDetailEntry.setFdocTypCd(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_REQUEST_EXTRACT_PDP_DOCUMENT_TYPE));
        List<String> paymentTexts = new ArrayList<String>();
        paymentTexts.add(detailFileLine.getRequestEntryDescription());
        pdpDetailEntry.setPaymentText(paymentTexts);
        List<PdpFeedAccountingEntry> accountingLines = new ArrayList<PdpFeedAccountingEntry>();
        accountingLines.add(pdpAccountingEntry);
        pdpDetailEntry.setAccounting(accountingLines);
        return pdpDetailEntry;
    }

    private PdpFeedAccountingEntry buildPdpFeedAccountingEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        PdpFeedAccountingEntry pdpAccountingEntry =  new PdpFeedAccountingEntry();
        pdpAccountingEntry.setCoaCd(detailFileLine.getChart());
        pdpAccountingEntry.setAccountNbr(detailFileLine.getAccountNumber());
        pdpAccountingEntry.setSubAccountNbr(detailFileLine.getSubAccountNumber());
        pdpAccountingEntry.setObjectCd(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE));
        pdpAccountingEntry.setSubObjectCd(detailFileLine.getSubObjectCode());
        pdpAccountingEntry.setOrgRefId(detailFileLine.getOrgRefId());
        pdpAccountingEntry.setProjectCd(detailFileLine.getProjectCode());
        pdpAccountingEntry.setAmount(detailFileLine.getRequestAmount());
        return pdpAccountingEntry;
    }

    private PdpFeedPayeeIdEntry buildPdpFeedPayeeIdEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        PdpFeedPayeeIdEntry payeeIdEntry = new PdpFeedPayeeIdEntry();
        payeeIdEntry.setContent(detailFileLine.getEmployeeId());
        if (StringUtils.equalsIgnoreCase(detailFileLine.getPayeeIdType(), ConcurConstants.EMPLOYEE_STATUS_CODE)) {
            payeeIdEntry.setIdType(ConcurConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else if (StringUtils.equalsIgnoreCase(detailFileLine.getPayeeIdType(), ConcurConstants.NON_EMPLOYEE_STATUS_CODE)) {
            payeeIdEntry.setIdType(ConcurConstants.NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else {
            LOG.error("buildPdpFeedPayeeIdEntry: Invalid PayeeIdType detected in buildPdpFeedPayeeIdEntry AFTER validation while building PDP output file:" + detailFileLine.getPayeeIdType());
        }
        return payeeIdEntry;
    }

    private PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
        header.setChart(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_LOCATION));
        header.setCreationDate(getConcurBatchUtilityService().formatDate_MMddyyyy(batchDate));
        header.setSubUnit(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_SUB_UNIT));
        header.setUnit(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_UNIT));
        return header;
    }

    private PdpFeedTrailerEntry buildPdpFeedTrailerEntry(int totalDetailRecordCount, KualiDecimal totalDetailRecordAmount) {
        PdpFeedTrailerEntry trailer = new PdpFeedTrailerEntry();
        trailer.setDetailCount(totalDetailRecordCount);
        trailer.setDetailTotAmt(totalDetailRecordAmount);
        return trailer;
    }

    private void updateReportDataWithProcessingTotals(ConcurRequestExtractBatchReportData reportData,
            int totalPdpDetailRecordsCount, KualiDecimal totalPdpDetailRecordsAmount,
            int totalClonedCashAdvanceLinesCount, KualiDecimal totalClonedCashAdvanceLinesAmount,
            int totalCashAdvancesUseInExpenseReportCount, KualiDecimal totalCashAdvancesUseInExpenseReportAmount,
            int totalDuplicateCashAdvanceCount, KualiDecimal totalDuplicateCashAdvanceAmount,
            int totalNonCashAdvanceRecordsCount, KualiDecimal totalNonCashAdvanceRecordsAmount,
            int totalCashAdvancesNotProcessedDueToValidationErrorsCount, KualiDecimal totalCashAdvancesNotProcessedDueToValidationErrorsAmount,
            int totalProcessingAttemptedLineCount, KualiDecimal totalProcessingAttemptedLineAmount) {
        updateReportDataWithPdpFeedFileTotals(reportData, totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount);
        updateReportDataWithClonedCashAdvanceTotals(reportData, totalClonedCashAdvanceLinesCount, totalClonedCashAdvanceLinesAmount);
        updateReportDataWithCashAdvancesUsedInExpenseReportTotals(reportData, totalCashAdvancesUseInExpenseReportCount, totalCashAdvancesUseInExpenseReportAmount);
        updateReportDataWithDuplicateCashAdvanceTotals(reportData, totalDuplicateCashAdvanceCount, totalDuplicateCashAdvanceAmount);
        updateReportDataWithTravelRequestsOnlyTotals(reportData, totalNonCashAdvanceRecordsCount, totalNonCashAdvanceRecordsAmount);
        updateReportDataWithValidationErrorTotals(reportData, totalCashAdvancesNotProcessedDueToValidationErrorsCount, totalCashAdvancesNotProcessedDueToValidationErrorsAmount);
        updateReportDataWithRequestExtractFileTotals(reportData, totalProcessingAttemptedLineCount, totalProcessingAttemptedLineAmount);
    }

    private void updateReportDataWithPdpFeedFileTotals(ConcurRequestExtractBatchReportData reportData, int totalPdpDetailRecordsCount, KualiDecimal totalPdpDetailRecordsAmount) {
        reportData.getCashAdvancesProcessedInPdp().setRecordCount(totalPdpDetailRecordsCount);
        reportData.getCashAdvancesProcessedInPdp().setDollarAmount(totalPdpDetailRecordsAmount);
    }

    private void updateReportDataWithClonedCashAdvanceTotals(ConcurRequestExtractBatchReportData reportData, int totalClonedCashAdvanceLinesCount, KualiDecimal totalClonedCashAdvanceLinesAmount) {
        reportData.getClonedCashAdvanceRequests().setRecordCount(totalClonedCashAdvanceLinesCount);
        reportData.getClonedCashAdvanceRequests().setDollarAmount(totalClonedCashAdvanceLinesAmount);
    }

    private void updateReportDataWithCashAdvancesUsedInExpenseReportTotals(ConcurRequestExtractBatchReportData reportData, int totalCashAdvancesUseInExpenseReportCount, KualiDecimal totalCashAdvancesUseInExpenseReportAmount) {
        reportData.getCashAdvancesBypassedRelatedToExpenseReport().setRecordCount(totalCashAdvancesUseInExpenseReportCount);
        reportData.getCashAdvancesBypassedRelatedToExpenseReport().setDollarAmount(totalCashAdvancesUseInExpenseReportAmount);
    }

    private void updateReportDataWithDuplicateCashAdvanceTotals(ConcurRequestExtractBatchReportData reportData, int totalDuplicateCashAdvanceCount, KualiDecimal totalDuplicateCashAdvanceAmount) {
        reportData.getDuplicateCashAdvanceRequests().setRecordCount(totalDuplicateCashAdvanceCount);
        reportData.getDuplicateCashAdvanceRequests().setDollarAmount(totalDuplicateCashAdvanceAmount);
    }

    private void updateReportDataWithTravelRequestsOnlyTotals(ConcurRequestExtractBatchReportData reportData, int totalNonCashAdvanceRecordsCount, KualiDecimal totalNonCashAdvanceRecordsAmount) {
        reportData.getRecordsBypassedTravelRequestOnly().setRecordCount(totalNonCashAdvanceRecordsCount);
        reportData.getRecordsBypassedTravelRequestOnly().setDollarAmount(totalNonCashAdvanceRecordsAmount);
    }

    private void updateReportDataWithValidationErrorTotals(ConcurRequestExtractBatchReportData reportData, int totalCashAdvancesNotProcessedDueToValidationErrorsCount, KualiDecimal totalCashAdvancesNotProcessedDueToValidationErrorsAmount) {
        reportData.getCashAdvancesNotProcessedValidationErrors().setRecordCount(totalCashAdvancesNotProcessedDueToValidationErrorsCount);
        reportData.getCashAdvancesNotProcessedValidationErrors().setDollarAmount(totalCashAdvancesNotProcessedDueToValidationErrorsAmount);
    }

    private void updateReportDataWithRequestExtractFileTotals(ConcurRequestExtractBatchReportData reportData, int totalProcessingAttemptedLineCount, KualiDecimal totalProcessingAttemptedLineAmount) {
        reportData.getTotalsForFile().setRecordCount(totalProcessingAttemptedLineCount);
        reportData.getTotalsForFile().setDollarAmount(totalProcessingAttemptedLineAmount);
    }

    private void updateReportDataWithFileLineValidationError(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        ConcurBatchReportLineValidationErrorItem errorDetails =
                new ConcurBatchReportLineValidationErrorItem(detailFileLine.getRequestId(), detailFileLine.getEmployeeId(), detailFileLine.getLastName(), detailFileLine.getFirstName(), detailFileLine.getMiddleInitial(), detailFileLine.getValidationResult().getMessages());
        reportData.getValidationErrorFileLines().add(errorDetails);
    }

   public void createDoneFileForPdpFile(String concurCashAdvancePdpFeedFileName) {
       getConcurBatchUtilityService().createDoneFileFor(concurCashAdvancePdpFeedFileName);
   }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public void setPaymentImportDirectory(String paymentImportDirectory) {
        this.paymentImportDirectory = paymentImportDirectory;
    }

    public String getPaymentImportDirectory() {
        return this.paymentImportDirectory;
    }

    public ConcurRequestedCashAdvanceService getConcurRequestedCashAdvanceService() {
        return concurRequestedCashAdvanceService;
    }

    public void setConcurRequestedCashAdvanceService(ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService) {
        this.concurRequestedCashAdvanceService = concurRequestedCashAdvanceService;
    }

}
