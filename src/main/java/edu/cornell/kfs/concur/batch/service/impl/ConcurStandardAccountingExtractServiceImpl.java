package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractServiceImpl.class);

    protected String reimbursementFeedDirectory;
    protected BatchInputFileService batchInputFileService;
    protected CUMarshalService cuMarshalService;
    protected DataDictionaryService dataDictionaryService;
    protected BatchInputFileType batchInputFileType;
    
    private Integer payeeNameFieldSize;

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
    public boolean extractPdpFeedFromStandardAccounitngExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        boolean success = true;
        
        if (!concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().isEmpty()){
            PdpFeedFileBaseEntry pdpFeedFileBaseEntry = new PdpFeedFileBaseEntry();
            pdpFeedFileBaseEntry.setHeader(buildPdpFeedHeaderEntry(concurStandardAccountingExtractFile.getBatchDate()));
            
            PdpFeedGroupEntry currentGroup = new PdpFeedGroupEntry();
            currentGroup.setPayeeId(new PdpFeedPayeeIdEntry());
            PdpFeedDetailEntry currentDetailEntry = new PdpFeedDetailEntry();
            PdpFeedAccountingEntry currentAccountingEntry = new PdpFeedAccountingEntry();
            KualiDecimal pdpTotal = KualiDecimal.ZERO;
            for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) {
                if (StringUtils.equalsIgnoreCase(line.getPaymentCode(), "CASH")) {
                    if (!StringUtils.equalsIgnoreCase(line.getEmployeeId(), currentGroup.getPayeeId().getContent())) {
                        if (StringUtils.isNotBlank(currentGroup.getPayeeId().getContent())) {
                            currentDetailEntry.getAccounting().add(currentAccountingEntry);
                            currentGroup.getDetail().add(currentDetailEntry);
                            pdpFeedFileBaseEntry.getGroup().add(currentGroup);
                            currentAccountingEntry =  new PdpFeedAccountingEntry();
                            currentDetailEntry = new PdpFeedDetailEntry();
                        }
                        currentGroup = new PdpFeedGroupEntry();
                        currentGroup.setPayeeName(buildPayeeName(line.getEmployeeLastName(), line.getEmployeeFirstName(), 
                                line.getEmployeeMiddleInitital()));
                        currentGroup.setPayeeId(buildPayeeIdEntry(line));
                        currentGroup.setPaymentDate(line.getBatchDate().toString());
                        currentGroup.setCombineGroupInd("Y");
                        currentGroup.setBankCode("DISB");
                    }
                    if (!StringUtils.equalsIgnoreCase(currentDetailEntry.getSourceDocNbr(), buildSourceDocumentNumber(line.getReportId()))) {
                        if (StringUtils.isNotBlank(currentDetailEntry.getSourceDocNbr())) {
                            pdpFeedFileBaseEntry.getGroup().add(currentGroup);
                            currentGroup.getDetail().add(currentDetailEntry);
                            currentAccountingEntry =  new PdpFeedAccountingEntry();
                        }
                        currentDetailEntry = new PdpFeedDetailEntry();
                        currentDetailEntry.setSourceDocNbr(buildSourceDocumentNumber(line.getReportId()));
                        currentDetailEntry.setFsOriginCd("Z6");
                        currentDetailEntry.setFdocTypCd("APTR");
                    }
                    if (!isCurrentAccountingEntrySameAsLineDetail(currentAccountingEntry, line)) {
                        if (StringUtils.isNotBlank(currentAccountingEntry.getAccountNbr())) {
                            currentDetailEntry.getAccounting().add(currentAccountingEntry);
                        }
                        currentAccountingEntry =  new PdpFeedAccountingEntry();
                        currentAccountingEntry.setCoaCd(line.getChartOfAccountsCode());
                        currentAccountingEntry.setAccountNbr(line.getAccountNumber());
                        currentAccountingEntry.setObjectCd(line.getJournalAccountCode());
                        currentAccountingEntry.setSubObjectCd(line.getSubObjectCode());
                        currentAccountingEntry.setOrgRefId(line.getOrgRefId());
                        currentAccountingEntry.setProjectCd(line.getProjectCode());
                        currentAccountingEntry.setAmount("0");
                    }
                    pdpTotal = pdpTotal.add(line.getJournalAmount());
                    currentAccountingEntry.setAmount(addAmounts(currentAccountingEntry.getAmount(), line.getJournalAmount()));
                }
            }
            currentDetailEntry.getAccounting().add(currentAccountingEntry);
            currentGroup.getDetail().add(currentDetailEntry);
            pdpFeedFileBaseEntry.getGroup().add(currentGroup);
            pdpFeedFileBaseEntry.setTrailer(buildPdpFeedTrailerEntry(pdpFeedFileBaseEntry, pdpTotal));
            
            String outputFilePath = getReimbursementFeedDirectory() + "foo.xml";
            success = marshalPdpFeedFle(pdpFeedFileBaseEntry, outputFilePath);
        }
        
        return success;
    }

    private PdpFeedTrailerEntry buildPdpFeedTrailerEntry(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, KualiDecimal pdpTotal) {
        PdpFeedTrailerEntry trailerEntry = new PdpFeedTrailerEntry();
        trailerEntry.setDetailCount(pdpFeedFileBaseEntry.getGroup().size());
        trailerEntry.setDetailTotAmt(new Double(pdpTotal.doubleValue()));
        return trailerEntry;
    }
    
    protected String addAmounts(String startingAmount, KualiDecimal addAmount) {
        return new KualiDecimal(startingAmount).add(addAmount).toString();
    }
    
    protected boolean isCurrentAccountingEntrySameAsLineDetail(PdpFeedAccountingEntry currentAccountingEntry, 
            ConcurStandardAccountingExtractDetailLine line) {
        boolean isSame = StringUtils.equals(currentAccountingEntry.getCoaCd(), line.getChartOfAccountsCode()) &&
                StringUtils.equals(currentAccountingEntry.getAccountNbr(), line.getAccountNumber()) &&
                StringUtils.equals(currentAccountingEntry.getSubAccountNbr(), line.getSubAccountNumber()) &&
                StringUtils.equals(currentAccountingEntry.getObjectCd(), line.getJournalAccountCode()) &&
                StringUtils.equals(currentAccountingEntry.getSubObjectCd(), line.getSubObjectCode()) &&
                StringUtils.equals(currentAccountingEntry.getOrgRefId(), line.getOrgRefId()) &&
                StringUtils.equals(currentAccountingEntry.getProjectCd(), line.getProjectCode());
        return isSame;
        
    }
    
    protected String buildSourceDocumentNumber(String reportId) {
        String sourceDocNumber = "APTR" + StringUtils.substring(reportId, 0, 19);
        return sourceDocNumber;
    }

    private PdpFeedPayeeIdEntry buildPayeeIdEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedPayeeIdEntry payeeIdEntry = new PdpFeedPayeeIdEntry();
        payeeIdEntry.setContent(line.getEmployeeId());
        if (StringUtils.equalsIgnoreCase(line.getEmployeeStatus(), "EMPLOYEE")) {
            payeeIdEntry.setIdType("E");
        } else {
            payeeIdEntry.setIdType("Y");
        }
        return payeeIdEntry;
    }

    protected PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry value = new PdpFeedHeaderEntry();
        value.setChart("IT");
        value.setCreationDate(batchDate.toString());
        value.setSubUnit("CNCR");
        value.setUnit("CRNL");
        return value;
    }
    
    protected String buildPayeeName(String lastName, String firstName, String middleInitial) {
        String seperator = KFSConstants.COMMA + KFSConstants.BLANK_SPACE;
        String fullName = lastName + seperator + firstName;
        if (StringUtils.isNotBlank(middleInitial)) {
            fullName = fullName + seperator + middleInitial + KFSConstants.DELIMITER;
        }
        if(fullName.length() > getPayeeNameFieldSize()) {
            fullName = StringUtils.substring(fullName, 0, getPayeeNameFieldSize());
            fullName = removeLastCharacterWhenComma(fullName);
        }
        return fullName;
    }

    private String removeLastCharacterWhenComma(String fullName) {
        if (fullName.lastIndexOf(KFSConstants.COMMA) >= getPayeeNameFieldSize()-2) {
            fullName = fullName.substring(0, fullName.lastIndexOf(KFSConstants.COMMA));
        }
        return fullName;
    }

    private boolean marshalPdpFeedFle(PdpFeedFileBaseEntry cdpFeedFileBaseEntry, String outputFilePath) {
        boolean success = true;
        try {
            File pdpFeedFile = getCuMarshalService().marshalObjectToXML(cdpFeedFileBaseEntry, outputFilePath);
            success = true;
        } catch (JAXBException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOG.error("There was an error marshalling the PDP feed file.", e);
            success = false;
        }
        return success;
    }

    @Override
    public boolean extractCollectorFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        return true;
    }

    public String getReimbursementFeedDirectory() {
        return reimbursementFeedDirectory;
    }

    public void setReimbursementFeedDirectory(String reimbursementFeedDirectory) {
        this.reimbursementFeedDirectory = reimbursementFeedDirectory;
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

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public Integer getPayeeNameFieldSize() {
        if (payeeNameFieldSize == null) {
            payeeNameFieldSize = getDataDictionaryService().getAttributeMaxLength(PaymentGroup.class, PdpConstants.PaymentDetail.PAYEE_NAME);
        }
        LOG.info("getPayeeNameFieldSizem return " + payeeNameFieldSize.intValue());
        return payeeNameFieldSize;
    }

    public void setPayeeNameFieldSize(Integer payeeNameFieldSize) {
        this.payeeNameFieldSize = payeeNameFieldSize;
    }

}
