package edu.cornell.kfs.module.ld.service.impl;

import java.io.IOException;

import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.ld.service.CULaborObjectService;

@ConfigureContext
public class CULaborObjectServiceImplTest extends KualiTestBase {

    private CULaborObjectService cuLaborObjectService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cuLaborObjectService = SpringContext.getBean(CULaborObjectService.class);
    }
    
    public void testGetByPrimaryKey() throws IOException {
    	LaborObject laborObject = cuLaborObjectService.getByPrimaryKey(2013, "IT", "5150");
    	assertEquals("S", laborObject.getFinancialObjectFringeOrSalaryCode());
    }
    
    public void testGetByPrimaryKeyBad() throws IOException {
    	LaborObject laborObject = cuLaborObjectService.getByPrimaryKey(2013, "IT", "1289");
    	assertNull(laborObject);
    }

}
