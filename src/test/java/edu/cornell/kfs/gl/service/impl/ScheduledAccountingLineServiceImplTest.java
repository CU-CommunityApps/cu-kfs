package edu.cornell.kfs.gl.service.impl;

import static org.junit.Assert.*;
import static org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX;

import java.sql.Date;
import java.util.Calendar;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes;
import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.fp.businessobject.fixture.ScheduledSourceAccountingLineFixture;
import edu.cornell.kfs.fp.service.impl.ScheduledAccountingLineServiceImpl;
import edu.cornell.kfs.gl.service.impl.fixture.ScheduledAccountingLineServiceImplEndDateFixture;

public class ScheduledAccountingLineServiceImplTest {

    ScheduledAccountingLineServiceImpl scheduledAccountingLineService;
    Date startingPointDate;
    Calendar startingPointCal;
    ScheduledSourceAccountingLine accountingLine;

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(ScheduledAccountingLineServiceImpl.class.getName(), Level.DEBUG);
        scheduledAccountingLineService = new ScheduledAccountingLineServiceImpl();

        startingPointDate = new Date(Calendar.getInstance().getTimeInMillis());
        startingPointCal = Calendar.getInstance();
        startingPointCal.setTimeInMillis(startingPointDate.getTime());

        accountingLine = new TestableScheduledSourceAccountingLine();
        accountingLine.setStartDate(startingPointDate);
    }

    @AfterEach
    void tearDown() throws Exception {
        scheduledAccountingLineService = null;
        startingPointDate = null;
        startingPointCal = null;
        accountingLine = null;
        GlobalVariables.getMessageMap().clearErrorMessages();
    }

    @ParameterizedTest
    @EnumSource(ScheduledAccountingLineServiceImplEndDateFixture.class)
    void testGenerateEndDates(ScheduledAccountingLineServiceImplEndDateFixture fixture) {
        doTest(fixture.expectedCalendarType, fixture.expectedAmount, fixture.resultType, fixture.resultAmount);
    }

    private void doTest(int expectedCalendarType, int expectedAmount,
            CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes resultType, String resultAmount) {
        Date expectedDate = getExpectedDate(expectedCalendarType, expectedAmount);
        Date results = getDateResult(resultType, resultAmount);

        assertEquals("Day is not what we expected.", expectedDate.getDay(), results.getDay());
        assertEquals("Date is not what we expected.", expectedDate.getDate(), results.getDate());
        assertEquals("Month is not what we expected.", expectedDate.getMonth(), results.getMonth());
        assertEquals("Year is not what we expected.", expectedDate.getYear(), results.getYear());
    }

    private Date getExpectedDate(int dateAddElement, int addAmount) {
        Calendar calcCal = startingPointCal.getInstance();
        calcCal.add(dateAddElement, addAmount);
        Date expectedDate = new Date(calcCal.getTimeInMillis());
        return expectedDate;
    }

    private Date getDateResult(ScheduleTypes scheduleType, String count) {
        accountingLine.setScheduleType(scheduleType.name);
        accountingLine.setPartialTransactionCount(count);
        ;
        Date results = scheduledAccountingLineService.generateEndDate(accountingLine);
        return results;
    }

    @ParameterizedTest
    @EnumSource(ScheduledSourceAccountingLineFixture.class)
    void testGenerateDatesAndAmounts(ScheduledSourceAccountingLineFixture fixture) {
        accountingLine.setChartOfAccountsCode(fixture.chart);
        accountingLine.setAccountNumber(fixture.account);
        accountingLine.setFinancialObjectCode(fixture.objectCode);
        accountingLine.setAmount(new KualiDecimal(fixture.amount));
        accountingLine.setPartialAmount(new KualiDecimal(fixture.partialAmount));
        accountingLine.setPartialTransactionCount(fixture.transactionCount);
        accountingLine.setScheduleType(fixture.scheduleType);

        scheduledAccountingLineService.generateDatesAndAmounts(accountingLine, fixture.rowId);

        assertTrue("There shouldn't be any error messages, basic",
                GlobalVariables.getMessageMap().getErrorCount() == fixture.errorCount);

        if (fixture.errorCount > 0) {
            String errorField = DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + fixture.rowId
                    + "].partialTransactionCount";
            String errorKey = GlobalVariables.getMessageMap().getErrorMessages().get(errorField).get(0).getErrorKey();
            assertEquals(fixture.errorKey, errorKey);
        }
    }

    private class TestableScheduledSourceAccountingLine extends ScheduledSourceAccountingLine {
        private static final long serialVersionUID = -5975895658306173971L;

        @Override
        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }
    }

}
