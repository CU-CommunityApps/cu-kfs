package edu.cornell.kfs.concur.batch.service.impl;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
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
	private static final Logger LOG = LogManager.getLogger(ConcurCashAdvancePdpFeedFileServiceImpl.class);
    protected String paymentImportDirectory;
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Transactional
    public boolean createPdpFeedFileForValidatedDetailFileLines(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData) {
        boolean pdpFileSuccessfullyCreated = false;
        PdpFeedFileBaseEntry pdpFeedFileDataObject = buildPdpFeedBaseEntry(requestExtractFile, reportData);
        if (pdpFeedFileDataObject.getTrailer().getDetailCount().intValue() != 0) {
            String fullyQualifiedPdpFileName = getConcurBatchUtilityService().buildFullyQualifiedPdpCashAdvanceOutputFileName(getPaymentImportDirectory(), requestExtractFile.getFileName());
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
        PdpFeedFileBaseEntry pdpBaseEntry = new PdpFeedFileBaseEntry();
        pdpBaseEntry.setHeader(buildPdpFeedHeaderEntry(requestExtractFile.getBatchDate()));
        List<PdpFeedGroupEntry> groupEntries = new ArrayList<PdpFeedGroupEntry>();

        for (ConcurRequestExtractRequestDetailFileLine detailFileLine : requestExtractFile.getRequestDetails()) {
            if (isDetailFileLineValidCashAdvanceRequest(detailFileLine)) {
                PdpFeedDetailEntry pdpDetailEntry = buildPdpFeedDetailEntry(detailFileLine, buildPdpFeedAccountingEntry(detailFileLine));
                List<PdpFeedDetailEntry> pdpDetailEntries = new ArrayList<PdpFeedDetailEntry>();
                pdpDetailEntries.add(pdpDetailEntry);
                groupEntries.add(buildPdpFeedGroupEntry(detailFileLine, buildPdpFeedPayeeIdEntry(detailFileLine), pdpDetailEntries));
                recordCashAdvanceGenerationInDuplicateTrackingTable(detailFileLine, pdpDetailEntry.getSourceDocNbr(), requestExtractFile.getFileName());
                totalPdpDetailRecordsCount++;
                totalPdpDetailRecordsAmount = totalPdpDetailRecordsAmount.add(detailFileLine.getRequestAmount());
            }
            updateReportDataForDetailFileLineBeingProcessed(reportData, detailFileLine, totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount);
        }
        pdpBaseEntry.setGroup(groupEntries);
        pdpBaseEntry.setTrailer(buildPdpFeedTrailerEntry(totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount));
        pdpBaseEntry.setVersion(ConcurConstants.FEED_FILE_ENTRY_HEADER_VERSION);
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
        if (getConcurBatchUtilityService().isValidTravelerStatusForProcessingAsPDPEmployeeType(detailFileLine.getPayeeIdType())) {
            payeeIdEntry.setIdType(ConcurConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else {
            LOG.error("buildPdpFeedPayeeIdEntry: Invalid PayeeIdType detected in buildPdpFeedPayeeIdEntry AFTER validation while building PDP output file:" + detailFileLine.getPayeeIdType());
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

    private boolean isDetailFileLineValidCashAdvanceRequest(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isValidCashAdvanceLine());
    }

    private boolean isDetailFileLineClonedCashAdvanceRequest(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isClonedCashAdvance());
    }

    private boolean isDetailFileLineCashAdvanceUsedInExpenseReport(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isCashAdvanceUsedInExpenseReport());
    }

    private boolean isDetailFileLineDuplicateCashAdvanceRequest(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isDuplicatedCashAdvanceLine());
    }

    private boolean isDetailFileLineNotACashAdvanceRequest(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        return (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isNotValidCashAdvanceLine());
    }

    private void updateReportDataForDetailFileLineBeingProcessed(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine, int totalPdpDetailRecordsCount, KualiDecimal totalPdpDetailRecordsAmount) {
        if (isDetailFileLineValidCashAdvanceRequest(detailFileLine)) {
            updateReportDataPdpFeedFileCountAndAmountTotals(reportData, totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount);
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: Cash advance included in PDP feed file :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        else if (isDetailFileLineClonedCashAdvanceRequest(detailFileLine)) {
            updateReportDataClonedCashAdvanceCountAndAmountTotals(reportData, detailFileLine);
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: Cloned cash advance :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        else if (isDetailFileLineCashAdvanceUsedInExpenseReport(detailFileLine)) {
            updateReportDataCashAdvancesUsedInExpenseReportCountAndAmountTotals(reportData, detailFileLine);
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: Cash advance used in expense report :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        else if (isDetailFileLineDuplicateCashAdvanceRequest(detailFileLine)) {
            updateReportDataDuplicateCashAdvanceCountAndAmountTotals(reportData, detailFileLine);
            updateReportDataWithFileLineValidationError(reportData, detailFileLine);
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: DUPLICATE cash advance :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        else if (detailFileLine.getValidationResult().isNotCashAdvanceLine()) {
            updateReportDataTravelRequestsOnlyCountAndAmountTotals(reportData, detailFileLine);
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: NOT a cash advance :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        else if (isDetailFileLineNotACashAdvanceRequest(detailFileLine)) {
            updateReportDataValidationErrorCountAndAmountTotals(reportData, detailFileLine);
            updateReportDataWithFileLineValidationError(reportData, detailFileLine);
            LOG.info("updateReportDataForDetailFileLineBeingProcessed: Cash Advance but validation failed :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        else {
            LOG.error("updateReportDataForDetailFileLineBeingProcessed: ***PROBLEM*** detailFileLine Line NOT included in any report totals and this should NOT happen :  " + KFSConstants.NEWLINE + detailFileLine.toString());
        }
        updateReportDataRequestExtractFileTotalCountAndAmountTotals(reportData, detailFileLine);
    }

    private void updateReportDataPdpFeedFileCountAndAmountTotals(ConcurRequestExtractBatchReportData reportData, int totalPdpDetailRecordsCount, KualiDecimal totalPdpDetailRecordsAmount) {
        reportData.getCashAdvancesProcessedInPdp().setRecordCount(totalPdpDetailRecordsCount);
        reportData.getCashAdvancesProcessedInPdp().setDollarAmount(totalPdpDetailRecordsAmount);
    }

    private void updateReportDataClonedCashAdvanceCountAndAmountTotals(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        reportData.getClonedCashAdvanceRequests().incrementRecordCount();
        reportData.getClonedCashAdvanceRequests().addDollarAmount(detailFileLine.getRequestAmount());
    }

    private void updateReportDataCashAdvancesUsedInExpenseReportCountAndAmountTotals(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        reportData.getCashAdvancesBypassedRelatedToExpenseReport().incrementRecordCount();
        reportData.getCashAdvancesBypassedRelatedToExpenseReport().addDollarAmount(detailFileLine.getRequestAmount());
    }

    private void updateReportDataDuplicateCashAdvanceCountAndAmountTotals(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        reportData.getDuplicateCashAdvanceRequests().incrementRecordCount();
        reportData.getDuplicateCashAdvanceRequests().addDollarAmount(detailFileLine.getRequestAmount());
    }

    private void updateReportDataTravelRequestsOnlyCountAndAmountTotals(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        reportData.getRecordsBypassedTravelRequestOnly().incrementRecordCount();
        reportData.getRecordsBypassedTravelRequestOnly().addDollarAmount(detailFileLine.getRequestAmount());
    }

    private void updateReportDataValidationErrorCountAndAmountTotals(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        reportData.getCashAdvancesNotProcessedValidationErrors().incrementRecordCount();
        reportData.getCashAdvancesNotProcessedValidationErrors().addDollarAmount(detailFileLine.getRequestAmount());
    }

    private void updateReportDataRequestExtractFileTotalCountAndAmountTotals(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        reportData.getTotalsForFile().incrementRecordCount();
        reportData.getTotalsForFile().addDollarAmount(detailFileLine.getRequestAmount());
    }

    private void updateReportDataWithFileLineValidationError(ConcurRequestExtractBatchReportData reportData, ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        ConcurBatchReportLineValidationErrorItem errorDetails =
                new ConcurBatchReportLineValidationErrorItem(detailFileLine.getRequestId(), detailFileLine.getEmployeeId(), detailFileLine.getLastName(), detailFileLine.getFirstName(), detailFileLine.getMiddleInitial(), detailFileLine.getValidationResult().getErrorMessages());
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
