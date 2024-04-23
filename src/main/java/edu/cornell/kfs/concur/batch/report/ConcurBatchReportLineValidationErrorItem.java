package edu.cornell.kfs.concur.batch.report;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

public class ConcurBatchReportLineValidationErrorItem {
    private String lineId;
    private String reportId;
    private String employeeId;
    private String lastName;
    private String firstName;
    private String middleInitial;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String subAccountNumber;
    private String objectCode;
    private String subObjectCode;
    private String projectCode;
    private String orgRefId;
    private KualiDecimal lineAmount;
    private List<String> itemErrorResults;
    private boolean reportableAsLineLevelValidationError = true;
    
    public ConcurBatchReportLineValidationErrorItem() {
        this.lineId = KFSConstants.EMPTY_STRING;
        this.reportId = KFSConstants.EMPTY_STRING;
        this.employeeId = KFSConstants.EMPTY_STRING;
        this.lastName = KFSConstants.EMPTY_STRING;
        this.firstName = KFSConstants.EMPTY_STRING;
        this.middleInitial = KFSConstants.EMPTY_STRING;
        this.chartOfAccountsCode = KFSConstants.EMPTY_STRING;
        this.accountNumber = KFSConstants.EMPTY_STRING;
        this.subAccountNumber = KFSConstants.EMPTY_STRING;
        this.objectCode = KFSConstants.EMPTY_STRING;
        this.subObjectCode = KFSConstants.EMPTY_STRING;
        this.projectCode = KFSConstants.EMPTY_STRING;
        this.orgRefId = KFSConstants.EMPTY_STRING;
        this.lineAmount = KualiDecimal.ZERO;
        this.itemErrorResults = new ArrayList<String>();
    }

    public ConcurBatchReportLineValidationErrorItem(String reportId, String employeeId, String lastName, String firstName, String middleInitial, List<String> itemErrorResults) {
        this(KFSConstants.EMPTY_STRING, reportId, employeeId, lastName, firstName, middleInitial, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING,
                KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING,
                KualiDecimal.ZERO, itemErrorResults);
    }

    public ConcurBatchReportLineValidationErrorItem(String reportId, String employeeId, String lastName, String firstName, String middleInitial, String itemErrorResult) {
        this(KFSConstants.EMPTY_STRING, reportId, employeeId, lastName, firstName, middleInitial, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING,
                KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING,
                KualiDecimal.ZERO, itemErrorResult);
    }

    public ConcurBatchReportLineValidationErrorItem(ConcurStandardAccountingExtractDetailLine saeLine, String itemErrorResult) {
        this(saeLine.getSequenceNumber(), saeLine.getReportId(), saeLine.getEmployeeId(), saeLine.getEmployeeLastName(), saeLine.getEmployeeFirstName(),
                saeLine.getEmployeeMiddleInitial(), saeLine.getChartOfAccountsCode(), saeLine.getAccountNumber(), saeLine.getSubAccountNumber(),
                saeLine.getJournalAccountCode(), saeLine.getSubObjectCode(), saeLine.getProjectCode(), saeLine.getOrgRefId(),
                saeLine.getJournalAmount(), itemErrorResult);
    }

    public ConcurBatchReportLineValidationErrorItem(ConcurStandardAccountingExtractDetailLine saeLine, String itemErrorResult, boolean reportableAsLineLevelValidationError) {
        this(saeLine.getSequenceNumber(), saeLine.getReportId(), saeLine.getEmployeeId(), saeLine.getEmployeeLastName(), saeLine.getEmployeeFirstName(),
                saeLine.getEmployeeMiddleInitial(), saeLine.getChartOfAccountsCode(), saeLine.getAccountNumber(), saeLine.getSubAccountNumber(),
                saeLine.getJournalAccountCode(), saeLine.getSubObjectCode(), saeLine.getProjectCode(), saeLine.getOrgRefId(),
                saeLine.getJournalAmount(), itemErrorResult);
        this.reportableAsLineLevelValidationError = reportableAsLineLevelValidationError;
    }

    public ConcurBatchReportLineValidationErrorItem(ConcurStandardAccountingExtractDetailLine saeLine, List<String> itemErrorResults) {
        this(saeLine.getSequenceNumber(), saeLine.getReportId(), saeLine.getEmployeeId(), saeLine.getEmployeeLastName(), saeLine.getEmployeeFirstName(),
                saeLine.getEmployeeMiddleInitial(), saeLine.getChartOfAccountsCode(), saeLine.getAccountNumber(), saeLine.getSubAccountNumber(),
                saeLine.getJournalAccountCode(), saeLine.getSubObjectCode(), saeLine.getProjectCode(), saeLine.getOrgRefId(),
                saeLine.getJournalAmount(), itemErrorResults);
    }

    public ConcurBatchReportLineValidationErrorItem(String lineId, String reportId, String employeeId, String lastName, String firstName, String middleInitial,
            String chartOfAccountsCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode,
            String projectCode, String orgRefId, KualiDecimal lineAmount, String itemErrorResult) {
        this(lineId, reportId, employeeId, lastName, firstName, middleInitial, chartOfAccountsCode, accountNumber, subAccountNumber,
                objectCode, subObjectCode, projectCode, orgRefId, lineAmount, new ArrayList<>());
        addItemErrorResult(itemErrorResult);
    }

    public ConcurBatchReportLineValidationErrorItem(String lineId, String reportId, String employeeId, String lastName, String firstName, String middleInitial,
            String chartOfAccountsCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode,
            String projectCode, String orgRefId, KualiDecimal lineAmount, List<String> itemErrorResults) {
        this.lineId = lineId;
        this.reportId = reportId;
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.objectCode = objectCode;
        this.subObjectCode = subObjectCode;
        this.projectCode = projectCode;
        this.orgRefId = orgRefId;
        this.lineAmount = lineAmount;
        this.itemErrorResults = itemErrorResults;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public String getSubObjectCode() {
        return subObjectCode;
    }

    public void setSubObjectCode(String subObjectCode) {
        this.subObjectCode = subObjectCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getOrgRefId() {
        return orgRefId;
    }

    public void setOrgRefId(String orgRefId) {
        this.orgRefId = orgRefId;
    }

    public KualiDecimal getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(KualiDecimal lineAmount) {
        this.lineAmount = lineAmount;
    }

    public List<String> getItemErrorResults() {
        return itemErrorResults;
    }

    public void setItemErrorResults(List<String> itemErrorResults) {
        this.itemErrorResults = itemErrorResults;
    }
    
    public void addItemErrorResult(String itemErrorResult) {
        if (itemErrorResults == null) {
            itemErrorResults = new ArrayList<String>();
        }
        this.itemErrorResults.add(itemErrorResult);
    }

    public boolean getReportableAsLineLevelValidationError() {
        return reportableAsLineLevelValidationError;
    }

    public void setReportableAsLineLevelValidationError(boolean reportableAsLineLevelValidationError) {
        this.reportableAsLineLevelValidationError = reportableAsLineLevelValidationError;
    }

}
