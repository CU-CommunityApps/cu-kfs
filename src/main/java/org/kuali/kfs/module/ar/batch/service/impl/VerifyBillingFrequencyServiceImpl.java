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
package org.kuali.kfs.module.ar.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.batch.service.VerifyBillingFrequencyService;
import org.kuali.kfs.module.ar.businessobject.BillingFrequency;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.util.KfsDateUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

public class VerifyBillingFrequencyServiceImpl implements VerifyBillingFrequencyService {
    protected BusinessObjectService businessObjectService;
    private AccountingPeriodService accountingPeriodService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;

    private static final Set<String> invalidPeriodCodes = new TreeSet<>();

    static {
        invalidPeriodCodes.add(KFSConstants.MONTH13);
        invalidPeriodCodes.add(KFSConstants.PERIOD_CODE_ANNUAL_BALANCE);
        invalidPeriodCodes.add(KFSConstants.PERIOD_CODE_BEGINNING_BALANCE);
        invalidPeriodCodes.add(KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE);
    }

    @Override
    public boolean validateBillingFrequency(final ContractsAndGrantsBillingAward award, final boolean checkBillingPeriodEnd) {
        return validateBillingFrequency(award, award.getLastBilledDate(), checkBillingPeriodEnd);
    }

    @Override
    public boolean validateBillingFrequency(final ContractsAndGrantsBillingAward award, final ContractsAndGrantsBillingAwardAccount awardAccount, final boolean checkBillingPeriodEnd) {
        return validateBillingFrequency(award, awardAccount.getCurrentLastBilledDate(), checkBillingPeriodEnd);
    }

    private boolean validateBillingFrequency(final ContractsAndGrantsBillingAward award, final Date lastBilledDate, final boolean checkBillingPeriodEnd) {
        final Date today = dateTimeService.getCurrentSqlDate();
        final AccountingPeriod currPeriod = accountingPeriodService.getByDate(today);

        final BillingPeriod billingPeriod = getStartDateAndEndDateOfPreviousBillingPeriod(award, currPeriod);
        if (!billingPeriod.isBillable()) {
            return false;
        }
        if (billingPeriod.getStartDate().after(billingPeriod.getEndDate())
                && !ArConstants.BillingFrequencyValues.isMilestone(award)
                && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            return false;
        }
        if (beforeBillingPeriodStart(billingPeriod)) {
            return false;
        }
        return validateBillingFrequencyWithGracePeriod(today, billingPeriod, lastBilledDate, (BillingFrequency) award.getBillingFrequency(), checkBillingPeriodEnd);
    }

    /**
     * @param billingPeriod the billing period to be checked
     * @return true if today is earlier than the start date of billing period; false if today is same day or after billing start
     */
    private boolean beforeBillingPeriodStart(final BillingPeriod billingPeriod) {
        final Date today = dateTimeService.getCurrentSqlDate();
        return KfsDateUtils.isEarlierDay(today, billingPeriod.getStartDate());
    }

    public boolean validateBillingFrequencyWithGracePeriod(final Date today, final BillingPeriod billingPeriod, final Date lastBilledDate, final BillingFrequency billingFrequency, final boolean checkBillingPeriodEnd) {
        final Date gracePeriodAfterBillingEnd = calculateDaysBeyond(billingPeriod.getEndDate(), billingFrequency.getGracePeriodDays());
        Date gracePeriodAfterLastBilled = null;
        if (lastBilledDate != null) {
            gracePeriodAfterLastBilled = calculateDaysBeyond(lastBilledDate, billingFrequency.getGracePeriodDays());
        }

        final boolean afterBillingPeriodEnd = !checkBillingPeriodEnd || KfsDateUtils.isSameDayOrLater(today, gracePeriodAfterBillingEnd);
        final boolean haveNotBilledYet = lastBilledDate == null || KfsDateUtils.isEarlierDay(gracePeriodAfterLastBilled, today);
        return afterBillingPeriodEnd && haveNotBilledYet;
    }

    /*
     * CU Customization: KFSPTS-23690
     * When creating CINV documents, obtain creationProcessType transient attribute from Award input parameter and use it;
     * otherwise, execute base code version of the method for viewing and editing CINV.
     */
    @Override
    public BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(final ContractsAndGrantsBillingAward award, final AccountingPeriod currPeriod) {
        /* KFSPTS-23690 */
        if (ObjectUtils.isNotNull(award.getCreationProcessType())) {
            return BillingPeriod.determineBillingPeriodPriorTo(award.getAwardBeginningDate(), dateTimeService.getCurrentSqlDate(), award.getLastBilledDate(), ArConstants.BillingFrequencyValues.fromCode(award.getBillingFrequencyCode()), accountingPeriodService, award.getCreationProcessType());
        }
        return BillingPeriod.determineBillingPeriodPriorTo(award.getAwardBeginningDate(), dateTimeService.getCurrentSqlDate(), award.getLastBilledDate(), ArConstants.BillingFrequencyValues.fromCode(award.getBillingFrequencyCode()), accountingPeriodService);
    }

    private Date calculateDaysBeyond(final Date date, final int daysBeyond) {
        final LocalDate beyondDate = date.toLocalDate().plusDays(daysBeyond);
        return Date.valueOf(beyondDate);
    }

    /**
     * This checks to see if the period code is empty or invalid ("13", "AB", "BB", "CB")
     *
     * @param period
     * @return
     */
    protected boolean isInvalidPeriodCode(final AccountingPeriod period) {
        final String periodCode = period.getUniversityFiscalPeriodCode();
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
    public void setAccountingPeriodService(final AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    /**
     * Sets the universityDateService attribute value.
     *
     * @param universityDateService The universityDateService to set.
     */
    public void setUniversityDateService(final UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
