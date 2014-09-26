package edu.cornell.kfs.module.cab.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.module.cab.service.impl.CapitalAssetBuilderModuleServiceImpl;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.module.cab.fixture.AccountingDocumentFixture;
import edu.cornell.kfs.module.cab.fixture.CapitalAssetInformationFixture;

@ConfigureContext
public class CuCapitalAssetBuilderModuleServiceImplTest extends KualiTestBase {
	
	private CuCapitalAssetBuilderModuleServiceImpl cuCapitalAssetBuilderModuleServiceImpl;
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuCapitalAssetBuilderModuleServiceImpl.class);

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	cuCapitalAssetBuilderModuleServiceImpl = (CuCapitalAssetBuilderModuleServiceImpl) SpringContext.getBean(CapitalAssetBuilderModuleServiceImpl.class);
    }
    
    public void testCheckNewCapitalAssetFieldsExist () {
    	
    }
    
    public void testValidateAssetTagLocationLines() {
    	try {
    	// 	cuCapitalAssetBuilderModuleServiceImpl.validateAssetTagLocationLines(capitalAssetInformation, capitalAssetIndex, accountingDocument)

	    	Class cuCapitalAssetBuilderModuleServiceImplClass = CuCapitalAssetBuilderModuleServiceImpl.class;
	    	Class[] args_validateAssetTagLocationLines = new Class[3];
	    	args_validateAssetTagLocationLines[0] = CapitalAssetInformation.class;
	    	args_validateAssetTagLocationLines[1] = int.class;
	    	args_validateAssetTagLocationLines[2] = AccountingDocument.class;
	    	
	    	Method validateAssetTagLocationLines = cuCapitalAssetBuilderModuleServiceImplClass.getDeclaredMethod("validateAssetTagLocationLines", args_validateAssetTagLocationLines);
	    
	    	Object[] vals_validateAssetTagLocationLines = new Object[3];
	    	CapitalAssetInformation capitalAssetInformation = CapitalAssetInformationFixture.ONE.createCapitalAssetInformation();
	    	int index = 0;
	    	AccountingDocument ad = AccountingDocumentFixture.ONE.createAccountingDocument();

	    	vals_validateAssetTagLocationLines[0] = capitalAssetInformation;
	    	vals_validateAssetTagLocationLines[1] = index;
	    	vals_validateAssetTagLocationLines[2] = ad;
	    	
	    	Object result = validateAssetTagLocationLines.invoke(cuCapitalAssetBuilderModuleServiceImpl, vals_validateAssetTagLocationLines);
	    	
		} catch (NoSuchMethodException nmse) {
			nmse.printStackTrace();
		} catch (InvocationTargetException ite) {
			ite.printStackTrace();
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		}	
    }
}
