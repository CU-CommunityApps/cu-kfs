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
package org.kuali.kfs.coa.businessobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.cornell.kfs.coa.service.CuAccountService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.service.SubFundGroupService;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.gl.businessobject.SufficientFundRebuild;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.CFDA;
import org.kuali.kfs.module.cg.service.ContractsAndGrantsService;
import org.kuali.kfs.module.ld.businessobject.LaborBenefitRateCategory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.businessobject.PostalCode;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.sys.businessobject.serialization.PersistableBusinessObjectSerializer;
import org.kuali.kfs.sys.context.SpringContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Account extends PersistableBusinessObjectBase implements AccountIntf, MutableInactivatable {

    private static final Logger LOG = LogManager.getLogger();

    public static final String CACHE_NAME = "Account";

    protected String chartOfAccountsCode;
    protected String accountNumber;
    protected String accountName;
    protected boolean accountsFringesBnftIndicator;
    protected Date accountRestrictedStatusDate;
    protected String accountCityName;
    protected String accountStateCode;
    protected String accountStreetAddress;
    protected String accountZipCode;
    protected String accountCountryCode;
    protected Date accountCreateDate;
    protected Date accountEffectiveDate;
    protected Date accountExpirationDate;
    protected String acctIndirectCostRcvyTypeCd;
    protected String acctCustomIndCstRcvyExclCd;
    protected String financialIcrSeriesIdentifier;
    protected boolean accountInFinancialProcessingIndicator;
    protected String budgetRecordingLevelCode;
    protected String accountSufficientFundsCode;
    protected boolean pendingAcctSufficientFundsIndicator;
    protected boolean financialObjectivePrsctrlIndicator;
    protected String accountCfdaNumber;
    protected boolean accountOffCampusIndicator;
    protected boolean active;
    protected String sourceOfFundsTypeCode;

    protected String accountFiscalOfficerSystemIdentifier;
    protected String accountFiscalOfficerUserPrincipalName;
    protected String accountsSupervisorySystemsIdentifier;
    protected String accountSupervisoryUserPrincipalName;
    protected String accountManagerSystemIdentifier;
    protected String accountManagerUserPrincipalName;
    protected String organizationCode;
    protected String accountTypeCode;
    protected String accountPhysicalCampusCode;
    protected String subFundGroupCode;
    protected String financialHigherEdFunctionCd;
    protected String accountRestrictedStatusCode;
    protected String reportsToChartOfAccountsCode;
    protected String reportsToAccountNumber;
    protected String continuationFinChrtOfAcctCd;
    protected String continuationAccountNumber;
    protected String endowmentIncomeAcctFinCoaCd;
    protected String endowmentIncomeAccountNumber;
    protected String contractControlFinCoaCode;
    protected String contractControlAccountNumber;
    protected String incomeStreamFinancialCoaCode;
    protected String incomeStreamAccountNumber;
    protected Integer contractsAndGrantsAccountResponsibilityId;

    protected Chart chartOfAccounts;
    protected Chart endowmentIncomeChartOfAccounts;
    protected Organization organization;
    protected AccountType accountType;
    protected Campus accountPhysicalCampus;
    protected State accountState;
    protected SubFundGroup subFundGroup;
    protected HigherEducationFunction financialHigherEdFunction;
    protected RestrictedStatus accountRestrictedStatus;
    protected Account reportsToAccount;
    protected Account continuationAccount;
    protected Account endowmentIncomeAccount;
    protected Account contractControlAccount;
    protected Account incomeStreamAccount;
    protected IndirectCostRecoveryType acctIndirectCostRcvyType;
    protected Person accountFiscalOfficerUser;
    protected Person accountSupervisoryUser;
    protected Person accountManagerUser;
    protected PostalCode postalZipCode;
    protected BudgetRecordingLevel budgetRecordingLevel;
    protected SufficientFundsCode sufficientFundsCode;
    protected CFDA cfda;
    protected SourceOfFunds sourceOfFunds;

    protected Chart fringeBenefitsChartOfAccount;
    protected Chart continuationChartOfAccount;
    protected Chart incomeStreamChartOfAccounts;
    protected Chart contractControlChartOfAccounts;

    // Several kinds of Dummy Attributes for dividing sections on Inquiry page
    protected String accountResponsibilitySectionBlank;
    protected String accountResponsibilitySection;
    protected String contractsAndGrantsSectionBlank;
    protected String contractsAndGrantsSection;
    protected String guidelinesAndPurposeSectionBlank;
    protected String guidelinesAndPurposeSection;
    protected String accountDescriptionSectionBlank;
    protected String accountDescriptionSection;

    protected Boolean forContractsAndGrants;

    protected AccountGuideline accountGuideline;
    protected AccountDescription accountDescription;

    protected List subAccounts;
    protected List<AwardAccount> awards;
    protected List<IndirectCostRecoveryAccount> indirectCostRecoveryAccounts;
    //added for the employee labor benefit calculation
    protected String laborBenefitRateCategoryCode;
    protected LaborBenefitRateCategory laborBenefitRateCategory;

    @JsonIgnore
    private transient PersonService personService;

    public Account() {
        // assume active is true until set otherwise
        active = true;
        indirectCostRecoveryAccounts = new ArrayList<>();
        // we need country code to have a value for OJB foreign keys to other location types but it isn't exposed on
        // docs so it never gets set. setting a default value on the column in the db did not do what we want b/c
        // the ojb insert explicitly specifies every column it knows about. this will work for now.
        accountCountryCode = KFSConstants.COUNTRY_CODE_UNITED_STATES;
    }

    /**
     * This method gathers all SubAccounts related to this account if the account is marked as closed to deactivate
     */
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        // Retrieve all the existing sub accounts for this
        final List<SubAccount> bosToDeactivate = new ArrayList<>();
        if (!isActive()) {
            final Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, getChartOfAccountsCode());
            fieldValues.put(KFSPropertyConstants.ACCOUNT_NUMBER, getAccountNumber());
            fieldValues.put(KFSPropertyConstants.ACTIVE, true);
            final Collection<SubAccount> existingSubAccounts = SpringContext.getBean(BusinessObjectService.class)
                    .findMatching(SubAccount.class, fieldValues);
            bosToDeactivate.addAll(existingSubAccounts);
        }
        // mark all the sub accounts as inactive
        for (final SubAccount subAccount : bosToDeactivate) {
            subAccount.setActive(false);
        }
        return new ArrayList<>(bosToDeactivate);
    }

    @Override
    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public boolean isAccountsFringesBnftIndicator() {
        return accountsFringesBnftIndicator;
    }

    @Override
    public void setAccountsFringesBnftIndicator(final boolean _AccountsFringesBnftIndicator_) {
        accountsFringesBnftIndicator = _AccountsFringesBnftIndicator_;
    }

    @Override
    public Date getAccountRestrictedStatusDate() {
        return accountRestrictedStatusDate;
    }

    @Override
    public void setAccountRestrictedStatusDate(final Date accountRestrictedStatusDate) {
        this.accountRestrictedStatusDate = accountRestrictedStatusDate;
    }

    @Override
    public String getAccountCityName() {
        return accountCityName;
    }

    @Override
    public void setAccountCityName(final String accountCityName) {
        this.accountCityName = accountCityName;
    }

    @Override
    public String getAccountStateCode() {
        return accountStateCode;
    }

    @Override
    public void setAccountStateCode(final String accountStateCode) {
        this.accountStateCode = accountStateCode;
    }

    @Override
    public String getAccountStreetAddress() {
        return accountStreetAddress;
    }

    @Override
    public void setAccountStreetAddress(final String accountStreetAddress) {
        this.accountStreetAddress = accountStreetAddress;
    }

    @Override
    public String getAccountZipCode() {
        return accountZipCode;
    }

    @Override
    public void setAccountZipCode(final String accountZipCode) {
        this.accountZipCode = accountZipCode;
    }

    @Override
    public Date getAccountCreateDate() {
        return accountCreateDate;
    }

    @Override
    public void setAccountCreateDate(final Date accountCreateDate) {
        this.accountCreateDate = accountCreateDate;
    }

    @Override
    public Date getAccountEffectiveDate() {
        return accountEffectiveDate;
    }

    @Override
    public void setAccountEffectiveDate(final Date accountEffectiveDate) {
        this.accountEffectiveDate = accountEffectiveDate;
    }

    @Override
    public Date getAccountExpirationDate() {
        return accountExpirationDate;
    }

    @Override
    public void setAccountExpirationDate(final Date accountExpirationDate) {
        this.accountExpirationDate = accountExpirationDate;
    }

    /**
     * This method determines whether the account is expired or not. Note that if Expiration Date is the same as
     * today, then this will return false. It will only return true if the account expiration date is one day earlier
     * than today or earlier. Note that this logic ignores all time components when doing the comparison. It only
     * does the before/after comparison based on date values, not time-values.
     *
     * @return true or false based on the logic outlined above
     */
    @Override
    public boolean isExpired() {
        LOG.debug("entering isExpired()");
        // don't even bother trying to test if the accountExpirationDate is null
        if (accountExpirationDate == null) {
            return false;
        }

        return isExpired(getDateTimeService().getLocalDateNow());
    }

    /**
     * This method determines whether the account is expired or not. Note that if Expiration Date is the same date as
     * testDate, then this will return false. It will only return true if the account expiration date is one day
     * earlier than testDate or earlier. Note that this logic ignores all time components when doing the comparison.
     * It only does the before/after comparison based on date values, not time-values.
     *
     * @param testDate Calendar instance with the date to test the Account's Expiration Date against. This is most
     *                 commonly set to today's date.
     * @return true or false based on the logic outlined above
     */
    @Override
    public boolean isExpired(final LocalDate testDate) {
        LOG.debug("entering isExpired({})", testDate);

        // don't even bother trying to test if the accountExpirationDate is null
        if (accountExpirationDate == null) {
            return false;
        }

        // get a reference to the Account Expiration
        final LocalDate acctDate = getDateTimeService().getLocalDate(accountExpirationDate);

        // if the Account Expiration Date is before the testDate
        return acctDate.isBefore(testDate);
    }

    /**
     * This method determines whether the account is expired or not. Note that if Expiration Date is the same date as
     * testDate, then this will return false. It will only return true if the account expiration date is one day
     * earlier than testDate or earlier. Note that this logic ignores all time components when doing the comparison.
     * It only does the before/after comparison based on date values, not time-values.
     *
     * @param testDate java.util.Date instance with the date to test the Account's Expiration Date against. This is
     *                 most commonly set to today's date.
     * @return true or false based on the logic outlined above
     */
    @Override
    public boolean isExpired(final Date testDate) {
        // don't even bother trying to test if the accountExpirationDate is null
        if (accountExpirationDate == null) {
            return false;
        }

        final LocalDate acctDate = getDateTimeService().getLocalDate(testDate);
        return isExpired(acctDate);
    }

    @Override
    public String getAcctIndirectCostRcvyTypeCd() {
        return acctIndirectCostRcvyTypeCd;
    }

    @Override
    public void setAcctIndirectCostRcvyTypeCd(final String acctIndirectCostRcvyTypeCd) {
        this.acctIndirectCostRcvyTypeCd = acctIndirectCostRcvyTypeCd;
    }

    @Override
    public String getAcctCustomIndCstRcvyExclCd() {
        return acctCustomIndCstRcvyExclCd;
    }

    @Override
    public void setAcctCustomIndCstRcvyExclCd(final String acctCustomIndCstRcvyExclCd) {
        this.acctCustomIndCstRcvyExclCd = acctCustomIndCstRcvyExclCd;
    }

    @Override
    public String getFinancialIcrSeriesIdentifier() {
        return financialIcrSeriesIdentifier;
    }

    @Override
    public void setFinancialIcrSeriesIdentifier(final String financialIcrSeriesIdentifier) {
        this.financialIcrSeriesIdentifier = financialIcrSeriesIdentifier;
    }

    @Override
    public boolean getAccountInFinancialProcessingIndicator() {
        return accountInFinancialProcessingIndicator;
    }

    @Override
    public void setAccountInFinancialProcessingIndicator(final boolean accountInFinancialProcessingIndicator) {
        this.accountInFinancialProcessingIndicator = accountInFinancialProcessingIndicator;
    }

    @Override
    public String getBudgetRecordingLevelCode() {
        return budgetRecordingLevelCode;
    }

    @Override
    public void setBudgetRecordingLevelCode(final String budgetRecordingLevelCode) {
        this.budgetRecordingLevelCode = budgetRecordingLevelCode;
    }

    @Override
    public String getAccountSufficientFundsCode() {
        return accountSufficientFundsCode;
    }

    @Override
    public void setAccountSufficientFundsCode(final String accountSufficientFundsCode) {
        this.accountSufficientFundsCode = accountSufficientFundsCode;
    }

    @Override
    public boolean isPendingAcctSufficientFundsIndicator() {
        return pendingAcctSufficientFundsIndicator;
    }

    @Override
    public void setPendingAcctSufficientFundsIndicator(final boolean pendingAcctSufficientFundsIndicator) {
        this.pendingAcctSufficientFundsIndicator = pendingAcctSufficientFundsIndicator;
    }

    @Override
    public boolean isFinancialObjectivePrsctrlIndicator() {
        return financialObjectivePrsctrlIndicator;
    }

    @Override
    public void setFinancialObjectivePrsctrlIndicator(final boolean _FinancialObjectivePrsctrlIndicator_) {
        financialObjectivePrsctrlIndicator = _FinancialObjectivePrsctrlIndicator_;
    }

    @Override
    public String getAccountCfdaNumber() {
        return accountCfdaNumber;
    }

    @Override
    public void setAccountCfdaNumber(final String accountCfdaNumber) {
        this.accountCfdaNumber = accountCfdaNumber;
    }

    public CFDA getCfda() {
        return cfda;
    }

    public void setCfda(final CFDA cfda) {
        this.cfda = cfda;
    }

    public List<AwardAccount> getAwards() {
        return awards;
    }

    public void setAwards(final List<AwardAccount> awards) {
        this.awards = awards;
    }

    @Override
    public List<IndirectCostRecoveryAccount> getIndirectCostRecoveryAccounts() {
        return indirectCostRecoveryAccounts;
    }

    public List<IndirectCostRecoveryAccount> getActiveIndirectCostRecoveryAccounts() {
        final List<IndirectCostRecoveryAccount> activeList = new ArrayList<>();
        for (final IndirectCostRecoveryAccount icr : getIndirectCostRecoveryAccounts()) {
            if (icr.isActive()) {
                activeList.add(IndirectCostRecoveryAccount.copyICRAccount(icr));
            }
        }
        return activeList;
    }

    @Override
    public void setIndirectCostRecoveryAccounts(
            final List<? extends IndirectCostRecoveryAccount> indirectCostRecoveryAccounts) {
        final List<IndirectCostRecoveryAccount> accountIcrList = new ArrayList<>(indirectCostRecoveryAccounts);
        this.indirectCostRecoveryAccounts = accountIcrList;
    }

    @Override
    public boolean isAccountOffCampusIndicator() {
        return accountOffCampusIndicator;
    }

    @Override
    public void setAccountOffCampusIndicator(final boolean accountOffCampusIndicator) {
        this.accountOffCampusIndicator = accountOffCampusIndicator;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public boolean isClosed() {
        return !active;
    }

    public void setClosed(final boolean closed) {
        active = !closed;
    }

    @Override
    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    @Deprecated
    @Override
    public void setChartOfAccounts(final Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Deprecated
    @Override
    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    @Override
    public AccountType getAccountType() {
        return accountType;
    }

    @Deprecated
    @Override
    public void setAccountType(final AccountType accountType) {
        this.accountType = accountType;
    }

    @Override
    public Campus getAccountPhysicalCampus() {
        return accountPhysicalCampus;
    }

    @Deprecated
    @Override
    public void setAccountPhysicalCampus(final Campus accountPhysicalCampus) {
        this.accountPhysicalCampus = accountPhysicalCampus;
    }

    @Override
    public State getAccountState() {
        return accountState;
    }

    @Deprecated
    @Override
    public void setAccountState(final State state) {
        accountState = state;
    }

    @Override
    public SubFundGroup getSubFundGroup() {
        return subFundGroup;
    }

    @Deprecated
    @Override
    public void setSubFundGroup(final SubFundGroup subFundGroup) {
        this.subFundGroup = subFundGroup;
    }

    @Override
    public HigherEducationFunction getFinancialHigherEdFunction() {
        return financialHigherEdFunction;
    }

    @Deprecated
    @Override
    public void setFinancialHigherEdFunction(final HigherEducationFunction financialHigherEdFunction) {
        this.financialHigherEdFunction = financialHigherEdFunction;
    }

    @Override
    public RestrictedStatus getAccountRestrictedStatus() {
        return accountRestrictedStatus;
    }

    @Deprecated
    @Override
    public void setAccountRestrictedStatus(final RestrictedStatus accountRestrictedStatus) {
        this.accountRestrictedStatus = accountRestrictedStatus;
    }

    @Override
    @JsonSerialize(using = PersistableBusinessObjectSerializer.class)
    public Account getReportsToAccount() {
        return reportsToAccount;
    }

    @Deprecated
    @Override
    public void setReportsToAccount(final Account reportsToAccount) {
        this.reportsToAccount = reportsToAccount;
    }

    @Override
    @JsonSerialize(using = PersistableBusinessObjectSerializer.class)
    public Account getEndowmentIncomeAccount() {
        return endowmentIncomeAccount;
    }

    @Deprecated
    @Override
    public void setEndowmentIncomeAccount(final Account endowmentIncomeAccount) {
        this.endowmentIncomeAccount = endowmentIncomeAccount;
    }

    @Override
    @JsonSerialize(using = PersistableBusinessObjectSerializer.class)
    public Account getContractControlAccount() {
        return contractControlAccount;
    }

    @Deprecated
    @Override
    public void setContractControlAccount(final Account contractControlAccount) {
        this.contractControlAccount = contractControlAccount;
    }

    @Override
    @JsonSerialize(using = PersistableBusinessObjectSerializer.class)
    public Account getIncomeStreamAccount() {
        return incomeStreamAccount;
    }

    @Deprecated
    @Override
    public void setIncomeStreamAccount(final Account incomeStreamAccount) {
        this.incomeStreamAccount = incomeStreamAccount;
    }

    @Override
    public Person getAccountFiscalOfficerUser() {
        return getPersonService().updatePrincipalNameIfNecessary(
                accountFiscalOfficerSystemIdentifier,
                accountFiscalOfficerUserPrincipalName,
                accountFiscalOfficerUser
        );
    }

    @Deprecated
    @Override
    public void setAccountFiscalOfficerUser(final Person accountFiscalOfficerUser) {
        this.accountFiscalOfficerUser = accountFiscalOfficerUser;
    }

    @Override
    public Person getAccountManagerUser() {
        return getPersonService().updatePrincipalNameIfNecessary(
                accountManagerSystemIdentifier,
                accountManagerUserPrincipalName,
                accountManagerUser
        );
    }

    @Deprecated
    @Override
    public void setAccountManagerUser(final Person accountManagerUser) {
        this.accountManagerUser = accountManagerUser;
    }

    @Override
    public Person getAccountSupervisoryUser() {
        return getPersonService().updatePrincipalNameIfNecessary(
                accountsSupervisorySystemsIdentifier,
                accountSupervisoryUserPrincipalName,
                accountSupervisoryUser
        );
    }

    @Deprecated
    @Override
    public void setAccountSupervisoryUser(final Person accountSupervisoryUser) {
        this.accountSupervisoryUser = accountSupervisoryUser;
    }

    @Override
    public Account getContinuationAccount() {
        return continuationAccount;
    }

    @Deprecated
    @Override
    public void setContinuationAccount(final Account continuationAccount) {
        this.continuationAccount = continuationAccount;
    }

    @Override
    public AccountGuideline getAccountGuideline() {
        return accountGuideline;
    }

    @Override
    public void setAccountGuideline(final AccountGuideline accountGuideline) {
        this.accountGuideline = accountGuideline;
    }

    @Override
    public AccountDescription getAccountDescription() {
        return accountDescription;
    }

    @Override
    public void setAccountDescription(final AccountDescription accountDescription) {
        this.accountDescription = accountDescription;
    }

    @Override
    public List getSubAccounts() {
        return subAccounts;
    }

    @Override
    public void setSubAccounts(final List subAccounts) {
        this.subAccounts = subAccounts;
    }

    @Override
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    @Override
    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    @Override
    public String getAccountFiscalOfficerSystemIdentifier() {
        return accountFiscalOfficerSystemIdentifier;
    }

    public String getAccountFiscalOfficerSystemIdentifierForSearching() {
        return getAccountFiscalOfficerSystemIdentifier();
    }

    @Override
    public void setAccountFiscalOfficerSystemIdentifier(final String accountFiscalOfficerSystemIdentifier) {
        this.accountFiscalOfficerSystemIdentifier = accountFiscalOfficerSystemIdentifier;
    }

    public String getAccountFiscalOfficerUserPrincipalName() {
        return accountFiscalOfficerUserPrincipalName;
    }

    public void setAccountFiscalOfficerUserPrincipalName(final String accountFiscalOfficerUserPrincipalName) {
        this.accountFiscalOfficerUserPrincipalName = accountFiscalOfficerUserPrincipalName;
    }

    @Override
    public String getAccountManagerSystemIdentifier() {
        return accountManagerSystemIdentifier;
    }

    public String getAccountManagerSystemIdentifierForSearching() {
        return getAccountManagerSystemIdentifier();
    }

    @Override
    public void setAccountManagerSystemIdentifier(final String accountManagerSystemIdentifier) {
        this.accountManagerSystemIdentifier = accountManagerSystemIdentifier;
    }

    public String getAccountManagerUserPrincipalName() {
        return accountManagerUserPrincipalName;
    }

    public void setAccountManagerUserPrincipalName(final String accountManagerUserPrincipalName) {
        this.accountManagerUserPrincipalName = accountManagerUserPrincipalName;
    }

    @Override
    public String getAccountPhysicalCampusCode() {
        return accountPhysicalCampusCode;
    }

    @Override
    public void setAccountPhysicalCampusCode(final String accountPhysicalCampusCode) {
        this.accountPhysicalCampusCode = accountPhysicalCampusCode;
    }

    @Override
    public String getAccountRestrictedStatusCode() {
        return accountRestrictedStatusCode;
    }

    @Override
    public void setAccountRestrictedStatusCode(final String accountRestrictedStatusCode) {
        this.accountRestrictedStatusCode = accountRestrictedStatusCode;
    }

    @Override
    public String getAccountsSupervisorySystemsIdentifier() {
        return accountsSupervisorySystemsIdentifier;
    }

    public String getAccountsSupervisorySystemsIdentifierForSearching() {
        return accountsSupervisorySystemsIdentifier;
    }

    @Override
    public void setAccountsSupervisorySystemsIdentifier(final String accountsSupervisorySystemsIdentifier) {
        this.accountsSupervisorySystemsIdentifier = accountsSupervisorySystemsIdentifier;
    }

    public String getAccountSupervisoryUserPrincipalName() {
        return accountSupervisoryUserPrincipalName;
    }

    public void setAccountSupervisoryUserPrincipalName(final String accountSupervisoryUserPrincipalName) {
        this.accountSupervisoryUserPrincipalName = accountSupervisoryUserPrincipalName;
    }

    @Override
    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    @Override
    public void setAccountTypeCode(final String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    @Override
    public String getContinuationAccountNumber() {
        return continuationAccountNumber;
    }

    @Override
    public void setContinuationAccountNumber(final String continuationAccountNumber) {
        this.continuationAccountNumber = continuationAccountNumber;
    }

    @Override
    public String getContinuationFinChrtOfAcctCd() {
        return continuationFinChrtOfAcctCd;
    }

    @Override
    public void setContinuationFinChrtOfAcctCd(final String continuationFinChrtOfAcctCd) {
        this.continuationFinChrtOfAcctCd = continuationFinChrtOfAcctCd;
    }

    @Override
    public String getContractControlAccountNumber() {
        return contractControlAccountNumber;
    }

    @Override
    public void setContractControlAccountNumber(final String contractControlAccountNumber) {
        this.contractControlAccountNumber = contractControlAccountNumber;
    }

    @Override
    public String getContractControlFinCoaCode() {
        return contractControlFinCoaCode;
    }

    @Override
    public void setContractControlFinCoaCode(final String contractControlFinCoaCode) {
        this.contractControlFinCoaCode = contractControlFinCoaCode;
    }

    @Override
    public String getEndowmentIncomeAccountNumber() {
        return endowmentIncomeAccountNumber;
    }

    @Override
    public void setEndowmentIncomeAccountNumber(final String endowmentIncomeAccountNumber) {
        this.endowmentIncomeAccountNumber = endowmentIncomeAccountNumber;
    }

    @Override
    public String getEndowmentIncomeAcctFinCoaCd() {
        return endowmentIncomeAcctFinCoaCd;
    }

    @Override
    public void setEndowmentIncomeAcctFinCoaCd(final String endowmentIncomeAcctFinCoaCd) {
        this.endowmentIncomeAcctFinCoaCd = endowmentIncomeAcctFinCoaCd;
    }

    @Override
    public String getFinancialHigherEdFunctionCd() {
        return financialHigherEdFunctionCd;
    }

    @Override
    public void setFinancialHigherEdFunctionCd(final String financialHigherEdFunctionCd) {
        this.financialHigherEdFunctionCd = financialHigherEdFunctionCd;
    }

    @Override
    public String getIncomeStreamAccountNumber() {
        return incomeStreamAccountNumber;
    }

    @Override
    public void setIncomeStreamAccountNumber(final String incomeStreamAccountNumber) {
        this.incomeStreamAccountNumber = incomeStreamAccountNumber;
    }

    @Override
    public String getIncomeStreamFinancialCoaCode() {
        return incomeStreamFinancialCoaCode;
    }

    @Override
    public void setIncomeStreamFinancialCoaCode(final String incomeStreamFinancialCoaCode) {
        this.incomeStreamFinancialCoaCode = incomeStreamFinancialCoaCode;
    }

    @Override
    public String getOrganizationCode() {
        return organizationCode;
    }

    @Override
    public void setOrganizationCode(final String organizationCode) {
        this.organizationCode = organizationCode;
    }

    @Override
    public String getReportsToAccountNumber() {
        return reportsToAccountNumber;
    }

    @Override
    public void setReportsToAccountNumber(final String reportsToAccountNumber) {
        this.reportsToAccountNumber = reportsToAccountNumber;
    }

    @Override
    public String getReportsToChartOfAccountsCode() {
        return reportsToChartOfAccountsCode;
    }

    @Override
    public void setReportsToChartOfAccountsCode(final String reportsToChartOfAccountsCode) {
        this.reportsToChartOfAccountsCode = reportsToChartOfAccountsCode;
    }

    @Override
    public String getSubFundGroupCode() {
        return subFundGroupCode;
    }

    @Override
    public void setSubFundGroupCode(final String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
        forContractsAndGrants = null;
    }

    @Override
    public PostalCode getPostalZipCode() {
        return postalZipCode;
    }

    @Override
    public void setPostalZipCode(final PostalCode postalZipCode) {
        this.postalZipCode = postalZipCode;
    }

    @Override
    public BudgetRecordingLevel getBudgetRecordingLevel() {
        return budgetRecordingLevel;
    }

    @Override
    public void setBudgetRecordingLevel(final BudgetRecordingLevel budgetRecordingLevel) {
        this.budgetRecordingLevel = budgetRecordingLevel;
    }

    @Override
    public SufficientFundsCode getSufficientFundsCode() {
        return sufficientFundsCode;
    }

    @Override
    public void setSufficientFundsCode(final SufficientFundsCode sufficientFundsCode) {
        this.sufficientFundsCode = sufficientFundsCode;
    }

    public IndirectCostRecoveryType getAcctIndirectCostRcvyType() {
        return acctIndirectCostRcvyType;
    }

    public void setAcctIndirectCostRcvyType(final IndirectCostRecoveryType acctIndirectCostRcvyType) {
        this.acctIndirectCostRcvyType = acctIndirectCostRcvyType;
    }

    /**
     * Implementing equals since I need contains to behave reasonably in a hashed data structure.
     */
    @Override
    public boolean equals(final Object obj) {
        boolean equal = false;

        if (obj != null) {
            if (getClass().equals(obj.getClass())) {
                final Account other = (Account) obj;

                if (StringUtils.equals(getChartOfAccountsCode(), other.getChartOfAccountsCode())) {
                    if (StringUtils.equals(getAccountNumber(), other.getAccountNumber())) {
                        equal = true;
                    }
                }
            }
        }

        return equal;
    }

    /**
     * Calculates hashCode based on current values of chartOfAccountsCode and accountNumber fields. Somewhat
     * dangerous, since both of those fields are mutable, but I don't expect people to be editing those values
     * directly for Accounts stored in hashed data structures.
     */
    @Override
    public int hashCode() {
        final String hashString = getChartOfAccountsCode() + "|" + getAccountNumber();

        return hashString.hashCode();
    }

    /**
     * Convenience method to make the primitive account fields from this Account easier to compare to the account
     * fields from another Account or an AccountingLine
     *
     * @return String representing the account associated with this Accounting
     */
    @Override
    public String getAccountKey() {
        return getChartOfAccountsCode() + ":" + getAccountNumber();
    }

    @Override
    public String getAccountResponsibilitySection() {
        return accountResponsibilitySection;
    }

    @Override
    public void setAccountResponsibilitySection(final String accountResponsibilitySection) {
        this.accountResponsibilitySection = accountResponsibilitySection;
    }

    @Override
    public String getContractsAndGrantsSection() {
        return contractsAndGrantsSection;
    }

    @Override
    public void setContractsAndGrantsSection(final String contractsAndGrantsSection) {
        this.contractsAndGrantsSection = contractsAndGrantsSection;
    }

    @Override
    public String getAccountDescriptionSection() {
        return accountDescriptionSection;
    }

    @Override
    public void setAccountDescriptionSection(final String accountDescriptionSection) {
        this.accountDescriptionSection = accountDescriptionSection;
    }

    @Override
    public String getGuidelinesAndPurposeSection() {
        return guidelinesAndPurposeSection;
    }

    @Override
    public void setGuidelinesAndPurposeSection(final String guidelinesAndPurposeSection) {
        this.guidelinesAndPurposeSection = guidelinesAndPurposeSection;
    }

    @Override
    public String getAccountResponsibilitySectionBlank() {
        return accountResponsibilitySectionBlank;
    }

    @Override
    public String getContractsAndGrantsSectionBlank() {
        return contractsAndGrantsSectionBlank;
    }

    @Override
    public String getAccountDescriptionSectionBlank() {
        return accountDescriptionSectionBlank;
    }

    @Override
    public String getGuidelinesAndPurposeSectionBlank() {
        return guidelinesAndPurposeSectionBlank;
    }

    public Chart getEndowmentIncomeChartOfAccounts() {
        return endowmentIncomeChartOfAccounts;
    }

    public void setEndowmentIncomeChartOfAccounts(final Chart endowmentIncomeChartOfAccounts) {
        this.endowmentIncomeChartOfAccounts = endowmentIncomeChartOfAccounts;
    }

    @Override
    protected void beforeUpdate() {
        super.beforeUpdate();
        try {
            // KULCOA-549: update the sufficient funds table get the current data from the database
            final BusinessObjectService boService = SpringContext.getBean(BusinessObjectService.class);
            final Account originalAcct = (Account) boService.retrieve(this);

            if (originalAcct != null) {
                if (!originalAcct.getSufficientFundsCode().equals(getSufficientFundsCode())
                        || originalAcct.isPendingAcctSufficientFundsIndicator() != isPendingAcctSufficientFundsIndicator()) {
                    final SufficientFundRebuild sfr = new SufficientFundRebuild();
                    sfr.setAccountFinancialObjectTypeCode(SufficientFundRebuild.REBUILD_ACCOUNT);
                    sfr.setChartOfAccountsCode(getChartOfAccountsCode());
                    sfr.setAccountNumberFinancialObjectCode(getAccountNumber());
                    if (boService.retrieve(sfr) == null) {
                        boService.save(sfr);
                    }
                }
            }
        } catch (final Exception ex) {
            LOG.error("Problem updating sufficient funds rebuild table: ", ex);
        }
    }

    @Override
    public boolean isForContractsAndGrants() {
        if (forContractsAndGrants == null) {
            forContractsAndGrants = SpringContext.getBean(SubFundGroupService.class).isForContractsAndGrants(getSubFundGroup());
        }
        return forContractsAndGrants;
    }

    /**
     * determine if the given account is awarded by a federal agency
     *
     * @param federalAgencyTypeCodes the given federal agency type code
     * @return true if the given account is funded by a federal agency or associated with federal pass through indicator;
     *         otherwise false
     */
    public boolean isAwardedByFederalAgency(final Collection<String> federalAgencyTypeCodes) {
        return SpringContext.getBean(ContractsAndGrantsService.class).isAwardedByFederalAgency(
                getChartOfAccountsCode(), getAccountNumber(), federalAgencyTypeCodes);
    }

    public Integer getContractsAndGrantsAccountResponsibilityId() {
        return contractsAndGrantsAccountResponsibilityId;
    }

    public void setContractsAndGrantsAccountResponsibilityId(final Integer contractsAndGrantsAccountResponsibilityId) {
        this.contractsAndGrantsAccountResponsibilityId = contractsAndGrantsAccountResponsibilityId;
    }

    public String getLaborBenefitRateCategoryCode() {
        return laborBenefitRateCategoryCode;
    }

    public void setLaborBenefitRateCategoryCode(final String laborBenefitRateCategoryCode) {
        this.laborBenefitRateCategoryCode = laborBenefitRateCategoryCode;
    }

    public LaborBenefitRateCategory getLaborBenefitRateCategory() {
        return laborBenefitRateCategory;
    }

    public void setLaborBenefitRateCategory(final LaborBenefitRateCategory laborBenefitRateCategory) {
        this.laborBenefitRateCategory = laborBenefitRateCategory;
    }

    public Chart getFringeBenefitsChartOfAccount() {
        return fringeBenefitsChartOfAccount;
    }

    public void setFringeBenefitsChartOfAccount(final Chart fringeBenefitsChartOfAccount) {
        this.fringeBenefitsChartOfAccount = fringeBenefitsChartOfAccount;
    }

    public Chart getContinuationChartOfAccount() {
        return continuationChartOfAccount;
    }

    public void setContinuationChartOfAccount(final Chart continuationChartOfAccount) {
        this.continuationChartOfAccount = continuationChartOfAccount;
    }

    public Chart getIncomeStreamChartOfAccounts() {
        return incomeStreamChartOfAccounts;
    }

    public void setIncomeStreamChartOfAccounts(final Chart incomeStreamChartOfAccounts) {
        this.incomeStreamChartOfAccounts = incomeStreamChartOfAccounts;
    }

    public Chart getContractControlChartOfAccounts() {
        return contractControlChartOfAccounts;
    }

    public void setContractControlChartOfAccounts(final Chart contractControlChartOfAccounts) {
        this.contractControlChartOfAccounts = contractControlChartOfAccounts;
    }

    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        final List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();
        managedLists.add(new ArrayList<>(getIndirectCostRecoveryAccounts()));
        return managedLists;
    }

    public String getAccountCountryCode() {
        return accountCountryCode;
    }

    public void setAccountCountryCode(final String accountCountryCode) {
        this.accountCountryCode = accountCountryCode;
    }

    public String getSourceOfFundsTypeCode() {
        return sourceOfFundsTypeCode;
    }

    public void setSourceOfFundsTypeCode(final String sourceOfFundsTypeCode) {
        this.sourceOfFundsTypeCode = sourceOfFundsTypeCode;
    }

    public SourceOfFunds getSourceOfFunds() {
        return sourceOfFunds;
    }

    public void setSourceOfFunds(final SourceOfFunds sourceOfFunds) {
        this.sourceOfFunds = sourceOfFunds;
    }

    public PersonService getPersonService() {
        if (personService == null) {
            personService = SpringContext.getBean(PersonService.class);
        }
        return personService;
    }

    public List<Note> getBoNotes() {
        CuAccountService accountService = SpringContext.getBean(CuAccountService.class);
        return accountService.getAccountNotes(this);
    }

    public void setBoNotes(List<Note> boNotes) {
    	
    }
}
