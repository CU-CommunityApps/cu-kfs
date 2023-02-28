package edu.cornell.kfs.module.purap.businessobject.lookup;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.lookup.VendorLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

public class IWantVendorLookupableHelperServiceImpl extends VendorLookupableHelperServiceImpl {
    
    private static final long serialVersionUID = 1L;
   

    /**
     * Customized search so that it adds * before and after the vendor name entered by the user
     * 
     * @see org.kuali.kfs.vnd.businessobject.lookup.VendorLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        if (fieldValues.containsKey(VendorPropertyConstants.VENDOR_NAME)) {
            String vendorName = fieldValues.get(VendorPropertyConstants.VENDOR_NAME);
            if (StringUtils.isNotEmpty(vendorName)) {
                vendorName = KFSConstants.WILDCARD_CHARACTER + vendorName + KFSConstants.WILDCARD_CHARACTER;

            } else {
                vendorName = KFSConstants.WILDCARD_CHARACTER;
            }
            fieldValues.put(VendorPropertyConstants.VENDOR_NAME, vendorName);
        }
        
        return super.getSearchResults(fieldValues);
    }
   
    
  
}
