package edu.cornell.kfs.coa.businessobject.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.AccountDelegateModel;
import org.kuali.kfs.kns.document.authorization.BusinessObjectRestrictionsBase;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;

@SuppressWarnings("deprecation")
@ConfigureContext
public class CuOrganizationRoutingModelNameLookupableHelperServiceImplIntegTest extends KualiIntegTestBase {

    private LookupableHelperService lookupableHelperServiceImpl;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        lookupableHelperServiceImpl =  
                LookupableSpringContext.getLookupableHelperService("organizationRoutingModelLookupableHelperService");
    }
    
    public void testGetReturnUrl() {
        BusinessObjectRestrictionsBase businessObjectRestrictions = new BusinessObjectRestrictionsBase();
        
        Map<String,String> fieldConversions = new HashMap<String, String>();
        fieldConversions.put("organizationCode", "modelOrganizationCode");
        fieldConversions.put("chartOfAccountsCode", "modelChartOfAccountsCode");
        fieldConversions.put("accountDelegateModelName", "modelName");
        
        lookupableHelperServiceImpl.setFieldConversions(fieldConversions);
        lookupableHelperServiceImpl.setBusinessObjectClass(AccountDelegateModel.class);
        LookupForm lookupForm = new LookupForm();
        lookupForm.setFieldConversions(fieldConversions);
        lookupForm.setLookupableImplServiceName("organizationRoutingModelLookupable");
        
        AccountDelegateModel accountDelegateModel = new AccountDelegateModel();
        accountDelegateModel.setAccountDelegateModelName("Stuff");
        accountDelegateModel.setChartOfAccountsCode("IT");
        accountDelegateModel.setOrganizationCode("1000");
        
        List<String> returnKeys = new ArrayList<String>();
        returnKeys.add("chartOfAccountsCode");
        returnKeys.add("organizationCode");
        returnKeys.add("accountDelegateModelName");
        
        String befoerBackLocation = lookupForm.getBackLocation();
        HtmlData results = lookupableHelperServiceImpl.getReturnUrl(accountDelegateModel, lookupForm, returnKeys, businessObjectRestrictions);
        assertEquals("return value", results.getDisplayText());
        assertEquals(befoerBackLocation, lookupForm.getBackLocation());

    }

    
}
