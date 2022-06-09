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

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

/**
 * Bank Business Object
 */
/* Cornell Customization: backport redis*/
public class Bank extends PersistableBusinessObjectBase implements MutableInactivatable {

    public static final String CACHE_NAME = "Bank";

    protected String bankCode;
    protected String bankName;
    protected String bankShortName;
    protected String bankRoutingNumber;
    protected String bankAccountNumber;
    protected String bankAccountDescription;
    protected String bankIdentificationCode;
    protected String achInstitutionId;
    protected String achInstitutionSchemeName;
    protected String cashOffsetFinancialChartOfAccountCode;
    protected String cashOffsetAccountNumber;
    protected String cashOffsetSubAccountNumber;
    protected String cashOffsetObjectCode;
    protected String cashOffsetSubObjectCode;
    protected String continuationBankCode;
    protected boolean bankDepositIndicator;
    protected boolean bankDisbursementIndicator;
    protected boolean bankAchIndicator;
    protected boolean bankCheckIndicator;
    protected boolean active;

    protected Chart cashOffsetFinancialChartOfAccount;
    protected Account cashOffsetAccount;
    protected ObjectCode cashOffsetObject;
    protected SubAccount cashOffsetSubAccount;
    protected SubObjectCode cashOffsetSubObject;
    protected Bank continuationBank;

    public Bank() {
        super();
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankShortName() {
        return bankShortName;
    }

    public void setBankShortName(String bankShortName) {
        this.bankShortName = bankShortName;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountDescription() {
        return bankAccountDescription;
    }

    public void setBankAccountDescription(String bankAccountDescription) {
        this.bankAccountDescription = bankAccountDescription;
    }
    
    public String getBankIdentificationCode() {
        return bankIdentificationCode;
    }

    public void setBankIdentificationCode(final String bankIdentificationCode) {
        this.bankIdentificationCode = bankIdentificationCode;
    }

    public String getAchInstitutionId() {
        return achInstitutionId;
    }

    public void setAchInstitutionId(final String achInstitutionId) {
        this.achInstitutionId = achInstitutionId;
    }

    public String getAchInstitutionSchemeName() {
        return achInstitutionSchemeName;
    }

    public void setAchInstitutionSchemeName(final String achInstitutionSchemeName) {
        this.achInstitutionSchemeName = achInstitutionSchemeName;
    }

    public String getCashOffsetFinancialChartOfAccountCode() {
        return cashOffsetFinancialChartOfAccountCode;
    }

    public void setCashOffsetFinancialChartOfAccountCode(String cashOffsetFinancialChartOfAccountCode) {
        this.cashOffsetFinancialChartOfAccountCode = cashOffsetFinancialChartOfAccountCode;
    }

    public String getCashOffsetAccountNumber() {
        return cashOffsetAccountNumber;
    }

    public void setCashOffsetAccountNumber(String cashOffsetAccountNumber) {
        this.cashOffsetAccountNumber = cashOffsetAccountNumber;
    }

    public String getCashOffsetSubAccountNumber() {
        return cashOffsetSubAccountNumber;
    }

    public void setCashOffsetSubAccountNumber(String cashOffsetSubAccountNumber) {
        this.cashOffsetSubAccountNumber = cashOffsetSubAccountNumber;
    }

    public String getCashOffsetObjectCode() {
        return cashOffsetObjectCode;
    }

    public void setCashOffsetObjectCode(String cashOffsetObjectCode) {
        this.cashOffsetObjectCode = cashOffsetObjectCode;
    }

    public String getCashOffsetSubObjectCode() {
        return cashOffsetSubObjectCode;
    }

    public void setCashOffsetSubObjectCode(String cashOffsetSubObjectCode) {
        this.cashOffsetSubObjectCode = cashOffsetSubObjectCode;
    }

    public boolean isBankDepositIndicator() {
        return bankDepositIndicator;
    }

    public void setBankDepositIndicator(boolean bankDepositIndicator) {
        this.bankDepositIndicator = bankDepositIndicator;
    }

    public boolean isBankDisbursementIndicator() {
        return bankDisbursementIndicator;
    }

    public void setBankDisbursementIndicator(boolean bankDisbursementIndicator) {
        this.bankDisbursementIndicator = bankDisbursementIndicator;
    }

    public boolean isBankAchIndicator() {
        return bankAchIndicator;
    }

    public void setBankAchIndicator(boolean bankAchIndicator) {
        this.bankAchIndicator = bankAchIndicator;
    }

    public boolean isBankCheckIndicator() {
        return bankCheckIndicator;
    }

    public void setBankCheckIndicator(boolean bankCheckIndicator) {
        this.bankCheckIndicator = bankCheckIndicator;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public Chart getCashOffsetFinancialChartOfAccount() {
        return cashOffsetFinancialChartOfAccount;
    }

    public void setCashOffsetFinancialChartOfAccount(Chart cashOffsetFinancialChartOfAccount) {
        this.cashOffsetFinancialChartOfAccount = cashOffsetFinancialChartOfAccount;
    }

    public Account getCashOffsetAccount() {
        return cashOffsetAccount;
    }

    public void setCashOffsetAccount(Account cashOffsetAccount) {
        this.cashOffsetAccount = cashOffsetAccount;
    }

    public ObjectCode getCashOffsetObject() {
        return cashOffsetObject;
    }

    public void setCashOffsetObject(ObjectCode cashOffsetObject) {
        this.cashOffsetObject = cashOffsetObject;
    }

    public SubAccount getCashOffsetSubAccount() {
        return cashOffsetSubAccount;
    }

    public void setCashOffsetSubAccount(SubAccount cashOffsetSubAccount) {
        this.cashOffsetSubAccount = cashOffsetSubAccount;
    }

    public SubObjectCode getCashOffsetSubObject() {
        return cashOffsetSubObject;
    }

    public void setCashOffsetSubObject(SubObjectCode cashOffsetSubObject) {
        this.cashOffsetSubObject = cashOffsetSubObject;
    }

    public String getContinuationBankCode() {
        return continuationBankCode;
    }

    public void setContinuationBankCode(String continuationBankCode) {
        this.continuationBankCode = continuationBankCode;
    }

    public Bank getContinuationBank() {
        return continuationBank;
    }

    public void setContinuationBank(Bank continuationBank) {
        this.continuationBank = continuationBank;
    }

}
