package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;
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
    protected ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService;
    
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
            PdpFeedFileBaseEntry pdpFeedFileBaseEntry = buildPdpFeedFileBaseEntry(concurStandardAccountingExtractFile);
            String outputFilePath = getReimbursementFeedDirectory() + "foo.xml";
            success = marshalPdpFeedFle(pdpFeedFileBaseEntry, outputFilePath);
        }
        
        return success;
    }

    private PdpFeedFileBaseEntry buildPdpFeedFileBaseEntry(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        KualiDecimal pdpTotal = KualiDecimal.ZERO;
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = new PdpFeedFileBaseEntry();
        pdpFeedFileBaseEntry.setVersion("1.0");
        pdpFeedFileBaseEntry.setHeader(buildPdpFeedHeaderEntry(concurStandardAccountingExtractFile.getBatchDate()));
        
        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) {
            if (StringUtils.equalsIgnoreCase(line.getPaymentCode(), ConcurConstants.StandardAccountingExtractPdpConstants.PAYMENT_CODE_CASH)) {
                if (getConcurStandardAccountingExtractValidationService().validateConcurStandardAccountingExtractDetailLine(line)) {
                    PdpFeedGroupEntry currentGroup = getGroupForLine(pdpFeedFileBaseEntry, line);
                    PdpFeedDetailEntry currentDetail = getDetailEntryForLine(currentGroup, line);
                    PdpFeedAccountingEntry currentAccounting = getAccountingEntryForLine(currentDetail, line);
                    
                    pdpTotal = pdpTotal.add(line.getJournalAmount());
                    String newAmount = addAmounts(currentAccounting.getAmount(), line.getJournalAmount());
                    currentAccounting.setAmount(newAmount);
                }
            }
        }
        pdpFeedFileBaseEntry.setTrailer(buildPdpFeedTrailerEntry(pdpFeedFileBaseEntry, pdpTotal));
        return pdpFeedFileBaseEntry;
    }
    
    private PdpFeedGroupEntry getGroupForLine(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, ConcurStandardAccountingExtractDetailLine line) {
        for (PdpFeedGroupEntry groupEntry : pdpFeedFileBaseEntry.getGroup()) {
            if (StringUtils.equalsIgnoreCase(line.getEmployeeId(), groupEntry.getPayeeId().getContent())) {
                return groupEntry;
            }
        }
        PdpFeedGroupEntry group = buildPdpFeedGroupEntry(line);
        pdpFeedFileBaseEntry.getGroup().add(group);
        return group;
    }
    
    private PdpFeedDetailEntry getDetailEntryForLine(PdpFeedGroupEntry groupEntry, ConcurStandardAccountingExtractDetailLine line) {
        for (PdpFeedDetailEntry detailEntry : groupEntry.getDetail()) {
            if (StringUtils.equalsIgnoreCase(detailEntry.getSourceDocNbr(), buildSourceDocumentNumber(line.getReportId()))) {
                return detailEntry;
            }
        }
        PdpFeedDetailEntry detailEntry = buildPdpFeedDetailEntry(line);
        groupEntry.getDetail().add(detailEntry);
        return detailEntry;
    }
    
    private PdpFeedAccountingEntry getAccountingEntryForLine(PdpFeedDetailEntry detailEntry, ConcurStandardAccountingExtractDetailLine line) {
        for (PdpFeedAccountingEntry accountingEntry : detailEntry.getAccounting()) {
            if (isCurrentAccountingEntrySameAsLineDetail(accountingEntry, line)) {
                return accountingEntry;
            }
        }
        PdpFeedAccountingEntry accountingEntry = buildPdpFeedAccountingEntry(line);
        detailEntry.getAccounting().add(accountingEntry);
        return accountingEntry;
    }
    

    private PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedGroupEntry currentGroup;
        currentGroup = new PdpFeedGroupEntry();
        currentGroup.setPayeeName(buildPayeeName(line.getEmployeeLastName(), line.getEmployeeFirstName(), 
                line.getEmployeeMiddleInitital()));
        currentGroup.setPayeeId(buildPayeeIdEntry(line));
        currentGroup.setPaymentDate(formatDate(line.getBatchDate()));
        currentGroup.setCombineGroupInd(ConcurConstants.StandardAccountingExtractPdpConstants.COMBINDED_GROUP_INDICATOR);
        currentGroup.setBankCode(ConcurConstants.StandardAccountingExtractPdpConstants.BANK_CODE);
        return currentGroup;
    }

    private PdpFeedDetailEntry buildPdpFeedDetailEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedDetailEntry currentDetailEntry;
        currentDetailEntry = new PdpFeedDetailEntry();
        currentDetailEntry.setSourceDocNbr(buildSourceDocumentNumber(line.getReportId()));
        currentDetailEntry.setFsOriginCd(ConcurConstants.StandardAccountingExtractPdpConstants.FS_ORIGIN_CODE);
        currentDetailEntry.setFdocTypCd(ConcurConstants.StandardAccountingExtractPdpConstants.DOCUMENT_TYPE);
        /**
         * @todo verify these three, not in the documentation, but looks like needed
         */
        currentDetailEntry.setInvoiceNbr(line.getReportId());
        currentDetailEntry.setInvoiceDate(formatDate(line.getBatchDate()));
        currentDetailEntry.setOrigInvoiceAmt(new Double(0));
        return currentDetailEntry;
    }

    private PdpFeedAccountingEntry buildPdpFeedAccountingEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedAccountingEntry currentAccountingEntry;
        currentAccountingEntry =  new PdpFeedAccountingEntry();
        currentAccountingEntry.setCoaCd(line.getChartOfAccountsCode());
        currentAccountingEntry.setAccountNbr(line.getAccountNumber());
        currentAccountingEntry.setObjectCd(line.getJournalAccountCode());
        currentAccountingEntry.setSubObjectCd(line.getSubObjectCode());
        currentAccountingEntry.setOrgRefId(line.getOrgRefId());
        currentAccountingEntry.setProjectCd(line.getProjectCode());
        currentAccountingEntry.setAmount(KualiDecimal.ZERO.toString());
        return currentAccountingEntry;
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
        String sourceDocNumber = ConcurConstants.StandardAccountingExtractPdpConstants.DOCUMENT_TYPE + StringUtils.substring(reportId, 0, 19);
        return sourceDocNumber;
    }

    private PdpFeedPayeeIdEntry buildPayeeIdEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedPayeeIdEntry payeeIdEntry = new PdpFeedPayeeIdEntry();
        payeeIdEntry.setContent(line.getEmployeeId());
        if (StringUtils.equalsIgnoreCase(line.getEmployeeStatus(), ConcurConstants.StandardAccountingExtractPdpConstants.EMPLOYEE_STATUS_CODE)) {
            payeeIdEntry.setIdType(ConcurConstants.StandardAccountingExtractPdpConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else {
            payeeIdEntry.setIdType(ConcurConstants.StandardAccountingExtractPdpConstants.NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        }
        return payeeIdEntry;
    }

    protected PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry value = new PdpFeedHeaderEntry();
        value.setChart(ConcurConstants.StandardAccountingExtractPdpConstants.DEFAULT_CHART_CODE);
        value.setCreationDate(formatDate(batchDate));
        value.setSubUnit(ConcurConstants.StandardAccountingExtractPdpConstants.DEFAULT_SUB_UNIT);
        value.setUnit(ConcurConstants.StandardAccountingExtractPdpConstants.DEFAULT_UNIT);
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
    
    protected String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat(ConcurConstants.DATE_FORMAT);
        return df.format(date);
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
        return payeeNameFieldSize;
    }

    public void setPayeeNameFieldSize(Integer payeeNameFieldSize) {
        this.payeeNameFieldSize = payeeNameFieldSize;
    }

    public ConcurStandardAccountingExtractValidationService getConcurStandardAccountingExtractValidationService() {
        return concurStandardAccountingExtractValidationService;
    }

    public void setConcurStandardAccountingExtractValidationService(
            ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService) {
        this.concurStandardAccountingExtractValidationService = concurStandardAccountingExtractValidationService;
    }

}
