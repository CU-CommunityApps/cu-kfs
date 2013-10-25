package edu.cornell.kfs.fp.businessobject;
import static org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.rice.kns.util.AbstractKualiDecimal;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSKeyConstants;


public class PreEncumbranceAccountingLineUtil {
        public static void copyFrom(PreEncumbranceSourceAccountingLine toLine, AccountingLine other) {
                if (PreEncumbranceSourceAccountingLine.class.isAssignableFrom(other.getClass())) {
                        PreEncumbranceSourceAccountingLine fromLine = (PreEncumbranceSourceAccountingLine) other;
                        if (toLine != fromLine) {
                                toLine.setPartialAmount(fromLine.getPartialAmount());
                                toLine.setPartialTransactionCount(fromLine.getPartialTransactionCount());
                                toLine.setStartDate(fromLine.getStartDate());
                                toLine.setEndDate(fromLine.getEndDate());
                                toLine.setAutoDisEncumberType(fromLine.getAutoDisEncumberType());
                                toLine.setAmount(fromLine.getAmount());
                        }
                }
        }
        
        public static TreeMap<Date, KualiDecimal> generateDatesAndAmounts(String autoDisEncumberType, Date startDate, Date endDate, int count, KualiDecimal totalAmount, KualiDecimal partialAmount, Integer rowId) {
                TreeMap<Date, KualiDecimal> datesAndAmounts = new TreeMap<Date,KualiDecimal>();
                
                endDate = generateEndDate(startDate,count,autoDisEncumberType);
                
                if (totalAmount.isNegative()) {
                        // flip the total amount to generate the entries
                        totalAmount = totalAmount.abs();
                }
                Date currentReversalDate = (Date) startDate.clone();
                int i = 0;
                while (!currentReversalDate.after(endDate) && totalAmount.isGreaterThan(KualiDecimal.ZERO)) {
//                        int i=0;
                        if (autoDisEncumberType.equals("biWeekly")) {
                                if (partialAmount.isGreaterThan(totalAmount)) {
                                        datesAndAmounts.put(currentReversalDate, totalAmount);
                                } else {
                                        datesAndAmounts.put(currentReversalDate, partialAmount);
                                }
                                Calendar cRD = Calendar.getInstance();
                                cRD.setTime(currentReversalDate);
                                cRD.add(Calendar.DATE, 14);
                                currentReversalDate = new Date(cRD.getTimeInMillis());
                                totalAmount = totalAmount.subtract(partialAmount);
                                i++;
                        }
                        if (autoDisEncumberType.equals("monthly")) {
                                Calendar cRD = Calendar.getInstance();                          
                                cRD.setTime(startDate);
                                cRD = incrementMonthByVal(cRD, i);
                                Date rDate = new Date(cRD.getTimeInMillis());
                                if (partialAmount.isGreaterThan(totalAmount)) {
                                        datesAndAmounts.put(rDate, totalAmount);
                                } else {
                                		datesAndAmounts.put(rDate, partialAmount);	
                                }
                                currentReversalDate = new Date(cRD.getTimeInMillis());
                                totalAmount = totalAmount.subtract(partialAmount);
                                if (currentReversalDate.after(endDate)) {
                                	datesAndAmounts.remove(rDate);
                                }
                                i++;
                        }
                        if (autoDisEncumberType.equals("semiMonthly")) {
                                if (partialAmount.isGreaterThan(totalAmount)) {
                                        datesAndAmounts.put(currentReversalDate, totalAmount);
                                } else {
                                        datesAndAmounts.put(currentReversalDate, partialAmount);
                                }
                                currentReversalDate = nextSemiMonthlyDate(currentReversalDate);
                                totalAmount = totalAmount.subtract(partialAmount);  
                                i++;
                        }
                        if (autoDisEncumberType.equals("custom")) {
                        	    if (count == 1) {
                        	    	i = 1;
                        	    }
                                datesAndAmounts.put(currentReversalDate, totalAmount);
                                break;
                        }
                }
               
                if (count != (i)) {
                	GlobalVariables.getMessageMap().putError(DOCUMENT_ERROR_PREFIX + "sourceAccountingLine[" + rowId + "].partialTransactionCount", CUKFSKeyConstants.ERROR_DOCUMENT_PREENCUMBER_WRONG_COUNT);                               
                }
                return datesAndAmounts;
        }
        
