package edu.cornell.kfs.sys.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.batch.dto.DelimitedFileDTO;
import edu.cornell.kfs.sys.batch.dto.DelimitedFileLineDTO;
import edu.cornell.kfs.sys.batch.dto.fixture.DelimitedFileDTOFixture;
import edu.cornell.kfs.sys.batch.dto.fixture.DelimitedFileLineDTOFixture;
import edu.cornell.kfs.sys.util.LoadFileUtils;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@LoadSpringFile("edu/cornell/kfs/sys/batch/cu-spring-delimited-flat-file-test.xml")
public class CuDelimitedFlatFileParserTest extends SpringEnabledMicroTestBase {

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
        assertDelimitedFileDataParsesCorrectly(csvFileParser, expectedDTO, fileName);
    }

    protected void assertSemicolonDelimitedFileDataParsesCorrectly(DelimitedFileDTOFixture expectedDTO, String fileName) throws Exception {
        CuDelimitedFlatFileParser csvFileParser = getFlatFileParser(SEMICOLON_DELIMITED_FILE_PARSER_BEAN);
        assertDelimitedFileDataParsesCorrectly(csvFileParser, expectedDTO, fileName);
    }

    protected void assertDelimitedFileDataParsesCorrectly(
            CuDelimitedFlatFileParser flatFileParser, DelimitedFileDTOFixture expectedDTO, String fileName) throws Exception {
        assertFileNameUsesExpectedNamingConventionsFromParser(flatFileParser, fileName);
        
        byte[] fileByteContent = getFileByteContent(flatFileParser, fileName);
        List<?> results = (List<?>) flatFileParser.parse(fileByteContent);
        
        assertEquals("Wrong number of top-level objects parsed from file", 1, results.size());
        DelimitedFileDTO actualDTO = (DelimitedFileDTO) results.get(0);
        assertFileDataWasParsedCorrectly(expectedDTO, actualDTO);
    }

    protected void assertFileNameUsesExpectedNamingConventionsFromParser(CuDelimitedFlatFileParser flatFileParser, String fileName) {
        assertEquals("File Name has the wrong extension", flatFileParser.getFileExtension(),
                StringUtils.substringAfterLast(fileName, KFSConstants.DELIMITER));
        assertTrue("File Name does not have the expected prefix", StringUtils.startsWith(fileName, flatFileParser.getFileNamePrefix()));
    }

    protected void assertFileDataWasParsedCorrectly(DelimitedFileDTOFixture expectedDTO, DelimitedFileDTO actualDTO) throws Exception {
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

    protected void assertDelimitedFileFailsParsingDueToMisusedQuotes(
            CuDelimitedFlatFileParser flatFileParser, String fileName) throws Exception {
        assertFileNameUsesExpectedNamingConventionsFromParser(flatFileParser, fileName);
        byte[] fileByteContent = getFileByteContent(flatFileParser, fileName);
        try {
            flatFileParser.parse(fileByteContent);
            fail("File parsing should have thrown an exception due to misused quotes");
        } catch (ParseException e) {
            assertTrue("File parsing failure should have been caused by a NoSuchElementException when iterating over the file rows, "
                    + "due to a row-reading problem resulting from misused quotes", e.getCause() instanceof NoSuchElementException);
        }
    }

    protected byte[] getFileByteContent(CuDelimitedFlatFileParser flatFileParser, String fileName) {
        String fullFileName = flatFileParser.getDirectoryPath() + CUKFSConstants.SLASH + fileName;
        File dataFile = new File(fullFileName);
        return LoadFileUtils.safelyLoadFileBytes(dataFile);
    }

    protected CuDelimitedFlatFileParser getFlatFileParser(String beanName) {
        return springContext.getBean(beanName, CuDelimitedFlatFileParser.class);
    }

}
