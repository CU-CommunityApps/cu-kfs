package edu.cornell.kfs.fp.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.businessobject.AmazonBillResultsDTO;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.service.CloudCheckrService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.service.AmazonWebServicesBillingService;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class AmazonWebServicesBillingServiceImpl implements AmazonWebServicesBillingService, Serializable {

    private static final long serialVersionUID = 7430710204404759511L;
    private static final Logger LOG = LogManager.getLogger(AmazonWebServicesBillingServiceImpl.class);
    
    private String directoryPath;
    
    protected ParameterService parameterService;
    protected DocumentService documentService;
    protected BusinessObjectService businessObjectService;
    protected CloudCheckrService cloudCheckrService;
    protected AwsAccountingXmlDocumentAccountingLineService awsAccountingXmlDocumentAccountingLineService;
    protected CUMarshalService cuMarshalService;
    protected FileStorageService fileStorageService;
    protected ConfigurationService configurationService;
    protected DataDictionaryService dataDictionaryService;
    protected EmailService emailService;


    @Override
    public void generateDistributionOfIncomeDocumentsFromAWSService() {
        String startMessageBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.MESSAGE_AWS_BILLING_JOB_START_MESSAGE);
        boolean totalJobSuccess = true;
        LOG.info("generateDistributionOfIncomeDocumentsFromAWSService, " + 
                MessageFormat.format(startMessageBase, findProcessYear(), findMonthName(), findStartDate(), findEndDate()));
        DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper = null;
        List<AmazonBillResultsDTO> resultsDTOs = new ArrayList<AmazonBillResultsDTO>();
        
        try {
            defaultAccountWrapper = cloudCheckrService.getDefaultKfsAccountForAwsResultWrapper();
            if (LOG.isDebugEnabled()) {
                LOG.info("generateDistributionOfIncomeDocumentsFromAWSService, defaultAccountWrapper: " + defaultAccountWrapper.toString());
            }
        } catch (URISyntaxException | IOException e) {
            LOG.error("generateDistributionOfIncomeDocumentsFromAWSService, Unable to call default account service.", e);
            totalJobSuccess = false;
        }
        
        Map<String, String> masterAccountMap = buildMasterAccountMap();
        
        if (ObjectUtils.isNotNull(defaultAccountWrapper)) {
            for (String masterAccountNumber : masterAccountMap.keySet()) {
                String masterAccountName = masterAccountMap.get(masterAccountNumber);
                AmazonBillResultsDTO resultsDTO = processRootAccount(masterAccountNumber, masterAccountName, defaultAccountWrapper);
                totalJobSuccess &= resultsDTO.successfullyProcessed;
                resultsDTOs.add(resultsDTO);
            }
        }
        
        resultsDTOs.stream().forEach(AmazonBillResultsDTO::logResults);
        
        if (!totalJobSuccess) {
            sendErrorEmailForFailedAccounts(resultsDTOs);
            throw new RuntimeException("There was a problem with Amazon Billing Service");
        }
    }
    
    protected AmazonBillResultsDTO processRootAccount(String masterAccountNumber, String masterAccountName, 
            DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper) {
        AmazonBillResultsDTO resultsDTO = new AmazonBillResultsDTO();
        resultsDTO.masterAccountName = masterAccountName;
        resultsDTO.masterAccountNumber = masterAccountNumber;
        boolean masterAccountSuccess = true;
        CloudCheckrWrapper cloudCheckrWrapper = null;
        try {
            cloudCheckrWrapper = cloudCheckrService.getCloudCheckrWrapper(findStartDate(), findEndDate(), masterAccountName);
        } catch (URISyntaxException | IOException e) {
            StringBuilder errorMessageBuilder = new StringBuilder("processRootAccount, unable to call cloudcheckr endpoint.");
            errorMessageBuilder.append("This exception may print the URL which has an access token in it, so not logging the exception on purpose.");
            errorMessageBuilder.append(" startDate: ").append(findStartDate());
            errorMessageBuilder.append(" endDate: ").append(findEndDate());
            errorMessageBuilder.append(" masterAccountName: ").append(masterAccountName);
            LOG.error(errorMessageBuilder);
            masterAccountSuccess = false;
        }
        if (ObjectUtils.isNotNull(cloudCheckrWrapper) && ObjectUtils.isNotNull(defaultAccountWrapper)) {
            AccountingXmlDocumentListWrapper documentWrapper = buildAccountingXmlDocumentListWrapper(cloudCheckrWrapper, defaultAccountWrapper, 
                    masterAccountNumber, masterAccountName, resultsDTO);
            LOG.info("processRootAccount, built documentWrapper: " + documentWrapper);
            masterAccountSuccess &= generateXML(documentWrapper, masterAccountName);
        } else {
            masterAccountSuccess = false;
            LOG.error("processRootAccount, cloudCheckrWrapper or defaultAccountWrapper was not built, can not generate DIs");
        }
        resultsDTO.successfullyProcessed = masterAccountSuccess;
        return resultsDTO;
    }
    
    protected Map<String, String> buildMasterAccountMap() {
        String cornellMasterAccounts = getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_CORNELL_MASTER_ACCOUNTS_PARAMETER_NAME);
        Map<String, String> masterAccountMap = new HashMap<String, String>();
        for (String masterAccountNameSet : StringUtils.split(cornellMasterAccounts, ";")) {
            String[] master = StringUtils.split(masterAccountNameSet, "=");
            masterAccountMap.put(master[0], master[1]);
            
        }
        logMasterAccountMap(masterAccountMap);
        return masterAccountMap;
    }
    
    private void logMasterAccountMap(Map<String, String> masterAccountMap) {
        for (String key : masterAccountMap.keySet()) {
            LOG.info("logMasterAccountMap, Account Number: " + key + " Account Name: " + masterAccountMap.get(key));
        }
    }
    
    protected AccountingXmlDocumentListWrapper buildAccountingXmlDocumentListWrapper(CloudCheckrWrapper cloudCheckrWrapper, 
            DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper, String masterAccountNumber, String masterAccountName, AmazonBillResultsDTO resultsDTO) {
        
        AccountingXmlDocumentListWrapper documentWrapper = new AccountingXmlDocumentListWrapper();
        documentWrapper.setCreateDate(Calendar.getInstance().getTime());
        documentWrapper.setReportEmail(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_ADMIN_EMAIL_PARAMETER_NAME));
        
        String overViewBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_ACCOUNTING_XML_OVERVIEW);
        documentWrapper.setOverview(MessageFormat.format(overViewBase, buildFriendlyAccountName(masterAccountName), findMonthName(), findProcessYear()));
        
        addDocumentsToDocumentWrapper(cloudCheckrWrapper, defaultAccountWrapper, documentWrapper, masterAccountNumber, resultsDTO);
        return documentWrapper;
        
    }
    
    protected String buildFriendlyAccountName(String accountName) {
        return StringUtils.replace(accountName, "+", KFSConstants.BLANK_SPACE);
    }
    
    protected void addDocumentsToDocumentWrapper(CloudCheckrWrapper cloudCheckrWrapper,
            DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper, AccountingXmlDocumentListWrapper documentWrapper,
            String masterAccountNumber, AmazonBillResultsDTO resultsDTO) {
        Long documentIndex = new Long(1);
        
        for (GroupLevel awsAccountGroup : cloudCheckrWrapper.getCostsByGroup()) {
            resultsDTO.numberOfAwsAccountInCloudCheckr++;
            AccountingXmlDocumentEntry document = new AccountingXmlDocumentEntry();
            document.setDocumentTypeCode(CuFPConstants.DI);
            document.setIndex(documentIndex);
            String awsAccount = parseAWSAccountFromCloudCheckrGroupValue(awsAccountGroup.getGroupValue());
            document.setExplanation(buildDocumentExplanation(awsAccount));
            
            String departmentName = parseDepartmentNameFromCloudCheckrGroupValue(awsAccountGroup.getGroupValue());
            document.setDescription(buildDocumentDescription(departmentName));
            
            KualiDecimal targetLineTotal = addTargetLinesToDocument(defaultAccountWrapper, resultsDTO, awsAccountGroup, document, awsAccount);
            
            addSourceLineToDocument(document, targetLineTotal);
            
            addNotesToDocument(document);
            
            addBackupDocumentLinksToDocument(document, awsAccount, departmentName, masterAccountNumber);
            
            if (shouldBuildDistributionOfIncomeDocument(awsAccount, departmentName, targetLineTotal)) {
                documentWrapper.getDocuments().add(document);
                resultsDTO.xmlCreationCount++;
                resultsDTO.awsAccountGeneratedDIxml.add(awsAccount);
                documentIndex = Long.sum(documentIndex, 1);
            } else {
                resultsDTO.awsAccountWithExistingDI.add(awsAccountGroup.getGroupValue());
            }
            
        }
    }
    
    protected String parseAWSAccountFromCloudCheckrGroupValue(String cloudCheckrAWSAccount) {
        int indexOfFirstOpenParen = StringUtils.indexOf(cloudCheckrAWSAccount, CuFPConstants.LEFT_PARENTHESIS);
        String parsedAWSAccount = StringUtils.substring(cloudCheckrAWSAccount, 0, indexOfFirstOpenParen);
        return StringUtils.trim(parsedAWSAccount);
    }
    
    protected String buildDocumentExplanation(String AWSAccount) {
        String explanationFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_DOCUMENT_EXPLANATION_FORMAT);
        return MessageFormat.format(explanationFormat, AWSAccount);
    }
    
    protected String parseDepartmentNameFromCloudCheckrGroupValue(String cloudCheckrAWSAccount) {
        int indexOfFirstOpenParen = StringUtils.indexOf(cloudCheckrAWSAccount, CuFPConstants.LEFT_PARENTHESIS);
        int indexOfFirstCloseParen = StringUtils.indexOf(cloudCheckrAWSAccount, CuFPConstants.RIGHT_PARENTHESIS);
        String parsedDepartmentName = StringUtils.substring(cloudCheckrAWSAccount, indexOfFirstOpenParen + 1, indexOfFirstCloseParen);
        return StringUtils.trim(parsedDepartmentName);
    }
    
    protected String buildDocumentDescription(String departmentName) {
        String documentDescriptionBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_DOCUMENT_DESCRIPTION_FORMAT);
        String documentDescription = MessageFormat.format(documentDescriptionBase, findMonthName(), findProcessYear(), departmentName);
        int descriptionMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class, KFSPropertyConstants.DOCUMENT_DESCRIPTION);
        return StringUtils.substring(documentDescription, 0, descriptionMaxLength);
    }
    
    protected KualiDecimal addTargetLinesToDocument(DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper,
            AmazonBillResultsDTO resultsDTO, GroupLevel awsAccountGroup, AccountingXmlDocumentEntry document,
            String awsAccount) {
        DefaultKfsAccountForAws defaultKfsAccount = findDefaultKfsAccountForAws(awsAccount, defaultAccountWrapper, resultsDTO);
        KualiDecimal targetLineTotal = KualiDecimal.ZERO;
        for (GroupLevel costCenterGroup : awsAccountGroup.getNextLevel()) {
            if (KualiDecimal.ZERO.isLessThan(costCenterGroup.getCost())) {
                AccountingXmlDocumentAccountingLine targetLine = awsAccountingXmlDocumentAccountingLineService.createAccountingXmlDocumentAccountingLine(costCenterGroup, defaultKfsAccount, resultsDTO);
                targetLineTotal = targetLineTotal.add(targetLine.getAmount());
                document.getTargetAccountingLines().add(targetLine);
            } else {
                LOG.error("addTargetLinesToDocument, found a group with a cost of zero: " + costCenterGroup);
            }
        }
        return targetLineTotal;
    }
    
    private DefaultKfsAccountForAws findDefaultKfsAccountForAws(String awsAccount, DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper, 
            AmazonBillResultsDTO resultsDTO) {
        for (DefaultKfsAccountForAws defaultAccount : defaultAccountWrapper.getDefaultKfsAccountsForAws()) {
            if (StringUtils.equalsIgnoreCase(awsAccount, defaultAccount.getAwsAccount())) {
                return defaultAccount;
            }
        }
        resultsDTO.awsAccountWithoutDefaultAccount.add(awsAccount);
        return createDefaultKfsAccountForAwsAccount(awsAccount);
    }

    private DefaultKfsAccountForAws createDefaultKfsAccountForAwsAccount(String awsAccount) {
        LOG.error("createDefaultKfsAccountForAwsAccount, no match found for AWS Account " + awsAccount + ", will return a default account to allow further processing.");
        DefaultKfsAccountForAws defaultKfsAccountForAws = new DefaultKfsAccountForAws();
        defaultKfsAccountForAws.setAwsAccount(awsAccount);
        defaultKfsAccountForAws.setKfsDefaultAccount("nodfact");
        defaultKfsAccountForAws.setUpdatedAt(Calendar.getInstance().getTime());
        return defaultKfsAccountForAws;
    }
    
    protected void addSourceLineToDocument(AccountingXmlDocumentEntry document, KualiDecimal targetLineTotal) {
        AccountingXmlDocumentAccountingLine sourceLine = new AccountingXmlDocumentAccountingLine();
        sourceLine.setChartCode(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_CHART_CODE_PARAMETER_NAME));
        sourceLine.setAccountNumber(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_FROM_ACCOUNT_PARAMETER_NAME));
        sourceLine.setObjectCode(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_OBJECT_CODE_PARAMETER_NAME));
        sourceLine.setAmount(targetLineTotal);
        sourceLine.setLineDescription(buildAccountingLineDescription());
        document.getSourceAccountingLines().add(sourceLine);
    }
    
    protected String buildAccountingLineDescription() {
        String descriptionBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_ACCOUNTING_LINE_DESCRIPTION);
        return MessageFormat.format(descriptionBase, findMonthName(), findProcessYear());
    }
    
    protected void addNotesToDocument(AccountingXmlDocumentEntry document) {
        AccountingXmlDocumentNote note = new AccountingXmlDocumentNote();
        note.setDescription(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_NOTE_HELP_TEXT_PARAMETER_NAME));
        document.getNotes().add(note);
    }
    
    protected void addBackupDocumentLinksToDocument(AccountingXmlDocumentEntry document, String awsAccount, String departmentName, String masterAccountNumber) {
        AccountingXmlDocumentBackupLink link = new AccountingXmlDocumentBackupLink();
        link.setCredentialGroupCode(CuFPConstants.AmazonWebServiceBillingConstants.AWS_BILL_CREDENTIAL_GROUP_CODE);

        String fileNameFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_INVOICE_FILE_NAME_FORMAT);
        String noSpacesDepartmentName = StringUtils.replace(departmentName, KRADConstants.BLANK_SPACE, KFSConstants.DASH);
        link.setFileName(MessageFormat.format(fileNameFormat, noSpacesDepartmentName, findMonthName(), findProcessYear()));
        
        String noteTextFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_INVOICE_NOTE_TEXT_FORMAT);
        
        link.setDescription(MessageFormat.format(noteTextFormat, departmentName, findMonthName(), findProcessYear()));
        
        link.setLinkUrl(cloudCheckrService.buildAttachmentUrl(findProcessYear(), findProcessMonthNumber(), awsAccount, masterAccountNumber));
        document.getBackupLinks().add(link);
    }
    
    private boolean shouldBuildDistributionOfIncomeDocument(String awsAccount, String departmentName, KualiDecimal cost) {
        for (DocumentHeader dh : findDocumentHeadersForAmazonDetail(awsAccount, departmentName)) {
            try {
                DistributionOfIncomeAndExpenseDocument di = (DistributionOfIncomeAndExpenseDocument) documentService.getByDocumentHeaderId(dh.getDocumentNumber());
                if (di.getTotalDollarAmount().equals(cost)) {
                    LOG.info("shouldBuildDistributionOfIncomeDocument() DI document number " + di.getDocumentNumber() + 
                            " already exists for Amazon account: " + awsAccount);
                    return false;
                }
            } catch (WorkflowException e) {
                LOG.error("shouldBuildDistributionOfIncomeDocument() There was a problems creating a DI document from a document header: " + dh.getDocumentNumber(), e); 
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    
    private Collection<FinancialSystemDocumentHeader> findDocumentHeadersForAmazonDetail(String awsAccount, String departmentName) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put(KFSPropertyConstants.EXPLANATION, buildDocumentExplanation(awsAccount));
        fieldValues.put(KFSPropertyConstants.DOCUMENT_DESCRIPTION, buildDocumentDescription(departmentName));
        fieldValues.put(KFSPropertyConstants.WORKFLOW_DOCUMENT_TYPE_NAME, CuFPConstants.DI);
        Collection<FinancialSystemDocumentHeader> documentHeaders = businessObjectService.findMatching(FinancialSystemDocumentHeader.class, fieldValues);
        return documentHeaders;
    }
    
    protected boolean generateXML(AccountingXmlDocumentListWrapper documentWrapper, String masterAccountName) {
        boolean success = true;
        String fileNameBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_OUTPUT_FILE_NAME_FORMAT);
        String fileName = MessageFormat.format(fileNameBase, directoryPath, findProcessYear(), findMonthName(), masterAccountName, 
                Calendar.getInstance().getTime().getTime());
        try {
            cuMarshalService.marshalObjectToXML(documentWrapper, fileName);
            fileStorageService.createDoneFile(fileName);
            LOG.info("generateXML, successfully created an XML and DONE file for " + fileName);
        } catch (JAXBException | IOException e) {
            success = false;
            LOG.error("generateXML, unable to marshal documentWrapper to " + fileName, e);
        }
        return success;
    }
    
    private void sendErrorEmailForFailedAccounts(List<AmazonBillResultsDTO> resultsDTOs) {
        Map<String, String> accountsInError = new HashMap<String, String>();
        resultsDTOs.stream()
            .filter(result -> !result.successfullyProcessed)
            .forEach(result -> accountsInError.put(result.masterAccountNumber, result.masterAccountName));
        
        if (!accountsInError.isEmpty()) {
            BodyMailMessage message = new BodyMailMessage();
            message.addToAddress(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_ADMIN_EMAIL_PARAMETER_NAME));
            message.setFromAddress(emailService.getDefaultFromAddress());
            message.setSubject(configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_ERROR_EMAIL_SUBJECT));
            String messageBody = buildErrorEmailMessageBody(accountsInError);
            LOG.error("sendErrorEmailForFailedAccounts. sending error email with the body of " + messageBody);
            message.setMessage(messageBody);
            emailService.sendMessage(message, false);
        } else {
            LOG.info("sendErrorEmailForFailedAccounts, no accounts in error, so email to be sent.");
        }
        
    }

    protected String buildErrorEmailMessageBody(Map<String, String> accountsInError) {
        String bodyFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_ERROR_EMAIL_BODY);
        StringBuilder sb = new StringBuilder();
        boolean needComma = false;
        for (String key : accountsInError.keySet()) {
            if (needComma) {
                sb.append(", ");
            }
            sb.append(key).append(" (account name: ").append(buildFriendlyAccountName(accountsInError.get(key))).append(")");
            needComma = true;
        }
        String emailMessage = MessageFormat.format(bodyFormat, sb.toString());
        return emailMessage;
    }
    
    protected String findStartDate() {
        DateFormat transactionDateFormat = new SimpleDateFormat(CuFPConstants.AmazonWebServiceBillingConstants.DATE_FORMAT);
        Calendar cal = findProcessDate();
        cal.set(Calendar.DATE, 1);
        return transactionDateFormat.format(cal.getTime());
    }
    
    protected String findEndDate() {
        DateFormat transactionDateFormat = new SimpleDateFormat(CuFPConstants.AmazonWebServiceBillingConstants.DATE_FORMAT);
        Calendar cal = findProcessDate();
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DATE, -1);
        return transactionDateFormat.format(cal.getTime());
    }
    
    protected String findProcessYear() {
        return String.valueOf(findProcessDate().get(Calendar.YEAR));
    }
    
    protected String findProcessMonthNumber() {
        return String.valueOf(findProcessDate().get(Calendar.MONTH) + 1);
    }
    
    protected String findMonthName() {
        int monthIndex = findProcessDate().get(Calendar.MONTH);
        return new DateFormatSymbols().getMonths()[monthIndex];
    }
    
    private Calendar findProcessDate() {
        if (StringUtils.equalsIgnoreCase(getBillingPeriodParameterValue(), CuFPConstants.AmazonWebServiceBillingConstants.DEFAULT_BILLING_PERIOD_PARAMETER)) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            return cal;
        } else {
            String year = StringUtils.split(getBillingPeriodParameterValue(), ",")[0];
            String month = StringUtils.split(getBillingPeriodParameterValue(), ",")[1];
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(year), Integer.parseInt(month)-1, CuFPConstants.AmazonWebServiceBillingConstants.PROCESSING_DAY_OF_MONTH);
            return cal;
        }
    }
    
    public String getBillingPeriodParameterValue() {
        String processingDate = getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_PROCESSING_DATE_PARAMETER_NAME);
        String billingPeriodParameterValue = StringUtils.isNotBlank(processingDate) ? processingDate : 
            CuFPConstants.AmazonWebServiceBillingConstants.DEFAULT_BILLING_PERIOD_PARAMETER;
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBillingPeriodParameterValue() The AWS_PROCESSING_DATE parameter value is '" + processingDate + 
                    "' and returning " + billingPeriodParameterValue);
        }
        return billingPeriodParameterValue;
    }

    protected String getAWSParameterValue(String parameterName) {
        String parmValue = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPONENT_NAME, parameterName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAWSParameterValue, parametername: " + parameterName + " value: " + parmValue);
        }
        return parmValue;
    }
    
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
       
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setCloudCheckrService(CloudCheckrService cloudCheckrService) {
        this.cloudCheckrService = cloudCheckrService;
    }

    public void setAwsAccountingXmlDocumentAccountingLineService(
            AwsAccountingXmlDocumentAccountingLineService awsAccountingXmlDocumentAccountingLineService) {
        this.awsAccountingXmlDocumentAccountingLineService = awsAccountingXmlDocumentAccountingLineService;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

}
