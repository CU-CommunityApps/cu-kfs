package edu.cornell.kfs.concur;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurUtilsTest {
    public static final String GOOD_EXPENSE_URI = "https://www.concursolutions.com/api/expense/expensereport/v1.1/reportfulldetails/123456578";
    public static final String BAD_EXPENSE_URI = "https://www.concursolutions.com/api/someEndPoint";
    
    public static final String GOOD_TRAVEL_REQUEST_URI = "https://www.concursolutions.com/api/travelrequest/v1.0/requests/1234567678";
    public static final String BAD_TRAVEL_REQUEST_URI = "https://www.concursolutions.com/api/someEndPoint";
    
    public static final String GOOD_CONCUR_FORMAT_CODE_AND_DESCRIPTION= "(1234567) some account description";
    public static final String KFS_FORMAT_ACCOUNT_NUMBER = "1234567";   
    public static final String GOOD_CONCUR_FORMAT_CODE = "123";
    
    public static final String STRING_WITH_OPEN_CLOSE_PARENTHESIS = "(1234567) some account description";
    public static final String STRING_WITHOUT_OPEN_CLOSE_PARENTHESIS = "1234567 some account description";
    public static final String CHART = "IT";
    public static final String ACCOUNT_NUMBER = "1234567";
    public static final String STRING_FORMATTED_FOR_ERROR_MESSAGE = ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER + CUKFSConstants.COLON + CHART + KFSConstants.COMMA + ACCOUNT_NUMBER;
    
    @Before
    public void setUp() throws Exception {
        
    }
    
    @Test
    public void isGoodExpenseReportURI(){      
        Assert.assertTrue("The URI should have been a valid expense report URI", ConcurUtils.isExpenseReportURI(GOOD_EXPENSE_URI));
    }
    
    @Test
    public void isBadExpenseReportURI(){      
        Assert.assertFalse("The URI should have been an invalid expense report URI", ConcurUtils.isExpenseReportURI(BAD_EXPENSE_URI));
    }
    
    @Test
    public void isEmptyExpenseReportURI(){      
        Assert.assertFalse("An empty URI should have been an invalid expense report URI", ConcurUtils.isExpenseReportURI(StringUtils.EMPTY));
    }
    
    
    @Test
    public void isGoodTravelRequestURI(){      
        Assert.assertTrue("The URI should have been a valid Travel Request URI", ConcurUtils.isTravelRequestURI(GOOD_TRAVEL_REQUEST_URI));
    }
    
    @Test
    public void isBadTravelRequestURI(){      
        Assert.assertFalse("The URI should have been an invalid Travel Request URI", ConcurUtils.isTravelRequestURI(BAD_TRAVEL_REQUEST_URI));
    }
    
    @Test
    public void isEmptyTravelRequestURI(){      
        Assert.assertFalse("An empty URI should have been an invalid TravelRequest URI", ConcurUtils.isTravelRequestURI(StringUtils.EMPTY));
    }
    
    @Test
    public void extractAccountNumberFromConcurValue(){      
        Assert.assertEquals(KFS_FORMAT_ACCOUNT_NUMBER, ConcurUtils.extractCodeFromCodeAndDescriptionValue(GOOD_CONCUR_FORMAT_CODE_AND_DESCRIPTION));
    }
    
    @Test
    public void extracCodeFromEmptyString(){      
        Assert.assertEquals(StringUtils.EMPTY, ConcurUtils.extractCodeFromCodeAndDescriptionValue(StringUtils.EMPTY));
    }
    
    @Test
    public void stringContainOpenCloseParenthesis(){      
        Assert.assertTrue("String does contain open/close parenthesis", ConcurUtils.stringMatchesCodeAndDescriptionPattern(STRING_WITH_OPEN_CLOSE_PARENTHESIS));
    }
    
    @Test
    public void stringDoesNotContainOpenCloseParenthesis(){      
        Assert.assertFalse("String does not contain open/close parenthesis", ConcurUtils.stringMatchesCodeAndDescriptionPattern(STRING_WITHOUT_OPEN_CLOSE_PARENTHESIS));
    }
    
    @Test
    public void emptyStringDoesNotContainOpenCloseParenthesis(){      
        Assert.assertFalse("Empty string does not contain open/close parenthesis", ConcurUtils.stringMatchesCodeAndDescriptionPattern(StringUtils.EMPTY));
    }
    
    @Test
    public void formatStringForErrorMessage(){
        Assert.assertEquals(STRING_FORMATTED_FOR_ERROR_MESSAGE, ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, CHART, ACCOUNT_NUMBER));
    }  

}
