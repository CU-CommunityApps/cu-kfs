/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.businessobject;

import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

/* Cornell Customization: backport redis*/
public class SystemOptions extends PersistableBusinessObjectBase implements FiscalYearBasedBusinessObject {

    public static final String CACHE_NAME = "SystemOptions";

    private Integer universityFiscalYear;
    private String actualFinancialBalanceTypeCd;
    private String budgetCheckingBalanceTypeCd;
    private boolean budgetCheckingOptionsCode;
    private Integer universityFiscalYearStartYr;
    private String universityFiscalYearStartMo;
    private String finObjectTypeIncomecashCode;
    private String finObjTypeExpenditureexpCd;
    private String finObjTypeExpendNotExpCode;
    private String finObjTypeExpNotExpendCode;
    private String financialObjectTypeAssetsCd;
    private String finObjectTypeLiabilitiesCode;
    private String finObjectTypeFundBalanceCd;
    private String extrnlEncumFinBalanceTypCd;
    private String intrnlEncumFinBalanceTypCd;
    private String preencumbranceFinBalTypeCd;
    private String eliminationsFinBalanceTypeCd;
    private String finObjTypeIncomeNotCashCd;
    private String finObjTypeCshNotIncomeCd;
    private String universityFiscalYearName;
    private boolean financialBeginBalanceLoadInd;
    private String universityFinChartOfAcctCd;
    private String costShareEncumbranceBalanceTypeCd;
    private String baseBudgetFinancialBalanceTypeCd;
    private String monthlyBudgetFinancialBalanceTypeCd;
    private String financialObjectTypeTransferIncomeCd;
    private String financialObjectTypeTransferExpenseCd;
    private String nominalFinancialBalanceTypeCd;
    private Chart universityFinChartOfAcct;

    private ObjectType objectType;
    private ObjectType finObjTypeExpenditureexp;
    private ObjectType finObjTypeExpendNotExp;
    private ObjectType finObjTypeExpNotExpend;
    private ObjectType financialObjectTypeAssets;
    private ObjectType finObjectTypeLiabilities;
    private ObjectType finObjectTypeFundBalance;
    private ObjectType finObjTypeIncomeNotCash;
    private ObjectType finObjTypeCshNotIncome;
    private ObjectType financialObjectTypeTransferIncome;
    private ObjectType financialObjectTypeTransferExpense;
    private BalanceType actualFinancialBalanceType;
    private BalanceType budgetCheckingBalanceType;
    private BalanceType extrnlEncumFinBalanceTyp;
    private BalanceType intrnlEncumFinBalanceTyp;
    private BalanceType preencumbranceFinBalType;
    private BalanceType eliminationsFinBalanceType;
    private BalanceType costShareEncumbranceBalanceType;
    private BalanceType baseBudgetFinancialBalanceType;
    private BalanceType monthlyBudgetFinancialBalanceType;
    private BalanceType nominalFinancialBalanceType;

    public SystemOptions() {
    }

    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    public String getActualFinancialBalanceTypeCd() {
        return actualFinancialBalanceTypeCd;
    }

    public void setActualFinancialBalanceTypeCd(String actualFinancialBalanceTypeCd) {
        this.actualFinancialBalanceTypeCd = actualFinancialBalanceTypeCd;
    }

    public String getBudgetCheckingBalanceTypeCd() {
        return budgetCheckingBalanceTypeCd;
    }

    public void setBudgetCheckingBalanceTypeCd(String budgetCheckingBalanceTypeCd) {
        this.budgetCheckingBalanceTypeCd = budgetCheckingBalanceTypeCd;
    }

    public boolean isBudgetCheckingOptionsCode() {
        return budgetCheckingOptionsCode;
    }

    public void setBudgetCheckingOptionsCode(boolean budgetCheckingOptionsCode) {
        this.budgetCheckingOptionsCode = budgetCheckingOptionsCode;
    }

    public Integer getUniversityFiscalYearStartYr() {
        return universityFiscalYearStartYr;
    }

    public void setUniversityFiscalYearStartYr(Integer universityFiscalYearStartYr) {
        this.universityFiscalYearStartYr = universityFiscalYearStartYr;
    }

    public String getUniversityFiscalYearStartMo() {
        return universityFiscalYearStartMo;
    }

    public void setUniversityFiscalYearStartMo(String universityFiscalYearStartMo) {
        this.universityFiscalYearStartMo = universityFiscalYearStartMo;
    }

