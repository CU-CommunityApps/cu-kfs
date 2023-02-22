/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

import java.sql.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;

public class BillingPeriod {

    // CU Customization: Added logging
    private static final Logger LOG = LogManager.getLogger();

    private final AccountingPeriodService accountingPeriodService;
    private final ArConstants.BillingFrequencyValues billingFrequency;
    private final Date awardStartDate;
    private final Date currentDate;
    private final Date lastBilledDate;
    private Date startDate;
    private Date endDate;
    private boolean billable;

    BillingPeriod(
            final ArConstants.BillingFrequencyValues billingFrequency,
            final Date awardStartDate,
            final Date currentDate,
            final Date lastBilledDate,
            final AccountingPeriodService accountingPeriodService
    ) {
        this.awardStartDate = awardStartDate;
        this.lastBilledDate = lastBilledDate;
        this.accountingPeriodService = accountingPeriodService;
        this.billingFrequency = billingFrequency;
        this.currentDate = currentDate;
    }
    
    public static BillingPeriod determineBillingPeriodPriorTo(            
            final Date awardStartDate,
            final Date currentDate,
            final Date lastBilledDate,
            final ArConstants.BillingFrequencyValues billingFrequency,
            final AccountingPeriodService accountingPeriodService
   ) {
        final BillingPeriod billingPeriod = new BillingPeriod(billingFrequency,
                awardStartDate,
                currentDate,
                lastBilledDate,
                accountingPeriodService
        );
        billingPeriod.billable = billingPeriod.canThisBeBilled();
        if (billingPeriod.billable) {
            billingPeriod.startDate = billingPeriod.determineStartDate();
            billingPeriod.endDate = billingPeriod.determineEndDateByFrequency();
        }

        return billingPeriod;
    }

    /*
     * CU Customization (KFSPTS-23675):
     * 
     * Updated the signature of the determineBillingPeriodPriorTo() method to add the creation process type
     * as an argument, and to allow for adjusting the period's end date when the process type is Manual.
     */
    public static BillingPeriod determineBillingPeriodPriorTo(Date awardStartDate, Date currentDate,
            Date lastBilledDate, ArConstants.BillingFrequencyValues billingFrequency, AccountingPeriodService accountingPeriodService,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        final BillingPeriod billingPeriod = new BillingPeriod(billingFrequency,
                awardStartDate,
                currentDate,
                lastBilledDate,
                accountingPeriodService
        );
        billingPeriod.billable = billingPeriod.canThisBeBilled();
        if (billingPeriod.billable) {
            billingPeriod.startDate = billingPeriod.determineStartDate();
            billingPeriod.endDate = billingPeriod.determineEndDateByFrequency();
            if (ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL == creationProcessType) {
                LOG.info("determineBillingPeriodPriorTo: Adjusting billable period's end date for manual invoice");
                billingPeriod.endDate = billingPeriod.determineEndDateForManualBilling();
            }
            LOG.info("determineBillingPeriodPriorTo: Period is billable, Start Date: "
                    + billingPeriod.startDate + ", End Date = " + billingPeriod.endDate);
        }

        return billingPeriod;
    }

    /*
     * CU Customization (KFSPTS-23675):
     * Added helper method that can override the period's end date when creating an invoice manually.
     */
    protected Date determineEndDateForManualBilling() {
        Objects.requireNonNull(startDate, "startDate should have been initialized prior to invoking this method");
        Objects.requireNonNull(endDate, "endDate should have been initialized prior to invoking this method");
        if (startDate.after(endDate)) {
            LOG.info("determineEndDateForManualBilling: Start date is after end date, returning current date instead");
            return currentDate;
        } else {
            LOG.info("determineEndDateForManualBilling: No adjustments necessary, returning existing end date");
            return endDate;
        }
    }

    private Date determineEndDateByFrequency() {
        final AccountingPeriod accountingPeriod = findPreviousAccountingPeriod(currentDate);
        return accountingPeriod.getUniversityFiscalPeriodEndDate();
    }

    private static Integer calculatePreviousPeriodByFrequency(
            final Integer currentAccountingPeriodCode,
            final int periodsInBillingFrequency
    ) {
        final int subAmt = currentAccountingPeriodCode % periodsInBillingFrequency
                           == 0 ? periodsInBillingFrequency : currentAccountingPeriodCode % periodsInBillingFrequency;

        return currentAccountingPeriodCode - subAmt;
    }

