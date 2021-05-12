package edu.cornell.kfs.module.purap.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class IWantAccount extends PersistableBusinessObjectBase implements GeneralLedgerPendingEntrySourceDetail {

	private static final long serialVersionUID = 1L;
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

    public IWantAccount() {
        super();
        this.useAmountOrPercent = CUPurapConstants.PERCENT;
        this.amountOrPercent = new KualiDecimal(100);

    }

    @SuppressWarnings("rawtypes")
	protected LinkedHashMap toStringMapper() {
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

    /**
     * Returns the amount-or-percent value if configured to use amounts, zero otherwise.
     *
     * @see org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail#getAmount()
     */
    @Override
    public KualiDecimal getAmount() {
        return CUPurapConstants.AMOUNT.equals(getUseAmountOrPercent()) ? getAmountOrPercent() : KualiDecimal.ZERO;
    }

    // Only here to comply with the GeneralLedgerPendingEntrySourceDetail interface.
    @Override
    public String getBalanceTypeCode() {
        return null;
    }

    // Only here to comply with the GeneralLedgerPendingEntrySourceDetail interface.
    @Override
    public String getFinancialDocumentLineDescription() {
        return null;
    }

    // Only here to comply with the GeneralLedgerPendingEntrySourceDetail interface.
    @Override
    public String getReferenceNumber() {
        return null;
    }

    // Only here to comply with the GeneralLedgerPendingEntrySourceDetail interface.
    @Override
    public String getReferenceOriginCode() {
        return null;
    }

    // Only here to comply with the GeneralLedgerPendingEntrySourceDetail interface.
    @Override
    public String getReferenceTypeCode() {
        return null;
    }

	/**
     * Helper method for copying an account.
     * 
     * @param oldAccount
     * @return A copy of the account.
     */
    public static IWantAccount createCopy(IWantAccount oldAccount) {
    	if (oldAccount == null) {
    		throw new IllegalArgumentException("source account cannot be null");
    	}
    	IWantAccount copyAccount = new IWantAccount();

    	// NOTE: We do not copy the accountLineIdentifier (the primary key).
    	copyAccount.documentNumber = oldAccount.documentNumber;
    	copyAccount.lineNumber = oldAccount.lineNumber;
    	copyAccount.chartOfAccountsCode = oldAccount.chartOfAccountsCode;
    	copyAccount.accountNumber = oldAccount.accountNumber;
    	copyAccount.financialObjectCode = oldAccount.financialObjectCode;
    	copyAccount.subAccountNumber = oldAccount.subAccountNumber;
    	copyAccount.financialSubObjectCode = oldAccount.financialSubObjectCode;
    	copyAccount.projectCode = oldAccount.projectCode;
    	copyAccount.organizationReferenceId = oldAccount.organizationReferenceId;
    	copyAccount.postingYear = oldAccount.postingYear;
    	copyAccount.amountOrPercent = oldAccount.amountOrPercent;
    	copyAccount.useAmountOrPercent = oldAccount.useAmountOrPercent;
    	copyAccount.chart = oldAccount.chart;
    	copyAccount.account = oldAccount.account;
    	copyAccount.objectCode = oldAccount.objectCode;
    	copyAccount.subAccount = oldAccount.subAccount;
    	copyAccount.subObjectCode = oldAccount.subObjectCode;
    	copyAccount.project = oldAccount.project;
    	
    	return copyAccount;
    }

}
