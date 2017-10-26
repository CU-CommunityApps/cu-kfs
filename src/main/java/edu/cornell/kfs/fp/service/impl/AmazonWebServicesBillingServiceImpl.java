package edu.cornell.kfs.fp.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.AmazonBillingCostCenterDTO;
import edu.cornell.kfs.fp.businessobject.AmazonBillingDistributionOfIncomeTransactionDTO;
import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;
import edu.cornell.kfs.fp.document.service.impl.CULegacyTravelServiceImpl;
import edu.cornell.kfs.fp.service.AmazonWebServicesBillingService;
import edu.cornell.kfs.fp.xmlObjects.AmazonAccountDetail;
import edu.cornell.kfs.fp.xmlObjects.AmazonAccountDetailContainer;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class AmazonWebServicesBillingServiceImpl implements AmazonWebServicesBillingService, Serializable {

    private static final long serialVersionUID = 7430710204404759511L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AmazonWebServicesBillingServiceImpl.class);
    
    private String awsURL;
    private String awsToken;
    private String billingPeriodParameterValue;
    private String defaultDocumentDescription;
    private String transactionObjectCode;
    private String transactionFromAccountNumber;
    private String helpNoteText;

    protected ParameterService parameterService;
    protected DocumentService documentService;
    protected ChartService chartService;
    protected AccountService accountService;
    protected SubAccountService subAccountService;
    protected BusinessObjectService businessObjectService;
    protected ObjectCodeService objectCodeService;
    protected SubObjectCodeService subObjectCodeService;
    protected ProjectCodeService projectCodeService;

    @Override
    public void generateDistributionOfIncomeDocumentsFromAWSService() {
        LOG.info("generateDistributionOfIncomeDocumentsFromAWSService(): AWS URL: " + getAwsURL());
        LOG.info("generateDistributionOfIncomeDocumentsFromAWSService(): Processing Year: " + findProcessYear() + "  Month: " + findProcessMonthNumber());
        
        String jsonResults = buildJsonOutput();
        LOG.debug("JSON results: " + jsonResults);

        List<AmazonAccountDetail> amazonAccountDetails = buildAmazonAcountListFromJson(jsonResults);
        LOG.info("generateDistributionOfIncomeDocumentsFromAWSService() Number of amazon account details received: " + amazonAccountDetails.size());
        
        boolean didProcessingCompleteWithoutErrors = true;
        
        if (amazonAccountDetails.size() == 0) {
            LOG.error("generateDistributionOfIncomeDocumentsFromAWSService() No amazon account detail objects parsed from the webservice.");
            didProcessingCompleteWithoutErrors =  false;
        }
        
        didProcessingCompleteWithoutErrors = didProcessingCompleteWithoutErrors && processAmazonAccountDetails(amazonAccountDetails);
        
        clearMemberVariables();
        if (!didProcessingCompleteWithoutErrors) {
            throw new RuntimeException("There was error processing AWS Billing.");
        }
    }
    
    private String buildJsonOutput() {
        Client client = null;
        Response response = null;
        
        try {
            ClientConfig clientConfig = new ClientConfig();
            client = ClientBuilder.newClient(clientConfig);
            
            Invocation request = buildClientRequest(client);
            response = request.invoke();
            
            response.bufferEntity();
            String results = response.readEntity(String.class);
            return results;
        } finally {
            CURestClientUtils.closeQuietly(response);
            CURestClientUtils.closeQuietly(client);
        }
    }
    
    protected Invocation buildClientRequest(Client client) {
        URI uri = buildAwsServiceUrl();
        return client.target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(CuFPConstants.AmazonWebServiceBillingConstants.AUTHORIZATION_HEADER_NAME, 
                        CuFPConstants.AmazonWebServiceBillingConstants.AUTHORIZATION_TOKEN_VALUE_STARTER + getAwsToken())
                .buildGet();
    }
    
    protected URI buildAwsServiceUrl() {
        try {
            return new URI(getAwsURL() + CuFPConstants.AmazonWebServiceBillingConstants.URL_PARAMETER_YEAR + findProcessYear() + 
                    CuFPConstants.AmazonWebServiceBillingConstants.URL_PARAMETER_MONTH + findProcessMonthNumber());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected List<AmazonAccountDetail> buildAmazonAcountListFromJson(String jsonResults) {
        ObjectMapper mapper = new ObjectMapper();
        AmazonAccountDetailContainer detailContainer = new AmazonAccountDetailContainer();
        try {
            detailContainer = mapper.readValue(jsonResults, AmazonAccountDetailContainer.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return detailContainer.getAccountDetail();
    }

    protected boolean processAmazonAccountDetails(List<AmazonAccountDetail> amazonAccountDetails) {
        boolean didProcessingCompleteWithoutErrors = true;
        AmazonBillResultsDTO resultsDTO = new AmazonBillResultsDTO();
        for (AmazonAccountDetail amazonAccountDetail : amazonAccountDetails) {
            if (StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION, amazonAccountDetail.getKfsAccount())) {
                LOG.info("processAmazonAccountDetails() Found an internal kfs account, ignoring this transaction for AWS account " 
                    + amazonAccountDetail.getAwsAccount());
                resultsDTO.internalCount++;
            } else {
                didProcessingCompleteWithoutErrors = processExternalBill(amazonAccountDetail, resultsDTO) && didProcessingCompleteWithoutErrors;
            }
        }
        logSummaryReport(resultsDTO);
        return didProcessingCompleteWithoutErrors;
    }

    private boolean processExternalBill(AmazonAccountDetail amazonAccountDetail, AmazonBillResultsDTO resultsDTO) {
        boolean valid = true;
        try {
            AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO = buildTransactionDTO(amazonAccountDetail);
            if (transactionDTO.isTransactionInputError()) {
                valid = false;
                LOG.error("processAmazonAccountDetails() there errors that prevent a creation of a DI " + amazonAccountDetail.toString());
                resultsDTO.erroredCount++;
            } else if (shouldBuildDistributionOfIncomeDocument(amazonAccountDetail, transactionDTO.getAmount())) {
                buidAndRouteDistributionOfIncomeDocument(amazonAccountDetail, transactionDTO, resultsDTO);
            } else {
                resultsDTO.existingDICount++;
            }
        } catch (Throwable e) {
            valid = false;
            LOG.error("processAmazonAccountDetails() there was a problem building a DI for " + amazonAccountDetail.toString(), e);
            GlobalVariables.getMessageMap().getErrorMessages().clear();
            resultsDTO.erroredCount++;
        }
        return valid;
    }
    
    protected AmazonBillingDistributionOfIncomeTransactionDTO buildTransactionDTO(AmazonAccountDetail amazonAccountDetail) {
        
        AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO = new AmazonBillingDistributionOfIncomeTransactionDTO();
        transactionDTO.setLineDescription(buildAccountingLineDescription());
        
        updateTransactionDTOAmount(amazonAccountDetail, transactionDTO);
        
        try {
            AmazonBillingCostCenterDTO costCenterDTO = convertCostCenterToAmazonBillingCostCenterDTO(amazonAccountDetail.getCostCenter());
            updateTransactionDTOChartAccountSubAccount(transactionDTO, costCenterDTO, amazonAccountDetail.getKfsAccount());
            updateTransactionDTOObjectCodes(transactionDTO, costCenterDTO);
            updateTransactionDTOProjectCodeNumber(transactionDTO, costCenterDTO);
            transactionDTO.setOrganizationReferenceId(costCenterDTO.getOrgReferenceId());
        } catch (Exception e) {
            transactionDTO.setTransactionInputError(true);
            LOG.error("buildAmazonDistributionOfIncomeTransactionDTO: There was a problem parsing cost center.", e);
        }
        return transactionDTO;
    }

    private void updateTransactionDTOAmount(AmazonAccountDetail amazonAccountDetail, AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO) {
        transactionDTO.setAmount(convertCostStringToKualiDecimal(amazonAccountDetail.getCost()));
        if (transactionDTO.getAmount().isLessEqual(KualiDecimal.ZERO)) {
            transactionDTO.setTransactionInputError(true);
        }
    }
    
    protected KualiDecimal convertCostStringToKualiDecimal(String costString) {
        KualiDecimal cost = new KualiDecimal(costString);
        final double oneCent = .01;
        if (cost.doubleValue() < oneCent) {
            return KualiDecimal.ZERO;
        }
        return cost;
    }
    
    protected AmazonBillingCostCenterDTO convertCostCenterToAmazonBillingCostCenterDTO(String costCenter) throws Exception {
        AmazonBillingCostCenterDTO costCenterDTO = new AmazonBillingCostCenterDTO();
        
        if (StringUtils.contains(costCenter, KFSConstants.DASH)) {
            String[] costCenterArray = StringUtils.splitPreserveAllTokens(costCenter, KFSConstants.DASH);
            if (costCenterArray.length == 2) {
                costCenterDTO.setAccountNumber(costCenterArray[0]);
                costCenterDTO.setSubAccountNumber(costCenterArray[1]);
            } else if (costCenterArray.length == 7) {
                costCenterDTO.setChartCode(costCenterArray[0]);
                costCenterDTO.setAccountNumber(costCenterArray[1]);
                costCenterDTO.setSubAccountNumber(costCenterArray[2]);
                costCenterDTO.setObjectCode(costCenterArray[3]);
                costCenterDTO.setSubObjectCode(costCenterArray[4]);
                costCenterDTO.setProjectCode(costCenterArray[5]);
                costCenterDTO.setOrgReferenceId(costCenterArray[6]);
            } else {
                throw new Exception("There was an incorrect number of elements in the Cost Center");
            }
        } else {
            costCenterDTO.setAccountNumber(costCenter);
        }
        
        return costCenterDTO;
    }
    
    private void updateTransactionDTOChartAccountSubAccount(AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO, AmazonBillingCostCenterDTO costCenterDTO,
            String amazonDetailKFSAccount) {
        Account account = retrieveAccount(costCenterDTO.getAccountNumber(), amazonDetailKFSAccount);
        if (validAccount(account)) {
            transactionDTO.setAccountNumber(account.getAccountNumber());
            updateTansactionDTOChart(transactionDTO, costCenterDTO, account);
            updateTransactionDTOSubAccount(transactionDTO, costCenterDTO, account);
        } else {
            LOG.info("updateTransactionDTOChartAccountSubAccount() Invalid account.");
            transactionDTO.setTransactionInputError(true);
        }
    }

    private Account retrieveAccount(String costCenterAccount, String amazonDetailKFSAccount) {
        Account account = null;
        if (StringUtils.isNotBlank(costCenterAccount)) {
            account = findAccountFromAccountNumber(costCenterAccount);
        } else {
            account = findAccountFromAccountNumber(amazonDetailKFSAccount);
        }
        return account;
    }
    
    
    
    private Account findAccountFromAccountNumber(String accountNumber) {
        if (StringUtils.isNotBlank(accountNumber)) {
            Account account = getAccountService().getUniqueAccountForAccountNumber(accountNumber);
            return account;
        }
        return null;
    }
    
    private boolean validAccount(Account account) {
        return account != null && account.isActive() && !account.isExpired();
    }
    
    private void updateTansactionDTOChart(AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO, AmazonBillingCostCenterDTO costCenterDTO, 
            Account account) {
        if (StringUtils.isNotBlank(costCenterDTO.getChartCode())) {
            Chart chart = getChartService().getByPrimaryId(costCenterDTO.getChartCode());
            if (ObjectUtils.isNotNull(chart) && chart.isActive()) {
                transactionDTO.setChartCode(chart.getChartOfAccountsCode());
            } else {
                LOG.info("updateTansactionDTOChart() Invalid Chart.");
                transactionDTO.setTransactionInputError(true);
            }
        } else {
            transactionDTO.setChartCode(account.getChartOfAccountsCode());
        }
    }
    
    private void updateTransactionDTOSubAccount(AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO, AmazonBillingCostCenterDTO costCenterDTO, 
            Account account) {
        if (StringUtils.isNotBlank(costCenterDTO.getSubAccountNumber())) {
            SubAccount subAccount = findSubAccountFromSubAccountNumber(account, costCenterDTO.getSubAccountNumber());
            if (ObjectUtils.isNotNull(subAccount)) {
                transactionDTO.setSubAccountNumber(subAccount.getSubAccountNumber());
            } else {
                LOG.info("updateTransactionDTOSubAccount() Invalid Sub Account.");
                transactionDTO.setTransactionInputError(true);
            }
        }
    }
    
    private SubAccount findSubAccountFromSubAccountNumber(Account account, String subAccountNumber) {
        SubAccount subAccount = null;
        if (ObjectUtils.isNotNull(account)) {
            subAccount = getSubAccountService().getByPrimaryId(account.getChartOfAccountsCode(), account.getAccountNumber(), subAccountNumber);
            if (ObjectUtils.isNull(subAccount) || !subAccount.isActive()) {
                LOG.info("findSubAccountFromSubAccountNumber() An invalid sub account number was provided: " + subAccountNumber);
                subAccount = null;
            }
        }
        return subAccount;
    }
    
    private void updateTransactionDTOObjectCodes(AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO, AmazonBillingCostCenterDTO costCenterDTO) {
        if (StringUtils.isNotBlank(costCenterDTO.getObjectCode())) {
            ObjectCode objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(transactionDTO.getChartCode(), costCenterDTO.getObjectCode());
            if (ObjectUtils.isNotNull(objectCode)) {
                transactionDTO.setObjectCodeNumber(objectCode.getFinancialObjectCode());
                if (StringUtils.isNotBlank(costCenterDTO.getSubObjectCode())) {
                    SubObjectCode subObjectCode = getSubObjectCodeService().getByPrimaryIdForCurrentYear(transactionDTO.getChartCode(), 
                            transactionDTO.getAccountNumber(), objectCode.getFinancialObjectCode(), costCenterDTO.getSubObjectCode());
                    if (ObjectUtils.isNotNull(subObjectCode)) {
                        transactionDTO.setSubObjectCodeNumber(subObjectCode.getFinancialSubObjectCode());
                    } else {
                        LOG.info("updateTransactionDTOObjectCodes() Invalid Sub Object Code.");
                        transactionDTO.setTransactionInputError(true);
                    }
                }
            } else {
                LOG.info("updateTransactionDTOObjectCodes() Invalid Object Code.");
                transactionDTO.setTransactionInputError(true);
            }
        } else {
            transactionDTO.setObjectCodeNumber(getTransactionObjectCode());
        }
    }
    
    private void updateTransactionDTOProjectCodeNumber(AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO, AmazonBillingCostCenterDTO costCenterDTO) {
        if (StringUtils.isNotBlank(costCenterDTO.getProjectCode())) {
            ProjectCode projectCode = getProjectCodeService().getByPrimaryId(costCenterDTO.getProjectCode());
            if (ObjectUtils.isNotNull(projectCode)) {
                transactionDTO.setProjectCodeNumber(projectCode.getCode());
            } else {
                LOG.info("updateTransactionDTOProjectCodeNumber() Invalid Project Code.");
                transactionDTO.setTransactionInputError(true);
            }
        }
    }
    
    private boolean shouldBuildDistributionOfIncomeDocument(AmazonAccountDetail amazonAccountDetail, KualiDecimal cost) {
        for (DocumentHeader dh : findDocumentHeadersForAmazonDetail(amazonAccountDetail)) {
            try {
                DistributionOfIncomeAndExpenseDocument di = (DistributionOfIncomeAndExpenseDocument) getDocumentService().getByDocumentHeaderId(dh.getDocumentNumber());
                if (di.getTotalDollarAmount().equals(cost)) {
                    LOG.info("shouldBuildDistributionOfIncomeDocument() DI document number " + di.getDocumentNumber() + " already exists for Amazon account: " + amazonAccountDetail.toString());
                    return false;
                }
            } catch (WorkflowException e) {
                LOG.error("shouldBuildDistributionOfIncomeDocument() There was a problems creating a DI document from a document header: " + dh.getDocumentNumber(), e); 
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    
    private Collection<DocumentHeader> findDocumentHeadersForAmazonDetail(AmazonAccountDetail amazonAccountDetail) {
        Map fieldValues = new HashMap();
        fieldValues.put(KFSPropertyConstants.EXPLANATION, buildDocumentExplanation(amazonAccountDetail.getAwsAccount()));
        fieldValues.put(KFSPropertyConstants.DOCUMENT_DESCRIPTION, buildDocumentDescription(amazonAccountDetail.getBusinessPurpose()));
        Collection<DocumentHeader> documentHeaders = getBusinessObjectService().findMatching(DocumentHeader.class, fieldValues);
        return documentHeaders;
    }
    
    private void buidAndRouteDistributionOfIncomeDocument(AmazonAccountDetail amazonAccountDetail, 
            AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO, AmazonBillResultsDTO resultsDTO) throws WorkflowException {
        CuDistributionOfIncomeAndExpenseDocument diDocument = createDistributionOfIncomeDocument(amazonAccountDetail, transactionDTO);
        getDocumentService().routeDocument(diDocument, CuFPConstants.AmazonWebServiceBillingConstants.DI_ROUTE_ANNOTATION, null);
        LOG.info("buidAndRouteDistributionOfIncomeDocument() Created DI document " + diDocument.getDocumentNumber() + " for AWS account " + 
                amazonAccountDetail.getAwsAccount() + " with a Cornell acount of " + transactionDTO.getAccountNumber() + " for a value of " + transactionDTO.getAmount().doubleValue());
        resultsDTO.diCreationCount++;
    }

    private CuDistributionOfIncomeAndExpenseDocument createDistributionOfIncomeDocument(AmazonAccountDetail amazonAccountDetail, 
            AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO) throws WorkflowException {
        CuDistributionOfIncomeAndExpenseDocument diDocument = (CuDistributionOfIncomeAndExpenseDocument) getDocumentService().getNewDocument(
                CuDistributionOfIncomeAndExpenseDocument.class);
        diDocument.getDocumentHeader().setDocumentDescription(buildDocumentDescription(amazonAccountDetail.getBusinessPurpose()));
        diDocument.getDocumentHeader().setExplanation(buildDocumentExplanation(amazonAccountDetail.getAwsAccount()));
        diDocument.getNotes().add(buildDINote());
        diDocument.getTargetAccountingLines().add((buildToAccountingLine(diDocument.getDocumentNumber(), transactionDTO)));
        diDocument.getSourceAccountingLines().add(buildFromAccountLine(diDocument.getDocumentNumber(), transactionDTO.getAmount()));
        diDocument.setTripAssociationStatusCode(CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_NOT_TRIP_DOC);
        return diDocument;
    }
    
    protected String buildDocumentDescription(String businessPurpose) {
        String documentDescription;
        if (StringUtils.isNotBlank(businessPurpose)) {
            documentDescription = businessPurpose;
        } else {
            documentDescription = getDefaultDocumentDescription();
        }
        return StringUtils.substring(documentDescription, 0, 40);
    }
    
    protected String buildDocumentExplanation(String AWSAccount) {
        return "AWS charges for account number " + AWSAccount + " for " + findMonthName() + " " + findProcessYear();
    }

    private Note buildDINote() {
        Note diNote = new Note();
        diNote.setNoteText(getHelpNoteText());
        diNote.setAuthorUniversalIdentifier(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        diNote.setNotePostedTimestampToCurrent();
        return diNote;
    }
    
    private TargetAccountingLine buildToAccountingLine(String diDocumentNumber, AmazonBillingDistributionOfIncomeTransactionDTO transactionDTO) {
        TargetAccountingLine line = new TargetAccountingLine();
        line.setDocumentNumber(diDocumentNumber);
        line.setChartOfAccountsCode(transactionDTO.getChartCode());
        line.setAccountNumber(transactionDTO.getAccountNumber());
        line.setSubAccountNumber(transactionDTO.getSubAccountNumber());
        line.setFinancialObjectCode(transactionDTO.getObjectCodeNumber());
        line.setFinancialSubObjectCode(transactionDTO.getSubObjectCodeNumber());
        line.setProjectCode(transactionDTO.getProjectCodeNumber());
        line.setOrganizationReferenceId(StringUtils.substring(transactionDTO.getOrganizationReferenceId(), 0, 8));
        line.setAmount(transactionDTO.getAmount());
        line.setFinancialDocumentLineDescription(buildAccountingLineDescription());
        line.setSequenceNumber(new Integer(1));
        return line;
    }
    
    private SourceAccountingLine buildFromAccountLine(String diDocumentNumber, KualiDecimal amount) {
        SourceAccountingLine line = new SourceAccountingLine();
        Account fromAccount = findAccountFromAccountNumber(getTransactionFromAccountNumber());
        line.setDocumentNumber(diDocumentNumber);
        line.setAccountNumber(fromAccount.getAccountNumber());
        line.setChartOfAccountsCode(fromAccount.getChartOfAccountsCode());
        line.setFinancialObjectCode(getTransactionObjectCode());
        line.setAmount(amount);
        line.setFinancialDocumentLineDescription(buildAccountingLineDescription());
        line.setSequenceNumber(new Integer(1));
        return line;
    }
    
    protected String buildAccountingLineDescription() {
        return CuFPConstants.AmazonWebServiceBillingConstants.TRANSACTION_DESCRIPTION_STARTER + findMonthName() + " " + findProcessYear();
    }
    
    private void logSummaryReport(AmazonBillResultsDTO resultsDTO) {
        LOG.info("logSummaryReport() Number of DIs created: " + resultsDTO.diCreationCount);
        LOG.info("logSummaryReport() Number of internal account records found: " + resultsDTO.internalCount);
        LOG.info("logSummaryReport() Number of already existing DIs found: " + resultsDTO.existingDICount);
        LOG.info("logSummaryReport() Number of accounts that had errors preventing creation of a DI: " + resultsDTO.erroredCount);
    }
    
    private void clearMemberVariables() {
        billingPeriodParameterValue = null;
        defaultDocumentDescription = null;;
        transactionObjectCode = null;;
        transactionFromAccountNumber = null;;
        helpNoteText = null;
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
            String processingDate = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
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
    
    public String getDefaultDocumentDescription() {
        if (defaultDocumentDescription == null) {
            defaultDocumentDescription = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_DEFAULT_DOCUMENT_DESCRIPTION_PROPERTY_NAME);
        }
        return defaultDocumentDescription;
    }

    public void setDefaultDocumentDescription(String defaultDocumentDescription) {
        this.defaultDocumentDescription = defaultDocumentDescription;
    }

    public String getTransactionObjectCode() {
        if (transactionObjectCode == null) {
            transactionObjectCode = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
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
            transactionFromAccountNumber = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
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
            helpNoteText = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME, 
                    CuFPConstants.AmazonWebServiceBillingConstants.AWS_NOTE_HELP_TEXT_PROERTY_NAME);
        }
        return helpNoteText;
    }
    
    public void setHelpNoteText(String helpNoteText) {
        this.helpNoteText = helpNoteText;
    }
    
    public String getAwsURL() {
        return awsURL;
    }

    public void setAwsURL(String awsURL) {
        this.awsURL = awsURL;
    }

    public String getAwsToken() {
        return awsToken;
    }

    public void setAwsToken(String awsToken) {
        this.awsToken = awsToken;
    }
    
    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

    public SubAccountService getSubAccountService() {
        return subAccountService;
    }

    public void setSubAccountService(SubAccountService subAccountService) {
        this.subAccountService = subAccountService;
    }

    public ObjectCodeService getObjectCodeService() {
        return objectCodeService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public SubObjectCodeService getSubObjectCodeService() {
        return subObjectCodeService;
    }

    public void setSubObjectCodeService(SubObjectCodeService subObjectCodeService) {
        this.subObjectCodeService = subObjectCodeService;
    }

    public ProjectCodeService getProjectCodeService() {
        return projectCodeService;
    }

    public void setProjectCodeService(ProjectCodeService projectCodeService) {
        this.projectCodeService = projectCodeService;
    }
    
    private class AmazonBillResultsDTO {
        public int internalCount = 0;
        public int erroredCount = 0;
        public int diCreationCount = 0;
        public int existingDICount = 0;
    }
    
}
