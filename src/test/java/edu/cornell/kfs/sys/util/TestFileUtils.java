package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.core.io.support.ResourcePatternResolver;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;

public final class TestFileUtils {

    private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("\\r?\\n");

    public static String getFileContentsWithoutFirstLineAndBlankLines(
            final String fileName) throws IOException {
        final String fileContents = getFileContents(fileName);
        final String[] fileLines = LINE_SEPARATOR_PATTERN.split(fileContents);
        final StringBuilder cleanedContents = new StringBuilder(fileContents.length());
        for (int i = 1; i < fileLines.length; i++) {
            if (StringUtils.isNotBlank(fileLines[i])) {
                cleanedContents.append(fileLines[i]).append(KFSConstants.NEWLINE);
            }
        }
        return cleanedContents.toString();
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

    public static void assertFileContentsMatch(final String expectedFileName, final String actualFileName,
            final Function<String, String> expectedFileContentConverter) throws IOException {
        final String expectedUnconvertedContent = getFileContents(expectedFileName);
        final String expectedConvertedContent = expectedFileContentConverter.apply(expectedUnconvertedContent);
        final String actualContent = getFileContents(actualFileName);
        assertEquals(expectedConvertedContent, actualContent, "Wrong file contents");
    }

}
