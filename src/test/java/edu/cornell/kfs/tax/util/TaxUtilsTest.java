package edu.cornell.kfs.tax.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;

public class TaxUtilsTest {

    @Test
    void testBuildMappingKeys() throws Exception {
        assertMappingKeyIsBuiltCorrectly("MISC(3)", CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "3");
        assertMappingKeyIsBuiltCorrectly("NEC(1)", CUTaxConstants.TAX_1099_NEC_FORM_TYPE, "1");
        assertMappingKeyIsBuiltCorrectly("MISC(15B)", CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "15B");
        assertMappingKeyIsBuiltCorrectly("MISC(???)", 
                CUTaxConstants.TAX_1099_MISC_FORM_TYPE, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
        assertMappingKeyIsBuiltCorrectly("????(12)",
                CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE, "12");
        assertMappingKeyIsBuiltCorrectly("????(???)",
                CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
    }

    @Test
    void testBuildMappingKeysWithBlankInputs() throws Exception {
        assertMappingKeyIsBuiltCorrectly("????(???)", null, null);
        assertMappingKeyIsBuiltCorrectly("????(???)", KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING);
        assertMappingKeyIsBuiltCorrectly("????(???)", KFSConstants.BLANK_SPACE, KFSConstants.BLANK_SPACE);
        assertMappingKeyIsBuiltCorrectly("MISC(???)", CUTaxConstants.TAX_1099_MISC_FORM_TYPE, null);
        assertMappingKeyIsBuiltCorrectly("????(4)", null, "4");
        assertMappingKeyIsBuiltCorrectly("MISC(???)", CUTaxConstants.TAX_1099_MISC_FORM_TYPE, KFSConstants.EMPTY_STRING);
        assertMappingKeyIsBuiltCorrectly("????(4)", KFSConstants.EMPTY_STRING, "4");
        assertMappingKeyIsBuiltCorrectly("MISC(???)", CUTaxConstants.TAX_1099_MISC_FORM_TYPE, KFSConstants.BLANK_SPACE);
        assertMappingKeyIsBuiltCorrectly("????(4)", KFSConstants.BLANK_SPACE, "4");
    }

    @Test
    void testBuildMappingKeysWithCaseInsensitiveInputs() throws Exception {
        assertMappingKeyIsBuiltCorrectly("MISC(12A)", CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "12a");
        assertMappingKeyIsBuiltCorrectly("NEC(4)", "nec", "4");
        assertMappingKeyIsBuiltCorrectly("MISC(1BB)", "mIsC", "1Bb");
    }

    @Test
    void testVerificationOfCorrectlyFormattedMappingKeys() throws Exception {
        assertMappingKeyIsFormattedCorrectly("MISC(1)");
        assertMappingKeyIsFormattedCorrectly("NEC(5)");
        assertMappingKeyIsFormattedCorrectly("MISC(15C)");
        assertMappingKeyIsFormattedCorrectly("????(???)");
    }

    @Test
    void testDetectionOfBadlyFormattedMappingKeys() throws Exception {
        assertMappingKeyIsBadlyFormatted(null);
        assertMappingKeyIsBadlyFormatted(KFSConstants.EMPTY_STRING);
        assertMappingKeyIsBadlyFormatted(KFSConstants.BLANK_SPACE);
        assertMappingKeyIsBadlyFormatted("MISC");
        assertMappingKeyIsBadlyFormatted("7");
        assertMappingKeyIsBadlyFormatted("?");
        assertMappingKeyIsBadlyFormatted("NEC()");
        assertMappingKeyIsBadlyFormatted("(8)");
        assertMappingKeyIsBadlyFormatted("MISCCCCCC(1)");
        assertMappingKeyIsBadlyFormatted("NEC(11111111)");
        assertMappingKeyIsBadlyFormatted("MISC[1]");
        assertMappingKeyIsBadlyFormatted("NEC{1}");
        assertMappingKeyIsBadlyFormatted("###(4)");
        assertMappingKeyIsBadlyFormatted("MISC(###)");
        assertMappingKeyIsBadlyFormatted("NEC?(6)");
        assertMappingKeyIsBadlyFormatted("NEC(2?)");
    }

    @Test
    void testBuildFormAndBoxPairs() throws Exception {
        assertFormAndBoxPairIsBuiltCorrectly(Pair.of(CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "3"), "MISC(3)");
        assertFormAndBoxPairIsBuiltCorrectly(Pair.of(CUTaxConstants.TAX_1099_NEC_FORM_TYPE, "1"), "NEC(1)");
        assertFormAndBoxPairIsBuiltCorrectly(Pair.of(CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "15B"), "MISC(15B)");
        assertFormAndBoxPairIsBuiltCorrectly(
                Pair.of(CUTaxConstants.TAX_1099_MISC_FORM_TYPE, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY), "MISC(???)");
        assertFormAndBoxPairIsBuiltCorrectly(Pair.of(CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE, "12"), "????(12)");
        assertFormAndBoxPairIsBuiltCorrectly(CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY, "????(???)");
    }

    @Test
    void testBuildFormAndBoxPairsWithCaseInsensitiveInputs() throws Exception {
        assertFormAndBoxPairIsBuiltCorrectly(Pair.of(CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "12A"), "MISC(12a)");
        assertFormAndBoxPairIsBuiltCorrectly(Pair.of(CUTaxConstants.TAX_1099_NEC_FORM_TYPE, "4"), "nec(4)");
        assertFormAndBoxPairIsBuiltCorrectly(Pair.of(CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "1BB"), "mIsC(1Bb)");
    }

    @Test
    void testBuildDefaultFormAndBoxPairsForBlankInputs() throws Exception {
        assertFormAndBoxPairIsBuiltCorrectly(CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY, null);
        assertFormAndBoxPairIsBuiltCorrectly(
                CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY, KFSConstants.EMPTY_STRING);
        assertFormAndBoxPairIsBuiltCorrectly(
                CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY, KFSConstants.BLANK_SPACE);
    }

    @Test
    void testCannotBuildFormAndBoxPairsWithInvalidMappingKeys() throws Exception {
        assertCannotBuildPairForInvalidMappingKey("MISC");
        assertCannotBuildPairForInvalidMappingKey("7");
        assertCannotBuildPairForInvalidMappingKey("?");
        assertCannotBuildPairForInvalidMappingKey("NEC()");
        assertCannotBuildPairForInvalidMappingKey("(8)");
        assertCannotBuildPairForInvalidMappingKey("MISCCCCCC(1)");
        assertCannotBuildPairForInvalidMappingKey("NEC(11111111)");
        assertCannotBuildPairForInvalidMappingKey("MISC[1]");
        assertCannotBuildPairForInvalidMappingKey("NEC{1}");
        assertCannotBuildPairForInvalidMappingKey("###(4)");
        assertCannotBuildPairForInvalidMappingKey("MISC(###)");
        assertCannotBuildPairForInvalidMappingKey("NEC?(6)");
        assertCannotBuildPairForInvalidMappingKey("NEC(2?)");
    }

    private void assertMappingKeyIsBuiltCorrectly(String expectedKey, String formType, String boxNumber) {
        String actualKey = TaxUtils.build1099BoxNumberMappingKey(formType, boxNumber);
        assertEquals(expectedKey, actualKey, "The 1099 box mapping key was not built correctly");
    }

    private void assertMappingKeyIsFormattedCorrectly(String mappingKey) {
        assertTrue(TaxUtils.is1099BoxNumberMappingKeyFormattedProperly(mappingKey),
                "The 1099 box mapping key should have been in the expected format");
    }

    private void assertMappingKeyIsBadlyFormatted(String mappingKey) {
        assertFalse(TaxUtils.is1099BoxNumberMappingKeyFormattedProperly(mappingKey),
                "The 1099 box mapping key should have had a bad format");
    }

    private void assertFormAndBoxPairIsBuiltCorrectly(
            Pair<String, String> expectedPair, String boxNumberMappingKey) {
        Pair<String, String> actualPair = TaxUtils.build1099FormTypeAndBoxNumberPair(boxNumberMappingKey);
        assertEquals(expectedPair, actualPair, "The 1099 form-and-box pair was not built correctly");
    }

    private void assertCannotBuildPairForInvalidMappingKey(String boxNumberMappingKey) {
        try {
            TaxUtils.build1099FormTypeAndBoxNumberPair(boxNumberMappingKey);
            fail("An exception should have been thrown when building a form-and-box pair for invalid key: "
                    + boxNumberMappingKey);
        } catch (IllegalArgumentException e) {
        }
    }

}
