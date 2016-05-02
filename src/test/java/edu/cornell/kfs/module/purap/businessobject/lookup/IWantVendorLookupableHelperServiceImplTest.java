package edu.cornell.kfs.module.purap.businessobject.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kns.lookup.LookupableHelperService;

@ConfigureContext
@SuppressWarnings("deprecation")
public class IWantVendorLookupableHelperServiceImplTest extends KualiTestBase{
    private LookupableHelperService lookupableHelperServiceImpl;
    private Map<String, String> fieldValues;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fieldValues = new HashMap<String, String>();
        lookupableHelperServiceImpl =  
                LookupableSpringContext.getLookupableHelperService("iWantDocVendorLookupableHelperService");
        lookupableHelperServiceImpl.setBusinessObjectClass(VendorDetail.class);
    }
    
    public void testGetSearchResults() {
        fieldValues.put(VendorPropertyConstants.VENDOR_NAME, "China");

        
        List<Account> accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have more than one result", accounts.size() > 1);

    }

}