    public static Date generateEndDate(Date startDate, int count, String autoDisEncumberType) {
        Date endDate;
        Calendar endDateCal = Calendar.getInstance();
        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTimeInMillis(startDate.getTime());
        if (autoDisEncumberType.equals("biWeekly")) {
                startDateCal.add(Calendar.DAY_OF_YEAR, (count-1)*14);
                endDateCal = (Calendar) startDateCal.clone();
        }
        if (autoDisEncumberType.equals("monthly")) {
                startDateCal.add(Calendar.MONTH, (count-1));
                endDateCal = (Calendar) startDateCal.clone();
        }
        if (autoDisEncumberType.equals("semiMonthly")) {                
                endDate = (Date) startDate.clone();
                for (int i=0;i<count;i++) {
                        endDate = nextSemiMonthlyDate(endDate);
                }
                endDateCal.setTime(endDate);
        }
        if (autoDisEncumberType.equals("custom")){
                endDateCal = startDateCal;
        }
        endDate = new Date(endDateCal.getTimeInMillis());
        return endDate;
    }
    private static Date nextSemiMonthlyDate(Date theDate) {
        Calendar last = Calendar.getInstance();
        Calendar fifteenth = Calendar.getInstance();
        Calendar theDateCal = Calendar.getInstance(); 
        theDateCal.setTime(theDate);
        int lastday = theDateCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        fifteenth.setTime(theDate);
        last.setTime(theDate);
        last.set(Calendar.DAY_OF_MONTH, lastday);
        fifteenth.set(Calendar.DAY_OF_MONTH, 15);
        if (theDateCal.before(fifteenth) || theDateCal.equals(fifteenth) ) {
                theDateCal = last;
        } else
        if (theDateCal.after(fifteenth) || theDateCal.equals(last)) {
                theDateCal = fifteenth;
                theDateCal.roll(Calendar.MONTH, true);
                if (theDateCal.before(last)){
                    theDateCal.roll(Calendar.YEAR, true);
                }
        }
        return new Date(theDateCal.getTimeInMillis());
    }
    
        private static Calendar incrementMonthByVal(Calendar theCal, int val) {
                // this increments by the val 
                // for dates of the form MM/[1-28]/YYYY this simply results in MM+1/[1-28]/YYYY
                // for dates of the form MM/[29-31]/YYYY this will increment the month, set the day 
                // to the closest available value to the original without overflowing into the next month
                Calendar incrementedCalendar = (Calendar) theCal.clone();
                int currentDay = incrementedCalendar.get(Calendar.DAY_OF_MONTH);
                incrementedCalendar.add(Calendar.MONTH, val);
                incrementedCalendar.set(Calendar.DAY_OF_MONTH, currentDay);
                int newDay = incrementedCalendar.get(Calendar.DAY_OF_MONTH);
                if (newDay<currentDay) { //we overflowed the "DAY_OF_MONTH" field, i.e. tried to set the date to February 31st.
                        incrementedCalendar.roll(Calendar.DAY_OF_MONTH, false);
                        int rolledBackDay = incrementedCalendar.get(Calendar.DAY_OF_MONTH);
                        while (rolledBackDay<newDay) {
                                incrementedCalendar.roll(Calendar.DAY_OF_MONTH, false);
                                rolledBackDay = incrementedCalendar.get(Calendar.DAY_OF_MONTH);
                        }
                        incrementedCalendar.roll(Calendar.MONTH, false);
                }
                return incrementedCalendar;
        }
}