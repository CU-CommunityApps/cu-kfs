package edu.cornell.kfs.fp.document.authorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.fp.document.authorization.AdvanceDepositAccountingLineAuthorizer;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.KFSConstants.RouteLevelNames;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSConstants;


public class CuAdvanceDepositAccountingLineAuthorizer extends AdvanceDepositAccountingLineAuthorizer{
	
    protected RoleService roleService;

    /**
     * @see org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase#determineEditPermissionOnField(org.kuali.kfs.sys.document.AccountingDocument,
     *      org.kuali.kfs.sys.businessobject.AccountingLine, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public boolean determineEditPermissionOnField(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, String fieldName, boolean editablePage) {
        boolean editable = super.determineEditPermissionOnField(accountingDocument, accountingLine, accountingLineCollectionProperty, fieldName, editablePage);
        boolean orgReviewEditable = false;

        if (isDocumentStoppedInRouteNode(RouteLevelNames.ACCOUNTING_ORGANIZATION_HIERARCHY, accountingDocument)) {
            if (accountingLineCollectionProperty.equalsIgnoreCase(KFSPropertyConstants.NEW_SOURCE_LINE)) {
                orgReviewEditable = true;
            } else {
                orgReviewEditable = isOrgReviewEditable(accountingLine);

            }
        } else {
            orgReviewEditable = true;
        }

        return editable && orgReviewEditable;
    }

    /**
     * @see org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase#determineEditPermissionOnLine(org.kuali.kfs.sys.document.AccountingDocument,
     *      org.kuali.kfs.sys.businessobject.AccountingLine, java.lang.String, boolean, boolean)
     */
    @Override
    public boolean determineEditPermissionOnLine(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, boolean currentUserIsDocumentInitiator, boolean pageIsEditable) {
        boolean editable = super.determineEditPermissionOnLine(accountingDocument, accountingLine, accountingLineCollectionProperty, currentUserIsDocumentInitiator, pageIsEditable);
        boolean orgReviewEditable = false;
        if (isDocumentStoppedInRouteNode(RouteLevelNames.ACCOUNTING_ORGANIZATION_HIERARCHY, accountingDocument)) {

            if (ObjectUtils.isNull(accountingLine.getAccount()) || (ObjectUtils.isNotNull(accountingLine.getAccount()) && StringUtils.isBlank(accountingLine.getAccount().getAccountNumber()))) {
                orgReviewEditable = true;
            }

            else if (ObjectUtils.isNotNull(accountingLine.getAccount())) {
                orgReviewEditable = isOrgReviewEditable(accountingLine);
            }
        } else {
            orgReviewEditable = true;
        }

        return editable && orgReviewEditable;
    }
    
    @Override
    protected boolean approvedForUnqualifiedEditing(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, boolean currentUserIsDocumentInitiator) {
       return true;
    }

    /**
     * Determines if accounting line is org review editable
     * 
     * @param accountingLine
     * @return true if editable, false otherwise
     */
    private boolean isOrgReviewEditable(AccountingLine accountingLine) {
        boolean orgReviewEditable = false;
        OrganizationService organizationService = SpringContext.getBean(OrganizationService.class);

        Person currentUser = GlobalVariables.getUserSession().getPerson();
        String roleId = getRoleService().getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.FINANCIAL, CUKFSConstants.SysKimApiConstants.ADVANCE_DEPOSIT_ORGANIZATION_REVIEWER_ROLE_NAME);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(roleId);
        
        List<Map<String, String>> qualifiers = new ArrayList<Map<String, String>>();
        qualifiers.addAll(getRoleService().getRoleQualifersForPrincipalByRoleIds(currentUser.getPrincipalId(), roleIds, new HashMap<String, String>()));
        
