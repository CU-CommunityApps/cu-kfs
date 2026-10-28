package edu.cornell.kfs.cemi.module.purap.batch;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Replace this enum with a mapping table.
public enum CemiUnitOfMeasure {
    ONE_GALLON_JUG("JU", "GAL"),
    BAG("BG", "BG"),
    BALE("BA", "BAL"),
    BELT("BT", "E2"),
    BOARD_FEET("BF", "BFT"),
    BOOK("BK", "D63"),
    BOTTLE("BO", "BO"),
    BOX("BX", "BX"),
    BULK("BN", "VQ"),
    BUNDLE("BD", "BE"),
    BUSHEL("BU", "BU"),
    CAN("CN", "CN"),
    CARD("CG", "CG"),
    CARTON("CT", "CT"),
    CARTRIDGE("CQ", "CAR"),
    CASE_MAPPING1("CAS", "CS"),
    CASE_MAPPING2("CA", "CS"),
    CASE_MAPPING3("CS", "CS"),
    CENTIMETER("CM", "CMT"),
    COIL("CX", "COI"),
    CONTAINER("CH", "CH"),
    CRADLE("CR", "CRD"),
    CUBIC_INCHES("CI", "INQ"),
    DAY("DA", "DAY"),
    DOZEN("DZ", "DZN"),
    EACH("EA", "EA"),
    FOOT("FT", "FT"),
    GRAM("GR", "GRM"),
    GROSS("GS", "GRO"),
    HOURS("HR", "HUR"),
    HUNDRED("HU", "CEN"),
    INCH("IN", "INH"),
    KILOGRAM("KG", "KGM"),
    KIT("KT", "KT"),
    LINEAL_FEET("LF", "FOT"),
    LINEAL_YARDS("LY", "YRD"),
    LITER("LT", "LTR"),
    LOT("LO", "LO"),
    METER("MR", "MTR"),
    MICRO_GRAM("MC", "MUG"),
    MICROCURIE("M5", "M5"),
    MILLIMETER("MM", "MMT"),
    MILLION_UNITS("UM", "MIU"),
    MONTH("MO", "MON"),
    OUNCE("OZ", "ONZ"),
    PACKAGE("PK", "PAC"),
    PAD("PD", "PD"),
    PAIL("PA", "PL"),
    PAIR("PR", "PR"),
    PIECES("PC", "H87"),
    PINT_US_LIQUID("PT", "PTL"),
    QUART("QT", "QT"),
    REAM("RM", "RM"),
    ROLL("RL", "RO"),
    SET("ST", "SET"),
    SHEET("SH", "ST"),
    SPOOL("SO", "SPL"),
    SQUARE("SQ", "SQ"),
    SQUARE_FOOT("SF", "FTK"),
    SQUARE_YARD("SY", "YDK"),
    THOUSAND("TH", "MIL"),
    TON("TN", "TNE"),
    TRAY("TY", "PU"),
    TUBE("TB", "TU"),
    US_POUND("LB", "LBR"),
    WEEK("WK", "WEE"),
    YEAR("YR", "ANN");

    public final String kfsUnitOfMeasureCode;
    public final String workdayUnitOfMeasureRefId;

    private CemiUnitOfMeasure(final String kfsUnitOfMeasureCode, final String workdayUnitOfMeasureRefId) {
        this.kfsUnitOfMeasureCode = kfsUnitOfMeasureCode;
        this.workdayUnitOfMeasureRefId = workdayUnitOfMeasureRefId;
    }

    public static Map<String, String> toUnitOfMeasureMappings() {
        return Arrays.stream(CemiUnitOfMeasure.values())
                .collect(Collectors.toUnmodifiableMap(
                        uom -> uom.kfsUnitOfMeasureCode, uom -> uom.workdayUnitOfMeasureRefId));
    }

}
