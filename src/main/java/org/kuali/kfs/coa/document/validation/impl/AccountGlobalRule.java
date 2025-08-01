/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.coa.document.validation.impl;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.CuAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;
import edu.cornell.kfs.coa.document.validation.impl.GlobalIndirectCostRecoveryAccountsRule;
import edu.cornell.kfs.coa.service.GlobalObjectWithIndirectCostRecoveryAccountsService;
import edu.cornell.kfs.coa.CuCOAKeyConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAConstants;
import org.kuali.kfs.coa.COAKeyConstants;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.coa.service.SubFundGroupService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import org.kuali.kfs.gl.service.BalanceService;
import org.kuali.kfs.gl.service.EncumbranceService;
import org.kuali.kfs.module.cg.service.ContractsAndGrantsService;
import org.kuali.kfs.module.ld.service.LaborLedgerPendingEntryService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.service.DictionaryValidationService;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.CGConstants;
import org.kuali.kfs.module.cg.businessobject.CFDA;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObject;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class represents the business rules for the maintenance of {@link AccountGlobal} business objects
 */
public class AccountGlobalRule extends GlobalIndirectCostRecoveryAccountsRule {
	private static final Logger LOG = LogManager.getLogger();

	protected static final BigDecimal BD100 = new BigDecimal(100);

	private static final String WHEN_FUND_PREFIX = "When Fund Group Code is ";
	private static final String AND_SUB_FUND = " and Sub-Fund Group Code is ";

	protected CuAccountGlobal newAccountGlobal;
	private DateTimeService dateTimeService;
	private LocalDate today;
	
	protected EncumbranceService encumbranceService;

	protected GeneralLedgerPendingEntryService generalLedgerPendingEntryService;
	protected BalanceService balanceService;
	protected AccountService accountService;
	protected static SubFundGroupService subFundGroupService;
	protected static ParameterService parameterService;
	protected ContractsAndGrantsService contractsAndGrantsService;

	public AccountGlobalRule() {
		this.setGeneralLedgerPendingEntryService(SpringContext.getBean(GeneralLedgerPendingEntryService.class));
		this.setBalanceService(SpringContext.getBean(BalanceService.class));
		this.setAccountService(SpringContext.getBean(AccountService.class));
		this.setContractsAndGrantsService(SpringContext.getBean(ContractsAndGrantsService.class));
	}

	/**
	 * This method sets the convenience objects like newAccountGlobal and oldAccount, so you have short and easy handles to the new
	 * and old objects contained in the maintenance document. It also calls the BusinessObjectBase.refresh(), which will attempt to
	 * load all sub-objects from the DB by their primary keys, if available.
	 */
	@Override
	public void setupConvenienceObjects() {
	    newAccountGlobal = (CuAccountGlobal) super.getNewBo();
	    today = LocalDate.now();
	}

    /**
     * This method checks the following rules: checkEmptyValues checkGeneralRules checkContractsAndGrants checkExpirationDate
     * checkOnlyOneChartErrorWrapper checkFiscalOfficerIsValidKualiUser but does not fail if any of them fail (this only happens on
     * routing)
     */
    @Override
    protected boolean processCustomSaveDocumentBusinessRules(final MaintenanceDocument document) {

        LOG.info("processCustomSaveDocumentBusinessRules called");
        setupConvenienceObjects();
        super.processCustomSaveDocumentBusinessRules(document);

        checkRemoveExpirationDate();
        checkRemoveContinuationChartAndAccount();
        checkRemoveIncomeStreamChartAndAccount();

        checkEmptyValues();
        checkGeneralRules(document);
        checkCloseAccounts();
        checkOrganizationValidity(newAccountGlobal);
        checkContractsAndGrants();
        checkExpirationDate(document);
        checkOnlyOneChartErrorWrapper(newAccountGlobal.getAccountGlobalDetails());
        checkSubFundProgram(document);
        checkAppropriationAccount(document);
        checkOpenEncumbrances();

        // Save always succeeds, even if there are business rule failures
        return true;
    }

    /**
     * This method checks the following rules: checkEmptyValues checkGeneralRules checkContractsAndGrants checkExpirationDate
     * checkOnlyOneChartErrorWrapper checkFiscalOfficerIsValidKualiUser but does fail if any of these rule checks fail
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(final MaintenanceDocument document) {
        LOG.info("processCustomRouteDocumentBusinessRules called");
        setupConvenienceObjects();

        boolean success = super.processCustomRouteDocumentBusinessRules(document);

        success &= checkRemoveExpirationDate();
        success &= checkRemoveContinuationChartAndAccount();
        success &= checkRemoveIncomeStreamChartAndAccount();

        success &= checkEmptyValues();
        success &= checkGeneralRules(document);
        success &= checkCloseAccounts();
        success &= checkContractsAndGrants();

        success &= checkExpirationDate(document);
        success &= checkAccountDetails(document, newAccountGlobal.getAccountGlobalDetails());
        success &= checkSubFundProgram(document);
        success &= checkAppropriationAccount(document);
        success &= checkOpenEncumbrances();

        return success;
    }

    /**
     * This method loops through the list of {@link AccountGlobalDetail}s and passes them off to checkAccountDetails for further
     * rule analysis One rule it does check is checkOnlyOneChartErrorWrapper
     *
     * @param document
     * @param details
     * @return true if the collection of {@link AccountGlobalDetail}s passes the sub-rules
     */
    public boolean checkAccountDetails(final MaintenanceDocument document, final List<AccountGlobalDetail> details) {
        boolean success = true;

        // check if there are any accounts
        if (details.size() == 0) {
                
                        putFieldError(KFSConstants.MAINTENANCE_ADD_PREFIX + "accountGlobalDetails.accountNumber",
                        COAKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_NO_ACCOUNTS);
                success = false;
        }
        else {
            // check each account
            int index = 0;
            for (final AccountGlobalDetail dtl : details) {
                final String errorPath = MAINTAINABLE_ERROR_PREFIX + KFSPropertyConstants.ACCOUNT_CHANGE_DETAILS + "[" + index + "]";
                GlobalVariables.getMessageMap().addToErrorPath(errorPath);
                success &= checkAccountDetails(dtl);
                success &= checkAccountExtensions(dtl);
                GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
                index++;
            }
            success &= checkOnlyOneChartErrorWrapper(details);
        }

        return success;
    }