    public String getFinObjectTypeIncomecashCode() {
        return finObjectTypeIncomecashCode;
    }

    public void setFinObjectTypeIncomecashCode(String finObjectTypeIncomecashCode) {
        this.finObjectTypeIncomecashCode = finObjectTypeIncomecashCode;
    }

    public String getFinObjTypeExpenditureexpCd() {
        return finObjTypeExpenditureexpCd;
    }

    public void setFinObjTypeExpenditureexpCd(String finObjTypeExpenditureexpCd) {
        this.finObjTypeExpenditureexpCd = finObjTypeExpenditureexpCd;
    }

    public String getFinObjTypeExpendNotExpCode() {
        return finObjTypeExpendNotExpCode;
    }

    public void setFinObjTypeExpendNotExpCode(String finObjTypeExpendNotExpCode) {
        this.finObjTypeExpendNotExpCode = finObjTypeExpendNotExpCode;
    }

    public String getFinObjTypeExpNotExpendCode() {
        return finObjTypeExpNotExpendCode;
    }

    public void setFinObjTypeExpNotExpendCode(String finObjTypeExpNotExpendCode) {
        this.finObjTypeExpNotExpendCode = finObjTypeExpNotExpendCode;
    }

    public String getFinancialObjectTypeAssetsCd() {
        return financialObjectTypeAssetsCd;
    }

    public void setFinancialObjectTypeAssetsCd(String financialObjectTypeAssetsCd) {
        this.financialObjectTypeAssetsCd = financialObjectTypeAssetsCd;
    }

    public String getFinObjectTypeLiabilitiesCode() {
        return finObjectTypeLiabilitiesCode;
    }

    public void setFinObjectTypeLiabilitiesCode(String finObjectTypeLiabilitiesCode) {
        this.finObjectTypeLiabilitiesCode = finObjectTypeLiabilitiesCode;
    }

    public String getFinObjectTypeFundBalanceCd() {
        return finObjectTypeFundBalanceCd;
    }

    public void setFinObjectTypeFundBalanceCd(String finObjectTypeFundBalanceCd) {
        this.finObjectTypeFundBalanceCd = finObjectTypeFundBalanceCd;
    }

    public String getExtrnlEncumFinBalanceTypCd() {
        return extrnlEncumFinBalanceTypCd;
    }

    public void setExtrnlEncumFinBalanceTypCd(String extrnlEncumFinBalanceTypCd) {
        this.extrnlEncumFinBalanceTypCd = extrnlEncumFinBalanceTypCd;
    }

    public String getIntrnlEncumFinBalanceTypCd() {
        return intrnlEncumFinBalanceTypCd;
    }

    public void setIntrnlEncumFinBalanceTypCd(String intrnlEncumFinBalanceTypCd) {
        this.intrnlEncumFinBalanceTypCd = intrnlEncumFinBalanceTypCd;
    }

    public String getPreencumbranceFinBalTypeCd() {
        return preencumbranceFinBalTypeCd;
    }

    public void setPreencumbranceFinBalTypeCd(String preencumbranceFinBalTypeCd) {
        this.preencumbranceFinBalTypeCd = preencumbranceFinBalTypeCd;
    }

    public String getEliminationsFinBalanceTypeCd() {
        return eliminationsFinBalanceTypeCd;
    }

    public void setEliminationsFinBalanceTypeCd(String eliminationsFinBalanceTypeCd) {
        this.eliminationsFinBalanceTypeCd = eliminationsFinBalanceTypeCd;
    }

    public String getFinObjTypeIncomeNotCashCd() {
        return finObjTypeIncomeNotCashCd;
    }

    public void setFinObjTypeIncomeNotCashCd(String finObjTypeIncomeNotCashCd) {
        this.finObjTypeIncomeNotCashCd = finObjTypeIncomeNotCashCd;
    }

    public String getFinObjTypeCshNotIncomeCd() {
        return finObjTypeCshNotIncomeCd;
    }

    public void setFinObjTypeCshNotIncomeCd(String finObjTypeCshNotIncomeCd) {
        this.finObjTypeCshNotIncomeCd = finObjTypeCshNotIncomeCd;
    }

    public String getUniversityFiscalYearName() {
        return universityFiscalYearName;
    }

    public void setUniversityFiscalYearName(String universityFiscalYearName) {
        this.universityFiscalYearName = universityFiscalYearName;
    }

    public boolean isFinancialBeginBalanceLoadInd() {
        return financialBeginBalanceLoadInd;
    }

