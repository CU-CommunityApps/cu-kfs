package edu.cornell.kfs.fp.service.impl;

import static org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.gl.service.ScheduledAccountingLineService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class ScheduledAccountingLineServiceImpl implements ScheduledAccountingLineService, Serializable {

    private static final long serialVersionUID = -5700446522442741269L;
    private transient ParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(ScheduledAccountingLineServiceImpl.class);

    public TreeMap<Date, KualiDecimal> generateDatesAndAmounts(ScheduledSourceAccountingLine scheduledAccountingLine, int rowId) {
        TreeMap<Date, KualiDecimal> datesAndAmounts = new TreeMap<Date, KualiDecimal>();
        Date endDate = generateEndDate(scheduledAccountingLine);
        scheduledAccountingLine.setEndDate(endDate);
        KualiDecimal totalAmount = calculateTotalAmount(scheduledAccountingLine.getAmount());

        Date currentStartDate = (Date) scheduledAccountingLine.getStartDate().clone();
        int iterationCount = 0;

        while (isSameDayOrEarlier(endDate, currentStartDate) && totalAmount.isGreaterThan(KualiDecimal.ZERO)) {
            Calendar calendarCalculator = Calendar.getInstance();
            calendarCalculator.setTime(scheduledAccountingLine.getStartDate());

            calendarCalculator = incrementDateByValue(scheduledAccountingLine, calendarCalculator, iterationCount);

            Date entryDate = new Date(calendarCalculator.getTimeInMillis());
            addToDateAndAmount(datesAndAmounts, scheduledAccountingLine, entryDate, totalAmount, endDate);

            currentStartDate = new Date(calendarCalculator.getTimeInMillis());
            totalAmount = totalAmount.subtract(scheduledAccountingLine.getPartialAmount());

            iterationCount++;
        }

        determineAccountingLineErrors(scheduledAccountingLine, rowId, iterationCount);

        return datesAndAmounts;
    }

    private boolean isSameDayOrEarlier(Date endingDate, Date startingDate) {
        return startingDate.before(endingDate) || KfsDateUtils.isSameDay(startingDate, endingDate);
    }

    private void determineAccountingLineErrors(ScheduledSourceAccountingLine scheduledAccountingLine, int rowId, int iterationCount) {
        String errorField = DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + rowId + "].partialTransactionCount";
        if (LOG.isDebugEnabled()) {
            LOG.debug("determineAccountingLineErrors, scheduledAccountingLine.getPartialTransactionCount() : " + scheduledAccountingLine.getPartialTransactionCount()
                + "  iterationCount: " + iterationCount);
        }
        if (isAccountingLineTotalMoreThanRecurranceTotal(scheduledAccountingLine)) {
            LOG.debug("determineAccountingLineErrors, sum less than transaction");
            GlobalVariables.getMessageMap().putError(errorField, CUKFSKeyConstants.ERROR_RCDV_RECURRENCE_SUM_LESS_THAN_TANSACTION);
        } else if (Integer.parseInt(scheduledAccountingLine.getPartialTransactionCount()) > iterationCount) {
            LOG.debug("determineAccountingLineErrors, sum less than transaction");
            GlobalVariables.getMessageMap().putError(errorField, CUKFSKeyConstants.ERROR_RCDV_TOO_MANY_RECURRENCES);
        }
    }

    private boolean isAccountingLineTotalMoreThanRecurranceTotal(ScheduledSourceAccountingLine scheduledAccountingLine) {
        KualiDecimal totalAmount = calculateTotalAmount(scheduledAccountingLine.getAmount());
        double recurranceTotal = scheduledAccountingLine.getPartialAmount().doubleValue() * Integer.parseInt(scheduledAccountingLine.getPartialTransactionCount());
        KualiDecimal kualiRecurranceTotal = new KualiDecimal(recurranceTotal);
        
        boolean isAccountingLineTotalMoreThanRecurranceTotal = totalAmount.isGreaterThan(kualiRecurranceTotal);
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAccountingLineTotalMoreThanRecurranceTotal, totalAmount = " + totalAmount + "  recurranceTotal: " + recurranceTotal
                + "  scheduledAccountingLine.getPartialTransactionCount(): " + scheduledAccountingLine.getPartialTransactionCount()
                + "  isAccountingLineTotalMoreThanRecurranceTotal: " + (isAccountingLineTotalMoreThanRecurranceTotal));
        }
        return isAccountingLineTotalMoreThanRecurranceTotal;
    }

    private void addToDateAndAmount(TreeMap<Date, KualiDecimal> datesAndAmounts, ScheduledSourceAccountingLine scheduledAccountingLine,
            Date entryDate, KualiDecimal totalAmountLeft, Date endDate) {
        if (isSameDayOrEarlier(endDate, entryDate)) {
            if (scheduledAccountingLine.getPartialAmount().isGreaterThan(totalAmountLeft)) {
                datesAndAmounts.put(entryDate, totalAmountLeft);
            } else {
                datesAndAmounts.put(entryDate, scheduledAccountingLine.getPartialAmount());
            }
        }
    }

    private KualiDecimal calculateTotalAmount(KualiDecimal totalAmount) {
        if (totalAmount.isNegative()) {
            // flip the total amount to generate the entries
            totalAmount = totalAmount.abs();
        }
        return totalAmount;
    }

    private Calendar incrementDateByValue(ScheduledSourceAccountingLine accountingLine, Calendar theCal, int iterationCount) {
        Calendar incrementedCalendar = (Calendar) theCal.clone();
        int amountToAdd = iterationCount * accountingLine.getScheduleTypeEnum().calendarIncrementorMutliplier;
        incrementedCalendar.add(accountingLine.getScheduleTypeEnum().calendarIncrementorType, amountToAdd);
        return incrementedCalendar;
    }


    public Date generateEndDate(ScheduledSourceAccountingLine accountingLine) {
        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTimeInMillis(accountingLine.getStartDate().getTime());
        int count = Integer.parseInt(accountingLine.getPartialTransactionCount());
        int addAmount = (count-1) * accountingLine.getScheduleTypeEnum().calendarIncrementorMutliplier;
        startDateCal.add(accountingLine.getScheduleTypeEnum().calendarIncrementorType, addAmount);
        return new Date(((Calendar) startDateCal.clone()).getTimeInMillis());
    }

    @Override
    public Date getMaximumScheduledAccountingLineEndDate() {
        String numberofDaysString = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_COMPONENT_NAME,
                CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_MAX_FUTURE_DATE);
        int numberOfDays = Integer.parseInt(numberofDaysString);
        Calendar calendarCaluclator = Calendar.getInstance();
        calendarCaluclator.add(Calendar.DATE, numberOfDays);
        return new Date(calendarCaluclator.getTimeInMillis());
    }

    public ParameterService getParameterService() {
        if(parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
