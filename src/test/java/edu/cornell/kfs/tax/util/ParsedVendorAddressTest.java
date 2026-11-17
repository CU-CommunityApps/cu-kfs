package edu.cornell.kfs.tax.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.fixture.ParsedVendorAddressFixture;

@Execution(ExecutionMode.SAME_THREAD)
public class ParsedVendorAddressTest {

    enum LocalTestCase {
        @ParsedVendorAddressFixture(
                rawLine1Address = "", rawLine2Address = "",
                expectedLine1 = "", expectedLine2 = "", expectedApartment = ""
        )
        EMPTY_ADDRESS,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_NO_APARTMENT,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St #45", rawLine2Address = "",
                expectedLine1 = "321 Main St #45", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_NUMERIC_SUFFIX_NO_APARTMENT,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Sector 55", rawLine2Address = "",
                expectedLine1 = "321 Main St Sector 55", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_NONAPARTMENT,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Sector #55", rawLine2Address = "",
                expectedLine1 = "321 Main St Sector #55", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_NONAPARTMENT_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Sector 55", rawLine2Address = "",
                expectedLine1 = "321 Main St, Sector 55", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_NONAPARTMENT_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Sector #55", rawLine2Address = "",
                expectedLine1 = "321 Main St, Sector #55", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_NONAPARTMENT_AND_POUND_SIGN_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apt 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apt 55"
        )
        SINGLE_LINE_WITH_APARTMENT_SHORTHAND,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apt #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apt #55"
        )
        SINGLE_LINE_WITH_APARTMENT_SHORTHAND_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Apt 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apt 55"
        )
        SINGLE_LINE_WITH_APARTMENT_SHORTHAND_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apt. 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apt. 55"
        )
        SINGLE_LINE_WITH_APARTMENT_SHORTHAND_AND_PERIOD,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Apt. #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apt. #55"
        )
        SINGLE_LINE_WITH_APARTMENT_SHORTHAND_AND_POUND_SIGN_AND_PUNCTUATION,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apartment 55"
        )
        SINGLE_LINE_WITH_APARTMENT,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apartment #55"
        )
        SINGLE_LINE_WITH_APARTMENT_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Apartment 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apartment 55"
        )
        SINGLE_LINE_WITH_APARTMENT_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Apartment #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apartment #55"
        )
        SINGLE_LINE_WITH_APARTMENT_AND_POUND_SIGN_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartments 55", rawLine2Address = "",
                expectedLine1 = "321 Main St Apartments", expectedLine2 = "", expectedApartment = "55"
        )
        SINGLE_LINE_WITH_APARTMENT_PLURAL,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartments #55", rawLine2Address = "",
                expectedLine1 = "321 Main St Apartments", expectedLine2 = "", expectedApartment = "#55"
        )
        SINGLE_LINE_WITH_APARTMENT_PLURAL_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartments, 55", rawLine2Address = "",
                expectedLine1 = "321 Main St Apartments", expectedLine2 = "", expectedApartment = "55"
        )
        SINGLE_LINE_WITH_APARTMENT_PLURAL_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartments, #55", rawLine2Address = "",
                expectedLine1 = "321 Main St Apartments", expectedLine2 = "", expectedApartment = "#55"
        )
        SINGLE_LINE_WITH_APARTMENT_PLURAL_AND_POUND_SIGN_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Ste 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Ste 55"
        )
        SINGLE_LINE_WITH_SUITE_SHORTHAND,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Ste #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Ste #55"
        )
        SINGLE_LINE_WITH_SUITE_SHORTHAND_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Ste 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Ste 55"
        )
        SINGLE_LINE_WITH_SUITE_SHORTHAND_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Ste. 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Ste. 55"
        )
        SINGLE_LINE_WITH_SUITE_SHORTHAND_AND_PERIOD,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Ste. #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Ste. #55"
        )
        SINGLE_LINE_WITH_SUITE_SHORTHAND_AND_POUND_SIGN_AND_PUNCTUATION,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Suite 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Suite 55"
        )
        SINGLE_LINE_WITH_SUITE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Suite #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Suite #55"
        )
        SINGLE_LINE_WITH_SUITE_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Suite 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Suite 55"
        )
        SINGLE_LINE_WITH_SUITE_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Suite #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Suite #55"
        )
        SINGLE_LINE_WITH_SUITE_AND_POUND_SIGN_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Bldg 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Bldg 55"
        )
        SINGLE_LINE_WITH_BUILDING_SHORTHAND,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Bldg #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Bldg #55"
        )
        SINGLE_LINE_WITH_BUILDING_SHORTHAND_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Bldg 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Bldg 55"
        )
        SINGLE_LINE_WITH_BUILDING_SHORTHAND_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Bldg. 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Bldg. 55"
        )
        SINGLE_LINE_WITH_BUILDING_SHORTHAND_AND_PERIOD,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Bldg. #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Bldg. #55"
        )
        SINGLE_LINE_WITH_BUILDING_SHORTHAND_AND_POUND_SIGN_AND_PUNCTUATION,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Building 55"
        )
        SINGLE_LINE_WITH_BUILDING,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Building #55"
        )
        SINGLE_LINE_WITH_BUILDING_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Building 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Building 55"
        )
        SINGLE_LINE_WITH_BUILDING_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Building #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Building #55"
        )
        SINGLE_LINE_WITH_BUILDING_AND_POUND_SIGN_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Rm 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Rm 55"
        )
        SINGLE_LINE_WITH_ROOM_SHORTHAND,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Rm #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Rm #55"
        )
        SINGLE_LINE_WITH_ROOM_SHORTHAND_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Rm 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Rm 55"
        )
        SINGLE_LINE_WITH_ROOM_SHORTHAND_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Rm. 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Rm. 55"
        )
        SINGLE_LINE_WITH_ROOM_SHORTHAND_AND_PERIOD,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Rm. #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Rm. #55"
        )
        SINGLE_LINE_WITH_ROOM_SHORTHAND_AND_POUND_SIGN_AND_PUNCTUATION,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Room 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Room 55"
        )
        SINGLE_LINE_WITH_ROOM,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Room #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Room #55"
        )
        SINGLE_LINE_WITH_ROOM_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Room 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Room 55"
        )
        SINGLE_LINE_WITH_ROOM_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Room #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Room #55"
        )
        SINGLE_LINE_WITH_ROOM_AND_POUND_SIGN_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Unit 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Unit 55"
        )
        SINGLE_LINE_WITH_UNIT,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Unit #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Unit #55"
        )
        SINGLE_LINE_WITH_UNIT_AND_POUND_SIGN,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Unit 55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Unit 55"
        )
        SINGLE_LINE_WITH_UNIT_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St, Unit #55", rawLine2Address = "",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Unit #55"
        )
        SINGLE_LINE_WITH_UNIT_AND_POUND_SIGN_AND_COMMA,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Apt", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Apt", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_APARTMENT_SHORTHAND_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Apartment", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Apartment", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_APARTMENT_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Apartments", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Apartments", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_APARTMENT_PLURAL_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Ste", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Ste", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_SUITE_SHORTHAND_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Suite", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Suite", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_SUITE_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Bldg", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Bldg", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_BUILDING_SHORTHAND_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Building", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Building", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_BUILDING_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Rm", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Rm", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_ROOM_SHORTHAND_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Room", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Room", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_ROOM_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St 7th Unit", rawLine2Address = "",
                expectedLine1 = "321 Main St 7th Unit", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_UNIT_KEYWORD_SUFFIX,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apt 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Apt 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_APARTMENT_SHORTHAND_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Apartment 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_APARTMENT_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartments 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Apartments 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_APARTMENT_PLURAL_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Ste 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Ste 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_SUITE_SHORTHAND_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Suite 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Suite 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_SUITE_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Bldg 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Bldg 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_BUILDING_SHORTHAND_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Building 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_BUILDING_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Rm 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Rm 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_ROOM_SHORTHAND_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Room 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Room 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_ROOM_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Unit 55 Sector 99", rawLine2Address = "",
                expectedLine1 = "321 Main St Unit 55 Sector 99", expectedLine2 = "", expectedApartment = ""
        )
        SINGLE_LINE_WITH_UNIT_IN_MIDDLE,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = ""
        )
        MULTI_LINE_NO_APARTMENT,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Apt 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apt 55"
        )
        MULTI_LINE_WITH_ONLY_APARTMENT_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Apartment 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Apartment 55"
        )
        MULTI_LINE_WITH_ONLY_APARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Ste 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Ste 55"
        )
        MULTI_LINE_WITH_ONLY_SUITE_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Suite 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Suite 55"
        )
        MULTI_LINE_WITH_ONLY_SUITE_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Bldg 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Bldg 55"
        )
        MULTI_LINE_WITH_ONLY_BUILDING_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Building 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Building 55"
        )
        MULTI_LINE_WITH_ONLY_BUILDING_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Rm 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Rm 55"
        )
        MULTI_LINE_WITH_ONLY_ROOM_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Room 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Room 55"
        )
        MULTI_LINE_WITH_ONLY_ROOM_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Unit 55",
                expectedLine1 = "321 Main St", expectedLine2 = "", expectedApartment = "Unit 55"
        )
        MULTI_LINE_WITH_ONLY_UNIT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Apt 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Apt 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_APARTMENT_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Apartment 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Apartment 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_APARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Ste 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Ste 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_SUITE_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Suite 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Suite 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_SUITE_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Bldg 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Bldg 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_BUILDING_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Building 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Building 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_BUILDING_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Rm 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Rm 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_ROOM_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Room 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Room 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_ROOM_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St", rawLine2Address = "Lot 9 Unit 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Unit 55", expectedApartment = ""
        )
        MULTI_LINE_WITH_MORE_THAN_UNIT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building 4", rawLine2Address = "Apt 55",
                expectedLine1 = "321 Main St Building 4", expectedLine2 = "", expectedApartment = "Apt 55"
        )
        MULTI_LINE_WITH_BUILDING_IN_LINE1_AND_ONLY_APARTMENT_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building 4", rawLine2Address = "Apartment 55",
                expectedLine1 = "321 Main St Building 4", expectedLine2 = "", expectedApartment = "Apartment 55"
        )
        MULTI_LINE_WITH_BUILDING_IN_LINE1_AND_ONLY_APARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Ste 55",
                expectedLine1 = "321 Main St Apartment 4", expectedLine2 = "", expectedApartment = "Ste 55"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_ONLY_SUITE_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Suite 55",
                expectedLine1 = "321 Main St Apartment 4", expectedLine2 = "", expectedApartment = "Suite 55"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_ONLY_SUITE_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Bldg 55",
                expectedLine1 = "321 Main St Apartment 4", expectedLine2 = "", expectedApartment = "Bldg 55"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_ONLY_BUILDING_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Building 55",
                expectedLine1 = "321 Main St Apartment 4", expectedLine2 = "", expectedApartment = "Building 55"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_ONLY_BUILDING_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Rm 55",
                expectedLine1 = "321 Main St Apartment 4", expectedLine2 = "", expectedApartment = "Rm 55"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_ONLY_ROOM_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Room 55",
                expectedLine1 = "321 Main St Apartment 4", expectedLine2 = "", expectedApartment = "Room 55"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_ONLY_ROOM_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Unit 55",
                expectedLine1 = "321 Main St Apartment 4", expectedLine2 = "", expectedApartment = "Unit 55"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_ONLY_UNIT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building 4", rawLine2Address = "Lot 9 Apt 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Apt 55", expectedApartment = "Building 4"
        )
        MULTI_LINE_WITH_BUILDING_IN_LINE1_AND_MORE_THAN_APARTMENT_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building 4", rawLine2Address = "Lot 9 Apartment 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Apartment 55", expectedApartment = "Building 4"
        )
        MULTI_LINE_WITH_BUILDING_IN_LINE1_AND_MORE_THAN_APARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Lot 9 Ste 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Ste 55", expectedApartment = "Apartment 4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_MORE_THAN_SUITE_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Lot 9 Suite 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Suite 55", expectedApartment = "Apartment 4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_MORE_THAN_SUITE_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Lot 9 Bldg 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Bldg 55", expectedApartment = "Apartment 4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_MORE_THAN_BUILDING_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Lot 9 Building 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Building 55", expectedApartment = "Apartment 4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_MORE_THAN_BUILDING_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Lot 9 Rm 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Rm 55", expectedApartment = "Apartment 4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_MORE_THAN_ROOM_SHORTHAND_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Lot 9 Room 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Room 55", expectedApartment = "Apartment 4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_MORE_THAN_ROOM_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment 4", rawLine2Address = "Lot 9 Unit 55",
                expectedLine1 = "321 Main St", expectedLine2 = "Lot 9 Unit 55", expectedApartment = "Apartment 4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_MORE_THAN_UNIT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apt #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Apt #4"
        )
        MULTI_LINE_WITH_APARTMENT_SHORTHAND_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartment #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Apartment #4"
        )
        MULTI_LINE_WITH_APARTMENT_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Apartments #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St Apartments", expectedLine2 = "Test Campus", expectedApartment = "#4"
        )
        MULTI_LINE_WITH_APARTMENT_PLURAL_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Ste #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Ste #4"
        )
        MULTI_LINE_WITH_SUITE_SHORTHAND_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Suite #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Suite #4"
        )
        MULTI_LINE_WITH_SUITE_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Bldg #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Bldg #4"
        )
        MULTI_LINE_WITH_BUILDING_SHORTHAND_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Building #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Building #4"
        )
        MULTI_LINE_WITH_BUILDING_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Rm #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Rm #4"
        )
        MULTI_LINE_WITH_ROOM_SHORTHAND_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Room #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Room #4"
        )
        MULTI_LINE_WITH_ROOM_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,

        @ParsedVendorAddressFixture(
                rawLine1Address = "321 Main St Unit #4", rawLine2Address = "Test Campus",
                expectedLine1 = "321 Main St", expectedLine2 = "Test Campus", expectedApartment = "Unit #4"
        )
        MULTI_LINE_WITH_UNIT_IN_LINE1_AND_NONAPARTMENT_IN_LINE2,
        ;

        public Arguments toNamedAnnotationFixtureArgument() {
            return FixtureUtils.createNamedAnnotationFixtureArgument(this, ParsedVendorAddressFixture.class);
        }
    }

    static Stream<Arguments> addressTestCases() {
        return Arrays.stream(LocalTestCase.values())
                .map(LocalTestCase::toNamedAnnotationFixtureArgument);
    }

    @Test
    void testEmptyAddressInstanceHasEmptyDataFields() throws Exception {
        final ParsedVendorAddressFixture emptyAddressFixture = FixtureUtils.getAnnotationBasedFixture(
                LocalTestCase.EMPTY_ADDRESS, ParsedVendorAddressFixture.class);
        assertAddressWasParsedCorrectly(emptyAddressFixture, ParsedVendorAddress.EMPTY);
    }

    @ParameterizedTest
    @MethodSource("addressTestCases")
    void testTax1099AddressParsing(final ParsedVendorAddressFixture fixture) throws Exception {
        final ParsedVendorAddress parsedAddress = ParsedVendorAddress.of(
                fixture.rawLine1Address(), fixture.rawLine2Address());
        assertAddressWasParsedCorrectly(fixture, parsedAddress);
    }

    @ParameterizedTest
    @MethodSource("addressTestCases")
    void testTax1099AddressParsingAllUppercase(final ParsedVendorAddressFixture fixture) throws Exception {
        final ParsedVendorAddress parsedAddress = ParsedVendorAddress.of(
                convertToUpperCase(fixture.rawLine1Address()), convertToUpperCase(fixture.rawLine2Address()));
        assertAddressWasParsedCorrectly(fixture, parsedAddress, this::convertToUpperCase);
    }

    @ParameterizedTest
    @MethodSource("addressTestCases")
    void testTax1099AddressParsingAllLowercase(final ParsedVendorAddressFixture fixture) throws Exception {
        final ParsedVendorAddress parsedAddress = ParsedVendorAddress.of(
                convertToLowerCase(fixture.rawLine1Address()), convertToLowerCase(fixture.rawLine2Address()));
        assertAddressWasParsedCorrectly(fixture, parsedAddress, this::convertToLowerCase);
    }

    private void assertAddressWasParsedCorrectly(final ParsedVendorAddressFixture fixture,
            final ParsedVendorAddress parsedAddress) throws Exception {
        assertAddressWasParsedCorrectly(fixture, parsedAddress, Function.identity());
    }

    private void assertAddressWasParsedCorrectly(final ParsedVendorAddressFixture fixture,
            final ParsedVendorAddress parsedAddress, final Function<String, String> expectedValueAdjuster) throws Exception {
        assertAddressFieldIsCorrect("rawLine1Address",
                expectedValueAdjuster.apply(fixture.rawLine1Address()), parsedAddress.getRawLine1Address());
        assertAddressFieldIsCorrect("rawLine2Address",
                expectedValueAdjuster.apply(fixture.rawLine2Address()), parsedAddress.getRawLine2Address());
        assertAddressFieldIsCorrect("line1Address",
                expectedValueAdjuster.apply(fixture.expectedLine1()), parsedAddress.getLine1Address());
        assertAddressFieldIsCorrect("line2Address",
                expectedValueAdjuster.apply(fixture.expectedLine2()), parsedAddress.getLine2Address());
        assertAddressFieldIsCorrect("apartmentNumber",
                expectedValueAdjuster.apply(fixture.expectedApartment()), parsedAddress.getApartmentNumber());
    }

    private void assertAddressFieldIsCorrect(final String fieldName, final String expectedValue,
            final String actualValue) throws Exception {
        if (StringUtils.isNotBlank(expectedValue)) {
            assertEquals(expectedValue, actualValue, fieldName + " field has the wrong value");
        } else {
            assertTrue(StringUtils.isEmpty(actualValue), fieldName + " field should have been null or empty");
        }
    }

    private String convertToUpperCase(final String value) {
        return StringUtils.upperCase(value, Locale.US);
    }

    private String convertToLowerCase(final String value) {
        return StringUtils.lowerCase(value, Locale.US);
    }

}
