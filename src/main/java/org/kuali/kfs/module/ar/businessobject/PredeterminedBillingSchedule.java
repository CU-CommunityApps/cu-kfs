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

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.integration.ar.AccountsReceivableModuleBillingService;
import org.kuali.kfs.integration.ar.AccountsReceivablePredeterminedBillingSchedule;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAgency;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.integration.cg.ContractsAndGrantsModuleBillingService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/*
 * CU Customization: Overlayed this file to include the FINP-4678 fix from the 2019-05-02 KualiCo patch.
 * Please remove this overlay once we upgrade to financials version 2019-05-02 or newer.
 */
public class PredeterminedBillingSchedule extends PersistableBusinessObjectBase
    implements AccountsReceivablePredeterminedBillingSchedule {

    private static final String PREDETERMINED_BILLING_SCHEDULE_INQUIRY_TITLE_PROPERTY
        = "message.inquiry.predetermined.billing.schedule.title";
    private String proposalNumber;
    private String chartOfAccountsCode;
    private String accountNumber;

    private List<Bill> bills = new LinkedList<>();
    private Account account;
    private Chart chart;
    private ContractsAndGrantsBillingAward award;
    private ContractsAndGrantsBillingAwardAccount awardAccount;

    private transient String agencyNumber;
    private transient ContractsAndGrantsBillingAgency agency;

    private transient AccountsReceivableModuleBillingService accountsReceivableModuleBillingService;
    private transient ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService;

    /**
     * Dummy values used to facilitate Award Account Lookup on the maintenance doc
     */
    private transient String proposalNumberForAwardAccountLookup;
    private transient String chartOfAccountsCodeForAwardAccountLookup;
    private transient String accountNumberForAwardAccountLookup;

    public PredeterminedBillingSchedule() {
    }

    private PredeterminedBillingSchedule(String proposalNumber, String chartOfAccountsCode, String accountNumber,
            List<Bill> bills) {
        this.proposalNumber = proposalNumber;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.bills = bills;
    }

    @Override
    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public KualiDecimal getTotalScheduledAccount() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (Bill bill : bills) {
            if (ObjectUtils.isNotNull(bill.getEstimatedAmount()) && bill.isActive()) {
                total = total.add(bill.getEstimatedAmount());
            }
        }
        return total;
    }

    public KualiDecimal getTotalScheduledAward() {
        return getTotalScheduledAccount().add(getAccountsReceivableModuleBillingService()
                .getBillsTotalAmountForOtherSchedules(proposalNumber, chartOfAccountsCode, accountNumber));
    }

    @Override
    public KualiDecimal getTotalAmountRemaining() {
        KualiDecimal total = KualiDecimal.ZERO;
        if (ObjectUtils.isNull(award)) {
            award = getAward();
        }
        if (ObjectUtils.isNotNull(award) && ObjectUtils.isNotNull(award.getAwardTotalAmount())) {
            total = award.getAwardTotalAmount().subtract(getTotalScheduledAward());
        }
        return total;
    }

    @Override
    public String getPredeterminedBillingScheduleInquiryTitle() {
        return SpringContext.getBean(ConfigurationService.class)
            .getPropertyValueAsString(PREDETERMINED_BILLING_SCHEDULE_INQUIRY_TITLE_PROPERTY);

    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public void addBill(Bill bill) {
        bills.add(bill);
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    /**
     * @return the award, if the award is null or the proposal number has changed, will attempt to retrieve it via
     * the module service
     */
    @Override
    public ContractsAndGrantsBillingAward getAward() {
        final ContractsAndGrantsBillingAward updatedAward = getContractsAndGrantsModuleBillingService().
                updateAwardIfNecessary(proposalNumber, award);

        // If the updated award is null, we return the original award instead so that we don't get an NPE in the case
        // where it has been set by PojoPropertyUtilsBean.
        if (ObjectUtils.isNull(updatedAward)) {
            return award;
        }
        return updatedAward;
    }

    public void setAward(ContractsAndGrantsBillingAward award) {
        this.award = award;
    }

    /**
     * This method forces an update of the Award by setting award to null prior to calling getAward which calls
     * updateAwardIfNecessary internally - the fact that award is null should satisfy the "if necessary" condition.
     */
    public void forceAwardUpdate() {
        this.award = null;
        this.award = getAward();
    }

    public ContractsAndGrantsBillingAwardAccount getAwardAccount() {
        final ContractsAndGrantsBillingAwardAccount updatedAwardAccount = getContractsAndGrantsModuleBillingService().
                updateAwardAccountIfNecessary(proposalNumber, chartOfAccountsCode, accountNumber, awardAccount);

        // If the updated award account is null, we return the original awardAccount instead so that we don't get an NPE
        // in the case where it has been set by PojoPropertyUtilsBean.
        if (ObjectUtils.isNull(updatedAwardAccount)) {
            return awardAccount;
        }
        return updatedAwardAccount;
    }

    public void setAwardAccount(ContractsAndGrantsBillingAwardAccount awardAccount) {
        this.awardAccount = awardAccount;
    }

    /**
     * This is special just for the lookup, but it is what is displayed in the maintenance doc, so if it's not populated
     * we want to return the proposal number.
     *
     * @return proposalNumberForAwardAccountLookup if populated, otherwise proposalNumber
     */
    public String getProposalNumberForAwardAccountLookup() {
        return StringUtils.isNotBlank(proposalNumberForAwardAccountLookup)
                ? proposalNumberForAwardAccountLookup : proposalNumber;
    }

    public void setProposalNumberForAwardAccountLookup(String proposalNumberForAwardAccountLookup) {
        this.proposalNumberForAwardAccountLookup = proposalNumberForAwardAccountLookup;
    }

    /**
     * If the lookup only value hasn't been set yet, return the chartOfAccountsCode so the value pre-populated on the
     * lookup is consistent with what the user saw on the maintenance doc.
     *
     * @return chartOfAccountsCodeForAwardAccountLookup if populated, otherwise chartOfAccountsCode
     */
    public String getChartOfAccountsCodeForAwardAccountLookup() {
        return StringUtils.isNotBlank(chartOfAccountsCodeForAwardAccountLookup)
                ? chartOfAccountsCodeForAwardAccountLookup : chartOfAccountsCode;
    }

    public void setChartOfAccountsCodeForAwardAccountLookup(String chartOfAccountsCodeForAwardAccountLookup) {
        this.chartOfAccountsCodeForAwardAccountLookup = chartOfAccountsCodeForAwardAccountLookup;
    }

    /**
     * If the lookup only value hasn't been set yet, return the accountNumber so the value pre-populated on the
     * lookup is consistent with what the user saw on the maintenance doc.
     *
     * @return accountNumberForAwardAccountLookup if populated, otherwise accountNumber
     */
    public String getAccountNumberForAwardAccountLookup() {
        return StringUtils.isNotBlank(accountNumberForAwardAccountLookup)
                ? accountNumberForAwardAccountLookup : accountNumber;
    }

    public void setAccountNumberForAwardAccountLookup(String accountNumberForAwardAccountLookup) {
        this.accountNumberForAwardAccountLookup = accountNumberForAwardAccountLookup;
    }

    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public ContractsAndGrantsBillingAgency getAgency() {
        return agency;
    }

    public void setAgency(ContractsAndGrantsBillingAgency agency) {
        this.agency = agency;
    }

    private AccountsReceivableModuleBillingService getAccountsReceivableModuleBillingService() {
        if (accountsReceivableModuleBillingService == null) {
            accountsReceivableModuleBillingService = SpringContext.getBean(AccountsReceivableModuleBillingService.class);
        }
        return accountsReceivableModuleBillingService;
    }

    protected void setAccountsReceivableModuleBillingService(
            AccountsReceivableModuleBillingService accountsReceivableModuleBillingService) {
        this.accountsReceivableModuleBillingService = accountsReceivableModuleBillingService;
    }

    protected ContractsAndGrantsModuleBillingService getContractsAndGrantsModuleBillingService() {
        if (contractsAndGrantsModuleBillingService == null) {
            contractsAndGrantsModuleBillingService = SpringContext.getBean(
                    ContractsAndGrantsModuleBillingService.class);
        }
        return contractsAndGrantsModuleBillingService;
    }

    protected void setContractsAndGrantsModuleBillingService(
            ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService) {
        this.contractsAndGrantsModuleBillingService = contractsAndGrantsModuleBillingService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PredeterminedBillingSchedule)) {
            return false;
        }
        PredeterminedBillingSchedule that = (PredeterminedBillingSchedule) o;
        return Objects.equals(proposalNumber, that.proposalNumber) &&
                Objects.equals(chartOfAccountsCode, that.chartOfAccountsCode) &&
                Objects.equals(accountNumber, that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proposalNumber, chartOfAccountsCode, accountNumber);
    }

    public static class PredeterminedBillingScheduleBuilder {
        private String proposalNumber;
        private String chartOfAccountsCode;
        private String accountNumber;
        private List<Bill> bills = new LinkedList<>();

        public PredeterminedBillingScheduleBuilder setProposalNumber(String proposalNumber) {
            this.proposalNumber = proposalNumber;
            return this;
        }

        public PredeterminedBillingScheduleBuilder setChartOfAccountsCode(String chartOfAccountsCode) {
            this.chartOfAccountsCode = chartOfAccountsCode;
            return this;
        }

        public PredeterminedBillingScheduleBuilder setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public PredeterminedBillingScheduleBuilder addBill(Bill bill) {
            bills.add(bill);
            return this;
        }

        public PredeterminedBillingSchedule build() {
            validate();
            return new PredeterminedBillingSchedule(proposalNumber, chartOfAccountsCode, accountNumber, bills);
        }

        private void validate() {
            if (StringUtils.isBlank(proposalNumber)) {
                throw new IllegalStateException("Proposal Number is required.");
            }
            if (StringUtils.isBlank(chartOfAccountsCode)) {
                throw new IllegalStateException("Chart of Accounts Code is required.");
            }
            if (StringUtils.isBlank(accountNumber)) {
                throw new IllegalStateException("Account Number is required.");
            }
        }
    }
}
