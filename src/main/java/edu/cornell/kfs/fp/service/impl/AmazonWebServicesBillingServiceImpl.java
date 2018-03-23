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
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.service.CloudcheckrService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.service.AmazonWebServicesBillingService;
import edu.cornell.kfs.fp.xmlObjects.AmazonAccountDetail;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class AmazonWebServicesBillingServiceImpl implements AmazonWebServicesBillingService, Serializable {

    private static final long serialVersionUID = 7430710204404759511L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AmazonWebServicesBillingServiceImpl.class);
    
    private String billingPeriodParameterValue;
    private String transactionObjectCode;
    private String transactionFromAccountNumber;
    private String helpNoteText;
    private String directoryPath;
    
    protected ParameterService parameterService;
    protected DocumentService documentService;
    protected BusinessObjectService businessObjectService;
    protected CloudcheckrService cloudcheckrService;
    protected AwsAccountingXmlDocumentAccountingLineService awsAccountingXmlDocumentAccountingLineService;
    protected CUMarshalService cuMarshalService;
    protected FileStorageService fileStorageService;


    @Override
    public void generateDistributionOfIncomeDocumentsFromAWSService() {
        String openingLogStatement = "generateDistributionOfIncomeDocumentsFromAWSService(), Processing Year:{0} Month: {1}, Start Date: {2}, End Date: {3}";
        LOG.info(MessageFormat.format(openingLogStatement, findProcessYear(), findMonthName(), findStartDate(), findEndDate()));
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

    }

    protected boolean generateXML(AccountingXmlDocumentListWrapper documentWrapper) {
        boolean success = true;
        String fileNameBase = "{0}/AmazonBill-{1}-{2}.xml";
        String fileName = MessageFormat.format(fileNameBase, directoryPath, findProcessYear(), findMonthName());
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
        LOG.info("logResults, number of DI XML Created: " + resultsDTO.xmlCreationCount);
        LOG.info("logResults, amazon accounts that already had a DI for this month: " + resultsDTO.awsAccountWithExstingDI);
        LOG.info("logResults, amazon accounts that DO NOT have a default kfs account: " + resultsDTO.awsAccountWithoutDefaultAccount);
        LOG.info("logResults, amazon accounts that do have a default kfs account: " + resultsDTO.awsAccountWithDefaultAccount);
    }
    
    protected AccountingXmlDocumentListWrapper buildAccountingXmlDocumentListWrapper(CloudCheckrWrapper cloudCheckrWrapper, 
            DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper, AmazonBillResultsDTO resultsDTO) {
        
        AccountingXmlDocumentListWrapper documentWrapper = new AccountingXmlDocumentListWrapper();
        documentWrapper.setCreateDate(Calendar.getInstance().getTime());
        /**
         * @todo create a report email address parameter and use that
         */
        documentWrapper.setReportEmail("nms32@cornell.edu");
        documentWrapper.setOverview("AWS Billing documents for " + findMonthName() + " " + findProcessYear());
        
        Long documentIndex = new Long(1);
        
        for (GroupLevel awsAccountGroup : cloudCheckrWrapper.getCostsByGroup()) {
            AmazonAccountDetail amazonAccountDetail = null;
            KualiDecimal cost = null;
            if (shouldBuildDistributionOfIncomeDocument(amazonAccountDetail, cost)) {
                AccountingXmlDocumentEntry document = new AccountingXmlDocumentEntry();
                document.setDocumentTypeCode("DI");
                document.setIndex(documentIndex);
                String awsAccount = parseAWSAccountFromCloudCheckrGroupValue(awsAccountGroup.getGroupValue());
                document.setExplanation(buildDocumentExplanation(awsAccount));
                document.setDescription(buildDocumentDescription(parseDeaprtmentNameFromCloudCheckrGroupValue(awsAccountGroup.getGroupValue())));
                
                DefaultKfsAccountForAws defaultKfsAccount = findDefaultKfsAccountForAws(awsAccount, defaultAccountWrapper, resultsDTO);
                KualiDecimal sourceLineTotal = KualiDecimal.ZERO;
                for (GroupLevel costCenterGroup : awsAccountGroup.getNextLevel()) {
                    AccountingXmlDocumentAccountingLine targetLine = awsAccountingXmlDocumentAccountingLineService.createAccountingXmlDocumentAccountingLine(costCenterGroup, defaultKfsAccount);
                    sourceLineTotal = sourceLineTotal.add(targetLine.getAmount());
                    document.getTargetAccountingLines().add(targetLine);
                }
                
                AccountingXmlDocumentAccountingLine sourceLine = new AccountingXmlDocumentAccountingLine();
                /**
                 * @todo get the chart from a parameter
                 */
                sourceLine.setChartCode("IT");
                sourceLine.setAccountNumber(getTransactionFromAccountNumber());
                sourceLine.setObjectCode(getTransactionObjectCode());
                sourceLine.setAmount(sourceLineTotal);
                document.getSourceAccountingLines().add(sourceLine);
                
                AccountingXmlDocumentNote note = new AccountingXmlDocumentNote();
                note.setDescription(getHelpNoteText());
                document.getNotes().add(note);
                
                documentWrapper.getDocuments().add(document);
                resultsDTO.xmlCreationCount++;
                documentIndex = Long.sum(documentIndex, 1);
            } else {
                resultsDTO.awsAccountWithExstingDI.add(awsAccountGroup.getGroupValue());
                LOG.error("buildAccountingXmlDocumentListWrapper, a DI already exists for " + awsAccountGroup.getGroupValue());
            }
            
        }
        return documentWrapper;
        
    }
    
    private DefaultKfsAccountForAws findDefaultKfsAccountForAws(String awsAccount, DefaultKfsAccountForAwsResultWrapper defaultAccountWrapper, 
            AmazonBillResultsDTO resultsDTO) {
        for (DefaultKfsAccountForAws defaultAccount : defaultAccountWrapper.getDefaultKfsAccountsForAws()) {
            if (StringUtils.equalsIgnoreCase(awsAccount, defaultAccount.getAwsAccount())) {
                resultsDTO.awsAccountWithDefaultAccount.add(awsAccount);
                return defaultAccount;
            }
        }
        resultsDTO.awsAccountWithoutDefaultAccount.add(awsAccount);
        return createDefaultKfsAccountForAwsAccount(awsAccount);
    }

    protected DefaultKfsAccountForAws createDefaultKfsAccountForAwsAccount(String awsAccount) {
        LOG.info("createDefaultKfsAccountForAwsAccount, no match found for AWS Account " + awsAccount + ", will return a default account to allow further processing.");
        DefaultKfsAccountForAws defaultKfsAccountForAws = new DefaultKfsAccountForAws();
        defaultKfsAccountForAws.setAwsAccount(awsAccount);
        defaultKfsAccountForAws.setKfsDefaultAccount("nodftat");
        defaultKfsAccountForAws.setUpdatedAt(Calendar.getInstance().getTime());
        return defaultKfsAccountForAws;
    }
    
    protected String parseAWSAccountFromCloudCheckrGroupValue(String cloudCheckrAWSAccount) {
        int indexOfFirstOpenParen = StringUtils.indexOf(cloudCheckrAWSAccount, "(");
        String parsedAWSAccount = StringUtils.substring(cloudCheckrAWSAccount, 0, indexOfFirstOpenParen);
        return StringUtils.trim(parsedAWSAccount);
    }
    
    protected String parseDeaprtmentNameFromCloudCheckrGroupValue(String cloudCheckrAWSAccount) {
        int indexOfFirstOpenParen = StringUtils.indexOf(cloudCheckrAWSAccount, "(");
        int indexOfFirstCloseParen = StringUtils.indexOf(cloudCheckrAWSAccount, ")");
        String parsedDepartmentName = StringUtils.substring(cloudCheckrAWSAccount, indexOfFirstOpenParen + 1, indexOfFirstCloseParen);
        return StringUtils.trim(parsedDepartmentName);
    }
    
    /*
     * @todo enable this
     */
    private boolean shouldBuildDistributionOfIncomeDocument(AmazonAccountDetail amazonAccountDetail, KualiDecimal cost) {
        /*
        for (DocumentHeader dh : findDocumentHeadersForAmazonDetail(amazonAccountDetail)) {
            try {
                DistributionOfIncomeAndExpenseDocument di = (DistributionOfIncomeAndExpenseDocument) documentService.getByDocumentHeaderId(dh.getDocumentNumber());
                if (di.getTotalDollarAmount().equals(cost)) {
                    LOG.info("shouldBuildDistributionOfIncomeDocument() DI document number " + di.getDocumentNumber() + " already exists for Amazon account: " + amazonAccountDetail.toString());
                    return false;
                }
            } catch (WorkflowException e) {
                LOG.error("shouldBuildDistributionOfIncomeDocument() There was a problems creating a DI document from a document header: " + dh.getDocumentNumber(), e); 
                throw new RuntimeException(e);
            }
        }
        */
        return true;
    }
    
    private Collection<DocumentHeader> findDocumentHeadersForAmazonDetail(AmazonAccountDetail amazonAccountDetail) {
        Map fieldValues = new HashMap();
        fieldValues.put(KFSPropertyConstants.EXPLANATION, buildDocumentExplanation(amazonAccountDetail.getAwsAccount()));
        fieldValues.put(KFSPropertyConstants.DOCUMENT_DESCRIPTION, buildDocumentDescription(amazonAccountDetail.getBusinessPurpose()));
        Collection<DocumentHeader> documentHeaders = businessObjectService.findMatching(DocumentHeader.class, fieldValues);
        return documentHeaders;
    }
    
    protected String buildDocumentDescription(String departmentName) {
        /**
         * @todo move this properties
         */
        String documentDescriptionBase = "{1} invoice for {0}";
        String documentDescription = MessageFormat.format(documentDescriptionBase, departmentName, findMonthName());
        return StringUtils.substring(documentDescription, 0, 40);
    }
    
    protected String buildDocumentExplanation(String AWSAccount) {
        return "AWS account " + AWSAccount;
    }

    protected String buildAccountingLineDescription() {
        return CuFPConstants.AmazonWebServiceBillingConstants.TRANSACTION_DESCRIPTION_STARTER + findMonthName() + " " + findProcessYear();
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
            String processingDate = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_PROCESSING_DATE_PROPERTY_NAME);
            billingPeriodParameterValue = StringUtils.isNotBlank(processingDate) ? processingDate : CuFPConstants.AmazonWebServiceBillingConstants.DEFAULT_BILLING_PERIOD_PARAMETER;
            LOG.debug("getBillingPeriodParameterValue() The AWS_PROCESSING_DATE parameter value is " + processingDate + " and returning " + billingPeriodParameterValue);
        }
        return billingPeriodParameterValue;
    }

    public void setBillingPeriodParameterValue(String billingPeriodParameterValue) {
        this.billingPeriodParameterValue = billingPeriodParameterValue;
    }
    
    
    public String getTransactionObjectCode() {
        if (transactionObjectCode == null) {
            transactionObjectCode = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_OBJECT_CODE_PROPERTY_NAME);
        }
        return transactionObjectCode;
    }

    public void setTransactionObjectCode(String transactionObjectCode) {
        this.transactionObjectCode = transactionObjectCode;
    }

    public String getTransactionFromAccountNumber() {
        if (transactionFromAccountNumber == null) {
            transactionFromAccountNumber = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_FROM_ACCOUNT_PROPERTY_NAME);
        }
        return transactionFromAccountNumber;
    }

    public void setTransactionFromAccountNumber(String transactionFromAccountNumber) {
        this.transactionFromAccountNumber = transactionFromAccountNumber;
    }

    public String getHelpNoteText() {
        if (helpNoteText == null) {
            helpNoteText = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_NOTE_HELP_TEXT_PROERTY_NAME);
        }
        return helpNoteText;
    }
    
    public void setHelpNoteText(String helpNoteText) {
        this.helpNoteText = helpNoteText;
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

    private class AmazonBillResultsDTO {
        public int xmlCreationCount;
        public List<String> awsAccountWithoutDefaultAccount;
        public List<String> awsAccountWithDefaultAccount;
        public List<String> awsAccountWithExstingDI;
        
        public AmazonBillResultsDTO() {
            awsAccountWithoutDefaultAccount = new ArrayList<String>();
            awsAccountWithDefaultAccount = new ArrayList<String>();
            awsAccountWithExstingDI = new ArrayList<String>();
        }
        
    }
    
}
