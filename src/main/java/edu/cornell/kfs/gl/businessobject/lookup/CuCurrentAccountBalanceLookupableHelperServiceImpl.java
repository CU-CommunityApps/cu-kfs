package edu.cornell.kfs.gl.businessobject.lookup;

import java.util.Arrays;
import java.util.Collection;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.CurrentAccountBalance;
import org.kuali.kfs.gl.businessobject.lookup.CurrentAccountBalanceLookupableHelperServiceImpl;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

/**
 * Custom CurrentAccountBalance lookupable helper subclass that adds some enhancements
 * to the lookup. One is an option for excluding "CB" amounts from certain calculations,
 * effectively turning the results into Current Fund Balances instead. Another change
 * is using object type code to perform Current Asset/Liability calculations instead,
 * and there are associated parameters to go along with that. Some miscellaneous
 * clean-up has been done as well.
 */
public class CuCurrentAccountBalanceLookupableHelperServiceImpl extends CurrentAccountBalanceLookupableHelperServiceImpl {

    private static final long serialVersionUID = 2542719296293895780L;

    /**
     * This override copies the superclass's version of the method, and tweaks it to add the CB exclusion option
     * and to use object type code for Current Asset/Liability checking.
     * It also fixes a comparison bug in the Current Budget calculation that was getting the right value from the wrong constant.
     * 
     * @see org.kuali.kfs.gl.businessobject.lookup.CurrentAccountBalanceLookupableHelperServiceImpl#updateCurrentBalance(
     * org.kuali.kfs.gl.businessobject.CurrentAccountBalance, org.kuali.kfs.gl.businessobject.Balance, java.lang.String)
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void updateCurrentBalance(CurrentAccountBalance currentBalance, Balance balance, String fiscalPeriod) {
        Collection<String> cashBudgetRecordLevelCodes = this.getParameterService().getParameterValuesAsString(
                CurrentAccountBalance.class, KFSParameterKeyConstants.GlParameterConstants.CASH_BUDGET_RECORD_LEVEL);
        Collection<String> expenseObjectTypeCodes = this.getParameterService().getParameterValuesAsString(
                CurrentAccountBalance.class, KFSParameterKeyConstants.GlParameterConstants.EXPENSE_OBJECT_TYPE);
        Collection<String> fundBalanceObjCodes = this.getParameterService().getParameterValuesAsString(
                CurrentAccountBalance.class, KFSParameterKeyConstants.GlParameterConstants.FUND_BALANCE_OBJECT_CODE);
        Collection<String> currentAssetObjTypeCodes = this.getParameterService().getParameterValuesAsString(
                CurrentAccountBalance.class, CUKFSParameterKeyConstants.GlParameterConstants.CURRENT_ASSET_OBJECT_TYPE_CODE);
        Collection<String> currentLiabilityObjTypeCodes = this.getParameterService().getParameterValuesAsString(
                CurrentAccountBalance.class, CUKFSParameterKeyConstants.GlParameterConstants.CURRENT_LIABILITY_OBJECT_TYPE_CODE);
        Collection<String> incomeObjTypeCodes = this.getParameterService().getParameterValuesAsString(
                CurrentAccountBalance.class, KFSParameterKeyConstants.GlParameterConstants.INCOME_OBJECT_TYPE);
        Collection<String> encumbranceBalTypes = this.getParameterService().getParameterValuesAsString(
                CurrentAccountBalance.class, KFSParameterKeyConstants.GlParameterConstants.ENCUMBRANCE_BALANCE_TYPE);
        boolean excludeCBPeriod = getParameterService().getParameterValueAsBoolean(
                CurrentAccountBalance.class, CUKFSParameterKeyConstants.GlParameterConstants.EXCLUDE_CB_PERIOD, Boolean.FALSE)
                .booleanValue();
        String balanceTypeCode = balance.getBalanceTypeCode();
        String objectTypeCode = balance.getObjectTypeCode();
        String objectCode = balance.getObjectCode();

        /*
         * TODO: The existing version of this method in KFS is using the new SystemOptions approach in some areas
         * while using the deprecated balance type constants elsewhere. If newer versions of KFS update this method
         * to only use the former, then this override should be updated accordingly. (A similar thing applies
         * to the Current Budget calculations below, which were grabbing the "CB" type from the wrong constant in base code
         * but are using the correct deprecated constant in this override.)
         */
        SystemOptions options = getOptionsService().getCurrentYearOptions();
        Collection<String> assetLiabilityFundBalanceTypeCodes = Arrays.asList(options.getFinancialObjectTypeAssetsCd(),  // AS
            options.getFinObjectTypeLiabilitiesCode(), // LI
            options.getFinObjectTypeFundBalanceCd());  // FB

