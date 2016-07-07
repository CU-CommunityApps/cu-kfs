package edu.cornell.kfs.fp.service.impl;

import static org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX;

import java.sql.Date;
import java.util.Calendar;
import java.util.TreeMap;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.gl.service.ScheduledAccountingLineService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class ScheduledAccountingLineServiceImpl implements ScheduledAccountingLineService {
	
	public TreeMap<Date, KualiDecimal> generateDatesAndAmounts(ScheduledSourceAccountingLine scheduledAccountingLine, int rowId) {
		TreeMap<Date, KualiDecimal> datesAndAmounts = new TreeMap<Date, KualiDecimal>();
		Date endDate = generateEndDate(scheduledAccountingLine);
		KualiDecimal totalAmount = calculateTotalAmount(scheduledAccountingLine.getAmount());
		
		Date currentStartDate = (Date) scheduledAccountingLine.getStartDate().clone();
		int iterationCount = 0;
		
		while (currentStartDate.before(endDate) && totalAmount.isGreaterThan(KualiDecimal.ZERO)) {
			Calendar calendarCalculator = Calendar.getInstance();
			calendarCalculator.setTime(scheduledAccountingLine.getStartDate());
			
			calendarCalculator = incrementDateByValue(scheduledAccountingLine, calendarCalculator, iterationCount);
			
			Date entryDate = new Date(calendarCalculator.getTimeInMillis());
			addToDateAndAmount(datesAndAmounts, scheduledAccountingLine, entryDate, totalAmount, endDate);
			
			currentStartDate = new Date(calendarCalculator.getTimeInMillis());
			totalAmount = totalAmount.subtract(scheduledAccountingLine.getPartialAmount());
			
			iterationCount++;
		}
		
		boolean showError = Integer.parseInt(scheduledAccountingLine.getPartialTransactionCount()) != iterationCount;
		showErrorMessage(showError, rowId);
		
		return datesAndAmounts;
	}
	
	private void addToDateAndAmount(TreeMap<Date, KualiDecimal> datesAndAmounts, ScheduledSourceAccountingLine scheduledAccountingLine, 
			Date entryDate, KualiDecimal totalAmountLeft, Date endDate) {
		if (!entryDate.after(endDate)) {
			if (scheduledAccountingLine.getPartialAmount().isGreaterThan(totalAmountLeft)) {
				datesAndAmounts.put(entryDate, totalAmountLeft);
			} else {
				datesAndAmounts.put(entryDate, scheduledAccountingLine.getPartialAmount());
			}
		}
	}
	
	private void showErrorMessage(boolean showError, int rowId) {
		if (showError) {
			GlobalVariables.getMessageMap().putError(
					DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + rowId + "].partialTransactionCount",
					CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_WRONG_COUNT);
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
	
}
