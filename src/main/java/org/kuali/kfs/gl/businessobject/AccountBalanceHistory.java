/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.gl.businessobject;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.sql.Date;

/**
 * AccountBalance BO for Balancing process. I.e. a shadow representation.
 */
public class AccountBalanceHistory extends AccountBalance {
    
    private static final Logger LOG = LogManager.getLogger();

    public AccountBalanceHistory() {
        super();
        setCurrentBudgetLineBalanceAmount(KualiDecimal.ZERO);
        setAccountLineActualsBalanceAmount(KualiDecimal.ZERO);
        setAccountLineEncumbranceBalanceAmount(KualiDecimal.ZERO);
    }

    /**
     * @param originEntry
     */
    public AccountBalanceHistory(final OriginEntryInformation originEntry) {
        this();
        setChartOfAccountsCode(originEntry.getChartOfAccountsCode());
        setAccountNumber(originEntry.getAccountNumber());
        setObjectCode(originEntry.getFinancialObjectCode());
        setSubObjectCode(originEntry.getFinancialSubObjectCode());
        setUniversityFiscalYear(originEntry.getUniversityFiscalYear());
        setSubAccountNumber(originEntry.getSubAccountNumber());
    }

    /**
     * Updates amount if the object already existed
     *
     * @param originEntryFull representing the update details
     */
    public boolean addAmount(final OriginEntryFull originEntryFull) {
        if (originEntryFull.getFinancialBalanceTypeCode().equals(originEntryFull.getOption().getBudgetCheckingBalanceTypeCd())) {
            setCurrentBudgetLineBalanceAmount(getCurrentBudgetLineBalanceAmount().add(originEntryFull.getTransactionLedgerEntryAmount()));
            LOG.info("addAmount 1");
        } else if (originEntryFull.getFinancialBalanceTypeCode().equals(originEntryFull.getOption().getActualFinancialBalanceTypeCd())) {
            if (originEntryFull.getObjectType().getFinObjectTypeDebitcreditCd().equals(originEntryFull.getTransactionDebitCreditCode())
                || !originEntryFull.getBalanceType().isFinancialOffsetGenerationIndicator()
                && KFSConstants.GL_BUDGET_CODE.equals(originEntryFull.getTransactionDebitCreditCode())) {
                setAccountLineActualsBalanceAmount(getAccountLineActualsBalanceAmount()
                        .add(originEntryFull.getTransactionLedgerEntryAmount()));
                LOG.info("addAmount 2");
            } else {
                setAccountLineActualsBalanceAmount(getAccountLineActualsBalanceAmount()
                        .subtract(originEntryFull.getTransactionLedgerEntryAmount()));
                LOG.info("addAmount 3");
            }
        } else if (originEntryFull.getFinancialBalanceTypeCode().equals(originEntryFull.getOption().getExtrnlEncumFinBalanceTypCd())
                || originEntryFull.getFinancialBalanceTypeCode().equals(originEntryFull.getOption().getIntrnlEncumFinBalanceTypCd())
                || originEntryFull.getFinancialBalanceTypeCode().equals(originEntryFull.getOption().getPreencumbranceFinBalTypeCd())
                || originEntryFull.getFinancialBalanceTypeCode().equals(originEntryFull.getOption().getCostShareEncumbranceBalanceTypeCd())) {
            if (originEntryFull.getObjectType().getFinObjectTypeDebitcreditCd().equals(originEntryFull.getTransactionDebitCreditCode())
                || !originEntryFull.getBalanceType().isFinancialOffsetGenerationIndicator()
                && KFSConstants.GL_BUDGET_CODE.equals(originEntryFull.getTransactionDebitCreditCode())) {
                setAccountLineEncumbranceBalanceAmount(getAccountLineEncumbranceBalanceAmount()
                        .add(originEntryFull.getTransactionLedgerEntryAmount()));
                LOG.info("addAmount 4");
            } else {
                setAccountLineEncumbranceBalanceAmount(getAccountLineEncumbranceBalanceAmount()
                        .subtract(originEntryFull.getTransactionLedgerEntryAmount()));
                LOG.info("addAmount 5");
            }
        } else {
            LOG.info("addAmount 6");
            return false;
        }
        LOG.info("addAmount 7");
        return true;
    }

    /**
     * Compare amounts
     *
     * @param accountBalance
     */
    public boolean compareAmounts(final AccountBalance accountBalance) {
        return ObjectUtils.isNotNull(accountBalance)
                && accountBalance.getCurrentBudgetLineBalanceAmount().equals(getCurrentBudgetLineBalanceAmount())
                && accountBalance.getAccountLineActualsBalanceAmount().equals(
                getAccountLineActualsBalanceAmount())
                && accountBalance.getAccountLineEncumbranceBalanceAmount().equals(
                getAccountLineEncumbranceBalanceAmount());
    }

    /**
     * History does not track this field.
     *
     * @see org.kuali.kfs.gl.businessobject.Balance#getTimestamp()
     */
    @Override
    public Date getTimestamp() {
        throw new UnsupportedOperationException();
    }

    /**
     * History does not track this field.
     *
     * @see org.kuali.kfs.gl.businessobject.Balance#getTimestamp()
     */
    @Override
    public void setTimestamp(final Date timestamp) {
        throw new UnsupportedOperationException();
    }
}
