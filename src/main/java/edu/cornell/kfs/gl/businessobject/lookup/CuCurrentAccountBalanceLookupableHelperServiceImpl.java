package edu.cornell.kfs.gl.businessobject.lookup;

import java.util.Arrays;
import java.util.Collection;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.CurrentAccountBalance;
import org.kuali.kfs.gl.businessobject.lookup.CurrentAccountBalanceLookupableHelperServiceImpl;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

/**
 * Custom CurrentAccountBalance lookupable helper subclass that adds some enhancements
 * to the lookup. One is an option for excluding "CB" amounts from certain calculations,
 * effectively turning the results into Current Fund Balances instead. Another change
 * is using object type code to perform Current Asset/Liability calculations instead,
 * and there are associated parameters to go along with that. Some miscellaneous
 * clean-up and refactoring has been done as well.
 */
public class CuCurrentAccountBalanceLookupableHelperServiceImpl extends CurrentAccountBalanceLookupableHelperServiceImpl {

    private static final long serialVersionUID = 2542719296293895780L;

    /**
     * This override constructs and uses a custom helper class to execute the superclass's processing.
     * 
     * @see org.kuali.kfs.gl.businessobject.lookup.CurrentAccountBalanceLookupableHelperServiceImpl#updateCurrentBalance(
     * org.kuali.kfs.gl.businessobject.CurrentAccountBalance, org.kuali.kfs.gl.businessobject.Balance, java.lang.String)
     */
    @Override
    protected void updateCurrentBalance(final CurrentAccountBalance currentBalance, final Balance balance, final String fiscalPeriod) {
        new BalanceUpdaterHelper(currentBalance, balance, fiscalPeriod)
                .updateCurrentBalance();
    }

    @SuppressWarnings("deprecation")
    protected class BalanceUpdaterHelper {

        protected final CurrentAccountBalance currentBalance;
        protected final Balance balance;
        protected final String fiscalPeriod;
        protected final Collection<String> cashBudgetRecordLevelCodes;
        protected final Collection<String> expenseObjectTypeCodes;
        protected final Collection<String> fundBalanceObjCodes;
        protected final Collection<String> currentAssetObjTypeCodes;
        protected final Collection<String> currentLiabilityObjTypeCodes;
        protected final Collection<String> incomeObjTypeCodes;
        protected final Collection<String> encumbranceBalTypes;
        protected final Collection<String> assetLiabilityFundBalanceTypeCodes;
        protected final boolean excludeCBPeriod;
        protected final String balanceTypeCode;
        protected final String objectTypeCode;
        protected final String objectCode;
        protected final Account replacementAccount;
        protected final boolean isCashBudgetRecording;
        
        public BalanceUpdaterHelper(final CurrentAccountBalance currentBalance, final Balance balance, final String fiscalPeriod) {
            this.currentBalance = currentBalance;
            this.balance = balance;
            this.fiscalPeriod = fiscalPeriod;
            
            this.cashBudgetRecordLevelCodes = getParameterValuesAsString(CASH_BUDGET_RECORD_LEVEL);
            this.expenseObjectTypeCodes = getParameterValuesAsString(EXPENSE_OBJECT_TYPE);
            this.fundBalanceObjCodes = getParameterValuesAsString(FUND_BALANCE_OBJECT_CODE);
            this.currentAssetObjTypeCodes = getParameterValuesAsString(CUKFSParameterKeyConstants.GlParameterConstants.CURRENT_ASSET_OBJECT_TYPE_CODE);
            this.currentLiabilityObjTypeCodes = getParameterValuesAsString(CUKFSParameterKeyConstants.GlParameterConstants.CURRENT_LIABILITY_OBJECT_TYPE_CODE);
            this.incomeObjTypeCodes = getParameterValuesAsString(INCOME_OBJECT_TYPE);
            this.encumbranceBalTypes = getParameterValuesAsString(ENCUMBRANCE_BALANCE_TYPE);
            this.excludeCBPeriod = getParameterValueAsBoolean(CUKFSParameterKeyConstants.GlParameterConstants.EXCLUDE_CB_PERIOD, Boolean.FALSE)
                    .booleanValue();
            
            SystemOptions options = getOptionsService().getCurrentYearOptions();
            this.assetLiabilityFundBalanceTypeCodes = Arrays.asList(options.getFinancialObjectTypeAssetsCd(),
                options.getFinObjectTypeLiabilitiesCode(),
                options.getFinObjectTypeFundBalanceCd());
            
            this.balanceTypeCode = balance.getBalanceTypeCode();
            this.objectTypeCode = balance.getObjectTypeCode();
            this.objectCode = balance.getObjectCode();
            
            Account account = balance.getAccount();
            if (ObjectUtils.isNull(account)) {
                account = getAccountService().getByPrimaryId(balance.getChartOfAccountsCode(), balance.getAccountNumber());
                this.replacementAccount = account;
            } else {
                this.replacementAccount = null;
            }

            this.isCashBudgetRecording = cashBudgetRecordLevelCodes.contains(account.getBudgetRecordingLevelCode());
        }
        
