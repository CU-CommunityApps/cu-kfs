package edu.cornell.kfs.sys.batch;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import edu.cornell.kfs.sys.batch.dto.DelimitedFileDTO;
import edu.cornell.kfs.sys.batch.dto.DelimitedFileLineDTO;
import edu.cornell.kfs.sys.batch.dto.fixture.DelimitedFileDTOFixture;
import edu.cornell.kfs.sys.batch.dto.fixture.DelimitedFileLineDTOFixture;
import edu.cornell.kfs.sys.util.LoadSpringFile;

@LoadSpringFile("edu/cornell/kfs/sys/batch/cu-spring-delimited-flat-file-test.xml")
public class CuDelimitedFlatFileParserTest extends CuDelimitedFlatFileParserTestBase {

    private static final String COMMA_DELIMITED_FILE_PARSER_BEAN = "TestCommaDelimitedFileType";
    private static final String SEMICOLON_DELIMITED_FILE_PARSER_BEAN = "TestSemicolonDelimitedFileType";

    @Test
    public void testLoadGoodCsvFile() throws Exception {
        assertCsvFileDataParsesCorrectly(DelimitedFileDTOFixture.GOOD_FILE, "test_comma_delim_goodFile.csv");
    }

    @Test
    public void testLoadGoodCsvFileWithQuotedCommas() throws Exception {
        assertCsvFileDataParsesCorrectly(DelimitedFileDTOFixture.GOOD_QUOTE_FILE, "test_comma_delim_goodQuoteFile.csv");
    }

    @Test
    public void testLoadBadCsvFileWithImproperlyQuotedCommas() throws Exception {
        assertCsvFileFailsParsingDueToMisusedQuotes("test_comma_delim_badQuoteFile.csv");
    }

    @Test
    public void testLoadGoodSemicolonDelimitedFile() throws Exception {
        assertSemicolonDelimitedFileDataParsesCorrectly(DelimitedFileDTOFixture.GOOD_FILE, "test_sc_delim_goodFile.txt");
    }

    @Test
    public void testLoadGoodSemicolonDelimitedFileWithQuotedSemicolons() throws Exception {
        assertSemicolonDelimitedFileDataParsesCorrectly(DelimitedFileDTOFixture.GOOD_SEMICOLON_QUOTE_FILE, "test_sc_delim_goodQuoteFile.txt");
    }

    @Test
    public void testLoadBadSemicolonDelimitedFileWithImproperlyQuotedSemicolons() throws Exception {
        assertSemicolonDelimitedFileFailsParsingDueToMisusedQuotes("test_sc_delim_badQuoteFile.txt");
    }

    protected void assertCsvFileDataParsesCorrectly(DelimitedFileDTOFixture expectedDTO, String fileName) throws Exception {
        CuDelimitedFlatFileParser csvFileParser = getFlatFileParser(COMMA_DELIMITED_FILE_PARSER_BEAN);
        assertDelimitedFileDataParsesToSingleTopLevelDTOCorrectly(csvFileParser, expectedDTO, fileName);
    }

    protected void assertSemicolonDelimitedFileDataParsesCorrectly(DelimitedFileDTOFixture expectedDTO, String fileName) throws Exception {
        CuDelimitedFlatFileParser csvFileParser = getFlatFileParser(SEMICOLON_DELIMITED_FILE_PARSER_BEAN);
        assertDelimitedFileDataParsesToSingleTopLevelDTOCorrectly(csvFileParser, expectedDTO, fileName);
    }

    @Override
    protected void assertFileDataWasParsedCorrectly(Object expectedResult, Object actualResult) throws Exception {
        DelimitedFileDTOFixture expectedDTO = (DelimitedFileDTOFixture) expectedResult;
        DelimitedFileDTO actualDTO = (DelimitedFileDTO) actualResult;
        assertEquals("Wrong file ID", expectedDTO.fileId, actualDTO.getFileId());
        assertEquals("Wrong file description", expectedDTO.description, actualDTO.getDescription());
        
        List<DelimitedFileLineDTOFixture> expectedLines = expectedDTO.fileLines;
        List<DelimitedFileLineDTO> actualLines = actualDTO.getFileLines();
        assertEquals("Wrong number of data lines", expectedLines.size(), actualLines.size());
        
        for (int i = 0; i < expectedLines.size(); i++) {
            DelimitedFileLineDTOFixture expectedLine = expectedLines.get(i);
            DelimitedFileLineDTO actualLine = actualLines.get(i);
            assertEquals("Wrong line ID at index " + i, expectedLine.lineId, actualLine.getLineId());
            assertEquals("Wrong line description at index " + i, expectedLine.description, actualLine.getDescription());
            assertEquals("Wrong line date at index " + i, expectedLine.getLineDateAsSqlDate(), actualLine.getLineDate());
            assertEquals("Wrong line amount at index " + i, expectedLine.lineAmount, actualLine.getLineAmount());
            assertEquals("Wrong line flag at index " + i, expectedLine.lineFlag, actualLine.getLineFlag());
        }
    }

    protected void assertCsvFileFailsParsingDueToMisusedQuotes(String fileName) throws Exception {
        CuDelimitedFlatFileParser csvFileParser = getFlatFileParser(COMMA_DELIMITED_FILE_PARSER_BEAN);
        assertDelimitedFileFailsParsingDueToMisusedQuotes(csvFileParser, fileName);
    }

    protected void assertSemicolonDelimitedFileFailsParsingDueToMisusedQuotes(String fileName) throws Exception {
        CuDelimitedFlatFileParser csvFileParser = getFlatFileParser(SEMICOLON_DELIMITED_FILE_PARSER_BEAN);
        assertDelimitedFileFailsParsingDueToMisusedQuotes(csvFileParser, fileName);
    }

}
