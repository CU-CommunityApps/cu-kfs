package edu.cornell.kfs.gl.businessobject.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.kns.lookup.LookupableHelperService;

import edu.cornell.kfs.gl.businessobject.CurrentAccountBalance;

@ConfigureContext
public class CurrentAccountBalanceLookupableHelperServiceImplTest extends KualiTestBase{

    private LookupableHelperService lookupableHelperServiceImpl;
    private Map<String, String> fieldValues;
    private Integer universityFiscalYear;
    
    private static final String CHART = "IT";
    private static final String ACCOUNT_NUMBER = "G254700";
    private static final String ORGANIZATION_CODE = "0100";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fieldValues = new HashMap<String, String>();
        lookupableHelperServiceImpl =  
                LookupableSpringContext.getLookupableHelperService("glCurrentAccountBalanceLookupableHelperService");
        lookupableHelperServiceImpl.setBusinessObjectClass(CurrentAccountBalance.class);
        
        universityFiscalYear = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
    }
    
    public void testGetSearchResults() {
    	
       	fieldValues.put("universityFiscalYear", universityFiscalYear.toString() );
    	fieldValues.put("account.chartOfAccountsCode", CHART);
        fieldValues.put("account.accountNumber", ACCOUNT_NUMBER);

        
        List<CurrentAccountBalance> accounts = (List<CurrentAccountBalance>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should have results", accounts.size() > 0);
        
    	fieldValues.put("universityFiscalYear", universityFiscalYear.toString() );
    	fieldValues.put("account.chartOfAccountsCode", CHART);
        fieldValues.put("account.accountNumber", KFSConstants.EMPTY_STRING);
        fieldValues.put("account.organizationCode", ORGANIZATION_CODE); 
        
        accounts = (List<CurrentAccountBalance>) lookupableHelperServiceImpl.getSearchResults(fieldValues);
        assertTrue("should not have results", accounts.isEmpty());

    }

}
