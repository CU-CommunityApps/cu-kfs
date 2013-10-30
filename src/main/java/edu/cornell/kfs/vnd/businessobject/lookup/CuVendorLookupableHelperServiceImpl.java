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
package edu.cornell.kfs.vnd.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.lookup.VendorLookupableHelperServiceImpl;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.util.BeanPropertyComparator;

import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

public class CuVendorLookupableHelperServiceImpl extends VendorLookupableHelperServiceImpl {
	private static final long serialVersionUID = 1L;
	private CuVendorDao vendorDao;

    /**
     * Overrides the getSearchResults in the super class so that we can do some customization in our vendor lookup. For example, for
     * vendor name as the search criteria, we want to search both the vendor detail table and the vendor alias table for the vendor
     * name. Display the vendor's default address state in the search results.
     *
     * @see org.kuali.rice.kns.lookup.Lookupable#getSearchResults(java.util.Map)
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
	@Override
    public List<BusinessObject> getSearchResults(Map<String, String> fieldValues) {

        super.setBackLocation((String) fieldValues.get(KFSConstants.BACK_LOCATION));
        super.setDocFormKey((String) fieldValues.get(KFSConstants.DOC_FORM_KEY));

        List<BusinessObject> searchResults = new ArrayList<BusinessObject>();

        searchResults = vendorDao.getSearchResults(fieldValues);

        for (BusinessObject bo : searchResults) {
        	VendorDetail vd = (VendorDetail) bo;
        	updateDefaultVendorAddress(vd);
        }
        for (BusinessObject businessObject :searchResults) {
        	VendorDetail vendor = (VendorDetail) businessObject;
            if (!vendor.isVendorParentIndicator()) {
                // find the parent object in the details collection and add that§
                for (BusinessObject tmpObject : searchResults) {
                	VendorDetail tmpVendor = (VendorDetail) tmpObject;
                    if (tmpVendor.getVendorHeaderGeneratedIdentifier().equals(vendor.getVendorHeaderGeneratedIdentifier()) 
                    		&& tmpVendor.isVendorParentIndicator()) {
                        vendor.setVendorName(tmpVendor.getVendorName() + " > " + vendor.getVendorName());
                        break;
                    }
                }
            }
        }


        // sort list if default sort column given
        List<String> defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            Collections.sort(searchResults, new BeanPropertyComparator(getDefaultSortColumns(), true));
        }

        return searchResults;
    }

    private void updateDefaultVendorAddress(VendorDetail vendor) {
        VendorAddress defaultAddress = vendorService.getVendorDefaultAddress(vendor.getVendorAddresses(),
        		vendor.getVendorHeader().getVendorType().getAddressType().getVendorAddressTypeCode(), "");
        if (defaultAddress != null) {
            if (defaultAddress.getVendorState() != null) {
                vendor.setVendorStateForLookup(defaultAddress.getVendorState().getName());
            }
            vendor.setDefaultAddressLine1(defaultAddress.getVendorLine1Address());
            vendor.setDefaultAddressLine2(defaultAddress.getVendorLine2Address());
            vendor.setDefaultAddressCity(defaultAddress.getVendorCityName());
            vendor.setDefaultAddressPostalCode(defaultAddress.getVendorZipCode());
            vendor.setDefaultAddressStateCode(defaultAddress.getVendorStateCode());
            vendor.setDefaultAddressInternationalProvince(defaultAddress.getVendorAddressInternationalProvinceName());
            vendor.setDefaultAddressCountryCode(defaultAddress.getVendorCountryCode());
            vendor.setDefaultFaxNumber(defaultAddress.getVendorFaxNumber());
        }
    }

    public void setVendorDao(CuVendorDao vendorDao) {
    	this.vendorDao = vendorDao;
    }


}
