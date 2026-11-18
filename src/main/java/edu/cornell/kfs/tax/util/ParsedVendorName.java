package edu.cornell.kfs.tax.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public final class ParsedVendorName {

    public static final ParsedVendorName EMPTY = new ParsedVendorName();

    private static final Pattern SUFFIXES_PATTERN = Pattern.compile(
            "(?i)^([JS]R\\.?)|(P\\.?(?:H\\.?D\\.?|(?:L\\.?L\\.?)?C\\.?)|M\\.?D\\.?)|(?-i:([IVX]+))$");

    private static final Pattern MIDDLE_INITIAL_PATTERN = Pattern.compile("^(?i)[A-Z]\\.?$");

    private static final int PROFESSIONAL_SUFFIX_GROUP = 2;
    private static final int NO_MATCH_INDEX = -1;

    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String suffix;

    public ParsedVendorName() {
        this.firstName = null;
        this.middleName = null;
        this.lastName = null;
        this.suffix = null;
    }

    public ParsedVendorName(final String firstName, final String middleName, final String lastName, final String suffix) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.suffix = suffix;
    }

    public static ParsedVendorName of(final String firstNameSection, final String lastNameSection) {
        final NameBuilder builder = new NameBuilder();
        parseFirstAndMiddleNames(builder, firstNameSection);
        parseLastName(builder, lastNameSection);
        return new ParsedVendorName(builder.firstName, builder.middleName, builder.lastName, builder.getJoinedSuffix());
    }

    private static void parseFirstAndMiddleNames(final NameBuilder builder, final String firstAndMiddleNameFragment) {
        if (StringUtils.isBlank(firstAndMiddleNameFragment)) {
            return;
        }

        final String[] fragments = StringUtils.split(firstAndMiddleNameFragment);
        if (fragments.length == 1) {
            builder.firstName = fragments[0];
            return;
        }

        final int startIndex = parseSuffixesFromStartOfNameThatWerePlacedAfterComma(builder, fragments);
        final int endIndex = parseSuffixesAfterFirstAndMiddleNames(builder, fragments, startIndex);

        builder.firstName = fragments[startIndex];
        if (endIndex > startIndex + 1) {
            builder.middleName = StringUtils.join(fragments, KFSConstants.BLANK_SPACE, startIndex + 1, endIndex);
        }
    }

    private static void parseLastName(final NameBuilder builder, final String lastNameFragment) {
        if (StringUtils.isBlank(lastNameFragment)) {
            return;
        }

        final String[] fragments = StringUtils.split(lastNameFragment);
        if (fragments.length == 1) {
            builder.lastName = fragments[0];
            return;
        }

        final int startIndex = 0;
        final int endIndex = parseSuffixesFromEndOfName(builder, fragments, startIndex);

        if (endIndex > startIndex + 1) {
            builder.lastName = StringUtils.join(fragments, KFSConstants.BLANK_SPACE, startIndex, endIndex);
        } else {
            builder.lastName = fragments[startIndex];
        }
    }

    private static int parseSuffixesFromStartOfNameThatWerePlacedAfterComma(
            final NameBuilder builder, final String[] fragments) {
        int startIndex = 0;
        boolean stillFindingSuffixes = true;
        do {
            final Matcher suffixMatcher = SUFFIXES_PATTERN.matcher(fragments[startIndex]);
            if (suffixMatcher.matches()) {
                if (isGenerationalSuffix(suffixMatcher)) {
                    builder.suffixes.add(fragments[startIndex]);
                }
                startIndex++;
            } else {
                stillFindingSuffixes = false;
            }
        } while (stillFindingSuffixes && startIndex < fragments.length - 1);

        return startIndex;
    }

    private static int parseSuffixesAfterFirstAndMiddleNames(final NameBuilder builder, final String[] fragments,
            final int startIndex) {
        final int endIndex = fragments.length;
        if (startIndex >= endIndex - 1 || MIDDLE_INITIAL_PATTERN.matcher(fragments[endIndex - 1]).matches()) {
            return endIndex;
        } else {
            return parseSuffixesFromEndOfName(builder, fragments, startIndex);
        }
    }

    private static int parseSuffixesFromEndOfName(final NameBuilder builder, final String[] fragments,
            final int startIndex) {
        int endIndex = fragments.length;
        boolean stillFindingSuffixes = true;
        do {
            final Matcher suffixMatcher = SUFFIXES_PATTERN.matcher(fragments[endIndex - 1]);
            if (suffixMatcher.matches()) {
                if (isGenerationalSuffix(suffixMatcher)) {
                    builder.suffixes.add(fragments[endIndex - 1]);
                }
                endIndex--;
            } else {
                stillFindingSuffixes = false;
            }
        } while (stillFindingSuffixes && endIndex > startIndex + 1);

        return endIndex;
    }

    private static boolean isGenerationalSuffix(final Matcher suffixMatcher) {
        return suffixMatcher.start(PROFESSIONAL_SUFFIX_GROUP) == NO_MATCH_INDEX;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSuffix() {
        return suffix;
    }

    private static final class NameBuilder {
        private String firstName;
        private String middleName;
        private String lastName;
        private List<String> suffixes;

        private NameBuilder() {
            this.suffixes = new ArrayList<>();
        }

        private String getJoinedSuffix() {
            if (suffixes.isEmpty()) {
                return null;
            } else if (suffixes.size() == 1) {
                return suffixes.get(0);
            } else {
                return suffixes.stream().collect(Collectors.joining(KFSConstants.BLANK_SPACE));
            }
        }
    }

}
