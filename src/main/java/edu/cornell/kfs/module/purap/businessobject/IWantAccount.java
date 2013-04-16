package edu.cornell.kfs.module.purap.businessobject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.kim.config.KIMConfigurer;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;

public class IWantAccount extends PersistableBusinessObjectBase {
    private String documentNumber;
    private Integer lineNumber;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    private String subAccountNumber;
    private String financialSubObjectCode;
    private String projectCode;
    private String organizationReferenceId;
    private Integer accountLineIdentifier;
    private Integer postingYear;
    private KualiDecimal amountOrPercent;
    private String useAmountOrPercent;

    // bo references
    private Chart chart;
    private Account account;
    private ObjectCode objectCode;
    private SubAccount subAccount;
    private SubObjectCode subObjectCode;
    private ProjectCode project;

    // KFSPTS-985
    private Integer favoriteAccountLineIdentifier;
    private boolean favoriteAccountExist;

    public IWantAccount() {
        super();
        this.useAmountOrPercent = CUPurapConstants.PERCENT;
        this.amountOrPercent = new KualiDecimal(100);
        this.favoriteAccountExist = isFavoriteAccountSet();

    }

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
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

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ObjectCode getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(ObjectCode objectCode) {
        this.objectCode = objectCode;
    }

    public SubAccount getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(SubAccount subAccount) {
        this.subAccount = subAccount;
    }

    public SubObjectCode getSubObjectCode() {
        return subObjectCode;
    }

    public void setSubObjectCode(SubObjectCode subObjectCode) {
        this.subObjectCode = subObjectCode;
    }

    public ProjectCode getProject() {
        return project;
    }

    public void setProject(ProjectCode project) {
        this.project = project;
    }

    public Integer getAccountLineIdentifier() {
        return accountLineIdentifier;
    }

    public void setAccountLineIdentifier(Integer accountLineIdentifier) {
        this.accountLineIdentifier = accountLineIdentifier;
    }

    public Integer getPostingYear() {
        if (postingYear == null) {
            postingYear = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
        }
        return postingYear;
    }

    public void setPostingYear(Integer postingYear) {
        this.postingYear = postingYear;
    }

    public KualiDecimal getAmountOrPercent() {
        return amountOrPercent;
    }

    public void setAmountOrPercent(KualiDecimal amountOrPercent) {
        this.amountOrPercent = amountOrPercent;
    }

    public String getUseAmountOrPercent() {
        return useAmountOrPercent;
    }

    public void setUseAmountOrPercent(String useAmountOrPercent) {
        this.useAmountOrPercent = useAmountOrPercent;
    }

    // KFSPTS-985
	public Integer getFavoriteAccountLineIdentifier() {
		return favoriteAccountLineIdentifier;
	}

	public void setFavoriteAccountLineIdentifier(Integer favoriteAccountLineIdentifier) {
		this.favoriteAccountLineIdentifier = favoriteAccountLineIdentifier;
	}

	public boolean isFavoriteAccountExist() {
		return favoriteAccountExist;
	}

	public void setFavoriteAccountExist(boolean favoriteAccountExist) {
		this.favoriteAccountExist = favoriteAccountExist;
	}

	private boolean isFavoriteAccountSet() {
    	Map<String, Object> fieldValues = new HashMap<String, Object>();
    	fieldValues.put(KimConstants.PrimaryKeyConstants.PRINCIPAL_ID, GlobalVariables.getUserSession().getPrincipalId());
    	List<UserProcurementProfile> userProfiles = (List<UserProcurementProfile>)SpringContext.getBean(BusinessObjectService.class).findMatching(UserProcurementProfile.class, fieldValues);

    	if (CollectionUtils.isNotEmpty(userProfiles)) {
    		UserProcurementProfile userProfile = userProfiles.get(0);
    		return CollectionUtils.isNotEmpty(userProfile.getFavoriteAccounts());
    	}
    	return false;

	}
	
}
