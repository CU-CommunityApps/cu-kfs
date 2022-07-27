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
package edu.cornell.kfs.coa.businessobject.lookup;

import org.kuali.kfs.coa.businessobject.AccountDelegateGlobal;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.lookup.OrganizationRoutingModelNameLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kns.document.authorization.BusinessObjectRestrictions;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * This class overrides the getBackLocation, getReturnUrl, setFieldConversions and getActionUrls for {@link OrganizationRoutingModelName}
 */
@SuppressWarnings("deprecation")
public class CuOrganizationRoutingModelNameLookupableHelperServiceImpl extends OrganizationRoutingModelNameLookupableHelperServiceImpl {

    @Override
    public HtmlData getReturnUrl(BusinessObject businessObject, LookupForm lookupForm, List returnKeys, BusinessObjectRestrictions businessObjectRestrictions) {
        String originalBackLocation = this.backLocation;
        Map<String, String> parameters = getParameters(businessObject, lookupForm.getFieldConversions(), lookupForm.getLookupableImplServiceName(), returnKeys);
        parameters.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.MAINTENANCE_NEW_WITH_EXISTING_ACTION);
        parameters.put(KFSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, AccountDelegateGlobal.class.getName());
        parameters.put(KFSConstants.OVERRIDE_KEYS, "modelName" + KFSConstants.FIELD_CONVERSIONS_SEPERATOR + "modelChartOfAccountsCode"
                + KFSConstants.FIELD_CONVERSIONS_SEPERATOR + "modelOrganizationCode");
        setBackLocation(KFSConstants.MAINTENANCE_ACTION);
        AnchorHtmlData htmlData = (AnchorHtmlData) getReturnAnchorHtmlData(businessObject, parameters, lookupForm, returnKeys, businessObjectRestrictions);
        // set this to prevent breaking Account Delegate Model returnLocation
        setBackLocation(originalBackLocation);
        return htmlData;
    }

}
