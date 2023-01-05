package edu.cornell.kfs.module.purap.businessobject.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;

@ConfigureContext
@SuppressWarnings("deprecation")
public class UnitOfMeasureLookupableHelperServiceImplIntegTest extends KualiIntegTestBase {
    private LookupableHelperService lookupableHelperServiceImpl;
    private Map<String, String> fieldValues;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fieldValues = new HashMap<String, String>();
        lookupableHelperServiceImpl =  
                LookupableSpringContext.getLookupableHelperService("unitOfMeasureLookupHelperService");
        lookupableHelperServiceImpl.setBusinessObjectClass(UnitOfMeasure.class);
    }
    
    public void testGetSearchResults() {
        fieldValues.put("itemUnitOfMeasureCode", "C");

        
        List<Account> accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have more than one result", accounts.size() > 1);

    }

}
