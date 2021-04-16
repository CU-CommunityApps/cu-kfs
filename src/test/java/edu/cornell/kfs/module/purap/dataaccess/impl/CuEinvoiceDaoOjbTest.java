package edu.cornell.kfs.module.purap.dataaccess.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.module.purap.dataaccess.CuEinvoiceDao;

/*
 * @todo probably remove this
 */
@ConfigureContext(session = UserNameFixture.ccs1)
public class CuEinvoiceDaoOjbTest extends KualiIntegTestBase {
    
    private CuEinvoiceDao cuEinvoiceDao;
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty( "oracle.jdbc.Trace", Boolean.TRUE.toString() );
        super.setUp();
        cuEinvoiceDao = SpringContext.getBean(CuEinvoiceDao.class);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cuEinvoiceDao = null;
        
    }
    
    public void testGetVendors() {
        List<String> vendorNumbers = new ArrayList<>();
        vendorNumbers.add("4113-0");
        vendorNumbers.add("15665-0");
        vendorNumbers.add("4527-0");
        List<VendorDetail>  details = cuEinvoiceDao.getVendors(vendorNumbers);
        assertEquals(3, details.size());
    }
    
    public void testgetFilteredVendorNumbers() {
        List<String> vendorNumbers = cuEinvoiceDao.getFilteredVendorNumbers("Finger");
        assertEquals(1, vendorNumbers.size());
    }


}
