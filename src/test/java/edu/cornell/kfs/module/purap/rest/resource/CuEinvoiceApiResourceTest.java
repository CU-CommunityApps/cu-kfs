package edu.cornell.kfs.module.purap.rest.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CuEinvoiceApiResourceTest {
    
    CuEinvoiceApiResource eInvoiceResource;

    @BeforeEach
    void setUp() {
        eInvoiceResource = new CuEinvoiceApiResource();
    }

    @AfterEach
    void tearDown() {
        eInvoiceResource = null;
    }

    @Test
    void testBuildVendorNumberListMany() {
        List<String> vendorNumbers = Arrays.asList("a", "b", "c");
        String actualResults = eInvoiceResource.buildVendorNumberList(vendorNumbers);
        String expectedResults = "a,b,c";
        assertEquals(expectedResults, actualResults);
    }
    
    @Test
    void testBuildVendorNumberListSingle() {
        List<String> vendorNumbers = Arrays.asList("a");
        String actualResults = eInvoiceResource.buildVendorNumberList(vendorNumbers);
        String expectedResults = "a";
        assertEquals(expectedResults, actualResults);
    }
    
    @Test
    void testBuildVendorNumberListEmpty() {
        List<String> vendorNumbers = Arrays.asList(StringUtils.EMPTY);
        String actualResults = eInvoiceResource.buildVendorNumberList(vendorNumbers);
        assertEquals(StringUtils.EMPTY, actualResults);
    }
    
    @Test
    void testBuildVendorNumberListNull() {
        String actualResults = eInvoiceResource.buildVendorNumberList(null);
        assertEquals(StringUtils.EMPTY, actualResults);
    }

}
