package edu.cornell.kfs.sys.batch.service.impl;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.batch.service.CreateKualiDeveloperXmlService;
import edu.cornell.kfs.sys.batch.xml.KualiDeveloperXmlEntry;
import edu.cornell.kfs.sys.batch.xml.KualiDeveloperXmlListWrapper;
import edu.cornell.kfs.sys.businessobject.KualiDeveloper;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.springframework.util.AutoPopulatingList;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateKualiDeveloperXmlServiceImpl implements CreateKualiDeveloperXmlService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CreateKualiDeveloperXmlServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private BatchInputFileType kualiDeveloperBatchInputFileType;
    private FileStorageService fileStorageService;
    private ConfigurationService configurationService;
    protected BusinessObjectService businessObjectService;

    @Override
    public void createKualiDevelopersFromXml() {
        List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(kualiDeveloperBatchInputFileType);
        LOG.info("createKualiDevelopersFromXml: Found " + inputFileNames.size() + " files to process");
        
        inputFileNames.stream().forEach(this::processKualiDevelopersFromXml);
        
        LOG.info("createKualiDevelopersFromXml: Finished processing all pending kuali developer XML files");
    }

    protected void processKualiDevelopersFromXml(String fileName) {
        try {
            LOG.info("processKualiDevelopersFromXml: Started processing kuali developer XML file: " + fileName);
            
            byte[] fileData = CuBatchFileUtils.safelyLoadFileBytes(fileName);
            KualiDeveloperXmlListWrapper kualiDeveloperXmlList = (KualiDeveloperXmlListWrapper) batchInputFileService.parse(
                    kualiDeveloperBatchInputFileType, fileData);
            int developerCount = kualiDeveloperXmlList.getKualiDevelopers().size();
            LOG.info("processKualiDevelopersFromXml: Found " + developerCount + " documents to process from file: " + fileName);

            kualiDeveloperXmlList.getKualiDevelopers().stream().forEach(this::processKualiDeveloperFromXml);
            LOG.info("processKualiDevelopersFromXml: Finished processing kuali developer XML file: " + fileName);
        }
        catch (Exception e) {
            LOG.error("processKualiDevelopersFromXml: Error processing kuali developer XML file", e);
        }
        finally {
            removeDoneFileQuietly(fileName);
        }
    }

    protected void processKualiDeveloperFromXml(KualiDeveloperXmlEntry kualiDeveloperXmlEntry) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        try {
            LOG.info("processKualiDeveloperFromXml: Started processing kuali developer : " + kualiDeveloperXmlEntry.getEmployeeId());
            KualiDeveloper kualiDeveloper = new KualiDeveloper();
            kualiDeveloper.setEmployeeId(kualiDeveloperXmlEntry.getEmployeeId());
            kualiDeveloper.setFirstName(kualiDeveloperXmlEntry.getFirstName());
            kualiDeveloper.setLastName(kualiDeveloperXmlEntry.getLastName());
            kualiDeveloper.setPositionName(kualiDeveloperXmlEntry.getPositionName());

            KualiDeveloper retrievedEntry = (KualiDeveloper) businessObjectService.retrieve(kualiDeveloper);
            if (ObjectUtils.isNotNull(retrievedEntry)) {
                kualiDeveloper.setVersionNumber(retrievedEntry.getVersionNumber());
            }
            businessObjectService.save(kualiDeveloper);

            LOG.info("processKualiDeveloperFromXml: Finished processing kuali developer " + kualiDeveloperXmlEntry.getEmployeeId());
        }
        catch (ValidationException ve){
            LOG.error("processKualiDeveloperFromXml: Could not create kuali developer - " + buildValidationErrorMessage(ve));
        }
        catch (Exception e) {
            LOG.error("processKualiDeveloperFromXml: Error processing Kuali Developer XML", e);
        }
        finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
    }

    protected String buildValidationErrorMessage(ValidationException validationException) {
        try {
            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();
            return errorMessages.values().stream()
                    .flatMap(List::stream)
                    .map(this::buildValidationErrorMessageForSingleError)
                    .collect(Collectors.joining(
                            KFSConstants.NEWLINE, validationException.getMessage() + KFSConstants.NEWLINE, KFSConstants.NEWLINE));
        }
        catch (RuntimeException e) {
            LOG.error("buildValidationErrorMessage: Could not build validation error message", e);
            return CuFPConstants.ALTERNATE_BASE_VALIDATION_ERROR_MESSAGE;
        }
    }

    protected String buildValidationErrorMessageForSingleError(ErrorMessage errorMessage) {
        String errorMessageString = configurationService.getPropertyValueAsString(errorMessage.getErrorKey());
        if (StringUtils.isBlank(errorMessageString)) {
            throw new RuntimeException("Cannot find error message for key: " + errorMessage.getErrorKey());
        }
        
        String[] messageParameters = errorMessage.getMessageParameters();
        if (ObjectUtils.isNotNull(messageParameters) && messageParameters.length > 0) {
            return MessageFormat.format(errorMessageString, messageParameters);
        }
        else {
            return errorMessageString;
        }
    }

    protected void removeDoneFileQuietly(String dataFileName) {
        try {
            fileStorageService.removeDoneFiles(Collections.singletonList(dataFileName));
        }
        catch (RuntimeException e) {
            LOG.error("removeDoneFileQuietly: Could not delete .done file for kuali developer XML", e);
        }
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setKualiDeveloperBatchInputFileType(BatchInputFileType kualiDeveloperBatchInputFileType) {
        this.kualiDeveloperBatchInputFileType = kualiDeveloperBatchInputFileType;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
