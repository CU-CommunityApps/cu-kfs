package edu.cornell.kfs.concur.batch.report;

import java.util.List;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

public class ConcurBatchReportMissingObjectCodeItem extends ConcurBatchReportLineValidationErrorItem {
    private String policyName;
    private String expenseTypeName;

    public ConcurBatchReportMissingObjectCodeItem() {
        super();
        this.policyName = KFSConstants.EMPTY_STRING;
        this.expenseTypeName = KFSConstants.EMPTY_STRING;
    }

    public ConcurBatchReportMissingObjectCodeItem(ConcurStandardAccountingExtractDetailLine saeLine, List<String> itemErrorResults) {
        super(saeLine, itemErrorResults);
        this.policyName = saeLine.getPolicy();
        this.expenseTypeName = saeLine.getExpenseType();
    }

    public ConcurBatchReportMissingObjectCodeItem(ConcurStandardAccountingExtractDetailLine saeLine, String itemErrorResult) {
        super(saeLine, itemErrorResult);
        this.policyName = saeLine.getPolicy();
        this.expenseTypeName = saeLine.getExpenseType();
    }

    public ConcurBatchReportMissingObjectCodeItem(String lineId, String reportId, String employeeId, String lastName, String firstName, String middleInitial,
            String chartOfAccountsCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode,
            String projectCode, String orgRefId, KualiDecimal lineAmount, List<String> itemErrorResults, String policyName, String expenseTypeName) {
        super(reportId, employeeId, lastName, firstName, middleInitial, itemErrorResults);
        this.policyName = policyName;
        this.expenseTypeName = expenseTypeName;
    }

    public ConcurBatchReportMissingObjectCodeItem(String lineId, String reportId, String employeeId, String lastName, String firstName, String middleInitial,
            String chartOfAccountsCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode,
            String projectCode, String orgRefId, KualiDecimal lineAmount, String itemErrorResult, String policyName, String expenseTypeName) {
        super(reportId, employeeId, lastName, firstName, middleInitial, itemErrorResult);
        this.policyName = policyName;
        this.expenseTypeName = expenseTypeName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getExpenseTypeName() {
        return expenseTypeName;
    }

    public void setExpenseTypeName(String expenseTypeName) {
        this.expenseTypeName = expenseTypeName;
    }

}
