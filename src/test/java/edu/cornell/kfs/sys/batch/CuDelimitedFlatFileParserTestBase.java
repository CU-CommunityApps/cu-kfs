package edu.cornell.kfs.sys.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.LoadFileUtils;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

public abstract class CuDelimitedFlatFileParserTestBase extends SpringEnabledMicroTestBase {

    protected void assertDelimitedFileDataParsesToSingleTopLevelDTOCorrectly(
            CuDelimitedFlatFileParser flatFileParser, Object expectedResult, String fileName) throws Exception {
        assertFileNameUsesExpectedNamingConventionsFromParser(flatFileParser, fileName);
        
        byte[] fileByteContent = getFileByteContent(flatFileParser, fileName);
        List<?> results = (List<?>) flatFileParser.parse(fileByteContent);
        
        assertEquals("Wrong number of top-level objects parsed from file", 1, results.size());
        Object actualResult = results.get(0);
        assertFileDataWasParsedCorrectly(expectedResult, actualResult);
    }

    protected abstract void assertFileDataWasParsedCorrectly(Object expectedResult, Object actualResult) throws Exception;

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

    protected void assertFileNameUsesExpectedNamingConventionsFromParser(CuDelimitedFlatFileParser flatFileParser, String fileName) {
        assertEquals("File Name has the wrong extension", flatFileParser.getFileExtension(),
                StringUtils.substringAfterLast(fileName, KFSConstants.DELIMITER));
        assertTrue("File Name does not have the expected prefix", StringUtils.startsWith(fileName, flatFileParser.getFileNamePrefix()));
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
