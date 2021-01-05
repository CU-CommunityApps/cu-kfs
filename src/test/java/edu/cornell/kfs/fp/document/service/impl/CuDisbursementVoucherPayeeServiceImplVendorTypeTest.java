package edu.cornell.kfs.fp.document.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;

public class CuDisbursementVoucherPayeeServiceImplVendorTypeTest {

    private CuDisbursementVoucherPayeeServiceImpl cuDisbursementVoucherPayeeService;

    @BeforeEach
    void setUp() throws Exception {
        cuDisbursementVoucherPayeeService = new CuDisbursementVoucherPayeeServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        cuDisbursementVoucherPayeeService = null;
    }

    @ParameterizedTest
    @MethodSource("vendorTypeAndExpectedPayeeType")
    void testConvertVendorTypeToPayeeType(String vendorType, String expectedPayeeType) throws Exception {
        String actualPayeeType = cuDisbursementVoucherPayeeService.getPayeeTypeCodeForVendorType(vendorType);
        assertEquals(expectedPayeeType, actualPayeeType, "Wrong mapping for vendor type '" + vendorType + "'");
    }

    static Stream<Arguments> vendorTypeAndExpectedPayeeType() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(KFSConstants.EMPTY_STRING, null),
                Arguments.of(KFSConstants.BLANK_SPACE, null),
                Arguments.of(VendorConstants.VendorTypes.DISBURSEMENT_VOUCHER, KFSConstants.PaymentPayeeTypes.VENDOR),
                Arguments.of(VendorConstants.VendorTypes.REFUND_PAYMENT, KFSConstants.PaymentPayeeTypes.REFUND_VENDOR),
                Arguments.of("ZZ", null)
                );
    }

}
