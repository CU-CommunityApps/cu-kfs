package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksFieldMapping;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCustomFieldDTO;

class PaymentWorksDtoToPaymentWorksVendorConversionServiceImplTest {
    
    private PaymentWorksDtoToPaymentWorksVendorConversionServiceImpl paymentWorksDtoToPaymentWorksVendorConversionServiceImpl;
    
    private static final String FILE_VALUE = "https://sandbox.paymentworks.com/api/files/jaydhulslander-1/private/custom-fields/53cee13c-8c6b-4410-8640-bb3b8a0c8624/yankees-jack_yQVOSVw.jpg";
    private static final String FIELD_VALUE_VALUE = "testing field value";
    
    private PaymentWorksCustomFieldDTO paymentWorksCustomFieldDTO;

    @BeforeEach
    public void setUp() throws Exception {
        paymentWorksDtoToPaymentWorksVendorConversionServiceImpl = new PaymentWorksDtoToPaymentWorksVendorConversionServiceImpl();
        paymentWorksCustomFieldDTO = new PaymentWorksCustomFieldDTO();
        paymentWorksCustomFieldDTO.setField_value(FIELD_VALUE_VALUE);
        paymentWorksCustomFieldDTO.setFile(FILE_VALUE);
    }

    @AfterEach
    public void tearDown() throws Exception {
        paymentWorksDtoToPaymentWorksVendorConversionServiceImpl = null;
        paymentWorksCustomFieldDTO = null;
    }

    @Test
    public void testGetValueOutOfPaymentWorksCustomFieldDTO_File() {
        PaymentWorksFieldMapping mapping = new PaymentWorksFieldMapping();
        mapping.setCustomAttributeValueToUse(PaymentWorksConstants.CustomAttributeValueToUse.FILE);
        String actualResults = paymentWorksDtoToPaymentWorksVendorConversionServiceImpl.getValueOutOfPaymentWorksCustomFieldDTO(paymentWorksCustomFieldDTO, mapping);
        assertEquals(FILE_VALUE, actualResults);
    }
    
    @Test
    public void testGetValueOutOfPaymentWorksCustomFieldDTO_FieldValue() {
        PaymentWorksFieldMapping mapping = new PaymentWorksFieldMapping();
        mapping.setCustomAttributeValueToUse(PaymentWorksConstants.CustomAttributeValueToUse.FIELD_VALUE);
        String actualResults = paymentWorksDtoToPaymentWorksVendorConversionServiceImpl.getValueOutOfPaymentWorksCustomFieldDTO(paymentWorksCustomFieldDTO, mapping);
        assertEquals(FIELD_VALUE_VALUE, actualResults);
    }
    
    @Test
    public void testGetValueOutOfPaymentWorksCustomFieldDTO_BadMap() {
        PaymentWorksFieldMapping mapping = new PaymentWorksFieldMapping();
        mapping.setCustomAttributeValueToUse("foo");
        try { 
            String actualResults = paymentWorksDtoToPaymentWorksVendorConversionServiceImpl.getValueOutOfPaymentWorksCustomFieldDTO(paymentWorksCustomFieldDTO, mapping);
        } catch (IllegalArgumentException iae) {
            assertTrue("Excptect this to have an illegal argument exception", true);
            return;
        }
        assertTrue("We should have caught an illegal arguement exception, something went wrong", false);
    }

}
