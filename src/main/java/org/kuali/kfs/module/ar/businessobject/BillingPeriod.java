/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.module.ar.ArConstants;

import java.sql.Date;

public abstract class BillingPeriod {

    private static final Logger LOG = LogManager.getLogger(BillingPeriod.class);
    
    protected Date startDate;
    protected Date endDate;
    protected boolean billable;
    protected final AccountingPeriodService accountingPeriodService;
    protected final ArConstants.BillingFrequencyValues billingFrequency;
    protected final Date awardStartDate;
    protected final Date currentDate;
    protected final Date lastBilledDate;

    protected BillingPeriod(ArConstants.BillingFrequencyValues billingFrequency, Date awardStartDate, Date currentDate, Date lastBilledDate, AccountingPeriodService accountingPeriodService) {
        this.awardStartDate = awardStartDate;
        this.lastBilledDate = lastBilledDate;
        this.accountingPeriodService = accountingPeriodService;
        this.billingFrequency = billingFrequency;
        this.currentDate = currentDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public static BillingPeriod determineBillingPeriodPriorTo(Date awardStartDate, Date currentDate, Date lastBilledDate, ArConstants.BillingFrequencyValues billingFrequency, AccountingPeriodService accountingPeriodService) {
        BillingPeriod billingPeriod;
        if (ArConstants.BillingFrequencyValues.LETTER_OF_CREDIT.equals(billingFrequency)) {
            billingPeriod = new LetterOfCreditBillingPeriod(billingFrequency, awardStartDate, currentDate, lastBilledDate, accountingPeriodService);
        } else {
            billingPeriod = new TimeBasedBillingPeriod(billingFrequency, awardStartDate, currentDate, lastBilledDate, accountingPeriodService);
        }
        billingPeriod.billable = billingPeriod.canThisBeBilled();
        if (billingPeriod.billable) {
            billingPeriod.startDate = billingPeriod.determineStartDate();
            billingPeriod.endDate = billingPeriod.determineEndDateByFrequency();
            LOG.info("determineBillingPeriodPriorTo: IS billable with billingPeriod.startDate = " + billingPeriod.startDate + " billingPeriod.endDate = " + billingPeriod.endDate);
        }

        return billingPeriod;
    }

    protected abstract Date determineEndDateByFrequency();

    protected AccountingPeriod findAccountingPeriodBy(Date date) {
        return accountingPeriodService.getByDate(date);
    }

    protected boolean canThisBeBilled() {
        if (awardStartDate.after(currentDate)) {
            LOG.info("canThisBeBilled: NO -- award startDate after currentDate.");
            return false; // do not bill future awards!
        }

        if (lastBilledDate == null) {
            LOG.info("canThisBeBilled: YES -- lastBilledDate is NULL");
            return true;
        }
        return canThisBeBilledByBillingFrequency();
    }

    protected abstract boolean canThisBeBilledByBillingFrequency();

    protected Date determineStartDate() {
        if (lastBilledDate == null) {
            return awardStartDate;
        }
        return determineStartDateByFrequency();
    }

    protected abstract Date determineStartDateByFrequency();

    protected Date calculatePreviousDate(Date date) {
        return new Date(DateUtils.addDays(date, -1).getTime());
    }

    protected Date calculateNextDay(Date date) {
        return new Date(DateUtils.addDays(date, 1).getTime());
    }

    public boolean isBillable() {
        return billable;
    }

}
