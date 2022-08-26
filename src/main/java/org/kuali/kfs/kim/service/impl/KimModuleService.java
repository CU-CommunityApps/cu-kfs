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
package org.kuali.kfs.kim.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.role.RoleContract;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.impl.ModuleServiceBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kim.CuKimConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

/*
 * CU Customization:
 * Updated this class to allow for retrieving custom Person and Role inquiry URLs.
 */
public class KimModuleService extends ModuleServiceBase {

    @Override
    public List listPrimaryKeyFieldNames(Class businessObjectInterfaceClass) {
        // for Person objects (which are not real PersistableBOs) pull them through the person service
        if (Person.class.isAssignableFrom(businessObjectInterfaceClass)) {
            return Collections.singletonList(KimConstants.PrimaryKeyConstants.PRINCIPAL_ID);
        }

        // otherwise, use the default implementation
        return super.listPrimaryKeyFieldNames(businessObjectInterfaceClass);
    }

    // CU Customization: Add new method to check for custom inquiry URLs.
    public boolean hasCustomInquiryUrl(Class<?> inquiryBusinessObjectClass) {
        return Person.class.isAssignableFrom(inquiryBusinessObjectClass)
                || RoleContract.class.isAssignableFrom(inquiryBusinessObjectClass);
    }

    // CU Customization: Add custom URL parameters for Role inquiries, and make this method public.
    @Override
    public Map<String, String> getUrlParameters(String businessObjectClassAttribute,
            Map<String, String[]> parameters) {
        Map<String, String> urlParameters = new HashMap<>();
        for (String paramName : parameters.keySet()) {
            String[] parameterValues = parameters.get(paramName);
            if (parameterValues.length > 0) {
                urlParameters.put(paramName, parameterValues[0]);
            }
        }
        urlParameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, businessObjectClassAttribute);
        try {
            Class inquiryBusinessObjectClass = Class.forName(businessObjectClassAttribute);
            if (Person.class.isAssignableFrom(inquiryBusinessObjectClass)
                    || RoleContract.class.isAssignableFrom(inquiryBusinessObjectClass)) {
                urlParameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER,
                        KRADConstants.PARAM_MAINTENANCE_VIEW_MODE_INQUIRY);
            } else {
                urlParameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER,
                        KRADConstants.CONTINUE_WITH_INQUIRY_METHOD_TO_CALL);
            }
        } catch (Exception eix) {
            urlParameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER,
                    KRADConstants.CONTINUE_WITH_INQUIRY_METHOD_TO_CALL);
        }
        urlParameters.put(KRADConstants.PARAMETER_COMMAND, KewApiConstants.INITIATE_COMMAND);
        return urlParameters;
    }

    // CU Customization: Return custom URLs for Person and Role inquiries, and expand visibility to public.
    @Override
    public String getInquiryUrl(Class inquiryBusinessObjectClass) {
        if (Person.class.isAssignableFrom(inquiryBusinessObjectClass)) {
            return getCustomKimInquiryBaseUrl() + CuKimConstants.KIM_PERSON_INQUIRY_ACTION;
        } else if (RoleContract.class.isAssignableFrom(inquiryBusinessObjectClass)) {
            return getCustomKimInquiryBaseUrl() + CuKimConstants.KIM_ROLE_INQUIRY_ACTION;
        } else {
            return super.getInquiryUrl(inquiryBusinessObjectClass);
        }
    }

    // CU Customization. Added helper method to simplify building the custom inquiry URLs.
    protected String getCustomKimInquiryBaseUrl() {
        return KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY) + CUKFSConstants.SLASH;
    }
}
