package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountExtractPdpEntryService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractServiceImpl.class);

    protected String paymentImportDirectory;
    protected BatchInputFileService batchInputFileService;
    protected CUMarshalService cuMarshalService;
    protected BatchInputFileType batchInputFileType;
    protected ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService;
    protected ConcurStandardAccountExtractPdpEntryService concurStandardAccountExtractPdpEntryService;
    protected ParameterService parameterService;

    @Override
    public ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFile(String standardAccountingExtractFileName) throws ValidationException {
        LOG.debug("parseStandardAccoutingExtractFile, Attempting to parse the file " + standardAccountingExtractFileName);

        ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile = loadConcurStandardAccountingExtractFile(standardAccountingExtractFileName);
        logDetailedInfoForConcurStandardAccountingExtractFile(concurStandardAccountingExtractFile);

        return concurStandardAccountingExtractFile;
    }

    private ConcurStandardAccountingExtractFile loadConcurStandardAccountingExtractFile(String standardAccountingExtractFileName) {
        ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile;
        File standardAccountingExtractFile = new File(standardAccountingExtractFileName);
        List parsed = (List) batchInputFileService.parse(batchInputFileType, safelyLoadFileBytes(standardAccountingExtractFile));
        if (parsed == null || parsed.size() != 1) {
            LOG.error("parseStandardAccoutingExtractFileToStandardAccountingExtractFile, Unable to parse the file into exactly 1 POJO");
            throw new ValidationException(
                    "parseStandardAccoutingExtractFileToStandardAccountingExtractFile, did not parse the file into exactly 1 parse file ");
        }
        concurStandardAccountingExtractFile = (ConcurStandardAccountingExtractFile) parsed.get(0);
        concurStandardAccountingExtractFile.setOriginalFileName(standardAccountingExtractFile.getName());
        return concurStandardAccountingExtractFile;
    }

    protected byte[] safelyLoadFileBytes(File file) {
        InputStream fileContents;
        byte[] fileByteContent;
        String fileName = file.getName();
        try {
            fileContents = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            LOG.error("safelyLoadFileBytes, Batch file not found [" + fileName + "]. " + e1.getMessage(), e1);
            throw new RuntimeException("Batch File not found [" + fileName + "]. " + e1.getMessage());
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("safelyLoadFileBytes, IO Exception loading: [" + fileName + "]. " + e1.getMessage(), e1);
            throw new RuntimeException("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        return fileByteContent;
    }
    
    protected void logDetailedInfoForConcurStandardAccountingExtractFile(ConcurStandardAccountingExtractFile saeFile) {
        if (LOG.isDebugEnabled()) {
            if (saeFile != null) {
                LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, " + saeFile.getDebugInformation());
                if (saeFile.getConcurStandardAccountingExtractDetailLines() != null) {
                    LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, Number of line items: " + 
                            saeFile.getConcurStandardAccountingExtractDetailLines().size());
                    for (ConcurStandardAccountingExtractDetailLine line : saeFile.getConcurStandardAccountingExtractDetailLines()) {
                        LOG.debug("logDetailedInfoForConcurStandardAccountingExtractFile, " + line.getDebugInformation());
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
    public List<String> buildListOfFileNamesToBeProcessed() {
        List<String> listOfFileNames = getBatchInputFileService().listInputFileNamesWithDoneFile(getBatchInputFileType());
        if (LOG.isDebugEnabled()) {
            String numberOfFiles = listOfFileNames != null ? String.valueOf(listOfFileNames.size()) : "NULL";
            LOG.debug("buildListOfFileNamesToBeProcessed number of files found to process: " + numberOfFiles);
        }
        return listOfFileNames;
    }

    @Override
    public String extractPdpFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        boolean success = true;
        String pdpFileName = StringUtils.EMPTY;
        if (!concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().isEmpty()){
            PdpFeedFileBaseEntry pdpFeedFileBaseEntry = buildPdpFeedFileBaseEntry(concurStandardAccountingExtractFile);
            pdpFileName = buildPdpOutputFileName(concurStandardAccountingExtractFile.getOriginalFileName());
            String pdpFilePath = getPaymentImportDirectory() + pdpFileName;
            success = marshalPdpFeedFile(pdpFeedFileBaseEntry, pdpFilePath);
        }
        return success ? pdpFileName : StringUtils.EMPTY;
    }
    
    private PdpFeedFileBaseEntry buildPdpFeedFileBaseEntry(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        KualiDecimal pdpTotal = KualiDecimal.ZERO;
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = new PdpFeedFileBaseEntry();
        pdpFeedFileBaseEntry.setVersion(ConcurConstants.FEED_FILE_ENTRY_HEADER_VERSION);
        pdpFeedFileBaseEntry.setHeader(
                getConcurStandardAccountExtractPdpEntryService().buildPdpFeedHeaderEntry(concurStandardAccountingExtractFile.getBatchDate()));
        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) {
            if (StringUtils.equalsIgnoreCase(line.getPaymentCode(), ConcurConstants.PAYMENT_CODE_CASH)) {
                logJournalAccountCodeOverriden(line);
                if (getConcurStandardAccountingExtractValidationService().validateConcurStandardAccountingExtractDetailLine(line)) {
                    buildAndUpdateAccountingEntryFromLine(pdpFeedFileBaseEntry, line);
                    pdpTotal = pdpTotal.add(line.getJournalAmount());
                }
            }
        }
        pdpFeedFileBaseEntry.setTrailer(getConcurStandardAccountExtractPdpEntryService().buildPdpFeedTrailerEntry(pdpFeedFileBaseEntry, pdpTotal));
        return pdpFeedFileBaseEntry;
    }
    
    private void logJournalAccountCodeOverriden(ConcurStandardAccountingExtractDetailLine line) {
        if (line.getJournalAccountCodeOverriden().booleanValue()) {
            LOG.error("logJournalAccountCodeOverriden, the journal account code needed to be overriden");
        }
    }

    private void buildAndUpdateAccountingEntryFromLine(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedGroupEntry currentGroup = getGroupEntryForLine(pdpFeedFileBaseEntry, line);
        PdpFeedDetailEntry currentDetail = getDetailEntryForLine(currentGroup, line);
        PdpFeedAccountingEntry currentAccounting = getAccountingEntryForLine(currentDetail, line);
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
    
    private PdpFeedAccountingEntry getAccountingEntryForLine(PdpFeedDetailEntry detailEntry, ConcurStandardAccountingExtractDetailLine line) {
        for (PdpFeedAccountingEntry accountingEntry : detailEntry.getAccounting()) {
            if (isCurrentAccountingEntrySameAsLineDetail(accountingEntry, line)) {
                return accountingEntry;
            }
        }
        PdpFeedAccountingEntry accountingEntry = getConcurStandardAccountExtractPdpEntryService().buildPdpFeedAccountingEntry(line);
        detailEntry.getAccounting().add(accountingEntry);
        return accountingEntry;
    }
    
    protected String buildPdpOutputFileName(String originalFileName) {
        return "pdp_concur_" +  StringUtils.replace(originalFileName, GeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION, ConcurConstants.XML_FILE_EXTENSION);
    }
    
    protected boolean isCurrentAccountingEntrySameAsLineDetail(PdpFeedAccountingEntry currentAccountingEntry, 
            ConcurStandardAccountingExtractDetailLine line) {
        boolean isSame = compareStrings(currentAccountingEntry.getCoaCd(), line.getChartOfAccountsCode()) &&
                compareStrings(currentAccountingEntry.getAccountNbr(), line.getAccountNumber()) &&
                compareStrings(currentAccountingEntry.getSubAccountNbr(), line.getSubAccountNumber()) &&
                compareStrings(currentAccountingEntry.getObjectCd(), line.getJournalAccountCode()) &&
                compareStrings(currentAccountingEntry.getSubObjectCd(), line.getSubObjectCode()) &&
                compareStrings(currentAccountingEntry.getOrgRefId(), line.getOrgRefId()) &&
                compareStrings(currentAccountingEntry.getProjectCd(), line.getProjectCode());
        return isSame;
    }
    
    private boolean compareStrings(String one, String two) {
        one = StringUtils.trimToEmpty(one);
        two = StringUtils.trimToEmpty(two);
        return StringUtils.equalsIgnoreCase(one, two);
    }

    private boolean marshalPdpFeedFile(PdpFeedFileBaseEntry cdpFeedFileBaseEntry, String outputFilePath) {
        boolean success = true;
        try {
            File pdpFeedFile = getCuMarshalService().marshalObjectToXML(cdpFeedFileBaseEntry, outputFilePath);
            LOG.debug("marshalPdpFeedFile, marshaled the file " + outputFilePath);
            success = true;
        } catch (JAXBException | IOException e) {
            LOG.error("marshalPdpFeedFile, There was an error marshalling the PDP feed file.", e);
            success = false;
        }
        return success;
    }
    
    @Override
    public void createDoneFileForPdpFile(String pdpFileName) throws IOException {
        String fullFilePath = StringUtils.replace(getPaymentImportDirectory() + pdpFileName, ConcurConstants.XML_FILE_EXTENSION, 
                GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);
        LOG.info("createDoneFileForPdpFile, fullFilePath: " + fullFilePath);
        FileUtils.touch(new File(fullFilePath));
    }

    @Override
    public boolean extractCollectorFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        return true;
    }

    public String getPaymentImportDirectory() {
        return paymentImportDirectory;
    }

    public void setPaymentImportDirectory(String paymentImportDirectory) {
        this.paymentImportDirectory = paymentImportDirectory;
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

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
