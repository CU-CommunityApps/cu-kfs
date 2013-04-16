package edu.cornell.kfs.sys.businessobject;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.ObjectUtils;


public class FavoriteAccount extends PersistableBusinessObjectBase {

	private String description;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    private String subAccountNumber;
    private String financialSubObjectCode;
    private String projectCode;
    private String organizationReferenceId;
    private Integer accountLineIdentifier;
    private Integer userProfileId; // user profile PK
    private Boolean primaryInd;
    private Integer currentYear;  // not sure about this field yet


    // bo references
    private Chart chart;
    private Account account;
    private ObjectCode objectCode;
    private SubAccount subAccount;
    private SubObjectCode subObjectCode;
    private ProjectCode project;
    private UserProcurementProfile userProcurementProfile;
    
    public FavoriteAccount() {
        super();
        primaryInd = false;
    }
   

	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public String getChartOfAccountsCode() {
		return chartOfAccountsCode;
	}



	public void setChartOfAccountsCode(String chartOfAccountsCode) {
		this.chartOfAccountsCode = chartOfAccountsCode;
	}



	public String getAccountNumber() {
		return accountNumber;
	}



	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}



	public String getFinancialObjectCode() {
		return financialObjectCode;
	}



	public void setFinancialObjectCode(String financialObjectCode) {
		this.financialObjectCode = financialObjectCode;
	}



	public String getSubAccountNumber() {
		return subAccountNumber;
	}



	public void setSubAccountNumber(String subAccountNumber) {
		this.subAccountNumber = subAccountNumber;
	}



	public String getFinancialSubObjectCode() {
		return financialSubObjectCode;
	}



	public void setFinancialSubObjectCode(String financialSubObjectCode) {
		this.financialSubObjectCode = financialSubObjectCode;
	}



	public String getProjectCode() {
		return projectCode;
	}



	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}



	public String getOrganizationReferenceId() {
		return organizationReferenceId;
	}



	public void setOrganizationReferenceId(String organizationReferenceId) {
		this.organizationReferenceId = organizationReferenceId;
	}



	public Integer getAccountLineIdentifier() {
		return accountLineIdentifier;
	}



	public void setAccountLineIdentifier(Integer accountLineIdentifier) {
		this.accountLineIdentifier = accountLineIdentifier;
	}



	public Integer getUserProfileId() {
		return userProfileId;
	}



	public void setUserProfileId(Integer userProfileId) {
		this.userProfileId = userProfileId;
	}



	public Boolean getPrimaryInd() {
		return primaryInd;
	}



	public void setPrimaryInd(Boolean primaryInd) {
		this.primaryInd = primaryInd;
	}



	public Chart getChart() {
		if (StringUtils.isNotBlank(chartOfAccountsCode) && ObjectUtils.isNull(chart)) {
			this.refreshReferenceObject("chart");
			if (ObjectUtils.isNull(chart)) {
				chart = null;
			}
		}
		return chart;
	}


	public void setChart(Chart chart) {
		this.chart = chart;
	}



	public Account getAccount() {
		if (StringUtils.isNotBlank(accountNumber) && ObjectUtils.isNull(account)) {
			this.refreshReferenceObject("account");
			if (ObjectUtils.isNull(account)) {
				account = null;
			}
		}
		return account;
	}



	public void setAccount(Account account) {
		this.account = account;
	}



	public ObjectCode getObjectCode() {
		if (objectCode == null && StringUtils.isNotBlank(chartOfAccountsCode) && StringUtils.isNotBlank(financialObjectCode)) {
			objectCode = SpringContext.getBean(ObjectCodeService.class).getByPrimaryId(getCurrentYear(), chartOfAccountsCode, financialObjectCode);
		}
		return objectCode;
	}



	public void setObjectCode(ObjectCode objectCode) {
		this.objectCode = objectCode;
	}



	public SubAccount getSubAccount() {
		if (StringUtils.isNotBlank(subAccountNumber) && subAccount == null) {
			this.refreshReferenceObject("subAccount");
		}
		return subAccount;
	}



	public void setSubAccount(SubAccount subAccount) {
		this.subAccount = subAccount;
	}



	public SubObjectCode getSubObjectCode() {
		if (subObjectCode == null && StringUtils.isNotBlank(chartOfAccountsCode) && StringUtils.isNotBlank(accountNumber) && StringUtils.isNotBlank(financialObjectCode) && StringUtils.isNotBlank(financialSubObjectCode)) {
			subObjectCode = SpringContext.getBean(SubObjectCodeService.class).getByPrimaryIdForCurrentYear(chartOfAccountsCode, accountNumber, financialObjectCode, financialSubObjectCode);
		}
		return subObjectCode;
	}



	public void setSubObjectCode(SubObjectCode subObjectCode) {
		this.subObjectCode = subObjectCode;
	}



	public ProjectCode getProject() {
		if (StringUtils.isNotBlank(projectCode) && project == null) {
			this.refreshReferenceObject("project");
		}
		return project;
	}



	public void setProject(ProjectCode project) {
		this.project = project;
	}
	
	@Override
	protected LinkedHashMap toStringMapper() {
		// TODO Auto-generated method stub
		return null;
	}


	public UserProcurementProfile getUserProcurementProfile() {
		return userProcurementProfile;
	}


	public void setUserProcurementProfile(
			UserProcurementProfile userProcurementProfile) {
		this.userProcurementProfile = userProcurementProfile;
	}

	public Integer getCurrentYear() {
        if (currentYear == null) {
        	currentYear = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
        }
        return currentYear;
	}


	public void setCurrentYear(Integer currentYear) {
		this.currentYear = currentYear;
	}

    public boolean equals(Object obj) {
        if (!(obj instanceof FavoriteAccount)) {
            return false;
        }
        FavoriteAccount accountingLine = (FavoriteAccount) obj;
        return new EqualsBuilder().append(this.chartOfAccountsCode, accountingLine.getChartOfAccountsCode()).append(this.accountNumber, accountingLine.getAccountNumber()).append(this.subAccountNumber, accountingLine.getSubAccountNumber()).append(this.financialObjectCode, accountingLine.getFinancialObjectCode()).append(this.financialSubObjectCode, accountingLine.getFinancialSubObjectCode()).append(this.projectCode, accountingLine.getProjectCode()).append(this.organizationReferenceId, accountingLine.getOrganizationReferenceId()).isEquals();
    }

    /**
     * Override needed for PURAP GL entry creation (hjs) - please do not add "amount" to this method
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(37, 41).append(this.chartOfAccountsCode).append(this.accountNumber).append(this.subAccountNumber).append(this.financialObjectCode).append(this.financialSubObjectCode).append(this.projectCode).append(this.organizationReferenceId).toHashCode();
    }

}
