package edu.cornell.kfs.gl.batch.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryExclusionAccount;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.gl.batch.service.impl.PostExpenditureTransaction;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuPostExpenditureTransaction extends PostExpenditureTransaction {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPostExpenditureTransaction.class);
	
	/**
     * Determines if there's an exclusion by account record for the given account and object code
     * @param account the account to check
     * @param objectCode the object code to check
     * @return true if the given account and object code have an exclusion by account record, false otherwise
     */
	@Override
    public boolean hasExclusionByAccount(Account account, ObjectCode objectCode) {
    	if (LOG.isDebugEnabled()) {
            LOG.debug("CuPostExpenditureTransaction/hasExclusionByAccount() started, looking up accountL " + account.getAccountNumber() + " and objectCode " + objectCode.getCode());
        }
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, account.getChartOfAccountsCode());
        keys.put(KFSPropertyConstants.ACCOUNT_NUMBER, account.getAccountNumber());
        keys.put(KFSPropertyConstants.FINANCIAL_OBJECT_CHART_OF_ACCOUNT_CODE, objectCode.getChartOfAccountsCode());
        keys.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode.getFinancialObjectCode());
        keys.put(KFSPropertyConstants.ACTIVE, "Y");
        final IndirectCostRecoveryExclusionAccount excAccount = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(IndirectCostRecoveryExclusionAccount.class, keys);
        
        if (LOG.isDebugEnabled()) {
        	if(ObjectUtils.isNull(excAccount)) {
        		 LOG.debug("CuPostExpenditureTransaction/hasExclusionByAccount() excAccount is NULL");
        	} else {
        		 LOG.debug("CuPostExpenditureTransaction/hasExclusionByAccount() excAccount:" + excAccount.getAccountNumber());
        	}
        }
        
        return !ObjectUtils.isNull(excAccount);
    }

}
