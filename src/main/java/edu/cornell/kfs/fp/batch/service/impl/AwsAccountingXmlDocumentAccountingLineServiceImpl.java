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
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;

public class AwsAccountingXmlDocumentAccountingLineServiceImpl implements AwsAccountingXmlDocumentAccountingLineService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImpl.class);

    protected ParameterService parameterService;
    protected ChartService chartService;
    protected AccountService accountService;
    protected SubAccountService subAccountService;
    protected ObjectCodeService objectCodeService;
    protected SubObjectCodeService subObjectCodeService;
    protected ProjectCodeService projectCodeService;
    protected ConfigurationService configurationService;

    @Override
    public AccountingXmlDocumentAccountingLine createAccountingXmlDocumentAccountingLine(GroupLevel costCenterGroupLevel,
                                                                                         DefaultKfsAccountForAws defaultKfsAccountForAws) throws IllegalArgumentException {
        LOG.debug("createAccountingXmlDocumentAccountingLine for " + costCenterGroupLevel.getGroupName());
        if (!costCenterGroupLevel.isCostCenterGroupLevel()) {
            throw new IllegalArgumentException(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_INVALID_GROUP_LEVEL_TYPE));
        }

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
            String defaultChartCode = getDefaultChartCodeFromParameter();
            xmlAccountingLine.setChartCode(defaultChartCode);
        }

        if (StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.ACCOUNT_NONE, xmlAccountingLine.getAccountNumber())) {
            xmlAccountingLine = getDefaultAccountingLine(defaultAccountString);
        }

        if (!StringUtils.equalsIgnoreCase(defaultAccountString, CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION) &&
                !validateAccount(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber())) {
            xmlAccountingLine = getDefaultAccountingLine(defaultAccountString);
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
        if (ObjectUtils.isNull(chart)) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_NOT_FOUND), chartCode));
            return false;
        }
        if (!chart.isActive()) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_INACTIVE), chartCode));
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
            LOG.error(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NUMBER_BLANK));
            return false;
        }

        if (StringUtils.equalsIgnoreCase(accountNumber, CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION)) {
            return true;
        }

        Account account = accountService.getByPrimaryId(chartCode, accountNumber);
        if (ObjectUtils.isNull(account)) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NOT_FOUND), chartCode, accountNumber));
            return false;
        }
        if (account.isClosed()) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_CLOSED), chartCode, accountNumber));
            return false;
        }
        if (account.isExpired()) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_EXPIRED), chartCode, accountNumber));
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
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND), chartCode, objectCodeValue));
            return false;
        }
        if (!objectCode.isActive()) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_INACTIVE), chartCode, objectCodeValue));
            return false;
        }
        return true;
    }

    private boolean validateSubAccountNumber(String chartCode, String accountNumber, String subAccountNumber) {
        if (StringUtils.isBlank(subAccountNumber)) {
            return true;
        }

        SubAccount subAccount = subAccountService.getByPrimaryId(chartCode, accountNumber, subAccountNumber);
        if (ObjectUtils.isNull(subAccount)) {
            String errorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_NOT_FOUND);
            LOG.error(String.format(errorMessage, chartCode, accountNumber, subAccountNumber));
            return false;
        }
        if (!subAccount.isActive()) {
            String errorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_INACTIVE);
            LOG.error(String.format(errorMessage, chartCode, accountNumber, subAccountNumber));
            return false;
        }
        return true;
    }

    private boolean validateSubObjectCode(String chartCode, String accountNumber, String objectCode, String subObjectCode) {
        if (StringUtils.isBlank(subObjectCode)) {
            return true;
        }

        SubObjectCode subObject = subObjectCodeService.getByPrimaryIdForCurrentYear(chartCode, accountNumber, objectCode, subObjectCode);
        if (ObjectUtils.isNull(subObject)) {
            String errorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_NOT_FOUND);
            LOG.error(String.format(errorMessage, chartCode, accountNumber, objectCode, subObjectCode));
            return false;
        }
        if (!subObject.isActive()) {
            String errorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_INACTIVE);
            LOG.error(String.format(errorMessage, chartCode, accountNumber, objectCode, subObjectCode));
            return false;
        }
        return true;
    }

    private boolean validateProjectCode(String projectCodeValue) {
        if (StringUtils.isBlank(projectCodeValue)) {
            return true;
        }

        ProjectCode projectCode = projectCodeService.getByPrimaryId(projectCodeValue);
        if (ObjectUtils.isNull(projectCode)) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_NOT_FOUND), projectCodeValue));
            return false;
        }
        if (!projectCode.isActive()) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_INACTIVE), projectCodeValue));
            return false;
        }
        return true;
    }

    private boolean validateOrgRefId(String orgRefId) {
        if (StringUtils.isBlank(orgRefId)) {
            return true;
        }
        if (orgRefId.length() > 8) {
            LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ORG_REF_ID_TOO_LONG), orgRefId));
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
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
