/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2018 Kuali, Inc.
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
package org.kuali.kfs.module.ar.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.module.ar.ArConstants;

import java.sql.Date;

public class TimeBasedBillingPeriod extends BillingPeriod {
    
    /*
     * CU Customization
     */
    private static final Logger LOG = LogManager.getLogger(TimeBasedBillingPeriod.class);
    
    public TimeBasedBillingPeriod(ArConstants.BillingFrequencyValues billingFrequency, Date awardStartDate,
            Date currentDate, Date lastBilledDate, AccountingPeriodService accountingPeriodService) {
        super(billingFrequency, awardStartDate, currentDate, lastBilledDate, accountingPeriodService);
    }

    @Override
    protected Date determineEndDateByFrequency() {
        final AccountingPeriod accountingPeriod = findPreviousAccountingPeriod(currentDate);
        return accountingPeriod.getUniversityFiscalPeriodEndDate();
    }

    protected Integer calculatePreviousPeriodByFrequency(Integer currentAccountingPeriodCode, int periodsInBillingFrequency) {
        Integer previousAccountingPeriodCode;
        final int subAmt = (currentAccountingPeriodCode % periodsInBillingFrequency) == 0 ? periodsInBillingFrequency : currentAccountingPeriodCode % periodsInBillingFrequency;

        previousAccountingPeriodCode = currentAccountingPeriodCode - subAmt;
        return previousAccountingPeriodCode;
    }

    @Override
    protected boolean canThisBeBilledByBillingFrequency() {
        if (ArConstants.BillingFrequencyValues.ANNUALLY.equals(billingFrequency)
                && accountingPeriodService.getByDate(lastBilledDate).getUniversityFiscalYear() >= accountingPeriodService.getByDate(currentDate).getUniversityFiscalYear()) {
            return false;
        } else if (StringUtils.equals(findPreviousAccountingPeriod(currentDate).getUniversityFiscalPeriodCode(), findPreviousAccountingPeriod(lastBilledDate).getUniversityFiscalPeriodCode()) &&
            accountingPeriodService.getByDate(lastBilledDate).getUniversityFiscalYear().equals(accountingPeriodService.getByDate(currentDate).getUniversityFiscalYear())) {
            return false;
        }

        return true;
    }
    
    /*
     * CU Customization
     */
    @Override
    protected Date determineStartDateByFrequency() {
        if (lastBilledDate == null) {
            LOG.info("determineStartDateByFrequency, no previous billed date, so award start date is the next start date");
            return awardStartDate;
        }
        
        return calculateNextDay(lastBilledDate);
    }

    protected AccountingPeriod findPreviousAccountingPeriod(final Date date) {
        final AccountingPeriod currentAccountingPeriod = findAccountingPeriodBy(date);
        final Integer currentAccountingPeriodCode = Integer.parseInt(currentAccountingPeriod.getUniversityFiscalPeriodCode());
        Integer previousAccountingPeriodCode;
        previousAccountingPeriodCode = findPreviousAccountingPeriodCode(currentAccountingPeriodCode);

        Integer currentFiscalYear = currentAccountingPeriod.getUniversityFiscalYear();
        if (previousAccountingPeriodCode == 0) {
            previousAccountingPeriodCode = 12;
            currentFiscalYear -= 1;
        }

        String periodCode;
        if (previousAccountingPeriodCode < 10) {
            periodCode = "0" + previousAccountingPeriodCode;
        } else {
            periodCode = "" + previousAccountingPeriodCode;
        }

        return accountingPeriodService.getByPeriod(periodCode, currentFiscalYear);
    }

    protected Integer findPreviousAccountingPeriodCode(Integer currentAccountingPeriodCode) {
        Integer previousAccountingPeriodCode;
        if (ArConstants.BillingFrequencyValues.MONTHLY.equals(billingFrequency) ||
            ArConstants.BillingFrequencyValues.MILESTONE.equals(billingFrequency) ||
            ArConstants.BillingFrequencyValues.PREDETERMINED_BILLING.equals(billingFrequency)) {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 1);
        } else if (ArConstants.BillingFrequencyValues.QUARTERLY.equals(billingFrequency)) {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 3);
        } else if (ArConstants.BillingFrequencyValues.SEMI_ANNUALLY.equals(billingFrequency)) {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 6);
        } else {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 12);
        }
        return previousAccountingPeriodCode;
    }
}
