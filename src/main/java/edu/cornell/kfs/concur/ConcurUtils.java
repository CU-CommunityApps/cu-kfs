package edu.cornell.kfs.concur;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurUtils {
    private static final Logger LOG = LogManager.getLogger(ConcurUtils.class);

    private static final DateTimeFormatter UTC_DATE_FORMATTER = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS_Z).withLocale(Locale.US).withZoneUTC();

    private static final Pattern URL_PARAMS_PATTERN = Pattern.compile(
            "^[\\w%\\-\\.:]+=[\\w%\\-\\.:]*(\\&[\\w%\\-\\.:]+=[\\w%\\-\\.:]+)*$");

    private static final String CODE_PATTERN = "\\((.*?)\\)";
    private static final String CODE_AND_DESCRIPTION_PATTERN = CODE_PATTERN + "(.*?)";

    public static boolean isExpenseReportURI(String URI) {
        return StringUtils.isNotBlank(URI) && URI.contains(ConcurConstants.EXPENSE_REPORT_URI_INDICATOR);
    }

    public static boolean isTravelRequestURI(String URI) {
        return StringUtils.isNotBlank(URI) && URI.contains(ConcurConstants.TRAVEL_REQUEST_URI_INDICATOR);
    }

    public static String extractCodeFromCodeAndDescriptionValue(String codeAndDescriptionValue) {
        String result = StringUtils.EMPTY;
        LOG.info("extractCodeFromCodeAndDescriptionValue(): Extract code from: " + codeAndDescriptionValue);
        
        if (StringUtils.isNotBlank(codeAndDescriptionValue) && stringMatchesCodeAndDescriptionPattern(codeAndDescriptionValue)) {
            Pattern regexCode = Pattern.compile(CODE_PATTERN);
            Matcher regexMatcherCode = regexCode.matcher(codeAndDescriptionValue);
            if (regexMatcherCode.find()) {
                result = regexMatcherCode.group(1);
            }
        }
        
        LOG.info("extractCodeFromCodeAndDescriptionValue(): Result value: " + result);
        return result;
    }
    
    public static boolean stringMatchesCodeAndDescriptionPattern(String input) {
        boolean matches = false;
        if (StringUtils.isNotBlank(input)) {
            Pattern regexCodeAndDescription = Pattern.compile(CODE_AND_DESCRIPTION_PATTERN);
            Matcher regexMatcherCodeAndDescription = regexCodeAndDescription.matcher(input);
            matches = regexMatcherCodeAndDescription.matches();
        }
        return matches;
    }
    
    public static String formatStringForErrorMessage(String label, String... values) {
        String formattedString = StringUtils.EMPTY;
        if (values != null && values.length > 0) {
            formattedString += label + CUKFSConstants.COLON;
            for (String value : values) {
                formattedString += value + KFSConstants.COMMA;
            }
            formattedString = StringUtils.removeEnd(formattedString, KFSConstants.COMMA);
        }

        return formattedString;
    }
    
    public static boolean isConcurReportStatusAwaitingExternalValidation(String statusCode){
        return ConcurConstants.EXPENSE_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE.equalsIgnoreCase(statusCode) || ConcurConstants.REQUEST_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE.equalsIgnoreCase(statusCode);
    }

    public static String base64Encode(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("value cannot be blank");
        }
        byte[] valueAsBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] encodedValueAsBytes = Base64.getEncoder().encode(valueAsBytes);
        String encodedValue = new String(encodedValueAsBytes, StandardCharsets.UTF_8);
        return encodedValue;
    }

    public static String formatAsUTCDate(Date value) {
        Objects.requireNonNull(value, "value cannot be null");
        return UTC_DATE_FORMATTER.print(value.getTime());
    }

    public static DateTime parseUTCDateToDateTime(String value) {
        long millisecondValue = parseUTCDateToMilliseconds(value);
        return new DateTime(millisecondValue);
    }

    public static long parseUTCDateToMilliseconds(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("value cannot be blank");
        }
        return UTC_DATE_FORMATTER.parseMillis(value);
    }

    public static boolean validateFormatAndPrefixOfParameterizedUrl(String url, String expectedUrlPrefix) {
        if (StringUtils.isBlank(url)) {
            LOG.error("validateFormatAndPrefixOfParameterizedUrl, URL was blank");
            return false;
        } else if (!StringUtils.contains(url, KFSConstants.QUESTION_MARK)) {
            LOG.error("validateFormatAndPrefixOfParameterizedUrl, URL was not parameterized");
            return false;
        }
        String actualUrlPrefix = StringUtils.substringBefore(url, KFSConstants.QUESTION_MARK);
        if (!StringUtils.equalsIgnoreCase(expectedUrlPrefix, actualUrlPrefix)) {
            LOG.error("validateFormatAndPrefixOfParameterizedUrl, URL had the wrong prefix; expected: "
                    + expectedUrlPrefix + " , actual: " + actualUrlPrefix);
            return false;
        }
        String urlParameters = StringUtils.substringAfter(url, KFSConstants.QUESTION_MARK);
        if (!URL_PARAMS_PATTERN.matcher(urlParameters).matches()) {
            LOG.error("validateFormatAndPrefixOfParameterizedUrl, URL had malformed parameters");
            return false;
        }
        return true;
    }

}
