package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.cornell.kfs.pdp.batch.fixture.StandardEntryClassConversionFixture;
import edu.cornell.kfs.pdp.batch.fixture.StandardEntryClassConversionFixture.ExpectFailureData;
import edu.cornell.kfs.pdp.batch.fixture.StandardEntryClassConversionFixture.ExpectPaymentWorksSuccessData;


public class PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImplTest {
    
    private PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImpl paymentWorksVendorToKfsPayeeAchAccountConversionService;
        
    @BeforeEach
    public void setUp() throws Exception {
        paymentWorksVendorToKfsPayeeAchAccountConversionService = new PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImpl();
    }
        
    @AfterEach
    public void tearDown() throws Exception {
        paymentWorksVendorToKfsPayeeAchAccountConversionService = null;
    }
    
    static Stream<Arguments> goodPaymentWorksACHAccountTypesForValidation() {
        return Stream.of(
                StandardEntryClassConversionFixture.ExpectPaymentWorksSuccessData.CORPORATE_CHECKING_TEST,
                StandardEntryClassConversionFixture.ExpectPaymentWorksSuccessData.CORPORATE_SAVINGS_TEST,
                StandardEntryClassConversionFixture.ExpectPaymentWorksSuccessData.PERSONAL_CHECKING_TEST,
                StandardEntryClassConversionFixture.ExpectPaymentWorksSuccessData.PERSONAL_SAVINGS_TEST)
                .map(Arguments::of);
    }
    
    @ParameterizedTest
    @MethodSource("goodPaymentWorksACHAccountTypesForValidation")
    void testGoodPMWAccountTypeToStandardEntryClassConversion(ExpectPaymentWorksSuccessData testFixture) {
        assertEquals(paymentWorksVendorToKfsPayeeAchAccountConversionService.determineStandardEntryClass(testFixture.achBankAccountTypeBeingTested),
                testFixture.standardEntryClassExpected,
                "PaymnetWorks provided ACH bank account type was not expected to map to this Standard Entry Class.");
    }
    
    static Stream<Arguments> badPaymentWorksACHAccountTypesForValidation() {
        return Stream.of(
                StandardEntryClassConversionFixture.ExpectFailureData.NULL_TEST,
                StandardEntryClassConversionFixture.ExpectFailureData.BLANK_TEST,
                StandardEntryClassConversionFixture.ExpectFailureData.WHITESPACE_TEST,
                StandardEntryClassConversionFixture.ExpectFailureData.SINGLE_CHAR_TEST,
                StandardEntryClassConversionFixture.ExpectFailureData.TWO_CHAR_TEST,
                StandardEntryClassConversionFixture.ExpectFailureData.INVALID_THREE_CHAR_TEST,
                StandardEntryClassConversionFixture.ExpectFailureData.INVALID_FOUR_CHAR_TEST)
                .map(Arguments::of);
    }
    
    @ParameterizedTest
    @MethodSource("badPaymentWorksACHAccountTypesForValidation")
    void testBadAccountTypeToStandardEntryClassConversion(ExpectFailureData testFixture) {
        assertThrows(IllegalArgumentException.class,
                () -> paymentWorksVendorToKfsPayeeAchAccountConversionService.determineStandardEntryClass(testFixture.achBankAccountTypeBeingTested));
    }
    

}
