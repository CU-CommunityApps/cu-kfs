package edu.cornell.kfs.concur.batch.service.impl;

import java.io.IOException;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.util.LoadFileUtils;
import jakarta.xml.bind.JAXBException;

public class ConcurBatchUtilityServiceImpl implements ConcurBatchUtilityService {
    private static final Logger LOG = LogManager.getLogger(ConcurBatchUtilityServiceImpl.class);
    protected BatchInputFileService batchInputFileService;
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected FileStorageService fileStorageService;
    protected CUMarshalService cuMarshalService;
    protected ParameterService parameterService;
    
    @Override
    public String getConcurParameterValue(String parameterName) {
        String parameterValue = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        return parameterValue;
    }
    
    @Override
    public void setConcurParameterValue(String parameterName, String parameterValue) {
        if (StringUtils.isBlank(parameterName)) {
            throw new IllegalArgumentException("parameterName cannot be blank");
        }
        String preparedValue = StringUtils.defaultIfBlank(parameterValue, KFSConstants.EMPTY_STRING);
        Parameter parameter = parameterService.getParameter(CUKFSConstants.ParameterNamespaces.CONCUR,
                CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        parameter.setValue(preparedValue);
        parameterService.updateParameter(parameter);
    }
    
    @Override
    public void createDoneFileFor(String fullyQualifiedFileName) throws FileStorageException {
        getFileStorageService().createDoneFile(fullyQualifiedFileName);
    }
    
    @Override
    public void removeDoneFileFor(String fullyQualifiedFileName) throws FileStorageException {
        getFileStorageService().removeDoneFiles(Collections.singletonList(fullyQualifiedFileName));
    }
    
    @Override
    public String formatPdpPayeeName(String lastName, String firstName, String middleInitial) {
        String fullName = lastName + KFSConstants.COMMA + KFSConstants.BLANK_SPACE + firstName;
        if (StringUtils.isNotBlank(middleInitial)) {
            fullName = fullName + KFSConstants.BLANK_SPACE + middleInitial + KFSConstants.DELIMITER;
        }

        Integer payeeNameFieldSize = getDataDictionaryService().getAttributeMaxLength(PaymentGroup.class, PdpConstants.PaymentDetail.PAYEE_NAME);
        if (fullName.length() > payeeNameFieldSize.intValue()) {
            fullName = StringUtils.substring(fullName, 0, payeeNameFieldSize);
            fullName = removeLastPayeeNameCharacterWhenComma(fullName, payeeNameFieldSize);
        }
        return fullName;
    }

    private String removeLastPayeeNameCharacterWhenComma(String fullName, int payeeNameFieldSize) {
        if (fullName.lastIndexOf(KFSConstants.COMMA) >= payeeNameFieldSize-2) {
            fullName = fullName.substring(0, fullName.lastIndexOf(KFSConstants.COMMA));
        }
        return fullName;
    }
    
    @Override
    public String formatSourceDocumentNumber(String documentTypeCode, String concurDocumentId) {
        String sourceDocNumber = (documentTypeCode + concurDocumentId);
        return StringUtils.substring(sourceDocNumber, 0, ConcurConstants.SOURCE_DOCUMENT_NUMBER_FIELD_SIZE);
    }
    
    @Override
    public String formatDate_MMddyyyy(Date date) {
        return getDateTimeService().toString(date, ConcurConstants.DATE_FORMAT);
    }
    
    @Override
    public String buildFullyQualifiedPdpCashAdvanceOutputFileName(String paymentImportDirectory, String pdpInputfileName) {
        String fullyQualifiedPdpOutputFileName = new String(paymentImportDirectory + ConcurConstants.PDP_CONCUR_CASH_ADVANCE_OUTPUT_FILE_NAME_PREFIX + pdpInputfileName);
        return StringUtils.replace(fullyQualifiedPdpOutputFileName, CuGeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION, ConcurConstants.XML_FILE_EXTENSION);
    }
    
    @Override
    public boolean createPdpFeedFile(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, String fullyQualifiedPdpFileName) {
        boolean success = true;
        try {
            getCuMarshalService().marshalObjectToXML(pdpFeedFileBaseEntry, fullyQualifiedPdpFileName);
            LOG.info("createPdpFeedFile:  Created PDP Feed file: " + fullyQualifiedPdpFileName);
            success = true;
        } catch (JAXBException | IOException e) {
            LOG.error("createPdpFeedFile.marshalObjectToXML: There was an error marshalling the PDP feed file: " + fullyQualifiedPdpFileName, e);
            success = false;
        }
        return success;
    }
    
    @Override
    public Object loadFile(String fullyQualifiedFileName, BatchInputFileType batchInputFileType) {
        byte[] fileByteContent = LoadFileUtils.safelyLoadFileBytes(fullyQualifiedFileName);
        Object parsedObject = getBatchInputFileService().parse(batchInputFileType, fileByteContent);
        return parsedObject;
    }

    @Override
    public boolean lineRepresentsPersonalExpenseChargedToCorporateCard(ConcurStandardAccountingExtractDetailLine line) {
        return Boolean.TRUE.equals(line.getReportEntryIsPersonalFlag())
                && StringUtils.equals(ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID, line.getPaymentCode());
    }
    
    @Override
    public boolean lineRepresentsReturnOfCorporateCardPersonalExpenseToUser(ConcurStandardAccountingExtractDetailLine line) {
        return lineRepresentsPersonalExpenseChargedToCorporateCard(line)
                && StringUtils.equalsIgnoreCase(ConcurConstants.DEBIT, line.getJournalDebitCredit())
                && StringUtils.equalsIgnoreCase(ConcurConstants.UNIVERSITY_PAYMENT_TYPE, line.getJournalPayerPaymentTypeName())
                && StringUtils.equalsIgnoreCase(ConcurConstants.USER_PAYMENT_TYPE, line.getJournalPayeePaymentTypeName());
    }
    
    @Override
    public boolean lineRepresentsReturnOfCorporateCardPersonalExpenseToUniversity(ConcurStandardAccountingExtractDetailLine line) {
        return lineRepresentsPersonalExpenseChargedToCorporateCard(line)
                && StringUtils.equalsIgnoreCase(ConcurConstants.CREDIT, line.getJournalDebitCredit())
                && StringUtils.equalsIgnoreCase(ConcurConstants.CORPORATE_CARD_PAYMENT_TYPE, line.getJournalPayerPaymentTypeName())
                && StringUtils.equalsIgnoreCase(ConcurConstants.UNIVERSITY_PAYMENT_TYPE, line.getJournalPayeePaymentTypeName());
    }

    @Override
    public String getFileContents(String fileName) {
        try {
            byte[] fileByteArray = LoadFileUtils.safelyLoadFileBytes(fileName);
            String formattedString = new String(fileByteArray);
            return formattedString;
        } catch (RuntimeException e) {
            LOG.error("getFileContents, unable to read the file.", e);
            return StringUtils.EMPTY;
        }
    }

    @Override
    public boolean isValidTravelerStatusForProcessingAsPDPEmployeeType(String status) {
        String validStatusesString = getConcurParameterValue(ConcurParameterConstants.CONCUR_VALID_TRAVELER_STATUSES_FOR_PDP_EMPLOYEE_PROCESSING);
        String[] validStatuses = StringUtils.split(validStatusesString, CUKFSConstants.SEMICOLON);
        return Arrays.stream(validStatuses)
                .anyMatch((validStatusValue) -> StringUtils.equalsIgnoreCase(validStatusValue, status));
    }
    
    @Override
    public boolean shouldProcessRequestedCashAdvancesFromSaeData() {
        return (StringUtils.equalsIgnoreCase(getConcurParameterValue(ConcurParameterConstants.CONCUR_PROCESS_CASH_ADVANCES_FROM_SAE_DATA_IND), KFSConstants.ParameterValues.YES));
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public CUMarshalService getCuMarshalService() {
        return cuMarshalService;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

}