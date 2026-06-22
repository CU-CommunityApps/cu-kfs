package edu.cornell.kfs.cemi.sys.util;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

public final class CemiUtils {

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter
                .ofPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US)
                .withZone(ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN));

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("^(\\w+/)*\\w+(\\.[A-Za-z0-9]+)?$");

    private static final Pattern WORD_CHARS_PATTERN = Pattern.compile("^\\w+$");

    private static final String generateDateTimeInConsistentFormat(final LocalDateTime dateTime) {
        return FILE_DATE_TIME_FORMATTER.format(dateTime);
    }

    public static String generateFileNameContainingDateTime(
            final LocalDateTime dateTime, final String fileNamePrefix, final String fileExtension) {
        final String dateTimeString = generateDateTimeInConsistentFormat(dateTime);
        return StringUtils.join(fileNamePrefix, dateTimeString, fileExtension);
    }

    public static String convertToBooleanValueForFileExtract(final boolean value) {
        return Boolean.toString(value)
                .toUpperCase(Locale.US);
    }

    public static String convertToBooleanValueForEIBFileExtract(final boolean value) {
        return value ? KRADConstants.YES_INDICATOR_VALUE : KRADConstants.NO_INDICATOR_VALUE;
    }

    public static final String generateBatchJobRunDateAsString(final LocalDateTime jobRunDate) {
        return generateDateTimeInConsistentFormat(jobRunDate);
    }

    public static int getHeaderRowCount(final CemiSheetDefinition sheetDefinition) {
        return sheetDefinition.getNumHeaderRows();
    }

    public static int getFullColumnCount(final CemiSheetDefinition sheetDefinition) {
        return sheetDefinition.getStartColumnIndex() + sheetDefinition.getFields().size();
    }

    public static int getDataColumnCount(final CemiSheetDefinition sheetDefinition) {
        return sheetDefinition.getFields().size();
    }

    public static String generateKeyForGroupingDuplicates(final String... propertyValues) {
        return generateConcatenatedKey(propertyValues);
    }

    public static String generateConcatenatedKey(final String... propertyValues) {
        return Stream.of(propertyValues)
                .map(StringUtils::trimToEmpty)
                .map(propertyValue -> propertyValue.toUpperCase(Locale.US))
                .collect(Collectors.joining(CUKFSConstants.SEMICOLON));
    }

    public static <T> String[] getDistinctValuesFromMatchingSubLists(
            final Map<String, List<String>> subLists, final List<T> dataObjects, final Function<T, String> keyGetter) {
        return dataObjects.stream()
                .map(keyGetter::apply)
                .filter(StringUtils::isNotBlank)
                .map(subLists::get)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .distinct()
                .toArray(String[]::new);
    }

    public static List<String> createListPaddedToMinimumSizeIfNecessary(final int minSize, final String... elements) {
        return createListPaddedToMinimumSizeIfNecessary(KFSConstants.EMPTY_STRING, minSize, elements);
    }

    @SafeVarargs
    public static <T> List<T> createListPaddedToMinimumSizeIfNecessary(
            final T emptyValue, final int minSize, final T... elements) {
        if (elements.length == 0) {
            return createListOfEmptyValues(minSize, emptyValue);
        } else if (elements.length >= minSize) {
            return List.of(elements);
        } else {
            final T[] minSizeArray = Arrays.copyOf(elements, minSize);
            Arrays.fill(minSizeArray, elements.length, minSize, emptyValue);
            return List.of(minSizeArray);
        }
    }

    public static List<String> createListOfEmptyStrings(final int size) {
        return createListOfEmptyValues(size, KFSConstants.EMPTY_STRING);
    }

    public static <T> List<T> createListOfEmptyValues(final int size, final T emptyValue) {
        return Collections.nCopies(size, emptyValue);
    }

    public static CemiOutputDefinition getOutputDefinitionFromCemiResourcesFile(
            final CemiOutputDefinitionFileType cemiOutputDefinitionFileType, final String subPath) throws IOException {
        Validate.notNull(cemiOutputDefinitionFileType, "cemiOutputDefinitionFileType cannot be null");
        Validate.isTrue(isFormattedAsValidFilePath(subPath), "subPath was blank or malformed");

        try (
            final InputStream inputStream = CuCoreUtilities.getResourceAsStream(
                    CemiBaseConstants.CEMI_OUTPUT_DEFINITION_FILE_PATH_PREFIX + subPath);
        ) {
            final byte[] fileContents = IOUtils.toByteArray(inputStream);
            return cemiOutputDefinitionFileType.parse(fileContents);
        }
    }

    public static boolean isFormattedAsValidFilePath(final String path) {
        return StringUtils.isNotBlank(path) && FILE_PATH_PATTERN.matcher(path).matches();
    }

    public static boolean valueOnlyContainsWordCharacters(final String value) {
        return StringUtils.isNotBlank(value) && WORD_CHARS_PATTERN.matcher(value).matches();
    }

}
