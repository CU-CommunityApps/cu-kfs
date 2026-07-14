package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.sql.Date;
import java.text.MessageFormat;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.CemiAwardScheduleConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleFileAwardScheduleTabRowBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@SuppressWarnings("deprecation")
public class CemiAwardScheduleFileAwardScheduleTabRowBoFactory {
    
    private DateTimeService dateTimeService;
    
    private Award award;
    private AwardExtendedAttribute awardExtendedAttribute;

    private boolean maskSensitiveData;
    
    //moved from CemiAwardScheduleBo during restructuring
//    private Long extractTableUniqueRowId;
//    private String jobRunDateAsString;
//    private String proposalNumberUsedForDataRow; 
    
     //moved from CemiAwardScheduleBo during restructuring
//    public CemiAwardScheduleBo (CemiAwardSchedule cemiAwardScheduleDataRow, String proposalNumberForScheduleRow,
//            LocalDateTime jobRunDate, CemiAwardScheduleBoSequence awardScheduleTabTableSequence) {
//        
//        // These values are to make the row that would appear in an extract spreadsheet seachable as well as
//        // identifiable by the actual KFS data object keys used to create that row and date-time of the extract file.
        this.extractTableUniqueRowId = awardScheduleTabTableSequence.getLongValue();
        this.jobRunDateAsString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        this.proposalNumberUsedForDataRow = proposalNumberForScheduleRow;
//        
//        // These table data values should be the same as what would be in the extract file tabbed sheet columns.
//        this.spreadsheetKey = cemiAwardScheduleDataRow.getSpreadsheetKey();
//        this.addOnly = cemiAwardScheduleDataRow.getAddOnly();
//        this.awardSchedule = cemiAwardScheduleDataRow .getAwardSchedule();
//        this.awardScheduleReferenceId = cemiAwardScheduleDataRow.getAwardScheduleReferenceId();
//        this.awardScheduleName = cemiAwardScheduleDataRow.getAwardScheduleName();
//        this.awardPostingIntervalGroup = cemiAwardScheduleDataRow.getAwardPostingIntervalGroup();
//        this.awardPeriodDataRowId = cemiAwardScheduleDataRow.getAwardPeriodDataRowId();
//        this.awardPeriodReferenceId = cemiAwardScheduleDataRow.getAwardPeriodReferenceId();
//        this.awardPeriodName = cemiAwardScheduleDataRow.getAwardPeriodName();
//        this.awardPeriodNumber = cemiAwardScheduleDataRow.getAwardPeriodNumber();
//        this.awardIntervalRowId = cemiAwardScheduleDataRow.getAwardIntervalRowId();
//        this.awardPostingInterval = cemiAwardScheduleDataRow.getAwardPostingInterval();
//        this.awardPostingIntervalId = cemiAwardScheduleDataRow.getAwardPostingIntervalId();
//        this.awardPostingIntervalName = cemiAwardScheduleDataRow.getAwardPostingIntervalName();
//        this.awardIntervalStartDate = cemiAwardScheduleDataRow.getAwardIntervalStartDate();
//        this.awardIntervalEndDate = cemiAwardScheduleDataRow.getAwardIntervalEndDate();
//        this.isAwardContractStartDate = cemiAwardScheduleDataRow.getIsAwardContractStartDate();
//        this.isAwardContractEndDate = cemiAwardScheduleDataRow.getIsAwardContractEndDate();
//    }    
    
    
    
    public CemiAwardScheduleFileAwardScheduleTabRowBoFactory (final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute,
            final DateTimeService dateTimeService, final boolean maskSensitiveData) { 
        //Were input pararmeteters
        //final String awardScheduleReferenceId, String spreadsheetKey, String awardIntervalStartDate, String awardIntervalEndDate,
        
        this.award = award;
        this.awardExtendedAttribute = awardExtendedAttribute;
        this.dateTimeService = dateTimeService;
        this.maskSensitiveData = maskSensitiveData;
    }
     
