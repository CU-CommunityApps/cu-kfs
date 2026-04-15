package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import edu.cornell.kfs.cemi.module.cg.batch.dto.CemiAwardSchedule;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiAwardScheduleBo extends PersistableBusinessObjectBase implements Serializable {
    
    private static final long serialVersionUID = -7478232634840096057L;
    
    private Long extractTableUniqueRowId;
    private String jobRunDateAsString;
    private String proposalNumberUsedForDataRow; 
    
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
    private String testingDummyValue;
    
    public CemiAwardScheduleBo (CemiAwardSchedule cemiAwardScheduleDataRow, String proposalNumberForScheduleRow,
            LocalDateTime jobRunDate, CemiAwardScheduleBoSequence awardScheduleTabTableSequence) {
        
        // These values are to make the row that would appear in an extract spreadsheet seachable as well as
        // identifiable by the actual KFS data object keys used to create that row and date-time of the extract file.
        this.extractTableUniqueRowId = awardScheduleTabTableSequence.getLongValue();
        this.jobRunDateAsString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        this.proposalNumberUsedForDataRow = proposalNumberForScheduleRow;
        
        // These table data values should be the same as what would be in the extract file tabbed sheet columns.
        this.spreadsheetKey = cemiAwardScheduleDataRow.getSpreadsheetKey();
        this.addOnly = cemiAwardScheduleDataRow.getAddOnly();
        this.awardSchedule = cemiAwardScheduleDataRow .getAwardSchedule();
        this.awardScheduleReferenceId = cemiAwardScheduleDataRow.getAwardScheduleReferenceId();
        this.awardScheduleName = cemiAwardScheduleDataRow.getAwardScheduleName();
        this.awardPostingIntervalGroup = cemiAwardScheduleDataRow.getAwardPostingIntervalGroup();
        this.awardPeriodDataRowId = cemiAwardScheduleDataRow.getAwardPeriodDataRowId();
        this.awardPeriodReferenceId = cemiAwardScheduleDataRow.getAwardPeriodReferenceId();
        this.awardPeriodName = cemiAwardScheduleDataRow.getAwardPeriodName();
        this.awardPeriodNumber = cemiAwardScheduleDataRow.getAwardPeriodNumber();
        this.awardIntervalRowId = cemiAwardScheduleDataRow.getAwardIntervalRowId();
        this.awardPostingInterval = cemiAwardScheduleDataRow.getAwardPostingInterval();
        this.awardPostingIntervalId = cemiAwardScheduleDataRow.getAwardPostingIntervalId();
        this.awardPostingIntervalName = cemiAwardScheduleDataRow.getAwardPostingIntervalName();
        this.awardIntervalStartDate = cemiAwardScheduleDataRow.getAwardIntervalStartDate();
        this.awardIntervalEndDate = cemiAwardScheduleDataRow.getAwardIntervalEndDate();
        this.isAwardContractStartDate = cemiAwardScheduleDataRow.getIsAwardContractStartDate();
        this.isAwardContractEndDate = cemiAwardScheduleDataRow.getIsAwardContractEndDate();
    }

    public Long getExtractTableUniqueRowId() {
        return extractTableUniqueRowId;
    }

    public void setExtractTableUniqueRowId(Long extractTableUniqueRowId) {
        this.extractTableUniqueRowId = extractTableUniqueRowId;
    }

    public String getJobRunDateAsString() {
        return jobRunDateAsString;
    }

    public void setJobRunDateAsString(String jobRunDateAsString) {
        this.jobRunDateAsString = jobRunDateAsString;
    }

    public String getProposalNumberUsedForDataRow() {
        return proposalNumberUsedForDataRow;
    }

    public void setProposalNumberUsedForDataRow(String proposalNumberUsedForDataRow) {
        this.proposalNumberUsedForDataRow = proposalNumberUsedForDataRow;
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

    public String getTestingDummyValue() {
        return testingDummyValue;
    }

    public void setTestingDummyValue(String testingDummyValue) {
        this.testingDummyValue = testingDummyValue;
    }

}