    private boolean canThisBeBilledByBillingFrequency() {
        if (billingFrequency == ArConstants.BillingFrequencyValues.ANNUALLY
            && accountingPeriodService.getByDate(lastBilledDate).getUniversityFiscalYear() >=
                    accountingPeriodService.getByDate(currentDate).getUniversityFiscalYear()) {
            return false;
        } else {
            return !StringUtils.equals(findPreviousAccountingPeriod(currentDate).getUniversityFiscalPeriodCode(),
                        findPreviousAccountingPeriod(lastBilledDate).getUniversityFiscalPeriodCode())
                    || !accountingPeriodService.getByDate(lastBilledDate).getUniversityFiscalYear()
                        .equals(accountingPeriodService.getByDate(currentDate).getUniversityFiscalYear());
        }

    }

    private Date determineStartDate() {
        if (lastBilledDate == null) {
            if (awardStartDate.after(currentDate)) {
                final AccountingPeriod previousAccountingPeriod = findPreviousAccountingPeriod(currentDate);
                final AccountingPeriod beforePreviousAccountingPeriod = findPreviousAccountingPeriod(
                        previousAccountingPeriod.getUniversityFiscalPeriodEndDate());
                return calculateNextDay(beforePreviousAccountingPeriod.getUniversityFiscalPeriodEndDate());
            } else {
                return awardStartDate;
            }
        }
        return determineStartDateByFrequency();
    }

    private Date determineStartDateByFrequency() {
        if (lastBilledDate == null) {
            LOG.info("determineStartDateByFrequency, no previous billed date, so award start date is the next start date");
            return awardStartDate;
        }
        return calculateNextDay(lastBilledDate);
    }

    private AccountingPeriod findPreviousAccountingPeriod(final Date date) {
        final AccountingPeriod currentAccountingPeriod = findAccountingPeriodBy(date);
        final Integer currentAccountingPeriodCode =
                Integer.parseInt(currentAccountingPeriod.getUniversityFiscalPeriodCode());
        Integer previousAccountingPeriodCode = findPreviousAccountingPeriodCode(currentAccountingPeriodCode);

        Integer currentFiscalYear = currentAccountingPeriod.getUniversityFiscalYear();
        if (previousAccountingPeriodCode == 0) {
            previousAccountingPeriodCode = 12;
            currentFiscalYear -= 1;
        }

        final String periodCode;
        if (previousAccountingPeriodCode < 10) {
            periodCode = "0" + previousAccountingPeriodCode;
        } else {
            periodCode = String.valueOf(previousAccountingPeriodCode);
        }

        return accountingPeriodService.getByPeriod(periodCode, currentFiscalYear);
    }

    private Integer findPreviousAccountingPeriodCode(final Integer currentAccountingPeriodCode) {
        final Integer previousAccountingPeriodCode;
        if (billingFrequency == ArConstants.BillingFrequencyValues.LETTER_OF_CREDIT
            || billingFrequency == ArConstants.BillingFrequencyValues.MONTHLY
            || billingFrequency == ArConstants.BillingFrequencyValues.MANUAL
            || billingFrequency == ArConstants.BillingFrequencyValues.MILESTONE
            || billingFrequency == ArConstants.BillingFrequencyValues.PREDETERMINED_BILLING) {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 1);
        } else if (billingFrequency == ArConstants.BillingFrequencyValues.QUARTERLY) {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 3);
        } else if (billingFrequency == ArConstants.BillingFrequencyValues.SEMI_ANNUALLY) {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 6);
        } else {
            previousAccountingPeriodCode = calculatePreviousPeriodByFrequency(currentAccountingPeriodCode, 12);
        }
        return previousAccountingPeriodCode;
    }
    
    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    
    private AccountingPeriod findAccountingPeriodBy(final Date date) {
        return accountingPeriodService.getByDate(date);
    }

    boolean canThisBeBilled() {
        if (lastBilledDate == null) {
            return true;
        }

        return canThisBeBilledByBillingFrequency();
    }

    private static Date calculateNextDay(final Date date) {
        return new Date(DateUtils.addDays(date, 1).getTime());
    }

    public boolean isBillable() {
        return billable;
    }

}
