package edu.cornell.kfs.coa.businessobject.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;

@ConfigureContext
@SuppressWarnings("deprecation")
public class AccountGlobalSearchLookupableHelperServiceImplIntegTest extends KualiIntegTestBase {

    private LookupableHelperService lookupableHelperServiceImpl;
    private Map<String, String> fieldValues;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fieldValues = new HashMap<>();
        lookupableHelperServiceImpl = LookupableSpringContext.getLookupableHelperService("accountGlobalLookupableHelperService");
        lookupableHelperServiceImpl.setBusinessObjectClass(Account.class);
    }
    
    public void testGetSearchResults() {
        setupSearchValues();
        
        List<Account> accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have results", accounts.size() > 0);

        setupSearchValues();
        fieldValues.put("useOrgHierarchy", "N");

        accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should not have results", accounts.isEmpty());
    }

    public void testGetSearchResultsFiscalOfficer() {
        setupSearchValues();
        fieldValues.put("accountFiscalOfficerUser.principalName", "tjv9");

        List<Account> accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have results", accounts.size() > 0);
    }

    public void testGetSearchResultsAccountManager() {
        setupSearchValues();
        fieldValues.put("accountManagerUser.principalName", "slk38");

        List<Account> accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have results", accounts.size() > 0);
    }

    public void testGetSearchResultsAccountSupervisor() {
        setupSearchValues();
        fieldValues.put("accountSupervisoryUser.principalName", "tjs33");

        List<Account> accounts = (List<Account>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have results", accounts.size() > 0);
    }

    public void testValidateSearchParameters() {
        setupSearchValues();

        lookupableHelperServiceImpl.validateSearchParameters(fieldValues);

        assertTrue("should not have any error messages", GlobalVariables.getMessageMap().hasNoMessages());

        fieldValues.clear();
        fieldValues.put("organizationCode", "0121");
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

    private void setupSearchValues() {
        fieldValues.put("chartOfAccountsCode", "IT");
        fieldValues.put("organizationCode", "0121");
        fieldValues.put("useOrgHierarchy", "Y");
        fieldValues.put("closed", "N");
    }

}
