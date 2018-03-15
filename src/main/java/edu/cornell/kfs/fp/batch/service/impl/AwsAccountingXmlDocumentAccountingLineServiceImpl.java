package edu.cornell.kfs.fp.batch.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.CuFPConstants;
import org.kuali.kfs.sys.KFSConstants;

public class AwsAccountingXmlDocumentAccountingLineServiceImpl implements AwsAccountingXmlDocumentAccountingLineService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImpl.class);

    protected ParameterService parameterService;
    protected ChartService chartService;
    protected AccountService accountService;
    protected SubAccountService subAccountService;
    protected ObjectCodeService objectCodeService;
    protected SubObjectCodeService subObjectCodeService;
    protected ProjectCodeService projectCodeService;

    @Override
    public AccountingXmlDocumentAccountingLine createAccountingXmlDocumentAccountingLine(GroupLevel costCenterGroupLevel,
                                                                                         DefaultKfsAccountForAws defaultKfsAccountForAws) {
        LOG.info("createAccountingXmlDocumentAccountingLine for " + costCenterGroupLevel.getGroupName());

        String costCenterGroupValue = costCenterGroupLevel.getGroupValue();
        AccountingXmlDocumentAccountingLine xmlAccountingLine = getEmptyAccountingXmlDocumentAccountingLine();
        if (StringUtils.countMatches(costCenterGroupValue, "*") == 6) {
            xmlAccountingLine = parseCostCenterGroupLevel(costCenterGroupLevel.getGroupValue());
        }
        else if (StringUtils.countMatches(costCenterGroupValue, "-") == 1) {
            xmlAccountingLine.setAccountNumber(StringUtils.split(costCenterGroupValue, "-")[0]);
            xmlAccountingLine.setSubAccountNumber(StringUtils.split(costCenterGroupValue, "-")[1]);
        }
        else {
            xmlAccountingLine.setAccountNumber(costCenterGroupValue);
        }

        xmlAccountingLine = fixAccountFieldReferences(xmlAccountingLine, defaultKfsAccountForAws.getKfsDefaultAccount());
        xmlAccountingLine.setAmount(costCenterGroupLevel.getCost());
        return xmlAccountingLine;
    }

    private AccountingXmlDocumentAccountingLine fixAccountFieldReferences(AccountingXmlDocumentAccountingLine xmlAccountingLine, String defaultAccountString) {
        if (!validateChartCode(xmlAccountingLine.getChartCode())) {
            String defaultCharCode = getDefaultChartCodeFromParameter();
            xmlAccountingLine.setChartCode(defaultCharCode);
        }

        if (!StringUtils.equalsIgnoreCase(defaultAccountString, CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION) &&
                !validateAccount(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber())) {
            return getDefaultAccountingLine(defaultAccountString);
        }

        if (!validateObjectCode(xmlAccountingLine.getChartCode(), xmlAccountingLine.getObjectCode())) {
            String defaultObjectCode = getDefaultObjectCodeFromParameter();
            xmlAccountingLine.setObjectCode(defaultObjectCode);
        }

        if (!validateSubAccountNumber(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getSubAccountNumber())) {
            xmlAccountingLine.setSubAccountNumber(StringUtils.EMPTY);
        }
        if (!validateSubObjectCode(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getObjectCode(),
                xmlAccountingLine.getSubObjectCode())) {
            xmlAccountingLine.setSubObjectCode(StringUtils.EMPTY);
        }
        if (!validateProjectCode(xmlAccountingLine.getProjectCode())) {
            xmlAccountingLine.setProjectCode(StringUtils.EMPTY);
        }
        if (!validateOrgRefId(xmlAccountingLine.getOrgRefId())) {
            xmlAccountingLine.setOrgRefId(StringUtils.EMPTY);
        }

        return xmlAccountingLine;
    }

    private boolean validateChartCode(String chartCode) {
        if (StringUtils.isBlank(chartCode)) {
            return false;
        }
        Chart chart = chartService.getByPrimaryId(chartCode);
        if (ObjectUtils.isNull(chart) || !chart.isActive()){
            LOG.error(String.format("Invalid Chart Code %s", chartCode));
            return false;
        }
        return true;
    }

    private AccountingXmlDocumentAccountingLine parseCostCenterGroupLevel(String costCenterGroupLevelValue) {
        String[] costCenterGroupValueSplit = StringUtils.splitByWholeSeparatorPreserveAllTokens(costCenterGroupLevelValue,"*", -1);
        AccountingXmlDocumentAccountingLine ret = getEmptyAccountingXmlDocumentAccountingLine();
        ret.setChartCode(costCenterGroupValueSplit[0]);
        ret.setAccountNumber(costCenterGroupValueSplit[1]);
        ret.setSubAccountNumber(costCenterGroupValueSplit[2]);
        ret.setObjectCode(costCenterGroupValueSplit[3]);
        ret.setSubObjectCode(costCenterGroupValueSplit[4]);
        ret.setProjectCode(costCenterGroupValueSplit[5]);
        ret.setOrgRefId(costCenterGroupValueSplit[6]);

        return ret;
    }

    private boolean validateAccount(String chartCode, String accountNumber) {
        if (StringUtils.isBlank(accountNumber)) {
            LOG.error("Account Number cannot be blank.");
            return false;
        }

        Account account = accountService.getByPrimaryId(chartCode, accountNumber);
        if (ObjectUtils.isNull(account)) {
            LOG.error(String.format("Could not find Account (%s, %s).", chartCode, accountNumber));
            return false;
        }
        if (!account.isActive()) {
            LOG.error(String.format("Invalid Account (%s, %s) is not active.", chartCode, accountNumber));
            return false;
        }
        if (account.isClosed()) {
            LOG.error(String.format("Invalid Account (%s, %s) is closed.", chartCode, accountNumber));
            return false;
        }
        if (account.isExpired()) {
            LOG.error(String.format("Invalid Account (%s, %s) is expired.", chartCode, accountNumber));
            return false;
        }
        return true;
    }

    private boolean validateObjectCode(String chartCode, String objectCodeValue) {
        if (StringUtils.isBlank(objectCodeValue)) {
            return false;
        }

        ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(chartCode, objectCodeValue);
        if (ObjectUtils.isNull(objectCode)) {
            LOG.error(String.format("Could not find Object Code (%s, %s).", chartCode, objectCodeValue));
            return false;
        }
        if (!objectCode.isActive()){
            LOG.error(String.format("Invalid Object Code (%s, %s) is not active.", chartCode, objectCodeValue));
            return false;
        }
        return true;
    }

    private boolean validateSubAccountNumber(String chartCode, String accountNumber, String subAccountNumber) {
        if (StringUtils.isBlank(subAccountNumber)) {
            return false;
        }

        SubAccount subAccount = subAccountService.getByPrimaryId(chartCode, accountNumber, subAccountNumber);
        if (ObjectUtils.isNull(subAccount)) {
            LOG.error(String.format("Could not find Sub-Account (%s, %s, %s).", chartCode, accountNumber, subAccountNumber));
            return false;
        }
        if (!subAccount.isActive()){
            LOG.error(String.format("Invalid Sub-Account (%s, %s, %s) is not active.", chartCode, accountNumber, subAccountNumber));
            return false;
        }
        return true;
    }

    private boolean validateSubObjectCode(String chartCode, String accountNumber, String objectCode, String subObjectCode) {
        if (StringUtils.isBlank(subObjectCode)) {
            return false;
        }

        SubObjectCode subObject = subObjectCodeService.getByPrimaryIdForCurrentYear(chartCode, accountNumber, objectCode, subObjectCode);
        if (ObjectUtils.isNull(subObject)) {
            LOG.error(String.format("Could not find Sub-Object Code (%s, %s, %s, %s).", chartCode, accountNumber, objectCode, subObjectCode));
            return false;
        }
        if (!subObject.isActive()){
            LOG.error(String.format("Invalid Sub-Object Code (%s, %s, %s, %s) is not active.", chartCode, accountNumber, objectCode, subObjectCode));
            return false;
        }
        return true;
    }

    private boolean validateProjectCode(String projectCodeValue) {
        if (StringUtils.isBlank(projectCodeValue)) {
            return false;
        }

        ProjectCode projectCode = projectCodeService.getByPrimaryId(projectCodeValue);
        if (ObjectUtils.isNull(projectCode)) {
            LOG.error(String.format("Could not find Project Code %s", projectCodeValue));
            return false;
        }
        if (!projectCode.isActive()){
            LOG.error(String.format("Invalid Project Code %s is not Active", projectCodeValue));
            return false;
        }
        return true;
    }

    private boolean validateOrgRefId(String orgRefId) {
        if (StringUtils.isBlank(orgRefId)) {
            return false;
        }
        if (orgRefId.length() > 8) {
            LOG.error(String.format("Organization Reference ID cannot be more than 8 characters in length. (%s)", orgRefId));
            return false;
        }
        return true;
    }

    private AccountingXmlDocumentAccountingLine getDefaultAccountingLine(String defaultAccountString) {
        AccountingXmlDocumentAccountingLine xmlAccountingLine = getEmptyAccountingXmlDocumentAccountingLine();

        if (StringUtils.countMatches(defaultAccountString, "*") == 6) {
            xmlAccountingLine = parseCostCenterGroupLevel(defaultAccountString);
        }
        else if (StringUtils.countMatches(defaultAccountString, "-") == 1) {
            xmlAccountingLine.setAccountNumber(StringUtils.split(defaultAccountString, "-")[0]);
            xmlAccountingLine.setSubAccountNumber(StringUtils.split(defaultAccountString, "-")[1]);
        }
        else {
            xmlAccountingLine.setAccountNumber(defaultAccountString);
        }

        validateAccount(xmlAccountingLine.getChartCode(), defaultAccountString);
        return xmlAccountingLine;
    }

    private String getDefaultChartCodeFromParameter() {
        return getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_CHART_CODE_PROPERTY_NAME);
    }

    private String getDefaultObjectCodeFromParameter() {
        return getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_OBJECT_CODE_PROPERTY_NAME);
    }

    private AccountingXmlDocumentAccountingLine getEmptyAccountingXmlDocumentAccountingLine() {
        AccountingXmlDocumentAccountingLine xmlAccountingLine = new AccountingXmlDocumentAccountingLine();
        xmlAccountingLine.setChartCode(getDefaultChartCodeFromParameter());
        xmlAccountingLine.setSubAccountNumber(StringUtils.EMPTY);
        xmlAccountingLine.setObjectCode(getDefaultObjectCodeFromParameter());
        xmlAccountingLine.setSubObjectCode(StringUtils.EMPTY);
        xmlAccountingLine.setProjectCode(StringUtils.EMPTY);
        xmlAccountingLine.setOrgRefId(StringUtils.EMPTY);
        xmlAccountingLine.setLineDescription(StringUtils.EMPTY);
        return xmlAccountingLine;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
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
}
