package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.fixture.ConcurSAEFileFixture;
import edu.cornell.kfs.sys.batch.CuDelimitedFlatFileParser;
import edu.cornell.kfs.sys.batch.CuDelimitedFlatFileParserTestBase;
import edu.cornell.kfs.sys.util.LoadSpringFile;

@LoadSpringFile("edu/cornell/kfs/concur/batch/service/impl/cu-spring-concur-sae-file-test.xml")
public class ConcurStandardAccountingExtractFileParserTest extends CuDelimitedFlatFileParserTestBase {

    private static final String CONCUR_SAE_FILE_PARSER_BEAN = "standardAccountExtractFileType";

    @Test
    public void testLoadGoodSAEFile() throws Exception {
        assertConcurSAEFileParsesCorrectly(
                ConcurSAEFileFixture.PARSE_FLAT_FILE_NO_QUOTES_TEST, "extract_CES_SAE_v3_testGoodFile.txt");
    }

    @Test
    public void testLoadGoodSAEFileWithQuotedDelimiters() throws Exception {
        assertConcurSAEFileParsesCorrectly(
                ConcurSAEFileFixture.PARSE_FLAT_FILE_WITH_QUOTES_TEST, "extract_CES_SAE_v3_testGoodQuoteFile.txt");
    }

    @Test
    public void testLoadGoodSAEFileWithQuotedDelimitersAndFilteringOfInCellQuotes() throws Exception {
        final Map<Integer, List<Integer>> expectedFilteredCells = Map.ofEntries(
                Map.entry(3, columnNumbers(163)),
                Map.entry(4, columnNumbers(19, 71))
        );
        assertConcurSAEFileParsesCorrectly(
                ConcurSAEFileFixture.PARSE_FLAT_FILE_WITH_QUOTES_TEST,
                "extract_CES_SAE_v3_testGoodInCellQuoteFile.txt",
                expectedFilteredCells);
    }

    @Test
    public void testLoadGoodSAEFileWithFilteringOfSpecialCharacters() throws Exception {
        final Map<Integer, List<Integer>> expectedFilteredCells = Map.ofEntries(
                Map.entry(1, columnNumbers(4)),
                Map.entry(2, columnNumbers(9, 19, 23, 63))
        );
        assertConcurSAEFileParsesCorrectly(
                ConcurSAEFileFixture.PARSE_FLAT_FILE_NO_QUOTES_TEST,
                "extract_CES_SAE_v3_testGoodFileWithSpecialChars.txt",
                expectedFilteredCells);
    }

    @Test
    public void testLoadGoodSAEFileWithCorrectableInternalQuotes() throws Exception {
        final Map<Integer, List<Integer>> expectedFilteredCells = Map.ofEntries(
                Map.entry(2, columnNumbers(42)),
                Map.entry(4, columnNumbers(23))
        );
        /*
         * The "...NO_QUOTES_TEST" reference is intentional here because the parsing will ultimately remove
         * the corrected quotes.
         */
        assertConcurSAEFileParsesCorrectly(
                ConcurSAEFileFixture.PARSE_FLAT_FILE_NO_QUOTES_TEST,
                "extract_CES_SAE_v3_testGoodFileWithCorrectableQuotes.txt",
                expectedFilteredCells);
    }

    @Test
    public void testLoadBadSAEFileWithImproperlyQuotedDelimiters() throws Exception {
        assertConcurSAEFileFailsParsingDueToMisusedQuotes("extract_CES_SAE_v3_testBadQuoteFile.txt");
    }

    @Test
    public void testLoadAnotherBadSAEFileWithImproperQuoting() throws Exception {
        assertConcurSAEFileFailsParsingDueToMisusedQuotes("extract_CES_SAE_v3_testBadQuoteFile2.txt");
    }

    protected void assertConcurSAEFileParsesCorrectly(ConcurSAEFileFixture expectedFixture, String fileName) throws Exception {
        assertConcurSAEFileParsesCorrectly(expectedFixture, fileName, Map.of());
    }

