package edu.cornell.kfs.module.ar.service.impl;

import java.sql.Date;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsBillingUtilityServiceImpl;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.ar.CuArParameterKeyConstants;
import edu.cornell.kfs.module.ar.service.CuContractsGrantsBillingUtilityService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuContractsGrantsBillingUtilityServiceImpl extends ContractsGrantsBillingUtilityServiceImpl implements CuContractsGrantsBillingUtilityService {
    
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsBillingUtilityServiceImpl.class);
    
    protected AccountService accountService;
    protected ParameterService parameterService;
    
    @Override
    public Date determineLastBilledDateByInvoicingOption(List<ContractsAndGrantsBillingAwardAccount> awardAccounts, String invoicingOptionCode, Date awardLastBilledDate) {
        Date computedLastBilledDate = null;
        
        if (StringUtils.equalsIgnoreCase(ArConstants.INV_ACCOUNT, invoicingOptionCode)
                || StringUtils.equalsIgnoreCase(ArConstants.INV_CONTRACT_CONTROL_ACCOUNT, invoicingOptionCode)) {
            ContractsAndGrantsBillingAwardAccount accountToUse = null;
            for (ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                if (ObjectUtils.isNotNull(accountToUse)) {
                    if (isNotExpenditureAccount(awardAccount) 
                            && awardAccount.getCurrentLastBilledDate().after(accountToUse.getCurrentLastBilledDate())) {
                        accountToUse = awardAccount;
                    }
                } else if (isNotExpenditureAccount(awardAccount)) {
                    accountToUse = awardAccount;
                }
            }
            if (ObjectUtils.isNotNull(accountToUse)) {
                computedLastBilledDate = accountToUse.getCurrentLastBilledDate();
            } else {
                LOG.error("determineLastBilledDateByInvoicingOption: NO award accounts passed to method when award had invoice option of Account or Contract Control Account. lastBilledDate being returned as NULL.");
            }
        } else if (StringUtils.equalsIgnoreCase(ArConstants.INV_AWARD, invoicingOptionCode)
                || StringUtils.equalsIgnoreCase(ArConstants.INV_SCHEDULE, invoicingOptionCode)) {
            computedLastBilledDate = awardLastBilledDate;
        }
        return computedLastBilledDate;
    }
    
    @Override
    public boolean isNotExpenditureAccount(ContractsAndGrantsBillingAwardAccount billingAwardAccount) {
        Account accountLinkedToAward = getAccountService().getByPrimaryId(billingAwardAccount.getChartOfAccountsCode(), billingAwardAccount.getAccountNumber());
        if (ObjectUtils.isNotNull(accountLinkedToAward)) {
            return !isExpenditureSubFund(accountLinkedToAward.getSubFundGroupCode());
        }
        return true;
    }
    
    protected boolean isExpenditureSubFund(String subFundGroupCode) {
        if (StringUtils.isNotBlank(subFundGroupCode)) {
            Collection<String> acceptedValuesForExpenditureSubFundCodes = getParameterService().getParameterValuesAsString(KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE,
                    CUKFSParameterKeyConstants.ALL_COMPONENTS, CuArParameterKeyConstants.CG_INVOICING_EXCLUDE_EXPENSES_SUB_FUNDS);
            if (CollectionUtils.isNotEmpty(acceptedValuesForExpenditureSubFundCodes)) {
                return acceptedValuesForExpenditureSubFundCodes.stream().anyMatch(subFundGroupCode::equalsIgnoreCase);
            }
        }
        return false;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
