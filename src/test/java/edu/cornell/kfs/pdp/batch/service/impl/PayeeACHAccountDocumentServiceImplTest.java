package edu.cornell.kfs.pdp.batch.service.impl;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.cornell.kfs.pdp.batch.fixture.StandardEntryClassConversionFixture;
import edu.cornell.kfs.pdp.batch.fixture.StandardEntryClassConversionFixture.ExpectFailureData;
import edu.cornell.kfs.pdp.batch.fixture.StandardEntryClassConversionFixture.ExpectPDPSuccessData;

public class PayeeACHAccountDocumentServiceImplTest {
    
    private PayeeACHAccountDocumentServiceImpl payeeACHAccountDocumentService;
    
    @BeforeEach
    public void setUp() throws Exception {
        payeeACHAccountDocumentService = new PayeeACHAccountDocumentServiceImpl();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        payeeACHAccountDocumentService = null;
    }
    
    static Stream<Arguments> goodACHAccountTypesForValidation() {
        return Stream.of(
                StandardEntryClassConversionFixture.ExpectPDPSuccessData.WORKDAY_PERSONAL_CHECKING_TEST,
                StandardEntryClassConversionFixture.ExpectPDPSuccessData.WORKDAY_PERSONAL_SAVINGS_TEST)
                .map(Arguments::of);
    }
    
    @ParameterizedTest
    @MethodSource("goodACHAccountTypesForValidation")
    void testGoodAccountTypeToStandardEntryClassConversion(ExpectPDPSuccessData testFixture) {
        assertEquals(payeeACHAccountDocumentService.determineStandardEntryClass(testFixture.achBankAccountTypeBeingTested),
                testFixture.standardEntryClassExpected,
                "Workday provided ACH bank account type was not expected to map to this Standard Entry Class.");
    }
    
    static Stream<Arguments> badACHAccountTypesForValidation() {
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
    @MethodSource("badACHAccountTypesForValidation")
    void testBadAccountTypeToStandardEntryClassConversion(ExpectFailureData testFixture) {
        assertThrows(IllegalArgumentException.class,
                () -> payeeACHAccountDocumentService.determineStandardEntryClass(testFixture.achBankAccountTypeBeingTested));
    }
    
}