    protected void assertConcurSAEFileParsesCorrectly(ConcurSAEFileFixture expectedFixture, String fileName,
            Map<Integer, List<Integer>> expectedFilteredCells) throws Exception {
        Pair<ConcurSAEFileFixture, Map<Integer, List<Integer>>> fixtureAndFilteredCells = Pair.of(
                expectedFixture, expectedFilteredCells);
        CuDelimitedFlatFileParser saeParser = getFlatFileParser(CONCUR_SAE_FILE_PARSER_BEAN);
        assertDelimitedFileDataParsesToSingleTopLevelDTOCorrectly(saeParser, fixtureAndFilteredCells, fileName);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void assertFileDataWasParsedCorrectly(Object expectedResult, Object actualResult) throws Exception {
        Pair<?, ?> fixtureAndFilteredCells = (Pair<?, ?>) expectedResult;
        ConcurSAEFileFixture expectedFixture = (ConcurSAEFileFixture) fixtureAndFilteredCells.getLeft();
        Map<Integer, List<Integer>> expectedFilteredCells =
                (Map<Integer, List<Integer>>) fixtureAndFilteredCells.getRight();
        ConcurStandardAccountingExtractFile expectedDTO = expectedFixture.toExtractFile();
        ConcurStandardAccountingExtractFile actualDTO = (ConcurStandardAccountingExtractFile) actualResult;
        
        assertEquals("Wrong line number", Integer.valueOf(1), actualDTO.getLineNumber());
        assertEquals("Wrong batch date", expectedDTO.getBatchDate(), actualDTO.getBatchDate());
        assertEquals("Wrong record count", expectedDTO.getRecordCount(), actualDTO.getRecordCount());
        assertEquals("Wrong journal amount total", expectedDTO.getJournalAmountTotal(), actualDTO.getJournalAmountTotal());
        assertEquals("Wrong batch ID", expectedDTO.getBatchId(), actualDTO.getBatchId());
        assertDetailLinesWereParsedCorrectly(
                expectedDTO.getConcurStandardAccountingExtractDetailLines(), actualDTO.getConcurStandardAccountingExtractDetailLines());
        assertSpecialCharacterRemovalsWereRecordedProperly(expectedFilteredCells, actualDTO);
    }

    protected void assertDetailLinesWereParsedCorrectly(
            List<ConcurStandardAccountingExtractDetailLine> expectedLines, List<ConcurStandardAccountingExtractDetailLine> actualLines)
            throws Exception {
        assertEquals("Wrong number of detail lines", expectedLines.size(), actualLines.size());
        
        for (int i = 0; i < expectedLines.size(); i++) {
            ConcurStandardAccountingExtractDetailLine expectedLine = expectedLines.get(i);
            ConcurStandardAccountingExtractDetailLine actualLine = actualLines.get(i);
            assertEquals("Wrong line number", Integer.valueOf(i + 2), actualLine.getLineNumber());
            assertEquals("Wrong batch ID", expectedLine.getBatchID(), actualLine.getBatchID());
            assertEquals("Wrong batch date", expectedLine.getBatchDate(), actualLine.getBatchDate());
            assertEquals("Wrong sequence number", expectedLine.getSequenceNumber(), actualLine.getSequenceNumber());
            assertEquals("Wrong employee ID", expectedLine.getEmployeeId(), actualLine.getEmployeeId());
            assertEquals("Wrong employee last name", expectedLine.getEmployeeLastName(), actualLine.getEmployeeLastName());
            assertEquals("Wrong employee first name", expectedLine.getEmployeeFirstName(), actualLine.getEmployeeFirstName());
            assertEquals("Wrong employee middle initial", expectedLine.getEmployeeMiddleInitial(), actualLine.getEmployeeMiddleInitial());
            assertEqualsIgnoreCase("Wrong employee group ID", expectedLine.getEmployeeGroupId(), actualLine.getEmployeeGroupId());
            assertEquals("Wrong report ID", expectedLine.getReportId(), actualLine.getReportId());
            assertEquals("Wrong policy", expectedLine.getPolicy(), actualLine.getPolicy());
            assertEquals("Wrong report chart", expectedLine.getReportChartOfAccountsCode(), actualLine.getReportChartOfAccountsCode());
            assertEquals("Wrong report account", expectedLine.getReportAccountNumber(), actualLine.getReportAccountNumber());
            assertEquals("Wrong report sub-account", expectedLine.getReportSubAccountNumber(), actualLine.getReportSubAccountNumber());
            assertEquals("Wrong report sub-object", expectedLine.getReportSubObjectCode(), actualLine.getReportSubObjectCode());
            assertEquals("Wrong report project code", expectedLine.getReportProjectCode(), actualLine.getReportProjectCode());
            assertEquals("Wrong report org ref ID", expectedLine.getReportOrgRefId(), actualLine.getReportOrgRefId());
            assertEqualsIgnoreCase("Wrong employee status", expectedLine.getEmployeeStatus(), actualLine.getEmployeeStatus());
            assertEquals("Wrong report entry ID", expectedLine.getReportEntryId(), actualLine.getReportEntryId());
            assertEquals("Wrong expense type", expectedLine.getExpenseType(), actualLine.getExpenseType());
            assertEquals("Wrong report-entry-personal flag", expectedLine.getReportEntryIsPersonalFlag(), actualLine.getReportEntryIsPersonalFlag());
            assertEquals("Wrong payment code", expectedLine.getPaymentCode(), actualLine.getPaymentCode());
            assertEquals("Wrong journal payer payment type", expectedLine.getJournalPayerPaymentTypeName(), actualLine.getJournalPayerPaymentTypeName());
            assertEquals("Wrong journal payee payment type", expectedLine.getJournalPayeePaymentTypeName(), actualLine.getJournalPayeePaymentTypeName());
            assertEquals("Wrong journal account code", expectedLine.getJournalAccountCode(), actualLine.getJournalAccountCode());
            assertEquals("Wrong journal account code override", expectedLine.getJournalAccountCodeOverridden(), actualLine.getJournalAccountCodeOverridden());
            assertEquals("Wrong journal debit/credit", expectedLine.getJournalDebitCredit(), actualLine.getJournalDebitCredit());
            assertEquals("Wrong journal amount", expectedLine.getJournalAmount(), actualLine.getJournalAmount());
            assertEquals("Wrong journal amount string", expectedLine.getJournalAmountString(), actualLine.getJournalAmountString());
            assertEquals("Wrong cash advance payment code name", expectedLine.getCashAdvancePaymentCodeName(), actualLine.getCashAdvancePaymentCodeName());
            assertEquals("Wrong cash advance key", expectedLine.getCashAdvanceKey(), actualLine.getCashAdvanceKey());
            assertEquals("Wrong chart", expectedLine.getChartOfAccountsCode(), actualLine.getChartOfAccountsCode());
            assertEquals("Wrong account", expectedLine.getAccountNumber(), actualLine.getAccountNumber());
            assertEquals("Wrong sub-account", expectedLine.getSubAccountNumber(), actualLine.getSubAccountNumber());
            assertEquals("Wrong sub-object", expectedLine.getSubObjectCode(), actualLine.getSubObjectCode());
            assertEquals("Wrong project code", expectedLine.getProjectCode(), actualLine.getProjectCode());
            assertEquals("Wrong org ref ID", expectedLine.getOrgRefId(), actualLine.getOrgRefId());
            assertEquals("Wrong report end date", expectedLine.getReportEndDate(), actualLine.getReportEndDate());
        }
    }

    protected void assertSpecialCharacterRemovalsWereRecordedProperly(
            final Map<Integer, List<Integer>> expectedFilteredCells,
            final ConcurStandardAccountingExtractFile fileDTO) {
        final Map<Integer, List<Integer>> actualFilteredCells = Stream
                .concat(Stream.of(fileDTO), fileDTO.getConcurStandardAccountingExtractDetailLines().stream())
                .filter(line -> CollectionUtils.isNotEmpty(line.getColumnNumbersContainingSpecialCharacters()))
                .collect(Collectors.toUnmodifiableMap(
                        line -> line.getLineNumber(), line -> line.getColumnNumbersContainingSpecialCharacters()));
        assertEquals("Wrong number of lines that had special characters removed",
                expectedFilteredCells.size(), actualFilteredCells.size());
        for (final Map.Entry<Integer, List<Integer>> expectedFilteredLine : expectedFilteredCells.entrySet()) {
            final Integer lineNumber = expectedFilteredLine.getKey();
            final List<Integer> expectedColumnNumbers = expectedFilteredLine.getValue();
            final List<Integer> actualColumnNumbers = actualFilteredCells.get(lineNumber);
            assertNotNull("Line " + lineNumber + " should have had special characters removed from it",
                    actualColumnNumbers);
            assertArrayEquals("Wrong columns had special characters removed on line " + lineNumber,
                    expectedColumnNumbers.toArray(), actualColumnNumbers.toArray());
        }
    }

    protected void assertEqualsIgnoreCase(String message, String expected, String actual) throws Exception {
        assertEquals(message, StringUtils.upperCase(expected, Locale.US), StringUtils.upperCase(actual, Locale.US));
    }

    protected void assertConcurSAEFileFailsParsingDueToMisusedQuotes(String fileName) throws Exception {
        CuDelimitedFlatFileParser saeParser = getFlatFileParser(CONCUR_SAE_FILE_PARSER_BEAN);
        assertDelimitedFileFailsParsingDueToMisusedQuotes(saeParser, fileName);
    }

    private List<Integer> columnNumbers(final Integer... columnNumbers) {
        return List.of(columnNumbers);
    }

}
