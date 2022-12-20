package edu.cornell.kfs.sys.businessobject.lookup;

import static org.kuali.kfs.sys.fixture.UserNameFixture.mgw3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;

@ConfigureContext(session = mgw3)
public class CuUserProcurementProfileLookupableHelperServiceImplIntegTest extends KualiIntegTestBase {

    private LookupableHelperService lookupableHelperServiceImpl;
    private Map<String, String> fieldValues;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fieldValues = new HashMap<String, String>();
        lookupableHelperServiceImpl =  
                LookupableSpringContext.getLookupableHelperService("userProcurementProfileLookupableHelperService");
        lookupableHelperServiceImpl.setBusinessObjectClass(UserProcurementProfile.class);
    }
    
    /**
     * test search user profile with no accounts criteria specified
     */
    public void testGetSearchResultsWithNoAccountCriteria() {

        assertTrue("should have results", lookupableHelperServiceImpl.getSearchResults(fieldValues).size() > 0);

        List<UserProcurementProfile> userProcurementProfile = (List<UserProcurementProfile>)SpringContext.getBean(BusinessObjectService.class).findAll(UserProcurementProfile.class);
        fieldValues.put("profileUser.principalName", userProcurementProfile.get(0).getProfileUser().getPrincipalName());        
        assertTrue("should have results", lookupableHelperServiceImpl.getSearchResults(fieldValues).size() > 0);
        
        fieldValues.put("profileUser.principalName", "kfs");        
        assertTrue("should not have results", lookupableHelperServiceImpl.getSearchResults(fieldValues).isEmpty());
    }
    
    /**
     * test search user profile with accounts criteria specified
     */
    public void testGetSearchResultsWithAccountCriteria() {
        List<FavoriteAccount> favoriteAccounts = (List<FavoriteAccount>)SpringContext.getBean(BusinessObjectService.class).findAll(FavoriteAccount.class);

        fieldValues.put("favoriteAccounts.chartOfAccountsCode", favoriteAccounts.get(0).getChartOfAccountsCode());
        fieldValues.put("favoriteAccounts.accountNumber", favoriteAccounts.get(0).getAccountNumber());
        
        assertTrue("should have results", lookupableHelperServiceImpl.getSearchResults(fieldValues).size() > 0);
        
        fieldValues.clear();
        fieldValues.put("favoriteAccounts.chartOfAccountsCode","IT");
        assertTrue("should have results", lookupableHelperServiceImpl.getSearchResults(fieldValues).size() > 0);

        fieldValues.put("favoriteAccounts.chartOfAccountsCode", "CS");
        fieldValues.put("favoriteAccounts.accountNumber", "G254700");
        
        assertTrue("should not have results", lookupableHelperServiceImpl.getSearchResults(fieldValues).isEmpty());

    }

}
