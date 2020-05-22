//KualiCo Patch Release 2020-02-13
//FINP-4769 FK-117 changes applied.
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
package org.kuali.kfs.module.ar.batch.service;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;

/**
 * Interface class for Billing Frequency validation.
 */
public interface VerifyBillingFrequencyService {

    /**
     * This method checks if the award is within the grace period.
     *
     * @param award ContractsAndGrantsBillingAward to validate billing frequency for
     * @param checkBillingPeriodEnd boolean to check grace period logic
     * @return true if valid else false.
     */
    boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, boolean checkBillingPeriodEnd);

    /**
     * This method checks if the award account is within the grace period.
     *
     * @param award ContractsAndGrantsBillingAward to validate billing frequency for
     * @param award ContractsAndGrantsBillingAwardAccount to validate billing frequency for
     * * @param checkBillingPeriodEnd boolean to check grace period logic
     * @return true if valid else false.
     */
    boolean validateBillingFrequency(ContractsAndGrantsBillingAward award, ContractsAndGrantsBillingAwardAccount awardAccount, boolean checkBillingPeriodEnd);

    /**
     * This method returns the start and end date of previous billing period.
     *
     * @param award      ContractsAndGrantsBillingAward used to get dates and billing frequency for calculations
     * @param currPeriod accounting period used for calculations (typically the current period)
     * @return Date array containing start date and end date of previous billing period
     */
    BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(ContractsAndGrantsBillingAward award, AccountingPeriod currPeriod);

}
