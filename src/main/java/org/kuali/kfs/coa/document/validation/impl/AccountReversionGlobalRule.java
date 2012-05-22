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
package org.kuali.kfs.coa.document.validation.impl;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobal;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobalAccount;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobalDetail;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.options.ReversionCodeValuesFinder;
import org.kuali.kfs.coa.document.AccountReversionGlobalMaintainableImpl;
import org.kuali.kfs.coa.service.AccountReversionService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.bo.PersistableBusinessObject;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;

/**
 * 
 * This class implements the business rules for {@link AccountReversionGlobal}
 */
public class AccountReversionGlobalRule extends GlobalDocumentRuleBase {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionGlobalRule.class);
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
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#setupConvenienceObjects()
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
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
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
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomApproveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
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
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        return checkSimpleRules(getGlobalAccountReversion());
    }

    /**
     * This performs rules checks whenever a new {@link AccountReversionGlobalDetail} or {@link AccountReversionGlobalOrganization} is added
     * <p>
     * This includes:
     * <ul>
     * <li>{@link AccountReversionGlobalRule#checkDetailObjectCodeValidity(AccountReversionGlobal, AccountReversionGlobalDetail)}</li>
     * <li>{@link AccountReversionGlobalRule#checkDetailObjectReversionCodeValidity(AccountReversionGlobalDetail)}</li>
     * <li>ensure that the chart of accounts code and organization codes for {@link AccountReversionGlobalOrganization} are not empty values</li>
     * <li>{@link AccountReversionGlobalRule#checkAllObjectCodesForValidity(AccountReversionGlobal, AccountReversionGlobalOrganization)}</li>
     * <li>{@link AccountReversionGlobalRule#checkOrganizationChartValidity(AccountReversionGlobalOrganization)</li>
     * <li>{@link AccountReversionGlobalRule#checkOrganizationValidity(AccountReversionGlobalOrganization)</li>
     * <li>{@link AccountReversionGlobalRule#checkAccountReversionForOrganizationExists(AccountReversionGlobal, AccountReversionGlobalOrganization)</li>
     * <li>{@link AccountReversionGlobalRule#checkOrganizationIsNotAmongOrgRevOrganizations(AccountReversionGlobal, AccountReversionGlobalOrganization)</li>
     * </ul>
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument,
     *      java.lang.String, org.kuali.rice.kns.bo.PersistableBusinessObject)
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
            if (!checkEmptyValue(acct.getChartOfAccountsCode())) {
                GlobalVariables.getMessageMap().putError("chartOfAccountsCode", KFSKeyConstants.ERROR_REQUIRED, "Chart of Accounts Code");
            }
            if (!checkEmptyValue(acct.getAccountNumber())) {
                GlobalVariables.getMessageMap().putError("accountNumber", KFSKeyConstants.ERROR_REQUIRED, "Account Number");
            }
            if (success) {
                success &= checkAllObjectCodesForValidity(globalAcctRev, acct);
                success &= checkAccountChartValidity(acct);
                success &= checkAccountValidity(acct);
                success &= checkAccountReversionForAccountExists(globalAcctRev, acct);
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

        success &= areAllDetailsValid(globalAcctRev);
        success &= areAllAccountsValid(globalAcctRev);

        return success;
    }

    /**
     * This method makes sure that if one part of the Budget Reversion Chart/Account pair is specified, both are specified, or an
     * error is thrown.
     * 
     * @param globalOrgRev the Global Organization Reversion to check
     * @return true if budget reversion chart/account pair is specified correctly, false if otherwise
     */
    public boolean checkBudgetReversionAccountPair(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        if ((!StringUtils.isBlank(globalAcctRev.getBudgetReversionChartOfAccountsCode()) && StringUtils.isBlank(globalAcctRev.getBudgetReversionAccountNumber())) || (StringUtils.isBlank(globalAcctRev.getBudgetReversionChartOfAccountsCode()) && !StringUtils.isBlank(globalAcctRev.getBudgetReversionAccountNumber()))) {
            success = false;
            GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + "budgetReversionChartOfAccountsCode", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_BUDGET_REVERSION_INCOMPLETE, new String[] {});
        }
        return success;
    }

    /**
     * This method makes sure that if one part of the Cash Reversion Chart/Account pair is specified, both are specified, or an
     * error is thrown.
     * 
     * @param globalOrgRev the Global Organization Reversion to check
     * @return true if cash reversion chart/account pair is specified correctly, false if otherwise
     */
    public boolean checkCashReversionAccountPair(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        if ((!StringUtils.isBlank(globalAcctRev.getCashReversionFinancialChartOfAccountsCode()) && StringUtils.isBlank(globalAcctRev.getCashReversionAccountNumber())) || (StringUtils.isBlank(globalAcctRev.getCashReversionFinancialChartOfAccountsCode()) && !StringUtils.isBlank(globalAcctRev.getCashReversionAccountNumber()))) {
            success = false;
            GlobalVariables.getMessageMap().putError(MAINTAINABLE_ERROR_PREFIX + "cashReversionFinancialChartOfAccountsCode", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_CASH_REVERSION_INCOMPLETE, new String[] {});
        }
        return success;
    }

    /**
     * Tests if all of the {@link AccountReversionGlobalDetail} objects associated with the given global organization reversion are
     * valid.
     * 
     * @param globalOrgRev the global organization reversion to check
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
     * Tests if the Organization Reversion Category existed in the database and was active.
     * 
     * @param detail AccountReversionGlobalDetail to check
     * @return true if the category is valid, false if otherwise
     */
    public boolean checkDetailAcctReversionCategoryValidity(AccountReversionGlobalDetail detail) {
        boolean success = true;
        if (StringUtils.isBlank(detail.getAccountReversionCategoryCode())) {
            success = false;
            GlobalVariables.getMessageMap().putError("accountReversionCategoryCode", KFSKeyConstants.ERROR_REQUIRED, new String[] {});
        }
        else {
            detail.refreshReferenceObject("reversionCategory");
            if (detail.getReversionCategory() == null || !detail.getReversionCategory().isActive()) {
                success = false;
                GlobalVariables.getMessageMap().putError("organizationReversionCategoryCode", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_ORG_REVERSION_CATEGORY, new String[] { detail.getAccountReversionCategoryCode() });
            }
        }
        return success;
    }

    /**
     * For each organization, tests if the object code in the detail exists in the system and is active
     * 
     * @param detail the AccountReversionGlobalDetail to check
     * @return true if it is valid, false if otherwise
     */
    public boolean checkDetailObjectCodeValidity(AccountReversionGlobal globalAcctRev, AccountReversionGlobalDetail detail) {
        boolean success = true;
        for (AccountReversionGlobalAccount acct : globalAcctRev.getAccountReversionGlobalAccounts()) {
            if (!validObjectCode(globalAcctRev.getUniversityFiscalYear(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode())) {
                success = false;
                GlobalVariables.getMessageMap().putError("accountReversionObjectCode", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_OBJECT_CODE_INVALID, new String[] { globalAcctRev.getUniversityFiscalYear().toString(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode(), acct.getChartOfAccountsCode(), acct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * This method loops through each of the AccountReversionGlobalDetail objects, checking that the entered object codes for
     * each of them are compatible with the AccountReversionGlobalOrganization specified.
     * 
     * @param globalOrgRev the global organization reversion to check
     * @param org the AccountReversionGlobalOrganization with a new chart to check against all of the object codes
     * @return true if there are no conflicts, false if otherwise
     */
    public boolean checkAllObjectCodesForValidity(AccountReversionGlobal globalAcctRev, AccountReversionGlobalAccount acct) {
        boolean success = true;
        for (AccountReversionGlobalDetail detail : globalAcctRev.getAccountReversionGlobalDetails()) {
            if (!validObjectCode(globalAcctRev.getUniversityFiscalYear(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode())) {
                success = false;
                GlobalVariables.getMessageMap().putError("accountNumber", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_OBJECT_CODE_INVALID, new String[] { globalAcctRev.getUniversityFiscalYear().toString(), acct.getChartOfAccountsCode(), detail.getAccountReversionObjectCode(), acct.getChartOfAccountsCode(), acct.getAccountNumber() });
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
            // TODO Dude!! The *only* place that the org reversion code values are defined
            // is in the lookup class, so I've got to use a web-based class to actually
            // search through the values. Is that right good & healthy?
            for (Object kvPairObj : new ReversionCodeValuesFinder().getKeyValues()) {
                KeyLabelPair kvPair = (KeyLabelPair) kvPairObj;
                if (kvPair.getKey().toString().equals(detail.getAccountReversionCode())) {
                    foundInList = true;
                    break;
                }
            }
            if (!foundInList) {
                success = false; // we've failed to find the code in the list...FAILED!
                GlobalVariables.getMessageMap().putError("accountReversionCode", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_ORG_REVERSION_CODE, new String[] { detail.getAccountReversionCode() });
            }
        }
        return success;
    }

    /**
     * This method tests if all the AccountReversionGlobalOrganization objects associated with the given global organization
     * reversion pass all of their tests.
     * 
     * @param globalOrgRev the global organization reversion to check
     * @return true if valid, false otherwise
     */
    public boolean areAllAccountsValid(AccountReversionGlobal globalAcctRev) {
        boolean success = true;
        if (globalAcctRev.getAccountReversionGlobalAccounts().size() == 0) {
            putFieldError(KFSConstants.MAINTENANCE_ADD_PREFIX + "accountReversionGlobalAccounts.organizationCode", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_NO_ORGANIZATIONS);
        }
        else {
            for (int i = 0; i < globalAcctRev.getAccountReversionGlobalAccounts().size(); i++) {
                AccountReversionGlobalAccount acct = globalAcctRev.getAccountReversionGlobalAccounts().get(i);
                String errorPath = MAINTAINABLE_ERROR_PREFIX + "accountReversionGlobalAccounts[" + i + "]";
                GlobalVariables.getMessageMap().addToErrorPath(errorPath);
                success &= checkAllObjectCodesForValidity(globalAcctRev, acct);
                success &= checkAccountValidity(acct);
                success &= checkAccountChartValidity(acct);
                success &= checkAccountReversionForAccountExists(globalAcctRev, acct);
                GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
            }
        }
        return success;
    }

    /**
     * Tests if the the organization of the given AccountReversionGlobalOrganization is within the chart of the global
     * organization reversion as a whole.
     * 
     * @param globalOrgRev the global organization reversion that is currently being validated.
     * @param org the AccountReversionGlobalOrganization to check
     * @return true if valid, false otherwise
     */
    public boolean checkAccountChartValidity(AccountReversionGlobalAccount acct) {
        boolean success = true;
        if (StringUtils.isBlank(acct.getChartOfAccountsCode())) {
            if (!StringUtils.isBlank(acct.getAccountNumber())) {
                success = false;
                GlobalVariables.getMessageMap().putError("chartOfAccountsCode", KFSKeyConstants.ERROR_REQUIRED, new String[] {});
            }
        }
        else {
            acct.setChartOfAccountsCode(acct.getChartOfAccountsCode().toUpperCase());
            acct.refreshReferenceObject("chartOfAccounts");
            if (acct.getChartOfAccounts() == null) {
                success = false;
                GlobalVariables.getMessageMap().putError("chartOfAccountsCode", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_CHART, new String[] { acct.getChartOfAccountsCode() });
            }
        }
        return success;
    }

    /**
     * Tests if the given AccountReversionGlobalOrganization's Organization is active and within the system.
     * 
     * @param org the AccountReversionGlobalOrganization to check
     * @return true if valid, false otherwise
     */
    public boolean checkAccountValidity(AccountReversionGlobalAccount acct) {
        boolean success = true;
        if (StringUtils.isBlank(acct.getAccountNumber())) {
            if (!StringUtils.isBlank(acct.getChartOfAccountsCode())) {
                success = false;
                GlobalVariables.getMessageMap().putError("accountNumber", KFSKeyConstants.ERROR_REQUIRED, new String[] {});
            }
        }
        else if (!StringUtils.isBlank(acct.getChartOfAccountsCode())) {
            acct.refreshReferenceObject("account");
            if (acct.getAccount() == null) {
                success = false;
                GlobalVariables.getMessageMap().putError("accountNumber", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_ORGANIZATION, new String[] { acct.getChartOfAccountsCode(), acct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * Checks that an organization reversion for the given organization reversion change and organization reversion change
     * organization exist.
     * 
     * @param globalOrgRev global Organization Reversion to check
     * @param org organization within that Global Organization Reversion to check specifically
     * @return true if organization reversion for organization exists, false if otherwise
     */
    public boolean checkAccountReversionForAccountExists(AccountReversionGlobal globalAcctRev, AccountReversionGlobalAccount acct) {
        boolean success = true;
        if (globalAcctRev.getUniversityFiscalYear() != null) {
            if (accountReversionService.getByPrimaryId(globalAcctRev.getUniversityFiscalYear(), acct.getChartOfAccountsCode(), acct.getAccountNumber()) == null) {
                success = false;
                GlobalVariables.getMessageMap().putError("accountNumber", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_NO_ORG_REVERSION, new String[] { globalAcctRev.getUniversityFiscalYear().toString(), acct.getChartOfAccountsCode(), acct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * This method checks if a newly added organization is already among the organizations already listed. WARNING: only use on add
     * line rules; there's no good way to use this method when testing the entire document.
     * 
     * @param globalOrgRev the global Organization Reversion to check
     * @param orgRevOrg the newly adding organization reversion change organization
     * @return true if organization should be added as it is not currently in the collection, false if otherwise
     */
    public boolean checkAccountIsNotAmongAcctRevAccounts(AccountReversionGlobal globalAcctRev, AccountReversionGlobalAccount acctRevAcct) {
        boolean success = true;
        Iterator<AccountReversionGlobalAccount> iter = globalAcctRev.getAccountReversionGlobalAccounts().iterator();
        while (iter.hasNext() && success) {
            AccountReversionGlobalAccount currAcct = iter.next();
            if (areContainingSameAccounts(currAcct, acctRevAcct)) {
                success = false;
                GlobalVariables.getMessageMap().putError("accountNumber", KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_DUPLICATE_ORGS, new String[] { acctRevAcct.getChartOfAccountsCode(), acctRevAcct.getAccountNumber() });
            }
        }
        return success;
    }

    /**
     * This method tests if two AccountReversionGlobalOrganization objects are holding the same underlying Organization.
     * 
     * @param orgRevOrgA the first AccountReversionGlobalOrganization to check
     * @param orgRevOrgB the second AccountReversionGlobalOrganization to check
     * @return true if they share the organization, false if otherwise
     */
    public static boolean areContainingSameAccounts(AccountReversionGlobalAccount acctRevAcctA, AccountReversionGlobalAccount acctRevAcctB) {
        boolean containingSame = false;
        if (acctRevAcctA.getChartOfAccountsCode() != null && acctRevAcctB.getChartOfAccountsCode() != null && acctRevAcctA.getAccountNumber() != null && acctRevAcctB.getAccountNumber() != null) {
            containingSame = (acctRevAcctA.getChartOfAccountsCode().equals(acctRevAcctB.getChartOfAccountsCode()) && acctRevAcctA.getAccountNumber().equals(acctRevAcctB.getAccountNumber()));
        }
        return containingSame;
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
