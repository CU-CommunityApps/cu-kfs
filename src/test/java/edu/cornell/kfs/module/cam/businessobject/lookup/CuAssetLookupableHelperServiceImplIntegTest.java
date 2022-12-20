package edu.cornell.kfs.module.cam.businessobject.lookup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;

@ConfigureContext
public class CuAssetLookupableHelperServiceImplIntegTest extends KualiIntegTestBase {

	private static final Logger LOG = LogManager.getLogger();
	private CuAssetLookupableHelperServiceImpl cuAssetLookupableHelperServiceImpl;
	private Class cuAssetLookupableHelperServiceImplClass;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		cuAssetLookupableHelperServiceImplClass = CuAssetLookupableHelperServiceImpl.class;
		cuAssetLookupableHelperServiceImpl = new CuAssetLookupableHelperServiceImpl();
		cuAssetLookupableHelperServiceImpl.setBusinessObjectClass(Asset.class);

	}
	
	public void testGetSearchResultsHelper() 
	{
		try {

			//argument types for getSearchResultsHelper
			Class[] args_getSearchResultsHelper = new Class[2];
			args_getSearchResultsHelper[0] = Map.class;
			args_getSearchResultsHelper[1] = boolean.class;

			//values to use as method parameters for getSearchResultsHelper
			Object[] vals_getSearchResultsHelper = new Object[2];
			HashMap<String,String> hm = new HashMap<String,String>();
			hm.put("createDate", ">=01/01/2013");
			Boolean unbounded = new Boolean(false);
			Boolean bounded = new Boolean(true);
			vals_getSearchResultsHelper[0] = hm;
			vals_getSearchResultsHelper[1] = bounded;
			
			Method m_getSearchResultsHelper = cuAssetLookupableHelperServiceImplClass.getDeclaredMethod("getSearchResultsHelper", args_getSearchResultsHelper);
				
			Object result = m_getSearchResultsHelper.invoke(cuAssetLookupableHelperServiceImpl, vals_getSearchResultsHelper);
			CollectionIncomplete resultArray = (CollectionIncomplete) result;
			
			assertTrue("size of array should be greater than zero", 0!=resultArray.size());

			LOG.info("Array size (bounded = true): " + resultArray.size());
			
			vals_getSearchResultsHelper[1] = unbounded;
			result = m_getSearchResultsHelper.invoke(cuAssetLookupableHelperServiceImpl, vals_getSearchResultsHelper);
			resultArray = (CollectionIncomplete) result;
			
			LOG.info("Array size (bounded = false): " + resultArray.size());

			assertTrue("size of array should be greater than zero", 0!=resultArray.size());

			
			hm.clear();
			hm.put("createDate", ">=01/01/2014");

			vals_getSearchResultsHelper[0] = hm;
			vals_getSearchResultsHelper[1] = unbounded;
			
			Object unfilteredResult = m_getSearchResultsHelper.invoke(cuAssetLookupableHelperServiceImpl, vals_getSearchResultsHelper);
			
			CollectionIncomplete unfilteredArray = (CollectionIncomplete) unfilteredResult;

			//using this value in the search criteria results in the excludeBlankOffCampusLocations method being invoked
			hm.put("assetLocations.assetLocationTypeCode", "O");
			vals_getSearchResultsHelper[0] = hm;
			vals_getSearchResultsHelper[1]= unbounded;
			
			Object filteredResult = m_getSearchResultsHelper.invoke(cuAssetLookupableHelperServiceImpl, vals_getSearchResultsHelper);
			
			ArrayList filteredArray = (ArrayList) filteredResult;
			
			
			LOG.info("unfiltered array size: " + unfilteredArray.size());
			LOG.info("filtered array size: " + filteredArray.size());
			
			assertTrue("Array of results excluding blank off campus locations is smaller than the array not excluding them", filteredArray.size()<unfilteredArray.size() );
									
		} catch (NoSuchMethodException nmse) {
			nmse.printStackTrace();
		} catch (InvocationTargetException ite) {
			ite.printStackTrace();
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		}	
		
	}	

}
