package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.core.io.support.ResourcePatternResolver;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;

public final class TestFileUtils {

    private static final Pattern FIRST_LINE_AND_SUBSEQUENT_BLANK_LINES_PATTERN = Pattern.compile(
            "^.*?\\r?\\n(\\s*?\\r?\\n)*", Pattern.MULTILINE);

    public static String getFileContentsWithoutFirstLineAndSubsequentBlankLines(
            final String fileName) throws IOException {
        final String fileContents = getFileContents(fileName);
        return FIRST_LINE_AND_SUBSEQUENT_BLANK_LINES_PATTERN.matcher(fileContents)
                .replaceFirst(KFSConstants.EMPTY_STRING);
    }

    public static String getFileContents(final String fileName) throws IOException {
         try (
            final InputStream fileStream =
                    StringUtils.startsWithIgnoreCase(fileName, ResourcePatternResolver.CLASSPATH_URL_PREFIX)
                            ? CuCoreUtilities.getResourceAsStream(fileName)
                            : new FileInputStream(fileName);
        ) {
            return IOUtils.toString(fileStream, StandardCharsets.UTF_8);
        }
    }

    public static void assertFileContentsMatch(final String expectedFileName, final String actualFileName)
            throws IOException {
        final String expectedContent = getFileContents(expectedFileName);
        final String actualContent = getFileContents(actualFileName);
        assertEquals(expectedContent, actualContent, "Wrong file contents");
    }

}
