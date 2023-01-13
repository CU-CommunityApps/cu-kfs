/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package edu.cornell.kfs.sec.businessobject.lookup;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sec.SecKeyConstants;
import org.kuali.kfs.sec.service.AccessSecurityService;

import edu.cornell.kfs.coa.businessobject.lookup.CuOrganizationRoutingModelNameLookupableHelperServiceImpl;

/*
 * CU Customization:
 * This class is a renamed copy of base code's AccessSecurityAccountDelegateModelLookupableHelperServiceImpl
 * that has been modified to extend from CuOrganizationRoutingModelNameLookupableHelperServiceImpl instead.
 */
public class CuAccessSecurityAccountDelegateModelLookupableHelperServiceImpl
        extends CuOrganizationRoutingModelNameLookupableHelperServiceImpl {

    protected AccessSecurityService accessSecurityService;

    /**
     * Gets search results and passes to access security service to apply access restrictions
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        List<? extends BusinessObject> results = super.getSearchResults(fieldValues);

        int resultSizeBeforeRestrictions = results.size();
        accessSecurityService.applySecurityRestrictionsForLookup(results, GlobalVariables.getUserSession().getPerson());

        accessSecurityService.compareListSizeAndAddMessageIfChanged(resultSizeBeforeRestrictions, results,
                SecKeyConstants.MESSAGE_LOOKUP_RESULTS_RESTRICTED);

        return results;
    }

    /**
     * Gets search results and passes to access security service to apply access restrictions
     */
    @Override
    public List<? extends BusinessObject> getSearchResultsUnbounded(Map<String, String> fieldValues) {
        List<? extends BusinessObject> results = super.getSearchResultsUnbounded(fieldValues);

        int resultSizeBeforeRestrictions = results.size();
        accessSecurityService.applySecurityRestrictionsForLookup(results, GlobalVariables.getUserSession().getPerson());

        accessSecurityService.compareListSizeAndAddMessageIfChanged(resultSizeBeforeRestrictions, results,
                SecKeyConstants.MESSAGE_LOOKUP_RESULTS_RESTRICTED);

        return results;
    }

    public void setAccessSecurityService(AccessSecurityService accessSecurityService) {
        this.accessSecurityService = accessSecurityService;
    }

}
