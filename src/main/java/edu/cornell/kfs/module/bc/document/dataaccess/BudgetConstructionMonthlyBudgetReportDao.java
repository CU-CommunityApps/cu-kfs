package edu.cornell.kfs.module.bc.document.dataaccess;

import java.util.Collection;

import edu.cornell.kfs.module.bc.businessobject.MonthlyBudgetReportLine;

public interface BudgetConstructionMonthlyBudgetReportDao {

    /**
     * Builds a collection of records for the monthly budget report.
     * 
     * @param universalId the principal id of the user generating the report
     * @return a collection of records for the monthly budget report
     */
    public Collection<MonthlyBudgetReportLine> getMonthlyBudgetReportLines(String universalId);

}