    public CemiAwardScheduleFileAwardScheduleTabRowBo createCemiAwardScheduleFileAwardScheduleTabRowBo() {
        Validate.validState(award != null, "An Award was not defined.");
        Validate.validState(awardExtendedAttribute != null, "An AwardExtendedAttribute was not defined.");
        
        final CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabDataRow = new CemiAwardScheduleFileAwardScheduleTabRowBo();
        
        final String rowSpreadsheetKey = buildSpreadsheetKey(award.getProposalNumber());
        final String rowAwardScheduleReferenceId = buildAwardScheduleReferenceId(award.getProposalNumber());
        final String rowAwardScheduleName = determineAwardScheduleName(award.getAwardProjectTitle());
        final String rowAwardPeriodReferenceId = buildAwardPeriodReferenceId(award.getProposalNumber());
        final String rowAwardPostingIntervalId = buildAwardPostingIntervalId(award.getProposalNumber());
        final String rowAwardIntervalStartDate = determineFormattedDate(awardExtendedAttribute.getBudgetBeginningDate());
        final String rowAwardIntervalEndDate = determineFormattedDate(awardExtendedAttribute.getBudgetEndingDate());
        
        awardScheduleTabDataRow.setSpreadsheetKey(rowSpreadsheetKey);
        awardScheduleTabDataRow.setAddOnly(CemiBaseConstants.YES);
        awardScheduleTabDataRow.setAwardSchedule(CemiBaseConstants.EMPTY_STRING);
        awardScheduleTabDataRow.setAwardScheduleReferenceId(rowAwardScheduleReferenceId);
        awardScheduleTabDataRow.setAwardScheduleName(rowAwardScheduleName);
        awardScheduleTabDataRow.setAwardPostingIntervalGroup(CemiAwardScheduleConstants.BUDGET_PERIOD);
        awardScheduleTabDataRow.setAwardPeriodDataRowId(CemiAwardScheduleConstants.NUMERIC_ONE);
        awardScheduleTabDataRow.setAwardPeriodReferenceId(rowAwardPeriodReferenceId);
        awardScheduleTabDataRow.setAwardPeriodName(CemiAwardScheduleConstants.CINV_PERIOD);
        awardScheduleTabDataRow.setAwardPeriodNumber(CemiAwardScheduleConstants.NUMERIC_ONE);
        awardScheduleTabDataRow.setAwardIntervalRowId(CemiAwardScheduleConstants.NUMERIC_ONE);
        awardScheduleTabDataRow.setAwardPostingInterval(CemiBaseConstants.EMPTY_STRING);
        awardScheduleTabDataRow.setAwardPostingIntervalId(rowAwardPostingIntervalId);
        awardScheduleTabDataRow.setAwardPostingIntervalName(CemiAwardScheduleConstants.AWARD_PERIOD);
        awardScheduleTabDataRow.setAwardIntervalStartDate(rowAwardIntervalStartDate);
        awardScheduleTabDataRow.setAwardIntervalEndDate(rowAwardIntervalEndDate);
        awardScheduleTabDataRow.setIsAwardContractStartDate(CemiBaseConstants.YES);
        awardScheduleTabDataRow.setIsAwardContractEndDate(CemiBaseConstants.YES);
        
        return awardScheduleTabDataRow;
    }
    
    private static String buildSpreadsheetKey(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardScheduleConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
    }
    
    private static String buildAwardPeriodReferenceId(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_PERIOD_REFERENCE_ID_FORMAT, awardProposalNumber);
    }
    
    private static String buildAwardPostingIntervalId(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_POSTING_INTERVAL_ID_FORMAT, awardProposalNumber);
    }
    
    private String determineAwardScheduleName(String awardScheduleName) {
        return StringUtils.isNotBlank(awardScheduleName) ? awardScheduleName : KFSConstants.EMPTY_STRING;
    }
    
    private String determineFormattedDate(Date dateToFormat) {
        return ObjectUtils.isNotNull(dateToFormat)
                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
                        : KFSConstants.EMPTY_STRING;
    }
    
    private static String buildAwardScheduleReferenceId(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_SCHEDULE_REFERENCE_ID_FORMAT, awardProposalNumber);
    }
    
}
