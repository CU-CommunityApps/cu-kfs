package edu.cornell.kfs.coa.businessobject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl;

import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;

public class SubAccountGlobalDetail extends GlobalBusinessObjectDetailBase {
	
	private static final Logger LOG = LogManager.getLogger(SubAccountGlobalDetail.class);
	
    private String chartOfAccountsCode;
    private String accountNumber;
    private String subAccountNumber;
    
    // field used for routing; not persisted
    private Integer contractsAndGrantsAccountResponsibilityIdForRouting;

    private Account account;
    private Chart chartOfAccounts;
    private SubAccount subAccount;
    
    public SubAccountGlobalDetail() {
    	super();
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

	public String getSubAccountNumber() {
		return subAccountNumber;
	}

	public void setSubAccountNumber(String subAccountNumber) {
		this.subAccountNumber = subAccountNumber;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
	
    /**
     * Returns a map of the keys<propName,value> based on the primary key names of the underlying BO and reflecting into this
     * object.
     */
    public Map<String, Object> getPrimaryKeys() {
        try {
            List<String> keys = SpringContext.getBean(PersistenceStructureService.class).getPrimaryKeys(Account.class);
            HashMap<String, Object> pks = new HashMap<String, Object>(keys.size());
            for (String key : keys) {
                // attempt to read the property of the current object
                // this requires that the field names match between the underlying BO object
                // and this object
                pks.put(key, ObjectUtils.getPropertyValue(this, key));
            }
            return pks;
        }
        catch (Exception ex) {
            LOG.error("unable to get primary keys for global detail object", ex);
        }
        return new HashMap<String, Object>(0);
    }

	public SubAccount getSubAccount() {
		return subAccount;
	}

	public void setSubAccount(SubAccount subAccount) {
		this.subAccount = subAccount;
	}

	public Chart getChartOfAccounts() {
		return chartOfAccounts;
	}

	public void setChartOfAccounts(Chart chartOfAccounts) {
		this.chartOfAccounts = chartOfAccounts;
	}

	/**
	 * Determines the contractsAndGrantsAccountResponsibilityId For Routing. If SubAccountTypeCode is CS then the ContractsAndGrantsAccountResponsibilityId on the sub account account is returned.
	 * Otherwise null is returned. This ensures that document is only routed to CG if sub account type is CS.
	 * 
	 * @return ContractsAndGrantsAccountResponsibilityId on account if sub account type is CS, null otherwise
	 */
	public Integer getContractsAndGrantsAccountResponsibilityIdForRouting() {
		
		this.refreshReferenceObject("subAccount");
    	SubAccount subAccount = this.getSubAccount();
    	
    	if(subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE)){
    		subAccount.refreshReferenceObject("account");
    		contractsAndGrantsAccountResponsibilityIdForRouting = subAccount.getAccount().getContractsAndGrantsAccountResponsibilityId();
    	}
    	
		return contractsAndGrantsAccountResponsibilityIdForRouting;
	}

	public void setContractsAndGrantsAccountResponsibilityIdForRouting(
			Integer contractsAndGrantsAccountResponsibilityIdForRouting) {
		this.contractsAndGrantsAccountResponsibilityIdForRouting = contractsAndGrantsAccountResponsibilityIdForRouting;
	}

}