    public void setFinancialBeginBalanceLoadInd(boolean financialBeginBalanceLoadInd) {
        this.financialBeginBalanceLoadInd = financialBeginBalanceLoadInd;
    }

    public Chart getUniversityFinChartOfAcct() {
        return universityFinChartOfAcct;
    }

    public void setUniversityFinChartOfAcct(Chart universityFinChartOfAcct) {
        this.universityFinChartOfAcct = universityFinChartOfAcct;
    }

    public String getUniversityFinChartOfAcctCd() {
        return universityFinChartOfAcctCd;
    }

    public void setUniversityFinChartOfAcctCd(String universityFinChartOfAcctCd) {
        this.universityFinChartOfAcctCd = universityFinChartOfAcctCd;
    }

    public BalanceType getActualFinancialBalanceType() {
        return actualFinancialBalanceType;
    }

    public void setActualFinancialBalanceType(BalanceType actualFinancialBalanceType) {
        this.actualFinancialBalanceType = actualFinancialBalanceType;
    }

    public BalanceType getBudgetCheckingBalanceType() {
        return budgetCheckingBalanceType;
    }

    public void setBudgetCheckingBalanceType(BalanceType budgetCheckingBalanceType) {
        this.budgetCheckingBalanceType = budgetCheckingBalanceType;
    }

    public BalanceType getEliminationsFinBalanceType() {
        return eliminationsFinBalanceType;
    }

    public void setEliminationsFinBalanceType(BalanceType eliminationsFinBalanceType) {
        this.eliminationsFinBalanceType = eliminationsFinBalanceType;
    }

    public BalanceType getExtrnlEncumFinBalanceTyp() {
        return extrnlEncumFinBalanceTyp;
    }

    public void setExtrnlEncumFinBalanceTyp(BalanceType extrnlEncumFinBalanceTyp) {
        this.extrnlEncumFinBalanceTyp = extrnlEncumFinBalanceTyp;
    }

    public ObjectType getFinancialObjectTypeAssets() {
        return financialObjectTypeAssets;
    }

    public void setFinancialObjectTypeAssets(ObjectType financialObjectTypeAssets) {
        this.financialObjectTypeAssets = financialObjectTypeAssets;
    }

    public ObjectType getFinObjectTypeFundBalance() {
        return finObjectTypeFundBalance;
    }

    public void setFinObjectTypeFundBalance(ObjectType finObjectTypeFundBalance) {
        this.finObjectTypeFundBalance = finObjectTypeFundBalance;
    }

    public ObjectType getFinObjectTypeLiabilities() {
        return finObjectTypeLiabilities;
    }

    public void setFinObjectTypeLiabilities(ObjectType finObjectTypeLiabilities) {
        this.finObjectTypeLiabilities = finObjectTypeLiabilities;
    }

    public ObjectType getFinObjTypeCshNotIncome() {
        return finObjTypeCshNotIncome;
    }

    public void setFinObjTypeCshNotIncome(ObjectType finObjTypeCshNotIncome) {
        this.finObjTypeCshNotIncome = finObjTypeCshNotIncome;
    }

    public ObjectType getFinObjTypeExpenditureexp() {
        return finObjTypeExpenditureexp;
    }

    public void setFinObjTypeExpenditureexp(ObjectType finObjTypeExpenditureexp) {
        this.finObjTypeExpenditureexp = finObjTypeExpenditureexp;
    }

    public ObjectType getFinObjTypeExpendNotExp() {
        return finObjTypeExpendNotExp;
    }

    public void setFinObjTypeExpendNotExp(ObjectType finObjTypeExpendNotExp) {
        this.finObjTypeExpendNotExp = finObjTypeExpendNotExp;
    }

    public ObjectType getFinObjTypeExpNotExpend() {
        return finObjTypeExpNotExpend;
    }

    public void setFinObjTypeExpNotExpend(ObjectType finObjTypeExpNotExpend) {
        this.finObjTypeExpNotExpend = finObjTypeExpNotExpend;
    }

    public ObjectType getFinObjTypeIncomeNotCash() {
        return finObjTypeIncomeNotCash;
    }

    public void setFinObjTypeIncomeNotCash(ObjectType finObjTypeIncomeNotCash) {
        this.finObjTypeIncomeNotCash = finObjTypeIncomeNotCash;
    }

    public BalanceType getIntrnlEncumFinBalanceTyp() {
        return intrnlEncumFinBalanceTyp;
    }

