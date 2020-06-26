package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.businessobject.KfsToPMWSupplierDiversityDTO;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;

class PaymentWorksVendorSupplierDiversityServiceImplTest {
    private static final String DAV_DESCRIPTION = "Disabled Veteran Owned Business";
    private static final String DAV_NY_CODE = "NV";
    private static final String EIGHTA_CODE = "8(A)";
    private static final String EIGHTA_DESCRIPTION_AFRICAN_AMERICAN = "8(A) African American";
    private static final String EIGHTA_DESCRIPTION_HISPANIC_AMERICAN = "8(A) Hispanic American";
    private static final String MBE_CODE = "MBE";
    private static final String MBE_DESCRIPTION = "MBE (Minority Business Enterprise)";
    private static final String MBE_NY_CODE = "NM";
    private static final String SB_CODE = "SB";
    private static final String SDVOSB_DESCRIPTION = "SDVOSB (Service-Disabled Veteran-Owned Small Business)";
    private static final String WBE_DESCRIPTION = "WBE (Womens Business Enterprise)";
    private static final String WBE_NY_CODE = "NW";
    
    private static final String FEDERAL_CLASSIFICAITONS = "8(A) African American, 8(A) Hispanic American, 8(a) Native American (Includes Alaskan Natives, Native Hawaiians & Native Americans), 8(a) Asian Pacific American, 8(a) Subcontinent Asian Americans, Small Disadvantaged Business, LGBTBE (LGBT-Owned Businesses), HUB Zone (Historically Under-Utilized Small Busines, MBE (Minority Business Enterprise), SDB (Small Disadvantaged Business), SDVOSB (Service-Disabled Veteran-Owned Small Business), VOSB (Veteran-Owned Small Business           , WBE (Woman Business Enterprise), WOSB (Women-Owned Small Business), Ability One, Small Business";
    private static final String STATE_CLASSIFICAITONS  = "MBE (Minority Business Enterprise), WBE (Womens Business Enterprise), Disabled Veteran Owned Business";
    
    private PaymentWorksVendorSupplierDiversityServiceImpl supplierSerivce;
    private PaymentWorksVendor pmwVendor;

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
        List<KfsToPMWSupplierDiversityDTO> federalSuppliers = new ArrayList<KfsToPMWSupplierDiversityDTO>();
        addSUpplierDiversityToMap(federalSuppliers, EIGHTA_CODE, EIGHTA_DESCRIPTION_AFRICAN_AMERICAN);
        addSUpplierDiversityToMap(federalSuppliers, EIGHTA_CODE, EIGHTA_DESCRIPTION_HISPANIC_AMERICAN);
        addSUpplierDiversityToMap(federalSuppliers, MBE_CODE, MBE_DESCRIPTION);
        String DV_CODE = "DV";
        addSUpplierDiversityToMap(federalSuppliers, DV_CODE, SDVOSB_DESCRIPTION);
        addSUpplierDiversityToMap(federalSuppliers, SB_CODE, SDVOSB_DESCRIPTION);
        Mockito.when(dao.buildPmwToKfsFederalSupplierDiversityMapForForeignForm()).thenReturn(federalSuppliers);
        
        List<KfsToPMWSupplierDiversityDTO> newYorksuppliers = new ArrayList<KfsToPMWSupplierDiversityDTO>();
        addSUpplierDiversityToMap(newYorksuppliers, MBE_NY_CODE, MBE_DESCRIPTION);
        addSUpplierDiversityToMap(newYorksuppliers, WBE_NY_CODE, WBE_DESCRIPTION);
        addSUpplierDiversityToMap(newYorksuppliers, DAV_NY_CODE, DAV_DESCRIPTION);
        Mockito.when(dao.buildPmwToKfsNewYorkSupplierDiversityMapForForeignForm()).thenReturn(newYorksuppliers);
        return dao;
    }
    
    private void addSUpplierDiversityToMap(List<KfsToPMWSupplierDiversityDTO> suppliers, String code, String description) {
        KfsToPMWSupplierDiversityDTO dto = new KfsToPMWSupplierDiversityDTO(code, description, description);
        suppliers.add(dto);
    }

    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithRealExamples() {
        pmwVendor.setFederalDivsersityClassifications(FEDERAL_CLASSIFICAITONS);
        pmwVendor.setStateDivsersityClassifications(STATE_CLASSIFICAITONS);
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(7, actualDiversities.size());
    }
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithNulls() {
        pmwVendor.setFederalDivsersityClassifications(null);
        pmwVendor.setStateDivsersityClassifications(null);
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(0, actualDiversities.size());
    }
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithEmptyStrings() {
        pmwVendor.setFederalDivsersityClassifications(StringUtils.EMPTY);
        pmwVendor.setStateDivsersityClassifications(StringUtils.EMPTY);
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(0, actualDiversities.size());
    }
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithNoCommas() {
        pmwVendor.setFederalDivsersityClassifications(EIGHTA_DESCRIPTION_AFRICAN_AMERICAN);
        pmwVendor.setStateDivsersityClassifications(WBE_DESCRIPTION);
        
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
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithOneFederalClass() {
        pmwVendor.setFederalDivsersityClassifications(EIGHTA_DESCRIPTION_AFRICAN_AMERICAN);
        pmwVendor.setStateDivsersityClassifications(StringUtils.EMPTY);
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(1, actualDiversities.size());
        VendorSupplierDiversity diversity = actualDiversities.get(0);
        assertEquals(EIGHTA_CODE, diversity.getVendorSupplierDiversityCode());
        CuVendorSupplierDiversityExtension diversityExtension = (CuVendorSupplierDiversityExtension) diversity.getExtension();
        assertEquals(EIGHTA_CODE, diversityExtension.getVendorSupplierDiversityCode());
    }
    
    @Test
    public void testBuildSuppplierDivsersityListFromPaymentWorksVendorWithCaseDifference() {
        pmwVendor.setFederalDivsersityClassifications("8(A) AFRICAN AMERICAN");
        pmwVendor.setStateDivsersityClassifications(StringUtils.EMPTY);
        
        List<VendorSupplierDiversity> actualDiversities = supplierSerivce.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor);
        assertEquals(1, actualDiversities.size());
    }
    
    @Test
    public void testBuildDateOneYearFromToday() {
        Date actualDate = supplierSerivce.buildDateOneYearFromToday();
        
        Date expectedDate = new Date(Calendar.getInstance().getTimeInMillis());
        expectedDate.setYear(expectedDate.getYear() + 1);
        
        assertEquals(expectedDate, actualDate);
    }

}
