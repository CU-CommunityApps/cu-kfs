package edu.cornell.kfs.cemi.patterntemplate.batch.service.impl;

import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiEXTRACTNAMEFileTABNAMETabRowBo;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

// The factory class deals with converting the legacy data values to the new data value representation.
// Depending upon how the data needs to be placed in the data extraction file, multiple business object factories
// may be required to transform the information, one per business object type.
// Ensure that your classes, attributes, method names, business object factories and business objects have
// meaningful names to make the code self documenting.

@SuppressWarnings("deprecation")
public class CemiEXTRACTNAMEFileTABNAMETabRowBoFactory {
    
//    private Award award;
//    private AwardExtendedAttribute awardExtendedAttribute;
//    private String jobRunDateString;
//    private DateTimeService dateTimeService;
//    private boolean maskSensitiveData = true;   // Initialize default processing to mask in the event 
//                                                // KFS system parameter has not been created. 

    public CemiEXTRACTNAMEFileTABNAMETabRowBoFactory (final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute, final String jobRunDateString, 
            final DateTimeService dateTimeService, final boolean maskSensitiveData) {
//        this.award = award;
//        this.awardExtendedAttribute = awardExtendedAttribute;
//        this.jobRunDateString = jobRunDateString;
//        this.dateTimeService = dateTimeService;
//        this.maskSensitiveData = maskSensitiveData;
    }
     
    public CemiEXTRACTNAMEFileTABNAMETabRowBo createCemiEXTRACTNAMEFileTABNAMETabRowBo() {
//        Validate.validState(award != null, "Award cannot be null.");
//        Validate.validState(awardExtendedAttribute != null, "AwardExtension cannot be null.");
//        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null.");
//        Validate.validState(dateTimeService != null, "DateTimeService cannot be null.");
//        
//        final CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabDataRow = new CemiAwardScheduleFileAwardScheduleTabRowBo();
          final CemiEXTRACTNAMEFileTABNAMETabRowBo extractNameTabDataRow = new CemiEXTRACTNAMEFileTABNAMETabRowBo();
//        
//        final String rowSpreadsheetKey = buildSpreadsheetKey(award.getProposalNumber());
//        final String rowAwardScheduleReferenceId = buildAwardScheduleReferenceId(award.getProposalNumber());
//        final String rowAwardScheduleName = determineAwardScheduleName(award.getAwardProjectTitle());
//        final String rowAwardPeriodReferenceId = buildAwardPeriodReferenceId(award.getProposalNumber());
//        final String rowAwardPostingIntervalId = buildAwardPostingIntervalId(award.getProposalNumber());
//        final String rowAwardIntervalStartDate = determineFormattedDate(awardExtendedAttribute.getBudgetBeginningDate());
//        final String rowAwardIntervalEndDate = determineFormattedDate(awardExtendedAttribute.getBudgetEndingDate());
//        
//        // Reference information related to business object being created that must be specified.
//        //      attribute runRowIndex is being set by abstract class CemiIndexedBusinessObjectBase.
//        awardScheduleTabDataRow.setJobRunDateString(jobRunDateString);
//        awardScheduleTabDataRow.setProposalNumberUsedForDataRow(award.getProposalNumber());
//        
//        //Format and assign data values for these attributes as defined by the Huron mapping template specification.
//        awardScheduleTabDataRow.setSpreadsheetKey(rowSpreadsheetKey);
//        awardScheduleTabDataRow.setAddOnly(CemiBaseConstants.YES);
//        awardScheduleTabDataRow.setAwardSchedule(CemiBaseConstants.EMPTY_STRING);
//        awardScheduleTabDataRow.setAwardScheduleReferenceId(rowAwardScheduleReferenceId);
//        awardScheduleTabDataRow.setAwardScheduleName(rowAwardScheduleName);
//        awardScheduleTabDataRow.setAwardPostingIntervalGroup(CemiAwardScheduleConstants.BUDGET_PERIOD);
//        awardScheduleTabDataRow.setAwardPeriodDataRowId(CemiAwardScheduleConstants.NUMERIC_ONE);
//        awardScheduleTabDataRow.setAwardPeriodReferenceId(rowAwardPeriodReferenceId);
//        awardScheduleTabDataRow.setAwardPeriodName(CemiAwardScheduleConstants.CINV_PERIOD);
//        awardScheduleTabDataRow.setAwardPeriodNumber(CemiAwardScheduleConstants.NUMERIC_ONE);
//        awardScheduleTabDataRow.setAwardIntervalRowId(CemiAwardScheduleConstants.NUMERIC_ONE);
//        awardScheduleTabDataRow.setAwardPostingInterval(CemiBaseConstants.EMPTY_STRING);
//        awardScheduleTabDataRow.setAwardPostingIntervalId(rowAwardPostingIntervalId);
//        awardScheduleTabDataRow.setAwardPostingIntervalName(CemiAwardScheduleConstants.AWARD_PERIOD);
//        awardScheduleTabDataRow.setAwardIntervalStartDate(rowAwardIntervalStartDate);
//        awardScheduleTabDataRow.setAwardIntervalEndDate(rowAwardIntervalEndDate);
//        awardScheduleTabDataRow.setIsAwardContractStartDate(CemiBaseConstants.YES);
//        awardScheduleTabDataRow.setIsAwardContractEndDate(CemiBaseConstants.YES);
//        
        return extractNameTabDataRow;
    }

//
// All of these methods are examples of data value conversion routines.
//    
//    private static String buildSpreadsheetKey(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
//    }
//    
//    private static String buildAwardPeriodReferenceId(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_PERIOD_REFERENCE_ID_FORMAT, awardProposalNumber);
//    }
//    
//    private static String buildAwardPostingIntervalId(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_POSTING_INTERVAL_ID_FORMAT, awardProposalNumber);
//    }
//    
//    private String determineAwardScheduleName(String awardScheduleName) {
//        return StringUtils.isNotBlank(awardScheduleName) ? awardScheduleName : KFSConstants.EMPTY_STRING;
//    }
//    
//    private String determineFormattedDate(Date dateToFormat) {
//        return ObjectUtils.isNotNull(dateToFormat)
//                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
//                        : KFSConstants.EMPTY_STRING;
//    }
//    
//    private static String buildAwardScheduleReferenceId(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_SCHEDULE_REFERENCE_ID_FORMAT, awardProposalNumber);
//    }
    
}
