package edu.cornell.kfs.concur.batch.report;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public class ConcurBatchReportLineValidationErrorItem {
    private String reportId;
    private String employeeId;
    private String lastName;
    private String firstName;
    private String middleInitial;
    List<ValidationResult> itemErrorResults;
    
    public ConcurBatchReportLineValidationErrorItem () {
        this.reportId = KFSConstants.EMPTY_STRING;
        this.employeeId = KFSConstants.EMPTY_STRING;
        this.lastName = KFSConstants.EMPTY_STRING;
        this.firstName = KFSConstants.EMPTY_STRING;
        this.middleInitial = KFSConstants.EMPTY_STRING;
        this.itemErrorResults = new ArrayList<ValidationResult>();
    }
    
    public ConcurBatchReportLineValidationErrorItem (String reportId, String employeeId, String lastName, String firstName, String middleInitial, List<ValidationResult> itemErrorResults) {
        this.reportId = reportId;
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.itemErrorResults = itemErrorResults;
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

    public List<ValidationResult> getItemErrorResults() {
        return itemErrorResults;
    }

    public void setItemErrorResults(List<ValidationResult> itemErrorResults) {
        this.itemErrorResults = itemErrorResults;
    }
    
    public void addItemErrorResult(ValidationResult itemErrorResult) {
        if (itemErrorResults == null) {
            itemErrorResults = new ArrayList<ValidationResult>();
        }
        this.itemErrorResults.add(itemErrorResult);
    }
    
}
