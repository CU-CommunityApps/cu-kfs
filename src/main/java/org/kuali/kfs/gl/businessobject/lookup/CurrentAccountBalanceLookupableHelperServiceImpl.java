/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.gl.businessobject.lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.coa.service.AccountService;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.gl.Constant;
import org.kuali.kfs.gl.batch.service.BalanceCalculator;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.lookup.AbstractGeneralLedgerLookupableHelperServiceImpl;
import org.kuali.kfs.gl.businessobject.lookup.BusinessObjectFieldConverter;
import org.kuali.kfs.gl.service.BalanceService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.ObjectUtil;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.gl.businessobject.CurrentAccountBalance;
import org.kuali.kfs.sys.KFSParameterKeyConstants;

import org.kuali.kfs.gl.businessobject.CurrentAccountBalance;
/**
 * AER-0630 - Rq_GL_630 add a simple balance inquiry menu item to KFS main menu
 * An extension of KualiLookupableImpl to support account balance lookups
 */
public class CurrentAccountBalanceLookupableHelperServiceImpl extends AbstractGeneralLedgerLookupableHelperServiceImpl {

    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CurrentAccountBalanceLookupableHelperServiceImpl.class);

    private final static String PRINCIPAL_ID_KEY = KFSPropertyConstants.ACCOUNT + "." + KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_SYSTEM_IDENTIFIER;
    private final static String PRINCIPAL_NAME_KEY = KFSPropertyConstants.ACCOUNT + "." + KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_USER + "." + KFSPropertyConstants.PERSON_USER_ID;
    private final static String ORGANIZATION_FIELD_KEY = KFSPropertyConstants.ACCOUNT + "." + KFSPropertyConstants.ORGANIZATION_CODE;
    private final static String ACCOUNT_NUMBER_FIELD_KEY = KFSPropertyConstants.ACCOUNT + "." + KFSPropertyConstants.ACCOUNT_NUMBER;
    private final static String SUPERVISOR_PRINCIPAL_NAME_KEY = KFSPropertyConstants.ACCOUNT + "." + KFSPropertyConstants.ACCOUNT_SUPERVISORY_USER + "." + KFSPropertyConstants.PERSON_USER_ID;
    private final static String SUPERVISOR_PRINCIPAL_ID_KEY = KFSPropertyConstants.ACCOUNT + ".accountsSupervisorySystemsIdentifier";
    
    private BalanceCalculator postBalance;
    private BalanceService balanceService;
    private PersonService<Person> personService;
    private AccountingPeriodService accountingPeriodService;

    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getInquiryUrl(org.kuali.rice.kns.bo.BusinessObject,
     *      java.lang.String)
     */
    @Override
    public HtmlData getInquiryUrl(BusinessObject bo, String propertyName) {

	if (StringUtils.equals(propertyName, KFSPropertyConstants.SUB_ACCOUNT_NUMBER)) {
	    String subAccountNumber = (String) ObjectUtils.getPropertyValue(bo, propertyName);
	    if (StringUtils.equals(Constant.CONSOLIDATED_SUB_ACCOUNT_NUMBER, subAccountNumber)) {
		return super.getEmptyAnchorHtmlData();
	    }
	}

	return super.getInquiryUrl(bo, propertyName);
    }

    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List getSearchResults(Map fieldValues) {
	setBackLocation((String) fieldValues.get(KFSConstants.BACK_LOCATION));
	setDocFormKey((String) fieldValues.get(KFSConstants.DOC_FORM_KEY));

	// get the pending entry option. This method must be prior to the get
	// search results
	String pendingEntryOption = this.getSelectedPendingEntryOption(fieldValues);

	// test if the consolidation option is selected or not
	boolean isConsolidated = isConsolidationSelected(fieldValues);

	Map<String, String> localFieldValues = this.getLocalFieldValues(fieldValues);

	Collection<CurrentAccountBalance> searchResultsCollection = this.buildCurrentBalanceCollection(localFieldValues, isConsolidated, pendingEntryOption);
	LOG.info("searchResultsCollection.size(): " + searchResultsCollection.size());

	return this.buildSearchResultList(searchResultsCollection, Long.valueOf(searchResultsCollection.size()));
    }

    /**
     * clear up the search criteria
     */
    protected Map<String, String> getLocalFieldValues(Map<String, String> fieldValues) {
	Map<String, String> localFieldValues = new HashMap<String, String>();
	localFieldValues.putAll(fieldValues);

	String principalName = fieldValues.get(PRINCIPAL_NAME_KEY);
	if (StringUtils.isNotBlank(principalName)) {
	    localFieldValues.remove(PRINCIPAL_NAME_KEY);

	    Person person = personService.getPersonByPrincipalName(principalName);
	    if (ObjectUtils.isNotNull(person)) {
		localFieldValues.put(PRINCIPAL_ID_KEY, person.getPrincipalId());
	    } else {
		localFieldValues.put(PRINCIPAL_ID_KEY, principalName);
	    }
	}
	String supervisorPrncplName = fieldValues.get(SUPERVISOR_PRINCIPAL_NAME_KEY);
	 if (StringUtils.isNotBlank(supervisorPrncplName)) {
	 localFieldValues.remove(SUPERVISOR_PRINCIPAL_NAME_KEY);
	 Person person = personService.getPersonByPrincipalName(supervisorPrncplName);
	 if (ObjectUtils.isNotNull(person)) {
	 localFieldValues.put(SUPERVISOR_PRINCIPAL_ID_KEY, person.getPrincipalId());
	 } else {
	 localFieldValues.put(SUPERVISOR_PRINCIPAL_ID_KEY, supervisorPrncplName);
	 }
	 }

	return localFieldValues;
    }

    /**
     * build a search result list based on the given criteria
     */
    protected Collection<CurrentAccountBalance> buildCurrentBalanceCollection(Map<String, String> fieldValues, boolean isConsolidated, String pendingEntryOption) {
	String fiscalPeriod = fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE);

	Map<String, CurrentAccountBalance> balanceMap = new HashMap<String, CurrentAccountBalance>();

	Collection<Balance> balances = this.getQualifiedBalances(fieldValues, pendingEntryOption);

	for (Balance balance : balances) {
	    if (StringUtils.isBlank(balance.getSubAccountNumber())) {
		balance.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
	    }

	    String key = balance.getAccountNumber();
	    if (!isConsolidated) {
		key = key + ":" + balance.getSubAccountNumber();
	    }

	    if (balanceMap.containsKey(key)) {
		CurrentAccountBalance currentBalance = balanceMap.get(key);
		this.updateCurrentBalance(currentBalance, balance, fiscalPeriod);
	    } else {
		CurrentAccountBalance currentBalance = new CurrentAccountBalance();
		ObjectUtil.buildObject(currentBalance, balance);
		currentBalance.resetAmounts();

		this.updateCurrentBalance(currentBalance, balance, fiscalPeriod);
		balanceMap.put(key, currentBalance);
	    }
	}

	Collection<CurrentAccountBalance> currentBalanceList = balanceMap.values();

	return currentBalanceList;
    }

    /**
     * get qualified balances. If pending entries are needed, they can be
     * included.
     */
    protected Collection<Balance> getQualifiedBalances(Map<String, String> fieldValues, String pendingEntryOption) {
	boolean isConsolidated = false;

	Collection<Balance> balanceList = this.getLookupService().findCollectionBySearchUnbounded(Balance.class, fieldValues);

	updateByPendingLedgerEntry(balanceList, fieldValues, pendingEntryOption, isConsolidated, false);

	return balanceList;
    }

    /**
     * update current balance with the given balance for the specified period
     */
    protected void updateCurrentBalance(CurrentAccountBalance currentBalance, Balance balance, String fiscalPeriod) {
	List<String> cashBudgetRecordLevelCodes = this.getParameterService().getParameterValues(CurrentAccountBalance.class,
		KFSParameterKeyConstants.GeneralLedgerSysParmeterKeys.CASH_BUDGET_RECORD_LEVEL_PARM);
	List<String> expenseObjectTypeCodes = this.getParameterService().getParameterValues(CurrentAccountBalance.class,
		KFSParameterKeyConstants.GeneralLedgerSysParmeterKeys.EXPENSE_OBJECT_TYPE_CODE_PARAM);
	List<String> fundBalanceObjCodes = this.getParameterService().getParameterValues(CurrentAccountBalance.class,
		KFSParameterKeyConstants.GeneralLedgerSysParmeterKeys.FUND_BALANCE_OBJECT_CODE_PARAM);
	List<String> currentAssetObjTypeCodes = this.getParameterService().getParameterValues(CurrentAccountBalance.class,
		KFSParameterKeyConstants.GeneralLedgerSysParmeterKeys.CURRENT_ASSET_OBJECT_CODE_PARAM);
	List<String> currentLiabilityObjTypeCodes = this.getParameterService().getParameterValues(CurrentAccountBalance.class,
		KFSParameterKeyConstants.GeneralLedgerSysParmeterKeys.CURRENT_LIABILITY_OBJECT_CODE_PARAM);
	List<String> incomeObjTypeCodes = this.getParameterService().getParameterValues(CurrentAccountBalance.class,
		KFSParameterKeyConstants.GeneralLedgerSysParmeterKeys.INCOME_OBJECT_TYPE_CODE_PARAM);
	List<String> encumbranceBalTypes = this.getParameterService().getParameterValues(CurrentAccountBalance.class,
		KFSParameterKeyConstants.GeneralLedgerSysParmeterKeys.ENCUMBRANCE_BALANCE_TYPE_PARAM);
	List<String> aSlIfBObjectTypes = Arrays.asList(new String[] { "AS", "LI", "FB" });

	String balanceTypeCode = balance.getBalanceTypeCode();
	String objectTypeCode = balance.getObjectTypeCode();
	String objectCode = balance.getObjectCode();
	Account account = balance.getAccount();
	String bdgtCd = account.getBudgetRecordingLevelCode();

	if (ObjectUtils.isNull(account) || ObjectUtils.isNull(bdgtCd)) {
		 account = SpringContext.getBean(AccountService.class).getByPrimaryId(balance.getChartOfAccountsCode(), balance.getAccountNumber());
		 balance.setAccount(account);
		 currentBalance.setAccount(account);
		 }
	boolean isCashBdgtRecording = cashBudgetRecordLevelCodes.contains(account.getBudgetRecordingLevelCode());

	// Current Budget (A)
	if (isCashBdgtRecording) {
	    currentBalance.setCurrentBudget(KualiDecimal.ZERO);
	} else {
	    if (KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE.equals(balanceTypeCode) && expenseObjectTypeCodes.contains(objectTypeCode)) {
	    	currentBalance.setCurrentBudget(add(currentBalance.getCurrentBudget(),
	    			 add(accumulateMonthlyAmounts(balance, fiscalPeriod), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE))));
	    }
	}
	// Beginning Fund Balance (B)
	if (isCashBdgtRecording) {
	    if (fundBalanceObjCodes.contains(objectCode)) {
		currentBalance.setBeginningFundBalance(add(currentBalance.getBeginningFundBalance(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
	    }
	} else {
	    currentBalance.setBeginningFundBalance(KualiDecimal.ZERO);
	}

	// Beginning Current Assets (C)
	if (isCashBdgtRecording) {
	    if (currentAssetObjTypeCodes.contains(objectTypeCode)) {
		currentBalance.setBeginningCurrentAssets(add(currentBalance.getBeginningCurrentAssets(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));		
	    }
	} else {
	    currentBalance.setBeginningCurrentAssets(KualiDecimal.ZERO);	    
	}
 
	 //Beginning Current Liabilities (D)
	if (isCashBdgtRecording) {
	    if (currentLiabilityObjTypeCodes.contains(objectTypeCode)) {
		currentBalance.setBeginningCurrentLiabilities(add(currentBalance.getBeginningCurrentLiabilities(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)));
	    }
	} else {
	    currentBalance.setBeginningCurrentLiabilities(KualiDecimal.ZERO);
	}

	// Total Income (E)
	if (isCashBdgtRecording) {
	    if (incomeObjTypeCodes.contains(objectTypeCode) && KFSConstants.BALANCE_TYPE_ACTUAL.equals(balanceTypeCode)) {
		currentBalance.setTotalIncome(add(currentBalance.getTotalIncome(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
		currentBalance.setTotalIncome(add(currentBalance.getTotalIncome(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE)));
	    }
	} else {
	    currentBalance.setTotalIncome(KualiDecimal.ZERO);
	}

	// Total Expense (F)
	if (expenseObjectTypeCodes.contains(objectTypeCode) && KFSConstants.BALANCE_TYPE_ACTUAL.equals(balanceTypeCode)) {
	    currentBalance.setTotalExpense(add(currentBalance.getTotalExpense(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
	    currentBalance.setTotalExpense(add(currentBalance.getTotalExpense(), accumulateMonthlyAmounts(balance, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE)));
	}

	// Encumbrances (G)
	if (encumbranceBalTypes.contains(balanceTypeCode) && (expenseObjectTypeCodes.contains(objectTypeCode) || incomeObjTypeCodes.contains(objectTypeCode))
		&& !aSlIfBObjectTypes.contains(objectTypeCode)) {
		currentBalance.setEncumbrances(add(currentBalance.getEncumbrances(), accumulateMonthlyAmounts(balance, fiscalPeriod)));
}

	// Budget Balance Available (H)
	if (isCashBdgtRecording) {
	    currentBalance.setBudgetBalanceAvailable(KualiDecimal.ZERO);
	} else {
	    currentBalance.setBudgetBalanceAvailable(currentBalance.getCurrentBudget().subtract(currentBalance.getTotalExpense()).subtract(currentBalance.getEncumbrances()));
	}

	// Cash Expenditure Authority (I)
	if (isCashBdgtRecording) {
		currentBalance.setCashExpenditureAuthority(currentBalance.getBeginningCurrentAssets().subtract(currentBalance.getBeginningCurrentLiabilities()).add(currentBalance.getTotalIncome())
				 .subtract(currentBalance.getTotalExpense()).subtract(currentBalance.getEncumbrances()));
	} else {
	    currentBalance.setCashExpenditureAuthority(KualiDecimal.ZERO);
	}
	// Current Fund Balance (J)
	if (isCashBdgtRecording) {
	    currentBalance.setCurrentFundBalance(currentBalance.getBeginningFundBalance().add(currentBalance.getTotalIncome()).subtract(currentBalance.getTotalExpense()));
	} else {
	    currentBalance.setCurrentFundBalance(KualiDecimal.ZERO);
	}

    }

    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#validateSearchParameters(java.util.Map)
     */
    @Override
    public void validateSearchParameters(Map fieldValues) {
	super.validateSearchParameters(fieldValues);

	Integer fiscalYear = 0;
	String valueFiscalYear = (String) fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
	if (!StringUtils.isEmpty(valueFiscalYear)) {
	    try {
		fiscalYear = Integer.parseInt(valueFiscalYear);
	    } catch (NumberFormatException e) {
		GlobalVariables.getMessageMap().putError(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, KFSKeyConstants.PendingEntryLookupableImpl.FISCAL_YEAR_FOUR_DIGIT);
		throw new ValidationException("errors in search criteria");
	    }
	}

	String accountNumber = (String) fieldValues.get(ACCOUNT_NUMBER_FIELD_KEY);
	String organizationCode = (String) fieldValues.get(ORGANIZATION_FIELD_KEY);
	String fiscalOfficerPrincipalName = (String) fieldValues.get(PRINCIPAL_NAME_KEY);
	String supervisorPrincipalName = (String) fieldValues.get(SUPERVISOR_PRINCIPAL_NAME_KEY);

	if (StringUtils.isBlank(accountNumber) && StringUtils.isBlank(organizationCode) && StringUtils.isBlank(fiscalOfficerPrincipalName) && StringUtils.isBlank(supervisorPrincipalName)) {
	    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, KFSKeyConstants.ERROR_CURRBALANCE_LOOKUP_CRITERIA_REQD);
	    throw new ValidationException("errors in search criteria");
	}

	String fiscalPeriod = (String) fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE);
	AccountingPeriod accountingPeriod = accountingPeriodService.getByPeriod(fiscalPeriod, fiscalYear);
	if (ObjectUtils.isNull(accountingPeriod)) {
	    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE, KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_NOT_FOUND);
	    throw new ValidationException("errors in search criteria");
	}
    }

    /**
     * accumulate monthly amount up to the given period
     */
    public KualiDecimal accumulateMonthlyAmounts(Balance balance, String fiscalPeriodCode) {

	KualiDecimal beginningAmount = balance.getBeginningBalanceLineAmount();
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE)) {
	    return beginningAmount;
	}

	KualiDecimal CGBeginningAmount = balance.getContractsGrantsBeginningBalanceAmount();
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE)) {
	    return CGBeginningAmount;
	}

	KualiDecimal month0Amount = beginningAmount;
	KualiDecimal month1Amount = add(balance.getMonth1Amount(), month0Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH1)) {
	    return month1Amount;
	}

	KualiDecimal month2Amount = add(balance.getMonth2Amount(), month1Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH2)) {
	    return month2Amount;
	}

	KualiDecimal month3Amount = add(balance.getMonth3Amount(), month2Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH3)) {
	    return month3Amount;
	}

	KualiDecimal month4Amount = add(balance.getMonth4Amount(), month3Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH4)) {
	    return month4Amount;
	}

	KualiDecimal month5Amount = add(balance.getMonth5Amount(), month4Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH5)) {
	    return month5Amount;
	}

	KualiDecimal month6Amount = add(balance.getMonth6Amount(), month5Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH6)) {
	    return month6Amount;
	}

	KualiDecimal month7Amount = add(balance.getMonth7Amount(), month6Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH7)) {
	    return month7Amount;
	}

	KualiDecimal month8Amount = add(balance.getMonth8Amount(), month7Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH8)) {
	    return month8Amount;
	}

	KualiDecimal month9Amount = add(balance.getMonth9Amount(), month8Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH9)) {
	    return month9Amount;
	}

	KualiDecimal month10Amount = add(balance.getMonth10Amount(), month9Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH10)) {
	    return month10Amount;
	}

	KualiDecimal month11Amount = add(balance.getMonth11Amount(), month10Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH11)) {
	    return month11Amount;
	}

	KualiDecimal month12Amount = add(balance.getMonth12Amount(), month11Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH12)) {
	    return month12Amount;
	}

	KualiDecimal month13Amount = add(balance.getMonth13Amount(), month12Amount);
	if (StringUtils.equals(fiscalPeriodCode, KFSConstants.MONTH13)) {
	    return month13Amount;
	}

	return balance.getAccountLineAnnualBalanceAmount();
    }

    /**
     * check null and add up the two given amounts
     */
    protected KualiDecimal add(KualiDecimal augend, KualiDecimal addend) {
	KualiDecimal first = augend == null ? KualiDecimal.ZERO : augend;
	KualiDecimal second = addend == null ? KualiDecimal.ZERO : addend;

	return first.add(second);
    }

    /**
     * @see org.kuali.kfs.gl.businessobject.lookup.AbstractGeneralLedgerLookupableHelperServiceImpl#updateEntryCollection(java.util.Collection,
     *      java.util.Map, boolean, boolean, boolean)
     */
    protected void updateEntryCollection(Collection entryCollection, Map fieldValues, boolean isApproved, boolean isConsolidated, boolean isCostShareInclusive) {

	// convert the field names of balance object into corresponding ones of
	// pending entry object
	Map pendingEntryFieldValues = BusinessObjectFieldConverter.convertToTransactionFieldValues(fieldValues);
	pendingEntryFieldValues.remove(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE);

	// go through the pending entries to update the balance collection
	Iterator<GeneralLedgerPendingEntry> pendingEntryIterator = getGeneralLedgerPendingEntryService().findPendingLedgerEntriesForBalance(pendingEntryFieldValues, isApproved);
	while (pendingEntryIterator.hasNext()) {
	    GeneralLedgerPendingEntry pendingEntry = pendingEntryIterator.next();

	    Balance balance = this.getPostBalance().findBalance(entryCollection, pendingEntry);

	    this.getPostBalance().updateBalance(pendingEntry, balance);
	}
    }

    /**
     * Sets the balanceService attribute value.
     * 
     * @param balanceService
     *            The balanceService to set.
     */
    public void setBalanceService(BalanceService balanceService) {
	this.balanceService = balanceService;
    }

    /**
     * Gets the personService attribute.
     * 
     * @return Returns the personService.
     */
    public PersonService<Person> getPersonService() {
	return personService;
    }

    /**
     * Sets the personService attribute value.
     * 
     * @param personService
     *            The personService to set.
     */
    public void setPersonService(PersonService<Person> personService) {
	this.personService = personService;
    }

    /**
     * Gets the postBalance attribute.
     * 
     * @return Returns the postBalance.
     */
    public BalanceCalculator getPostBalance() {
	return postBalance;
    }

    /**
     * Sets the postBalance attribute value.
     * 
     * @param postBalance
     *            The postBalance to set.
     */
    public void setPostBalance(BalanceCalculator postBalance) {
	this.postBalance = postBalance;
    }

    /**
     * Gets the accountingPeriodService attribute.
     * 
     * @return Returns the accountingPeriodService.
     */
    public AccountingPeriodService getAccountingPeriodService() {
	return accountingPeriodService;
    }

    /**
     * Sets the accountingPeriodService attribute value.
     * 
     * @param accountingPeriodService
     *            The accountingPeriodService to set.
     */
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
	this.accountingPeriodService = accountingPeriodService;
    }
}