        if (qualifiers == null || qualifiers.isEmpty()) {
        	 qualifiers.addAll(getRoleService().getNestedRoleQualifiersForPrincipalByRoleIds(currentUser.getPrincipalId(), roleIds, new HashMap<String, String>()));
        }
        // getRoleQualifiersForPrincipalIncludingNested does not work for simple principal members so we try
        // RoleMembershipInfo for principals
        if (qualifiers == null || qualifiers.isEmpty()) {
            List<RoleMembership> roleMemberships = getRoleService().getRoleMembers(roleIds, null);
            if (roleMemberships != null && roleMemberships.size() > 0) {
                for (RoleMembership roleMembershipInfo : roleMemberships) {
                    if (currentUser.getPrincipalId().equalsIgnoreCase(roleMembershipInfo.getMemberId())) {
                        qualifiers.add(roleMembershipInfo.getQualifier());
                    }
                }
            }
        }

        boolean chartMatch = false;
        boolean orgMatch = false;
        boolean docTypeMatch = false;
        String org = StringUtils.EMPTY;
        String chart = StringUtils.EMPTY;

        if (qualifiers != null && qualifiers.size() > 0) {

            for (Map<String, String> qualifier : qualifiers) {
                {
                    for (String key : qualifier.keySet()) {
                        if (KfsKimAttributes.CHART_OF_ACCOUNTS_CODE.equalsIgnoreCase(key)) {
                            if (qualifier.get(key) != null && qualifier.get(key).equalsIgnoreCase(accountingLine.getChartOfAccountsCode())) {
                                chartMatch = true;
                                chart = qualifier.get(key);
                            }
                        }
                        if (KfsKimAttributes.ORGANIZATION_CODE.equalsIgnoreCase(key)) {
                            if (qualifier.get(key) != null) {
                                org = qualifier.get(key);
                            }
                        }
                        if (KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME.equalsIgnoreCase(key)) {
                            if (qualifier.get(key) != null && qualifier.get(key).equalsIgnoreCase("AD")) {
                                docTypeMatch = true;
                            }
                        }

                    }
                    
                    
                    String acctLineOrgCode = KFSConstants.EMPTY_STRING;
                    accountingLine.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
                    if (accountingLine.getAccount() != null) {
                        acctLineOrgCode = accountingLine.getAccount().getOrganizationCode();
                    }
                    if ((chart.equalsIgnoreCase(accountingLine.getChartOfAccountsCode()) && org.equalsIgnoreCase(acctLineOrgCode)) || organizationService.isParentOrganization(accountingLine.getChartOfAccountsCode(), acctLineOrgCode, chart, org)) {
                        orgMatch = true;
                    }
                    if ((chart.equalsIgnoreCase(accountingLine.getChartOfAccountsCode()) && org.equalsIgnoreCase(accountingLine.getAccount().getOrganizationCode())) || organizationService.isParentOrganization(accountingLine.getChartOfAccountsCode(), accountingLine.getAccount().getOrganizationCode(), chart, org)) {
                        orgMatch = true;
                    }

                    if (chartMatch && orgMatch && docTypeMatch) {
                        orgReviewEditable = true;
                    }
                    
                    if(orgReviewEditable){
                        break;
                    }
                }

            }

        }

        

        return orgReviewEditable;

    }

    /**
     * Determines if document is in org review node for approval.
     * 
     * @param nodeName
     * @param accountingDocument
     * @return true if in org review node for approval, false otherwise
     */
    public boolean isDocumentStoppedInRouteNode(String nodeName, AccountingDocument accountingDocument) {
        WorkflowDocument workflowDoc = accountingDocument.getDocumentHeader().getWorkflowDocument();
        Set<String> currentRouteLevels = accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames();
        if (currentRouteLevels.contains(nodeName) && workflowDoc.isApprovalRequested()) {
            return true;
        }
        return false;
    }

    /**
     * Gets the roleService.
     * 
     * @return roleService
     */
    protected RoleService getRoleService() {
        if (roleService == null) {
            roleService = KimApiServiceLocator.getRoleService();
        }
        return roleService;
    }

}
