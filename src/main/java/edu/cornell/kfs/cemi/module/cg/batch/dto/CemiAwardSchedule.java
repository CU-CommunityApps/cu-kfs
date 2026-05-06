package edu.cornell.kfs.cemi.module.cg.batch.dto;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.CemiCGConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@SuppressWarnings("deprecation")
public class CemiAwardSchedule {
    
    private Award award;
    private AwardExtendedAttribute awardExtendedAttribute; 
    private String spreadsheetKey;
    private String addOnly;
    private String awardSchedule;
    private String awardScheduleReferenceId;
    private String awardScheduleName;
    private String awardPostingIntervalGroup;
    private String awardPeriodDataRowId;        //template field name duplicated as rowId
    private String awardPeriodReferenceId;
    private String awardPeriodName;
    private String awardPeriodNumber;
    private String awardIntervalRowId;         //template field name duplicated as rowId
    private String awardPostingInterval;
    private String awardPostingIntervalId;
    private String awardPostingIntervalName;
    private String awardIntervalStartDate;
    private String awardIntervalEndDate;
    private String isAwardContractStartDate;
    private String isAwardContractEndDate;
    
    private DateTimeService dateTimeService;
    
    public CemiAwardSchedule (final Award award, String spreadsheetKey, String awardScheduleReferenceId,
            String awardIntervalStartDate, String awardIntervalEndDate, boolean maskSensitiveData) {
        this.award = award;
        this.spreadsheetKey = spreadsheetKey;
        this.addOnly = CemiBaseConstants.YES;
        this.awardSchedule = CemiBaseConstants.EMPTY_STRING;
        this.awardScheduleReferenceId = awardScheduleReferenceId;
        this.awardScheduleName = determineAwardScheduleName(award.getAwardProjectTitle());
        this.awardPostingIntervalGroup = CemiCGConstants.BUDGET_PERIOD;
        this.awardPeriodDataRowId = CemiCGConstants.NUMERIC_ONE;
        this.awardPeriodReferenceId = buildAwardPeriodReferenceId(award.getProposalNumber());
        this.awardPeriodName = CemiCGConstants.CINV_PERIOD;
        this.awardPeriodNumber = CemiCGConstants.NUMERIC_ONE;
        this.awardIntervalRowId = CemiCGConstants.NUMERIC_ONE;
        this.awardPostingInterval = CemiBaseConstants.EMPTY_STRING;
        this.awardPostingIntervalId = buildAwardPostingIntervalId(award.getProposalNumber());
        this.awardPostingIntervalName = CemiCGConstants.BUDGET_PERIOD;
        this.awardIntervalStartDate = awardIntervalStartDate;
        this.awardIntervalEndDate = awardIntervalEndDate;
        this.isAwardContractStartDate = CemiBaseConstants.YES;
        this.isAwardContractEndDate = CemiBaseConstants.YES;
    }
    
    private static String buildAwardPeriodReferenceId(final String awardProposalNumber) {
        return MessageFormat.format(CemiCGConstants.AWARD_PERIOD_REFERENCE_ID_FORMAT, awardProposalNumber);
    }
    
    private static String buildAwardPostingIntervalId(final String awardProposalNumber) {
        return MessageFormat.format(CemiCGConstants.AWARD_POSTING_INTERVAL_ID_FORMAT, awardProposalNumber);
    }
    
    private String determineAwardScheduleName(String awardScheduleName) {
        return StringUtils.isNotBlank(awardScheduleName) ? awardScheduleName : KFSConstants.EMPTY_STRING;
    }

    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    public AwardExtendedAttribute getAwardExtendedAttribute() {
        return awardExtendedAttribute;
    }

    public void setAwardExtendedAttribute(AwardExtendedAttribute awardExtendedAttribute) {
        this.awardExtendedAttribute = awardExtendedAttribute;
    }

    public String getSpreadsheetKey() {
        return spreadsheetKey;
    }

