package edu.cornell.kfs.gl.service;

import java.sql.Date;
import java.util.TreeMap;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;

/**
 * ScheduledAccountingLineService defines functions to aid in creation of Geneeral Ledger Pending Entries for a date range defined on
 * a scheduled accounting line.
 *
 */
public interface ScheduledAccountingLineService {

    /**
     * Returns a map of dates and a transaction amount for that date.  The calculation is based on the start date, schedule type, and number of occurrences  as
     * entered on the accounting line.
     * @param scheduledAccountingLine the accounting line to be processed.
     * @param rowId The rowID in the JSP so that any errors can be linked to the correct row on the web page.
     * @return
     */
    TreeMap<Date, KualiDecimal> generateDatesAndAmounts(ScheduledSourceAccountingLine scheduledAccountingLine, int rowId);

    /**
     * Generates the end date of the re-occurrence schedule as defined on an accounting line.  For example an accounting line with a a start date of 1/1/2016
     * and is scheduled to go for three weeks, the end date would be calculated to 1/15/2016   
     * @param accountingLine
     * @return
     */
    Date generateEndDate(ScheduledSourceAccountingLine accountingLine);

    /**
     * Calculates what the the maximum date is that a new scheduled account line end date may be.
     * It gets the value of the "RECURRING_DV_MAX_FUTURE_DATE" parameter and adds that value of days to the current date to return the max date.
     * @return
     */
    Date getMaximumScheduledAccountingLineEndDate();
}
