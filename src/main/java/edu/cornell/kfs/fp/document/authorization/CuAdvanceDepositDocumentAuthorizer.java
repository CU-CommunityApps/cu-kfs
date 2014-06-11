package edu.cornell.kfs.fp.document.authorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.fp.document.AdvanceDepositDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSConstants;


public class CuAdvanceDepositDocumentAuthorizer extends FinancialSystemTransactionalDocumentAuthorizerBase{
    protected RoleService roleService;
    protected OrganizationService organizationService;
    
    
    @Override
    protected void addPermissionDetails(Object dataObject, Map<String, String> attributes) {
    	super.addPermissionDetails(dataObject, attributes);
        AdvanceDepositDocument advanceDepositDocument = (AdvanceDepositDocument) dataObject;
    }

    @Override
    protected void addRoleQualification(Object dataObject, Map<String, String> attributes) {
    	super.addRoleQualification(dataObject, attributes);
    	AdvanceDepositDocument advanceDepositDocument = (AdvanceDepositDocument) dataObject;
    }


    protected void addAttributes(AdvanceDepositDocument advanceDepositDocument, Map<String, String> attributes) {

        organizationService = SpringContext.getBean(OrganizationService.class);

        Person currentUser = GlobalVariables.getUserSession().getPerson();
        String roleId = getRoleService().getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.FINANCIAL, CUKFSConstants.SysKimApiConstants.ADVANCE_DEPOSIT_ORGANIZATION_REVIEWER_ROLE_NAME);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(roleId);

        List<Map<String, String>> qualifiers = new ArrayList<Map<String, String>>();
        qualifiers.addAll(getRoleService().getRoleQualifersForPrincipalByRoleIds(currentUser.getPrincipalId(), roleIds, new HashMap<String, String>()));
        
        if (qualifiers == null || qualifiers.isEmpty()) {
        	 qualifiers.addAll(getRoleService().getNestedRoleQualifiersForPrincipalByRoleIds(currentUser.getPrincipalId(), roleIds, new HashMap<String, String>()));
        }

        //getRoleQualifiersForPrincipalIncludingNested does not work for simple principal members so we try RoleMembershipInfo for principals
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

        Map<String, String> permissionDetails = new HashMap<String, String>();

        if (qualifiers != null && qualifiers.size() > 0) {

            for (Map<String, String> qualifier : qualifiers) {
                {
                    for (String key : qualifier.keySet()) {
                        if (KfsKimAttributes.CHART_OF_ACCOUNTS_CODE.equalsIgnoreCase(key)) {
                            permissionDetails.put(key, qualifier.get(key));
                        }
                        if (KfsKimAttributes.ORGANIZATION_CODE.equalsIgnoreCase(key)) {
                            permissionDetails.put(key, qualifier.get(key));
                        }
                        if (KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME.equalsIgnoreCase(key)) {
                            permissionDetails.put(key, qualifier.get(key));
                        }

                    }
                }

            }

            if (permissionDetails.containsKey(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME) && "AD".equalsIgnoreCase(permissionDetails.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME))) {
                if (permissionDetails.containsKey(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE) && permissionDetails.containsKey(KfsKimAttributes.ORGANIZATION_CODE)) {
                    if (advanceDepositDocument.getSourceAccountingLines() != null && advanceDepositDocument.getSourceAccountingLines().size() > 0) {
                        String chart = permissionDetails.get(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE);
                        String org = permissionDetails.get(KfsKimAttributes.ORGANIZATION_CODE);

                        for (Object accountingLine : advanceDepositDocument.getSourceAccountingLines()) {
                            SourceAccountingLine sourceAccountingLine = (SourceAccountingLine) accountingLine;
                            String accountingLineChart = sourceAccountingLine.getChartOfAccountsCode();
                            String accountingLineOrg = KFSConstants.EMPTY_STRING;
                            sourceAccountingLine.refreshReferenceObject("account");
                            
                            if(sourceAccountingLine.getAccount()!=null){
                             accountingLineOrg = sourceAccountingLine.getAccount().getOrganizationCode();
                            }

                            if (chart != null && chart.equalsIgnoreCase(accountingLineChart) && org != null && (org.equalsIgnoreCase(accountingLineOrg) || organizationService.isParentOrganization(accountingLineChart, accountingLineOrg, chart, org))) {
                                attributes.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, accountingLineChart);
                                attributes.put(KfsKimAttributes.ORGANIZATION_CODE, accountingLineOrg);
                            }
                        }
                    }
                }
            }
        }

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

    protected RoleService getRoleService() {
        if (roleService == null) {
            roleService = KimApiServiceLocator.getRoleService();
        }
        return roleService;
    }

}