    /**
     * This method ensures that each {@link AccountGlobalDetail} is valid and has a valid account number
     *
     * @param dtl
     * @return true if the detail object contains a valid account
     */
    public boolean checkAccountDetails(final AccountGlobalDetail dtl) {
        int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        getDictionaryValidationService().validateBusinessObject(dtl);
        if (StringUtils.isNotBlank(dtl.getAccountNumber()) && StringUtils.isNotBlank(dtl.getChartOfAccountsCode())) {
            dtl.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            if (ObjectUtils.isNull(dtl.getAccount())) {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, 
                        COAKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_INVALID_ACCOUNT, dtl.getChartOfAccountsCode(),
                        dtl.getAccountNumber());
            }
        }
        return GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;
    }

    /**
     * This method checks the basic rules for empty reference key values on a continuation account and an income
     * stream account
     *
     * @return true if no empty values or partially filled out reference keys
     */
    protected boolean checkEmptyValues() {
        LOG.info("checkEmptyValues called");

        // this set confirms that all fields which are grouped (ie, foreign keys of a reference
        // object), must either be none filled out, or all filled out.
        boolean success = checkForPartiallyFilledOutReferenceForeignKeys("continuationAccount");
        success &= checkForPartiallyFilledOutReferenceForeignKeys(KFSPropertyConstants.INCOME_STREAM_ACCOUNT);
        success &= checkForPartiallyFilledOutReferenceForeignKeys(KFSPropertyConstants.ENDOWMENT_INCOME_ACCOUNT);
        success &= checkForPartiallyFilledOutReferenceForeignKeys(KFSPropertyConstants.REPORTS_TO_ACCOUNT);
        success &= checkForPartiallyFilledOutReferenceForeignKeys(KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT);

        return success;
    }

    /**
     * This method checks some of the general business rules associated with this document Such as: valid user for fiscal officer,
     * supervisor or account manager (and not the same individual) are they trying to use an expired continuation account
     *
     * @param maintenanceDocument
     * @return false on rules violation
     */
    protected boolean checkGeneralRules(final MaintenanceDocument maintenanceDocument) {
        LOG.info("checkGeneralRules called");
        final Person fiscalOfficer = newAccountGlobal.getAccountFiscalOfficerUser();
        final Person accountManager = newAccountGlobal.getAccountManagerUser();
        final Person accountSupervisor = newAccountGlobal.getAccountSupervisoryUser();

        boolean success = true;

        if (isNonSystemSupervisorEditingAClosedAccount(maintenanceDocument, GlobalVariables.getUserSession().getPerson())) {
            success &= false;
            putFieldError(KFSPropertyConstants.ACCOUNT_CHANGE_DETAILS, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ONLY_SUPERVISORS_CAN_EDIT);
        }

        if (StringUtils.isNotBlank(newAccountGlobal.getAccountFiscalOfficerSystemIdentifier()) && (ObjectUtils.isNull(fiscalOfficer) || StringUtils.isEmpty(fiscalOfficer.getPrincipalId()) || !getDocumentHelperService().getDocumentAuthorizer(maintenanceDocument).isAuthorized(maintenanceDocument, KFSConstants.PermissionNames.SERVE_AS_FISCAL_OFFICER.namespace, KFSConstants.PermissionNames.SERVE_AS_FISCAL_OFFICER.name, fiscalOfficer.getPrincipalId()))) {
            final String fiscalOfficerName = fiscalOfficer != null ? fiscalOfficer.getName() : newAccountGlobal.getAccountFiscalOfficerSystemIdentifier();
            super.putFieldError(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_USER + "." + KFSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER, KFSKeyConstants.ERROR_USER_MISSING_PERMISSION, new String[] {fiscalOfficerName, KFSConstants.PermissionNames.SERVE_AS_FISCAL_OFFICER.namespace, KFSConstants.PermissionNames.SERVE_AS_FISCAL_OFFICER.name});
            success = false;
        } else if (ObjectUtils.isNotNull(fiscalOfficer) && StringUtils.isNotBlank(fiscalOfficer.getPrincipalName())
                && ObjectUtils.isNull(fiscalOfficer.getPrincipalId())) {
            super.putFieldError(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_USER + "." + KFSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER, COAKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_PRINCIPAL_NAME_FISCAL_OFFICER_SUPER_INVALID);
            success = false;
        }

        if (StringUtils.isNotBlank(newAccountGlobal.getAccountsSupervisorySystemsIdentifier()) && (ObjectUtils.isNull(accountSupervisor) || StringUtils.isEmpty(accountSupervisor.getPrincipalId()) || !getDocumentHelperService().getDocumentAuthorizer(maintenanceDocument).isAuthorized(maintenanceDocument, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_SUPERVISOR.namespace, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_SUPERVISOR.name, accountSupervisor.getPrincipalId()))) {
            final String accountSupervisorName = accountSupervisor != null ? accountSupervisor.getName() : newAccountGlobal.getAccountsSupervisorySystemsIdentifier();
            super.putFieldError(KFSPropertyConstants.ACCOUNT_SUPERVISORY_USER + "." + KFSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER, KFSKeyConstants.ERROR_USER_MISSING_PERMISSION, new String[] {accountSupervisorName, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_SUPERVISOR.namespace, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_SUPERVISOR.name});
            success = false;
        } else if (ObjectUtils.isNotNull(accountSupervisor) && StringUtils.isNotBlank(accountSupervisor.getPrincipalName()) && ObjectUtils.isNull(accountSupervisor.getPrincipalId())) {
            super.putFieldError(KFSPropertyConstants.ACCOUNT_SUPERVISORY_USER + "." + KFSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER, COAKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_PRINCIPAL_NAME_ACCOUNT_SUPER_INVALID);
            success = false;
        }
        if (StringUtils.isNotBlank(newAccountGlobal.getAccountManagerSystemIdentifier()) && (ObjectUtils.isNull(accountManager) || StringUtils.isEmpty(accountManager.getPrincipalId()) || !getDocumentHelperService().getDocumentAuthorizer(maintenanceDocument).isAuthorized(maintenanceDocument, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_MANAGER.namespace, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_MANAGER.name, accountManager.getPrincipalId()))) {
            final String accountManagerName = accountManager != null ? accountManager.getName() : newAccountGlobal.getAccountManagerSystemIdentifier();
            super.putFieldError(KFSPropertyConstants.ACCOUNT_MANAGER_USER + "." + KFSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER, KFSKeyConstants.ERROR_USER_MISSING_PERMISSION, new String[] {accountManagerName, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_MANAGER.namespace, KFSConstants.PermissionNames.SERVE_AS_ACCOUNT_MANAGER.name});
            success = false;
        } else if (ObjectUtils.isNotNull(accountManager) && StringUtils.isNotBlank(accountManager.getPrincipalName()) &&  ObjectUtils.isNull(accountManager.getPrincipalId())) {
            super.putFieldError(KFSPropertyConstants.ACCOUNT_MANAGER_USER + "." + KFSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER, COAKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_PRINCIPAL_NAME_ACCOUNT_MANAGER_INVALID);
            success = false;
        }

        // check FringeBenefit account rules
        success &= checkFringeBenefitAccountRule(newAccountGlobal);

        // the supervisor cannot be the same as the fiscal officer or account manager.
        if (isSupervisorSameAsFiscalOfficer(newAccountGlobal)) {
            success &= false;
            putFieldError(KFSPropertyConstants.ACCOUNTS_SUPERVISORY_SYSTEMS_IDENTIFIER, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_BE_FISCAL_OFFICER);
        }
        if (isSupervisorSameAsManager(newAccountGlobal)) {
            success &= false;
            putFieldError(KFSPropertyConstants.ACCOUNT_MANAGER_SYSTEM_IDENTIFIER, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_BE_ACCT_MGR);
        }

        // disallow continuation account being expired
        if (isContinuationAccountExpired(newAccountGlobal)) {
            success &= false;
            putFieldError(KFSPropertyConstants.CONTINUATION_ACCOUNT_NUMBER, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCOUNT_EXPIRED_CONTINUATION);
        }

        // loop over change detail objects to test if the supervisor/FO/mgr restrictions are in place
        // only need to do this check if the entered information does not already violate the rules
        if (!isSupervisorSameAsFiscalOfficer(newAccountGlobal) && !isSupervisorSameAsManager(newAccountGlobal)) {
            success &= checkAllAccountUsers(newAccountGlobal, fiscalOfficer, accountManager, accountSupervisor);
        }

        success &= checkCfda(  newAccountGlobal.getAccountCfdaNumber());

        return success;
    }

    protected boolean isNonSystemSupervisorEditingAClosedAccount(MaintenanceDocument document, Person user) {
        if(ObjectUtils.isNotNull(newAccountGlobal.getAccountGlobalDetails()) && newAccountGlobal.getAccountGlobalDetails().size() > 0){
            for(AccountGlobalDetail accountGlobalDetail : newAccountGlobal.getAccountGlobalDetails()){
                 accountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
                  if (accountGlobalDetail.getAccount().isClosed() ) {
                            return !getDocumentHelperService().getDocumentAuthorizer(document).isAuthorized(document, KFSConstants.PermissionNames.EDIT_INACTIVE_ACCOUNT.namespace, KFSConstants.PermissionNames.EDIT_INACTIVE_ACCOUNT.name, user.getPrincipalId());
                        }

              }
        }

        return false;
    }

    private boolean checkCfda(final String accountCfdaNumber) {
        boolean success = true;
        final CFDA cfda;
        if (StringUtils.isNotEmpty(accountCfdaNumber)) {
            cfda = getBoService().findBySinglePrimaryKey(CFDA.class, accountCfdaNumber);
            success = ObjectUtils.isNotNull(cfda);
            if (!success) {
                putFieldError("accountCfdaNumber", COAKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_CFDA_NUMBER_INVALID);
            }
        }
        return success;
    }

    /**
     * This method checks to make sure that if the users are filled out (fiscal officer, supervisor, manager) that they are not the
     * same individual Only need to check this if these are new users that override existing users on the {@link Account} object
     *
     * @param doc
     * @param newFiscalOfficer
     * @param newManager
     * @param newSupervisor
     * @return true if the users are either not changed or pass the sub-rules
     */
    protected boolean checkAllAccountUsers(final AccountGlobal doc, final Person newFiscalOfficer, final Person newManager, final Person newSupervisor) {
        boolean success = true;

        LOG.debug("newSupervisor: {}", newSupervisor);
        LOG.debug("newFiscalOfficer: {}", newFiscalOfficer);
        LOG.debug("newManager: {}", newManager);
        // only need to do this check if at least one of the user fields is non null
        if (newSupervisor != null || newFiscalOfficer != null || newManager != null) {
            // loop over all AccountGlobalDetail records
            int index = 0;
            for (final AccountGlobalDetail detail : doc.getAccountGlobalDetails()) {
                success &= checkAccountUsers(detail, newFiscalOfficer, newManager, newSupervisor, index);
                index++;
            }
        }

        return success;
    }

    /**
     * This method checks that the new users (fiscal officer, supervisor, manager) are not the same individual for the
     * {@link Account} being changed (contained in the {@link AccountGlobalDetail})
     *
     * @param detail           where the Account information is stored
     * @param newFiscalOfficer
     * @param newManager
     * @param newSupervisor
     * @param index            for storing the error line
     * @return true if the new users pass this sub-rule
     */
    protected boolean checkAccountUsers(
            final AccountGlobalDetail detail, final Person newFiscalOfficer, final Person newManager,
            final Person newSupervisor, final int index) {
        boolean success = true;

        // only need to do this check if at least one of the user fields is non null
        if (newSupervisor != null || newFiscalOfficer != null || newManager != null) {
            // loop over all AccountGlobalDetail records
            detail.refreshReferenceObject("account");
            final Account account = detail.getAccount();
            if (ObjectUtils.isNotNull(account)) {
                LOG.debug("old-Supervisor: {}", account::getAccountSupervisoryUser);
                LOG.debug("old-FiscalOfficer: {}", account::getAccountFiscalOfficerUser);
                LOG.debug("old-Manager: {}", account::getAccountManagerUser);
                // only need to check if they are not being overridden by the change document
                if (newSupervisor != null && newSupervisor.getPrincipalId() != null) {
                    if (areTwoUsersTheSame(newSupervisor, account.getAccountFiscalOfficerUser())) {
                        success = false;
                        putFieldError("accountGlobalDetails[" + index + "].accountNumber",
                                COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_EQUAL_EXISTING_FISCAL_OFFICER,
                                new String[]{
                                    account.getAccountFiscalOfficerUser().getPrincipalName(),
                                    "Fiscal Officer", detail.getAccountNumber()});
                    }
                    if (areTwoUsersTheSame(newSupervisor, account.getAccountManagerUser())) {
                        success = false;
                        putFieldError("accountGlobalDetails[" + index + "].accountNumber",
                                COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_EQUAL_EXISTING_ACCT_MGR,
                                new String[]{
                                    account.getAccountManagerUser().getPrincipalName(),
                                    "Account Manager", detail.getAccountNumber()});
                    }
                }
                if (newManager != null && newManager.getPrincipalId() != null) {
                    if (areTwoUsersTheSame(newManager, account.getAccountSupervisoryUser())) {
                        success = false;
                        putFieldError("accountGlobalDetails[" + index + "].accountNumber",
                                COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_MGR_CANNOT_EQUAL_EXISTING_ACCT_SUPERVISOR,
                                new String[]{
                                    account.getAccountSupervisoryUser().getPrincipalName(),
                                    "Account Supervisor", detail.getAccountNumber()});
                    }
                }
                if (newFiscalOfficer != null && newFiscalOfficer.getPrincipalId() != null) {
                    if (areTwoUsersTheSame(newFiscalOfficer, account.getAccountSupervisoryUser())) {
                        success = false;
                        putFieldError("accountGlobalDetails[" + index + "].accountNumber",
                                COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_FISCAL_OFFICER_CANNOT_EQUAL_EXISTING_ACCT_SUPERVISOR,
                                new String[]{
                                    account.getAccountSupervisoryUser().getPrincipalName(),
                                    "Account Supervisor", detail.getAccountNumber()});
                    }
                }
            } else {
                LOG.warn(
                        "AccountGlobalDetail object has null account object:{}-{}",
                        detail::getChartOfAccountsCode,
                        detail::getAccountNumber
                );
            }
        }

        return success;
    }

    /**
     * This method is a helper method for checking if the supervisor user is the same as the fiscal officer Calls
     * {@link AccountGlobalRule#areTwoUsersTheSame(Person, Person)}
     *
     * @param accountGlobals
     * @return true if the two users are the same
     */
    protected boolean isSupervisorSameAsFiscalOfficer(final AccountGlobal accountGlobals) {
        return areTwoUsersTheSame(accountGlobals.getAccountSupervisoryUser(), accountGlobals.getAccountFiscalOfficerUser());
    }

    /**
     * This method is a helper method for checking if the supervisor user is the same as the manager Calls
     * {@link AccountGlobalRule#areTwoUsersTheSame(Person, Person)}
     *
     * @param accountGlobals
     * @return true if the two users are the same
     */
    protected boolean isSupervisorSameAsManager(final AccountGlobal accountGlobals) {
        return areTwoUsersTheSame(accountGlobals.getAccountSupervisoryUser(), accountGlobals.getAccountManagerUser());
    }

    /**
     * This method checks to see if two users are the same Person using their identifiers
     *
     * @param user1
     * @param user2
     * @return true if these two users are the same
     */
    protected boolean areTwoUsersTheSame(final Person user1, final Person user2) {
        if (ObjectUtils.isNull(user1) || user1.getPrincipalId() == null) {
            return false;
        }
        if (ObjectUtils.isNull(user2) || user2.getPrincipalId() == null) {
            return false;
        }
        return user1.getPrincipalId().equals(user2.getPrincipalId());
    }

    /**
     * This method checks to see if any expiration date field rules were violated Loops through each detail object and calls
     * {@link AccountGlobalRule#checkExpirationDate(MaintenanceDocument, AccountGlobalDetail)}
     *
     * @param maintenanceDocument
     * @return false on rules violation
     */
    protected boolean checkExpirationDate(final MaintenanceDocument maintenanceDocument) {
        LOG.info("checkExpirationDate called");

        boolean success = true;
        final LocalDate newExpDate = getDateTimeService().getLocalDate(newAccountGlobal.getAccountExpirationDate());

        // If creating a new account if acct_expiration_dt is set then
        // the acct_expiration_dt must be changed to a date that is today or later
        // unless the date was valid upon submission, this is an approval action
        // and the approver hasn't changed the value
        if (maintenanceDocument.isNew() && ObjectUtils.isNotNull(newExpDate)) {
            LocalDate oldExpDate = null;

            if (maintenanceDocument.getDocumentHeader().getWorkflowDocument().isApprovalRequested()) {
                final MaintenanceDocument oldMaintDoc = (MaintenanceDocument) SpringContext.getBean(DocumentService.class)
                        .getByDocumentHeaderId(maintenanceDocument.getDocumentNumber());
                final AccountGlobal oldAccountGlobal = (AccountGlobal) oldMaintDoc.getDocumentBusinessObject();
                if (ObjectUtils.isNotNull(oldAccountGlobal)) {
                    oldExpDate = getDateTimeService().getLocalDate(oldAccountGlobal.getAccountExpirationDate());
                }
            }

            if (ObjectUtils.isNull(oldExpDate) || !oldExpDate.equals(newExpDate)) {
                // KFSUPGRADE-925 check parameter to see if back date is allowed
                Collection<String> fundGroups = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(COAConstants.COA_NAMESPACE_CODE, COAParameterConstants.Components.ACCOUNT_CMPNT, COAParameterConstants.EXPIRATION_BACKDATING_FUNDS);
                if (fundGroups == null || (ObjectUtils.isNotNull(newAccountGlobal.getSubFundGroup()) && !fundGroups.contains(newAccountGlobal.getSubFundGroup().getFundGroupCode()))) {
                    if (newExpDate.isBefore(today)) {
                        putFieldError("accountExpirationDate", COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_EXP_DATE_TODAY_LATER);
                        success = false;
                    }
                }
            }
        }

        // a continuation account is required if the expiration date is completed.
        success &= checkContinuationAccount(maintenanceDocument, getDateTimeService().getSqlDate(newExpDate));

        for (final AccountGlobalDetail detail : newAccountGlobal.getAccountGlobalDetails()) {
            success &= checkExpirationDate(maintenanceDocument, detail);
        }
        return success;
    }

    /**
     * This method checks to see if any expiration date field rules were violated in relation to the given detail record
     *
     * @param maintenanceDocument
     * @param detail              the account detail we are investigating
     * @return false on rules violation
     */
    protected boolean checkExpirationDate(final MaintenanceDocument maintenanceDocument, final AccountGlobalDetail detail) {
        boolean success = true;
        final LocalDate newExpDate = getDateTimeService().getLocalDate(newAccountGlobal.getAccountExpirationDate());

        LocalDate prevExpDate = null;

        // get previous expiration date for possible check later
        if (maintenanceDocument.getDocumentHeader().getWorkflowDocument().isApprovalRequested()) {
            final MaintenanceDocument oldMaintDoc = (MaintenanceDocument) SpringContext.getBean(DocumentService.class)
                    .getByDocumentHeaderId(maintenanceDocument.getDocumentNumber());
            final AccountGlobal oldAccountGlobal = (AccountGlobal) oldMaintDoc.getDocumentBusinessObject();
            if (ObjectUtils.isNotNull(oldAccountGlobal)) {
                prevExpDate = getDateTimeService().getLocalDate(oldAccountGlobal.getAccountExpirationDate());
            }
        }


        // load the object by keys
        final Account account = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Account.class, detail.getPrimaryKeys());
        if (ObjectUtils.isNotNull(account)) {
            LocalDate oldExpDate = getDateTimeService().getLocalDate(account.getAccountExpirationDate());

            // When updating an account expiration date, the date must be today or later
            // (except for C&G accounts). Only run this test if this maint doc is an edit doc
            if (isUpdatedExpirationDateInvalid(account, newAccountGlobal)) {
                // if the date was valid upon submission, and this is an approval,
                // we're not interested unless the approver changed the value
                if (ObjectUtils.isNull(prevExpDate) || !prevExpDate.equals(newExpDate)) {
                    if(newAccountGlobal.getClosed() !=null && newAccountGlobal.getClosed()){
                        /*If the Account is being closed and the date is before today's date, the EXP date can only be today*/
                        putFieldError(KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_CANNOT_BE_CLOSED_EXP_DATE_INVALID);
                        }
                    else{
                        /*If the Account is not being closed and the date is before today's date, the EXP date can only be today or at a later date*/
                        putFieldError(KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_EXP_DATE_TODAY_LATER);
                    }
                    success = false;
                }

            }

            // acct_expiration_dt can not be before acct_effect_dt
            LocalDate effectiveDate = null;
            if (ObjectUtils.isNotNull(newAccountGlobal.getAccountEffectiveDate())) {
                effectiveDate = getDateTimeService().getLocalDate(newAccountGlobal.getAccountEffectiveDate());
            } else {
                effectiveDate = getDateTimeService().getLocalDate(account.getAccountEffectiveDate());
            }

            if (ObjectUtils.isNotNull(effectiveDate) && ObjectUtils.isNotNull(newExpDate)) {
                if (newExpDate.isBefore(effectiveDate)) {
                    putFieldError(KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_EXP_DATE_CANNOT_BE_BEFORE_EFFECTIVE_DATE, new String[] { detail.getAccountNumber() });
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * This method checks to see if the updated expiration is not a valid one Only gets checked for specific {@link SubFundGroup}s
     *
     * @param oldAccount
     * @param newAccountGlobal
     * @return true if date has changed and is invalid
     */
    protected boolean isUpdatedExpirationDateInvalid(final Account oldAccount, final AccountGlobal newAccountGlobal) {

        final LocalDate oldExpDate = getDateTimeService().getLocalDate(oldAccount.getAccountExpirationDate());
        final LocalDate newExpDate = getDateTimeService().getLocalDate(newAccountGlobal.getAccountExpirationDate());

        // When updating an account expiration date, the date must be today or later
        // (except for C&G accounts). Only run this test if this maint doc is an edit doc
        boolean expDateHasChanged = false;

        // if the old version of the account had no expiration date, and the new one has a date
        if (ObjectUtils.isNull(oldExpDate) && ObjectUtils.isNotNull(newExpDate)) {
            expDateHasChanged = true;
        } else if (ObjectUtils.isNotNull(oldExpDate) && ObjectUtils.isNotNull(newExpDate)) {
            // if there was an old and a new expDate, but they're different
            if (!oldExpDate.equals(newExpDate)) {
                expDateHasChanged = true;
            }
        }

        // if the expiration date hasn't changed, we're not interested
        if (!expDateHasChanged) {
            return false;
        }

        // if a subFundGroup isn't present, we cannot continue the testing
        final SubFundGroup subFundGroup = newAccountGlobal.getSubFundGroup();
        if (ObjectUtils.isNull(subFundGroup)) {
            return false;
        }

        // if the account is part of the CG fund group, then this rule does not apply, so we're done
        if (SpringContext.getBean(SubFundGroupService.class).isForContractsAndGrants(newAccountGlobal.getSubFundGroup())) {
            return false;
        }

        // at this point, we know its not a CG fund group, so we must apply the rule

        // KFSUPGRADE-925
        Collection<String> fundGroups = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(COAConstants.COA_NAMESPACE_CODE, COAParameterConstants.Components.ACCOUNT_CMPNT,COAParameterConstants.EXPIRATION_BACKDATING_FUNDS);
        if (fundGroups != null && !ObjectUtils.isNull(newAccountGlobal.getSubFundGroup()) && fundGroups.contains(newAccountGlobal.getSubFundGroup().getFundGroupCode())) {
        		return false;
        }

        // expirationDate must be today or later than today (cannot be before today)
        return newExpDate.isBefore(today);
    }


    /**
     * This method tests whether the continuation account entered (if any) has expired or not.
     *
     * @param accountGlobals
     * @return true if the continuation account has expired
     */
    protected boolean isContinuationAccountExpired(final AccountGlobal accountGlobals) {
        final String chartCode = accountGlobals.getContinuationFinChrtOfAcctCd();
        final String accountNumber = accountGlobals.getContinuationAccountNumber();

        // if either chartCode or accountNumber is not entered, then we cant continue, so exit
        if (StringUtils.isBlank(chartCode) || StringUtils.isBlank(accountNumber)) {
            return false;
        }

        // attempt to retrieve the continuation account from the DB
        final Account continuation;
        final Map<String, String> pkMap = new HashMap<>();
        pkMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartCode);
        pkMap.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        continuation = super.getBoService().findByPrimaryKey(Account.class, pkMap);

        // if the object doesn't exist, then we cant continue, so exit
        if (ObjectUtils.isNull(continuation)) {
            return false;
        }

        // at this point, we have a valid continuation account, so we just need to know whether its expired or not
        return continuation.isExpired();
    }

    /**
     * This method checks to see if any Contracts and Grants business rules were violated
     *
     * @return false on rules violation
     */
    protected boolean checkContractsAndGrants() {
        LOG.info("checkContractsAndGrants called");

        boolean success = true;

        // Certain C&G fields are required if the Account belongs to the CG Fund Group
        success &= checkCgRequiredFields(newAccountGlobal);

        // Income Stream account is required based the fund/subfund group set up in income stream parameters
        success &= checkCgIncomeStreamRequired(newAccountGlobal);

        // check if the new account has a valid responsibility id
        if (ObjectUtils.isNotNull(newAccountGlobal.getContractsAndGrantsAccountResponsibilityId())) {
            Account tmpAcct = new Account();
            tmpAcct.setContractsAndGrantsAccountResponsibilityId(newAccountGlobal.getContractsAndGrantsAccountResponsibilityId());
            final boolean hasValidAccountResponsibility = hasValidAccountResponsibilityIdIfNotNull(tmpAcct);
            if (!hasValidAccountResponsibility) {
                success &= hasValidAccountResponsibility;
                putFieldError(CUKFSPropertyConstants.CONTRACTS_AND_GRANTS_ACCOUNT_RESPOSIBILITY_ID, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_INVALID_CG_RESPONSIBILITY , new String[] { newAccountGlobal.getContractsAndGrantsAccountResponsibilityId().toString() });
            }
        }

        return success;
    }
    
    private boolean hasValidAccountResponsibilityIdIfNotNull(final Account account) {
        final Integer accountResponsibilityId = account.getContractsAndGrantsAccountResponsibilityId();

        if (accountResponsibilityId == null) {
            return true;
        }

        return accountResponsibilityId >= 1 && accountResponsibilityId <= getMaximumAccountResponsibilityId();
    }
    
    private int getMaximumAccountResponsibilityId() {
        final String maxResponsibilityId = getParameterService().getParameterValueAsString(
                COAConstants.COA_NAMESPACE_CODE,
                CGConstants.Components.ACCOUNT_CMPNT,
                CGConstants.CG_RESPONSIBILITY_ID);
        return Integer.parseInt(maxResponsibilityId);
    }

    /**
     * This method checks to see if the contracts and grants income stream account is required
     *
     * @param accountGlobals
     * @return false if it is required (and not entered) or invalid/inactive
     */
    protected boolean checkCgIncomeStreamRequired(final AccountGlobal accountGlobals) {
        boolean required = false;

        // if the subFundGroup object is null, we cant test, so exit
        if (ObjectUtils.isNull(accountGlobals.getSubFundGroup())) {
            return true;
        }

        // retrieve the subfundcode and fundgroupcode
        final String subFundGroupCode = accountGlobals.getSubFundGroupCode().trim();
        final String fundGroupCode = accountGlobals.getSubFundGroup().getFundGroupCode().trim();

        // changed foundation code.  Now, it is using similar 'income stream account' validation rule for 'Account'
        if (isIncomeStreamAccountRequired(fundGroupCode, subFundGroupCode)) {
            required = true;
        }

        // if the income stream account is not required, then we're done
        if (!required) {
            return true;
        }

        if(newAccountGlobal.isRemoveIncomeStreamChartAndAccount()){
            putFieldError(CUKFSPropertyConstants.REMOVE_INCOME_STREAM_CHART_AND_ACCOUNT, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED_WHEN_INC_STR_REQ);
        }

        // make sure both coaCode and accountNumber are filled out
        String error_message_prefix =  WHEN_FUND_PREFIX + fundGroupCode + AND_SUB_FUND + subFundGroupCode;
        boolean result = checkEmptyBOField(KFSPropertyConstants.INCOME_STREAM_ACCOUNT_NUMBER, accountGlobals.getIncomeStreamAccountNumber(), error_message_prefix + ", Income Stream Account Number");
        result &= checkEmptyBOField(KFSPropertyConstants.INCOME_STREAM_CHART_OF_ACCOUNTS_CODE, accountGlobals.getIncomeStreamFinancialCoaCode(), error_message_prefix + ", Income Stream Chart Of Accounts Code");

        // if both fields aren't present, then we're done
        if (!result) {
            return false;
        }

        // do an existence/active test
        final DictionaryValidationService dvService = super.getDictionaryValidationService();
        final boolean referenceExists = dvService.validateReferenceExists(accountGlobals, KFSPropertyConstants.INCOME_STREAM_ACCOUNT);
        if (!referenceExists) {
            putFieldError("incomeStreamAccount", KFSKeyConstants.ERROR_EXISTENCE, "Income Stream Account: " +
                    accountGlobals.getIncomeStreamFinancialCoaCode() + "-" + accountGlobals.getIncomeStreamAccountNumber());
            result = false;
        }

        return result;
    }

    /**
     * This method calls checkAccountDetails checkExpirationDate checkOnlyOneChartAddLineErrorWrapper whenever a new
     * {@link AccountGlobalDetail} is added to this global
     */
    @Override
    public boolean processCustomAddCollectionLineBusinessRules(
            final MaintenanceDocument document, final String collectionName,
            final PersistableBusinessObject bo) {
        boolean success = super.processCustomAddCollectionLineBusinessRules(document, collectionName, bo);

        // this incoming bo needs to be refreshed because it doesn't have its subobjects setup
        bo.refreshNonUpdateableReferences();

        if(bo instanceof AccountGlobalDetail){
            final AccountGlobalDetail detail = (AccountGlobalDetail) bo;

            success &= checkAccountDetails(detail);
            success &= checkExpirationDate(document, detail);
            success &= checkOnlyOneChartAddLineErrorWrapper(detail, newAccountGlobal.getAccountGlobalDetails());
        }

        return success;
    }

    /**
     * This method validates that a continuation account is required and that the values provided exist
     *
     * @param document   An instance of the maintenance document being validated.
     * @param newExpDate The expiration date assigned to the account being validated for submission.
     * @return True if the continuation account values are valid for the associated account, false otherwise.
     */
    protected boolean checkContinuationAccount(final MaintenanceDocument document, final Date newExpDate) {
        LOG.info("checkContinuationAccount called");

        boolean continuationAccountIsValid = true;

        // make sure both coaCode and accountNumber are filled out
        if (ObjectUtils.isNotNull(newExpDate)) {
            if (!checkEmptyValue(newAccountGlobal.getContinuationAccountNumber())) {
                putFieldError("continuationAccountNumber",
                        COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_CONTINUATION_ACCT_REQD_IF_EXP_DATE_COMPLETED);
                continuationAccountIsValid = false;
            }
            if (!checkEmptyValue(newAccountGlobal.getContinuationFinChrtOfAcctCd())) {
                putFieldError("continuationFinChrtOfAcctCd",
                        COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_CONTINUATION_FIN_CODE_REQD_IF_EXP_DATE_COMPLETED);
                continuationAccountIsValid = false;
            }
        }

        // if both fields aren't present, then we're done
        if (continuationAccountIsValid && ObjectUtils.isNotNull(newAccountGlobal.getContinuationAccountNumber())
                && ObjectUtils.isNotNull(newAccountGlobal.getContinuationFinChrtOfAcctCd())) {
            // do an existence/active test
            final DictionaryValidationService dvService = super.getDictionaryValidationService();
            final boolean referenceExists = dvService.validateReferenceExists(newAccountGlobal, "continuationAccount");
            if (!referenceExists) {
                putFieldError("continuationAccountNumber", KFSKeyConstants.ERROR_EXISTENCE,
                        "Continuation Account: " + newAccountGlobal.getContinuationFinChrtOfAcctCd() + "-" +
                                newAccountGlobal.getContinuationAccountNumber());
                continuationAccountIsValid = false;
            }
        }

        boolean result = true;
        if (!continuationAccountIsValid) {
            final List<AccountGlobalDetail> gAcctDetails = newAccountGlobal.getAccountGlobalDetails();
            for (final AccountGlobalDetail detail : gAcctDetails) {
                if (null != detail.getAccountNumber() && null != newAccountGlobal.getContinuationAccountNumber()) {
                    result &= detail.getAccountNumber().equals(newAccountGlobal.getContinuationAccountNumber());
                    result &= detail.getChartOfAccountsCode().equals(newAccountGlobal.getContinuationFinChrtOfAcctCd());
                }
            }
        }

        return result;
    }

    /**
     * Validate that the object code on the form (if entered) is valid for all charts used in the detail sections.
     *
     * @param acctGlobal
     * @return
     */
    protected boolean checkOrganizationValidity(final AccountGlobal acctGlobal) {
        boolean result = true;

        // check that an org has been entered
        if (StringUtils.isNotBlank(acctGlobal.getOrganizationCode())) {
            // get all distinct charts
            final HashSet<String> charts = new HashSet<>(10);
            for (final AccountGlobalDetail acct : acctGlobal.getAccountGlobalDetails()) {
                charts.add(acct.getChartOfAccountsCode());
            }
            final OrganizationService orgService = SpringContext.getBean(OrganizationService.class);
            // test for an invalid organization
            for (final String chartCode : charts) {
                if (StringUtils.isNotBlank(chartCode)) {
                    if (orgService.getByPrimaryIdWithCaching(chartCode, acctGlobal.getOrganizationCode()) == null) {
                        result = false;
                        putFieldError("organizationCode",
                                COAKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_INVALID_ORG,
                                new String[]{chartCode, acctGlobal.getOrganizationCode()});
                        break;
                    }
                }
            }
        }

        return result;
    }

    /*
     * Check if the fund/subfund matched the income stream account required parameters.
     * CU changed this to match the income stream account requirement validation in 'AccountRule'.
     */
    private boolean isIncomeStreamAccountRequired(String fundGroupCode, String subFundGroupCode) {

        boolean required = false;

        if (StringUtils.isNotBlank(fundGroupCode) && StringUtils.isNotBlank(subFundGroupCode)) {
            if (SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(Account.class, COAParameterConstants.INCOME_STREAM_FUNDS, fundGroupCode).evaluationSucceeds()) {
                if (SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(Account.class, COAParameterConstants.INCOME_STREAM_SUB_FUNDS, subFundGroupCode).evaluationSucceeds()) {
                    required = true;
                }
            }

        }

        return required;
    }

    protected boolean checkSubFundProgram(MaintenanceDocument document) {
        boolean success = true;

        String subFundGroupCode = newAccountGlobal.getSubFundGroupCode();
        String subFundProg = newAccountGlobal.getProgramCode();
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);

        if (StringUtils.isNotBlank(subFundProg)) {
            Map<String, String> fieldValues = new HashMap<>();
            fieldValues.put(KFSPropertyConstants.SUB_FUND_GROUP_CODE, subFundGroupCode);
            fieldValues.put(CUKFSPropertyConstants.PROGRAM_CODE, subFundProg);

            Collection<SubFundProgram> retVals = bos.findMatching(SubFundProgram.class, fieldValues);

            if (retVals.isEmpty()) {
                success = false;
                putFieldError(CUKFSPropertyConstants.PROGRAM_CODE, CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_NOT_GROUP_CODE, new String[] {subFundProg, subFundGroupCode});
            } else {
                for (SubFundProgram subFundProgram : retVals) {
                    if (!subFundProgram.isActive()) {
                        putFieldError(CUKFSPropertyConstants.PROGRAM_CODE, KFSKeyConstants.ERROR_INACTIVE, getFieldLabel(Account.class, CUKFSPropertyConstants.PROGRAM_CODE));
                        success = false;
                        break;
                    }
                }
            }

        } else {
            Map<String, String> fieldValues = new HashMap<String, String>();
            fieldValues.put(KFSPropertyConstants.SUB_FUND_GROUP_CODE, subFundGroupCode);
            Collection<SubFundProgram> retVals = bos.findMatching(SubFundProgram.class, fieldValues);
            if (!retVals.isEmpty()) {
                success = false;
                putFieldError(CUKFSPropertyConstants.PROGRAM_CODE, CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_CANNOT_BE_BLANK_FOR_GROUP_CODE, new String[] { subFundGroupCode});
            }
        }
        return success;
    }

    protected boolean checkAppropriationAccount(MaintenanceDocument document) {
        boolean success = true;

        String subFundGroupCode = newAccountGlobal.getSubFundGroupCode();
        String appropriationAccountNumber = newAccountGlobal.getAppropriationAccountNumber();
        BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);

        if (StringUtils.isNotBlank(appropriationAccountNumber) && StringUtils.isNotBlank(subFundGroupCode)) {
            Map<String, String> fieldValues = new HashMap<String, String>();
            fieldValues.put(KFSPropertyConstants.SUB_FUND_GROUP_CODE, subFundGroupCode);
            fieldValues.put(CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER, appropriationAccountNumber);

            Collection<AppropriationAccount> retVals = businessObjectService.findMatching(AppropriationAccount.class, fieldValues);

            if (retVals.isEmpty()) {
                success = false;
                putFieldError(CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_APPROP_ACCT_NOT_GROUP_CODE,
                        new String[] {appropriationAccountNumber, subFundGroupCode});
            } else {
                for (AppropriationAccount appropriationAccount : retVals) {
                    if (!appropriationAccount.isActive()) {
                        putFieldError(CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER, KFSKeyConstants.ERROR_INACTIVE,
                                getFieldLabel(AccountGlobal.class, CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER));
                        success = false;
                        break;
                    }
                }
            }
        }
        return success;
    }

    private boolean checkAccountExtensions(AccountGlobalDetail dtl) {
        boolean success = true;
        String subFundGroupCode = newAccountGlobal.getSubFundGroupCode();
        String appropriationAccountNumber = newAccountGlobal.getAppropriationAccountNumber();
        String subFundProg =  newAccountGlobal.getProgramCode();
        dtl.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
        if (ObjectUtils.isNotNull(dtl.getAccount())) {
            success &= checkAccountExtensionProgramCd(dtl.getAccount(), subFundGroupCode, subFundProg);
            success &= checkAccountExtensionApprAcct(dtl.getAccount(), subFundGroupCode, appropriationAccountNumber);
        }
        return success;
    }


    private boolean checkAccountExtensionProgramCd(Account account, String subFundGroupCode, String subFundProg) {
        boolean success = true;
        if (StringUtils.isBlank(subFundGroupCode)) {
            if (StringUtils.isNotBlank(subFundProg)) {
                SubFundProgram subFundProgram = getMatchedRecord(SubFundProgram.class, account.getSubFundGroupCode(), CUKFSPropertyConstants.PROGRAM_CODE, subFundProg);
                if (subFundProgram == null) {
                    success = false;
                    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCT_PROGRAM_CODE_NOT_GROUP_CODE, new String[] {subFundProg, account.getSubFundGroupCode(), account.getAccountNumber()});
                } else {
                    if (!subFundProgram.isActive()) {
                        putFieldError(CUKFSPropertyConstants.PROGRAM_CODE, KFSKeyConstants.ERROR_INACTIVE, getFieldLabel(Account.class, CUKFSPropertyConstants.PROGRAM_CODE));
                        success = false;
                    }
                }
            }
        } else {
            AccountExtendedAttribute accountExtension = (AccountExtendedAttribute)account.getExtension();
            if (StringUtils.isBlank(subFundProg)) {
                if (StringUtils.isBlank(accountExtension.getProgramCode())) {
                    SubFundProgram subFundProgram = getMatchedRecord(SubFundProgram.class, subFundGroupCode, CUKFSPropertyConstants.PROGRAM_CODE, accountExtension.getProgramCode());
                    if (subFundProgram != null) {
                        success = false;
                        GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCT_PROGRAM_CODE_CANNOT_BE_BLANK_FOR_GROUP_CODE, new String[] { subFundGroupCode, account.getAccountNumber()});
                    }         
                } else {
                    SubFundProgram subFundProgram = getMatchedRecord(SubFundProgram.class, subFundGroupCode, CUKFSPropertyConstants.PROGRAM_CODE, accountExtension.getProgramCode());
                    if (subFundProgram == null) {
                        success = false;
                        GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCT_PROGRAM_CODE_NOT_GROUP_CODE, new String[]
                                {accountExtension.getProgramCode(), subFundGroupCode, account.getAccountNumber()});
                    }
                }
            }
        }
        return success;
    }

    private boolean checkAccountExtensionApprAcct(Account account, String subFundGroupCode, String appropriationAccountNumber) {
        boolean success = true;
        if (StringUtils.isBlank(subFundGroupCode)) {
            if (StringUtils.isNotBlank(appropriationAccountNumber)) {
                AppropriationAccount appropriationAcct = getMatchedRecord(AppropriationAccount.class, account.getSubFundGroupCode(), CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER, appropriationAccountNumber);
                if (appropriationAcct == null) {
                    success = false;
                    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_APPROP_ACCT_NOT_GROUP_CODE,
                            new String[] {appropriationAccountNumber, account.getSubFundGroupCode(), account.getAccountNumber()});
                } else {
                    if (!appropriationAcct.isActive()) {
                        putFieldError(CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER, KFSKeyConstants.ERROR_INACTIVE,
                                getFieldLabel(AccountGlobal.class, CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER));
                        success = false;
                    }
                }
            }
        } else {
            AccountExtendedAttribute accountExtension = (AccountExtendedAttribute)account.getExtension();
            if (StringUtils.isBlank(appropriationAccountNumber)) {
                if (StringUtils.isNotBlank(accountExtension.getAppropriationAccountNumber())) {
                    AppropriationAccount appropriationAcct = getMatchedRecord(AppropriationAccount.class, subFundGroupCode, CUKFSPropertyConstants.APPROPRIATION_ACCT_NUMBER, accountExtension.getAppropriationAccountNumber());
                    if (appropriationAcct == null) {
                        success = false;
                        GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_APPROP_ACCT_NOT_GROUP_CODE,
                                new String[] {accountExtension.getAppropriationAccountNumber(), subFundGroupCode, account.getAccountNumber()});
                    }
                }
            }
        }
        return success;
    }

    private <T extends BusinessObject> T getMatchedRecord(Class<T> clazz, String subFundGroupCode, String propertyName, String propertyValue) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put(KFSPropertyConstants.SUB_FUND_GROUP_CODE, subFundGroupCode);
        fieldValues.put(propertyName, propertyValue);

        return getBoService().findByPrimaryKey(clazz, fieldValues);

    }

    protected boolean checkOpenEncumbrances() {
        boolean success = true;
        for (AccountGlobalDetail detail : newAccountGlobal.getAccountGlobalDetails()) {
            success &= checkOpenEncumbrances(detail);
        }
        return success;
    }

    protected boolean checkOpenEncumbrances(AccountGlobalDetail detail) {
        boolean success = true;
        if(!detail.getAccount().isClosed() && newAccountGlobal.getClosed()!=null && newAccountGlobal.getClosed()){
            Map<String, String> pkMap = new HashMap<String, String>();
            String chart = detail.getAccount().getChartOfAccountsCode();
            String accountNumber = detail.getAccount().getAccountNumber();
            pkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().toString() );
            pkMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
            pkMap.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
            int encumbranceCount = getEncumbranceService().getOpenEncumbranceRecordCount(pkMap, false);
            if ( encumbranceCount > 0){
                success = false;
                putFieldError(KFSPropertyConstants.CLOSED, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_CLOSED_CHECKED_WHEN_ACCOUNT_HAS_OPEN_ENCUMBRENCES, new String[] {chart, accountNumber});
            }
        }
        return success;
    }

    /**
     * the fringe benefit account (otherwise known as the reportsToAccount) is required if the fringe benefit code is set to N. The
     * fringe benefit code of the account designated to accept the fringes must be Y.
     *
     * @param newAccount
     * @return
     */
     protected boolean checkFringeBenefitAccountRule(CuAccountGlobal newAccount) {

         boolean result = true;

         if (newAccount.getAccountsFringesBnftIndicator() !=null && !newAccount.getAccountsFringesBnftIndicator()){
             if (StringUtils.isBlank(newAccount.getReportsToAccountNumber())) {
                 putFieldError(KFSPropertyConstants.REPORTS_TO_ACCOUNT_NUMBER, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_RPTS_TO_ACCT_REQUIRED_IF_FRINGE_BENEFIT_FALSE);
                 result &= false;
             }

             if (StringUtils.isBlank(newAccount.getReportsToChartOfAccountsCode())) {
                 putFieldError(KFSPropertyConstants.REPORTS_TO_CHART_OF_ACCOUNTS_CODE, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_RPTS_TO_ACCT_REQUIRED_IF_FRINGE_BENEFIT_FALSE);
                 result &= false;
             }

             if (result == false) {
                 return result;
             }

             Account fringeBenefitAccount = accountService.getByPrimaryId(newAccount.getReportsToChartOfAccountsCode(), newAccount.getReportsToAccountNumber());

             if (fringeBenefitAccount == null) {
                 putFieldError(KFSPropertyConstants.REPORTS_TO_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE, getFieldLabel(Account.class, "reportsToAccountNumber"));
                 return false;
             }

             if (!fringeBenefitAccount.isActive()) {
                 putFieldError(KFSPropertyConstants.REPORTS_TO_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_INACTIVE, getFieldLabel(Account.class, "reportsToAccountNumber"));
                 result &= false;
             }

             if (!fringeBenefitAccount.isAccountsFringesBnftIndicator()) {
                 putFieldError(KFSPropertyConstants.REPORTS_TO_ACCOUNT_NUMBER, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_RPTS_TO_ACCT_MUST_BE_FLAGGED_FRINGE_BENEFIT, fringeBenefitAccount.getChartOfAccountsCode() + "-" + fringeBenefitAccount.getAccountNumber());
                 result &= false;
             }
         }

         return result;
     }

    /**
     * This method checks to make sure that if the contracts and grants fields are required they are entered correctly
     *
     * @param newAccount
     * @return
     */
    protected boolean checkCgRequiredFields(CuAccountGlobal newAccount) {
        boolean result = true;

        if (ObjectUtils.isNotNull(newAccount.getSubFundGroup())) {
            if (getSubFundGroupService().isForContractsAndGrants(newAccount.getSubFundGroup())) {
                result &= checkIcrTypeCodeAndSeriesIdentifierExistWhenSubFundIsCG(newAccount);
                result &= checkContractControlAccountNumberRequired(newAccount);
                result &= checkICRCollectionExistsWhenUpdatingToCGSubFund(newAccount, newAccount.getSubFundGroupCode());
            }
            else{
                result &= checkCGFieldNotFilledIn(newAccount, KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, newAccount.getSubFundGroupCode());
                result &= checkCGFieldNotFilledIn(newAccount, KFSPropertyConstants.ACCT_INDIRECT_COST_RCVY_TYPE_CD, newAccount.getSubFundGroupCode());
                result &= checkICRCollectionDoesNotExistWhenUpdatingToNonCGSubFund(newAccount, newAccount.getSubFundGroupCode());
            }
        }
        else{
            if(ObjectUtils.isNotNull(newAccount.getAccountGlobalDetails()) && newAccount.getAccountGlobalDetails().size() >0){
                boolean hasAtLeastOneAccountWithCGSubFund = false;
                for(AccountGlobalDetail accountGlobalDetail : newAccount.getAccountGlobalDetails()){
                    accountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
                    if (ObjectUtils.isNotNull(accountGlobalDetail.getAccount().getSubFundGroup())) {
                        if (!getSubFundGroupService().isForContractsAndGrants(accountGlobalDetail.getAccount().getSubFundGroup())) {
                            result &= checkCGFieldNotFilledIn(newAccount, KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, accountGlobalDetail.getAccount().getSubFundGroupCode());
                            result &= checkCGFieldNotFilledIn(newAccount, KFSPropertyConstants.ACCT_INDIRECT_COST_RCVY_TYPE_CD, accountGlobalDetail.getAccount().getSubFundGroupCode());
                            result &= checkCGFieldNotFilledIn(newAccount, accountGlobalDetail.getAccount(), KFSPropertyConstants.ACCT_INDIRECT_COST_RCVY_TYPE_CD);
                            result &= checkCGFieldNotFilledIn(newAccount, accountGlobalDetail.getAccount(), KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER);
                            result &= checkICRCollectionDoesNotExistForExistingNonCGSubFund(newAccount, accountGlobalDetail, accountGlobalDetail.getAccount().getSubFundGroupCode());
                            }
                        else{
                            result &= checkICRCollectionExistsForExistingCGSubFund(newAccount, accountGlobalDetail,accountGlobalDetail.getAccount().getSubFundGroupCode());
                            hasAtLeastOneAccountWithCGSubFund = true;
                        }
                    }
                }
                if (hasAtLeastOneAccountWithCGSubFund && icrTypeCodeOrSeriesIdentifierAreSet(newAccount)) {
                    result &= checkIcrTypeCodeAndSeriesIdentifierExistWhenSubFundIsCG(newAccount);
                }
            }
        }
        return result;
    }

    private boolean icrTypeCodeOrSeriesIdentifierAreSet(CuAccountGlobal newAccount) {
        return checkEmptyValue(newAccount.getAcctIndirectCostRcvyTypeCd())
                || checkEmptyValue(newAccount.getFinancialIcrSeriesIdentifier());
    }

    private boolean checkIcrTypeCodeAndSeriesIdentifierExistWhenSubFundIsCG(CuAccountGlobal newAccount){
        boolean result = true;
        boolean filledIcrTypeCode = checkEmptyValue(newAccount.getAcctIndirectCostRcvyTypeCd());
        boolean filledFinancialIcrSeriesIdentifier = checkEmptyValue(newAccount.getFinancialIcrSeriesIdentifier());

        // Validation for financialIcrSeriesIdentifier
        if (filledFinancialIcrSeriesIdentifier) {
            String fiscalYear = StringUtils.EMPTY + SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
            String icrSeriesId = newAccount.getFinancialIcrSeriesIdentifier();

            Map<String, String> pkMap = new HashMap<String, String>();
            pkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
            pkMap.put(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, icrSeriesId);
            Collection<IndirectCostRecoveryRateDetail> icrRateDetails = getBoService().findMatching(IndirectCostRecoveryRateDetail.class, pkMap);

            if (ObjectUtils.isNull(icrRateDetails) || icrRateDetails.isEmpty()) {
                String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(Account.class, KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER);
                putFieldError(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, KFSKeyConstants.ERROR_EXISTENCE, label + " (" + icrSeriesId + ")");
                result &= false;
            }
            else {
                for(IndirectCostRecoveryRateDetail icrRateDetail : icrRateDetails) {
                    if(ObjectUtils.isNull(icrRateDetail.getIndirectCostRecoveryRate())){
                        putFieldError(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, COAKeyConstants.ERROR_DOCUMENT_ICR_RATE_NOT_FOUND, new String[]{fiscalYear, icrSeriesId});
                        result &= false;
                        break;
                    }
                }
            }
        }
        if(!filledIcrTypeCode || !filledFinancialIcrSeriesIdentifier){
            if(ObjectUtils.isNotNull(newAccount.getAccountGlobalDetails()) && newAccount.getAccountGlobalDetails().size() >0){
                for(AccountGlobalDetail accountGlobalDetail : newAccount.getAccountGlobalDetails()){
                    accountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);

                    if(!filledIcrTypeCode){
                        result &= checkEmptyBOField( KFSPropertyConstants.ACCT_INDIRECT_COST_RCVY_TYPE_CD, accountGlobalDetail.getAccount().getAcctIndirectCostRcvyTypeCd(), formatErrorMessage(CuCOAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ICR_TYPE_CODE_CANNOT_BE_EMPTY));
                    }
                    if(!filledFinancialIcrSeriesIdentifier){
                        result &= checkEmptyBOField(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, accountGlobalDetail.getAccount().getFinancialIcrSeriesIdentifier(), formatErrorMessage(CuCOAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ICR_SERIES_IDENTIFIER_CANNOT_BE_EMPTY));
                    }

                }
            }
        }
        return result;
    }

    protected boolean checkICRCollectionExistsWhenUpdatingToCGSubFund(CuAccountGlobal newAccount, String subFundGroupCode){
        boolean success = true;

        if(newAccount.getActiveIndirectCostRecoveryAccounts().isEmpty()){
            if(ObjectUtils.isNotNull(newAccount.getAccountGlobalDetails()) && !newAccount.getAccountGlobalDetails().isEmpty()){
                for(AccountGlobalDetail accountGlobalDetail : newAccount.getAccountGlobalDetails()){
                    List<IndirectCostRecoveryAccount> activeICRList = getActiveUpdatedIcrAccounts(newAccount, accountGlobalDetail);
                    if (activeICRList.isEmpty()){
                        success &= false;
                        putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_EMPTY_FOR_CG_ACCOUNT, new String[]{subFundGroupCode, accountGlobalDetail.getAccountNumber()});
                    }
                }
            }
        }

        return success;
    }

    protected boolean checkICRCollectionExistsForExistingCGSubFund(CuAccountGlobal newAccount, AccountGlobalDetail accountGlobalDetail, String subFundGroupCode){
        boolean success = true;

        if (newAccount.getActiveIndirectCostRecoveryAccounts().isEmpty()) {
            List<IndirectCostRecoveryAccount> activeICRList = getActiveUpdatedIcrAccounts(newAccount, accountGlobalDetail);
            if (activeICRList.isEmpty()) {
                success = false;
                putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_EMPTY_FOR_CG_ACCOUNT, new String[] { subFundGroupCode, accountGlobalDetail.getAccountNumber() });
            }
        }

        return success;
    }

    protected boolean checkICRCollectionDoesNotExistWhenUpdatingToNonCGSubFund(CuAccountGlobal newAccount, String subFundGroupCode){
        boolean success = true;
        boolean hasActiveUpdates = !newAccount.getActiveIndirectCostRecoveryAccounts().isEmpty();

        if (hasActiveUpdates){
            success = false;
            putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_CG_ICR_FIELDS_FILLED_FOR_NON_CG_ACCOUNT, newAccount.getSubFundGroupCode());
        }
        else {
            if(ObjectUtils.isNotNull(newAccount.getAccountGlobalDetails()) && !newAccount.getAccountGlobalDetails().isEmpty()){
                for(AccountGlobalDetail accountGlobalDetail : newAccount.getAccountGlobalDetails()){
                    List<IndirectCostRecoveryAccount> activeICRList = getActiveUpdatedIcrAccounts(newAccount, accountGlobalDetail);
                    if(!activeICRList.isEmpty()){
                        success &= false;
                        putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_NOT_EMPTY_FOR_NON_CG_ACCOUNT, new String[]{ subFundGroupCode, accountGlobalDetail.getAccountNumber()});
                    }
                }
            }
        }

        return success;
    }

    protected boolean checkICRCollectionDoesNotExistForExistingNonCGSubFund(CuAccountGlobal newAccount, AccountGlobalDetail accountGlobalDetail, String subFundGroupCode){
        boolean success = true;
        boolean hasActiveUpdates = !newAccount.getActiveIndirectCostRecoveryAccounts().isEmpty();

        if(hasActiveUpdates){
            success = false;
        }
        else {
            List<IndirectCostRecoveryAccount> activeICRList = getActiveUpdatedIcrAccounts(newAccount, accountGlobalDetail);
            success = activeICRList.isEmpty();
        }

        if (!success) {
            putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_NOT_EMPTY_FOR_NON_CG_ACCOUNT, new String[] { subFundGroupCode, accountGlobalDetail.getAccountNumber() });
        }

        return success;
    }

    private List<IndirectCostRecoveryAccount> getActiveUpdatedIcrAccounts(CuAccountGlobal newAccount,
            AccountGlobalDetail accountGlobalDetail) {
        accountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
        List<IndirectCostRecoveryAccount> existingICRList = accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts();
        List<IndirectCostRecoveryAccount> copyOfExistingICRList = new ArrayList<IndirectCostRecoveryAccount>();
        if (existingICRList != null && !existingICRList.isEmpty()) {
            for (IndirectCostRecoveryAccount icrAccount : existingICRList) {
                copyOfExistingICRList.add(new IndirectCostRecoveryAccount(icrAccount));
            }
        }
        List<IndirectCostRecoveryAccount> updatedICRList = SpringContext.getBean(GlobalObjectWithIndirectCostRecoveryAccountsService.class)
                .buildUpdatedIcrAccounts(newAccount, accountGlobalDetail, copyOfExistingICRList);
        List<IndirectCostRecoveryAccount> activeICRList = getActiveICRAccounts(updatedICRList);
        return activeICRList;
    }

    private List<IndirectCostRecoveryAccount> getActiveICRAccounts(List<IndirectCostRecoveryAccount> icrAccounts) {
        List<IndirectCostRecoveryAccount> activeICRList = new ArrayList<IndirectCostRecoveryAccount>();
        if (ObjectUtils.isNotNull(icrAccounts) && !icrAccounts.isEmpty()) {
            for (IndirectCostRecoveryAccount icrAccount : icrAccounts) {
                if (icrAccount.isActive()) {
                    activeICRList.add(icrAccount);
                }
            }
        }
        return activeICRList;
    }


    /**
     * This method is a helper method that replaces error tokens with values for
     * contracts and grants labels
     *
     * @param errorConstant
     * @return error string that has had tokens "{0}" and "{1}" replaced
     */
    protected String formatErrorMessage(String errorConstant) {
        String cngLabel = getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel();
        String cngValue = getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage();
        String result = getConfigService().getPropertyValueAsString(errorConstant);
        result = MessageFormat.format(result, cngLabel, cngValue);
        return result;
    }

    /**
     * This method checks to make sure that if the contract control account exists
     * it is the same as the Account that we are working on
     *
     * @param newAccount
     * @return false if the contract control account is entered and is not the same
     *         as the account we are maintaining
     */
    protected boolean checkContractControlAccountNumberRequired(CuAccountGlobal newAccount) {

        boolean result = true;

        // Contract Control account must either exist or be the same as account being
        // maintained

        if (ObjectUtils.isNull(newAccount.getContractControlFinCoaCode())) {
            return result;
        }
        if (ObjectUtils.isNull(newAccount.getContractControlAccountNumber())) {
            return result;
        }

        // if no account global details exist then don't validate
        if (ObjectUtils.isNull(newAccount.getAccountGlobalDetails())
                || newAccount.getAccountGlobalDetails().size() == 0) {
            return true;
        }

        if (newAccount.getAccountGlobalDetails().size() == 1) {
            if ((newAccount.getContractControlFinCoaCode().equals(newAccount.getChartOfAccountsCode()))
                    && (newAccount.getContractControlAccountNumber().equals(newAccount.getAccountGlobalDetails().get(0).getAccountNumber()))) {
                return true;
            }
        }

        // do an existence/active test
        DictionaryValidationService dvService = super.getDictionaryValidationService();
        boolean referenceExists = dvService.validateReferenceExists(newAccount,
                KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT);
        if (!referenceExists) {
            putFieldError(KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE,
                    "Contract Control Account: " + newAccount.getContractControlFinCoaCode() + "-"
                            + newAccount.getContractControlAccountNumber());
            result &= false;
        }

        return result;
    }

    /**
     * This method checks to make sure that if the contract control account exists
     * it is the same as the Account that we are working on
     *
     * @param newAccount
     * @return false if the contract control account is entered and is not the same
     *         as the account we are maintaining
     */
    protected boolean checkContractControlAccountNumberRequired(CuAccountGlobal newAccount, Account oldAccount) {

        boolean result = true;

        // Contract Control account must either exist or be the same as account being
        // maintained

        if (ObjectUtils.isNull(newAccount.getContractControlFinCoaCode())) {
            return result;
        }
        if (ObjectUtils.isNull(newAccount.getContractControlAccountNumber())) {
            return result;
        }

        // do an existence/active test
        DictionaryValidationService dvService = super.getDictionaryValidationService();
        boolean referenceExists = dvService.validateReferenceExists(newAccount,
                KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT);
        if (!referenceExists) {
            putFieldError(KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE,
                    "Contract Control Account: " + newAccount.getContractControlFinCoaCode() + "-"
                            + newAccount.getContractControlAccountNumber());
            result &= false;
        }

        return result;
    }

    protected boolean checkCloseAccounts() {
        boolean success = true;

        LOG.info("checkCloseAccount called");

        // check that at least one account is being closed
        boolean isBeingClosed = false;
        if (ObjectUtils.isNotNull(newAccountGlobal.getAccountGlobalDetails())
                && newAccountGlobal.getAccountGlobalDetails().size() > 0) {
            for (AccountGlobalDetail detail : newAccountGlobal.getAccountGlobalDetails()) {
                if (detail.getAccount().isActive() && newAccountGlobal.getClosed() != null
                        && newAccountGlobal.getClosed()) {
                    isBeingClosed = true;
                    break;
                }
            }
        }

        if (!isBeingClosed) {
            return true;
        }

        if (ObjectUtils.isNotNull(newAccountGlobal.getAccountGlobalDetails())
                && newAccountGlobal.getAccountGlobalDetails().size() > 0) {

            for (AccountGlobalDetail detail : newAccountGlobal.getAccountGlobalDetails()) {
                success &= checkCloseAccount(detail);
            }
        }

        return success;
    }

    /**
     * This method checks to see if the user is trying to close the account and if
     * so if any rules are being violated Calls the additional rule
     * checkAccountExpirationDateValidTodayOrEarlier
     *
     * @param maintenanceDocument
     * @return false on rules violation
     */
    protected boolean checkCloseAccount(AccountGlobalDetail detail) {

        LOG.info("checkCloseAccount called");

        boolean success = true;
        boolean isBeingClosed = false;

        // if the account isnt being closed, then dont bother processing the rest of
        // the method
        if (detail.getAccount().isActive() && newAccountGlobal.getClosed() != null && newAccountGlobal.getClosed()) {
            isBeingClosed = true;
        }

        if (!isBeingClosed) {
            return true;
        }

        // on an account being closed, the expiration date already on the account must
        // be valid OR
        // the expiration date being entered on the account global edoc must be valid
        success &= checkAccountExpirationDateValidTodayOrEarlier(detail, newAccountGlobal);

        // when closing an account, a continuation account is required
        if (StringUtils.isBlank(newAccountGlobal.getContinuationAccountNumber())
                && StringUtils.isBlank(detail.getAccount().getContinuationAccountNumber())) {
            putFieldError(KFSPropertyConstants.CONTINUATION_ACCOUNT_NUMBER,
                    COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_CLOSE_CONTINUATION_ACCT_REQD);
            success &= false;
        }
        if (StringUtils.isBlank(newAccountGlobal.getContinuationFinChrtOfAcctCd())
                && StringUtils.isBlank(detail.getAccount().getContinuationFinChrtOfAcctCd())) {
            putFieldError(KFSPropertyConstants.CONTINUATION_CHART_OF_ACCOUNTS_CODE,
                    COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_CLOSE_CONTINUATION_CHART_CODE_REQD);
            success &= false;
        }

        String errorPath = KFSPropertyConstants.ACCOUNT_CHANGE_DETAILS;
        // must have no pending ledger entries
        if (generalLedgerPendingEntryService.hasPendingGeneralLedgerEntry(detail.getAccount())) {

            putFieldError(errorPath,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_PENDING_LEDGER_ENTRIES,
                    new String[] { detail.getAccountNumber() });
            success &= false;
        }

        // beginning balance must be loaded in order to close account
        if (!balanceService.beginningBalanceLoaded(detail.getAccount())) {
            putFieldError(errorPath,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_NO_LOADED_BEGINNING_BALANCE,
                    new String[] { detail.getAccountNumber() });
            success &= false;
        }

        // must have no base budget, must have no open encumbrances, must have no asset,
        // liability or fund balance balances other
        // than object code 9899
        // (9899 is fund balance for us), and the process of closing income and expense
        // into 9899 must take the 9899 balance to
        // zero.
        if (balanceService.hasAssetLiabilityFundBalanceBalances(detail.getAccount())) {
            putFieldError(errorPath, CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_NO_FUND_BALANCES,
                    new String[] { detail.getAccountNumber() });
            success &= false;
        }

        // We must not have any pending labor ledger entries
        if (SpringContext.getBean(LaborLedgerPendingEntryService.class).hasPendingLaborLedgerEntry(
                detail.getAccount().getChartOfAccountsCode(), detail.getAccount().getAccountNumber())) {
            putFieldError(errorPath,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_PENDING_LABOR_LEDGER_ENTRIES,
                    new String[] { detail.getAccountNumber() });
            success &= false;
        }

        return success;
    }

    protected boolean checkAccountExpirationDateValidTodayOrEarlier(AccountGlobalDetail detail,
            CuAccountGlobal newAccount) {

        Date userEnteredExpirationDate = newAccount.getAccountExpirationDate();
        if (ObjectUtils.isNotNull(userEnteredExpirationDate)) {
            if (!isExpirationDateCurrentDateOrEarlier(userEnteredExpirationDate)) {
                putFieldError(KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE,
                        COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_CANNOT_BE_CLOSED_EXP_DATE_INVALID);
                return false;
            }
        } else {
            Date accountExistingExpriationDate = detail.getAccount().getAccountExpirationDate();

            if (ObjectUtils.isNotNull(accountExistingExpriationDate)) {
                if (!isExpirationDateCurrentDateOrEarlier(accountExistingExpriationDate)) {
                    putFieldError(KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE,
                            CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_CANNOT_BE_CLOSED_EXP_DATE_NOT_ENTERED_EXISTING_DATE_INVALID,
                            new String[] { detail.getAccount().getChartOfAccountsCode(),
                                    detail.getAccount().getAccountNumber() });
                    return false;
                }
            } else {
                putFieldError(KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE,
                        COAKeyConstants.ERROR_DOCUMENT_ACCMAINT_ACCT_CANNOT_BE_CLOSED_EXP_DATE_INVALID);
                return false;
            }
        }
        return true;
    }

    private boolean isExpirationDateCurrentDateOrEarlier(Date expirationDateToVerify) {
        Date todaysDate = new Date(getDateTimeService().getCurrentDate().getTime());
        todaysDate.setTime(DateUtils.truncate(todaysDate, Calendar.DAY_OF_MONTH).getTime());
        expirationDateToVerify.setTime(DateUtils.truncate(expirationDateToVerify, Calendar.DAY_OF_MONTH).getTime());

        return !expirationDateToVerify.after(todaysDate);
    }

    /**
     * This method checks to see if the contracts and grants fields are filled in or
     * not
     *
     * @param account
     * @param propertyName - property to attach error to
     * @return false if the contracts and grants fields are blank
     */
    protected boolean checkCGFieldNotFilledIn(Object account, String propertyName, String subFundGroupCode) {
        boolean success = true;
        Object value = ObjectUtils.getPropertyValue(account, propertyName);
        if ((value instanceof String && StringUtils.isNotBlank(value.toString())) || (value != null)) {
            success = false;
            String label = getDdService().getAttributeLabel(Account.class, propertyName);
            putFieldError(propertyName,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_CG_FIELDS_FILLED_FOR_NON_CG_ACCOUNT,
                    new String[] { subFundGroupCode, label });
        }

        return success;
    }


    /**
     * This method checks to see if the contracts and grants fields are filled in or
     * not
     *
     * @param account
     * @param propertyName - property to attach error to
     * @return false if the contracts and grants fields are blank
     */
    protected boolean checkCGFieldNotFilledIn(CuAccountGlobal account, Account accountGlobalDetail,
            String propertyName) {
        boolean success = true;
        Object value = ObjectUtils.getPropertyValue(accountGlobalDetail, propertyName);
        if ((value instanceof String && StringUtils.isNotBlank(value.toString())) || (value != null)) {
            success = false;
            putFieldError(KFSPropertyConstants.ACCOUNT_CHANGE_DETAILS,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CG_FIELDS_FILLED_FOR_NON_CG_ACCOUNT,
                    new String[] { account.getSubFundGroupCode(), accountGlobalDetail.getAccountNumber() });
        }

        return success;
    }

    protected boolean checkRemoveExpirationDate() {
        boolean success = true;

        if (newAccountGlobal.isRemoveAccountExpirationDate()
                && ObjectUtils.isNotNull(newAccountGlobal.getAccountExpirationDate())) {
            success = false;
            putFieldError(KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_EXP_DATE_NOT_EMPTY_AND_REMOVE_EXP_DATE_CHECKED);
        }

        return success;

    }

    protected boolean checkRemoveContinuationChartAndAccount() {
        boolean success = true;

        if (newAccountGlobal.isRemoveContinuationChartAndAccount()
                && (StringUtils.isNotBlank(newAccountGlobal.getContinuationFinChrtOfAcctCd())
                        || StringUtils.isNotBlank(newAccountGlobal.getContinuationAccountNumber()))) {
            success = false;
            putFieldError(KFSPropertyConstants.CONTINUATION_CHART_OF_ACCOUNTS_CODE,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_CNT_CHART_NOT_EMPTY_AND_REMOVE_CNT_CHART_AND_ACCT_CHECKED);
            putFieldError(KFSPropertyConstants.CONTINUATION_ACCOUNT_NUMBER,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_CNT_ACCT_NOT_EMPTY_AND_REMOVE_CNT_CHART_AND_ACCT_CHECKED);
        }

        if (newAccountGlobal.isRemoveContinuationChartAndAccount()) {
            // check that expiration date does not exist
            if (newAccountGlobal.isRemoveAccountExpirationDate()) {
                // no problem, we can remove
            } else {
                if (ObjectUtils.isNull(newAccountGlobal.getAccountExpirationDate())) {
                    for (AccountGlobalDetail accountGlobalDetail : newAccountGlobal.getAccountGlobalDetails()) {
                        if (ObjectUtils.isNotNull(accountGlobalDetail.getAccount().getAccountExpirationDate())) {
                            putFieldError(CUKFSPropertyConstants.REMOVE_CONTINUATION_CHART_AND_ACCOUNT,
                                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_EXP_DT_AND_REMOVE_CONT_ACCT,
                                    new String[] { accountGlobalDetail.getChartOfAccountsCode(),
                                            accountGlobalDetail.getAccountNumber() });
                        }
                    }
                }
            }
        }

        return success;

    }

    protected boolean checkRemoveIncomeStreamChartAndAccount() {
        boolean success = true;

        if (newAccountGlobal.isRemoveIncomeStreamChartAndAccount()
                && (StringUtils.isNotBlank(newAccountGlobal.getIncomeStreamFinancialCoaCode())
                        || StringUtils.isNotBlank(newAccountGlobal.getIncomeStreamAccountNumber()))) {
            success = false;
            putFieldError(KFSPropertyConstants.INCOME_STREAM_CHART_OF_ACCOUNTS_CODE,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_INC_STR_CHART_NOT_EMPTY_AND_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED);
            putFieldError(KFSPropertyConstants.INCOME_STREAM_ACCOUNT_NUMBER,
                    CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_INC_STR_ACCT_NOT_EMPTY_AND_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED);
        }

        if (newAccountGlobal.isRemoveIncomeStreamChartAndAccount()) {

            for (AccountGlobalDetail accountGlobalDetail : newAccountGlobal.getAccountGlobalDetails()) {
                boolean required = false;
                // if the subFundGroup object is null, we cant test, so exit
                if (ObjectUtils.isNull(accountGlobalDetail.getAccount().getSubFundGroup())) {
                    continue;
                }

                // retrieve the subfundcode and fundgroupcode
                String subFundGroupCode = accountGlobalDetail.getAccount().getSubFundGroupCode().trim();
                String fundGroupCode = accountGlobalDetail.getAccount().getSubFundGroup().getFundGroupCode().trim();

                // changed foundation code. Now, it is using similar 'income stream account'
                // validation rule for 'Account'
                if (isIncomeStreamAccountRequired(fundGroupCode, subFundGroupCode)) {
                    required = true;
                }

                // if the income stream account is not required, then we're done
                if (!required) {
                    continue;
                }

                if (newAccountGlobal.isRemoveIncomeStreamChartAndAccount()) {
                    putFieldError(CUKFSPropertyConstants.REMOVE_INCOME_STREAM_CHART_AND_ACCOUNT,
                            CUKFSKeyConstants.ERROR_DOCUMENT_ACCT_GLB_MAINT_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED_WHEN_INC_STR_REQ_FOR_ACCT,
                            new String[] { accountGlobalDetail.getChartOfAccountsCode(),
                                    accountGlobalDetail.getAccountNumber() });
                }
            }
        }

        return success;

    }

    protected String getDDAttributeLabel(String attribute) {
        return ddService.getAttributeLabel(IndirectCostRecoveryAccount.class, attribute);
    }

    // Cornell Customization: Override this method from the custom superclass.
    @Override
    protected String buildMessageFromPrimaryKey(GlobalBusinessObjectDetailBase detail) {
        if (detail instanceof AccountGlobalDetail) {
            AccountGlobalDetail accountGlobalDetail = (AccountGlobalDetail) detail;
            StringBuilder message = new StringBuilder();
            message.append(accountGlobalDetail.getChartOfAccountsCode());
            message.append(KFSConstants.DASH);
            message.append(accountGlobalDetail.getAccountNumber());
            return message.toString();
        } else {
            return super.buildMessageFromPrimaryKey(detail);
        }
    }

    public EncumbranceService getEncumbranceService() {
        if (encumbranceService == null) {
            encumbranceService = SpringContext.getBean(EncumbranceService.class);
        }
        return encumbranceService;
    }

    public GeneralLedgerPendingEntryService getGeneralLedgerPendingEntryService() {
        return generalLedgerPendingEntryService;
    }

    public void setGeneralLedgerPendingEntryService(GeneralLedgerPendingEntryService generalLedgerPendingEntryService) {
        this.generalLedgerPendingEntryService = generalLedgerPendingEntryService;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public SubFundGroupService getSubFundGroupService() {
        if (subFundGroupService == null) {
            subFundGroupService = SpringContext.getBean(SubFundGroupService.class);
        }
        return subFundGroupService;
    }

    public void setContractsAndGrantsService(ContractsAndGrantsService contractsAndGrantsService) {
        this.contractsAndGrantsService = contractsAndGrantsService;
    }

    @Override
    public DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }
    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

}

