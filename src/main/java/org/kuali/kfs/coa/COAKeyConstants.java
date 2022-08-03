/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.coa;

/* Cornell customization: backport FINP-8341 */
public final class COAKeyConstants {
    public static final String ERROR_DOCUMENT_ACCTDELEGATEMAINT_FROM_AMOUNT_NONNEGATIVE = "error.document.accountDelegateMaintenance.fromAmountMustBeNonNegative";
    public static final String ERROR_DOCUMENT_ACCTDELEGATEMAINT_INVALID_CHART_CODE = "error.document.accountDelegateMaintenance.invalidChartCode";
    public static final String ERROR_DOCUMENT_ACCTDELEGATEMAINT_INVALID_DOC_TYPE = "error.document.delegateMaintenance.invalidFinancialSystemDocumentTypeCode";
    public static final String ERROR_DOCUMENT_ACCTDELEGATEMAINT_INVALID_ORGANIZATION_CODE = "error.document.accountDelegateMaintenance.invalidOrganizationCode";
    public static final String ERROR_DOCUMENT_ACCTDELEGATEMAINT_PRIMARY_ROUTE_ALREADY_EXISTS_FOR_DOCTYPE = "error.document.accountDelegateMaintenance.primaryRouteAlreadyExistsForNewDocType";
    public static final String ERROR_DOCUMENT_ACCTDELEGATEMAINT_TO_AMOUNT_MORE_THAN_FROM_OR_ZERO = "error.document.accountDelegateMaintenance.toAmountMustBeEqualOrGreaterThanFromAmountOrZero";
    public static final String ERROR_DOCUMENT_ACCTDELEGATEMAINT_USER_DOESNT_EXIST = "error.document.accountDelegateMaintenance.userDoesntExist";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCOUNT_EXPIRED_CONTINUATION = "error.document.accountMaintenance.expiredAccount.continuationAccount";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCOUNT_CANNOT_CLOSE_OPEN_ENCUMBRANCE = "error.document.accountMaintenance.accountCannotCloseOpenEncumbrance";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCOUNT_CLOSED_PENDING_LEDGER_ENTRIES = "error.document.accountMaintenance.closedAccount.noPendingLedgerEntriesAllowed";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCOUNT_CLOSED_NO_LOADED_BEGINNING_BALANCE = "error.document.accountMaintenance.closedAccount.beginningBalanceNotLoaded";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCOUNT_CLOSED_NO_FUND_BALANCES = "error.document.accountMaintenance.closedAccount.noFundBalances";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCOUNT_CLOSED_PENDING_LABOR_LEDGER_ENTRIES = "error.document.accountMaintenance.closedAccount.noPendingLaborLedgerEntriesAllowed";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_CANNOT_BE_CLOSED_EXP_DATE_INVALID = "error.document.accountMaintenance.accountCannotBeClosedExpDateInvalid";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_CLOSE_CONTINUATION_CHART_CODE_REQD = "error.document.accountMaintenance.accountCloseContinuationChartCodeReqd";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_CLOSE_CONTINUATION_ACCT_REQD = "error.document.accountMaintenance.accountCloseContinuationAcctReqd";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_CONT_ACCOUNT_CANNOT_BE_SAME = "error.document.accountMaintenance.accountContinuationAccountCannotBeSame";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_MGR_CANNOT_EQUAL_EXISTING_ACCT_SUPERVISOR = "error.document.accountMaintenance.accountManagerCannotEqualExistingAccountSupervisor";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_NMBR_NOT_ALLOWED = "error.document.accountMaintenance.accountNumberNotAllowed";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_NMBR_NOT_UNIQUE = "error.document.accountMaintenance.accountNumberNotUnique";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_BE_ACCT_MGR = "error.document.accountMaintenance.accountSupervisorCannotBeAcctManager";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_BE_FISCAL_OFFICER = "error.document.accountMaintenance.accountSupervisorCannotBeFiscalOfficer";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_EQUAL_EXISTING_ACCT_MGR = "error.document.accountMaintenance.accountSupervisorCannotEqualExistingAcctManager";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_SUPER_CANNOT_EQUAL_EXISTING_FISCAL_OFFICER = "error.document.accountMaintenance.accountSupervisorCannotEqualExistingFiscalOfficer";
    public static final String ERROR_DOCUMENT_ACCMAINT_BLANK_SUBFUNDGROUP_WITH_BUILDING_CD = "error.document.accountMaintenance.blankSubFundGroupWithBuildingCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_BLANK_SUBFUNDGROUP_WITH_CAMPUS_CD_FOR_BLDG = "error.document.accountMaintenance.blankSubFundGroupWithCampusCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_CAMS_SUBFUNDGROUP_WITH_MISSING_BUILDING_CD = "error.document.accountMaintenance.camsSubFundGroupWithMissingBuildingCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_CAMS_SUBFUNDGROUP_WITH_MISSING_CAMPUS_CD_FOR_BLDG = "error.document.accountMaintenance.camsSubFundGroupWithMissingCampusCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_CG_FIELDS_FILLED_FOR_NON_CG_ACCOUNT = "error.document.accountMaintenance.cgFieldsFilledInForNonCGAccount";
    public static final String ERROR_DOCUMENT_ACCMAINT_CG_ICR_FIELDS_FILLED_FOR_NON_CG_ACCOUNT = "error.document.accountMaintenance.cgICRFieldsFilledInForNonCGAccount";
    public static final String ERROR_DOCUMENT_ACCMAINT_CONTINUATION_ACCT_REQD_IF_EXP_DATE_COMPLETED = "error.document.accountMaintenance.continuationAcctReqdIfExpDateCompleted";
    public static final String ERROR_DOCUMENT_ACCMAINT_CONTINUATION_FIN_CODE_REQD_IF_EXP_DATE_COMPLETED = "error.document.accountMaintenance.continuationFinCodeIfExpDateCompleted";
    public static final String ERROR_DOCUMENT_ACCMAINT_EXP_DATE_CANNOT_BE_BEFORE_EFFECTIVE_DATE = "error.document.accountMaintenance.expDateCannotBeBeforeEffectiveDate";
    public static final String ERROR_DOCUMENT_ACCMAINT_EXP_DATE_TODAY_LATER = "error.document.accountMaintenance.expDateTodayLater";
    public static final String ERROR_DOCUMENT_ACCMAINT_FISCAL_OFFICER_CANNOT_EQUAL_EXISTING_ACCT_SUPERVISOR = "error.document.accountMaintenance.fiscalOfficerCannotEqualExistingAccountSupervisor";
    public static final String ERROR_DOCUMENT_ACCMAINT_ICR_ACCOUNT_INVALID_LINE_PERCENT = "error.document.accountMaintenance.indirectCostRecoveryAccounts.invalidLinePercent";
    public static final String ERROR_DOCUMENT_ACCMAINT_ICR_ACCOUNT_TOTAL_NOT_100_PERCENT = "error.document.accountMaintenance.indirectCostRecoveryAccounts.totalNot100Percent";
    public static final String ERROR_DOCUMENT_ACCMAINT_ICR_CHART_CODE_CANNOT_BE_EMPTY = "error.document.accountMaintenance.icrChartCodeCannotBeEmpty";
    public static final String ERROR_DOCUMENT_ACCMAINT_ICR_FIELD_CANNOT_BE_EMPTY = "error.document.accountMaintenance.icrFieldCannotBeEmpty";
    public static final String ERROR_DOCUMENT_ACCMAINT_INCOME_STREAM_ACCT_NBR_CANNOT_BE_EMPTY = "error.document.accountMaintenance.incomeStreamAcctNbrCannotBeEmpty";
    public static final String ERROR_DOCUMENT_ACCMAINT_INCOME_STREAM_ACCT_COA_CANNOT_BE_EMPTY = "error.document.accountMaintenance.incomeStreamAcctCOACannotBeEmpty";
    public static final String ERROR_DOCUMENT_ACCTMAINT_INVALID_CG_RESPONSIBILITY = "error.document.accountMaintenance.invalidContractsAndGrantsResponsibility";
    public static final String ERROR_DOCUMENT_ACCMAINT_NONCAMS_SUBFUNDGROUP_WITH_BUILDING_CD = "error.document.accountMaintenance.nonCamsSubFundGroupWithBuildingCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_NONCAMS_SUBFUNDGROUP_WITH_CAMPUS_CD_FOR_BLDG = "error.document.accountMaintenance.nonCamsSubFundGroupWithCampusCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_ONLY_SUPERVISORS_CAN_EDIT = "error.document.accountMaintenance.onlySupervisorsCanEditClosedAccounts";
    public static final String ERROR_DOCUMENT_ACCMAINT_RPTS_TO_ACCT_MUST_BE_FLAGGED_FRINGE_BENEFIT = "error.document.accountMaintenance.reportsToAccountMustBeFringeBenefitFlaggedIfThisAccountFringeBenefitsIsFalse";
    public static final String ERROR_DOCUMENT_ACCMAINT_RPTS_TO_ACCT_REQUIRED_IF_FRINGE_BENEFIT_FALSE = "error.document.accountMaintenance.reportsToAccountRequiredIfFringeBenefitsFalse";
    public static final String ERROR_DOCUMENT_ACCT_PERIOD_INVALID_CLOSE_DATE = "error.document.acct.period.invalid.close.date";
    public static final String ERROR_DOCUMENT_BA_NO_INCOME_STREAM_ACCOUNT = "error.document.ba.noIncomeAccount";
    public static final String ERROR_DOCUMENT_CHART_REPORTS_TO_CHART_MUST_EXIST = "error.document.chart.reportsToChartMustExist";
    public static final String ERROR_DOCUMENT_DELEGATE_ACCOUNT_DELEGATES_LIMIT = "error.document.delegateGlobal.account.delegates.limit";
    public static final String ERROR_DOCUMENT_DELEGATE_CHANGE_NO_ACTIVE_DELEGATE = "error.document.delegateGlobal.noActiveDelegate";
    public static final String ERROR_DOCUMENT_DELEGATE_CHANGE_NO_DELEGATE = "error.document.delegateGlobal.noDelegate";
    public static final String ERROR_DOCUMENT_DELEGATE_MAXIMUM_ACCOUNT_DELEGATES = "error.document.delegateGlobal.max.account.delegates";
    public static final String ERROR_DOCUMENT_FISCAL_PERIOD_YEAR_DOESNT_EXIST = "error.document.fiscalPeriodMaintenance.yearDoesntExist";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_CFDA_NUMBER_INVALID = "error.document.accountGlobal.cfdaNumberInvalid";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_INVALID_ACCOUNT = "error.document.accountGlobalDetails.invalidAccount";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_INVALID_ORG = "error.document.accountGlobal.invalidOrganization";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_NO_ACCOUNTS = "error.document.accountGlobalDetails.noAccountsEntered";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_ONE_CHART_ONLY = "error.document.accountGlobalDetails.onlyOneChartAllowed";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_ONE_CHART_ONLY_ADDNEW = "error.document.accountGlobalDetails.onlyOneChartAllowedOnAddNew";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_PRINCIPAL_NAME_ACCOUNT_MANAGER_INVALID = "error.document.accountGlobal.principalNameAccountManagerInvalid";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_PRINCIPAL_NAME_ACCOUNT_SUPER_INVALID = "error.document.accountGlobal.principalNameAccountSupervisorInvalid";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCOUNT_PRINCIPAL_NAME_FISCAL_OFFICER_SUPER_INVALID = "error.document.accountGlobal.principalNameFiscalOfficerSupervisorInvalid";
    public static final String ERROR_DOCUMENT_GLOBAL_DELEGATEMAINT_PRIMARY_ROUTE_ALREADY_EXISTS_FOR_DOCTYPE = "error.document.globalDelegateMaintenance.primaryRouteAlreadyExistsForNewDocType";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_BUDGET_REVERSION_INCOMPLETE = "error.document.globalOrgReversion.incompleteBudgetReversion";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_CASH_REVERSION_INCOMPLETE = "error.document.globalOrgReversion.incompleteCashReversion";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_DUPLICATE_ORGS = "error.document.globalOrgReversion.duplicateOrgs";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_CHART = "error.document.globalOrgReversion.invalidChart";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_ORG_REVERSION_CATEGORY = "error.document.globalOrgReversion.invalidOrgReversionCategory";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_ORG_REVERSION_CODE = "error.document.globalOrgReversion.invalidOrgReversionCode";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_INVALID_ORGANIZATION = "error.document.globalOrgReversion.invalidOrganization";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_OBJECT_CODE_INVALID = "error.document.globalOrgReversion.objectCodeInvalid";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_NO_ORG_REVERSION = "error.document.globalOrgReversion.noOrgReversion";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_NO_ORGANIZATIONS = "error.document.globalOrgReversion.noOrganizations";
    public static final String ERROR_DOCUMENT_GLOBAL_ORG_REVERSION_NO_REVERSION_CODE = "error.document.globalOrgReversion.noReversionCode";
    public static final String ERROR_DOCUMENT_GLOBAL_SUBOBJECTMAINT_CHART_MUST_BE_SAME = "error.document.globalSubObjectCodeMaintenance.chartMustBeSame";
    public static final String ERROR_DOCUMENT_GLOBAL_SUBOBJECTMAINT_CHART_MUST_EXIST = "error.document.globalSubObjectCodeMaintenance.chartMustExist";
    public static final String ERROR_DOCUMENT_GLOBAL_SUBOBJECTMAINT_FISCAL_YEAR_MUST_BE_SAME = "error.document.globalSubObjectCodeMaintenance.fiscalYearMustBeSame";
    public static final String ERROR_DOCUMENT_GLOBAL_SUBOBJECTMAINT_FISCAL_YEAR_MUST_EXIST = "error.document.globalSubObjectCodeMaintenance.fiscalYearMustExist";
    public static final String ERROR_DOCUMENT_GLOBAL_SUBOBJECTMAINT_INVALID_OBJECT_CODE = "error.document.subObjCdGlobalDetails.invalidObjectCode";
    public static final String ERROR_DOCUMENT_GLOBAL_SUBOBJECTMAINT_NO_ACCOUNT = "error.document.globalSubObjectCodeMaintenance.noAccount";
    public static final String ERROR_DOCUMENT_GLOBAL_SUBOBJECTMAINT_NO_OBJECT_CODE = "error.document.globalSubObjectCodeMaintenance.noObjectCode";
    public static final String ERROR_DOCUMENT_ICR_ACCOUNT_USE_EXPENDITURE_ENTRY_WILDCARD_RESTRICTION_ON_SUB_ACCOUNT = "error.document.IndirectCostRecovery.accountUseExpenditureEntryWildcardRestrictionOnSubAccount";
    public static final String ERROR_DOCUMENT_ICR_CANNOT_BE_WILDCARD = "error.document.IndirectCostRecovery.cannotBeWildcard";
    public static final String ERROR_DOCUMENT_ICR_CHART_CODE_NOT_ONLY_WILDCARD = "error.document.IndirectCostRecovery.chartCodeNotOnlyWildcard";
    public static final String ERROR_DOCUMENT_ICR_EXISTENCE_CHART_CODE = "error.document.IndirectCostRecovery.existence.chartCode";
    public static final String ERROR_DOCUMENT_ICR_EXISTENCE_OBJECT_CODE = "error.document.IndirectCostRecovery.existence.objectCode";
    public static final String ERROR_DOCUMENT_ICR_EXISTENCE_OBJECT_CODE_DELETE = "error.document.IndirectCostRecovery.existence.objectCode.delete";
    public static final String ERROR_DOCUMENT_ICR_FIELD_MUST_BE_DASHES = "error.document.IndirectCostRecovery.fieldMustBeDashes";
    public static final String ERROR_DOCUMENT_ICR_MULTIPLE_WILDCARDS_ON_ITEM = "error.document.IndirectCostRecovery.multipleWildcardsOnItem";
    public static final String ERROR_DOCUMENT_ICR_RATE_NOT_FOUND = "error.document.IndirectCostRecovery.rateNotFound";
    public static final String ERROR_DOCUMENT_ICR_RATE_PERCENT_INVALID_FORMAT_SCALE = "error.document.IndirectCostRecovery.ratePercentsInvalidFormatScale";
    public static final String ERROR_DOCUMENT_ICR_RATE_PERCENT_INVALID_FORMAT_ZERO = "error.document.IndirectCostRecovery.ratePercentsInvalidFormatZero";
    public static final String ERROR_DOCUMENT_ICR_RATE_PERCENTS_NOT_EQUAL = "error.document.IndirectCostRecovery.ratePercentsNotEqual";
    public static final String ERROR_DOCUMENT_ICR_WILDCARD_NOT_VALID = "error.document.IndirectCostRecovery.wildcardNotValid";
    public static final String ERROR_DOCUMENT_ICR_WILDCARDS_MUST_MATCH = "error.document.IndirectCostRecovery.wildcardsMustMatch";
    public static final String ERROR_DOCUMENT_OBJCONSMAINT_ALREADY_EXISTS_AS_OBJ_LEVEL = "error.document.objConsMaintenance.alreadyExistsAsObjLevel";
    public static final String ERROR_DOCUMENT_OBJLEVELMAINT_ALREADY_EXISTS_AS_OBJ_CONS = "error.document.objLevelMaintenance.alreadyExistsAsObjCons";
    public static final String ERROR_DOCUMENT_OBJTYPE_INVALID_ACCT_CTGRY = "error.document.objType.invalidAccountCategory";
    public static final String ERROR_DOCUMENT_ORGMAINT_DEFAULT_ACCOUNT_NUMBER_REQUIRED = "error.document.orgMaintenance.defaultAccountNumberRequired";
    public static final String ERROR_DOCUMENT_ORGMAINT_END_DATE_GREATER_THAN_BEGIN_DATE = "error.document.orgMaintenance.endDateMustBeAfterBeginDate";
    public static final String ERROR_DOCUMENT_ORGMAINT_END_DATE_REQUIRED_ON_ORG_CLOSURE = "error.document.orgMaintenance.closingOrgMustHaveEndDate";
    public static final String ERROR_DOCUMENT_ORGMAINT_OPEN_CHILD_ACCOUNTS_ON_ORG_CLOSURE = "error.document.orgMaintenance.closingOrgMustHaveNoChildAccounts";
    public static final String ERROR_DOCUMENT_ORGMAINT_OPEN_CHILD_ORGS_ON_ORG_CLOSURE = "error.document.orgMaintenance.closingOrgMustHaveNoChildOrgs";
    public static final String ERROR_DOCUMENT_ORGMAINT_ONLY_ONE_TOP_LEVEL_ORG = "error.document.orgMaintenance.onlyOneTopLevelOrg";
    public static final String ERROR_DOCUMENT_ORGMAINT_REPORTING_ORG_CANNOT_BE_CIRCULAR_REF_TO_SAME_ORG = "error.document.orgMaintenance.reportingOrgCannotBeCircularRefToSameOrg";
    public static final String ERROR_DOCUMENT_ORGMAINT_REPORTING_ORG_CANNOT_BE_SAME_ORG = "error.document.orgMaintenance.reportingOrgCannotBeSameOrg";
    public static final String ERROR_DOCUMENT_ORGMAINT_REPORTING_ORG_MUST_BE_SAME_ORG = "error.document.orgMaintenance.reportingOrgMustBeSameOrg";
    public static final String ERROR_DOCUMENT_ORGMAINT_REPORTING_ORG_MUST_EXIST = "error.document.orgMaintenance.reportingOrgMustExist";
    public static final String ERROR_DOCUMENT_ORGMAINT_START_DATE_IN_PAST = "error.document.orgMaintenance.startDateMustBeGreaterThanOrEqualToToday";
    public static final String ERROR_DOCUMENT_ORGREVIEW_INVALID_DATES = "error.document.orgReview.invalidDates";
    public static final String ERROR_DOCUMENT_ORGREVIEW_INVALID_DOCUMENT_TYPE = "error.document.orgReview.invalidDocumentType";
    public static final String ERROR_DOCUMENT_ORGREVIEW_INVALID_END_DATE = "error.document.orgReview.invalidEndDate";
    public static final String ERROR_DOCUMENT_ORGREVIEW_INVALID_GROUP = "error.document.orgReview.invalidGroup";
    public static final String ERROR_DOCUMENT_ORGREVIEW_INVALID_PRINCIPLE = "error.document.orgReview.invalidPrincipal";
    public static final String ERROR_DOCUMENT_ORGREVIEW_INVALID_ROLE = "error.document.orgReview.invalidRole";
    public static final String ERROR_DOCUMENT_ORGREVIEW_INVALID_START_DATE = "error.document.orgReview.invalidStartDate";
    public static final String ERROR_DOCUMENT_ORGREVIEW_RECURSIVE_ROLE = "error.document.orgReview.recursiveRole";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_COST_SHARE_ACCOUNT_MAY_NOT_BE_CG_FUND_GROUP = "error.document.subAccountMaintenance.costSharingAccountCannotBeCGFundGroup";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_COST_SHARE_SECTION_INVALID = "error.document.subAccountMaintenance.costSharingSectionInvalid";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_ICR_SECTION_INVALID = "error.document.subAccountMaintenance.icrSectionInvalid";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_INVALID_SUBACCOUNT_TYPE_CODES = "error.document.subAccountMaintenance.invalidSubAccountTypeCodes";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_NOT_AUTHORIZED_CHANGE_CG_FIELDS = "error.document.subAccountMaintenance.cannotChangeCgValuesNotAuthorized";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_NOT_AUTHORIZED_ENTER_CG_FIELDS = "error.document.subAccountMaintenance.cannotEnterCgValuesNotAuthorized";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_CS_INVALID = "error.document.subAccountMaintenance.nonFundedAcctCsInvalid";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_ICR_INVALID = "error.document.subAccountMaintenance.nonFundedAcctIcrInvalid";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_SUB_ACCT_TYPE_CODE_INVALID = "error.document.subAccountMaintenance.nonFundedAcctSubAcctTypeCodeInvalid";
    public static final String ERROR_DOCUMENT_SUBACCTMAINT_RPTCODE_ALL_FIELDS_IF_ANY_FIELDS = "error.document.subAccountMaintenance.someReportingCodeFieldsEnteredButNotAll";
    public static final String ERROR_DOCUMENT_SUBOBJECTMAINT_ACCOUNT_MAY_NOT_BE_CLOSED = "error.document.subObjectCodeMaintenance.accountMayNotBeClosed";
    public static final String ERROR_DOCUMENT_TAX_REGION_CANT_ADD_PAST_OR_CURRENT_DATE_FOR_TAX_DISTRICT = "error.document.taxRegionMaintenance.cannotAddPastOrCurrentDateForTaxDistrict";
    public static final String ERROR_DOCUMENT_TAX_REGION_INVALID_COUNTY = "error.document.taxRegionMaintenance.invalidCounty";
    public static final String ERROR_DOCUMENT_TAX_REGION_INVALID_POSTAL_CODE = "error.document.taxRegionMaintenance.invalidPostalCode";
    public static final String ERROR_DOCUMENT_TAX_REGION_INVALID_STATE = "error.document.taxRegionMaintenance.invalidState";
    public static final String ERROR_OBJECT_CODE_ROUTING_ONE_AND_ONLY_ONE_REQUIRED =
            "error.object.code.routing.one.and.only.one.required";
    public static final String ERROR_DOCUMENT_TAX_REGION_TAX_RATE_BETWEEN0AND1 = "error.document.taxRegionMaintenance.taxRateShouldBeBetween0And1";
    public static final String INFO_DOCUMENT_DELEGATE_ACCOUNT_DELEGATES_LIMIT = "info.document.delegateGlobal.account.delegates.limit";
    public static final String INFO_DOCUMENT_DELEGATE_MAXIMUM_ACCOUNT_DELEGATES = "info.document.delegateGlobal.max.account.delegates";
    public static final String MESSAGE_ACCOUNT_DERIVED_ROLE_PRINCIPAL_INACTIVATED_ACCOUNT_DELEGATE_BLOCKED_INACTIVATION_INFORMATION = "message.accountDerivedRole.principalInactivated.accountDelegateBlockedInactivationInformation";
    public static final String MESSAGE_ACCOUNT_DERIVED_ROLE_PRINCIPAL_INACTIVATED_ACCOUNT_DELEGATE_INACTIVATED_INFORMATION = "message.accountDerivedRole.principalInactivated.accountDelegateInactivatedInformation";
    public static final String MESSAGE_ACCOUNT_DERIVED_ROLE_PRINCIPAL_INACTIVATED_ACCOUNT_SUPERVISOR_NOTIFICATION = "message.accountDerivedRole.principalInactivated.accountSupervisor.notification";
    public static final String MESSAGE_ACCOUNT_DERIVED_ROLE_PRINCIPAL_INACTIVATED_FISCAL_OFFICER_NOTIFICATION = "message.accountDerivedRole.principalInactivated.fiscalOfficer.notification";
    public static final String MESSAGE_ACCOUNT_DERIVED_ROLE_PRINCIPAL_INACTIVATED_FISCAL_OFFICER_PRIMARY_DELEGATE_NOTIFICATION = "message.accountDerivedRole.principalInactivated.fiscalOfficerPrimaryDelegate.notification";
    public static final String MESSAGE_ACCOUNT_DERIVED_ROLE_PRINCIPAL_INACTIVATED_FISCAL_OFFICER_SECONDARY_DELEGATE_NOTIFICATION = "message.accountDerivedRole.principalInactivated.fiscalOfficerSecondaryDelegate.notification";
    public static final String MESSAGE_ACCOUNT_DERIVED_ROLE_PRINCIPAL_INACTIVATED_NOTIFICATION_SUBJECT = "message.accountDerivedRole.principalInactivated.notification.subject";
    public static final String ORGANIZATION_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION = "note.trickleDownActivation.activatedOrganizationReversionDetail";
    public static final String ORGANIZATION_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION_ERROR_DURING_PERSISTENCE = "note.trickleDownActivation.activatedOrganizationReversionDetail.errorDuringPersistence";
    public static final String ORGANIZATION_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION = "note.trickleDownInactivation.inactivatedOrganizationReversionDetail";
    public static final String ORGANIZATION_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE = "note.trickleDownInactivation.inactivatedOrganizationReversionDetail.errorDuringPersistence";
    public static final String SUB_ACCOUNT_TRICKLE_DOWN_INACTIVATION = "note.trickleDownInactivation.inactivatedSubAccounts";
    public static final String SUB_ACCOUNT_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE = "note.trickleDownInactivation.inactivatedSubAccounts.errorDuringPersistence";
    public static final String SUB_ACCOUNT_TRICKLE_DOWN_INACTIVATION_RECORD_ALREADY_MAINTENANCE_LOCKED = "note.trickleDownInactivation.inactivatedSubAccounts.recordAlreadyMaintenanceLocked";
    public static final String SUB_OBJECT_TRICKLE_DOWN_INACTIVATION = "note.trickleDownInactivation.inactivatedSubObjects";
    public static final String SUB_OBJECT_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE = "note.trickleDownInactivation.inactivatedSubObjects.errorDuringPersistence";
    public static final String SUB_OBJECT_TRICKLE_DOWN_INACTIVATION_RECORD_ALREADY_MAINTENANCE_LOCKED = "note.trickleDownInactivation.inactivatedSubObjects.recordAlreadyMaintenanceLocked";

    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private COAKeyConstants() {
    }
}
