/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.sys.util.KfsDateUtils;

import java.sql.Date;

/*
 * CUMod: KFSPTS-15655 Brought this class file into CU customizations for additional logging.
 */
public class LetterOfCreditBillingPeriod extends BillingPeriod {
    
    private static final Logger LOG = LogManager.getLogger(LetterOfCreditBillingPeriod.class);
    
    public LetterOfCreditBillingPeriod(ArConstants.BillingFrequencyValues billingFrequency, Date awardStartDate,
            Date currentDate, Date lastBilledDate, AccountingPeriodService accountingPeriodService) {
        super(billingFrequency, awardStartDate, currentDate, lastBilledDate, accountingPeriodService);
    }

    @Override
    protected Date determineEndDateByFrequency() {
        Date computedEndDateByFrequency = calculatePreviousDate(currentDate);
        LOG.info("determineEndDateByFrequency: Calling calculatePreviousDate(currentDate) to return =" + computedEndDateByFrequency);
        return computedEndDateByFrequency;
    }

    @Override
    protected boolean canThisBeBilledByBillingFrequency() {
        boolean currentDateisNotSameDayAsLastBilledDate = !KfsDateUtils.isSameDay(currentDate, lastBilledDate);
        boolean lastBilledDateIsNotSameDayAsCalculatedPreviousDayBasedOnCurrentDate = !KfsDateUtils.isSameDay(lastBilledDate, calculatePreviousDate(currentDate));
        LOG.info("canThisBeBilledByBillingFrequency: Both must be TRUE to allow LOC billedByBillingFrequency: "
                + " currentDate and lastBilledDate NOT(isSameDay) = " + currentDateisNotSameDayAsLastBilledDate 
                + " AND lastBilledDate and calculatePreviousDate !(isSameDay) = " + lastBilledDateIsNotSameDayAsCalculatedPreviousDayBasedOnCurrentDate);
        return (currentDateisNotSameDayAsLastBilledDate && lastBilledDateIsNotSameDayAsCalculatedPreviousDayBasedOnCurrentDate);
    }

    @Override
    protected Date determineStartDateByFrequency() {
        LOG.info("determineStartDateByFrequency: returning lastBilledDate =" + lastBilledDate);
        return lastBilledDate;
    }
    
    /*
     * CUMod: KFSPTS-14970
     */
    @Override
    protected Date adjustEndDateForManualBilling(Date currentDate) {
        LOG.info("adjustEndDateForManualBilling: Invoked LOC billingPeriod.startDate = " + super.getStartDate() 
                + " LOC billingPeriod.endDate = " + super.getEndDate() + " with existing endDate being returned. No special logic.");
        return getEndDate();
    }

}
