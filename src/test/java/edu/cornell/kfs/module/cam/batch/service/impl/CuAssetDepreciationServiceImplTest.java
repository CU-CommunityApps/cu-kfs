package edu.cornell.kfs.module.cam.batch.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;

import org.kuali.kfs.module.cam.batch.service.AssetDepreciationService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.cam.businessobject.lookup.CuAssetLookupableHelperServiceImpl;

@ConfigureContext
public class CuAssetDepreciationServiceImplTest extends KualiTestBase {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAssetDepreciationServiceImpl.class);
	private CuAssetDepreciationServiceImpl cuAssetDepreciationServiceImpl;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();		

		cuAssetDepreciationServiceImpl = new CuAssetDepreciationServiceImpl();
	}
	
	public void testRunAssetDepreciationService() {
		try {
			Class assetDepreciationServiceImplClass = CuAssetDepreciationServiceImpl.class;
			Method runAssetDepreciation = assetDepreciationServiceImplClass.getDeclaredMethod("runAssetDepreciation");
			runAssetDepreciation.setAccessible(true);
			boolean result = (boolean) runAssetDepreciation.invoke(cuAssetDepreciationServiceImpl);
			LOG.info("Value of result returned by runAssetDepreciation: " + result);
			assertTrue("Run asset deprecation service value was true!", result);
		} catch (NoSuchMethodException nmse) {
			nmse.printStackTrace();
		} catch (InvocationTargetException ite) {
			ite.printStackTrace();
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		}
	}
	
	
}
