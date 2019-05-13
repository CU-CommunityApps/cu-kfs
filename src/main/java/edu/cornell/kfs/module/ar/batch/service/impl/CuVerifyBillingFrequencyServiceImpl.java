package edu.cornell.kfs.module.ar.batch.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.util.List;

import edu.cornell.kfs.module.ar.batch.service.CuVerifyBillingFrequencyService;
import edu.cornell.kfs.module.ar.service.CuContractsGrantsBillingUtilityService;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.batch.service.impl.VerifyBillingFrequencyServiceImpl;
import org.kuali.kfs.module.ar.businessobject.BillingFrequency;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;
import org.kuali.rice.core.api.datetime.DateTimeService;

public class CuVerifyBillingFrequencyServiceImpl extends VerifyBillingFrequencyServiceImpl implements CuVerifyBillingFrequencyService {
    
    private static final Logger LOG = LogManager.getLogger(CuVerifyBillingFrequencyServiceImpl.class);
    
    protected CuContractsGrantsBillingUtilityService cuContractsGrantsBillingUtilityService;
    protected DateTimeService dateTimeService;
    
    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award) {
        String awardInvoiceOption = award.getInvoicingOptionCode();
        Date lastBilledDateToUse;
        if (StringUtils.equals(awardInvoiceOption, ArConstants.INV_CONTRACT_CONTROL_ACCOUNT)
                || StringUtils.equals(awardInvoiceOption, ArConstants.INV_ACCOUNT)) {
            if (CollectionUtils.isNotEmpty(award.getActiveAwardAccounts())) {
                lastBilledDateToUse = award.getActiveAwardAccounts().get(0).getCurrentLastBilledDate();
            } else {
                LOG.error("validateBillingFrequency: NO active award accounts on Award with invoice option of Account or Contract Control Account. Award lastBilledDate being used for CINV creation.");
                lastBilledDateToUse = award.getLastBilledDate();
            }
        } else {
            lastBilledDateToUse = award.getLastBilledDate();
        }
        return validateBillingFrequency(award, lastBilledDateToUse);
    }
    
    @Override
    public boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, ContractsAndGrantsBillingAwardAccount awardAccount) {
        return validateBillingFrequency(award, awardAccount.getCurrentLastBilledDate());
    }

    private boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, Date lastBilledDate) {
        final Date today = getDateTimeService().getCurrentSqlDate();
        AccountingPeriod currPeriod = accountingPeriodService.getByDate(today);

        BillingPeriod billingPeriod = getStartDateAndEndDateOfPreviousBillingPeriod(award, award.getActiveAwardAccounts(), currPeriod);
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
    
    @Override
    public BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(ContractsAndGrantsBillingAward award, List<ContractsAndGrantsBillingAwardAccount> awardAccounts, AccountingPeriod currPeriod) {
        return BillingPeriod.determineBillingPeriodPriorTo(award.getAwardBeginningDate(), this.dateTimeService.getCurrentSqlDate(), 
                award.getLastBilledDate(), awardAccounts, award.getInvoicingOptionCode(), ArConstants.BillingFrequencyValues.fromCode(award.getBillingFrequencyCode()), this.accountingPeriodService, this.cuContractsGrantsBillingUtilityService);
    }

    public CuContractsGrantsBillingUtilityService getCuContractsGrantsBillingUtilityService() {
        return cuContractsGrantsBillingUtilityService;
    }

    public void setCuContractsGrantsBillingUtilityService(
            CuContractsGrantsBillingUtilityService cuContractsGrantsBillingUtilityService) {
        this.cuContractsGrantsBillingUtilityService = cuContractsGrantsBillingUtilityService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
