package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiImportBudgetFileImportBudgetHighVolumeTabRowBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = -3812506879321495012L;

    private String headerKey;
    private String budget;
    private String importMode;
    private String autoComplete;
    private String comment;
    private String worker;
    private String submit;
    private String budgetStructure;
    private String budgetName;
    private String budgetMemo;
    private String budgetPeriod;

    public String getHeaderKey() { return headerKey; }
    public void setHeaderKey(final String headerKey) { this.headerKey = headerKey; }

    public String getBudget() { return budget; }
    public void setBudget(final String budget) { this.budget = budget; }

    public String getImportMode() { return importMode; }
    public void setImportMode(final String importMode) { this.importMode = importMode; }

    public String getAutoComplete() { return autoComplete; }
    public void setAutoComplete(final String autoComplete) { this.autoComplete = autoComplete; }

    public String getComment() { return comment; }
    public void setComment(final String comment) { this.comment = comment; }

    public String getWorker() { return worker; }
    public void setWorker(final String worker) { this.worker = worker; }

    public String getSubmit() { return submit; }
    public void setSubmit(final String submit) { this.submit = submit; }

    public String getBudgetStructure() { return budgetStructure; }
    public void setBudgetStructure(final String budgetStructure) { this.budgetStructure = budgetStructure; }

    public String getBudgetName() { return budgetName; }
    public void setBudgetName(final String budgetName) { this.budgetName = budgetName; }

    public String getBudgetMemo() { return budgetMemo; }
    public void setBudgetMemo(final String budgetMemo) { this.budgetMemo = budgetMemo; }

    public String getBudgetPeriod() { return budgetPeriod; }
    public void setBudgetPeriod(final String budgetPeriod) { this.budgetPeriod = budgetPeriod; }

}
