/*
 * Copyright 2008 The Kuali Foundation Licensed under the Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/ecl2.php Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package edu.cornell.kfs.fp.document.authorization;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.RouteLevelNames;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.role.dto.RoleMembershipInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.RoleService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

/**
 * The default implementation of AccountingLineAuthorizer
 */
public class AdvanceDepositAccountingLineAuthorizer extends AccountingLineAuthorizerBase {
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
        String roleId = getRoleService().getRoleIdByName(KFSConstants.ParameterNamespaces.FINANCIAL, KFSConstants.SysKimConstants.ADVANCE_DEPOSIT_ORGANIZATION_REVIEWER_ROLE_NAME);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(roleId);

        List<AttributeSet> qualifiers = getRoleService().getRoleQualifiersForPrincipalIncludingNested(currentUser.getPrincipalId(), roleIds, new AttributeSet());

        // getRoleQualifiersForPrincipalIncludingNested does not work for simple principal members so we try
        // RoleMembershipInfo for principals
        if (qualifiers == null || qualifiers.isEmpty()) {
            List<RoleMembershipInfo> roleMemberships = getRoleService().getRoleMembers(roleIds, null);
            if (roleMemberships != null && roleMemberships.size() > 0) {
                for (RoleMembershipInfo roleMembershipInfo : roleMemberships) {
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

            for (AttributeSet qualifier : qualifiers) {
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
                        if (KfsKimAttributes.DOCUMENT_TYPE_NAME.equalsIgnoreCase(key)) {
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
        KualiWorkflowDocument workflowDoc = accountingDocument.getDocumentHeader().getWorkflowDocument();
        String currentRouteLevels = accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentRouteNodeNames();
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
            roleService = KIMServiceLocator.getRoleService();
        }
        return roleService;
    }

}
