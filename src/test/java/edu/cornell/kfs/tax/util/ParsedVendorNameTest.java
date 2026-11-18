package edu.cornell.kfs.tax.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.fixture.ParsedVendorNameFixture;

@Execution(ExecutionMode.SAME_THREAD)
public class ParsedVendorNameTest {

    enum LocalTestCase {
        @ParsedVendorNameFixture(
                fullName = "", expectedSuffix = "",
                expectedFirstName = "", expectedMiddleName = "", expectedLastName = ""
        )
        EMPTY_NAME,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_ONLY,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_ONLY,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q.", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q.", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_PERIOD_LAST_NAME_ONLY,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_ONLY,

        @ParsedVendorNameFixture(
                fullName = "Smith Jones, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith Jones"
        )
        FIRST_MIDDLE_MULTI_LAST_NAME_ONLY,

        @ParsedVendorNameFixture(
                fullName = "Smith Jr, Jack", expectedSuffix = "Jr",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_JR_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Jr, Jack Q", expectedSuffix = "Jr",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_JR_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Jr, Jack Quincy", expectedSuffix = "Jr",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_JR_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Jr., Jack Quincy", expectedSuffix = "Jr.",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_JR_SUFFIX_AND_PERIOD,

        @ParsedVendorNameFixture(
                fullName = "Smith Jones Jr, Jack Quincy", expectedSuffix = "Jr",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith Jones"
        )
        FIRST_MIDDLE_MULTI_LAST_NAME_WITH_JR_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Sr, Jack", expectedSuffix = "Sr",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_SR_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Sr, Jack Q", expectedSuffix = "Sr",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_SR_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Sr, Jack Quincy", expectedSuffix = "Sr",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_SR_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Sr., Jack Quincy", expectedSuffix = "Sr.",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_SR_SUFFIX_AND_PERIOD,

        @ParsedVendorNameFixture(
                fullName = "Smith II, Jack", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_II_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith II, Jack Q", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_II_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith II, Jack Quincy", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_II_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, II Jack Quincy", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_II_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith II, Jack", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_II_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Quincy II", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_II_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack II", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_II_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith III, Jack Quincy", expectedSuffix = "III",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_III_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith IV, Jack Quincy", expectedSuffix = "IV",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_IV_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith V, Jack Quincy", expectedSuffix = "V",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_V_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack V", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "V", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_V_LAST_NAME_NO_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith VI, Jack Quincy", expectedSuffix = "VI",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_VI_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, Vi Quincy", expectedSuffix = "",
                expectedFirstName = "Vi", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_NO_SUFFIX_WITH_VI_AS_FIRST_NAME,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Vi", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Vi", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_NO_SUFFIX_WITH_VI_AS_MIDDLE_NAME,

        @ParsedVendorNameFixture(
                fullName = "Vi, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Vi"
        )
        FIRST_MIDDLE_LAST_NAME_NO_SUFFIX_WITH_VI_AS_LAST_NAME,

        @ParsedVendorNameFixture(
                fullName = "Smith VII, Jack Quincy", expectedSuffix = "VII",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_VII_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith VIII, Jack Quincy", expectedSuffix = "VIII",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_VIII_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith IX, Jack Quincy", expectedSuffix = "IX",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_IX_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith X, Jack Quincy", expectedSuffix = "X",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_X_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack X", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "X", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_X_LAST_NAME_NO_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith XI, Jack Quincy", expectedSuffix = "XI",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_XI_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, XI Jack Quincy", expectedSuffix = "XI",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_XI_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Quincy XI", expectedSuffix = "XI",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_XI_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Hong, Xi Chang", expectedSuffix = "",
                expectedFirstName = "Xi", expectedMiddleName = "Chang", expectedLastName = "Hong"
        )
        FIRST_MIDDLE_LAST_NAME_NO_SUFFIX_WITH_XI_AS_FIRST_NAME,

        @ParsedVendorNameFixture(
                fullName = "Hong, Chang Xi", expectedSuffix = "",
                expectedFirstName = "Chang", expectedMiddleName = "Xi", expectedLastName = "Hong"
        )
        FIRST_MIDDLE_LAST_NAME_NO_SUFFIX_WITH_XI_AS_MIDDLE_NAME,

        @ParsedVendorNameFixture(
                fullName = "Xi, Chang Hong", expectedSuffix = "",
                expectedFirstName = "Chang", expectedMiddleName = "Hong", expectedLastName = "Xi"
        )
        FIRST_MIDDLE_LAST_NAME_NO_SUFFIX_WITH_XI_AS_LAST_NAME,

        @ParsedVendorNameFixture(
                fullName = "Smith PhD, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_PHD_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith PhD, Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PHD_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith Ph.D., Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_PHD_PERIODS_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, PhD Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PHD_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, PhD Jack Q", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_PHD_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack PhD", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PHD_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q PhD", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_PHD_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith MD, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_MD_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith MD, Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_MD_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith M.D., Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_MD_PERIODS_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, MD Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_MD_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, MD Jack Q", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_MD_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack MD", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_MD_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q MD", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_MD_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith PC, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_PC_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith PC, Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PC_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith P.C., Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_PC_PERIODS_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, PC Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PC_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, PC Jack Q", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_PC_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack PC", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PC_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q PC", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_PC_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith PLLC, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_PLLC_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith PLLC, Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PLLC_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith P.L.L.C., Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_EXCLUDED_PLLC_PERIODS_SUFFIX,

        @ParsedVendorNameFixture(
                fullName = "Smith, PLLC Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PLLC_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, PLLC Jack Q", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_PLLC_SUFFIX_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack PLLC", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_EXCLUDED_PLLC_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q PLLC", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_EXCLUDED_PLLC_SUFFIX_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith II PhD, Jack Quincy", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_MULTIPLE_SUFFIXES,

        @ParsedVendorNameFixture(
                fullName = "Smith II PhD, Jack", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_SUFFIXES,

        @ParsedVendorNameFixture(
                fullName = "Smith, II PhD Jack", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_SUFFIXES_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, II PhD Jack Q", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_MULTIPLE_SUFFIXES_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith II, PhD Jack", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_SUFFIXES_AROUND_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith II, PhD Jack Q", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_MULTIPLE_SUFFIXES_AROUND_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack II PhD", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_SUFFIXES_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q II PhD", expectedSuffix = "II",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_MULTIPLE_SUFFIXES_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith PhD MD, Jack Quincy", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Quincy", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES,

        @ParsedVendorNameFixture(
                fullName = "Smith PhD MD, Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES,

        @ParsedVendorNameFixture(
                fullName = "Smith, PhD MD Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, PhD MD Jack Q", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES_AFTER_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith PhD, MD Jack", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES_AROUND_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith PhD, MD Jack Q", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES_AROUND_COMMA,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack PhD MD", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "", expectedLastName = "Smith"
        )
        FIRST_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES_AT_END,

        @ParsedVendorNameFixture(
                fullName = "Smith, Jack Q PhD MD", expectedSuffix = "",
                expectedFirstName = "Jack", expectedMiddleName = "Q", expectedLastName = "Smith"
        )
        FIRST_MIDDLE_INITIAL_LAST_NAME_WITH_MULTIPLE_EXCLUDED_SUFFIXES_AT_END,
        ;

        public Arguments toNamedAnnotationFixtureArgument() {
            return FixtureUtils.createNamedAnnotationFixtureArgument(this, ParsedVendorNameFixture.class);
        }
    }

    static Stream<Arguments> nameTestCases() {
        return Arrays.stream(LocalTestCase.values())
                .map(LocalTestCase::toNamedAnnotationFixtureArgument);
    }

    @Test
    void testEmptyNameInstanceHasEmptyDataFields() throws Exception {
        final ParsedVendorNameFixture emptyNameFixture = FixtureUtils.getAnnotationBasedFixture(
                LocalTestCase.EMPTY_NAME, ParsedVendorNameFixture.class);
        assertNameWasParsedCorrectly(emptyNameFixture, ParsedVendorName.EMPTY);
    }

    @ParameterizedTest
    @MethodSource("nameTestCases")
    void testTax1099NameParsing(final ParsedVendorNameFixture fixture) throws Exception {
        final String lastNameSection = StringUtils.substringBefore(fixture.fullName(), CUKFSConstants.COMMA_AND_SPACE);
        final String firstNameSection = StringUtils.substringAfter(fixture.fullName(), CUKFSConstants.COMMA_AND_SPACE);
        final ParsedVendorName parsedName = ParsedVendorName.of(firstNameSection, lastNameSection);
        assertNameWasParsedCorrectly(fixture, parsedName);
    }

    private void assertNameWasParsedCorrectly(final ParsedVendorNameFixture fixture,
            final ParsedVendorName parsedName) throws Exception {
        assertNameFieldIsCorrect("firstName", fixture.expectedFirstName(), parsedName.getFirstName());
        assertNameFieldIsCorrect("middleName", fixture.expectedMiddleName(), parsedName.getMiddleName());
        assertNameFieldIsCorrect("lastName", fixture.expectedLastName(), parsedName.getLastName());
        assertNameFieldIsCorrect("suffix", fixture.expectedSuffix(), parsedName.getSuffix());
    }

    private void assertNameFieldIsCorrect(final String fieldName, final String expectedValue,
            final String actualValue) throws Exception {
        if (StringUtils.isNotBlank(expectedValue)) {
            assertEquals(expectedValue, actualValue, fieldName + " field has the wrong value");
        } else {
            assertTrue(StringUtils.isEmpty(actualValue), fieldName + " field should have been null or empty");
        }
    }

}
