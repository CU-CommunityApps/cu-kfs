package edu.cornell.kfs.coa.businessobject.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.rice.kns.lookup.LookupableHelperService;
import org.kuali.kfs.coa.businessobject.Account;

@ConfigureContext
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
        //{chartOfAccountsCode=IT, accountManagerUser.principalName=, subFundGroupCode=, accountTypeCode=, extension.appropriationAccountNumber=, backLocation=http://localhost:8080/kfs/kr/maintenance.do, accountSupervisoryUser.principalName=, docNum=5690634, organizationCode=044Q, accountNumber=, accountName=, extension.programCode=, extension.majorReportingCategoryCode=, closed=N, useOrgHierarchy=Y, docFormKey=2, accountFiscalOfficerUser.principalName=}
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
    
}
