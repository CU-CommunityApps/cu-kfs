package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;

class PaymentWorksVendorSupplierDiversityServiceImplTest {
    private PaymentWorksVendorSupplierDiversityServiceImpl supplierSerivce;
    private PaymentWorksVendor pmwVendor;
    
    private static final String FEDERAL_CLASSIFICAITONS = "8(A) African American, 8(A) Hispanic American, 8(a) Native American (Includes Alaskan Natives, Native Hawaiians & Native Americans), 8(a) Asian Pacific American, 8(a) Subcontinent Asian Americans, Small Disadvantaged Business, LGBTBE (LGBT-Owned Businesses), HUB Zone (Historically Under-Utilized Small Busines, MBE (Minority Business Enterprise), SDB (Small Disadvantaged Business), SDVOSB (Service-Disabled Veteran-Owned Small Business), VOSB (Veteran-Owned Small Business           , WBE (Woman Business Enterprise), WOSB (Women-Owned Small Business), Ability One, Small Business";
    private static final String STATE_CLASSIFICAITONS  = "MBE (Minority Business Enterprise), WBE (Womens Business Enterprise), Disabled Veteran Owned Business";

    @BeforeEach
    public void setUp() throws Exception {
        Configurator.setLevel(PaymentWorksVendorSupplierDiversityServiceImpl.class.getName(), Level.DEBUG);
        supplierSerivce = new PaymentWorksVendorSupplierDiversityServiceImpl();
        supplierSerivce.setKfsSupplierDiversityDao(buildMockKfsSupplierDiversityDao());
        pmwVendor = new PaymentWorksVendor();
    }

    @AfterEach
    public void tearDown() throws Exception {
        supplierSerivce = null;
        pmwVendor = null;
    }
    
    private KfsSupplierDiversityDao buildMockKfsSupplierDiversityDao() {
        KfsSupplierDiversityDao dao = Mockito.mock(KfsSupplierDiversityDao.class);
        Map<String, SupplierDiversity> diversityMap = new HashMap<String, SupplierDiversity>();
        addSUpplierDiversityToMap(diversityMap, "8(A)", "8(A) African American");
        addSUpplierDiversityToMap(diversityMap, "8(A)", "8(A) Hispanic American");
        addSUpplierDiversityToMap(diversityMap, "MBE", "MBE (Minority Business Enterprise)");
        addSUpplierDiversityToMap(diversityMap, "WBE", "WBE (Womens Business Enterprise)");
        Mockito.when(dao.buildPmwToKfsSupplierDiversityMap()).thenReturn(diversityMap);
        return dao;
    }
    
    private void addSUpplierDiversityToMap(Map<String, SupplierDiversity> diversityMap, String code, String description) {
        SupplierDiversity diversity = new SupplierDiversity();
        diversity.setVendorSupplierDiversityCode(code);
        diversity.setVendorSupplierDiversityDescription(description);
        diversityMap.put(description, diversity);
        
    }

    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithRealExamples() {
        pmwVendor.setFederalDivsersityClassifications(FEDERAL_CLASSIFICAITONS);
        pmwVendor.setStateDivsersityClassifications(STATE_CLASSIFICAITONS);
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(3, actualDiversities.size());
    }
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithNulls() {
        pmwVendor.setFederalDivsersityClassifications(null);
        pmwVendor.setStateDivsersityClassifications(null);
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(0, actualDiversities.size());
    }
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithNoCommas() {
        pmwVendor.setFederalDivsersityClassifications("8(A) African American");
        pmwVendor.setStateDivsersityClassifications("WBE (Womens Business Enterprise)");
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(2, actualDiversities.size());
    }
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithInvalidOptions() {
        pmwVendor.setFederalDivsersityClassifications("foo");
        pmwVendor.setStateDivsersityClassifications("bar");
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(0, actualDiversities.size());
    }

}
