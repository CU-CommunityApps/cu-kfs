package edu.cornell.kfs.module.ar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kuali.kfs.module.ar.ArConstants;

public class CuArConstants {
    public static final String CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION = "ConfirmationForFinalBillIndicatorOnCINV";
    public static final int CINV_DATE_RANGE_EXPECTED_FORMAT_LENGTH = 24;
    public static final int CINV_DATE_RANGE_START_DATE_START_INDEX = 0;
    public static final int CINV_DATE_RANGE_START_DATE_END_INDEX = 10;
    public static final int CINV_DATE_RANGE_END_DATE_START_INDEX = 14;
    public static final String QUESTION_NEWLINE_STRING = "[br]";

    public static final String QUARTER1_LABEL = "Quarter 1";
    public static final String QUARTER2_LABEL = "Quarter 2";
    public static final String QUARTER3_LABEL = "Quarter 3";
    public static final String QUARTER4_LABEL = "Quarter 4";
    public static final String SEMI_ANNUAL_LABEL = "Semi Annually";
    public static final String ANNUAL_LABEL = "Annually";
    public static final String FINAL_LABEL = "Final";

    public static final Map<String, String> REPORTING_PERIOD_LABEL_MAP;

    static {
        Map<String, String> labelsMap = new LinkedHashMap<String, String>();
        labelsMap.put(ArConstants.QUARTER1, QUARTER1_LABEL);
        labelsMap.put(ArConstants.QUARTER2, QUARTER2_LABEL);
        labelsMap.put(ArConstants.QUARTER3, QUARTER3_LABEL);
        labelsMap.put(ArConstants.QUARTER4, QUARTER4_LABEL);
        labelsMap.put(ArConstants.SEMI_ANNUAL, SEMI_ANNUAL_LABEL);
        labelsMap.put(ArConstants.ANNUAL, ANNUAL_LABEL);
        labelsMap.put(ArConstants.FINAL, FINAL_LABEL);
        REPORTING_PERIOD_LABEL_MAP = Collections.unmodifiableMap(labelsMap);
    }

}
