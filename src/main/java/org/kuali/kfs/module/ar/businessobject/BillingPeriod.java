package org.kuali.kfs.module.ar.businessobject;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.module.ar.ArConstants;
import edu.cornell.kfs.module.ar.service.CuContractsGrantsBillingUtilityService;

import java.sql.Date;
import java.util.List;

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

    public static BillingPeriod determineBillingPeriodPriorTo(Date awardStartDate, Date currentDate, Date awardLastBilledDate, 
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts, String invoicingOptionCode, 
            ArConstants.BillingFrequencyValues billingFrequency, AccountingPeriodService accountingPeriodService, CuContractsGrantsBillingUtilityService cuContractsGrantsBillingUtilityService) {
        
        //Declaring locals to be able to call non-static method from static method as well as get around final class attributes.
        BillingPeriod tempBillingPeriod;
        Date tempLastBilledDate = null;
        if (ArConstants.BillingFrequencyValues.LETTER_OF_CREDIT.equals(billingFrequency)) {
            tempBillingPeriod = new LetterOfCreditBillingPeriod(billingFrequency, awardStartDate, currentDate, tempLastBilledDate, accountingPeriodService, cuContractsGrantsBillingUtilityService);
        } else {
            tempBillingPeriod = new TimeBasedBillingPeriod(billingFrequency, awardStartDate, currentDate, tempLastBilledDate, accountingPeriodService, cuContractsGrantsBillingUtilityService);
        }
        Date lastBilledDate = tempBillingPeriod.determineLastBilledDateByInvoicingOption(awardAccounts, invoicingOptionCode, awardLastBilledDate);
        
        //Now construct billing period to return to caller based on temp calculated values
        BillingPeriod billingPeriod;
        if (ArConstants.BillingFrequencyValues.LETTER_OF_CREDIT.equals(billingFrequency)) {
            billingPeriod = new LetterOfCreditBillingPeriod(billingFrequency, awardStartDate, currentDate, lastBilledDate, accountingPeriodService, cuContractsGrantsBillingUtilityService);
        } else {
            billingPeriod = new TimeBasedBillingPeriod(billingFrequency, awardStartDate, currentDate, lastBilledDate, accountingPeriodService, cuContractsGrantsBillingUtilityService);
        }
        billingPeriod.billable = billingPeriod.canThisBeBilled();
        if (billingPeriod.billable) {
            billingPeriod.startDate = billingPeriod.determineStartDate();
            billingPeriod.endDate = billingPeriod.determineEndDateByFrequency();
        }
        return billingPeriod;
    }

    protected abstract Date determineLastBilledDateByInvoicingOption(List<ContractsAndGrantsBillingAwardAccount> awardAccounts, String invoicingOptionCode, Date awardLastBilledDate);

    protected abstract Date determineEndDateByFrequency();

    protected AccountingPeriod findAccountingPeriodBy(Date date) {
        return accountingPeriodService.getByDate(date);
    }

    protected boolean canThisBeBilled() {
        if (awardStartDate.after(currentDate)) {
            return false; // do not bill future awards!
        }

        if (lastBilledDate == null) {
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
