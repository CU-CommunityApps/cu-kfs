package edu.cornell.kfs.fp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.service.AwsAccountingXmlDocumentAccountingLineService;

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
    public AccountingXmlDocumentAccountingLine createAccountingXmlDocumentAccountingLine(GroupLevel costCenterGroupLevel, DefaultKfsAccountForAws defaultKfsAccountForAws) {
        LOG.info("createAccountingXmlDocumentAccountingLine");

        String costCenterGroupValue = costCenterGroupLevel.getGroupValue();
        String accountingLineAccountNumber = getAccountingLineAccountNumber(costCenterGroupValue, defaultKfsAccountForAws);
        String chartCode = getChartCode(costCenterGroupValue);
        String objectCode = getObjectCode(costCenterGroupValue);
        String subAccountNumber = getSubAccountNumber(costCenterGroupValue);
        String subObjectCode = getSubObjectCode(costCenterGroupValue);
        String projectCode = getProjectCode(costCenterGroupValue);
        String organizationReferenceId = getOrganizationReferenceId(costCenterGroupValue);

        AccountingXmlDocumentAccountingLine xmlDocumentAccountingLine = new AccountingXmlDocumentAccountingLine();
        xmlDocumentAccountingLine.setAccountNumber(accountingLineAccountNumber);
        xmlDocumentAccountingLine.setChartCode(chartCode);
        xmlDocumentAccountingLine.setObjectCode(objectCode);
        xmlDocumentAccountingLine.setAmount(costCenterGroupLevel.getCost());

        if (StringUtils.isNotBlank(subAccountNumber)) {
            xmlDocumentAccountingLine.setSubAccountNumber(subObjectCode);
        }
        if (StringUtils.isNotBlank(subObjectCode)) {
            xmlDocumentAccountingLine.setSubObjectCode(subObjectCode);
        }
        if (StringUtils.isNotBlank(projectCode)) {
            xmlDocumentAccountingLine.setProjectCode(projectCode);
        }
        if (StringUtils.isNotBlank(organizationReferenceId)) {
            xmlDocumentAccountingLine.setOrgRefId(organizationReferenceId);
        }

        return xmlDocumentAccountingLine;
    }

    public boolean validate(AccountingXmlDocumentAccountingLine xmlAccountingLine) {
        boolean validationPassed = true;
        String chartCode = xmlAccountingLine.getChartCode();
        String accountNumber = xmlAccountingLine.getAccountNumber();
        String objectCode = xmlAccountingLine.getObjectCode();

        if (!chartService.getByPrimaryId(chartCode).isActive()) {
            GlobalVariables.getMessageMap().putError("chartCode", KFSKeyConstants.ERROR_CUSTOM, "Chart is not active");
            LOG.error("Invalid Chart " + chartCode + " is not active.");
            validationPassed = false;
        }

        Account account = accountService.getByPrimaryId(chartCode, accountNumber);
        if (!account.isActive()){
            GlobalVariables.getMessageMap().putError("chartCode", KFSKeyConstants.ERROR_CUSTOM, "Account is not active");
            LOG.error(String.format("Invalid Account (%s, %s) is not active.", chartCode, accountNumber));
            validationPassed = false;
        }
        if (account.isClosed()){
            GlobalVariables.getMessageMap().putError("accountNumber", KFSKeyConstants.ERROR_CUSTOM, "Account is closed");
            LOG.error(String.format("Invalid Account (%s, %s) is closed.", chartCode, accountNumber));
            validationPassed = false;
        }

        if (!objectCodeService.getByPrimaryIdForCurrentYear(chartCode, objectCode).isActive()) {
            GlobalVariables.getMessageMap().putError("objectCode", KFSKeyConstants.ERROR_CUSTOM, "Object Code is not active");
            LOG.error(String.format("Invalid Object Code (%s, %s) is not active.", chartCode, objectCode));
            validationPassed = false;
        }

        if (StringUtils.isNotBlank(xmlAccountingLine.getSubAccountNumber()) &&
                !subAccountService.getByPrimaryId(chartCode, xmlAccountingLine.getAccountNumber(), xmlAccountingLine.getSubAccountNumber()).isActive()) {
            GlobalVariables.getMessageMap().putError("subAccount", KFSKeyConstants.ERROR_CUSTOM, "Sub Account is not active");
            LOG.error(String.format("Invalid Sub Account (%s, %s, %s) is not active.", chartCode, account, xmlAccountingLine.getSubAccountNumber()));
            validationPassed = false;
        }

        if (StringUtils.isNotBlank(xmlAccountingLine.getSubObjectCode()) &&
                !subObjectCodeService.getByPrimaryIdForCurrentYear(chartCode, accountNumber, objectCode, xmlAccountingLine.getSubObjectCode()).isActive()) {
            GlobalVariables.getMessageMap().putError("subObject", KFSKeyConstants.ERROR_CUSTOM, "Sub Object is not active");
            LOG.error(String.format("Invalid Sub Object (%s, %s, %s) is not active.", chartCode, account, xmlAccountingLine.getSubObjectCode()));
            validationPassed = false;
        }

        if (StringUtils.isNotBlank(xmlAccountingLine.getProjectCode()) && !projectCodeService.getByPrimaryId(xmlAccountingLine.getProjectCode()).isActive()) {
            GlobalVariables.getMessageMap().putError("projectCode", KFSKeyConstants.ERROR_CUSTOM, "Project Code is not active");
            LOG.error(String.format("Invalid Project Code %s is not active.", xmlAccountingLine.getProjectCode()));
            validationPassed = false;
        }

        if (StringUtils.isNotBlank(xmlAccountingLine.getOrgRefId()) && xmlAccountingLine.getOrgRefId().length() > 8) {
            GlobalVariables.getMessageMap().putError("orgRefId", KFSKeyConstants.ERROR_CUSTOM, "Organization Reference ID is too long");
            LOG.error(String.format("Organization Reference ID cannot be more than 8 characters in length. (%s)", xmlAccountingLine.getOrgRefId()));
            validationPassed = false;
        }

        return validationPassed;
    }

    protected String getChartCode(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*")) {
            String chartCode = StringUtils.split(costCenterGroupLevelValue,"*")[0].trim();
            if (StringUtils.isNotBlank(chartCode)) {
                return chartCode;
            }
        }
        return "6600"; //getParameterService().getParameterValueAsString("AWS_OBJECT_CODE");
    }

    protected String getAccountingLineAccountNumber(String costCenterGroupLevelValue, DefaultKfsAccountForAws defaultKfsAccountForAws) {
        if (StringUtils.contains(costCenterGroupLevelValue, "-")) {
            String accountNumber = StringUtils.split(costCenterGroupLevelValue, "-")[0];
            if (StringUtils.isNotBlank(accountNumber)) {
                return accountNumber;
            }
        }
        if (StringUtils.equalsIgnoreCase(costCenterGroupLevelValue, "None")){
            return defaultKfsAccountForAws.getKfsDefaultAccount();
        }
        if (StringUtils.contains(costCenterGroupLevelValue, "*")){
            return StringUtils.split(costCenterGroupLevelValue,"*")[1].trim();
        }

        return costCenterGroupLevelValue;
    }

    protected String getSubAccountNumber(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*")) {
            return StringUtils.split(costCenterGroupLevelValue,"*")[2].trim();
        }
        if(StringUtils.contains(costCenterGroupLevelValue, "-")) {
            return StringUtils.split(costCenterGroupLevelValue,"-")[1];
        }
        return null;
    }

    protected String getObjectCode(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*")) {
            String objectCode = StringUtils.split(costCenterGroupLevelValue,"*")[3].trim();
            if (StringUtils.isNotBlank(objectCode)) {
                return objectCode;
            }
        }
        return "6600"; //getParameterService().getParameterValueAsString("AWS_OBJECT_CODE");
    }

    protected String getSubObjectCode(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*")) {
            return StringUtils.split(costCenterGroupLevelValue,"*")[4].trim();
        }
        return null;
    }

    protected String getProjectCode(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*")) {
            return StringUtils.split(costCenterGroupLevelValue,"*")[5].trim();
        }
        return null;
    }

    protected String getOrganizationReferenceId(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*")) {
            return StringUtils.split(costCenterGroupLevelValue,"*")[6].trim();
        }
        return null;
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
