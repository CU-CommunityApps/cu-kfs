package edu.cornell.kfs.module.ld.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.service.impl.LaborBenefitsCalculationServiceImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

public class CuLaborBenefitsCalculationServiceImpl extends LaborBenefitsCalculationServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuLaborBenefitsCalculationServiceImpl.class);

    @Override
    public String getBenefitRateCategoryCode(String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        this.setCostSharingSourceAccountNumber(null);
        this.setCostSharingSourceAccountChartOfAccountsCode(null);
        this.setCostSharingSourceSubAccountNumber(null);
        //make sure the sub accout number is filled in
        if (subAccountNumber != null) {
            LOG.info("Sub Account Number was filled in. Checking to see if it is a Cost Sharing Sub Account.");

            //make sure the system parameter exists
            if (getParameterService().parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "USE_COST_SHARE_SOURCE_ACCOUNT_BENEFIT_RATE_IND")) {
                //parameter exists, determine the value of the parameter
                String sysParam2 = getParameterService().getParameterValueAsString(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "USE_COST_SHARE_SOURCE_ACCOUNT_BENEFIT_RATE_IND");
                LOG.debug("sysParam2: " + sysParam2);

                //if sysParam2 == Y then check to see if it's a cost sharing sub account
                if ("Y".equalsIgnoreCase(sysParam2)) {
                    //lookup the A21SubAccount to get the cost sharing source account
                    Map<String, Object> subFieldValues = new HashMap<String, Object>();
                    subFieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
                    subFieldValues.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, subAccountNumber);
                    subFieldValues.put(KFSPropertyConstants.SUB_ACCOUNT_TYPE_CODE, "CS");
                    subFieldValues.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
                    LOG.info("Looking for a cost sharing sub account for sub account number " + subAccountNumber);

                    //perform the lookup
                    List<A21SubAccount> subAccountList = (List<A21SubAccount>) getBusinessObjectService().findMatching(A21SubAccount.class, subFieldValues);
                    //check to see if the lookup returns an empty list
                    if (subAccountList.size() > 0) {
                        LOG.info("Found A21 Sub Account. Retrieving source account number for cost sharing.");
                        accountNumber = subAccountList.get(0).getCostShareSourceAccountNumber();
                        LOG.debug("Cost Sharing Source Account Number : " + accountNumber);
                        this.setCostSharingSourceAccountNumber(accountNumber);
                        this.setCostSharingSourceAccountChartOfAccountsCode(subAccountList.get(0).getCostShareChartOfAccountCode());
                        this.setCostSharingSourceSubAccountNumber(subAccountList.get(0).getCostShareSourceSubAccountNumber());
                    }
                    else {
                        LOG.info(subAccountNumber + " is not a cost sharing account.  Using the Labor Benefit Rate Category from the account number.");
                    }
                }
                else {
                    LOG.info("Using the Grant Account to determine the labor benefit rate category code.");

                }
            }
        }

        LOG.info("Looking up Account {" + chartOfAccountsCode + "," + accountNumber + "}");
        //lookup the account from the db based off the account code and the account number
        Account account = getAccountService().getByPrimaryId(chartOfAccountsCode, accountNumber);

        String laborBenefitRateCategoryCode = null;
        if (account == null) {
            LOG.info("The Account {" + chartOfAccountsCode + "," + accountNumber + "} could not be found.");
        } else {
            laborBenefitRateCategoryCode = account.getLaborBenefitRateCategoryCode();
        }

        //make sure the laborBenefitRateCategoryCode is not null or blank
        if(StringUtils.isBlank(laborBenefitRateCategoryCode)){
            LOG.info("The Account did not have a Labor Benefit Rate Category Code. Will use the system parameter default.");
            //The system parameter does not exist. Using a blank Labor Benefit Rate Category Code
            laborBenefitRateCategoryCode = StringUtils.defaultString(getParameterService().getParameterValueAsString(Account.class, COAParameterConstants.BENEFIT_RATE));
        }else{
            LOG.debug("Labor Benefit Rate Category Code for Account " + accountNumber + " is " + laborBenefitRateCategoryCode);
        }

        return laborBenefitRateCategoryCode;
    }

}
