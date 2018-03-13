package edu.cornell.kfs.fp.batch.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;

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
        LOG.info("createAccountingXmlDocumentAccountingLine for " + costCenterGroupLevel.getGroupName());

        String costCenterGroupValue = costCenterGroupLevel.getGroupValue();
        String chartCode = getChartCode(costCenterGroupValue);
        String accountingLineAccountNumber = getAccountingLineAccountNumber(costCenterGroupValue, defaultKfsAccountForAws, chartCode);
        String objectCode = getObjectCode(chartCode, costCenterGroupValue);
        String subAccountNumber = getSubAccountNumber(chartCode, accountingLineAccountNumber, costCenterGroupValue);
        String subObjectCode = getSubObjectCode(chartCode, accountingLineAccountNumber, objectCode, costCenterGroupValue);
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

    protected String getChartCode(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*")) {
            String chartCode = StringUtils.split(costCenterGroupLevelValue,"*")[0].trim();
            if (StringUtils.isNotBlank(chartCode)) {
                if(chartService.getByPrimaryId(chartCode).isActive()) {
                    return chartCode;
                }
                else{
                    LOG.error("AwsAccountingXmlDocumentAccountingLineServiceImpl: Invalid Chart " + chartCode + " is not active.");
                }
            }
        }
        return "IT"; //getParameterService().getParameterValueAsString("AWS_OBJECT_CODE");
    }

    protected String getAccountingLineAccountNumber(String costCenterGroupLevelValue, DefaultKfsAccountForAws defaultKfsAccountForAws, String chartCode) {
        String accountNumber = null;
        if (StringUtils.contains(costCenterGroupLevelValue, "-")) {
            accountNumber = StringUtils.split(costCenterGroupLevelValue, "-")[0];
        }
        else if (StringUtils.equalsIgnoreCase(costCenterGroupLevelValue, "None")){
            return defaultKfsAccountForAws.getKfsDefaultAccount();
        }
        else if (StringUtils.contains(costCenterGroupLevelValue, "*")){
            accountNumber = StringUtils.split(costCenterGroupLevelValue,"*")[1].trim();
        }

        if (validateAccount(chartCode, accountNumber)) {
            return accountNumber;
        }

        return costCenterGroupLevelValue;
    }

    protected boolean validateAccount(String chartCode, String accountNumber) {
        if (StringUtils.isBlank(accountNumber)) {
            LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Account Number cannot be blank.", chartCode, accountNumber));
            return false;
        }

        Account account = accountService.getByPrimaryId(chartCode, accountNumber);
        if (!account.isActive()){
            LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Invalid Account (%s, %s) is not active.", chartCode, accountNumber));
            return false;
        }
        if (account.isClosed()){
            LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Invalid Account (%s, %s) is closed.", chartCode, accountNumber));
            return false;
        }
        return true;
    }

    protected String getSubAccountNumber(String chartCode, String accountNumber, String costCenterGroupLevelValue) {
        String subAccountNumber = null;
        if (StringUtils.contains(costCenterGroupLevelValue, "*") && StringUtils.split(costCenterGroupLevelValue,"*").length >= 3) {
            subAccountNumber = StringUtils.split(costCenterGroupLevelValue,"*")[2].trim();
        }
        if(StringUtils.contains(costCenterGroupLevelValue, "-")) {
            subAccountNumber = StringUtils.split(costCenterGroupLevelValue,"-")[1];
        }

        if (StringUtils.isNotBlank(subAccountNumber)) {
            if (subAccountService.getByPrimaryId(chartCode, accountNumber, subAccountNumber).isActive()){
                return subAccountNumber;
            }
            else {
                LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Invalid Sub Account (%s, %s, %s) is not active.", chartCode, accountNumber, subAccountNumber));
            }
        }

        return null;
    }

    protected String getObjectCode(String chartCode, String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*") && StringUtils.split(costCenterGroupLevelValue,"*").length >= 4) {
            String objectCode = StringUtils.split(costCenterGroupLevelValue,"*")[3].trim();
            if (StringUtils.isNotBlank(objectCode)) {
                if (objectCodeService.getByPrimaryIdForCurrentYear(chartCode, objectCode).isActive()) {
                    return objectCode;
                }
                else{
                    LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Invalid Object Code (%s, %s) is not active.", chartCode, objectCode));
                }
            }
        }
        return "6600"; //getParameterService().getParameterValueAsString("AWS_OBJECT_CODE");
    }

    protected String getSubObjectCode(String chartCode, String accountNumber, String financialObjectCode, String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*") && StringUtils.split(costCenterGroupLevelValue,"*").length >= 5) {
            String financialSubObjectCode = StringUtils.split(costCenterGroupLevelValue,"*")[4].trim();
            if (StringUtils.isNotBlank(financialSubObjectCode)) {
                if (subObjectCodeService.getByPrimaryIdForCurrentYear(chartCode, accountNumber, financialObjectCode, financialSubObjectCode).isActive()) {
                    return financialSubObjectCode;
                }
                else{
                    LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Invalid financial sub-object Code (%s, %s, %s, %s) is not active.",
                            chartCode, accountNumber, financialObjectCode, financialSubObjectCode));
                }
            }
        }
        return null;
    }

    protected String getProjectCode(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*") && StringUtils.split(costCenterGroupLevelValue,"*").length >= 6) {
            String projectCode = StringUtils.split(costCenterGroupLevelValue,"*")[5].trim();
            if (StringUtils.isNotBlank(projectCode)) {
                if (projectCodeService.getByPrimaryId(projectCode).isActive()) {
                    return projectCode;
                }
                else{
                    LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Invalid Project Code %s is not active.", projectCode));
                }
            }
        }
        return null;
    }

    protected String getOrganizationReferenceId(String costCenterGroupLevelValue) {
        if (StringUtils.contains(costCenterGroupLevelValue, "*") && StringUtils.split(costCenterGroupLevelValue,"*").length >= 7) {
            String orgRefId = StringUtils.split(costCenterGroupLevelValue,"*")[6].trim();
            if (StringUtils.isNotBlank(orgRefId)){
                if (orgRefId.length() <= 8) {
                    return orgRefId;
                }
                else{
                    LOG.error(String.format("AwsAccountingXmlDocumentAccountingLineServiceImpl: Organization Reference ID cannot be more than 8 characters in length. (%s)", orgRefId));
                }
            }
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