    public void setIntrnlEncumFinBalanceTyp(BalanceType intrnlEncumFinBalanceTyp) {
        this.intrnlEncumFinBalanceTyp = intrnlEncumFinBalanceTyp;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public BalanceType getPreencumbranceFinBalType() {
        return preencumbranceFinBalType;
    }

    public void setPreencumbranceFinBalType(BalanceType preencumbranceFinBalType) {
        this.preencumbranceFinBalType = preencumbranceFinBalType;
    }

    public String getCostShareEncumbranceBalanceTypeCd() {
        return costShareEncumbranceBalanceTypeCd;
    }

    public void setCostShareEncumbranceBalanceTypeCd(String costShareEncumbranceBalanceTypeCd) {
        this.costShareEncumbranceBalanceTypeCd = costShareEncumbranceBalanceTypeCd;
    }

    public BalanceType getCostShareEncumbranceBalanceType() {
        return costShareEncumbranceBalanceType;
    }

    public void setCostShareEncumbranceBalanceType(BalanceType costShareEncumbranceBalanceType) {
        this.costShareEncumbranceBalanceType = costShareEncumbranceBalanceType;
    }

    public String getBaseBudgetFinancialBalanceTypeCd() {
        return baseBudgetFinancialBalanceTypeCd;
    }

    public void setBaseBudgetFinancialBalanceTypeCd(String baseBudgetFinancialBalanceTypeCd) {
        this.baseBudgetFinancialBalanceTypeCd = baseBudgetFinancialBalanceTypeCd;
    }

    public String getMonthlyBudgetFinancialBalanceTypeCd() {
        return monthlyBudgetFinancialBalanceTypeCd;
    }

    public void setMonthlyBudgetFinancialBalanceTypeCd(String monthlyBudgetFinancialBalanceTypeCode) {
        this.monthlyBudgetFinancialBalanceTypeCd = monthlyBudgetFinancialBalanceTypeCode;
    }

    public String getFinancialObjectTypeTransferIncomeCd() {
        return financialObjectTypeTransferIncomeCd;
    }

    public void setFinancialObjectTypeTransferIncomeCd(String financialObjectTypeTransferIncomeCd) {
        this.financialObjectTypeTransferIncomeCd = financialObjectTypeTransferIncomeCd;
    }

    public String getFinancialObjectTypeTransferExpenseCd() {
        return financialObjectTypeTransferExpenseCd;
    }

    public void setFinancialObjectTypeTransferExpenseCd(String financialObjectTypeTransferExpenseCd) {
        this.financialObjectTypeTransferExpenseCd = financialObjectTypeTransferExpenseCd;
    }

    public ObjectType getFinancialObjectTypeTransferIncome() {
        return financialObjectTypeTransferIncome;
    }

    public void setFinancialObjectTypeTransferIncome(ObjectType financialObjectTypeTransferIncome) {
        this.financialObjectTypeTransferIncome = financialObjectTypeTransferIncome;
    }

    public ObjectType getFinancialObjectTypeTransferExpense() {
        return financialObjectTypeTransferExpense;
    }

    public void setFinancialObjectTypeTransferExpense(ObjectType financialObjectTypeTransferExpense) {
        this.financialObjectTypeTransferExpense = financialObjectTypeTransferExpense;
    }

    public BalanceType getBaseBudgetFinancialBalanceType() {
        return baseBudgetFinancialBalanceType;
    }

    public void setBaseBudgetFinancialBalanceType(BalanceType baseBudgetFinancialBalanceType) {
        this.baseBudgetFinancialBalanceType = baseBudgetFinancialBalanceType;
    }

    public BalanceType getMonthlyBudgetFinancialBalanceType() {
        return monthlyBudgetFinancialBalanceType;
    }

    public void setMonthlyBudgetFinancialBalanceType(BalanceType monthlyBudgetFinancialBalanceType) {
        this.monthlyBudgetFinancialBalanceType = monthlyBudgetFinancialBalanceType;
    }

    public String getNominalFinancialBalanceTypeCd() {
        return nominalFinancialBalanceTypeCd;
    }

    public void setNominalFinancialBalanceTypeCd(String nominalFinancialBalanceTypeCd) {
        this.nominalFinancialBalanceTypeCd = nominalFinancialBalanceTypeCd;
    }

    public BalanceType getNominalFinancialBalanceType() {
        return nominalFinancialBalanceType;
    }

    public void setNominalFinancialBalanceType(BalanceType nominalFinancialBalanceType) {
        this.nominalFinancialBalanceType = nominalFinancialBalanceType;
    }

}
