package edu.cornell.kfs.concur;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurUtils {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurUtils.class);
    
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

}
