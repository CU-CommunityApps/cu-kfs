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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

import edu.cornell.kfs.module.ar.CuArConstants;
import edu.cornell.kfs.module.ar.batch.service.CuVerifyBillingFrequencyService;

import java.sql.Date;

public class CuVerifyBillingFrequencyServiceImpl extends VerifyBillingFrequencyServiceImpl implements CuVerifyBillingFrequencyService {
    
    private static final Logger LOG = LogManager.getLogger(CuVerifyBillingFrequencyServiceImpl.class);
    
    private Date calculateLastBilledDateBasedOnAwardInvoiceOption(ContractsAndGrantsBillingAward award, String awardInvoiceOption) {
        Date lastBilledDateToUse;
        if (StringUtils.equals(awardInvoiceOption, ArConstants.INV_CONTRACT_CONTROL_ACCOUNT)
                || StringUtils.equals(awardInvoiceOption, ArConstants.INV_ACCOUNT)) {
            if (CollectionUtils.isNotEmpty(award.getActiveAwardAccounts())) {
                lastBilledDateToUse = award.getActiveAwardAccounts().get(0).getCurrentLastBilledDate();
                LOG.info("calculateLastBilledDateBasedOnAwardInvoiceOption: Detected Award invoicingOptionCode = "
                        + CuArConstants.AwardInvoicingOptionCodeToName.getName(awardInvoiceOption)
                        + ". Will be using currentLastBilledDate = " + lastBilledDateToUse
                        + " from Account = " + award.getActiveAwardAccounts().get(0).getAccountNumber());
            } else {
                lastBilledDateToUse = award.getLastBilledDate();
                LOG.error("calculateLastBilledDateBasedOnAwardInvoiceOption: NO active award accounts on Award with invoicingOptionCode = "
                         + CuArConstants.AwardInvoicingOptionCodeToName.getName(awardInvoiceOption)
                         + ". Using Award lastBilledDate = " + lastBilledDateToUse);
            }
        } else {
            lastBilledDateToUse = award.getLastBilledDate();
            LOG.info("calculateLastBilledDateBasedOnAwardInvoiceOption: Detected Award invoicingOptionCode = "
                    + CuArConstants.AwardInvoicingOptionCodeToName.getName(awardInvoiceOption)
                    + ". Award lastBilledDate = " + lastBilledDateToUse + " will be used.");
        }
        return lastBilledDateToUse;
    }

    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award) {
        LOG.info("validateBillingFrequency: CU-MOD invoked with Award containing creationProcessTypeCode = " 
                + award.getCgInvoiceDocumentCreationProcessTypeCode() + ". Using Award invoicingOptionCode to determine lastBilledDate to send to private method.");
        return validateBillingFrequency(award, calculateLastBilledDateBasedOnAwardInvoiceOption(award, award.getInvoicingOptionCode()));
    }

    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, ContractsAndGrantsBillingAwardAccount awardAccount) {
        LOG.info("validateBillingFrequency: CU-MOD invoked with Award, Award Account. Sending awardAccount = "
                + awardAccount.getAccountNumber() + " with currentLastBilledDate = "
                + awardAccount.getCurrentLastBilledDate() + " and Award containing creationProcessTypeCode = "
                + ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.getName(award.getCgInvoiceDocumentCreationProcessTypeCode()) + " to private method.");
        return validateBillingFrequency(award, awardAccount.getCurrentLastBilledDate());
    }

    private boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, Date lastBilledDate) {
        LOG.info("validateBillingFrequency(private): For Award/Proposal# = " + award.getProposalNumber()
                + " with calculatedLastBilledDate = " + lastBilledDate
                + " creationProcessTypeCode = " + ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.getName(award.getCgInvoiceDocumentCreationProcessTypeCode()));
        final Date today = getDateTimeService().getCurrentSqlDate();
        AccountingPeriod currPeriod = getAccountingPeriodService().getByDate(today);

        BillingPeriod billingPeriod = getStartDateAndEndDateOfPreviousBillingPeriod(award, lastBilledDate, currPeriod, award.getCgInvoiceDocumentCreationProcessTypeCode());
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

    @Override
    public BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(ContractsAndGrantsBillingAward award, AccountingPeriod currPeriod) {
        LOG.info("getStartDateAndEndDateOfPreviousBillingPeriod: Basecode method called. Invoking CU-MOD with Award lastBilledDate = "
                + award.getLastBilledDate() + " and creationProcessTypeCode = " + award.getCgInvoiceDocumentCreationProcessTypeCode());
        return getStartDateAndEndDateOfPreviousBillingPeriod(award, award.getLastBilledDate(), currPeriod, award.getCgInvoiceDocumentCreationProcessTypeCode());
    }

    @Override
    public BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(ContractsAndGrantsBillingAward award, Date calculatedLastBilledDate, AccountingPeriod currPeriod, String creationProcessTypeCode) {
        return BillingPeriod.determineBillingPeriodPriorTo(award.getAwardBeginningDate(), this.dateTimeService.getCurrentSqlDate(), calculatedLastBilledDate, ArConstants.BillingFrequencyValues.fromCode(award.getBillingFrequencyCode()), this.accountingPeriodService, creationProcessTypeCode);
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