        protected Collection<String> getParameterValuesAsString(final String parameterName) {
            return getParameterService().getParameterValuesAsString(CurrentAccountBalance.class, parameterName);
        }
        
        protected Boolean getParameterValueAsBoolean(final String parameterName, final Boolean defaultValue) {
            return getParameterService().getParameterValueAsBoolean(CurrentAccountBalance.class, parameterName, defaultValue);
        }
        
        /**
         * Executes the balance-updating logic from the lookupable superclass's updateCurrentBalance() method.
         * Updates the CurrentAccountBalance object that was passed to the constructor, and also updates
         * the account reference on the constructor-given Balance object if it was originally null.
         */
        public void updateCurrentBalance() {
            if (ObjectUtils.isNotNull(replacementAccount)) {
                balance.setAccount(replacementAccount);
                currentBalance.setAccount(replacementAccount);
            }
            currentBalance.setUniversityFiscalPeriodCode(fiscalPeriod);
            
            updateCurrentBudget();
            updateBeginningFundBalance();
            updateBeginningCurrentAssets();
            updateBeginningCurrentLiabilities();
            updateTotalIncome();
            updateTotalExpense();
            updateEncumbrances();
            updateBudgetBalanceAvailable();
            updateCashExpenditureAuthority();
            updateCurrentFundBalance();
        }
        
        /*
         * NOTE: Base KFS grabs the "CB" balance type from the wrong constant. This has been corrected below.
         * The KFSPTS-7867 ticket will handle contributing this fix (and other related fixes) back to KualiCo.
         */
        protected void updateCurrentBudget() {
            if (isCashBudgetRecording) {
                currentBalance.setCurrentBudget(KualiDecimal.ZERO);
            } else {
                if (KFSConstants.BALANCE_TYPE_CURRENT_BUDGET.equals(balanceTypeCode) && expenseObjectTypeCodes.contains(objectTypeCode)) {
                    currentBalance.setCurrentBudget(
                            add(currentBalance.getCurrentBudget(),
                                    add(accumulateMonthlyAmounts(balance, fiscalPeriod),
                                            accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE))));
                }
            }
        }
        
