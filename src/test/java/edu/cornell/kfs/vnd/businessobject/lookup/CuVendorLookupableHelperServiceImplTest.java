package edu.cornell.kfs.vnd.businessobject.lookup;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.kfs.krad.lookup.CollectionIncomplete;

@ConfigureContext(session = ccs1)
public class CuVendorLookupableHelperServiceImplTest extends KualiTestBase{

	private LookupableHelperService vendorLookupableHelperServiceImpl;
	private Map<String,String> fieldValues;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		vendorLookupableHelperServiceImpl = LookupableSpringContext.getLookupableHelperService("vendorLookupableHelperService");
		vendorLookupableHelperServiceImpl.setBusinessObjectClass(VendorDetail.class);
	}
	
	public void testGetSearchResults() {
		Map<String,String> fieldValues = new HashMap<String,String>();
		fieldValues.put("vendorName", "Anak Inc");
		CollectionIncomplete<BusinessObject> theSearchResults = (CollectionIncomplete<BusinessObject>) vendorLookupableHelperServiceImpl.getSearchResults(fieldValues);
		
		assertTrue(theSearchResults.size() > 0 && theSearchResults.size() < 10 );
		
		Iterator<BusinessObject> it = theSearchResults.iterator();
		for (int i=0; i<theSearchResults.size(); i++) {
			VendorDetail vd = (VendorDetail) theSearchResults.get(i);
			System.out.println("vd: " + vd.getVendorName());
		}
		
		fieldValues.clear();
		
		theSearchResults = (CollectionIncomplete<BusinessObject>) vendorLookupableHelperServiceImpl.getSearchResults(fieldValues);
		
		assertTrue(theSearchResults.size()>=0);

		
	}
}
