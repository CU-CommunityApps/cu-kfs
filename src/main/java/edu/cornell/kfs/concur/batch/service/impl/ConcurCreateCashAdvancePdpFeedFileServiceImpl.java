package edu.cornell.kfs.concur.batch.service.impl;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurSaeRequestedCashAdvanceBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurCreateCashAdvancePdpFeedFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;

public class ConcurCreateCashAdvancePdpFeedFileServiceImpl implements ConcurCreateCashAdvancePdpFeedFileService {
    private static final Logger LOG = LogManager.getLogger(ConcurCreateCashAdvancePdpFeedFileServiceImpl.class);
    protected String paymentImportDirectory;
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Transactional
    public boolean createPdpFeedFileForValidatedDetailFileLines(ConcurStandardAccountingExtractFile standardAccountingFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        boolean pdpFileSuccessfullyCreated = false;
        PdpFeedFileBaseEntry pdpFeedFileDataObject = buildPdpFeedBaseEntry(standardAccountingFile, reportData);
        if (pdpFeedFileDataObject.getTrailer().getDetailCount().intValue() != 0) {
            String fullyQualifiedPdpFileName = getConcurBatchUtilityService().buildFullyQualifiedPdpCashAdvanceOutputFileName(getPaymentImportDirectory(), standardAccountingFile.getOriginalFileName());
            pdpFileSuccessfullyCreated = getConcurBatchUtilityService().createPdpFeedFile(pdpFeedFileDataObject, fullyQualifiedPdpFileName);
            if (pdpFileSuccessfullyCreated) {
                LOG.info("createPdpFeedFileForValidatedDetailFileLines: fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "]  was created for standardAccountingExtractFile [" + standardAccountingFile.getOriginalFileName() + "]");
                standardAccountingFile.setFullyQualifiedRequestedCashAdvancesPdpFileName(fullyQualifiedPdpFileName);
            } else {
                LOG.error("createPdpFeedFileForValidatedDetailFileLines: FAILED TO CREATE: fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "] for standardAccoutingExtractFile [" + standardAccountingFile.getOriginalFileName() + "]");
            }
        }
        return pdpFileSuccessfullyCreated;
    }

    private PdpFeedFileBaseEntry buildPdpFeedBaseEntry(ConcurStandardAccountingExtractFile standardAccountingExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        int totalPdpDetailRecordsCount = 0;
        KualiDecimal totalPdpDetailRecordsAmount = KualiDecimal.ZERO;
        PdpFeedFileBaseEntry pdpBaseEntry = new PdpFeedFileBaseEntry();
        pdpBaseEntry.setHeader(buildPdpFeedHeaderEntry(standardAccountingExtractFile.getBatchDate()));
        List<PdpFeedGroupEntry> groupEntries = new ArrayList<PdpFeedGroupEntry>();

        for (ConcurStandardAccountingExtractDetailLine detailFileLine : standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) {
            if (isDetailFileLineValidCashAdvanceRequest(detailFileLine)) {
                PdpFeedDetailEntry pdpDetailEntry = buildPdpFeedDetailEntry(detailFileLine, buildPdpFeedAccountingEntry(detailFileLine));
                List<PdpFeedDetailEntry> pdpDetailEntries = new ArrayList<PdpFeedDetailEntry>();
                pdpDetailEntries.add(pdpDetailEntry);
                groupEntries.add(buildPdpFeedGroupEntry(detailFileLine, buildPdpFeedPayeeIdEntry(detailFileLine), pdpDetailEntries));
                recordCashAdvanceGenerationInDuplicateTrackingTable(detailFileLine, pdpDetailEntry.getSourceDocNbr(), standardAccountingExtractFile.getOriginalFileName());
                totalPdpDetailRecordsCount++;
                totalPdpDetailRecordsAmount = totalPdpDetailRecordsAmount.add(detailFileLine.getCashAdvanceAmount());
            }
            updateReportDataForDetailFileLineBeingProcessed(reportData, detailFileLine, totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount);
        }
        pdpBaseEntry.setGroup(groupEntries);
        pdpBaseEntry.setTrailer(buildPdpFeedTrailerEntry(totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount));
        pdpBaseEntry.setVersion(ConcurConstants.FEED_FILE_ENTRY_HEADER_VERSION);
        return pdpBaseEntry;
    }

