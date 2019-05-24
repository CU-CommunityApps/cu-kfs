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
package edu.cornell.kfs.module.ar.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.batch.service.impl.VerifyBillingFrequencyServiceImpl;
import org.kuali.kfs.module.ar.businessobject.BillingFrequency;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.module.ar.batch.service.CuVerifyBillingFrequencyService;

import java.sql.Date;

public class CuVerifyBillingFrequencyServiceImpl extends VerifyBillingFrequencyServiceImpl implements CuVerifyBillingFrequencyService {
    
    private static final Logger LOG = LogManager.getLogger(CuVerifyBillingFrequencyServiceImpl.class);

    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award) {
        LOG.info("validateBillingFrequency: Called with just Award. Sending Award lastBilledDate = " + award.getLastBilledDate() + " to private method.");
        return validateBillingFrequency(award, award.getLastBilledDate());
    }

    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, ContractsAndGrantsBillingAwardAccount awardAccount) {
        LOG.info("validateBillingFrequency: Called with Award and Award Account. Sending awardAccount currentLastBilledDate = " + awardAccount.getCurrentLastBilledDate() + " to private method.");
        return validateBillingFrequency(award, awardAccount.getCurrentLastBilledDate());
    }

    private boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, Date lastBilledDate) {
        LOG.info("validateBillingFrequency: For Award/Proposal# = " + award.getProposalNumber() + " with lastBilledDate = " + lastBilledDate);
        final Date today = getDateTimeService().getCurrentSqlDate();
        AccountingPeriod currPeriod = getAccountingPeriodService().getByDate(today);

        BillingPeriod billingPeriod = getStartDateAndEndDateOfPreviousBillingPeriod(award, currPeriod);
        if (!billingPeriod.isBillable()) {
            LOG.info("validateBillingFrequency: NOT VALID: getStartDateAndEndDateOfPreviousBillingPeriod returned billingPeriod" 
                    + " denoting Award is NOT billable: billingPeriod.startDate = " + billingPeriod.getStartDate() 
                    + " billingPeriod.endDate = " + billingPeriod.getEndDate());
            return false;
        }
        if (billingPeriod.getStartDate().after(billingPeriod.getEndDate())
                && !ArConstants.BillingFrequencyValues.isMilestone(award)
                && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            LOG.info("validateBillingFrequency: NOT VALID: getStartDateAndEndDateOfPreviousBillingPeriod returned billingPeriod"
                    + " where startDate = " + billingPeriod.getStartDate() + " is AFTER billingPeriod"
                    + " endDate = " + billingPeriod.getEndDate() + " AND Award billing frequency = " + award.getBillingFrequencyCode()
                    + " is NOT Milestone and is NOT Predetermined Billing.");
            return false;
        }
        return calculateIfWithinGracePeriod(today, billingPeriod, lastBilledDate, (BillingFrequency) award.getBillingFrequency());
    }

    @Override
    public boolean calculateIfWithinGracePeriod(Date today, BillingPeriod billingPeriod, Date lastBilledDate, BillingFrequency billingFrequency) {
        Date gracePeriodEnd = calculateDaysBeyond(billingPeriod.getEndDate(), billingFrequency.getGracePeriodDays());
        Date gracePeriodAfterLastBilled = null;
        if (lastBilledDate != null) {
            gracePeriodAfterLastBilled = calculateDaysBeyond(lastBilledDate, billingFrequency.getGracePeriodDays());
        }
        boolean beforeGracePeriodEnd = KfsDateUtils.isSameDayOrEarlier(gracePeriodEnd, today);
        boolean afterBillingStart = KfsDateUtils.isSameDayOrLater(today, billingPeriod.getStartDate());
        boolean haveNotBilledYet = lastBilledDate == null || KfsDateUtils.isEarlierDay(gracePeriodAfterLastBilled, today);
        
        LOG.info("calculateIfWithinGracePeriod: gracePeriodEnd = " + gracePeriodEnd + " gracePeriodAfterLastBilled = " + gracePeriodAfterLastBilled + " lastBilledDate = " + lastBilledDate);
        LOG.info("calculateIfWithinGracePeriod: All must be true to be within grace period: beforeGracePeriodEnd = " + beforeGracePeriodEnd + " afterBillingStart = " + afterBillingStart + " haveNotBilledYet = " + haveNotBilledYet);

        return afterBillingStart && beforeGracePeriodEnd && haveNotBilledYet;
    }

    public DateTimeService getDateTimeService() {
        return super.getDateTimeService();
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        super.setDateTimeService(dateTimeService);
    }

    public AccountingPeriodService getAccountingPeriodService() {
        return super.accountingPeriodService;
    }

    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        super.setAccountingPeriodService(accountingPeriodService);
    }

    public BusinessObjectService getBusinessObjectService() {
        return super.getBusinessObjectService();
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        super.setBusinessObjectService(businessObjectService);
    }

}
