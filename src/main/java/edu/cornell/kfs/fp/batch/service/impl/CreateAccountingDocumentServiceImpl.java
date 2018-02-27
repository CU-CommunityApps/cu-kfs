package edu.cornell.kfs.fp.batch.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;

public class CreateAccountingDocumentServiceImpl implements CreateAccountingDocumentService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CreateAccountingDocumentServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType accountingDocumentBatchInputFileType;
    protected DocumentService documentService;
    protected FileStorageService fileStorageService;
    protected ConfigurationService configurationService;
    protected CreateAccountingDocumentReportService createAccountingDocumentReportService;
    protected ParameterService parameterService;

    @Override
    public void createAccountingDocumentsFromXml() {
        List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(accountingDocumentBatchInputFileType);
        LOG.info("createAccountingDocumentsFromXml: Found " + inputFileNames.size() + " files to process");
        
        inputFileNames.stream()
                .forEach(this::processAccountingDocumentFromXml);
        
        LOG.info("createAccountingDocumentsFromXml: Finished processing all pending accounting document XML files");
    }

    protected void processAccountingDocumentFromXml(String fileName) {
        CreateAccountingDocumentReportItem reportItem = new CreateAccountingDocumentReportItem(fileName);
        try {
            LOG.info("processAccountingDocumentFromXml: Started processing accounting document XML file: " + fileName);
            
            byte[] fileData = safelyLoadFileBytes(fileName);
            AccountingXmlDocumentListWrapper accountingXmlDocuments = (AccountingXmlDocumentListWrapper) batchInputFileService.parse(
                    accountingDocumentBatchInputFileType, fileData);
            int documentCount = accountingXmlDocuments.getDocuments().size();
            LOG.info("processAccountingDocumentFromXml: Found " + documentCount + " documents to process from file: " + fileName);
            reportItem.setXmlSuccessfullyLoaded(true);
            reportItem.setNumberOfDocumentInFile(documentCount);
            reportItem.setReportEmailAddress(accountingXmlDocuments.getReportEmail());
            reportItem.setFileOverview(accountingXmlDocuments.getOverview());
            accountingXmlDocuments.getDocuments().stream()
                    .forEach(xmlDocument -> processAccountingDocumentEntryFromXml(xmlDocument, reportItem));
            
            LOG.info("processAccountingDocumentFromXml: Finished processing accounting document XML file: " + fileName);
        } catch (Exception e) {
            reportItem.setXmlSuccessfullyLoaded(false);
            reportItem.setReportItemMessage(configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_XML_PROCESING_ERROR));
            LOG.error("processAccountingDocumentFromXml: Error processing accounting document XML file", e);
        } finally {
            removeDoneFileQuietly(fileName);
            createAndEmailReport(reportItem);
        }
    }

    protected byte[] safelyLoadFileBytes(String fileName) {
        FileInputStream inputStream = null;
        
        try {
            inputStream = new FileInputStream(fileName);
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    protected void processAccountingDocumentEntryFromXml(AccountingXmlDocumentEntry accountingXmlDocument, CreateAccountingDocumentReportItem reportItem) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        CreateAccountingDocumentReportItemDetail detail = new CreateAccountingDocumentReportItemDetail();
        detail.setIndexNumber(accountingXmlDocument.getIndex().intValue());
        detail.setDocumentType(accountingXmlDocument.getDocumentTypeCode());
        detail.setDocumentDescription(accountingXmlDocument.getDescription());
        detail.setDocumentExplanation(accountingXmlDocument.getExplanation());
        try {
            LOG.info("processAccountingDocumentEntryFromXml: Started processing accounting document of type: "
                    + accountingXmlDocument.getDocumentTypeCode());
            
            String documentGeneratorBeanName = CuFPConstants.ACCOUNTING_DOCUMENT_GENERATOR_BEAN_PREFIX + accountingXmlDocument.getDocumentTypeCode();
            AccountingDocumentGenerator<? extends AccountingDocument> documentGenerator = findDocumentGenerator(documentGeneratorBeanName);
            AccountingDocument document = documentGenerator.createDocument(this::getNewDocument, accountingXmlDocument);
            document = (AccountingDocument) documentService.routeDocument(
                    document, CuFPConstants.ACCOUNTING_DOCUMENT_XML_ROUTE_ANNOTATION, getAllAdHocRecipients(document));
            
            LOG.info("processAccountingDocumentEntryFromXml: Finished processing and routing accounting document " + document.getDocumentNumber()
                    + " of type: " + accountingXmlDocument.getDocumentTypeCode());
            detail.setSuccessfullyRouted(true);
            detail.setDocumentNumber(document.getDocumentNumber());
            reportItem.getDocumentsSuccessfullyRouted().add(detail);
        } catch (Exception e) {
            detail.setSuccessfullyRouted(false);
            if (e instanceof ValidationException) {
                String errorMessage = buildValidationErrorMessage((ValidationException) e);
                LOG.error("processAccountingDocumentEntryFromXml: Could not route accounting document - "
                        + errorMessage);
                detail.setErrorMessage(errorMessage);
            } else {
                LOG.error("processAccountingDocumentEntryFromXml: Error processing accounting XML document", e);
                detail.setErrorMessage("Non validation error: " + e.getMessage());
            }
            reportItem.getDocumentsInError().add(detail);
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
    }

    @SuppressWarnings("unchecked")
    protected AccountingDocumentGenerator<? extends AccountingDocument> findDocumentGenerator(String beanName) {
        return SpringContext.getBean(AccountingDocumentGenerator.class, beanName);
    }

    protected Document getNewDocument(Class<? extends Document> documentClass) {
        try {
            return documentService.getNewDocument(documentClass);
        } catch (WorkflowException e) {
            throw new RuntimeException(e);
        }
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
    }
    
    protected String getCreateAccountingDocumentReportEmailAddress() {
        return parameterService.getParameterValueAsString(KFSConstants.ParameterNamespaces.FINANCIAL, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS);
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

}