        protected void updateBeginningFundBalance() {
            if (isCashBudgetRecording) {
                if (fundBalanceObjCodes.contains(objectCode)) {
                    currentBalance.setBeginningFundBalance(
                            add(currentBalance.getBeginningFundBalance(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
                }
            } else {
                currentBalance.setBeginningFundBalance(KualiDecimal.ZERO);
            }
        }
        
        /*
         * NOTE: Base KFS does Current Asset checks based on object code,
         * but this version checks object type code instead.
         */
        protected void updateBeginningCurrentAssets() {
            if (isCashBudgetRecording) {
                if (currentAssetObjTypeCodes.contains(objectTypeCode)) {
                    currentBalance.setBeginningCurrentAssets(
                            add(currentBalance.getBeginningCurrentAssets(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
                }
            } else {
                currentBalance.setBeginningCurrentAssets(KualiDecimal.ZERO);
            }
        }
        
        /*
         * NOTE: Base KFS does Current Liability checks based on object code,
         * but this version checks object type code instead.
         */
        protected void updateBeginningCurrentLiabilities() {
            if (isCashBudgetRecording) {
                if (currentLiabilityObjTypeCodes.contains(objectTypeCode)) {
                    currentBalance.setBeginningCurrentLiabilities(
                            add(currentBalance.getBeginningCurrentLiabilities(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
                }
            } else {
                currentBalance.setBeginningCurrentLiabilities(KualiDecimal.ZERO);
            }
        }
        
        /*
         * NOTE: This version of the code adds an option for excluding "CB" period amounts.
         */
        protected void updateTotalIncome() {
            if (isCashBudgetRecording) {
                if (incomeObjTypeCodes.contains(objectTypeCode) && KFSConstants.BALANCE_TYPE_ACTUAL.equals(balanceTypeCode)) {
                    currentBalance.setTotalIncome(
                            add(currentBalance.getTotalIncome(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
                    if (!excludeCBPeriod) {
                        currentBalance.setTotalIncome(
                                add(currentBalance.getTotalIncome(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE)));
                    }
                }
            } else {
                currentBalance.setTotalIncome(KualiDecimal.ZERO);
            }
        }
        
        /*
         * NOTE: This version of the code adds an option for excluding "CB" period amounts.
         */
        protected void updateTotalExpense() {
            if (expenseObjectTypeCodes.contains(objectTypeCode) && KFSConstants.BALANCE_TYPE_ACTUAL.equals(balanceTypeCode)) {
                currentBalance.setTotalExpense(
                        add(currentBalance.getTotalExpense(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
                if (!excludeCBPeriod) {
                    currentBalance.setTotalExpense(
                            add(currentBalance.getTotalExpense(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE)));
                }
            }
        }
        
        protected void updateEncumbrances() {
            if (encumbranceBalTypes.contains(balanceTypeCode)
                    && (expenseObjectTypeCodes.contains(objectTypeCode) || incomeObjTypeCodes.contains(objectTypeCode))
                    && !assetLiabilityFundBalanceTypeCodes.contains(objectTypeCode)) {
                currentBalance.setEncumbrances(
                        add(currentBalance.getEncumbrances(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
            }
        }
        
        protected void updateBudgetBalanceAvailable() {
            if (isCashBudgetRecording) {
                currentBalance.setBudgetBalanceAvailable(KualiDecimal.ZERO);
            } else {
                currentBalance.setBudgetBalanceAvailable(
                        currentBalance.getCurrentBudget()
                                .subtract(currentBalance.getTotalExpense())
                                .subtract(currentBalance.getEncumbrances()));
            }
        }
        
        protected void updateCashExpenditureAuthority() {
            if (isCashBudgetRecording) {
                currentBalance.setCashExpenditureAuthority(
                        currentBalance.getBeginningCurrentAssets()
                                .subtract(currentBalance.getBeginningCurrentLiabilities())
                                .add(currentBalance.getTotalIncome())
                                .subtract(currentBalance.getTotalExpense())
                                .subtract(currentBalance.getEncumbrances()));
            } else {
                currentBalance.setCashExpenditureAuthority(KualiDecimal.ZERO);
            }
        }
        
        protected void updateCurrentFundBalance() {
            if (isCashBudgetRecording) {
                currentBalance.setCurrentFundBalance(
                        currentBalance.getBeginningFundBalance()
                                .add(currentBalance.getTotalIncome())
                                .subtract(currentBalance.getTotalExpense()));
            } else {
                currentBalance.setCurrentFundBalance(KualiDecimal.ZERO);
            }
        }
        
    }

}
