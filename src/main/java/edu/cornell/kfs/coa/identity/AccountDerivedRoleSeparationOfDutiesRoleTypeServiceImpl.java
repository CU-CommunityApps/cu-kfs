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
package edu.cornell.kfs.coa.identity;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountDelegate;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.module.cg.service.ContractsAndGrantsService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.kim.role.DerivedRoleTypeServiceBase;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountDerivedRoleSeparationOfDutiesRoleTypeServiceImpl extends DerivedRoleTypeServiceBase {
    protected AccountService accountService;
    protected ContractsAndGrantsService contractsAndGrantsService;
    protected volatile WorkflowDocumentService workflowDocumentService;
    
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Attributes: Chart Code Account Number Requirements: - KFS-COA Account Supervisor: CA_ACCOUNT_T.ACCT_SPVSR_UNVL_ID - KFS-SYS
     * Fiscal Officer: CA_ACCOUNT_T.ACCT_FSC_OFC_UID - KFS-SYS Fiscal Officer Primary Delegate: CA_ACCT_DELEGATE_T.ACCT_DLGT_UNVL_ID
     * where ACCT_DLGT_PRMRT_CD = Y - KFS-SYS Fiscal Officer Secondary Delegate: CA_ACCT_DELEGATE_T.ACCT_DLGT_UNVL_ID where
     * ACCT_DLGT_PRMRT_CD = N - KFS-SYS Award Project Director: use the getProjectDirectorForAccount(String chartOfAccountsCode,
     * String accountNumber) method on the contracts and grants module service
     */
    @Override
    public List<RoleMembership> getRoleMembersFromDerivedRole(
            final String namespaceCode, final String roleName,
            final Map<String, String> qualification) {
        // validate received attributes
        validateRequiredAttributesAgainstReceived(qualification);
        final List<RoleMembership> members = new ArrayList<>();
        if (qualification != null && !qualification.isEmpty()) {
            final String chartOfAccountsCode = qualification.get(KimAttributes.CHART_OF_ACCOUNTS_CODE);
            final String accountNumber = qualification.get(KimAttributes.ACCOUNT_NUMBER);

            final String financialSystemDocumentTypeCodeCode = qualification.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME);
            String totalDollarAmount = qualification.get(KimAttributes.FINANCIAL_DOCUMENT_TOTAL_AMOUNT);

            final String fiscalOfficerPrincipalID = qualification.get(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_SYSTEM_IDENTIFIER);
            final String accountSupervisorPrincipalID = qualification.get(KFSPropertyConstants.ACCOUNTS_SUPERVISORY_SYSTEMS_IDENTIFIER);
            final String principalId = qualification.get(KimConstants.AttributeConstants.PRINCIPAL_ID);
            String documentNumber = qualification.get(KimConstants.AttributeConstants.DOCUMENT_NUMBER);

            // Default to 0 total amount
            if (StringUtils.isEmpty(totalDollarAmount)) {
                totalDollarAmount = getDefaultTotalAmount();
            }

            final Map<String, String> roleQualifier = new HashMap<>();
            roleQualifier.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
            roleQualifier.put(KimAttributes.ACCOUNT_NUMBER, accountNumber);

            if (chartOfAccountsCode == null && accountNumber == null && StringUtils.isNotBlank(principalId)
                    && principalIsNotSubmitterApproverOrInitiator(principalId, documentNumber)) {
                final RoleMembership roleMembershipInfo = getRoleMembershipWhenAccountInfoUnavailable(roleName, principalId, roleQualifier);
                if (ObjectUtils.isNotNull(roleMembershipInfo)) {
LOG.info("chartOfAccountsCode check:: roleMembershipInfo.getMemberId()={}=", roleMembershipInfo.getMemberId());
                    members.add(roleMembershipInfo);
                }
            }

            if (KFSConstants.SysKimApiConstants.ACCOUNT_SUPERVISOR_KIM_ROLE_NAME.equals(roleName)) {
                final Account account = getAccount(chartOfAccountsCode, accountNumber);
                if (account != null && principalIsNotSubmitterApproverOrInitiator(account.getAccountsSupervisorySystemsIdentifier(), documentNumber)) {
LOG.info("ACCOUNT_SUPERVISOR_KIM_ROLE_NAME when account!=null check:: account.getAccountsSupervisorySystemsIdentifier()={}=", account.getAccountsSupervisorySystemsIdentifier());
                    members.add(RoleMembership.Builder.create(null, null, account.getAccountsSupervisorySystemsIdentifier(), MemberType.PRINCIPAL, roleQualifier).build());
                }
                // only add the additional approver if they are different AND NOT the submitter or initator
                if (StringUtils.isNotBlank(accountSupervisorPrincipalID) && (account == null || !StringUtils.equals(accountSupervisorPrincipalID, account.getAccountsSupervisorySystemsIdentifier()))
                        || principalIsNotSubmitterApproverOrInitiator(accountSupervisorPrincipalID, documentNumber)) {
LOG.info("ACCOUNT_SUPERVISOR_KIM_ROLE_NAME when account!=null check:: accountSupervisorPrincipalID={}=", accountSupervisorPrincipalID);
                    members.add(RoleMembership.Builder.create(null, null, accountSupervisorPrincipalID, MemberType.PRINCIPAL, roleQualifier).build());
                }
            } else if (KFSConstants.SysKimApiConstants.FISCAL_OFFICER_KIM_ROLE_NAME.equals(roleName)) {
                final Account account = getAccount(chartOfAccountsCode, accountNumber);
                if (account != null && principalIsNotSubmitterApproverOrInitiator(account.getAccountsSupervisorySystemsIdentifier(), documentNumber)) {
LOG.info("FISCAL_OFFICER_KIM_ROLE_NAME when account!=null check:: account.getAccountFiscalOfficerSystemIdentifier()={}=", account.getAccountFiscalOfficerSystemIdentifier());
                    members.add(RoleMembership.Builder.create(null, null, account.getAccountFiscalOfficerSystemIdentifier(), MemberType.PRINCIPAL, roleQualifier).build());
                }
                // only add the additional approver if they are different
                if (StringUtils.isNotBlank(fiscalOfficerPrincipalID) && (account == null || !StringUtils.equals(fiscalOfficerPrincipalID, account.getAccountFiscalOfficerSystemIdentifier()))
                        || principalIsNotSubmitterApproverOrInitiator(fiscalOfficerPrincipalID, documentNumber)) {
LOG.info("FISCAL_OFFICER_KIM_ROLE_NAME when account!=null check:: fiscalOfficerPrincipalID={}=", fiscalOfficerPrincipalID);
                    members.add(RoleMembership.Builder.create(null, null, fiscalOfficerPrincipalID, MemberType.PRINCIPAL, roleQualifier).build());
                }
            } else if (KFSConstants.SysKimApiConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME.equals(roleName)) {
                final AccountDelegate delegate = getPrimaryDelegate(chartOfAccountsCode, accountNumber, financialSystemDocumentTypeCodeCode, totalDollarAmount);
                if (delegate != null && principalIsNotSubmitterApproverOrInitiator(delegate.getAccountDelegateSystemId(), documentNumber)) {
LOG.info("if:: FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME when delegate!=null check:: delegate.getAccountDelegateSystemId()={}=", delegate.getAccountDelegateSystemId());
                    roleQualifier.put(KimAttributes.FINANCIAL_SYSTEM_DOCUMENT_TYPE_CODE, delegate.getFinancialDocumentTypeCode());
                    roleQualifier.put(KimAttributes.FROM_AMOUNT, delegate.getFinDocApprovalFromThisAmt() == null
                            ? "0" : delegate.getFinDocApprovalFromThisAmt().toString());
                    roleQualifier.put(KimAttributes.TO_AMOUNT, delegate.getFinDocApprovalToThisAmount() == null
                            ? "NOLIMIT" : delegate.getFinDocApprovalToThisAmount().toString());
                    members.add(RoleMembership.Builder.create(null, null, delegate.getAccountDelegateSystemId(), MemberType.PRINCIPAL, roleQualifier).build());
                } else {
LOG.info("else:: FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME when delegate!=null check:: delegate.getAccountDelegateSystemId()={}=", delegate.getAccountDelegateSystemId());
                }
            } else if (KFSConstants.SysKimApiConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME.equals(roleName)) {
                final List<AccountDelegate> delegates = getSecondaryDelegates(chartOfAccountsCode, accountNumber, financialSystemDocumentTypeCodeCode, totalDollarAmount);
                for (final AccountDelegate delegate : delegates) {
                    if (principalIsNotSubmitterApproverOrInitiator(delegate.getAccountDelegateSystemId(), documentNumber)) {
LOG.info("if:: FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME when delegate!=null check:: delegate.getAccountDelegateSystemId()={}=", delegate.getAccountDelegateSystemId());
                        roleQualifier.put(KimAttributes.FINANCIAL_SYSTEM_DOCUMENT_TYPE_CODE, delegate.getFinancialDocumentTypeCode());
                        roleQualifier.put(KimAttributes.FROM_AMOUNT, delegate.getFinDocApprovalFromThisAmt() == null
                                ? "0" : delegate.getFinDocApprovalFromThisAmt().toString());
                        roleQualifier.put(KimAttributes.TO_AMOUNT, delegate.getFinDocApprovalToThisAmount() == null
                                ? "NOLIMIT" : delegate.getFinDocApprovalToThisAmount().toString());
                        members.add(RoleMembership.Builder.create(null, null, delegate.getAccountDelegateSystemId(), MemberType.PRINCIPAL, roleQualifier).build());
                    } else {
LOG.info("else:: FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME when delegate!=null check:: delegate.getAccountDelegateSystemId()={}=", delegate.getAccountDelegateSystemId());
                    }
                }
            } else if (KFSConstants.SysKimApiConstants.AWARD_SECONDARY_DIRECTOR_KIM_ROLE_NAME.equals(roleName)) {
                final Person person = getProjectDirectorForAccount(chartOfAccountsCode, accountNumber);
                if (person != null && principalIsNotSubmitterApproverOrInitiator(person.getPrincipalId(), documentNumber)) {
LOG.info("if:: AWARD_SECONDARY_DIRECTOR_KIM_ROLE_NAME when person!=null check:: person.getPrincipalId()={}=", person.getPrincipalId());
                    members.add(RoleMembership.Builder.create(null, null, person.getPrincipalId(), MemberType.PRINCIPAL, roleQualifier).build());
                } else {
LOG.info("else:: AWARD_SECONDARY_DIRECTOR_KIM_ROLE_NAME when delegate!=null check:: person.getPrincipalId()={}=", person.getPrincipalId());
                }
            }
        }
        return members;
    }

    // build membership information based on the given role name for the given principal when account information is unavailable
    protected RoleMembership getRoleMembershipWhenAccountInfoUnavailable(
            final String roleName, final String principalId,
            final Map<String, String> roleQualifier) {
        final Map<String, Object> fieldValues = new HashMap<>();

        RoleMembership roleMembership = null;

        if (KFSConstants.SysKimApiConstants.FISCAL_OFFICER_KIM_ROLE_NAME.equals(roleName)) {
            fieldValues.put(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_SYSTEM_IDENTIFIER, principalId);

            if (businessObjectService.countMatching(Account.class, fieldValues) > 0) {
                roleMembership = RoleMembership.Builder.create(null, null, principalId, MemberType.PRINCIPAL,
                        roleQualifier).build();
            }
        } else if (KFSConstants.SysKimApiConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME.equals(roleName)) {
            fieldValues.put(KFSPropertyConstants.ACCOUNT_DELEGATE_SYSTEM_ID, principalId);
            fieldValues.put(KFSPropertyConstants.ACCOUNTS_DELEGATE_PRMRT_INDICATOR, Boolean.TRUE);

            if (businessObjectService.countMatching(AccountDelegate.class, fieldValues) > 0) {
                roleMembership = RoleMembership.Builder.create(null, null, principalId, MemberType.PRINCIPAL,
                        roleQualifier).build();
            }
        } else if (KFSConstants.SysKimApiConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME.equals(roleName)) {
            fieldValues.put(KFSPropertyConstants.ACCOUNT_DELEGATE_SYSTEM_ID, principalId);
            fieldValues.put(KFSPropertyConstants.ACCOUNTS_DELEGATE_PRMRT_INDICATOR, Boolean.FALSE);

            if (businessObjectService.countMatching(AccountDelegate.class, fieldValues) > 0) {
                roleMembership = RoleMembership.Builder.create(null, null, principalId, MemberType.PRINCIPAL,
                        roleQualifier).build();
            }
        }

        return roleMembership;
    }

    protected Account getAccount(final String chartOfAccountsCode, final String accountNumber) {
        return accountService.getByPrimaryId(chartOfAccountsCode, accountNumber);
    }

    protected AccountDelegate getPrimaryDelegate(
            final String chartOfAccountsCode, final String accountNumber,
            final String fisDocumentTypeCode, final String totalDollarAmount) {
        return accountService.getPrimaryDelegationByExample(
                getDelegateExample(chartOfAccountsCode, accountNumber, fisDocumentTypeCode), totalDollarAmount);
    }

    protected List<AccountDelegate> getSecondaryDelegates(
            final String chartOfAccountsCode, final String accountNumber,
            final String fisDocumentTypeCode, final String totalDollarAmount) {
        return accountService.getSecondaryDelegationsByExample(
                getDelegateExample(chartOfAccountsCode, accountNumber, fisDocumentTypeCode), totalDollarAmount);
    }

    protected Person getProjectDirectorForAccount(final String chartOfAccountsCode, final String accountNumber) {
        return contractsAndGrantsService.getProjectDirectorForAccount(chartOfAccountsCode, accountNumber);
    }

    // Default to 0 total amount
    protected String getDefaultTotalAmount() {
        return "0";
    }

    protected AccountDelegate getDelegateExample(final String chartOfAccountsCode, final String accountNumber, final String fisDocumentTypeCode) {
        final AccountDelegate delegateExample = new AccountDelegate();
        delegateExample.setChartOfAccountsCode(chartOfAccountsCode);
        delegateExample.setAccountNumber(accountNumber);
        delegateExample.setFinancialDocumentTypeCode(fisDocumentTypeCode);
        return delegateExample;
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }
    
    public void setContractsAndGrantsService(final ContractsAndGrantsService contractsAndGrantsService) {
        this.contractsAndGrantsService = contractsAndGrantsService;
    }
    
    public String getSubmitter(String documentId) {
        String submitterPrincipalId = null;
        String documentPrincipalId = workflowDocumentService.getRoutedByPrincipalIdByDocumentId(documentId);
        List<ActionTaken> actionTakenDTOs = workflowDocumentService.getActionsTaken(documentId);
        for (ActionTaken actionTaken : actionTakenDTOs) {
            if (StringUtils.equals(documentPrincipalId, actionTaken.getPrincipalId())) {
                submitterPrincipalId = documentPrincipalId;
            }
        }
        LOG.info("getSubmitter is returning submitterPrincipalId={}=", submitterPrincipalId);
        return submitterPrincipalId;
    }
    
    public String getApproverOrInitiator(String documentNumber) {
        String approverOrInitiatorPrincipalId = null;
        String documentPrincipalId = workflowDocumentService.getDocumentInitiatorPrincipalId(documentNumber);
        List<ActionTaken> actionTakenDTOs = workflowDocumentService.getActionsTaken(documentNumber);
        for (ActionTaken actionTaken : actionTakenDTOs) {
            if (documentPrincipalId.equals(actionTaken.getPrincipalId())) {
                approverOrInitiatorPrincipalId = documentPrincipalId;
            }
        }
        LOG.info("getApproverOrInitiator is returning approverOrInitiatorPrincipalId={}=", approverOrInitiatorPrincipalId);
        return approverOrInitiatorPrincipalId;
    }
    
    private boolean principalIsSubmitter(String principalId, String documentNumber) {
        if (StringUtils.isNotBlank(principalId)
                && StringUtils.isNotBlank(documentNumber)
                && principalId.equalsIgnoreCase(getSubmitter(documentNumber))) {
            return true;
        }
        return false;
    }
    
    private boolean principalIsApproverOrInitiator(String principalId, String documentNumber) {
        if (StringUtils.isNotBlank(principalId)
                && StringUtils.isNotBlank(documentNumber)
                && principalId.equalsIgnoreCase(getApproverOrInitiator(documentNumber))) {
            return true;
        }
        return false;
    }
    
    private boolean principalIsNotSubmitterApproverOrInitiator(String principalId, String documentNumber) {
        if (principalIsSubmitter(principalId, documentNumber)
                || principalIsApproverOrInitiator(principalId, documentNumber)) {
            return false;
        }
        return true;
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

}
