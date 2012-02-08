package edu.cornell.kfs.module.bc.document.dataaccess;

import java.util.Collection;

import edu.cornell.kfs.module.bc.document.dataaccess.impl.BudgetConstructionBudgetedSalaryLineExportDaoJdbc.BSLExportData;

public interface BudgetConstructionBudgetedSalaryLineExportDao {

    /**
     * use this method to retrieve Budgeted Salary Line Export data
     * @param univId
     */
	
	// NOTE: The executivesOnly parameter is not implemented but is there for potential future need
    public Collection<BSLExportData>  getBSLExtractByPersonUnivId(String univId);
}
