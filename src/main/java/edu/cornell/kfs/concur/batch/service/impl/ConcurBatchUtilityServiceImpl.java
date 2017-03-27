package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.Collections;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class ConcurBatchUtilityServiceImpl implements ConcurBatchUtilityService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurBatchUtilityServiceImpl.class);
    protected BatchInputFileService batchInputFileService;
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected FileStorageService fileStorageService;
    protected CUMarshalService cuMarshalService;
    protected ParameterService parameterService;

    public String getConcurParamterValue(String parameterName) {
        String parameterValue = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        return parameterValue;
    }
    
    public void createDoneFileForPdpFile(String fullyQualifiedPdpFileName) throws FileStorageException {
        getFileStorageService().createDoneFile(fullyQualifiedPdpFileName);
    }
    
    public void removeDoneFiles(String requestExtractFullyQualifiedFileName) {
        getFileStorageService().removeDoneFiles(Collections.singletonList(requestExtractFullyQualifiedFileName));
    }

    public String formatPdpPayeeName(String lastName, String firstName, String middleInitial) {
        String fullName = lastName + KFSConstants.COMMA + KFSConstants.BLANK_SPACE + firstName;
        if (StringUtils.isNotBlank(middleInitial)) {
            fullName = fullName + KFSConstants.BLANK_SPACE + middleInitial + KFSConstants.DELIMITER;
        }

        int payeeNameFieldSize = (getDataDictionaryService().getAttributeMaxLength(PaymentGroup.class, PdpConstants.PaymentDetail.PAYEE_NAME)).intValue();
        if (fullName.length() > payeeNameFieldSize) {
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

    public String formatSourceDocumentNumber(String documentTypeCode, String concurDocumentId) {
        String sourceDocNumber = new String(documentTypeCode + concurDocumentId);
        return StringUtils.substring(sourceDocNumber, 0, ConcurConstants.SOURCE_DOCUMENT_NUMBER_FIELD_SIZE);
    }

    public String formatDate(Date date) {
        return getDateTimeService().toString(date, ConcurConstants.DATE_FORMAT);
    }

    public String buildFullyQualifiedPdpOutputFileName(String paymentImportDirecotry, String pdpInputfileName) {
        String fullyQualifiedPdpOutputFileName = new String(paymentImportDirecotry + ConcurConstants.PDP_CONCUR_OUTPUT_FILE_NAME_PREFIX + pdpInputfileName);
        return StringUtils.replace(fullyQualifiedPdpOutputFileName, GeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION, ConcurConstants.XML_FILE_EXTENSION);
    }

    public boolean createPdpFeedFile(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, String fullyQualifiedPdpFileName) {
        boolean success = true;
        try {
            File pdpFeedFile = getCuMarshalService().marshalObjectToXML(pdpFeedFileBaseEntry, fullyQualifiedPdpFileName);
            LOG.info("Created PDP Feed file: " + fullyQualifiedPdpFileName);
            success = true;
        } catch (JAXBException | IOException e) {
            LOG.error("createPdpFeedFile.marshalObjectToXML: There was an error marshalling the PDP feed file: " + fullyQualifiedPdpFileName, e);
            success = false;
        }
        return success;
    }
    
    public Object loadFile(String fullyQualifiedFileName, BatchInputFileType batchInputFileType) {
        byte[] fileByteContent = safelyLoadFileBytes(fullyQualifiedFileName);
        Object parsedObject = getBatchInputFileService().parse(batchInputFileType, fileByteContent);
        return parsedObject;
    }

    public byte[] safelyLoadFileBytes(String fullyQualifiedFileName) {
        InputStream fileContents;
        byte[] fileByteContent;
        try {
            fileContents = new FileInputStream(fullyQualifiedFileName);
        } catch (FileNotFoundException e1) {
            LOG.error("Batch file not found [" + fullyQualifiedFileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fullyQualifiedFileName + "]. " + e1.getMessage());
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("IO Exception loading: [" + fullyQualifiedFileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fullyQualifiedFileName + "]. " + e1.getMessage());
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        return fileByteContent;
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