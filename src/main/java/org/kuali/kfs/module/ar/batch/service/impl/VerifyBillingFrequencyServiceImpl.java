//KualiCo Patch Release 2020-02-13
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
package org.kuali.kfs.module.ar.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.batch.service.VerifyBillingFrequencyService;
import org.kuali.kfs.module.ar.businessobject.BillingFrequency;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;

import java.sql.Date;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

public class VerifyBillingFrequencyServiceImpl implements VerifyBillingFrequencyService {
    protected BusinessObjectService businessObjectService;
    protected AccountingPeriodService accountingPeriodService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;

    protected static final Set<String> invalidPeriodCodes = new TreeSet<>();

    static {
        invalidPeriodCodes.add(KFSConstants.MONTH13);
        invalidPeriodCodes.add(KFSConstants.PERIOD_CODE_ANNUAL_BALANCE);
        invalidPeriodCodes.add(KFSConstants.PERIOD_CODE_BEGINNING_BALANCE);
        invalidPeriodCodes.add(KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE);
    }

    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award) {
        return validateBillingFrequency(award, award.getLastBilledDate());
    }

    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, ContractsAndGrantsBillingAwardAccount awardAccount) {
        return validateBillingFrequency(award, awardAccount.getCurrentLastBilledDate());
    }

    private boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, Date lastBilledDate) {
        final Date today = getDateTimeService().getCurrentSqlDate();
        AccountingPeriod currPeriod = accountingPeriodService.getByDate(today);

        BillingPeriod billingPeriod = getStartDateAndEndDateOfPreviousBillingPeriod(award, currPeriod);
        if (!billingPeriod.isBillable()) {
            return false;
        }
        if (billingPeriod.getStartDate().after(billingPeriod.getEndDate())
                && !ArConstants.BillingFrequencyValues.isMilestone(award)
                && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            return false;
        }
        return calculateIfWithinGracePeriod(today, billingPeriod, lastBilledDate, (BillingFrequency) award.getBillingFrequency());
    }

    public boolean calculateIfWithinGracePeriod(Date today, BillingPeriod billingPeriod, Date lastBilledDate, BillingFrequency billingFrequency) {
        Date gracePeriodEnd = calculateDaysBeyond(billingPeriod.getEndDate(), billingFrequency.getGracePeriodDays());
        Date gracePeriodAfterLastBilled = null;
        if (lastBilledDate != null) {
            gracePeriodAfterLastBilled = calculateDaysBeyond(lastBilledDate, billingFrequency.getGracePeriodDays());
        }
        boolean beforeGracePeriodEnd = KfsDateUtils.isSameDayOrEarlier(gracePeriodEnd, today);
        boolean afterBillingStart = KfsDateUtils.isSameDayOrLater(today, billingPeriod.getStartDate());
        boolean haveNotBilledYet = lastBilledDate == null || KfsDateUtils.isEarlierDay(gracePeriodAfterLastBilled, today);

        return afterBillingStart && beforeGracePeriodEnd && haveNotBilledYet;
    }

//Had to add "award.getCgInvoiceDocumentCreationProcessTypeCode()" to method call to get KualiCo Patch Release 2020-02-13 to compile
    @Override
    public BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(ContractsAndGrantsBillingAward award, AccountingPeriod currPeriod) {
        return BillingPeriod.determineBillingPeriodPriorTo(award.getAwardBeginningDate(), this.dateTimeService.getCurrentSqlDate(), award.getLastBilledDate(), ArConstants.BillingFrequencyValues.fromCode(award.getBillingFrequencyCode()), this.accountingPeriodService, award.getCgInvoiceDocumentCreationProcessTypeCode());
    }

    protected Date calculateDaysBeyond(Date date, int daysBeyond) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, daysBeyond);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * This checks to see if the period code is empty or invalid ("13", "AB", "BB", "CB")
     *
     * @param period
     * @return
     */
    protected boolean isInvalidPeriodCode(AccountingPeriod period) {
        String periodCode = period.getUniversityFiscalPeriodCode();
        if (StringUtils.isBlank(periodCode)) {
            throw new IllegalArgumentException("invalid (null) universityFiscalPeriodCode (" + periodCode + ")for" + period);
        }
        return invalidPeriodCodes.contains(periodCode);
    }

    /**
     * Sets the accountingPeriodService attribute value.
     *
     * @param accountingPeriodService The accountingPeriodService to set.
     */
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    /**
     * Sets the universityDateService attribute value.
     *
     * @param universityDateService The universityDateService to set.
     */
    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
