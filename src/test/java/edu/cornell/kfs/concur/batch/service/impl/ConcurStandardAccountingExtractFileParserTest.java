package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
    public void testLoadBadSAEFileWithImproperlyQuotedDelimiters() throws Exception {
        assertConcurSAEFileFailsParsingDueToMisusedQuotes("extract_CES_SAE_v3_testBadQuoteFile.txt");
    }

    protected void assertConcurSAEFileParsesCorrectly(ConcurSAEFileFixture expectedFixture, String fileName) throws Exception {
        CuDelimitedFlatFileParser saeParser = getFlatFileParser(CONCUR_SAE_FILE_PARSER_BEAN);
        assertDelimitedFileDataParsesToSingleTopLevelDTOCorrectly(saeParser, expectedFixture, fileName);
    }

    @Override
    protected void assertFileDataWasParsedCorrectly(Object expectedResult, Object actualResult) throws Exception {
        ConcurSAEFileFixture expectedFixture = (ConcurSAEFileFixture) expectedResult;
        ConcurStandardAccountingExtractFile expectedDTO = expectedFixture.toExtractFile();
        ConcurStandardAccountingExtractFile actualDTO = (ConcurStandardAccountingExtractFile) actualResult;
        
        assertEquals("Wrong batch date", expectedDTO.getBatchDate(), actualDTO.getBatchDate());
        assertEquals("Wrong record count", expectedDTO.getRecordCount(), actualDTO.getRecordCount());
        assertEquals("Wrong journal amount total", expectedDTO.getJournalAmountTotal(), actualDTO.getJournalAmountTotal());
        assertEquals("Wrong batch ID", expectedDTO.getBatchId(), actualDTO.getBatchId());
        assertDetailLinesWereParsedCorrectly(
                expectedDTO.getConcurStandardAccountingExtractDetailLines(), actualDTO.getConcurStandardAccountingExtractDetailLines());
    }

    protected void assertDetailLinesWereParsedCorrectly(
            List<ConcurStandardAccountingExtractDetailLine> expectedLines, List<ConcurStandardAccountingExtractDetailLine> actualLines)
            throws Exception {
        assertEquals("Wrong number of detail lines", expectedLines.size(), actualLines.size());
        
        for (int i = 0; i < expectedLines.size(); i++) {
            ConcurStandardAccountingExtractDetailLine expectedLine = expectedLines.get(i);
            ConcurStandardAccountingExtractDetailLine actualLine = actualLines.get(i);
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

    protected void assertEqualsIgnoreCase(String message, String expected, String actual) throws Exception {
        assertEquals(message, StringUtils.upperCase(expected), StringUtils.upperCase(actual));
    }

    protected void assertConcurSAEFileFailsParsingDueToMisusedQuotes(String fileName) throws Exception {
        CuDelimitedFlatFileParser saeParser = getFlatFileParser(CONCUR_SAE_FILE_PARSER_BEAN);
        assertDelimitedFileFailsParsingDueToMisusedQuotes(saeParser, fileName);
    }

}
