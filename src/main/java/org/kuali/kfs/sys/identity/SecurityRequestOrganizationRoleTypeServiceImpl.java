/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.sys.identity;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.support.impl.KimRoleTypeServiceBase;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KNSServiceLocator;

/**
 * Role type service for SecurityRequestDocument approval roles
 * 
 * <p>
 * Converts various qualification attributes to organization for nested roles of organization type. Roles of this type can then be
 * assigned as the distributed authorizer for security request documents in Rice
 * </p>
 * 
 * @author rSmart Development Team
 */
public class SecurityRequestOrganizationRoleTypeServiceImpl extends KimRoleTypeServiceBase {

    private BusinessObjectService businessObjectService;

    /**
     * Converts qualification sets containing chart and account, or CG responsibility id to qualification set on chart and
     * organization code
     * 
     * @see org.kuali.rice.kim.service.support.impl.KimRoleTypeServiceBase#convertQualificationForMemberRoles(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, org.kuali.rice.kim.bo.types.dto.AttributeSet)
     */
    @Override
    public AttributeSet convertQualificationForMemberRoles(String namespaceCode, String roleName, String memberRoleNamespaceCode, String memberRoleName, AttributeSet qualification) {
        if (qualification == null) {
            return null;
        }

        Map<String, String> searchValues = new HashMap<String, String>();
        if (qualification.containsKey(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE) && qualification.containsKey(KfsKimAttributes.ACCOUNT_NUMBER)) {
            searchValues.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, qualification.get(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE));
            searchValues.put(KfsKimAttributes.ACCOUNT_NUMBER, qualification.get(KfsKimAttributes.ACCOUNT_NUMBER));
        }
        else if (qualification.containsKey(KfsKimAttributes.CONTRACTS_AND_GRANTS_ACCOUNT_RESPONSIBILITY_ID)) {
            searchValues.put(KfsKimAttributes.CONTRACTS_AND_GRANTS_ACCOUNT_RESPONSIBILITY_ID, qualification.get(KfsKimAttributes.CONTRACTS_AND_GRANTS_ACCOUNT_RESPONSIBILITY_ID));
        }

        AttributeSet newQualification = new AttributeSet();
        if (!searchValues.isEmpty()) {
            Account account = (Account) getBusinessObjectService().findByPrimaryKey(Account.class, searchValues);
            if (account != null) {
                newQualification.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, account.getChartOfAccountsCode());
                newQualification.put(KfsKimAttributes.ORGANIZATION_CODE, account.getOrganizationCode());

                return newQualification;
            }
        }
        // if we can't convert and organization was not sent originally, add no match qualification
        else if (!(qualification.containsKey(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE) && qualification.containsKey(KfsKimAttributes.ORGANIZATION_CODE))) {
            newQualification.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, "--");
            newQualification.put(KfsKimAttributes.ORGANIZATION_CODE, "----");
        }
        else {
            newQualification = qualification;
        }

        return newQualification;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KNSServiceLocator.getBusinessObjectService();
        }

        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
