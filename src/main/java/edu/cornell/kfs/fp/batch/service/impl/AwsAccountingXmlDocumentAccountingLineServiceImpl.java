package edu.cornell.kfs.fp.batch.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import edu.cornell.kfs.fp.batch.businessobject.AmazonBillResultsDTO;
import edu.cornell.kfs.fp.batch.businessobject.AmazonKfsAccountDTO;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;

public class AwsAccountingXmlDocumentAccountingLineServiceImpl implements AwsAccountingXmlDocumentAccountingLineService {

	private static final Logger LOG = LogManager.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImpl.class);

    protected ParameterService parameterService;
    protected ChartService chartService;
    protected AccountService accountService;
    protected SubAccountService subAccountService;
    protected ObjectCodeService objectCodeService;
    protected SubObjectCodeService subObjectCodeService;
    protected ProjectCodeService projectCodeService;
    protected ConfigurationService configurationService;

    @Override
    public AccountingXmlDocumentAccountingLine createAccountingXmlDocumentAccountingLine(GroupLevel costCenterGroupLevel, DefaultKfsAccountForAws defaultKfsAccountForAws, 
            AmazonBillResultsDTO resultsDto) throws IllegalArgumentException {
        LOG.debug("createAccountingXmlDocumentAccountingLine for " + costCenterGroupLevel.getGroupName());
        if (!costCenterGroupLevel.isCostCenterGroupLevel()) {
            throw new IllegalArgumentException(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_INVALID_GROUP_LEVEL_TYPE));
        }
        
        String costCenterGroupValue = costCenterGroupLevel.getGroupValue();
        
        AmazonKfsAccountDTO defaultAccountDto = new AmazonKfsAccountDTO(defaultKfsAccountForAws.getAwsAccount(), defaultKfsAccountForAws.getKfsDefaultAccount(), 
                getDefaultChartCodeFromParameter(), getDefaultObjectCodeFromParameter());
        
        if (!resultsDto.defaultAccountErrors.contains(defaultAccountDto) && !validateAmazonKfsAccountDTO(defaultAccountDto, false)) {
            resultsDto.defaultAccountErrors.add(defaultAccountDto);
        }
        
        AmazonKfsAccountDTO costCenterDto = new AmazonKfsAccountDTO(defaultKfsAccountForAws.getAwsAccount(), costCenterGroupValue, 
                getDefaultChartCodeFromParameter(), getDefaultObjectCodeFromParameter());
        
        if (!resultsDto.costCenterErrors.contains(defaultAccountDto) && !validateAmazonKfsAccountDTO(costCenterDto, false)) {
            resultsDto.costCenterErrors.add(defaultAccountDto);
        }
        
        AccountingXmlDocumentAccountingLine xmlAccountingLine = buildAccountingXmlDocumentAccountingLineFromAmazonKfsAccountDTO(costCenterDto);

        fixAccountingLine(xmlAccountingLine, defaultAccountDto);
        xmlAccountingLine.setAmount(costCenterGroupLevel.getCost());
        return xmlAccountingLine;
    }
    
    private boolean validateAmazonKfsAccountDTO(AmazonKfsAccountDTO accountDto, boolean logErrorMessage) {
        boolean valid = true;
        
        if (!validateChartCode(accountDto.getKfsChart(), logErrorMessage)) {
            valid = false;
        } else if (!StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.ACCOUNT_NONE, accountDto.getKfsAccount()) 
                && !StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION, accountDto.getKfsAccount()) 
                && !validateAccount(accountDto.getKfsChart(), accountDto.getKfsAccount(), logErrorMessage)) {
            valid = false;
        } else if (StringUtils.isNotBlank(accountDto.getKfsSubAccount()) 
                && !validateSubAccountNumber(accountDto.getKfsChart(), accountDto.getKfsAccount(), accountDto.getKfsSubAccount())) {
            valid = false;
        } else if (!validateObjectCode(accountDto.getKfsChart(), accountDto.getKfsObject(), logErrorMessage)) {
            valid = false;
        } else if (StringUtils.isNotBlank(accountDto.getKfsSubObject()) 
                && !validateSubObjectCode(accountDto.getKfsChart(), accountDto.getKfsAccount(), accountDto.getKfsObject(), accountDto.getKfsSubObject())) {
            valid = false;
        } else if (StringUtils.isNotBlank(accountDto.getKfsProject())  && !validateProjectCode(accountDto.getKfsProject())) {
            valid = false;
        } else if (StringUtils.isNotBlank(accountDto.getKfsOrgRefId())  && !validateOrgRefId(accountDto.getKfsOrgRefId())) {
            valid = false;
        }
        
        return valid;
    }
    
    private AccountingXmlDocumentAccountingLine buildAccountingXmlDocumentAccountingLineFromAmazonKfsAccountDTO(AmazonKfsAccountDTO accountDto) {
        AccountingXmlDocumentAccountingLine line = new AccountingXmlDocumentAccountingLine();
        line.setChartCode(accountDto.getKfsChart());
        line.setAccountNumber(accountDto.getKfsAccount());
        line.setSubAccountNumber(accountDto.getKfsSubAccount());
        line.setObjectCode(accountDto.getKfsObject());
        line.setSubObjectCode(accountDto.getKfsSubObject());
        line.setProjectCode(accountDto.getKfsProject());
        line.setOrgRefId(accountDto.getKfsOrgRefId());
        line.setLineDescription(StringUtils.EMPTY);
        return line;
    }
    
    private void fixAccountingLine(AccountingXmlDocumentAccountingLine xmlAccountingLine, AmazonKfsAccountDTO defaultAccountDto) {
        boolean logErrorMessage = true;
        if (!validateChartCode(xmlAccountingLine.getChartCode(), logErrorMessage)) {
            xmlAccountingLine.setChartCode(defaultAccountDto.getKfsChart());
        }
        
        if (StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.ACCOUNT_NONE, xmlAccountingLine.getAccountNumber()) 
                || StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION, xmlAccountingLine.getAccountNumber()) 
                || !validateAccount(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), logErrorMessage)) {
            xmlAccountingLine.setAccountNumber(defaultAccountDto.getKfsAccount());
        }
        
        if (StringUtils.isNotBlank(xmlAccountingLine.getSubAccountNumber())
                && !validateSubAccountNumber(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getSubAccountNumber())) {
            xmlAccountingLine.setSubAccountNumber(defaultAccountDto.getKfsSubAccount());
        }
        
        if (!validateObjectCode(xmlAccountingLine.getChartCode(), xmlAccountingLine.getObjectCode(), logErrorMessage)) {
            xmlAccountingLine.setObjectCode(defaultAccountDto.getKfsObject());
        }
        
        if (StringUtils.isNotBlank(xmlAccountingLine.getSubObjectCode())
                && !validateSubObjectCode(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getObjectCode(),
                xmlAccountingLine.getSubObjectCode())) {
            xmlAccountingLine.setSubObjectCode(defaultAccountDto.getKfsSubObject());
        }
        
        if (StringUtils.isNotBlank(xmlAccountingLine.getProjectCode())
                && !validateProjectCode(xmlAccountingLine.getProjectCode())) {
            xmlAccountingLine.setProjectCode(defaultAccountDto.getKfsProject());
        }
        
        if (StringUtils.isNotBlank(xmlAccountingLine.getOrgRefId())
                && !validateOrgRefId(xmlAccountingLine.getOrgRefId())) {
            xmlAccountingLine.setOrgRefId(defaultAccountDto.getKfsOrgRefId());
        }
    }

    private boolean validateChartCode(String chartCode, boolean logErrorMessage) {
        boolean valid = true;
        if (StringUtils.isBlank(chartCode)) {
            valid = false;
        } else {
            Chart chart = chartService.getByPrimaryId(chartCode);
            if (ObjectUtils.isNull(chart)) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_NOT_FOUND), chartCode), logErrorMessage);
                valid = false;
            } else if (!chart.isActive()) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_INACTIVE), chartCode), logErrorMessage);
                valid = false;
            }
        }
        return valid;
    }
    
    private void logAccountValidationError(String message, boolean logErrorMessage) {
        if (logErrorMessage) {
            LOG.error("logAccountValidationError, " + message);
        }
    }

    private boolean validateAccount(String chartCode, String accountNumber, boolean logErrorMessage) {
        boolean valid = true;
        if (StringUtils.isBlank(accountNumber)) {
            logAccountValidationError(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NUMBER_BLANK), logErrorMessage);
            valid = false;
        } else if (StringUtils.equalsIgnoreCase(accountNumber, CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION)) {
            valid = true;
        } else {
            Account account = accountService.getByPrimaryId(chartCode, accountNumber);
            if (ObjectUtils.isNull(account)) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NOT_FOUND), chartCode, accountNumber), logErrorMessage);
                valid = false;
            } else if (account.isClosed()) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_CLOSED), chartCode, accountNumber), logErrorMessage);
                valid = false;
            } else if (account.isExpired()) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_EXPIRED), chartCode, accountNumber), logErrorMessage);
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateObjectCode(String chartCode, String objectCodeValue, boolean logErrorMessage) {
        boolean valid = true;
        if (StringUtils.isBlank(objectCodeValue)) {
            valid = false;
        } else {
            ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(chartCode, objectCodeValue);
            if (ObjectUtils.isNull(objectCode)) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND), chartCode, objectCodeValue), logErrorMessage);
                LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND), chartCode, objectCodeValue));
                valid = false;
            } else if (!objectCode.isActive()) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_INACTIVE), chartCode, objectCodeValue), logErrorMessage);
                valid = false;
            }
        }
        return valid;
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

    private String getDefaultChartCodeFromParameter() {
        return getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPONENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_CHART_CODE_PARAMETER_NAME);
    }

    private String getDefaultObjectCodeFromParameter() {
        return getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPONENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_OBJECT_CODE_PARAMETER_NAME);
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
