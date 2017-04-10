package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
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
    public boolean createPdpFeedFileForValidatedDetailFileLines(ConcurRequestExtractFile requestExtractFile) {
        boolean pdpFileSuccessfullyCreated = false;
        PdpFeedFileBaseEntry pdpFeedFileDataObject = buildPdpFeedBaseEntry(requestExtractFile);
        if (pdpFeedFileDataObject.getTrailer().getDetailCount().intValue() != 0) {
            String fullyQualifiedPdpFileName = getConcurBatchUtilityService().buildFullyQualifiedPdpOutputFileName(getPaymentImportDirectory(), requestExtractFile.getFileName());
            pdpFileSuccessfullyCreated = getConcurBatchUtilityService().createPdpFeedFile(pdpFeedFileDataObject, fullyQualifiedPdpFileName);
            if (pdpFileSuccessfullyCreated) {
                LOG.info("fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "]  was created for requestExtractFile [" + requestExtractFile.getFileName() + "]");
                requestExtractFile.setFullyQualifiedPdpFileName(fullyQualifiedPdpFileName);
            }
            else {
                LOG.error("FAILED TO CREATE: fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "] for requestExtractFile [" + requestExtractFile.getFileName() + "]");
            }
        }
        return pdpFileSuccessfullyCreated;
    }

    private PdpFeedFileBaseEntry buildPdpFeedBaseEntry(ConcurRequestExtractFile requestExtractFile) {
        int totalPdpDetailRecordsCount = 0;
        KualiDecimal totalPdpDetailRecordsAmount = KualiDecimal.ZERO;
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
          else if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isNotValidCashAdvanceLine()) {
              LOG.info("Cash Advance was detected but validation failed for:  " + KFSConstants.NEWLINE + detailFileLine.toString());
          }
        }
        pdpBaseEntry.setGroup(groupEntries);
        pdpBaseEntry.setTrailer(buildPdpFeedTrailerEntry(totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount));
        pdpBaseEntry.setVersion(ConcurConstants.FEED_FILE_ENTRY_HEADER_VERSION);
        return pdpBaseEntry;
    }

    private void recordCashAdvanceGenerationInDuplicateTrackingTable(ConcurRequestExtractRequestDetailFileLine detailFileLine, String soureDocumentNumber, String requestExtractFileName) {
        ConcurRequestedCashAdvance duplicateTrackingCashAdvance =
                new ConcurRequestedCashAdvance(detailFileLine.getRequestId(), detailFileLine.getEmployeeId(), detailFileLine.getRequestAmount(), detailFileLine.getBatchDate(), soureDocumentNumber, requestExtractFileName);
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
        pdpDetailEntry.setInvoiceNbr(StringUtils.EMPTY);
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
            LOG.error("Invalid PayeeIdType detected in buildPdpFeedPayeeIdEntry AFTER validation while building PDP output file:" + detailFileLine.getPayeeIdType());
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

   public void createDoneFileForPdpFile(String concurCashAdvancePdpFeedFileName) {
       getConcurBatchUtilityService().createDoneFile(concurCashAdvancePdpFeedFileName);
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
