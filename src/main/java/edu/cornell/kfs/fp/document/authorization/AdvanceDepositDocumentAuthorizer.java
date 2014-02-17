/*
 * Copyright 2014 The Kuali Foundation. Licensed under the Educational Community License, Version 1.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/ecl1.php Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package edu.cornell.kfs.fp.document.authorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.fp.document.AdvanceDepositDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.role.dto.RoleMembershipInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.RoleService;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.util.GlobalVariables;

public class AdvanceDepositDocumentAuthorizer extends FinancialSystemTransactionalDocumentAuthorizerBase {

    protected RoleService roleService;
    protected OrganizationService organizationService;

    @Override
    protected void addPermissionDetails(BusinessObject businessObject, Map<String, String> attributes) {

        super.addPermissionDetails(businessObject, attributes);
        AdvanceDepositDocument advanceDepositDocument = (AdvanceDepositDocument) businessObject;

        addAttributes(advanceDepositDocument, attributes);

    }

    @Override
    protected void addRoleQualification(BusinessObject businessObject, Map<String, String> attributes) {

        super.addRoleQualification(businessObject, attributes);
        AdvanceDepositDocument advanceDepositDocument = (AdvanceDepositDocument) businessObject;

        addAttributes(advanceDepositDocument, attributes);
    }

    protected void addAttributes(AdvanceDepositDocument advanceDepositDocument, Map<String, String> attributes) {

        organizationService = SpringContext.getBean(OrganizationService.class);

        Person currentUser = GlobalVariables.getUserSession().getPerson();
        String roleId = getRoleService().getRoleIdByName(KFSConstants.ParameterNamespaces.FINANCIAL, KFSConstants.SysKimConstants.ADVANCE_DEPOSIT_ORGANIZATION_REVIEWER_ROLE_NAME);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(roleId);

        List<AttributeSet> qualifiers = getRoleService().getRoleQualifiersForPrincipalIncludingNested(currentUser.getPrincipalId(), roleIds, new AttributeSet());

        //getRoleQualifiersForPrincipalIncludingNested does not work for simple principal members so we try RoleMembershipInfo for principals
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

        Map<String, String> permissionDetails = new HashMap<String, String>();

        if (qualifiers != null && qualifiers.size() > 0) {

            for (AttributeSet qualifier : qualifiers) {
                {
                    for (String key : qualifier.keySet()) {
                        if (KfsKimAttributes.CHART_OF_ACCOUNTS_CODE.equalsIgnoreCase(key)) {
                            permissionDetails.put(key, qualifier.get(key));
                        }
                        if (KfsKimAttributes.ORGANIZATION_CODE.equalsIgnoreCase(key)) {
                            permissionDetails.put(key, qualifier.get(key));
                        }
                        if (KfsKimAttributes.DOCUMENT_TYPE_NAME.equalsIgnoreCase(key)) {
                            permissionDetails.put(key, qualifier.get(key));
                        }

                    }
                }

            }

            if (permissionDetails.containsKey(KfsKimAttributes.DOCUMENT_TYPE_NAME) && "AD".equalsIgnoreCase(permissionDetails.get(KfsKimAttributes.DOCUMENT_TYPE_NAME))) {
                if (permissionDetails.containsKey(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE) && permissionDetails.containsKey(KfsKimAttributes.ORGANIZATION_CODE)) {
                    if (advanceDepositDocument.getSourceAccountingLines() != null && advanceDepositDocument.getSourceAccountingLines().size() > 0) {
                        String chart = permissionDetails.get(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE);
                        String org = permissionDetails.get(KfsKimAttributes.ORGANIZATION_CODE);

                        for (Object accountingLine : advanceDepositDocument.getSourceAccountingLines()) {
                            SourceAccountingLine sourceAccountingLine = (SourceAccountingLine) accountingLine;
                            String accountingLineChart = sourceAccountingLine.getChartOfAccountsCode();
                            String accountingLineOrg = sourceAccountingLine.getAccount().getOrganizationCode();

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

    protected RoleService getRoleService() {
        if (roleService == null) {
            roleService = KIMServiceLocator.getRoleService();
        }
        return roleService;
    }

}
