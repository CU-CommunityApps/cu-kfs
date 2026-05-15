package edu.cornell.kfs.cemi.sys.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.sys.CUKFSConstants;

public final class CemiUtils {

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter
                .ofPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US)
                .withZone(ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN));

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("\\W+");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z]\\w*$");
    private static final Pattern UNDERSCORE_AND_LETTER_PATTERN = Pattern.compile("_([A-Za-z])");
    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("^(\\w+/)*\\w+(\\.[A-Za-z0-9]+)?$");

    private static final Set<String> RESERVED_WORDS = Set.of("STATE");

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

    public static String formatTentativeSheetBoFieldName(final String name) {
        String result = formatSheetColumnName(name);
        result = StringUtils.lowerCase(result, Locale.US);
        result = UNDERSCORE_AND_LETTER_PATTERN.matcher(result).replaceAll(matchResult -> {
            final String letterAfterUnderscore = matchResult.group(1);
            return StringUtils.upperCase(letterAfterUnderscore, Locale.US);
        });
        return result;
    }

    public static String formatSheetColumnName(final String name) {
        final String columnName = formatNameForDatabase(name);
        return RESERVED_WORDS.contains(columnName) ? columnName + CemiBaseConstants.VAL_SUFFIX : columnName;
    }

    public static String formatNameForDatabase(final String name) {
        String result = StringUtils.defaultString(name);
        result = StringUtils.upperCase(result, Locale.US);
        result = WHITESPACE_PATTERN.matcher(result).replaceAll(CUKFSConstants.UNDERSCORE);
        result = NON_WORD_PATTERN.matcher(result).replaceAll(KFSConstants.EMPTY_STRING);
        return result;
    }

    public static boolean isFormattedAsValidIdentifier(final String name) {
        return StringUtils.isNotBlank(name) && IDENTIFIER_PATTERN.matcher(name).matches();
    }

    public static boolean isFormattedAsValidFilePath(final String path) {
        return StringUtils.isNotBlank(path) && FILE_PATH_PATTERN.matcher(path).matches();
    }

}