        Account account = balance.getAccount();
        if (ObjectUtils.isNull(account)) {
            account = getAccountService().getByPrimaryId(balance.getChartOfAccountsCode(), balance.getAccountNumber());
            balance.setAccount(account);
            currentBalance.setAccount(account);
        }

        boolean isCashBudgetRecording = cashBudgetRecordLevelCodes.contains(account.getBudgetRecordingLevelCode());
        currentBalance.setUniversityFiscalPeriodCode(fiscalPeriod);

        // Current Budget (A)
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

        // Beginning Fund Balance (B)
        if (isCashBudgetRecording) {
            if (fundBalanceObjCodes.contains(objectCode)) {
                currentBalance.setBeginningFundBalance(
                        add(currentBalance.getBeginningFundBalance(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
            }
        } else {
            currentBalance.setBeginningFundBalance(KualiDecimal.ZERO);
        }

        // Beginning Current Assets (C)
        if (isCashBudgetRecording) {
            if (currentAssetObjTypeCodes.contains(objectTypeCode)) {
                currentBalance.setBeginningCurrentAssets(
                        add(currentBalance.getBeginningCurrentAssets(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
            }
        } else {
            currentBalance.setBeginningCurrentAssets(KualiDecimal.ZERO);
        }

        // Beginning Current Liabilities (D)
        if (isCashBudgetRecording) {
            if (currentLiabilityObjTypeCodes.contains(objectTypeCode)) {
                currentBalance.setBeginningCurrentLiabilities(
                        add(currentBalance.getBeginningCurrentLiabilities(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
            }
        } else {
            currentBalance.setBeginningCurrentLiabilities(KualiDecimal.ZERO);
        }

        // Total Income (E)
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

        // Total Expense (F)
        if (expenseObjectTypeCodes.contains(objectTypeCode) && KFSConstants.BALANCE_TYPE_ACTUAL.equals(balanceTypeCode)) {
            currentBalance.setTotalExpense(
                    add(currentBalance.getTotalExpense(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
            if (!excludeCBPeriod) {
                currentBalance.setTotalExpense(
                        add(currentBalance.getTotalExpense(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE)));
            }
        }

        // Encumbrances (G)
        if (encumbranceBalTypes.contains(balanceTypeCode)
                && (expenseObjectTypeCodes.contains(objectTypeCode) || incomeObjTypeCodes.contains(objectTypeCode))
                && !assetLiabilityFundBalanceTypeCodes.contains(objectTypeCode)) {
            currentBalance.setEncumbrances(add(currentBalance.getEncumbrances(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
        }

        // Budget Balance Available (H)
        if (isCashBudgetRecording) {
            currentBalance.setBudgetBalanceAvailable(KualiDecimal.ZERO);
        } else {
            currentBalance.setBudgetBalanceAvailable(
                    currentBalance.getCurrentBudget()
                            .subtract(currentBalance.getTotalExpense())
                            .subtract(currentBalance.getEncumbrances()));
        }

        // Cash Expenditure Authority (I)
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

        // Current Fund Balance (J)
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
