package edu.cornell.kfs.fp.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        
        if (!resultsDto.defaultAccountsWithErrors.contains(defaultAccountDto) && !validateAmazonKfsAccountDTO(defaultAccountDto, false)) {
            resultsDto.defaultAccountsWithErrors.add(defaultAccountDto);
        }
        
        AmazonKfsAccountDTO costCenterDto = new AmazonKfsAccountDTO(defaultKfsAccountForAws.getAwsAccount(), costCenterGroupValue, 
                getDefaultChartCodeFromParameter(), getDefaultObjectCodeFromParameter());
        
        if (!resultsDto.costCentersWithErrors.contains(defaultAccountDto) && !validateAmazonKfsAccountDTO(costCenterDto, false)) {
            resultsDto.costCentersWithErrors.add(costCenterDto);
        }
        
        AccountingXmlDocumentAccountingLine xmlAccountingLine = buildAccountingXmlDocumentAccountingLineFromAmazonKfsAccountDTO(costCenterDto);

        validateAndSetDefaultsAccountingLine(xmlAccountingLine, defaultAccountDto);
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
                && !validateSubAccountNumber(accountDto.getKfsChart(), accountDto.getKfsAccount(), accountDto.getKfsSubAccount(), logErrorMessage)) {
            valid = false;
        } else if (!validateObjectCode(accountDto.getKfsChart(), accountDto.getKfsObject(), logErrorMessage)) {
            valid = false;
        } else if (StringUtils.isNotBlank(accountDto.getKfsSubObject()) 
                && !validateSubObjectCode(accountDto.getKfsChart(), accountDto.getKfsAccount(), accountDto.getKfsObject(), accountDto.getKfsSubObject(), logErrorMessage)) {
            valid = false;
        } else if (StringUtils.isNotBlank(accountDto.getKfsProject())  && !validateProjectCode(accountDto.getKfsProject(), logErrorMessage)) {
            valid = false;
        } else if (StringUtils.isNotBlank(accountDto.getKfsOrgRefId())  && !validateOrgRefId(accountDto.getKfsOrgRefId(), logErrorMessage)) {
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
    
    private void validateAndSetDefaultsAccountingLine(AccountingXmlDocumentAccountingLine xmlAccountingLine, AmazonKfsAccountDTO defaultAccountDto) {
        boolean logErrorMessage = true;
        validateAndSetDefaultsRequiredFields(xmlAccountingLine, defaultAccountDto, logErrorMessage);
        validateAndSetDefaultsNonRequiredFields(xmlAccountingLine, defaultAccountDto, logErrorMessage);
    }
    
    private void validateAndSetDefaultsRequiredFields(AccountingXmlDocumentAccountingLine xmlAccountingLine, AmazonKfsAccountDTO defaultAccountDto,
            boolean logErrorMessage) {
        validateAndSetDefaultsChart(xmlAccountingLine, defaultAccountDto, logErrorMessage);
        validateAndSetDefaultsAccount(xmlAccountingLine, defaultAccountDto, logErrorMessage);
        validateAndSetDefaultsObjectCode(xmlAccountingLine, defaultAccountDto, logErrorMessage);
    }
    
    private void validateAndSetDefaultsNonRequiredFields(AccountingXmlDocumentAccountingLine xmlAccountingLine, AmazonKfsAccountDTO defaultAccountDto,
            boolean logErrorMessage) {
        validateAndSetDefaultsSubAccount(xmlAccountingLine, defaultAccountDto, logErrorMessage);
        validateAndSetDefaultsSubObjectCode(xmlAccountingLine, defaultAccountDto, logErrorMessage);
        validateAndSetDefaultsProjectCode(xmlAccountingLine, defaultAccountDto, logErrorMessage);
        validateAndSetDefaultsOrgRefId(xmlAccountingLine, defaultAccountDto, logErrorMessage);
        
    }

    private void validateAndSetDefaultsChart(AccountingXmlDocumentAccountingLine xmlAccountingLine, AmazonKfsAccountDTO defaultAccountDto,
            boolean logErrorMessage) {
        if (!validateChartCode(xmlAccountingLine.getChartCode(), logErrorMessage)) {
            xmlAccountingLine.setChartCode(defaultAccountDto.getKfsChart());
        }
    }

    private void validateAndSetDefaultsAccount(AccountingXmlDocumentAccountingLine xmlAccountingLine,
            AmazonKfsAccountDTO defaultAccountDto, boolean logErrorMessage) {
        if (StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.ACCOUNT_NONE, xmlAccountingLine.getAccountNumber()) 
                || StringUtils.equalsIgnoreCase(CuFPConstants.AmazonWebServiceBillingConstants.INTERNAL_KFS_ACCOUNT_DESCRIPTION, xmlAccountingLine.getAccountNumber()) 
                || !validateAccount(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), logErrorMessage)) {
            xmlAccountingLine.setChartCode(defaultAccountDto.getKfsChart());
            xmlAccountingLine.setAccountNumber(defaultAccountDto.getKfsAccount());
        }
    }

    private void validateAndSetDefaultsSubAccount(AccountingXmlDocumentAccountingLine xmlAccountingLine,
            AmazonKfsAccountDTO defaultAccountDto, boolean logErrorMessage) {
        if (StringUtils.isNotBlank(xmlAccountingLine.getSubAccountNumber())
                && !validateSubAccountNumber(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getSubAccountNumber(), logErrorMessage)) {
            if (validateSubAccountNumber(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), defaultAccountDto.getKfsSubAccount(), logErrorMessage)) {
                xmlAccountingLine.setSubAccountNumber(defaultAccountDto.getKfsSubAccount());
            } else {
                xmlAccountingLine.setSubAccountNumber(StringUtils.EMPTY);
            }
        }
    }

    private void validateAndSetDefaultsObjectCode(AccountingXmlDocumentAccountingLine xmlAccountingLine,
            AmazonKfsAccountDTO defaultAccountDto, boolean logErrorMessage) {
        if (!validateObjectCode(xmlAccountingLine.getChartCode(), xmlAccountingLine.getObjectCode(), logErrorMessage)) {
            xmlAccountingLine.setObjectCode(defaultAccountDto.getKfsObject());
        }
    }

    private void validateAndSetDefaultsSubObjectCode(AccountingXmlDocumentAccountingLine xmlAccountingLine,
            AmazonKfsAccountDTO defaultAccountDto, boolean logErrorMessage) {
        if (StringUtils.isNotBlank(xmlAccountingLine.getSubObjectCode())
                && !validateSubObjectCode(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getObjectCode(),
                        xmlAccountingLine.getSubObjectCode(), logErrorMessage)) {
            if (validateSubObjectCode(xmlAccountingLine.getChartCode(), xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getObjectCode(), 
                    defaultAccountDto.getKfsSubObject(), logErrorMessage)) {
                xmlAccountingLine.setSubObjectCode(defaultAccountDto.getKfsSubObject());
            } else {
                xmlAccountingLine.setSubObjectCode(StringUtils.EMPTY);
            }
        }
    }

    private void validateAndSetDefaultsProjectCode(AccountingXmlDocumentAccountingLine xmlAccountingLine,
            AmazonKfsAccountDTO defaultAccountDto, boolean logErrorMessage) {
        if (StringUtils.isNotBlank(xmlAccountingLine.getProjectCode())
                && !validateProjectCode(xmlAccountingLine.getProjectCode(), logErrorMessage)) {
            if (validateProjectCode(defaultAccountDto.getKfsProject(), logErrorMessage)) {
                xmlAccountingLine.setProjectCode(defaultAccountDto.getKfsProject()); 
            } else {
                xmlAccountingLine.setProjectCode(StringUtils.EMPTY);
            }
        }
    }

    private void validateAndSetDefaultsOrgRefId(AccountingXmlDocumentAccountingLine xmlAccountingLine,
            AmazonKfsAccountDTO defaultAccountDto, boolean logErrorMessage) {
        if (StringUtils.isNotBlank(xmlAccountingLine.getOrgRefId())
                && !validateOrgRefId(xmlAccountingLine.getOrgRefId(), logErrorMessage)) {
            if (validateOrgRefId(defaultAccountDto.getKfsOrgRefId(), logErrorMessage)) {
                xmlAccountingLine.setOrgRefId(defaultAccountDto.getKfsOrgRefId());
            } else {
                xmlAccountingLine.setOrgRefId(StringUtils.EMPTY);
            }
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
        if (StringUtils.isNotBlank(objectCodeValue)) {
            ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(chartCode, objectCodeValue);
            if (ObjectUtils.isNull(objectCode)) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND), 
                        chartCode, objectCodeValue), logErrorMessage);
                valid = false;
            } else if (!objectCode.isActive()) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_INACTIVE), 
                        chartCode, objectCodeValue), logErrorMessage);
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateSubAccountNumber(String chartCode, String accountNumber, String subAccountNumber, boolean logErrorMessage) {
        boolean valid = true;
        if (StringUtils.isNotBlank(subAccountNumber)) {
            SubAccount subAccount = subAccountService.getByPrimaryId(chartCode, accountNumber, subAccountNumber);
            if (ObjectUtils.isNull(subAccount)) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_NOT_FOUND), 
                        chartCode, accountNumber, subAccountNumber), logErrorMessage);
                valid = false;
            } else if (!subAccount.isActive()) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_INACTIVE), 
                        chartCode, accountNumber, subAccountNumber), logErrorMessage);
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateSubObjectCode(String chartCode, String accountNumber, String objectCode, String subObjectCode, boolean logErrorMessage) {
        boolean valid = true;
        if (StringUtils.isNotBlank(subObjectCode)) {
            SubObjectCode subObject = subObjectCodeService.getByPrimaryIdForCurrentYear(chartCode, accountNumber, objectCode, subObjectCode);
            if (ObjectUtils.isNull(subObject)) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_NOT_FOUND), 
                        chartCode, accountNumber, objectCode, subObjectCode), logErrorMessage);
                valid = false;
            } else if (!subObject.isActive()) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_INACTIVE), 
                        chartCode, accountNumber, objectCode, subObjectCode), logErrorMessage);
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateProjectCode(String projectCodeValue, boolean logErrorMessage) {
        boolean valid = true;
        if (StringUtils.isNotBlank(projectCodeValue)) {
            ProjectCode projectCode = projectCodeService.getByPrimaryId(projectCodeValue);
            if (ObjectUtils.isNull(projectCode)) {
                logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_NOT_FOUND), projectCodeValue), logErrorMessage);
                valid = false;
            } else if (!projectCode.isActive()) {
                LOG.error(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_INACTIVE), projectCodeValue));
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateOrgRefId(String orgRefId, boolean logErrorMessage) {
        if (StringUtils.isNotBlank(orgRefId) && orgRefId.length() > 8) {
            logAccountValidationError(String.format(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ORG_REF_ID_TOO_LONG), orgRefId), logErrorMessage);
            return false;
        }
        return true;
    }
    
    private void logAccountValidationError(String message, boolean logErrorMessage) {
        if (logErrorMessage) {
            LOG.error("logAccountValidationError, " + message);
        }
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
