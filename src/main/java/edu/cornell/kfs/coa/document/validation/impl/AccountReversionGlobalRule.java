/*
 * Copyright 2007 The Kuali Foundation
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
package edu.cornell.kfs.coa.document.validation.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.FundGroup;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.AccountReversionGlobal;
import edu.cornell.kfs.coa.businessobject.AccountReversionGlobalAccount;
import edu.cornell.kfs.coa.businessobject.AccountReversionGlobalDetail;
import edu.cornell.kfs.coa.businessobject.Reversion;
import edu.cornell.kfs.coa.businessobject.options.ReversionCodeValuesFinder;
import edu.cornell.kfs.coa.document.AccountReversionGlobalMaintainableImpl;
import edu.cornell.kfs.coa.service.AccountReversionService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

/**
 * 
 * This class implements the business rules for {@link AccountReversionGlobal}
 */
public class AccountReversionGlobalRule extends GlobalDocumentRuleBase {
	private static final Logger LOG = LogManager.getLogger(AccountReversionGlobalRule.class);

    private static final String GLOBAL_ACCOUNT_FIELDS_SECTION = "Edit Global Account Reversion";
    private static final String GLOBAL_DETAIL_FIELDS_SECTION = "Edit Global Account Reversion Details";

    protected AccountReversionGlobal globalAccountReversion;
    protected AccountReversionService accountReversionService;
    protected ObjectCodeService objectCodeService;

    /**
     * 
     * Constructs a AccountReversionGlobalRule
     * Pseudo-injects services 
     */
    public AccountReversionGlobalRule() {
        super();
        setAccountReversionService(SpringContext.getBean(AccountReversionService.class));
        setObjectCodeService(SpringContext.getBean(ObjectCodeService.class));
    }

    /**
     * This method sets the convenience objects like newAccount and oldAccount, so you have short and easy handles to the new and
     * old objects contained in the maintenance document. It also calls the BusinessObjectBase.refresh(), which will attempt to load
     * all sub-objects from the DB by their primary keys, if available.
     * 
     * @param document - the maintenanceDocument being evaluated
     * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#setupConvenienceObjects()
     */
    @Override
    public void setupConvenienceObjects() {
        this.globalAccountReversion = (AccountReversionGlobal) super.getNewBo();
        for (AccountReversionGlobalDetail detail : this.globalAccountReversion.getAccountReversionGlobalDetails()) {
            detail.refreshNonUpdateableReferences();
        }
        for (AccountReversionGlobalAccount org : this.globalAccountReversion.getAccountReversionGlobalAccounts()) {
            org.refreshNonUpdateableReferences();
        }
    }

