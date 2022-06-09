package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentValidationService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class CreateAccountingDocumentServiceImpl implements CreateAccountingDocumentService {
	private static final Logger LOG = LogManager.getLogger(CreateAccountingDocumentServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType accountingDocumentBatchInputFileType;
    protected DocumentService documentService;
    protected FileStorageService fileStorageService;
    protected ConfigurationService configurationService;
    protected CreateAccountingDocumentReportService createAccountingDocumentReportService;
    protected ParameterService parameterService;
    protected CreateAccountingDocumentValidationService createAccountingDocumentValidationService;

    @Override
    public boolean createAccountingDocumentsFromXml() {
        List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(accountingDocumentBatchInputFileType);
        LOG.info("createAccountingDocumentsFromXml: Found " + inputFileNames.size() + " files to process");
        CreateAccountingDocumentLogReport logReport = new CreateAccountingDocumentLogReport();
        
        inputFileNames.stream()
                .forEach(fileName -> processAccountingDocumentFromXml(fileName, logReport));

        LOG.info("createAccountingDocumentsFromXml, files with non business rule errors: " + logReport.getFilesWithNonBusinessRuleFailures());
        LOG.info("createAccountingDocumentsFromXml, files successfully processed: " + logReport.getFilesSuccessfullyProcessed());
        LOG.info("createAccountingDocumentsFromXml: Finished processing all pending accounting document XML files");
        
        return logReport.getFilesWithNonBusinessRuleFailures().isEmpty();
    }

    protected void processAccountingDocumentFromXml(String fileName, CreateAccountingDocumentLogReport logReport) {
        CreateAccountingDocumentReportItem reportItem = new CreateAccountingDocumentReportItem(fileName);
        boolean headerValidationFailed = false;
        String headerValidationErrorMessage = KFSConstants.EMPTY_STRING;
        try {
            LOG.info("processAccountingDocumentFromXml: Started processing accounting document XML file: " + fileName);
            byte[] fileData = LoadFileUtils.safelyLoadFileBytes(fileName);
            AccountingXmlDocumentListWrapper accountingXmlDocuments = (AccountingXmlDocumentListWrapper) batchInputFileService.parse(accountingDocumentBatchInputFileType, fileData);
            reportItem.setReportEmailAddress(accountingXmlDocuments.getReportEmail());
            reportItem.setFileOverview(accountingXmlDocuments.getOverview());
            
            if (createAccountingDocumentValidationService.isValidXmlFileHeaderData(accountingXmlDocuments, reportItem)) {
                int documentCount = accountingXmlDocuments.getDocuments().size();
                LOG.info("processAccountingDocumentFromXml: Found " + documentCount + " documents to process from file: " + fileName);
                
                configureReportItemDataForXmlLoadSuccess(reportItem, documentCount);
                
                accountingXmlDocuments.getDocuments().stream()
                        .forEach(xmlDocument -> processAccountingDocumentEntryFromXml(xmlDocument, reportItem));

            } else {
                LOG.info("processAccountingDocumentFromXml: File failed header data elements validation checks: " + fileName);
                headerValidationFailed = true;
                StringBuilder sb = new StringBuilder();
                sb.append(configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_FAILED_HEADER_VALIDATION));
                sb.append(KFSConstants.NEWLINE).append(KFSConstants.NEWLINE).append(reportItem.getValidationErrorMessage());
                headerValidationErrorMessage = sb.toString();
                LOG.info("processAccountingDocumentFromXml: else clause throwing Exception with error message '" + headerValidationErrorMessage + "'");
                throw new Exception(sb.toString());
            }
            LOG.info("processAccountingDocumentFromXml: Finished processing accounting document XML file: " + fileName);
            
        } catch (Exception e) {
            reportItem.setXmlSuccessfullyLoaded(false);
            reportItem.setNonBusinessRuleFailure(true);
            String reportErrorMessage;
            if (headerValidationFailed) {
                reportErrorMessage =  configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_XML_PROCESSING_ERROR)
                        + KFSConstants.BLANK_SPACE + headerValidationErrorMessage;
            } else {
                reportErrorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_XML_PROCESSING_ERROR)
                        + KFSConstants.BLANK_SPACE + e.getMessage();
            }
            reportItem.setReportItemMessage(reportErrorMessage);
            LOG.error("processAccountingDocumentFromXml: Error processing accounting document XML file", e);
        } finally {
            removeDoneFileQuietly(fileName);
            createAndEmailReport(reportItem);
        }
        if (reportItem.isNonBusinessRuleFailure()) {
            logReport.getFilesWithNonBusinessRuleFailures().add(fileName);
        } else {
            logReport.getFilesSuccessfullyProcessed().add(fileName);
        }
        LOG.info("processAccountingDocumentFromXml: Value of reportItem.isNonBusinessRuleFailure just prior to method return =" + reportItem.isNonBusinessRuleFailure());
    }
    
    private void configureReportItemDataForXmlLoadSuccess(CreateAccountingDocumentReportItem reportItem, int fileDocumentCount) {
        reportItem.setXmlSuccessfullyLoaded(true);
        reportItem.setNumberOfDocumentInFile(fileDocumentCount);
    }

    protected void processAccountingDocumentEntryFromXml(AccountingXmlDocumentEntry accountingXmlDocument, CreateAccountingDocumentReportItem reportItem) {
        CreateAccountingDocumentReportItemDetail reportDetail = new CreateAccountingDocumentReportItemDetail();
        
        if (createAccountingDocumentValidationService.isAllRequiredDataValid(accountingXmlDocument, reportDetail)) {
            reportDetail.setIndexNumber(accountingXmlDocument.getIndex().intValue());
            reportDetail.setDocumentType(accountingXmlDocument.getDocumentTypeCode());
            reportDetail.setDocumentDescription(accountingXmlDocument.getDescription());
            reportDetail.setDocumentExplanation(accountingXmlDocument.getExplanation());
            
            createAndRouteAccountingDocumentFromXml(accountingXmlDocument, reportDetail, reportItem);
        } else {
            reportDetail.setRawDataValidationError(true);
            reportDetail.setRawDocumentData(accountingXmlDocument.toString());
            reportDetail.setSuccessfullyRouted(false);
            reportItem.getDocumentsInError().add(reportDetail);
        }
    }

    protected void createAndRouteAccountingDocumentFromXml(AccountingXmlDocumentEntry accountingXmlDocument, CreateAccountingDocumentReportItemDetail reportDetail, CreateAccountingDocumentReportItem reportItem) {
        try {
            LOG.info("createAndRouteAccountingDocumentFromXml: Started processing accounting document of type: " + accountingXmlDocument.getDocumentTypeCode());
            
            String documentGeneratorBeanName = CuFPConstants.ACCOUNTING_DOCUMENT_GENERATOR_BEAN_PREFIX + accountingXmlDocument.getDocumentTypeCode();
            AccountingDocumentGenerator<? extends AccountingDocument> documentGenerator = findDocumentGenerator(documentGeneratorBeanName);
            AccountingDocument document = documentGenerator.createDocument(this::getNewDocument, accountingXmlDocument);
            document = (AccountingDocument) documentService.routeDocument(
                    document, CuFPConstants.ACCOUNTING_DOCUMENT_XML_ROUTE_ANNOTATION, getAllAdHocRecipients(document));
            
            LOG.info("createAndRouteAccountingDocumentFromXml: Finished processing and routing accounting document "
                    + document.getDocumentNumber() + " of type: " + accountingXmlDocument.getDocumentTypeCode());
            reportDetail.setSuccessfullyRouted(true);
            reportDetail.setDocumentNumber(document.getDocumentNumber());
            reportItem.getDocumentsSuccessfullyRouted().add(reportDetail);
            documentGenerator.handleDocumentWarningMessage(reportDetail);
        } catch (RuntimeException e) {
            reportDetail.setSuccessfullyRouted(false);
            if (e instanceof ValidationException) {
                String errorMessage = buildValidationErrorMessage((ValidationException) e);
                LOG.error("createAndRouteAccountingDocumentFromXml: Could not route accounting document - " + errorMessage);
                reportDetail.setErrorMessage(errorMessage);
            } else {
                LOG.error("createAndRouteAccountingDocumentFromXml: Error processing accounting XML document", e);
                reportDetail.setErrorMessage("Non validation error: " + e.getMessage());
                reportItem.setNonBusinessRuleFailure(true);
            }
            reportItem.getDocumentsInError().add(reportDetail);
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().clearWarningMessages();
        }
    }

    @SuppressWarnings("unchecked")
    protected AccountingDocumentGenerator<? extends AccountingDocument> findDocumentGenerator(String beanName) {
        return SpringContext.getBean(AccountingDocumentGenerator.class, beanName);
    }

    protected Document getNewDocument(Class<? extends Document> documentClass) {
        return documentService.getNewDocument(documentClass);
    }

    protected List<AdHocRouteRecipient> getAllAdHocRecipients(Document document) {
        return Stream.concat(document.getAdHocRoutePersons().stream(), document.getAdHocRouteWorkgroups().stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected String buildValidationErrorMessage(ValidationException validationException) {
        try {
            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();
            return errorMessages.values().stream()
                    .flatMap(List::stream)
                    .map(this::buildValidationErrorMessageForSingleError)
                    .collect(Collectors.joining(
                            KFSConstants.NEWLINE, validationException.getMessage() + KFSConstants.NEWLINE, KFSConstants.NEWLINE));
        } catch (RuntimeException e) {
            LOG.error("buildValidationErrorMessage: Could not build validation error message", e);
            return CuFPConstants.ALTERNATE_BASE_VALIDATION_ERROR_MESSAGE;
        }
    }

    protected String buildValidationErrorMessageForSingleError(ErrorMessage errorMessage) {
        String errorMessageString = configurationService.getPropertyValueAsString(errorMessage.getErrorKey());
        if (StringUtils.isBlank(errorMessageString)) {
            throw new RuntimeException("Cannot find error message for key: " + errorMessage.getErrorKey());
        }
        
        Object[] messageParameters = (Object[]) errorMessage.getMessageParameters();
        if (messageParameters != null && messageParameters.length > 0) {
            return MessageFormat.format(errorMessageString, messageParameters);
        } else {
            return errorMessageString;
        }
    }

    protected void removeDoneFileQuietly(String dataFileName) {
        try {
            fileStorageService.removeDoneFiles(Collections.singletonList(dataFileName));
            LOG.info("removeDoneFileQuietly: Done file removed for file: " + dataFileName);
        } catch (RuntimeException e) {
            LOG.error("removeDoneFileQuietly: Could not delete .done file for accounting document XML", e);
        }
    }
    
    protected void createAndEmailReport(CreateAccountingDocumentReportItem reportItem) {
        createAccountingDocumentReportService.generateReport(reportItem);
        String toAddress;
        String fromAddress = getCreateAccountingDocumentReportEmailAddress();
        if (reportItem.isXmlSuccessfullyLoaded()) {
            toAddress = reportItem.getReportEmailAddress();
        } else {
            toAddress = fromAddress;
        }
        createAccountingDocumentReportService.sendReportEmail(toAddress, fromAddress);
        LOG.info("createAndEmailReport: Report created and emailed.");
        
        if (reportItem.doWarningMessagesExist()) {
            reportItem.getDocumentTypeWarningMessageCountMap().keySet().stream()
                .forEach(key -> sendWarningEmail(key, fromAddress));
        }
    }
    
    protected String getCreateAccountingDocumentReportEmailAddress() {
        return parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS);
    }
    
    protected void sendWarningEmail(String docType, String fromAddress) {
        LOG.info("sendWarningEmail, send warning email for doc type " + docType);
        try {
            String toAddress = parameterService.getSubParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                    CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME, 
                    CuFPParameterConstants.CreateAccountingDocumentService.WARNING_EMAIL_ADDRESS, docType);
            if (StringUtils.isNotBlank(toAddress)) {
                LOG.info("sendWarningEmail. sending report to " + toAddress);
                createAccountingDocumentReportService.sendReportEmail(toAddress, fromAddress);
            } else {
                LOG.error("sendWarningEmail, No warning email address for " + docType);
            }
        } catch (Exception e) {
            LOG.error("sendWarningEmail, unable to send warning email for document type " + docType, e);
        }
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setAccountingDocumentBatchInputFileType(BatchInputFileType accountingDocumentBatchInputFileType) {
        this.accountingDocumentBatchInputFileType = accountingDocumentBatchInputFileType;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setCreateAccountingDocumentReportService(
            CreateAccountingDocumentReportService createAccountingDocumentReportService) {
        this.createAccountingDocumentReportService = createAccountingDocumentReportService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setCreateAccountingDocumentValidationService(
            CreateAccountingDocumentValidationService createAccountingDocumentValidationService) {
        this.createAccountingDocumentValidationService = createAccountingDocumentValidationService;
    }

    protected class CreateAccountingDocumentLogReport {
        private List<String> filesWithNonBusinessRuleFailures;
        private List<String> filesSuccessfullyProcessed;
        
        public CreateAccountingDocumentLogReport() {
            filesWithNonBusinessRuleFailures = new ArrayList<String>();
            filesSuccessfullyProcessed = new ArrayList<String>();
        }

        public List<String> getFilesWithNonBusinessRuleFailures() {
            return filesWithNonBusinessRuleFailures;
        }

        public List<String> getFilesSuccessfullyProcessed() {
            return filesSuccessfullyProcessed;
        }
    }

}
