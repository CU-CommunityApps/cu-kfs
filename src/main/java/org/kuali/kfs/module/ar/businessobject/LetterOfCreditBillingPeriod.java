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

import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.sys.util.KfsDateUtils;

import edu.cornell.kfs.module.ar.service.CuContractsGrantsBillingUtilityService;

import java.sql.Date;
import java.util.List;

public class LetterOfCreditBillingPeriod extends BillingPeriod {
    
    protected final CuContractsGrantsBillingUtilityService cuContractsGrantsBillingUtilityService;
    
    public LetterOfCreditBillingPeriod(ArConstants.BillingFrequencyValues billingFrequency, Date awardStartDate, Date currentDate, Date lastBilledDate, AccountingPeriodService accountingPeriodService, CuContractsGrantsBillingUtilityService cuContractsGrantsBillingUtilityService) {
        super(billingFrequency, awardStartDate, currentDate, lastBilledDate, accountingPeriodService);
        this.cuContractsGrantsBillingUtilityService = cuContractsGrantsBillingUtilityService;
    }

    @Override
    protected Date determineEndDateByFrequency() {
        return calculatePreviousDate(currentDate);
    }

    @Override
    protected boolean canThisBeBilledByBillingFrequency() {
        return (!KfsDateUtils.isSameDay(currentDate, lastBilledDate) && !KfsDateUtils.isSameDay(lastBilledDate, calculatePreviousDate(currentDate)));
    }

    @Override
    protected Date determineStartDateByFrequency() {
        return lastBilledDate;
    }

    @Override
    protected Date determineLastBilledDateByInvoicingOption(List<ContractsAndGrantsBillingAwardAccount> awardAccounts, String invoicingOptionCode, Date awardLastBilledDate) {
        return cuContractsGrantsBillingUtilityService.determineLastBilledDateByInvoicingOption(awardAccounts, invoicingOptionCode,awardLastBilledDate );
    }

}
