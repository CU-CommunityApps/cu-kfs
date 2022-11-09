/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.coa.service.impl;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountDelegate;
import org.kuali.kfs.coa.dataaccess.AccountDao;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.util.KimCommonUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.SystemGroupParameterNames;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.springframework.cache.annotation.Cacheable;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* Cornell Customization: backport redis fix on FINP-8169*/
public class AccountServiceImpl implements AccountService {
    private static final Logger LOG = LogManager.getLogger();

    protected ParameterService parameterService;
    protected AccountDao accountDao;
    protected DateTimeService dateTimeService;
    protected DocumentTypeService documentTypeService;
    protected BusinessObjectService businessObjectService;

    /**
     * Retrieves an Account object based on primary key.
     *
     * @param chartOfAccountsCode Chart of Accounts Code
     * @param accountNumber       Account Number
     * @return Account
     */
    @Override
    @Cacheable(cacheNames = Account.CACHE_NAME, key = "'{" + Account.CACHE_NAME + "}'+#p0+'-'+#p1")
    public Account getByPrimaryId(String chartOfAccountsCode, String accountNumber) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieving account by primaryId (" + chartOfAccountsCode + "," + accountNumber + ")");
        }
        Map<String, Object> keys = new HashMap<>(2);
        keys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        keys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        Account account = businessObjectService.findByPrimaryKey(Account.class, keys);

        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieved account by primaryId (" + chartOfAccountsCode + "," + accountNumber + "): " + account);
        }
        return account;
    }

    /**
     * Method is used by KualiAccountAttribute to enable caching of accounts for routing.
     *
     * @see org.kuali.kfs.coa.service.impl.AccountServiceImpl#getByPrimaryId(java.lang.String, java.lang.String)
     */
    @Override
    @Cacheable(value = Account.CACHE_NAME, key = "'{" + Account.CACHE_NAME + "}'+#p0+'-'+#p1")
    public Account getByPrimaryIdWithCaching(String chartOfAccountsCode, String accountNumber) {
        Account account = getByPrimaryId(chartOfAccountsCode, accountNumber);
        if (account != null) {
            // force loading of chart reference object
            account.getChartOfAccounts().getChartOfAccountsCode();
        }
        return account;
    }

    @Override
    @Cacheable(value = Account.CACHE_NAME, key = "'ResponsibleForAccounts'+#p0.principalId")
    public List getAccountsThatUserIsResponsibleFor(Person person) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieving accountsResponsible list for user " + person.getName());
        }

        // gets the list of accounts that the user is the Fiscal Officer of
        List accountList = accountDao.getAccountsThatUserIsResponsibleFor(person, dateTimeService.getCurrentDate());
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieved accountsResponsible list for user " + person.getName());
        }
        return accountList;
    }

    @Override
    @Cacheable(value = Account.CACHE_NAME,
            key = "'ResponsibilityOnAccount'+#p0.principalId+'-'+#p1.chartOfAccountsCode+'-'+#p1.accountNumber")
    public boolean hasResponsibilityOnAccount(Person kualiUser, Account account) {
        return accountDao.determineUserResponsibilityOnAccount(kualiUser, account, dateTimeService.getCurrentSqlDate());
    }

    @Override
    public AccountDelegate getPrimaryDelegationByExample(AccountDelegate delegateExample, String totalDollarAmount) {
        Date currentSqlDate = dateTimeService.getCurrentSqlDate();
        List<AccountDelegate> primaryDelegations = filterAccountDelegates(delegateExample,
                accountDao.getPrimaryDelegationByExample(delegateExample, currentSqlDate, totalDollarAmount));
        if (primaryDelegations.isEmpty()) {
            return null;
        }
        for (AccountDelegate delegate : primaryDelegations) {
            if (!KFSConstants.ROOT_DOCUMENT_TYPE.equals(delegate.getFinancialDocumentTypeCode())) {
                return delegate;
            }
        }
        return primaryDelegations.iterator().next();
    }

    @Override
    public List getSecondaryDelegationsByExample(AccountDelegate delegateExample, String totalDollarAmount) {
        Date currentSqlDate = dateTimeService.getCurrentSqlDate();
        List secondaryDelegations = accountDao.getSecondaryDelegationsByExample(delegateExample, currentSqlDate,
                totalDollarAmount);
        return filterAccountDelegates(delegateExample, secondaryDelegations);
    }

    /**
     * This method filters account delegates by
     * 1) performing an exact match on the document type name of delegateExample
     * 2) if no match is found for 1), then by performing an exact match on
     * the closest parent document type name of delegateExample document type name.
     *
     * @param delegateExample
     * @param accountDelegatesToFilterFrom
     * @return
     */
    protected List<AccountDelegate> filterAccountDelegates(AccountDelegate delegateExample,
            List<AccountDelegate> accountDelegatesToFilterFrom) {
        String documentTypeName = delegateExample.getFinancialDocumentTypeCode();
        List<AccountDelegate> filteredAccountDelegates = filterAccountDelegates(accountDelegatesToFilterFrom,
                documentTypeName);
        if (StringUtils.isNotBlank(documentTypeName) && filteredAccountDelegates.size() == 0) {
            Set<String> potentialParentDocumentTypeNames = getPotentialParentDocumentTypeNames(accountDelegatesToFilterFrom);
            String closestParentDocumentTypeName = KimCommonUtils
                    .getClosestParentDocumentTypeName(documentTypeService.getDocumentTypeByName(documentTypeName),
                            potentialParentDocumentTypeNames);
            filteredAccountDelegates = filterAccountDelegates(accountDelegatesToFilterFrom,
                    closestParentDocumentTypeName);
        }
        return filteredAccountDelegates;
    }

    /**
     * This method filters account delegates by performing an exact match on the document type name passed in.
     *
     * @param delegations
     * @param documentTypeNameToFilterOn
     * @return
     */
    protected List<AccountDelegate> filterAccountDelegates(List<AccountDelegate> delegations,
            String documentTypeNameToFilterOn) {
        List<AccountDelegate> filteredSecondaryDelegations = new ArrayList<>();
        for (Object delegateObject : delegations) {
            AccountDelegate delegate = (AccountDelegate) delegateObject;
            if (StringUtils.equals(delegate.getFinancialDocumentTypeCode(), documentTypeNameToFilterOn)) {
                filteredSecondaryDelegations.add(delegate);
            }
        }
        return filteredSecondaryDelegations;
    }

    /**
     * This method gets a list of potential parent document type names by collecting the unique doc type names from
     * the list of account delegations
     *
     * @param delegations
     * @return
     */
    protected Set<String> getPotentialParentDocumentTypeNames(List<AccountDelegate> delegations) {
        AccountDelegate delegate;
        Set<String> potentialParentDocumentTypeNames = new HashSet<>();
        for (Object delegateObject : delegations) {
            delegate = (AccountDelegate) delegateObject;
            if (!potentialParentDocumentTypeNames.contains(delegate.getFinancialDocumentTypeCode())) {
                potentialParentDocumentTypeNames.add(delegate.getFinancialDocumentTypeCode());
            }
        }
        return potentialParentDocumentTypeNames;
    }

    /**
     * get all accounts in the system. This is needed by a sufficient funds rebuilder job
     *
     * @return iterator of all accounts
     */
    @Override
    public Iterator getAllAccounts() {
        LOG.debug("getAllAccounts() started");

        Iterator accountIter = accountDao.getAllAccounts();
        // FIXME: this loads all accounts into memory - could blow server
        return IteratorUtils.toList(accountIter).iterator();
    }

    @Override
    public Iterator<Account> getActiveAccountsForAccountSupervisor(String principalId) {
        return accountDao.getActiveAccountsForAccountSupervisor(principalId, dateTimeService.getCurrentSqlDate());
    }

    @Override
    public Iterator<Account> getActiveAccountsForFiscalOfficer(String principalId) {
        return accountDao.getActiveAccountsForFiscalOfficer(principalId, dateTimeService.getCurrentSqlDate());
    }

    @Override
    public Iterator<Account> getExpiredAccountsForAccountSupervisor(String principalId) {
        return accountDao.getExpiredAccountsForAccountSupervisor(principalId, dateTimeService.getCurrentSqlDate());
    }

    @Override
    public Iterator<Account> getExpiredAccountsForFiscalOfficer(String principalId) {
        return accountDao.getExpiredAccountsForFiscalOfficer(principalId, dateTimeService.getCurrentSqlDate());
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormAccountManager(String principalId) {
        return accountDao.isPrincipalInAnyWayShapeOrFormAccountManager(principalId);
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormAccountSupervisor(String principalId) {
        return accountDao.isPrincipalInAnyWayShapeOrFormAccountSupervisor(principalId);
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormFiscalOfficer(String principalId) {
        return accountDao.isPrincipalInAnyWayShapeOrFormFiscalOfficer(principalId);
    }

    @Override
    @Cacheable(value = Account.CACHE_NAME, key = "'AccountsForAccountNumber'+#p0")
    public Collection<Account> getAccountsForAccountNumber(String accountNumber) {
        return accountDao.getAccountsForAccountNumber(accountNumber);
    }

    @Override
    public String getDefaultLaborBenefitRateCategoryCodeForAccountType(String accountTypeCode) {
        String benefitRateCategory = parameterService.getSubParameterValueAsString(Account.class,
                COAParameterConstants.ACCOUNT_TYPE_BENEFIT_RATE, accountTypeCode);
        if (StringUtils.isBlank(benefitRateCategory)) {
            benefitRateCategory = parameterService.getParameterValueAsString(Account.class,
                    COAParameterConstants.BENEFIT_RATE);
        }
        return StringUtils.trimToEmpty(benefitRateCategory);
    }

    @Override
    public Boolean isFridgeBenefitCalculationEnable() {
        Boolean isFringeBenefitCalcEnable = null;

        //make sure the parameter exists
        if (parameterService.parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                KFSParameterKeyConstants.LdParameterConstants.ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_IND)) {
            //check the system param to see if the labor benefit rate category should be editable
            isFringeBenefitCalcEnable = parameterService.getParameterValueAsBoolean(
                    KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                    KFSParameterKeyConstants.LdParameterConstants.ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_IND);
            LOG.debug("System Parameter retrieved: " + isFringeBenefitCalcEnable);
        }

        return org.apache.commons.lang3.ObjectUtils.defaultIfNull(isFringeBenefitCalcEnable, false);
    }

    @Override
    @Cacheable(value = Account.CACHE_NAME, key = "'UniqueAccountForAccountNumber'+#p0")
    public Account getUniqueAccountForAccountNumber(String accountNumber) {
        Iterator<Account> accounts = accountDao.getAccountsForAccountNumber(accountNumber).iterator();
        Account account = null;
        // there should be only one account in the collection
        if (accounts.hasNext()) {
            account = accounts.next();
        }
        return account;
    }

    @Override
    public boolean accountsCanCrossCharts() {
        return parameterService.getParameterValueAsBoolean(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                SystemGroupParameterNames.ACCOUNTS_CAN_CROSS_CHARTS_IND);
    }

    @Override
    public void populateAccountingLineChartIfNeeded(AccountingLine line) {
        if (!accountsCanCrossCharts() /* && line.getChartOfAccountsCode() == null */) {
            Account account = getUniqueAccountForAccountNumber(line.getAccountNumber());
            if (ObjectUtils.isNotNull(account)) {
                line.setChartOfAccountsCode(account.getChartOfAccountsCode());
            }
        }
    }

    /**
     * @param account
     * @return an unexpired continuation account for the given account, or, if one cannot be found, null
     */
    @Override
    public Account getUnexpiredContinuationAccountOrNull(Account account) {
        int count = 0;
        // prevents infinite loops
        while (count++ < 10) {
            String continuationChartCode = account.getContinuationFinChrtOfAcctCd();
            String continuationAccountNumber = account.getContinuationAccountNumber();

            if (StringUtils.isBlank(continuationChartCode) || StringUtils.isBlank(continuationAccountNumber)) {
                return null;
            }
            account = getByPrimaryId(continuationChartCode, continuationAccountNumber);
            if (ObjectUtils.isNull(account)) {
                return null;
            }
            if (account.isActive() && !account.isExpired()) {
                return account;
            }
        }
        return null;
    }

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
