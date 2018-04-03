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
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.service.CloudcheckrService;
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
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AmazonWebServicesBillingServiceImpl.class);
    
    private String billingPeriodParameterValue;
    private String directoryPath;
    
    protected ParameterService parameterService;
    protected DocumentService documentService;
    protected BusinessObjectService businessObjectService;
    protected CloudcheckrService cloudcheckrService;
    protected AwsAccountingXmlDocumentAccountingLineService awsAccountingXmlDocumentAccountingLineService;
    protected CUMarshalService cuMarshalService;
    protected FileStorageService fileStorageService;
    protected ConfigurationService configurationService;


    @Override
    public void generateDistributionOfIncomeDocumentsFromAWSService() {
        String startMessageBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.MESSAGE_AWS_BILLING_JOB_START_MESSAGE);
        LOG.info("generateDistributionOfIncomeDocumentsFromAWSService, " + 
                MessageFormat.format(startMessageBase, findProcessYear(), findMonthName(), findStartDate(), findEndDate()));
        CloudCheckrWrapper cloudCheckrWrapper = null;
        DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper = null;
        AmazonBillResultsDTO resultsDTO = new AmazonBillResultsDTO();
        
        boolean success = true;
        
        try {
            cloudCheckrWrapper = cloudcheckrService.getCloudCheckrWrapper(findStartDate(), findEndDate());
            defaultAccountWrapper = cloudcheckrService.getDefaultKfsAccountForAwsResultWrapper();
        } catch (URISyntaxException | IOException exception) {
            LOG.error("unable to call cloud checker service", exception);
            success = false;
        }
        
        if (ObjectUtils.isNotNull(cloudCheckrWrapper) && ObjectUtils.isNotNull(defaultAccountWrapper)) {
            AccountingXmlDocumentListWrapper documentWrapper = buildAccountingXmlDocumentListWrapper(cloudCheckrWrapper, defaultAccountWrapper, resultsDTO);
            LOG.info("generateDistributionOfIncomeDocumentsFromAWSService, built documentWrapper: " + documentWrapper);
            success &= generateXML(documentWrapper);
        } else {
            success = false;
            LOG.error("generateDistributionOfIncomeDocumentsFromAWSService, cloudCheckrWrapper or defaultAccountWrapper was not built, can not generate DIs");
        }
        
        logResults(resultsDTO);
        resetProperties();
        
        if (!success) {
            throw new RuntimeException("There was a problem with Amazon Billing Service");
        }
    }
    
    protected AccountingXmlDocumentListWrapper buildAccountingXmlDocumentListWrapper(CloudCheckrWrapper cloudCheckrWrapper, 
            DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper, AmazonBillResultsDTO resultsDTO) {
        
        AccountingXmlDocumentListWrapper documentWrapper = new AccountingXmlDocumentListWrapper();
        documentWrapper.setCreateDate(Calendar.getInstance().getTime());
        documentWrapper.setReportEmail(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_ADMIN_EMAIL_PROPERTY_NAME));
        documentWrapper.setOverview("AWS Billing documents for " + findMonthName() + " " + findProcessYear());
        
        addDocumentsToDocumentWrapper(cloudCheckrWrapper, defaultAccountWrapper, resultsDTO, documentWrapper);
        return documentWrapper;
        
    }
    
    protected void addDocumentsToDocumentWrapper(CloudCheckrWrapper cloudCheckrWrapper,
            DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper, AmazonBillResultsDTO resultsDTO,
            AccountingXmlDocumentListWrapper documentWrapper) {
        Long documentIndex = new Long(1);
        
        for (GroupLevel awsAccountGroup : cloudCheckrWrapper.getCostsByGroup()) {
            resultsDTO.numberOfAwsAccountInCloudCheckr++;
            AccountingXmlDocumentEntry document = new AccountingXmlDocumentEntry();
            document.setDocumentTypeCode(CuFPConstants.DI);
            document.setIndex(documentIndex);
            String awsAccount = parseAWSAccountFromCloudCheckrGroupValue(awsAccountGroup.getGroupValue());
            document.setExplanation(buildDocumentExplanation(awsAccount));
            
            String departmentName = parseDeaprtmentNameFromCloudCheckrGroupValue(awsAccountGroup.getGroupValue());
            document.setDescription(buildDocumentDescription(departmentName));
            
            KualiDecimal targetLineTotal = addTargetLinesToDocument(defaultAccountWrapper, resultsDTO, awsAccountGroup, document, awsAccount);
            
            addSourceLineToDocument(document, targetLineTotal);
            
            addNotesToDocument(document);
            
            addBackupDocumentLinksToDocument(document, awsAccount, departmentName);
            
            if (shouldBuildDistributionOfIncomeDocument(awsAccount, departmentName, targetLineTotal)) {
                documentWrapper.getDocuments().add(document);
                resultsDTO.xmlCreationCount++;
                resultsDTO.awsAccountGeneratedDIxml.add(awsAccount);
                documentIndex = Long.sum(documentIndex, 1);
            } else {
                resultsDTO.awsAccountWithExstingDI.add(awsAccountGroup.getGroupValue());
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
    
    protected String parseDeaprtmentNameFromCloudCheckrGroupValue(String cloudCheckrAWSAccount) {
        int indexOfFirstOpenParen = StringUtils.indexOf(cloudCheckrAWSAccount, CuFPConstants.LEFT_PARENTHESIS);
        int indexOfFirstCloseParen = StringUtils.indexOf(cloudCheckrAWSAccount, CuFPConstants.RIGHT_PARENTHESIS);
        String parsedDepartmentName = StringUtils.substring(cloudCheckrAWSAccount, indexOfFirstOpenParen + 1, indexOfFirstCloseParen);
        return StringUtils.trim(parsedDepartmentName);
    }
    
    protected String buildDocumentDescription(String departmentName) {
        String documentDescriptionBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_DOCUMENT_DESCRIPTION_FORMAT);
        String documentDescription = MessageFormat.format(documentDescriptionBase, findMonthName(), departmentName);
        return StringUtils.substring(documentDescription, 0, 40);
    }
    
    protected KualiDecimal addTargetLinesToDocument(DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper,
            AmazonBillResultsDTO resultsDTO, GroupLevel awsAccountGroup, AccountingXmlDocumentEntry document,
            String awsAccount) {
        DefaultKfsAccountForAws defaultKfsAccount = findDefaultKfsAccountForAws(awsAccount, defaultAccountWrapper, resultsDTO);
        KualiDecimal targetLineTotal = KualiDecimal.ZERO;
        for (GroupLevel costCenterGroup : awsAccountGroup.getNextLevel()) {
            if (KualiDecimal.ZERO.isLessThan(costCenterGroup.getCost())) {
                AccountingXmlDocumentAccountingLine targetLine = awsAccountingXmlDocumentAccountingLineService.createAccountingXmlDocumentAccountingLine(costCenterGroup, defaultKfsAccount);
                targetLineTotal = targetLineTotal.add(targetLine.getAmount());
                document.getTargetAccountingLines().add(targetLine);
            } else {
                LOG.error("buildAccountingXmlDocumentListWrapper, found a group with a cost of zero: " + costCenterGroup);
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
        sourceLine.setChartCode(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_CHART_CODE_PROPERTY_NAME));
        sourceLine.setAccountNumber(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_FROM_ACCOUNT_PROPERTY_NAME));
        sourceLine.setObjectCode(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_OBJECT_CODE_PROPERTY_NAME));
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
        note.setDescription(getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_NOTE_HELP_TEXT_PROERTY_NAME));
        document.getNotes().add(note);
    }
    
    protected void addBackupDocumentLinksToDocument(AccountingXmlDocumentEntry document, String awsAccount, String departmentName) {
        AccountingXmlDocumentBackupLink link = new AccountingXmlDocumentBackupLink();
        link.setCredentialGroupCode(CuFPConstants.AmazonWebServiceBillingConstants.AWS_BILL_CREDENTIAL_GROUP_CODE);

        String fileNameFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_INVOICE_FILE_NAME_FORMAT);
        String noSpacesDepartmentName = StringUtils.replace(departmentName, KRADConstants.BLANK_SPACE, KFSConstants.DASH);
        link.setFileName(MessageFormat.format(fileNameFormat, noSpacesDepartmentName, findMonthName(), findProcessYear()));
        
        String noteTextFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_INVOICE_NOTE_TEXT_FORMAT);
        
        link.setDescription(MessageFormat.format(noteTextFormat, departmentName, findMonthName(), findProcessYear()));
        
        link.setLinkUrl(cloudcheckrService.buildAttachmentUrl(findProcessYear(), findProcessMonthNumber(), awsAccount));
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
    
    protected boolean generateXML(AccountingXmlDocumentListWrapper documentWrapper) {
        boolean success = true;
        String fileNameBase = configurationService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_OUTPUT_FILE_NAME_FORMAT);
        String fileName = MessageFormat.format(fileNameBase, directoryPath, findProcessYear(), findMonthName(), Calendar.getInstance().getTime().getTime());
        try {
            cuMarshalService.marshalObjectToXML(documentWrapper, fileName);
            fileStorageService.createDoneFile(fileName);
            LOG.info("generateXML, successfully created an XML and DONE file for " + fileName);
        } catch (JAXBException | IOException e) {
            success = false;
            LOG.error("generateDistributionOfIncomeDocumentsFromAWSService, unable to marshal documentWrapper to " + fileName, e);
        }
        return success;
    }
    
    private void logResults(AmazonBillResultsDTO resultsDTO) {
        LOG.info("logResults, number of AWS accounts in cloudcheckr: " + resultsDTO.numberOfAwsAccountInCloudCheckr);
        LOG.info("logResults, number of DI XML Created: " + resultsDTO.xmlCreationCount);
        LOG.info("logResults, amazon accounts that had XML DI created: " + resultsDTO.awsAccountGeneratedDIxml);
        LOG.info("logResults, amazon accounts that already had a DI for this month: " + resultsDTO.awsAccountWithExstingDI);
        LOG.info("logResults, amazon accounts that do NOT have a default kfs account: " + resultsDTO.awsAccountWithoutDefaultAccount);
    }
    
    protected void resetProperties() {
        billingPeriodParameterValue = null;
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
        if (StringUtils.isEmpty(billingPeriodParameterValue)) {
            String processingDate = getAWSParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.AWS_PROCESSING_DATE_PROPERTY_NAME);
            billingPeriodParameterValue = StringUtils.isNotBlank(processingDate) ? processingDate : 
                CuFPConstants.AmazonWebServiceBillingConstants.DEFAULT_BILLING_PERIOD_PARAMETER;
            if (LOG.isDebugEnabled()) {
                LOG.debug("getBillingPeriodParameterValue() The AWS_PROCESSING_DATE parameter value is '" + processingDate + 
                        "' and returning " + billingPeriodParameterValue);
            }
        }
        return billingPeriodParameterValue;
    }

    public void setBillingPeriodParameterValue(String billingPeriodParameterValue) {
        this.billingPeriodParameterValue = billingPeriodParameterValue;
    }

    protected String getAWSParameterValue(String parameterName) {
        String parmValue = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME, parameterName);
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

    public void setCloudcheckrService(CloudcheckrService cloudcheckrService) {
        this.cloudcheckrService = cloudcheckrService;
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

    private class AmazonBillResultsDTO {
        public int numberOfAwsAccountInCloudCheckr;
        public int xmlCreationCount;
        public List<String> awsAccountWithoutDefaultAccount;
        public List<String> awsAccountWithExstingDI;
        public List<String> awsAccountGeneratedDIxml;
        
        public AmazonBillResultsDTO() {
            awsAccountWithoutDefaultAccount = new ArrayList<String>();
            awsAccountWithExstingDI = new ArrayList<String>();
            awsAccountGeneratedDIxml = new ArrayList<String>();
        }
        
    }
    
}
