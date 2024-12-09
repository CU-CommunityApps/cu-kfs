package edu.cornell.kfs.concur;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationType;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;
import edu.cornell.kfs.concur.services.ConcurAccountValidationTestConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurUtilsTest {
    public static final String GOOD_EXPENSE_URI = "https://www.concursolutions.com/api/expense/expensereport/v1.1/reportfulldetails/123456578";
    public static final String SOME_CONCUR_ENDPOINT_URI = "https://www.concursolutions.com/api/someEndPoint";

    public static final String GOOD_TRAVEL_REQUEST_URI = "https://www.concursolutions.com/api/travelrequest/v1.0/requests/1234567678";

    public static final String GOOD_CONCUR_FORMAT_CODE_AND_DESCRIPTION = "(1234567) some account description";
    public static final String KFS_FORMAT_ACCOUNT_NUMBER = "1234567";
    public static final String GOOD_CONCUR_FORMAT_CODE = "123";

    public static final String VALUE_IN_CODE_AND_DESCRIPTION_FORMAT = "(1234567) some account description";
    public static final String VALUE_NOT_IN_CODE_AND_DESCRIPTION_FORMAT = "1234567 some account description";
    public static final String CHART = "IT";
    public static final String ACCOUNT_NUMBER = "1234567";
    public static final String STRING_FORMATTED_FOR_ERROR_MESSAGE = ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER
            + CUKFSConstants.COLON + CHART + KFSConstants.COMMA + ACCOUNT_NUMBER;

    @ParameterizedTest
    @MethodSource("testIsExpenseReportURIParams")
    public void testIsExpenseReportURI(String uri, boolean validationExpectation) {
        Assert.assertEquals(validationExpectation, ConcurUtils.isExpenseReportURI(uri));

    }

    private static Stream<Arguments> testIsExpenseReportURIParams() {
        return Stream.of(
                Arguments.of(GOOD_EXPENSE_URI, true), 
                Arguments.of(SOME_CONCUR_ENDPOINT_URI, false),
                Arguments.of(StringUtils.EMPTY, false), 
                Arguments.of(null, false));
    }

    @ParameterizedTest
    @MethodSource("testIsTravelRequestURIParams")
    public void testIsTravelRequestURI(String uri, boolean validationExpectation) {
        Assert.assertEquals(validationExpectation, ConcurUtils.isTravelRequestURI(uri));

    }

    private static Stream<Arguments> testIsTravelRequestURIParams() {
        return Stream.of(
                Arguments.of(GOOD_TRAVEL_REQUEST_URI, true), 
                Arguments.of(SOME_CONCUR_ENDPOINT_URI, false),
                Arguments.of(StringUtils.EMPTY, false), 
                Arguments.of(null, false));
    }

    @ParameterizedTest
    @MethodSource("testExtractCodeFromCodeAndDescriptionValueParams")
    public void testExtractCodeFromCodeAndDescriptionValue(String codeAndDescription, String expectedValue) {
        Assert.assertEquals(expectedValue, ConcurUtils.extractCodeFromCodeAndDescriptionValue(codeAndDescription));
    }

    private static Stream<Arguments> testExtractCodeFromCodeAndDescriptionValueParams() {
        return Stream.of(
                Arguments.of(GOOD_CONCUR_FORMAT_CODE_AND_DESCRIPTION, KFS_FORMAT_ACCOUNT_NUMBER),
                Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY), 
                Arguments.of(null, StringUtils.EMPTY));
    }

    @ParameterizedTest
    @MethodSource("testStringMatchesCodeAndDescriptionPatternParams")
    public void testStringMatchesCodeAndDescriptionPattern(String patternToMatch, boolean expectedMatchStatus) {
        Assert.assertEquals(expectedMatchStatus, ConcurUtils.stringMatchesCodeAndDescriptionPattern(patternToMatch));

    }

    private static Stream<Arguments> testStringMatchesCodeAndDescriptionPatternParams() {
        return Stream.of(
                Arguments.of(VALUE_IN_CODE_AND_DESCRIPTION_FORMAT, true),
                Arguments.of(VALUE_NOT_IN_CODE_AND_DESCRIPTION_FORMAT, false), 
                Arguments.of(StringUtils.EMPTY, false));
    }

    @Test
    public void formatStringForErrorMessage() {
        Assert.assertEquals(STRING_FORMATTED_FOR_ERROR_MESSAGE, ConcurUtils.formatStringForErrorMessage(
                ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, CHART, ACCOUNT_NUMBER));
    }
    
    @ParameterizedTest
    @MethodSource("testVuildValidationErrorMessageForWorkflowActionParams")
    public void testVuildValidationErrorMessageForWorkflowAction(List<String> errorMessages) {
        ConcurEventNotificationResponse dto = buildTestingConcurEventNotificationResponse(ConcurEventNotificationType.ExpenseReport, ConcurEventNotificationStatus.invalidAccounts);
        dto.setMessages(errorMessages);        
        
        String actualResults = ConcurUtils.buildValidationErrorMessageForWorkflowAction(dto);
        StringBuilder expectedResults = new StringBuilder(ConcurConstants.ERROR_MESSAGE_HEADER);
        boolean avoidNewLine = true;
        for (String message : errorMessages) {
            if (avoidNewLine) {
                avoidNewLine = false;
            } else {
                expectedResults.append(KFSConstants.NEWLINE);
            }
            expectedResults.append(message);
        }
        Assert.assertEquals("the error message wasn't what was expected", expectedResults.toString(), actualResults);
                
    }
    
    private static Stream<Arguments> testVuildValidationErrorMessageForWorkflowActionParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.buildMessages("one error line", "another error line")),
                Arguments.of(ConcurAccountValidationTestConstants.buildMessages("error line"))
        );
    }

    private ConcurEventNotificationResponse buildTestingConcurEventNotificationResponse(ConcurEventNotificationType type, ConcurEventNotificationStatus status) {
        String reportNumber = "rpt101";
        String reportName = "john doe trip";
        String reportStatus = "status";
        String travelerName = "John Doe";
        String travelerEmail = "jd321@cornell.edu";
        ConcurEventNotificationResponse dto = new ConcurEventNotificationResponse(type, status, reportNumber, reportName, reportStatus, travelerName, travelerEmail);
        return dto;
    }

}
