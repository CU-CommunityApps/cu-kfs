package edu.cornell.kfs.tax.dataaccess.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public final class ParsedVendorAddress {

    public static final ParsedVendorAddress EMPTY = new ParsedVendorAddress();

    private static final Pattern APARTMENT_SUITE_UNIT_PATTERN = Pattern.compile(
            "^(?i)(.*?)(,?\\s?)(AP(ARTMEN)?TS?|S(UI)?TE|UNIT)(,?\\.?#? #? ?)(\\S+?)\\s*$");

    private static final String SAVE_ALL_BUT_NUMBER_AND_SEPARATOR = "$1$2$3";
    private static final String SAVE_ONLY_APARTMENT_PORTION = "$3$6$7";

    private static final int NON_APARTMENT_FRAGMENT_GROUP = 1;
    private static final int SEPARATOR_BEFORE_APARTMENT_GROUP = 2;
    private static final int APARTMENT_SUITE_UNIT_WORD_GROUP = 3;
    private static final int APARTMENT_SUITE_UNIT_NUMBER_GROUP = 7;
    private static final String APARTMENTS = "APARTMENTS";

    private final String rawLine1Address;
    private final String rawLine2Address;
    private final String line1Address;
    private final String line2Address;
    private final String apartmentNumber;

    public ParsedVendorAddress() {
        this.rawLine1Address = null;
        this.rawLine2Address = null;
        this.line1Address = null;
        this.line2Address = null;
        this.apartmentNumber = null;
    }

    public ParsedVendorAddress(final String rawLine1Address, final String rawLine2Address, final String line1Address,
            final String line2Address, final String apartmentNumber) {
        this.rawLine1Address = rawLine1Address;
        this.rawLine2Address = rawLine2Address;
        this.line1Address = line1Address;
        this.line2Address = line2Address;
        this.apartmentNumber = apartmentNumber;
    }

    public static ParsedVendorAddress of(final String rawLine1Address, final String rawLine2Address) {
        final VendorAddressBuilder builder = new VendorAddressBuilder();
        parseLine1Address(builder, rawLine1Address);
        parseLine2Address(builder, rawLine2Address);
        return new ParsedVendorAddress(rawLine1Address, rawLine2Address, builder.line1Address,
                builder.line2Address, builder.apartmentNumber);
    }

    private static void parseLine1Address(final VendorAddressBuilder builder, final String rawLine1Address) {
        if (StringUtils.isBlank(rawLine1Address)) {
            return;
        }

        final Matcher apartmentMatcher = APARTMENT_SUITE_UNIT_PATTERN.matcher(rawLine1Address);
        if (apartmentMatcher.matches()) {
            final String apartmentSuiteUnitWord = apartmentMatcher.group(APARTMENT_SUITE_UNIT_WORD_GROUP);
            if (StringUtils.equalsIgnoreCase(apartmentSuiteUnitWord, APARTMENTS)) {
                builder.apartmentNumber = apartmentMatcher.group(APARTMENT_SUITE_UNIT_NUMBER_GROUP);
                builder.line1Address = apartmentMatcher.replaceAll(SAVE_ALL_BUT_NUMBER_AND_SEPARATOR);
            } else {
                builder.line1Address = apartmentMatcher.group(NON_APARTMENT_FRAGMENT_GROUP);
                builder.apartmentNumber = apartmentMatcher.replaceAll(SAVE_ONLY_APARTMENT_PORTION);
            }
        } else {
            builder.line1Address = rawLine1Address;
        }
    }

    private static void parseLine2Address(final VendorAddressBuilder builder, final String rawLine2Address) {
        if (StringUtils.isBlank(rawLine2Address)) {
            return;
        } else if (StringUtils.isNotBlank(builder.apartmentNumber)) {
            builder.line2Address = rawLine2Address;
            return;
        }

        final Matcher apartmentMatcher = APARTMENT_SUITE_UNIT_PATTERN.matcher(rawLine2Address);
        if (apartmentMatcher.matches()
                && StringUtils.isBlank(apartmentMatcher.group(NON_APARTMENT_FRAGMENT_GROUP))
                && StringUtils.isBlank(apartmentMatcher.group(SEPARATOR_BEFORE_APARTMENT_GROUP))) {
            builder.apartmentNumber = rawLine2Address;
        } else {
            builder.line2Address = rawLine2Address;
        }
    }

    public String getRawLine1Address() {
        return rawLine1Address;
    }

    public String getRawLine2Address() {
        return rawLine2Address;
    }

    public String getLine1Address() {
        return line1Address;
    }

    public String getLine2Address() {
        return line2Address;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    private static final class VendorAddressBuilder {
        private String line1Address;
        private String line2Address;
        private String apartmentNumber;
    }

}
