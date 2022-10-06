package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportMissingObjectCodeItem;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountExtractPdpEntryService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCreateCollectorFileService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {
    private static final Logger LOG = LogManager.getLogger(ConcurStandardAccountingExtractServiceImpl.class);

    protected String paymentImportDirectory;
    protected String collectorImportDirectory;
    protected BatchInputFileService batchInputFileService;
    protected CUMarshalService cuMarshalService;
    protected BatchInputFileType batchInputFileType;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService;
    protected ConcurStandardAccountExtractPdpEntryService concurStandardAccountExtractPdpEntryService;
    protected ConcurStandardAccountingExtractCreateCollectorFileService concurStandardAccountingExtractCreateCollectorFileService;
    protected ParameterService parameterService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;

    @Override
    public ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFile(String standardAccountingExtractFileName) throws ValidationException {
        LOG.info("parseStandardAccoutingExtractFile, Attempting to parse the file " + standardAccountingExtractFileName);

        ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile = loadConcurStandardAccountingExtractFile(standardAccountingExtractFileName);
        logDetailedInfoForConcurStandardAccountingExtractFile(concurStandardAccountingExtractFile);

        return concurStandardAccountingExtractFile;
    }

    private ConcurStandardAccountingExtractFile loadConcurStandardAccountingExtractFile(String standardAccountingExtractFileName) {
        ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile;
        File standardAccountingExtractFile = new File(standardAccountingExtractFileName);
        List parsed = (List) batchInputFileService.parse(batchInputFileType, LoadFileUtils.safelyLoadFileBytes(standardAccountingExtractFile));
        if (parsed == null || parsed.size() != 1) {
            LOG.error("parseStandardAccoutingExtractFileToStandardAccountingExtractFile, Unable to parse the file into exactly 1 POJO");
            throw new ValidationException(
                    "parseStandardAccoutingExtractFileToStandardAccountingExtractFile, did not parse the file into exactly 1 parse file ");
        }
        concurStandardAccountingExtractFile = (ConcurStandardAccountingExtractFile) parsed.get(0);
        concurStandardAccountingExtractFile.setOriginalFileName(standardAccountingExtractFile.getName());
        return concurStandardAccountingExtractFile;
    }
    
    protected void logDetailedInfoForConcurStandardAccountingExtractFile(ConcurStandardAccountingExtractFile saeFile) {
        if (LOG.isDebugEnabled()) {
            if (saeFile != null) {
                LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, " + saeFile.toString());
                if (saeFile.getConcurStandardAccountingExtractDetailLines() != null) {
                    LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, Number of line items: " + 
                            saeFile.getConcurStandardAccountingExtractDetailLines().size());
                    for (ConcurStandardAccountingExtractDetailLine line : saeFile.getConcurStandardAccountingExtractDetailLines()) {
                        LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, " + line.toString());
                    }
                } else {
                    LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, The getConcurStandardAccountingExtractDetailLines is null");
                }
                
            } else {
                LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, The SAE file is null");
            }
        }
    }
    
    @Override
    public List<String> buildListOfFullyQualifiedFileNamesToBeProcessed() {
        List<String> listOfFileNames = getBatchInputFileService().listInputFileNamesWithDoneFile(getBatchInputFileType());
        if (LOG.isInfoEnabled()) {
            String numberOfFiles = listOfFileNames != null ? String.valueOf(listOfFileNames.size()) : "NULL";
            LOG.info("buildListOfFileNamesToBeProcessed number of files found to process: " + numberOfFiles);
        }
        return listOfFileNames;
    }

    @Override
    public String extractPdpFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean success = true;
        String pdpFileName = StringUtils.EMPTY;
        if (!concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().isEmpty()){
            PdpFeedFileBaseEntry pdpFeedFileBaseEntry = buildPdpFeedFileBaseEntry(concurStandardAccountingExtractFile, reportData);
            pdpFeedFileBaseEntry = getConcurStandardAccountExtractPdpEntryService().createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
            logPdpSummaryInformation(pdpFeedFileBaseEntry);
            pdpFileName = buildPdpOutputFileName(concurStandardAccountingExtractFile.getOriginalFileName());
            String pdpFullyQualifiedFilePath = getPaymentImportDirectory() + pdpFileName;
            success = marshalPdpFeedFile(pdpFeedFileBaseEntry, pdpFullyQualifiedFilePath);
        } else {
            LOG.error("extractPdpFeedFromStandardAccountingExtract, there are no detail lines to process.");
        }
        return success ? pdpFileName : StringUtils.EMPTY;
    }
    
    private void logPdpSummaryInformation(PdpFeedFileBaseEntry pdpFeedFileBaseEntry) {
        if(LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder("logPdpSummaryInformation,");
            sb.append(" PDP creation date: ").append(pdpFeedFileBaseEntry.getHeader().getCreationDate());
            sb.append(", total number of payments in PDP file: ").append(pdpFeedFileBaseEntry.getTrailer().getDetailCount());
            sb.append(", total amount to be paid: ").append(pdpFeedFileBaseEntry.getTrailer().getDetailTotAmt());
            LOG.info(sb.toString());
        }
    }
    
    private PdpFeedFileBaseEntry buildPdpFeedFileBaseEntry(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData) {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = new PdpFeedFileBaseEntry();
        pdpFeedFileBaseEntry.setVersion(ConcurConstants.FEED_FILE_ENTRY_HEADER_VERSION);
        pdpFeedFileBaseEntry.setHeader(getConcurStandardAccountExtractPdpEntryService().buildPdpFeedHeaderEntry(concurStandardAccountingExtractFile.getBatchDate()));
        int totalReimbursementLineCount = 0;
        KualiDecimal totalReimbursementDollarAmount = KualiDecimal.ZERO;
        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) {
            if (shouldProcessSAELineToPDP(line)) {
                if (shouldLineTotalsBeAddedToReimbursementReportTotals(line)) {
                    totalReimbursementLineCount++;
                    totalReimbursementDollarAmount = totalReimbursementDollarAmount.add(line.getJournalAmount());
                }
                logJournalAccountCodeOverridden(line, reportData);
                String overriddenObjectCode = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, 
                        CUKFSParameterKeyConstants.ALL_COMPONENTS, ConcurParameterConstants.CONCUR_SAE_PDP_DEFAULT_OBJECT_CODE);
                String overriddenSubObjectCode = StringUtils.EMPTY;
                if (getConcurStandardAccountingExtractValidationService().validateConcurStandardAccountingExtractDetailLineWithObjectCodeOverrideForPdp(line, reportData, 
                        overriddenObjectCode, overriddenSubObjectCode)) {
                    buildAndUpdateAccountingEntryFromLine(pdpFeedFileBaseEntry, line, concurStandardAccountingExtractFile);
                }
            }
        }
        reportData.getReimbursementsInExpenseReport().setRecordCount(totalReimbursementLineCount);
        reportData.getReimbursementsInExpenseReport().setDollarAmount(totalReimbursementDollarAmount);
        pdpFeedFileBaseEntry.setTrailer(getConcurStandardAccountExtractPdpEntryService().buildPdpFeedTrailerEntry(pdpFeedFileBaseEntry, reportData));
        return pdpFeedFileBaseEntry;
    }
    
    private boolean shouldLineTotalsBeAddedToReimbursementReportTotals(ConcurStandardAccountingExtractDetailLine line ) {
        boolean isPersonalExpenseChargedToCorporateCard = getConcurBatchUtilityService().lineRepresentsPersonalExpenseChargedToCorporateCard(line);
        boolean isCashAdvanceLine = getConcurStandardAccountingExtractCashAdvanceService().isCashAdvanceToBeAppliedToReimbursement(line);
        return !isPersonalExpenseChargedToCorporateCard && !isCashAdvanceLine;
    }
    
    protected boolean shouldProcessSAELineToPDP(ConcurStandardAccountingExtractDetailLine line) {
        boolean isCashLine = StringUtils.equalsIgnoreCase(line.getPaymentCode(), ConcurConstants.PAYMENT_CODE_CASH);
        boolean isPersonalExpenseChargedToCorporateCard = getConcurBatchUtilityService().lineRepresentsPersonalExpenseChargedToCorporateCard(line);
        boolean isCreditLine = StringUtils.equalsIgnoreCase(line.getJournalDebitCredit(), ConcurConstants.CREDIT);
        boolean isReturnOfCorporateCardPersonalExpenseToUser = getConcurBatchUtilityService()
                .lineRepresentsReturnOfCorporateCardPersonalExpenseToUser(line);
        boolean isReturnOfCorporateCardPersonalExpenseToUniversity = getConcurBatchUtilityService()
                .lineRepresentsReturnOfCorporateCardPersonalExpenseToUniversity(line);
        boolean isAtmFeeDebit = getConcurStandardAccountingExtractCashAdvanceService().isAtmFeeDebitLine(line);
        boolean isAtmFeeCredit = getConcurStandardAccountingExtractCashAdvanceService().isAtmFeeCreditLine(line);
        return (isCashLine && !isAtmFeeDebit && !isAtmFeeCredit)
                || (isPersonalExpenseChargedToCorporateCard && !isReturnOfCorporateCardPersonalExpenseToUniversity
                        && (isCreditLine || isReturnOfCorporateCardPersonalExpenseToUser));
    }
    
    private void logJournalAccountCodeOverridden(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        if (line.getJournalAccountCodeOverridden().booleanValue()) {
            String overriddenMessage = "The journal account code needed to be overridden.";
            reportData.addPendingClientObjectCodeLine(new ConcurBatchReportMissingObjectCodeItem(line, overriddenMessage));
            LOG.error("logJournalAccountCodeOverridden, the journal account code needed to be overridden");
        }
    }

    private void buildAndUpdateAccountingEntryFromLine(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, ConcurStandardAccountingExtractDetailLine line, 
            ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        PdpFeedGroupEntry currentGroup = getGroupEntryForLine(pdpFeedFileBaseEntry, line);
        PdpFeedDetailEntry currentDetail = getDetailEntryForLine(currentGroup, line);
        PdpFeedAccountingEntry currentAccounting = getAccountingEntryForLine(currentDetail, line, concurStandardAccountingExtractFile);
        KualiDecimal newAmount = line.getJournalAmount().add(currentAccounting.getAmount());
        currentAccounting.setAmount(newAmount);
    }
    
    private PdpFeedGroupEntry getGroupEntryForLine(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, ConcurStandardAccountingExtractDetailLine line) {
        for (PdpFeedGroupEntry groupEntry : pdpFeedFileBaseEntry.getGroup()) {
            if (StringUtils.equalsIgnoreCase(line.getEmployeeId(), groupEntry.getPayeeId().getContent())) {
                return groupEntry;
            }
        }
        PdpFeedGroupEntry group = getConcurStandardAccountExtractPdpEntryService().buildPdpFeedGroupEntry(line);
        pdpFeedFileBaseEntry.getGroup().add(group);
        return group;
    }
    
    private PdpFeedDetailEntry getDetailEntryForLine(PdpFeedGroupEntry groupEntry, ConcurStandardAccountingExtractDetailLine line) {
        for (PdpFeedDetailEntry detailEntry : groupEntry.getDetail()) {
            if (StringUtils.equalsIgnoreCase(detailEntry.getSourceDocNbr(), 
                    getConcurStandardAccountExtractPdpEntryService().buildSourceDocumentNumber(line.getReportId()))) {
                return detailEntry;
            }
        }
        PdpFeedDetailEntry detailEntry = getConcurStandardAccountExtractPdpEntryService().buildPdpFeedDetailEntry(line);
        groupEntry.getDetail().add(detailEntry);
        return detailEntry;
    }
    
    private PdpFeedAccountingEntry getAccountingEntryForLine(PdpFeedDetailEntry detailEntry, ConcurStandardAccountingExtractDetailLine line, 
            ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        
        ConcurAccountInfo concurAccountInfo = buildConcurAccountInfoFromExtractDetailLine(line,
                concurStandardAccountingExtractFile);
        
        for (PdpFeedAccountingEntry accountingEntry : detailEntry.getAccounting()) {
            if (isCurrentAccountingEntrySameAsLineDetail(accountingEntry, concurAccountInfo)) {
                return accountingEntry;
            }
        }
        PdpFeedAccountingEntry accountingEntry = getConcurStandardAccountExtractPdpEntryService().buildPdpFeedAccountingEntry(concurAccountInfo);
        detailEntry.getAccounting().add(accountingEntry);
        return accountingEntry;
    }

    private ConcurAccountInfo buildConcurAccountInfoFromExtractDetailLine(ConcurStandardAccountingExtractDetailLine line,
            ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        ConcurAccountInfo concurAccountInfo;
        if (getConcurStandardAccountingExtractCashAdvanceService().isCashAdvanceToBeAppliedToReimbursement(line)) {
            concurAccountInfo = getConcurStandardAccountingExtractCashAdvanceService().findAccountingInfoForCashAdvanceLine(line, 
                    concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines());
        } else if (getConcurBatchUtilityService().lineRepresentsPersonalExpenseChargedToCorporateCard(line)) {
            concurAccountInfo = new ConcurAccountInfo(line.getReportChartOfAccountsCode(), line.getReportAccountNumber(), 
                    line.getReportSubAccountNumber(), line.getJournalAccountCode(), line.getReportSubObjectCode(), line.getReportProjectCode(), 
                    line.getReportOrgRefId());
        } else {
            concurAccountInfo = new ConcurAccountInfo(line.getChartOfAccountsCode(), line.getAccountNumber(), line.getSubAccountNumber(), 
                    line.getJournalAccountCode(), line.getSubObjectCode(), line.getProjectCode(), line.getOrgRefId());
        }
        return concurAccountInfo;
    }
    
    protected String buildPdpOutputFileName(String originalFileName) {
        return ConcurConstants.PDP_CONCUR_TRIP_REIMBURSEMENT_OUTPUT_FILE_NAME_PREFIX + 
                StringUtils.replace(originalFileName, CuGeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION, ConcurConstants.XML_FILE_EXTENSION);
    }
    
    protected boolean isCurrentAccountingEntrySameAsLineDetail(PdpFeedAccountingEntry currentAccountingEntry, 
            ConcurAccountInfo concurAccountInfo) {
        boolean isSame = compareStrings(currentAccountingEntry.getCoaCd(), concurAccountInfo.getChart()) &&
                compareStrings(currentAccountingEntry.getAccountNbr(), concurAccountInfo.getAccountNumber()) &&
                compareStrings(currentAccountingEntry.getSubAccountNbr(), concurAccountInfo.getSubAccountNumber()) &&
                compareStrings(currentAccountingEntry.getOrgRefId(), concurAccountInfo.getOrgRefId()) &&
                compareStrings(currentAccountingEntry.getProjectCd(), concurAccountInfo.getProjectCode());
        return isSame;
    }
    
    private boolean compareStrings(String one, String two) {
        one = StringUtils.trimToEmpty(one);
        two = StringUtils.trimToEmpty(two);
        return StringUtils.equalsIgnoreCase(one, two);
    }

    private boolean marshalPdpFeedFile(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, String outputFullyQualifiedFilePath) {
        boolean success = true;
        try {
            if (doesPdpFileBaseEntryHaveAccountingEntries(pdpFeedFileBaseEntry)) {
                File pdpFeedFile = getCuMarshalService().marshalObjectToXML(pdpFeedFileBaseEntry, outputFullyQualifiedFilePath);
                LOG.info("marshalPdpFeedFile, marshaled the file " + outputFullyQualifiedFilePath);
                success = true;
            } else {
                LOG.info("marshalPdpFeedFile, did not marshal " + outputFullyQualifiedFilePath + " as there were no accounting entries");
            }
        } catch (JAXBException | IOException e) {
            LOG.error("marshalPdpFeedFile, There was an error marshalling the PDP feed file.", e);
            success = false;
        }
        return success;
    }
    
    private boolean doesPdpFileBaseEntryHaveAccountingEntries(PdpFeedFileBaseEntry pdpFeedFileBaseEntry) {
        boolean hasAccountingEntries = false;
        for (PdpFeedGroupEntry group : pdpFeedFileBaseEntry.getGroup()) {
            for (PdpFeedDetailEntry detail : group.getDetail()) {
                hasAccountingEntries = hasAccountingEntries || detail.getAccounting().size() > 0;
            }
        }
        return hasAccountingEntries;
    }
    
    @Override
    public void createDoneFileForPdpFile(String pdpFileName) throws IOException {
        if (pdpUploadFileExists(pdpFileName)) {
            String fullDoneFilePath = StringUtils.replace(getPaymentImportDirectory() + pdpFileName, ConcurConstants.XML_FILE_EXTENSION, 
                    GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);
            LOG.info("createDoneFileForPdpFile, fullFilePath: " + fullDoneFilePath);
            FileUtils.touch(new File(fullDoneFilePath));
        } else {
            LOG.info("createDoneFileForPdpFile, the PDP upload file was not created, so we don't want to create the .done file.");
        }
    }

    @Override
    public void removeDoneFileForPdpFileQuietly(String pdpFileName) {
        try {
            if (!StringUtils.endsWith(pdpFileName, ConcurConstants.XML_FILE_EXTENSION) || !pdpUploadFileExists(pdpFileName)) {
                LOG.error("removeDoneFileForPdpFileQuietly, Cannot remove PDP .done file if the PDP upload file was not already created.");
                return;
            }
        
            String fullDoneFilePath = StringUtils.replace(getPaymentImportDirectory() + pdpFileName, ConcurConstants.XML_FILE_EXTENSION, 
                    GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);
            if (!FileUtils.deleteQuietly(new File(fullDoneFilePath))) {
                LOG.error("removeDoneFileForPdpFileQuietly, Could not remove PDP .done file: " + fullDoneFilePath);
            }
        } catch (RuntimeException e) {
            LOG.error("removeDoneFileForPdpFileQuietly, Unexpected runtime exception was thrown while attempting to remove PDP .done file", e);
        }
    }

    private boolean pdpUploadFileExists(String pdpFileName) {
        File pdpFile = new File(getPaymentImportDirectory() + pdpFileName);
        return pdpFile.exists();
    }

    @Override
    public String extractCollectorFeedFromStandardAccountingExtract(
            ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData) {
        return getConcurStandardAccountingExtractCreateCollectorFileService().buildCollectorFile(
                concurStandardAccountingExtractFile, reportData);
    }

    @Override
    public void createDoneFileForCollectorFile(String collectorFileName) throws IOException {
        if (collectorUploadFileExists(collectorFileName)) {
            String fullDoneFilePath = StringUtils.replace(getCollectorImportDirectory() + collectorFileName,
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION, GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);
            LOG.info("createDoneFileForCollectorFile, fullFilePath: " + fullDoneFilePath);
            FileUtils.touch(new File(fullDoneFilePath));
        } else {
            LOG.info("createDoneFileForCollectorFile, the Collector upload file was not created, so we don't want to create the .done file.");
        }
    }

    private boolean collectorUploadFileExists(String collectorFileName) {
        File collectorFile = new File(getCollectorImportDirectory() + collectorFileName);
        return collectorFile.exists();
    }

    public String getPaymentImportDirectory() {
        return paymentImportDirectory;
    }

    public void setPaymentImportDirectory(String paymentImportDirectory) {
        this.paymentImportDirectory = paymentImportDirectory;
    }

    public String getCollectorImportDirectory() {
        return collectorImportDirectory;
    }

    public void setCollectorImportDirectory(String collectorImportDirectory) {
        this.collectorImportDirectory = collectorImportDirectory;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public CUMarshalService getCuMarshalService() {
        return cuMarshalService;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public ConcurStandardAccountingExtractValidationService getConcurStandardAccountingExtractValidationService() {
        return concurStandardAccountingExtractValidationService;
    }

    public void setConcurStandardAccountingExtractValidationService(
            ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService) {
        this.concurStandardAccountingExtractValidationService = concurStandardAccountingExtractValidationService;
    }

    public ConcurStandardAccountExtractPdpEntryService getConcurStandardAccountExtractPdpEntryService() {
        return concurStandardAccountExtractPdpEntryService;
    }

    public void setConcurStandardAccountExtractPdpEntryService(
            ConcurStandardAccountExtractPdpEntryService concurStandardAccountExtractPdpEntryService) {
        this.concurStandardAccountExtractPdpEntryService = concurStandardAccountExtractPdpEntryService;
    }

    public ConcurStandardAccountingExtractCreateCollectorFileService getConcurStandardAccountingExtractCreateCollectorFileService() {
        return concurStandardAccountingExtractCreateCollectorFileService;
    }

    public void setConcurStandardAccountingExtractCreateCollectorFileService(
            ConcurStandardAccountingExtractCreateCollectorFileService concurStandardAccountingExtractCreateCollectorFileService) {
        this.concurStandardAccountingExtractCreateCollectorFileService = concurStandardAccountingExtractCreateCollectorFileService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ConcurStandardAccountingExtractCashAdvanceService getConcurStandardAccountingExtractCashAdvanceService() {
        return concurStandardAccountingExtractCashAdvanceService;
    }

    public void setConcurStandardAccountingExtractCashAdvanceService(
            ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService) {
        this.concurStandardAccountingExtractCashAdvanceService = concurStandardAccountingExtractCashAdvanceService;
    }

}