    /**
     * Calls the basic rules check on document save:
     * <ul>
     * <li>{@link AccountReversionGlobalRule#checkSimpleRules(AccountReversionGlobal)}</li>
     * </ul>
     * Does not fail on rules failure
     * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
        checkSimpleRules(getGlobalAccountReversion());
        return true; // always return true on save
    }

    /**
     * Calls the basic rules check on document approval:
     * <ul>
     * <li>{@link AccountReversionGlobalRule#checkSimpleRules(AccountReversionGlobal)}</li>
     * </ul>
     * Fails on rules failure
     * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomApproveDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
        return checkSimpleRules(getGlobalAccountReversion());
    }

    /**
     * Calls the basic rules check on document routing:
     * <ul>
     * <li>{@link AccountReversionGlobalRule#checkSimpleRules(AccountReversionGlobal)}</li>
     * </ul>
     * Fails on rules failure
     * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        return checkSimpleRules(getGlobalAccountReversion());
    }

    /**
     * This performs rules checks whenever a new {@link AccountReversionGlobalDetail} or {@link AccountReversionGlobalAccount} is added
     * <p>
     * This includes:
     * <ul>
     * <li>{@link AccountReversionGlobalRule#checkDetailObjectCodeValidity(AccountReversionGlobal, AccountReversionGlobalDetail)}</li>
     * <li>{@link AccountReversionGlobalRule#checkDetailObjectReversionCodeValidity(AccountReversionGlobalDetail)}</li>
     * <li>ensure that the chart of accounts code and account number for {@link AccountReversionGlobalAccount} are not empty values</li>
     * <li>{@link AccountReversionGlobalRule#checkAllObjectCodesForValidity(AccountReversionGlobal, AccountReversionGlobalAccount)}</li>
     * <li>{@link AccountReversionGlobalRule#checkAccountChartValidity(AccountReversionGlobalAccount)</li>
     * <li>{@link AccountReversionGlobalRule#checkAccountValidity(AccountReversionGlobalAccount)</li>
     * <li>{@link AccountReversionGlobalRule#checkAccountIsNotAmongAcctRevAccounts(AccountReversionGlobal, AccountReversionGlobalAccount)</li>
     * </ul>
     * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument,
     *      java.lang.String, org.kuali.kfs.kns.bo.PersistableBusinessObject)
     */
    @Override
    public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
        boolean success = true;
        AccountReversionGlobal globalAcctRev = (AccountReversionGlobal) ((AccountReversionGlobalMaintainableImpl) document.getNewMaintainableObject()).getBusinessObject();
        if (line instanceof AccountReversionGlobalDetail) {
            AccountReversionGlobalDetail detail = (AccountReversionGlobalDetail) line;
            success &= checkDetailObjectCodeValidity(globalAcctRev, detail);
            success &= checkDetailObjectReversionCodeValidity(detail);
        }
        else if (line instanceof AccountReversionGlobalAccount) {
            AccountReversionGlobalAccount acct = (AccountReversionGlobalAccount) line;
            if (!checkEmptyValue(acct.getChartOfAccountsCode()) || !checkEmptyValue(acct.getAccountNumber())) {
                // Skip Most validation if chart or account are empty. The default required-field checking will populate the error map accordingly.
                success = false;
            }
            if (success) {
                success &= checkAllObjectCodesForValidity(globalAcctRev, acct);
                success &= checkAccountChartValidity(acct);
                success &= checkAccountValidity(acct);
                success &= checkAccountIsNotAmongAcctRevAccounts(globalAcctRev, acct);
            }
        }
        return success;
    }

    /**
     * Convenient convenience method to test all the simple rules in one go. Including:
     * <ul>
     * <li>{@link AccountReversionGlobalRule#checkBudgetReversionAccountPair(AccountReversionGlobal)}</li>
     * <li>{@link AccountReversionGlobalRule#checkCashReversionAccountPair(AccountReversionGlobal)}</li>
     * <li>{@link AccountReversionGlobalRule#areAllDetailsValid(AccountReversionGlobal)}</li>
     * <li>{@link AccountReversionGlobalRule#areAllOrganizationsValid(AccountReversionGlobal)</li>
     * </ul>
     * @param globalOrgRev the global organization reversion to check
     * @return true if the new global organization reversion passes all tests, false if it deviates even a tiny little bit
     */
    public boolean checkSimpleRules(AccountReversionGlobal globalAcctRev) {
        boolean success = true;

        success &= checkBudgetReversionAccountPair(globalAcctRev);
        success &= checkCashReversionAccountPair(globalAcctRev);
        success &= validateAccountFundGroup(globalAcctRev);
        success &= validateAccountSubFundGroup(globalAcctRev);

        success &= areAllDetailsValid(globalAcctRev);
        success &= areAllAccountsValid(globalAcctRev);

        return success;
    }

    /**
     * This method makes sure that if one part of the Budget Reversion Chart/Account pair is specified, both are specified, or an
     * error is thrown.
     * 
     * @param globalAcctRev the Global Account Reversion to check
     * @return true if budget reversion chart/account pair is specified correctly, false if otherwise
     */
    public boolean checkBudgetReversionAccountPair(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        if ((!StringUtils.isBlank(globalAcctRev.getBudgetReversionChartOfAccountsCode()) && StringUtils.isBlank(globalAcctRev.getBudgetReversionAccountNumber())) || (StringUtils.isBlank(globalAcctRev.getBudgetReversionChartOfAccountsCode()) && !StringUtils.isBlank(globalAcctRev.getBudgetReversionAccountNumber()))) {
            success = false;
            GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + "budgetReversionChartOfAccountsCode", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_BUDGET_REVERSION_INCOMPLETE, new String[] {});
        }
        return success;
    }

    /**
     * This method makes sure that if one part of the Cash Reversion Chart/Account pair is specified, both are specified, or an
     * error is thrown.
     * 
     * @param globalAcctRev the Global Account Reversion to check
     * @return true if cash reversion chart/account pair is specified correctly, false if otherwise
     */
    public boolean checkCashReversionAccountPair(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        if ((!StringUtils.isBlank(globalAcctRev.getCashReversionFinancialChartOfAccountsCode()) && StringUtils.isBlank(globalAcctRev.getCashReversionAccountNumber())) || (StringUtils.isBlank(globalAcctRev.getCashReversionFinancialChartOfAccountsCode()) && !StringUtils.isBlank(globalAcctRev.getCashReversionAccountNumber()))) {
            success = false;
            GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + "cashReversionFinancialChartOfAccountsCode", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_CASH_REVERSION_INCOMPLETE, new String[] {});
        }
        return success;
    }

    /**
     * Tests if all of the {@link AccountReversionGlobalDetail} objects associated with the given global account reversion are
     * valid.
     * 
     * @param globalAcctRev the global account reversion to check
     * @return true if valid, false otherwise
     */
    public boolean areAllDetailsValid(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        for (int i = 0; i < globalAcctRev.getAccountReversionGlobalDetails().size(); i++) {
            AccountReversionGlobalDetail detail = globalAcctRev.getAccountReversionGlobalDetails().get(i);
            
            String errorPath = MAINTAINABLE_ERROR_PREFIX + "accountReversionGlobalDetails[" + i + "]";
            GlobalVariables.getMessageMap().addToErrorPath(errorPath);

            if (!StringUtils.isBlank(detail.getAccountReversionObjectCode()) && !StringUtils.isBlank(detail.getAccountReversionCode())) {
                success &= this.checkDetailAcctReversionCategoryValidity(detail);
                success &= this.checkDetailObjectCodeValidity(globalAcctRev, detail);
                success &= this.checkDetailObjectReversionCodeValidity(detail);
            }
            GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
        }
        return success;
    }

    /**
     * Tests if the Account Reversion Category existed in the database and was active.
     * 
     * @param detail AccountReversionGlobalDetail to check
     * @return true if the category is valid, false if otherwise
     */
    public boolean checkDetailAcctReversionCategoryValidity(AccountReversionGlobalDetail detail) {
        boolean success = true;
        if (StringUtils.isBlank(detail.getAccountReversionCategoryCode())) {
            success = false;
            GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_CATEGORY_CODE,
                    KFSKeyConstants.ERROR_REQUIRED, "Account Reversion Category");
        } else {
            detail.refreshReferenceObject("reversionCategory");
            if (ObjectUtils.isNull(detail.getReversionCategory()) || !detail.getReversionCategory().isActive()) {
                success = false;
                GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_CATEGORY_CODE,
                        CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_ACCT_REVERSION_CATEGORY,
                        new String[] { detail.getAccountReversionCategoryCode() });
            }
        }
        return success;
    }

    /**
     * For each account, tests if the object code in the detail exists in the system and is active
     * 
     * @param globalAcctRev the global account reversion to check
     * @param detail the AccountReversionGlobalDetail to check
     * @return true if it is valid, false if otherwise
     */
    public boolean checkDetailObjectCodeValidity(AccountReversionGlobal globalAcctRev, AccountReversionGlobalDetail detail) {
        boolean success = true;
        for (AccountReversionGlobalAccount acct : globalAcctRev.getAccountReversionGlobalAccounts()) {
            if (!validObjectCode(globalAcctRev.getUniversityFiscalYear(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode())) {
                success = false;
                GlobalVariables.getMessageMap().putError("accountReversionObjectCode", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_OBJECT_CODE_INVALID, new String[] { globalAcctRev.getUniversityFiscalYear().toString(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode(), acct.getChartOfAccountsCode(), acct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * This method loops through each of the AccountReversionGlobalDetail objects, checking that the entered object codes for
     * each of them are compatible with the AccountReversionGlobalAccount specified.
     * 
     * @param globalAcctRev the global account reversion to check
     * @param acct the AccountReversionGlobalOrganization with a new chart to check against all of the object codes
     * @return true if there are no conflicts, false if otherwise
     */
    public boolean checkAllObjectCodesForValidity(AccountReversionGlobal globalAcctRev, AccountReversionGlobalAccount acct) {
        boolean success = true;
        for (AccountReversionGlobalDetail detail : globalAcctRev.getAccountReversionGlobalDetails()) {
            if (!validObjectCode(globalAcctRev.getUniversityFiscalYear(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode())) {
                success = false;
                GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_OBJECT_CODE_INVALID, new String[] { globalAcctRev.getUniversityFiscalYear().toString(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode(), acct.getChartOfAccountsCode(), acct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * This method checks if an object code with the given primary key fields exists in the database.
     * 
     * @param universityFiscalYear the university fiscal year of the object code
     * @param chartOfAccountsCode the chart of accounts code of the object code
     * @param objectCode the object code itself
     * @return true if it exists (or was not filled in to begin with), false if otherwise
     */
    public boolean validObjectCode(Integer universityFiscalYear, String chartOfAccountsCode, String objectCode) {
        if (!StringUtils.isBlank(objectCode) && universityFiscalYear != null && !StringUtils.isBlank(chartOfAccountsCode)) {
            ObjectCode objCode = objectCodeService.getByPrimaryId(universityFiscalYear, chartOfAccountsCode, objectCode);
            return (ObjectUtils.isNotNull(objCode));
        }
        else {
            return true; // blank object code? well, it's not required...and thus, it's a valid choice
        }
    }

    /**
     * Tests if the object reversion code is a valid code.
     * 
     * @param detail the AccountReversionGlobalDetail to check
     * @return true if it the detail is valid, false if otherwise
     */
    public boolean checkDetailObjectReversionCodeValidity(AccountReversionGlobalDetail detail) {
        boolean success = true;
        if (!StringUtils.isBlank(detail.getAccountReversionCode())) {
            boolean foundInList = false;
            // TODO Dude!! The *only* place that the acct reversion code values are defined
            // is in the lookup class, so I've got to use a web-based class to actually
            // search through the values. Is that right good & healthy?
            for (Object kvPairObj : new ReversionCodeValuesFinder().getKeyValues()) {
            	ConcreteKeyValue kvPair = (ConcreteKeyValue) kvPairObj;
                if (kvPair.getKey().toString().equals(detail.getAccountReversionCode())) {
                    foundInList = true;
                    break;
                }
            }
            if (!foundInList) {
                success = false; // we've failed to find the code in the list...FAILED!
                GlobalVariables.getMessageMap().putError("accountReversionCode", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_ACCT_REVERSION_CODE, new String[] { detail.getAccountReversionCode() });
            }
        }
        return success;
    }

    /**
     * This method tests if all the AccountReversionGlobalAccount objects associated with the given global account
     * reversion pass all of their tests.
     * 
     * @param globalAcctRev the global account reversion to check
     * @return true if valid, false otherwise
     */
    public boolean areAllAccountsValid(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        if (globalAcctRev.getAccountReversionGlobalAccounts().size() == 0) {
            putFieldError(KFSConstants.MAINTENANCE_ADD_PREFIX + "accountReversionGlobalAccounts.organizationCode", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_NO_ACCOUNTS);
            success = false;
        }
        else {
            success &= checkAllAccountReversionsExistOrAllFieldsAreDefined(globalAcctRev);
            for (int i = 0; i < globalAcctRev.getAccountReversionGlobalAccounts().size(); i++) {
                AccountReversionGlobalAccount acct = globalAcctRev.getAccountReversionGlobalAccounts().get(i);
                String errorPath = MAINTAINABLE_ERROR_PREFIX + "accountReversionGlobalAccounts[" + i + "]";
                GlobalVariables.getMessageMap().addToErrorPath(errorPath);
                success &= checkAllObjectCodesForValidity(globalAcctRev, acct);
                success &= checkAccountValidity(acct);
                success &= checkAccountChartValidity(acct);
                success &= validateAccountFundGroup( acct);
                success &= validateAccountSubFundGroup( acct);
                GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
            }
        }
        return success;
    }

    /**
     * Tests if the the account of the given AccountReversionGlobalAccount is within the chart of the global
     * account reversion as a whole.
     * 
     * @param acct the AccountReversionGlobalAccount to check
     * @return true if valid, false otherwise
     */
    public boolean checkAccountChartValidity(AccountReversionGlobalAccount acct) {
        boolean success = true;
        if (StringUtils.isBlank(acct.getChartOfAccountsCode())) {
            if (!StringUtils.isBlank(acct.getAccountNumber())) {
                success = false;
                GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_CHART_OF_ACCT_CODE, KFSKeyConstants.ERROR_REQUIRED, "Chart of Accounts Code");
            }
        }
        else {
            acct.setChartOfAccountsCode(acct.getChartOfAccountsCode().toUpperCase(Locale.US));
            acct.refreshReferenceObject("chartOfAccounts");
            if (ObjectUtils.isNull(acct.getChartOfAccounts())) {
                success = false;
                GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_CHART_OF_ACCT_CODE, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_CHART, new String[] { acct.getChartOfAccountsCode() });
            }
        }
        return success;
    }

    /**
     * Tests if the given AccountReversionGlobalAccount's Account is active and within the system.
     * 
     * @param acct the AccountReversionGlobalAccount to check
     * @return true if valid, false otherwise
     */
    public boolean checkAccountValidity(AccountReversionGlobalAccount acct) {
        boolean success = true;
        if (StringUtils.isBlank(acct.getAccountNumber())) {
            if (!StringUtils.isBlank(acct.getChartOfAccountsCode())) {
                success = false;
                GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER, KFSKeyConstants.ERROR_REQUIRED, "Account Number");
            }
        }
        else if (!StringUtils.isBlank(acct.getChartOfAccountsCode())) {
            acct.refreshReferenceObject("account");
            if (ObjectUtils.isNull(acct.getAccount())) {
                success = false;
                GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_ACCOUNT, new String[] { acct.getChartOfAccountsCode(), acct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * Checks that either every account reversion on the document references an existing account reversion object,
     * or that all fields have been filled in so that new account reversions can be saved properly.
     * Will skip validation if a fiscal year has not been specified on the document.
     * 
     * @param globalAcctRev global Account Reversion to check
     * @return true if all referenced account reversions already exist or all reversion and detail fields are filled in, false otherwise.
     */
    public boolean checkAllAccountReversionsExistOrAllFieldsAreDefined(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        
        if (globalAcctRev.getUniversityFiscalYear() != null) {
            boolean allAccountReversionsExist = true;
            List<AccountReversionGlobalAccount> globalAccounts = globalAcctRev.getAccountReversionGlobalAccounts();
            for (int i = 0; allAccountReversionsExist && i < globalAccounts.size(); i++) {
                allAccountReversionsExist &= ObjectUtils.isNotNull(accountReversionService.getByPrimaryId(globalAcctRev.getUniversityFiscalYear(),
                        globalAccounts.get(i).getChartOfAccountsCode(), globalAccounts.get(i).getAccountNumber()));
            }
            if (!allAccountReversionsExist) {
                // If new account reversions were specified, make sure all reversion fields are filled. (We know fiscal year is defined at this point.)
                boolean allFieldsFilled = StringUtils.isNotBlank(globalAcctRev.getBudgetReversionChartOfAccountsCode())
                        && StringUtils.isNotBlank(globalAcctRev.getBudgetReversionAccountNumber())
                        && globalAcctRev.getCarryForwardByObjectCodeIndicator() != null
                        && StringUtils.isNotBlank(globalAcctRev.getCashReversionFinancialChartOfAccountsCode())
                        && StringUtils.isNotBlank(globalAcctRev.getCashReversionAccountNumber())
                        && globalAcctRev.getReversionActiveIndicator() != null;
                if (!allFieldsFilled) {
                    GlobalVariables.getMessageMap().putError(GLOBAL_ACCOUNT_FIELDS_SECTION,
                            CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_MISSING_FIELDS_FOR_NEW_REVERSION);
                    success = false;
                }
                // Also check that reversion details are all filled. (Reversion category code should already be set up by the document initialization.)
                for (AccountReversionGlobalDetail globalDetail : globalAcctRev.getAccountReversionGlobalDetails()) {
                    allFieldsFilled &= StringUtils.isNotBlank(globalDetail.getAccountReversionCategoryCode())
                            && StringUtils.isNotBlank(globalDetail.getAccountReversionCode());
                }
                if (!allFieldsFilled) {
                    GlobalVariables.getMessageMap().putError(GLOBAL_DETAIL_FIELDS_SECTION,
                            CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_MISSING_FIELDS_FOR_NEW_REVERSION_DETAIL);
                    success = false;
                }
            }
        }
        
        return success;
    }

    /**
     * This method checks if a newly added account is already among the accounts already listed. WARNING: only use on add
     * line rules; there's no good way to use this method when testing the entire document.
     * 
     * @param globalAcctRev the global Account Reversion to check
     * @param acctRevOrg the newly adding account reversion change account
     * @return true if account should be added as it is not currently in the collection, false if otherwise
     */
    public boolean checkAccountIsNotAmongAcctRevAccounts(AccountReversionGlobal globalAcctRev, AccountReversionGlobalAccount acctRevAcct) {
        boolean success = true;
        Iterator<AccountReversionGlobalAccount> iter = globalAcctRev.getAccountReversionGlobalAccounts().iterator();
        while (iter.hasNext() && success) {
            AccountReversionGlobalAccount currAcct = iter.next();
            if (areContainingSameAccounts(currAcct, acctRevAcct)) {
                success = false;
                GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_DUPLICATE_ACCOUNTS, new String[] { acctRevAcct.getChartOfAccountsCode(), acctRevAcct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * This method tests if two AccountReversionGlobalAccount objects are holding the same underlying Account.
     * 
     * @param acctRevAcctA the first AccountReversionGlobalAccount to check
     * @param acctRevAcctB the second AccountReversionGlobalAccount to check
     * @return true if they share the account, false if otherwise
     */
    public static boolean areContainingSameAccounts(AccountReversionGlobalAccount acctRevAcctA, AccountReversionGlobalAccount acctRevAcctB) {
        boolean containingSame = false;
        if (acctRevAcctA.getChartOfAccountsCode() != null && acctRevAcctB.getChartOfAccountsCode() != null && acctRevAcctA.getAccountNumber() != null && acctRevAcctB.getAccountNumber() != null) {
            containingSame = (acctRevAcctA.getChartOfAccountsCode().equals(acctRevAcctB.getChartOfAccountsCode()) && acctRevAcctA.getAccountNumber().equals(acctRevAcctB.getAccountNumber()));
        }
        return containingSame;
    }
    
    /**
     * Validates that the fund group code on the sub fund group on the reversion account is valid as defined by the allowed
     * values in SELECTION_1 system parameter.
     * 
     * @param acctRev
     * @return true if valid, false otherwise
     */
    protected boolean validateAccountFundGroup(AccountReversionGlobalAccount acctRev) {
        boolean valid = true;
        String fundGroups = SpringContext.getBean(ParameterService.class).getParameterValueAsString(Reversion.class, CUKFSConstants.Reversion.SELECTION_1);
        String propertyName = StringUtils.substringBefore(fundGroups, "=");
        List<String> ruleValues = Arrays.asList(StringUtils.substringAfter(fundGroups, "=").split(";"));

        if (ObjectUtils.isNotNull(ruleValues) && ruleValues.size() > 0) {
            if (ObjectUtils.isNotNull(acctRev.getAccount()) && ObjectUtils.isNotNull(acctRev.getAccount().getSubFundGroup())) {
                String accountFundGroupCode = acctRev.getAccount().getSubFundGroup().getFundGroupCode();

                if (!ruleValues.contains(accountFundGroupCode)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_ALLOWED_VALUES_PARAMETER, new String[]{getDataDictionaryService().getAttributeLabel(FundGroup.class, KFSPropertyConstants.CODE), accountFundGroupCode, getParameterAsStringForMessage(CUKFSConstants.Reversion.SELECTION_1), getParameterValuesForMessage(ruleValues), getDataDictionaryService().getAttributeLabel(AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER)});
                }
            }
        }

        return valid;
    }

    /**
     * Validates that the sub fund group code on the reversion account is valid as defined by the allowed values in
     * SELECTION_4 system parameter.
     * 
     * @param acctRev
     * @return true if valid, false otherwise
     */
    protected boolean validateAccountSubFundGroup(AccountReversionGlobalAccount acctRev) {
        boolean valid = true;
        
        String subFundGroups = SpringContext.getBean(ParameterService.class).getParameterValueAsString(Reversion.class, CUKFSConstants.Reversion.SELECTION_4);
        String propertyName = StringUtils.substringBefore(subFundGroups, "=");
        List<String> ruleValues = Arrays.asList(StringUtils.substringAfter(subFundGroups, "=").split(";"));

        if (ObjectUtils.isNotNull(ruleValues) && ruleValues.size() > 0) {
            if (ObjectUtils.isNotNull(acctRev.getAccount())) {
                String accountSubFundGroupCode = acctRev.getAccount().getSubFundGroupCode();

                if (ruleValues != null && ruleValues.size() > 0 && ruleValues.contains(accountSubFundGroupCode)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER, KFSKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_DENIED_VALUES_PARAMETER, new String[]{getDataDictionaryService().getAttributeLabel(SubFundGroup.class, KFSPropertyConstants.SUB_FUND_GROUP_CODE), accountSubFundGroupCode, getParameterAsStringForMessage(CUKFSConstants.Reversion.SELECTION_4), getParameterValuesForMessage(ruleValues), getDataDictionaryService().getAttributeLabel(AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER)});
                }
            }
        }

        return valid;
    }

    /**
     * Validates that the fund group code on the sub fund group on the cash and budget accounts is valid as defined by the
     * allowed values in SELECTION_1 system parameter.
     * 
     * @param globalAcctRev
     * @return
     */
    protected boolean validateAccountFundGroup(AccountReversionGlobal globalAcctRev) {
        boolean valid = true;
        
        String fundGroups = SpringContext.getBean(ParameterService.class).getParameterValueAsString(Reversion.class, CUKFSConstants.Reversion.SELECTION_1);
        String propertyName = StringUtils.substringBefore(fundGroups, "=");
        List<String> ruleValues = Arrays.asList(StringUtils.substringAfter(fundGroups, "=").split(";"));

        if (ObjectUtils.isNotNull(ruleValues) && ruleValues.size() > 0) {

            if (ObjectUtils.isNotNull(globalAcctRev.getBudgetReversionAccount()) && ObjectUtils.isNotNull(globalAcctRev.getBudgetReversionAccount().getSubFundGroup())) {
                String budgetAccountFundGroupCode = globalAcctRev.getBudgetReversionAccount().getSubFundGroup().getFundGroupCode();

                if (!ruleValues.contains(budgetAccountFundGroupCode)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_ALLOWED_VALUES_PARAMETER, new String[]{getDataDictionaryService().getAttributeLabel(FundGroup.class, KFSPropertyConstants.CODE), budgetAccountFundGroupCode, getParameterAsStringForMessage(CUKFSConstants.Reversion.SELECTION_1), getParameterValuesForMessage(ruleValues), getDataDictionaryService().getAttributeLabel(AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER)});
                }
            }

            if (ObjectUtils.isNotNull(globalAcctRev.getCashReversionAccount()) && ObjectUtils.isNotNull(globalAcctRev.getCashReversionAccount().getSubFundGroup())) {

                String cashAccountFundGroupCode = globalAcctRev.getCashReversionAccount().getSubFundGroup().getFundGroupCode();
                
                if (!ruleValues.contains(cashAccountFundGroupCode)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_ALLOWED_VALUES_PARAMETER, new String[]{getDataDictionaryService().getAttributeLabel(FundGroup.class, KFSPropertyConstants.CODE), cashAccountFundGroupCode, getParameterAsStringForMessage(CUKFSConstants.Reversion.SELECTION_1), getParameterValuesForMessage(ruleValues), getDataDictionaryService().getAttributeLabel(AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER)});
                }
            }
        }

        return valid;
    }
    
    
    /**
     * Validates that the sub fund group code on the cash and budget accounts is valid as defined by the allowed values in
     * SELECTION_4 system parameter.
     * 
     * @param globalAcctRev
     * @return
     */
    protected boolean validateAccountSubFundGroup(AccountReversionGlobal globalAcctRev) {
        boolean valid = true;
        
        String subFundGroups = SpringContext.getBean(ParameterService.class).getParameterValueAsString(Reversion.class, CUKFSConstants.Reversion.SELECTION_4);
        String propertyName = StringUtils.substringBefore(subFundGroups, "=");
        List<String> ruleValues = Arrays.asList(StringUtils.substringAfter(subFundGroups, "=").split(";"));

        if (ObjectUtils.isNotNull(ruleValues) && ruleValues.size() > 0) {

            if (ObjectUtils.isNotNull(globalAcctRev.getBudgetReversionAccount())) {
                String budgetAccountSubFundGroupCode = globalAcctRev.getBudgetReversionAccount().getSubFundGroupCode();
                
                if (ruleValues.contains(budgetAccountSubFundGroupCode)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER, KFSKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_DENIED_VALUES_PARAMETER, new String[]{getDataDictionaryService().getAttributeLabel(SubFundGroup.class, KFSPropertyConstants.SUB_FUND_GROUP_CODE), budgetAccountSubFundGroupCode, getParameterAsStringForMessage(CUKFSConstants.Reversion.SELECTION_4), getParameterValuesForMessage(ruleValues), getDataDictionaryService().getAttributeLabel(AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER)});
                }
            }

            if (ObjectUtils.isNotNull(globalAcctRev.getCashReversionAccount())) {
                String cashAccountSubFundGroupCode = globalAcctRev.getCashReversionAccount().getSubFundGroupCode();

                if (ruleValues.contains(cashAccountSubFundGroupCode)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER, KFSKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_DENIED_VALUES_PARAMETER, new String[]{getDataDictionaryService().getAttributeLabel(SubFundGroup.class, KFSPropertyConstants.SUB_FUND_GROUP_CODE), cashAccountSubFundGroupCode, getParameterAsStringForMessage(CUKFSConstants.Reversion.SELECTION_4), getParameterValuesForMessage(ruleValues), getDataDictionaryService().getAttributeLabel(AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER)});
                }
            }
        }
        return valid;
    }

    /**
     * Returns a comma separated String of values
     * 
     * @param values
     * @return
     */
    public String getParameterValuesForMessage(Collection<String> values) {
        StringBuilder result = new StringBuilder();
        if (ObjectUtils.isNotNull(values) && values.size() > 0) {
            for (String value : values) {
                result.append(value);
                result.append(",");
            }
            result.replace(result.lastIndexOf(","), result.length(), KFSConstants.EMPTY_STRING);
        }

        return result.toString();
    }

    /**
     * Returns a String containing information about the given selection system parameter.
     * 
     * @return a String
     */
    private String getParameterAsStringForMessage(String selectionParamName) {
        return new StringBuilder("parameter: ").append(selectionParamName).append(", module: ").append("KFS-COA").append(", component: ").append("Reversion").toString();
    }

    public void setAccountReversionService(AccountReversionService accountReversionService) {
        this.accountReversionService = accountReversionService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    protected AccountReversionGlobal getGlobalAccountReversion() {
        return this.globalAccountReversion;
    }
}

