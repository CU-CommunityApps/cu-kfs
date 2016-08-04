package edu.cornell.kfs.fp.businessobject;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public class AmazonBillingCostCenterDTO implements Serializable {

    private static final long serialVersionUID = 2223891834883162575L;

    private String chartCode;
    private String accountNumber;
    private String subAccountNumber;
    private String objectCode;
    private String subObjectCode;
    private String projectCode;
    private String orgReferenceId;

    public AmazonBillingCostCenterDTO() {
        final String empty = "";
        chartCode = empty;
        accountNumber = empty;
        subAccountNumber = empty;
        objectCode = empty;
        subObjectCode = empty;
        projectCode = empty;
        orgReferenceId = empty;
    }

    public String getChartCode() {
        return chartCode;
    }

    public void setChartCode(String chartCode) {
        if (StringUtils.isNotBlank(chartCode)) {
            this.chartCode = chartCode;
        }
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        if (StringUtils.isNotBlank(accountNumber)) {
            this.accountNumber = accountNumber;
        }
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        if (StringUtils.isNotBlank(subAccountNumber)) {
            this.subAccountNumber = subAccountNumber;
        }
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        if (StringUtils.isNotBlank(objectCode)) {
            this.objectCode = objectCode;
        }
    }

    public String getSubObjectCode() {
        return subObjectCode;
    }

    public void setSubObjectCode(String subObjectCode) {
        if (StringUtils.isNotBlank(subObjectCode)) {
            this.subObjectCode = subObjectCode;
        }
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        if (StringUtils.isNotBlank(projectCode)) {
            this.projectCode = projectCode;
        }
    }

    public String getOrgReferenceId() {
        return orgReferenceId;
    }

    public void setOrgReferenceId(String orgReferenceId) {
        if (StringUtils.isNotBlank(orgReferenceId)) {
            this.orgReferenceId = orgReferenceId;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(chartCode).append(KFSConstants.DASH).append(accountNumber).append(KFSConstants.DASH);
        sb.append(subAccountNumber).append(KFSConstants.DASH).append(objectCode).append(KFSConstants.DASH);
        sb.append(subObjectCode).append(KFSConstants.DASH).append(projectCode).append(KFSConstants.DASH).append(orgReferenceId);
        return sb.toString();

    }
}
