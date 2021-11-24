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
package org.kuali.kfs.coa.businessobject.lookup;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.bo.BusinessObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class overrides the base getActionUrls method.
 * CU Customization KFSPTS-23120 investigate NPE
 */
public class KualiAccountLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {
    
    /*
     * CU Customization KFSPTS-23120
     */
    private static final Logger LOG = LogManager.getLogger();

    private PermissionService permissionService;

    /**
     * If the account is not closed or the user is an Administrator the "edit" link is added The "copy" link is added
     * for Accounts.
     *
     * @return links to edit and copy maintenance action for the current maintenance record.
     */
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        Account theAccount = (Account) businessObject;
        List<HtmlData> anchorHtmlDataList = new ArrayList<>();
        Person user = GlobalVariables.getUserSession().getPerson();
        AnchorHtmlData urlDataCopy = getUrlData(businessObject, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL, pkNames);

        if (theAccount.isActive()) {
            anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL, pkNames));
        } else {
            String principalId = user.getPrincipalId();
            String namespaceCode = KFSConstants.PermissionNames.EDIT_INACTIVE_ACCOUNT.namespace;
            String permissionName = KFSConstants.PermissionNames.EDIT_INACTIVE_ACCOUNT.name;

            boolean isAuthorized = permissionService.hasPermission(principalId, namespaceCode, permissionName);

            if (isAuthorized) {
                anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL, pkNames));
            } else {
                urlDataCopy.setPrependDisplayText("&nbsp;&nbsp;&nbsp;&nbsp;");
            }
        }
        anchorHtmlDataList.add(urlDataCopy);

        return anchorHtmlDataList;
    }

    /**
     * Overridden to changed the "closed" parameter to an "active" parameter.
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> parameters) {
        if (parameters.containsKey(KFSPropertyConstants.CLOSED)) {
            final String closedValue = parameters.get(KFSPropertyConstants.CLOSED);

            if (closedValue != null && closedValue.length() != 0) {
                if ("Y1T".contains(closedValue)) {
                    parameters.put(KFSPropertyConstants.ACTIVE, "N");
                } else if ("N0F".contains(closedValue)) {
                    parameters.put(KFSPropertyConstants.ACTIVE, "Y");
                }
            }

            parameters.remove(KFSPropertyConstants.CLOSED);
        }
        if (parameters.containsKey(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_USER + ".principalName")) {
            String foPrincipalName = parameters.get(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_USER + ".principalName");
            if (StringUtils.isNotBlank(foPrincipalName)) {
                String foPrincipalId = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(foPrincipalName).getPrincipalId();
                parameters.put(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_SYSTEM_IDENTIFIER, foPrincipalId);
                parameters.remove(KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_USER + ".principalName");
            }
        }
        if (parameters.containsKey(KFSPropertyConstants.ACCOUNT_SUPERVISORY_USER + ".principalName")) {
            String superPrincipalName = parameters.get(KFSPropertyConstants.ACCOUNT_SUPERVISORY_USER + ".principalName");
            if (StringUtils.isNotBlank(superPrincipalName)) {
                
                /*
                 * CU Customization KFSPTS-23120 investigate NPE
                 * Moved the principalByPrincipalName to a variable to more clearly see what aspect is causing the NPE
                 */
                try {
                    Principal principalByPrincipalName = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(superPrincipalName);
                    String superPrincipalId = principalByPrincipalName.getPrincipalId();
                    parameters.put(KFSPropertyConstants.ACCOUNTS_SUPERVISORY_SYSTEMS_IDENTIFIER, superPrincipalId);
                } catch (NullPointerException npe ) {
                    LOG.error("getSearchResults, got an NPE trying to get the super principle ID for superPrincipalName: " + 
                            superPrincipalName, npe);
                    throw npe;
                }
                
                parameters.remove(KFSPropertyConstants.ACCOUNT_SUPERVISORY_USER + ".principalName");
            }
        }
        if (parameters.containsKey(KFSPropertyConstants.ACCOUNT_MANAGER_USER + ".principalName")) {
            String mgrPrincipalName = parameters.get(KFSPropertyConstants.ACCOUNT_MANAGER_USER + ".principalName");
            if (StringUtils.isNotBlank(mgrPrincipalName)) {
                String foPrincipalId = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(mgrPrincipalName).getPrincipalId();
                parameters.put(KFSPropertyConstants.ACCOUNT_MANAGER_SYSTEM_IDENTIFIER, foPrincipalId);
                parameters.remove(KFSPropertyConstants.ACCOUNT_MANAGER_USER + ".principalName");
            }
        }
        return super.getSearchResults(parameters);
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
}
