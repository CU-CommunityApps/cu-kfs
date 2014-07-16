package edu.cornell.kfs.coa.businessobject.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.rice.kns.lookup.LookupableHelperService;
import org.kuali.rice.krad.util.GlobalVariables;

@ConfigureContext
@SuppressWarnings("deprecation")
public class AccountGlobalSearchLookupableHelperServiceImplTest extends KualiTestBase {

    private LookupableHelperService lookupableHelperServiceImpl;
    private Map<String, String> fieldValues;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fieldValues = new HashMap<String, String>();
        lookupableHelperServiceImpl =  
                LookupableSpringContext.getLookupableHelperService("accountGlobalLookupableHelperService");
        lookupableHelperServiceImpl.setBusinessObjectClass(Account.class);
    }
    
    public void testGetSearchResults() {
        fieldValues.put("chartOfAccountsCode", "IT");
        fieldValues.put("organizationCode", "044Q");
        fieldValues.put("useOrgHierarchy", "Y");
        fieldValues.put("closed", "N");
        
        List<Account> accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have results", accounts.size() > 0);
        
        fieldValues.put("chartOfAccountsCode", "IT");
        fieldValues.put("organizationCode", "044Q");
        fieldValues.put("useOrgHierarchy", "N");
        fieldValues.put("closed", "N");
        
        accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should not have results", accounts.isEmpty());

    }
    
    public void testValidateSearchParameters() {
        fieldValues.put("chartOfAccountsCode", "IT");
        fieldValues.put("organizationCode", "044Q");
        fieldValues.put("useOrgHierarchy", "Y");
        fieldValues.put("closed", "N");
        
        lookupableHelperServiceImpl.validateSearchParameters(fieldValues);
        
        assertTrue("should have any error messages", GlobalVariables.getMessageMap().hasNoMessages());
        
        fieldValues.clear();
        fieldValues.put("organizationCode", "044Q");
        fieldValues.put("useOrgHierarchy", "Y");
        fieldValues.put("closed", "N");

        lookupableHelperServiceImpl.validateSearchParameters(fieldValues);
        assertTrue("should have error for chart code", GlobalVariables.getMessageMap().getErrorMessages().containsKey(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE));

        GlobalVariables.getMessageMap().clearErrorMessages();
        
        fieldValues.clear();
        fieldValues.put("chartOfAccountsCode", "IT");
        fieldValues.put("useOrgHierarchy", "Y");
        fieldValues.put("closed", "N");

        lookupableHelperServiceImpl.validateSearchParameters(fieldValues);
        assertTrue("should have error for org code", GlobalVariables.getMessageMap().getErrorMessages().containsKey(KFSPropertyConstants.ORGANIZATION_CODE));
        
        GlobalVariables.getMessageMap().clearErrorMessages();
    }
    
}