    public void setSpreadsheetKey(String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
    }

    public String getAddOnly() {
        return addOnly;
    }

    public void setAddOnly(String addOnly) {
        this.addOnly = addOnly;
    }

    public String getAwardSchedule() {
        return awardSchedule;
    }

    public void setAwardSchedule(String awardSchedule) {
        this.awardSchedule = awardSchedule;
    }

    public String getAwardScheduleReferenceId() {
        return awardScheduleReferenceId;
    }

    public void setAwardScheduleReferenceId(String awardScheduleReferenceId) {
        this.awardScheduleReferenceId = awardScheduleReferenceId;
    }

    public String getAwardScheduleName() {
        return awardScheduleName;
    }

    public void setAwardScheduleName(String awardScheduleName) {
        this.awardScheduleName = awardScheduleName;
    }

    public String getAwardPostingIntervalGroup() {
        return awardPostingIntervalGroup;
    }

    public void setAwardPostingIntervalGroup(String awardPostingIntervalGroup) {
        this.awardPostingIntervalGroup = awardPostingIntervalGroup;
    }

    public String getAwardPeriodDataRowId() {
        return awardPeriodDataRowId;
    }

    public void setAwardPeriodDataRowId(String awardPeriodDataRowId) {
        this.awardPeriodDataRowId = awardPeriodDataRowId;
    }

    public String getAwardPeriodReferenceId() {
        return awardPeriodReferenceId;
    }

    public void setAwardPeriodReferenceId(String awardPeriodReferenceId) {
        this.awardPeriodReferenceId = awardPeriodReferenceId;
    }

    public String getAwardPeriodName() {
        return awardPeriodName;
    }

    public void setAwardPeriodName(String awardPeriodName) {
        this.awardPeriodName = awardPeriodName;
    }

    public String getAwardPeriodNumber() {
        return awardPeriodNumber;
    }

    public void setAwardPeriodNumber(String awardPeriodNumber) {
        this.awardPeriodNumber = awardPeriodNumber;
    }

    public String getAwardIntervalRowId() {
        return awardIntervalRowId;
    }

    public void setAwardIntervalRowId(String awardIntervalRowId) {
        this.awardIntervalRowId = awardIntervalRowId;
    }

    public String getAwardPostingInterval() {
        return awardPostingInterval;
    }

    public void setAwardPostingInterval(String awardPostingInterval) {
        this.awardPostingInterval = awardPostingInterval;
    }

    public String getAwardPostingIntervalId() {
        return awardPostingIntervalId;
    }

    public void setAwardPostingIntervalId(String awardPostingIntervalId) {
        this.awardPostingIntervalId = awardPostingIntervalId;
    }

    public String getAwardPostingIntervalName() {
        return awardPostingIntervalName;
    }

    public void setAwardPostingIntervalName(String awardPostingIntervalName) {
        this.awardPostingIntervalName = awardPostingIntervalName;
    }

    public String getAwardIntervalStartDate() {
        return awardIntervalStartDate;
    }

    public void setAwardIntervalStartDate(String awardIntervalStartDate) {
        this.awardIntervalStartDate = awardIntervalStartDate;
    }

    public String getAwardIntervalEndDate() {
        return awardIntervalEndDate;
    }

    public void setAwardIntervalEndDate(String awardIntervalEndDate) {
        this.awardIntervalEndDate = awardIntervalEndDate;
    }

    public String getIsAwardContractStartDate() {
        return isAwardContractStartDate;
    }

    public void setIsAwardContractStartDate(String isAwardContractStartDate) {
        this.isAwardContractStartDate = isAwardContractStartDate;
    }

    public String getIsAwardContractEndDate() {
        return isAwardContractEndDate;
    }

    public void setIsAwardContractEndDate(String isAwardContractEndDate) {
        this.isAwardContractEndDate = isAwardContractEndDate;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
    
}