    private void recordCashAdvanceGenerationInDuplicateTrackingTable(ConcurStandardAccountingExtractDetailLine detailFileLine, String sourceDocumentNumber, String requestExtractFileName) {
        ConcurRequestedCashAdvance duplicateTrackingCashAdvance =
                new ConcurRequestedCashAdvance(detailFileLine.getKfsAssembledRequestId(),
                                               detailFileLine.getEmployeeId(),
                                               detailFileLine.getCashAdvanceAmount(),
                                               detailFileLine.getBatchDate(),
                                               sourceDocumentNumber,
                                               detailFileLine.getCashAdvanceKey(),
                                               detailFileLine.getEmployeeChart(),
                                               detailFileLine.getEmployeeAccountNumber(),
                                               StringUtils.defaultIfBlank(detailFileLine.getSubAccountNumber(), StringUtils.EMPTY),
                                               getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE),
                                               StringUtils.defaultIfBlank(detailFileLine.getSubObjectCode(), StringUtils.EMPTY),
                                               StringUtils.defaultIfBlank(detailFileLine.getProjectCode(), StringUtils.EMPTY),
                                               StringUtils.defaultIfBlank(detailFileLine.getOrgRefId(), StringUtils.EMPTY),
                                               requestExtractFileName);

        getConcurRequestedCashAdvanceService().saveConcurRequestedCashAdvance(duplicateTrackingCashAdvance);
    }

    private PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurStandardAccountingExtractDetailLine detailFileLine, PdpFeedPayeeIdEntry pdpPayeeIdEntry, List<PdpFeedDetailEntry> pdpDetailEntries) {
        PdpFeedGroupEntry pdpGroupEntry = new PdpFeedGroupEntry();
        pdpGroupEntry.setPayeeName(getConcurBatchUtilityService().formatPdpPayeeName(detailFileLine.getEmployeeLastName(), detailFileLine.getEmployeeFirstName(), detailFileLine.getEmployeeMiddleInitial()));
        pdpGroupEntry.setPayeeId(pdpPayeeIdEntry);
        pdpGroupEntry.setCustomerInstitutionIdentifier(StringUtils.EMPTY);
        pdpGroupEntry.setPaymentDate(getConcurBatchUtilityService().formatDate_MMddyyyy(detailFileLine.getBatchDate()));
        pdpGroupEntry.setCombineGroupInd(ConcurConstants.COMBINED_GROUP_INDICATOR);
        pdpGroupEntry.setBankCode(ConcurConstants.BANK_CODE);
        pdpGroupEntry.setDetail(pdpDetailEntries);
        return pdpGroupEntry;
    }

    private PdpFeedDetailEntry buildPdpFeedDetailEntry(ConcurStandardAccountingExtractDetailLine detailFileLine, PdpFeedAccountingEntry pdpAccountingEntry) {
        PdpFeedDetailEntry pdpDetailEntry  = new PdpFeedDetailEntry();
        pdpDetailEntry.setSourceDocNbr(getConcurBatchUtilityService().formatSourceDocumentNumber(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CASH_ADVANCE_PDP_DOCUMENT_TYPE), detailFileLine.getCashAdvanceKey()));
        pdpDetailEntry.setInvoiceNbr(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_PDP_DEFAULT_INVOICE_NUMBER));
        pdpDetailEntry.setPoNbr(StringUtils.EMPTY);
        pdpDetailEntry.setInvoiceDate(getConcurBatchUtilityService().formatDate_MMddyyyy(detailFileLine.getBatchDate()));
        pdpDetailEntry.setNetPaymentAmt(detailFileLine.getCashAdvanceAmount());
        pdpDetailEntry.setFsOriginCd(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_AP_PDP_ORIGINATION_CODE));
        pdpDetailEntry.setFdocTypCd(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CASH_ADVANCE_PDP_DOCUMENT_TYPE));
        List<String> paymentTexts = new ArrayList<String>();
        paymentTexts.add(detailFileLine.getCashAdvanceName());
        pdpDetailEntry.setPaymentText(paymentTexts);
        List<PdpFeedAccountingEntry> accountingLines = new ArrayList<PdpFeedAccountingEntry>();
        accountingLines.add(pdpAccountingEntry);
        pdpDetailEntry.setAccounting(accountingLines);
        return pdpDetailEntry;
    }

    private PdpFeedAccountingEntry buildPdpFeedAccountingEntry(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        PdpFeedAccountingEntry pdpAccountingEntry =  new PdpFeedAccountingEntry();
        pdpAccountingEntry.setCoaCd(detailFileLine.getEmployeeChart());
        pdpAccountingEntry.setAccountNbr(detailFileLine.getEmployeeAccountNumber());
        pdpAccountingEntry.setSubAccountNbr(StringUtils.EMPTY);
        pdpAccountingEntry.setObjectCd(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE));
        pdpAccountingEntry.setSubObjectCd(StringUtils.EMPTY);
        pdpAccountingEntry.setOrgRefId(StringUtils.EMPTY);
        pdpAccountingEntry.setProjectCd(StringUtils.EMPTY);
        pdpAccountingEntry.setAmount(detailFileLine.getCashAdvanceAmount());
        return pdpAccountingEntry;
    }

    
    private PdpFeedPayeeIdEntry buildPdpFeedPayeeIdEntry(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        String defautPayeeIdType = getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_CONCUR_VALID_TRAVELER_STATUS_FOR_PDP_EMPLOYEE_CASH_ADVANCE_PROCESSING);
        PdpFeedPayeeIdEntry payeeIdEntry = new PdpFeedPayeeIdEntry();
        payeeIdEntry.setContent(detailFileLine.getEmployeeId());
        if (getConcurBatchUtilityService().isValidTravelerStatusForProcessingAsPDPEmployeeType(defautPayeeIdType)) {
            payeeIdEntry.setIdType(ConcurConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else {
            LOG.error("buildPdpFeedPayeeIdEntry: Invalid PayeeIdType detected from KFS System Parameter while building PDP output file:" + defautPayeeIdType);
        }
        return payeeIdEntry;
    }

    private PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
        header.setCampus(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_LOCATION));
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

    private boolean isDetailFileLineValidCashAdvanceRequest(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isValidCashAdvanceLine());
    }

    private boolean isDetailFileLineCashAdvanceUsedInExpenseReport(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isCashAdvanceUsedInExpenseReport());
    }

    private boolean isDetailFileLineDuplicateCashAdvanceRequest(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isDuplicatedCashAdvanceLine());
    }

    private boolean isDetailFileLineNotValidCashAdvanceRequest(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isNotValidCashAdvanceLine());
    }

    private void updateReportDataForDetailFileLineBeingProcessed(ConcurSaeRequestedCashAdvanceBatchReportData reportData, ConcurStandardAccountingExtractDetailLine detailFileLine, int totalPdpDetailRecordsCount, KualiDecimal totalPdpDetailRecordsAmount) {
        if (isDetailFileLineValidCashAdvanceRequest(detailFileLine)) {
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: Cash advance included in PDP feed file :  " + KFSConstants.NEWLINE + detailFileLine.toString());
            updateReportDataPdpFeedFileCountAndAmountTotals(reportData, totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount);
        }
        else if (isDetailFileLineCashAdvanceUsedInExpenseReport(detailFileLine)) {
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: Cash advance used in expense report :  " + KFSConstants.NEWLINE + detailFileLine.toString());
            updateReportDataCashAdvancesUsedInExpenseReportCountAndAmountTotals(reportData, detailFileLine);
        }
        else if (isDetailFileLineDuplicateCashAdvanceRequest(detailFileLine)) {
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: DUPLICATE cash advance :  " + KFSConstants.NEWLINE + detailFileLine.toString());
            updateReportDataDuplicateCashAdvanceCountAndAmountTotals(reportData, detailFileLine);
            updateReportDataWithFileLineValidationError(reportData, detailFileLine);
        }
        else if (detailFileLine.getValidationResult().isNotCashAdvanceLine()) {
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: NOT a cash advance :  " + KFSConstants.NEWLINE + detailFileLine.toString());
            updateReportDataRecordsBypassedNotCashAdvanceCountAndAmountTotals(reportData, detailFileLine);
        }
        else if (isDetailFileLineNotValidCashAdvanceRequest(detailFileLine)) {
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: Cash Advance but validation failed :  " + KFSConstants.NEWLINE + detailFileLine.toString());
            updateReportDataValidationErrorCountAndAmountTotals(reportData, detailFileLine);
            updateReportDataWithFileLineValidationError(reportData, detailFileLine);
        }
        else {
            LOG.error("updateReportDataForDetailFileLineBeingProcessed: ***PROBLEM*** detailFileLine Line NOT included in any report totals and this should NOT happen :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        updateReportDataRequestExtractFileTotalCountAndAmountTotals(reportData, detailFileLine);
    }

    private void updateReportDataPdpFeedFileCountAndAmountTotals(ConcurSaeRequestedCashAdvanceBatchReportData reportData, int totalPdpDetailRecordsCount, KualiDecimal totalPdpDetailRecordsAmount) {
        reportData.getCashAdvancesProcessedInPdp().setRecordCount(totalPdpDetailRecordsCount);
        reportData.getCashAdvancesProcessedInPdp().setDollarAmount(totalPdpDetailRecordsAmount);
    }

    private void updateReportDataCashAdvancesUsedInExpenseReportCountAndAmountTotals(ConcurSaeRequestedCashAdvanceBatchReportData reportData, ConcurStandardAccountingExtractDetailLine detailFileLine) {
        reportData.getCashAdvancesBypassedRelatedToExpenseReport().incrementRecordCount();
        reportData.getCashAdvancesBypassedRelatedToExpenseReport().addDollarAmount(detailFileLine.getCashAdvanceAmount());
    }

    private void updateReportDataDuplicateCashAdvanceCountAndAmountTotals(ConcurSaeRequestedCashAdvanceBatchReportData reportData, ConcurStandardAccountingExtractDetailLine detailFileLine) {
        reportData.getDuplicateCashAdvanceRequests().incrementRecordCount();
        reportData.getDuplicateCashAdvanceRequests().addDollarAmount(detailFileLine.getCashAdvanceAmount());
    }

    private void updateReportDataRecordsBypassedNotCashAdvanceCountAndAmountTotals(ConcurSaeRequestedCashAdvanceBatchReportData reportData, ConcurStandardAccountingExtractDetailLine detailFileLine) {
        reportData.getRecordsBypassedNotCashAdvances().incrementRecordCount();
        reportData.getRecordsBypassedNotCashAdvances().addDollarAmount(detailFileLine.getJournalAmount());
    }

    private void updateReportDataValidationErrorCountAndAmountTotals(ConcurSaeRequestedCashAdvanceBatchReportData reportData, ConcurStandardAccountingExtractDetailLine detailFileLine) {
        reportData.getCashAdvancesNotProcessedValidationErrors().incrementRecordCount();
        reportData.getCashAdvancesNotProcessedValidationErrors().addDollarAmount(detailFileLine.getCashAdvanceAmount());
    }

    private void updateReportDataRequestExtractFileTotalCountAndAmountTotals(ConcurSaeRequestedCashAdvanceBatchReportData reportData, ConcurStandardAccountingExtractDetailLine detailFileLine) {
        reportData.getTotalsForFile().incrementRecordCount();
        KualiDecimal amountForTotal = ObjectUtils.isNotNull(detailFileLine.getCashAdvanceAmount()) ? detailFileLine.getCashAdvanceAmount() : detailFileLine.getJournalAmount();
        reportData.getTotalsForFile().addDollarAmount(amountForTotal);
    }

    private void updateReportDataWithFileLineValidationError(ConcurSaeRequestedCashAdvanceBatchReportData reportData, ConcurStandardAccountingExtractDetailLine detailFileLine) {
        ConcurBatchReportLineValidationErrorItem errorDetails =
                new ConcurBatchReportLineValidationErrorItem(detailFileLine.getCashAdvanceKey(), detailFileLine.getEmployeeId(), detailFileLine.getEmployeeLastName(), detailFileLine.getEmployeeFirstName(), detailFileLine.getEmployeeMiddleInitial(), detailFileLine.getValidationResult().getErrorMessages());
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
